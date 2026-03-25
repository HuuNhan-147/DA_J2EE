package com.example.DACK_J2EE.repository;

import com.example.DACK_J2EE.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderCode(String orderCode);
    List<Order> findByUserId(Long userId);

    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.isPaid = true")
    Double getTotalRevenue();

    @Query(value = "SELECT DATE_FORMAT(paid_at, '%Y-%m') as month, SUM(total_price) as total_revenue " +
                   "FROM orders WHERE is_paid = true GROUP BY month ORDER BY month", nativeQuery = true)
    List<Object[]> getMonthlyRevenue();

    @Query(value = "SELECT p.id, p.name, p.image, SUM(oi.quantity) as total_sold " +
                   "FROM order_items oi JOIN orders o ON oi.order_id = o.id " +
                   "JOIN products p ON oi.product_id = p.id " +
                   "GROUP BY p.id, p.name, p.image ORDER BY total_sold DESC LIMIT 5", nativeQuery = true)
    List<Object[]> getTopSellingProducts();

    List<Order> findTop5ByOrderByCreatedAtDesc();

    @Query("SELECT " +
           "SUM(CASE WHEN o.isPaid = true THEN 1 ELSE 0 END) as paid, " +
           "SUM(CASE WHEN o.isDelivered = true THEN 1 ELSE 0 END) as delivered, " +
           "SUM(CASE WHEN o.isPaid = false THEN 1 ELSE 0 END) as unpaid, " +
           "SUM(CASE WHEN o.isDelivered = false THEN 1 ELSE 0 END) as undelivered, " +
           "COUNT(o) as total " +
           "FROM Order o")
    List<Object[]> getOrderStatusStats();
}
