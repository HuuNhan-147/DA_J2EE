# Node.js → Spring Boot Migration Guide

## Project Structure

This repository contains two backends:

```
/nodejs      -> Existing backend written in Node.js + Express + MongoDB
/springboot -> Target backend written in Java Spring Boot + MySQL
```

The goal of this migration is to **convert the logic from the Node.js backend into the Spring Boot backend** while preserving the same API behavior.

---

# Technology Stack

## Source Backend

* Node.js
* Express.js
* MongoDB
* Mongoose
* JWT Authentication
* bcrypt password hashing

## Target Backend

* Java
* Spring Boot
* Spring Data JPA
* MySQL
* Spring Security
* JWT authentication

---

# Migration Rules

The AI agent must follow these rules when converting code.

## 1. API Endpoints Must Stay The Same

Example:

Node.js route

```
GET /api/products
GET /api/products/:id
POST /api/products
```

Spring Boot equivalent

```
GET /api/products
GET /api/products/{id}
POST /api/products
```

Do not change endpoint paths.

---

# 2. Folder Mapping

Node.js folder structure:

```
node-backend
 ├── models
 ├── controllers
 ├── routes
 ├── middleware
 └── server.js
```

Spring Boot structure must be:

```
springboot-backend
 └── src/main/java/com/project
      ├── controller
      ├── service
      ├── repository
      ├── entity
      ├── dto
      └── security
```

Mapping rules:

| Node.js          | Spring Boot     |
| ---------------- | --------------- |
| models           | entity          |
| controllers      | controller      |
| routes           | controller      |
| middleware       | security/filter |
| business logic   | service         |
| mongoose queries | JPA repository  |

---

# 3. MongoDB → MySQL Conversion

MongoDB collections must be converted to relational tables.

Collections:

* User
* Category
* Product
* Review
* ProductSpecification
* Cart
* CartItem
* Order
* OrderItem
* OrderShippingAddress

Each MongoDB document must become a **JPA Entity**.

Relationships must be converted:

Mongo reference

```
ref: "User"
```

becomes

```
@ManyToOne
@JoinColumn(name = "user_id")
```

Mongo arrays must become **separate tables**.

Example:

```
reviews: []
```

becomes

```
reviews table
```

---

# 4. Mongoose Model → JPA Entity

Example conversion.

Node.js:

```
const ProductSchema = new mongoose.Schema({
  name: String,
  price: Number,
  category: { type: ObjectId, ref: "Category" }
})
```

Spring Boot:

```
@Entity
@Table(name="products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Double price;

    @ManyToOne
    @JoinColumn(name="category_id")
    private Category category;
}
```

---

# 5. Controller Conversion

Node.js controller:

```
exports.getProducts = async (req,res)=>{
   const products = await Product.find()
   res.json(products)
}
```

Spring Boot controller:

```
@GetMapping("/products")
public List<Product> getProducts(){
    return productService.getAllProducts();
}
```

Controllers must only handle HTTP requests.

Business logic must be moved into **Service classes**.

---

# 6. Service Layer

Each entity must have a service.

Example:

```
ProductService
UserService
CartService
OrderService
```

Services contain business logic.

---

# 7. Repository Layer

Use Spring Data JPA repositories.

Example:

```
public interface ProductRepository extends JpaRepository<Product,Long> {}
```

---

# 8. Authentication

Node.js uses:

* jsonwebtoken
* bcrypt

Spring Boot must use:

* Spring Security
* JWT filter
* BCryptPasswordEncoder

Password hashing must remain compatible.

---

# 9. DTO Usage

DTOs must be created for:

* Login
* Register
* Product creation
* Order creation

Entities should not be exposed directly if sensitive fields exist.

---

# 10. Application Configuration

Database configuration must be placed in:

```
application.properties
```

Example:

```
spring.datasource.url=jdbc:mysql://localhost:3306/da_j2ee
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
```

---

# 11. Testing

All APIs should be testable using Postman.

Base URL:

```
http://localhost:8080/api
```

---

# Final Goal

The Spring Boot backend must fully replace the Node.js backend with identical functionality.

Required modules:

* Authentication
* Product management
* Category management
* Cart
* Order
* Reviews
* VNPay payment support

The final backend must compile and run successfully.
