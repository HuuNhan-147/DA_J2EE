package com.example.DACK_J2EE.controller;

import com.example.DACK_J2EE.config.VnpayConfig;
import com.example.DACK_J2EE.entity.Order;
import com.example.DACK_J2EE.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/vnpay")
@CrossOrigin(origins = "http://localhost:5173")
public class VnpayController {

    @Autowired
    private VnpayConfig vnpayConfig;

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<?> createPayment(HttpServletRequest request, @RequestBody Map<String, Object> payload) {
        try {
            Long orderId = Long.valueOf(payload.get("orderId").toString());
            String bankCode = payload.containsKey("bankCode") ? payload.get("bankCode").toString() : null;
            String locale = payload.containsKey("language") ? payload.get("language").toString() : "vn";

            Order order = orderService.getOrderById(orderId);
            if (order == null) {
                return ResponseEntity.status(404).body(Map.of("status", "error", "message", "Đơn hàng không tồn tại!"));
            }

            long amount = Math.round(order.getTotalPrice() * 100);

            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", "2.1.0");
            vnp_Params.put("vnp_Command", "pay");
            vnp_Params.put("vnp_TmnCode", vnpayConfig.getVnp_TmnCode());
            vnp_Params.put("vnp_Amount", String.valueOf(amount));
            vnp_Params.put("vnp_CurrCode", "VND");
            
            if (bankCode != null && !bankCode.isEmpty()) {
                vnp_Params.put("vnp_BankCode", bankCode);
            }
            
            vnp_Params.put("vnp_TxnRef", String.valueOf(orderId));
            vnp_Params.put("vnp_OrderInfo", "Thanh toan cho ma GD: " + orderId);
            vnp_Params.put("vnp_OrderType", "other");
            vnp_Params.put("vnp_Locale", locale);
            vnp_Params.put("vnp_ReturnUrl", vnpayConfig.getVnp_ReturnUrl());
            
            // Get IP Address
            String ipAddr = request.getHeader("X-FORWARDED-FOR");
            if (ipAddr == null || "".equals(ipAddr)) {
                ipAddr = request.getRemoteAddr();
            }
            vnp_Params.put("vnp_IpAddr", ipAddr);

            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnp_CreateDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
            
            cld.add(Calendar.MINUTE, 15);
            String vnp_ExpireDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnp_Params.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    //Build hash data
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    //Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }

            String queryUrl = query.toString();
            String vnp_SecureHash = VnpayConfig.hmacSHA512(vnpayConfig.getVnp_HashSecret(), hashData.toString());
            queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
            String paymentUrl = vnpayConfig.getVnp_Url() + "?" + queryUrl;

            return ResponseEntity.ok(paymentUrl);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @GetMapping("/return")
    public ResponseEntity<?> vnpayReturn(HttpServletRequest request) {
        try {
            Map<String, String> fields = new HashMap<>();
            
            for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
                String fieldName = params.nextElement();
                String fieldValue = request.getParameter(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    fields.put(fieldName, fieldValue);
                }
            }

            String vnp_SecureHash = request.getParameter("vnp_SecureHash");
            if (fields.containsKey("vnp_SecureHashType")) {
                fields.remove("vnp_SecureHashType");
            }
            if (fields.containsKey("vnp_SecureHash")) {
                fields.remove("vnp_SecureHash");
            }

            String signValue = VnpayConfig.hashAllFields(fields, vnpayConfig.getVnp_HashSecret());

            if (signValue.equals(vnp_SecureHash)) {
                String orderIdStr = request.getParameter("vnp_TxnRef");
                String responseCode = request.getParameter("vnp_ResponseCode");
                String transactionId = request.getParameter("vnp_TransactionNo");
                String amountStr = request.getParameter("vnp_Amount");
                
                Long orderId = Long.valueOf(orderIdStr);
                double amount = Double.parseDouble(amountStr) / 100;

                // Update order in DB
                Order updatedOrder = orderService.processVnpayReturn(orderId, transactionId, responseCode);

                if ("00".equals(responseCode)) {
                    return ResponseEntity.ok(Map.of(
                            "code", "SUCCESS",
                            "message", "Payment completed",
                            "data", Map.of(
                                    "orderId", updatedOrder.getId(),
                                    "amountPaid", amount,
                                    "transactionId", transactionId,
                                    "paymentStatus", updatedOrder.getPaymentStatus()
                            )
                    ));
                } else {
                    return ResponseEntity.status(400).body(Map.of(
                            "code", "PAYMENT_FAILED",
                            "message", "Payment failed (Code: " + responseCode + ")",
                            "data", Map.of(
                                    "orderId", updatedOrder.getId(),
                                    "errorCode", responseCode
                            )
                    ));
                }

            } else {
                return ResponseEntity.status(400).body(Map.of(
                        "code", "INVALID_SIGNATURE",
                        "message", "Invalid checksum",
                        "data", fields
                ));
            }

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "code", "SERVER_ERROR",
                    "message", "Payment processing failed",
                    "error", e.getMessage()
            ));
        }
    }
}
