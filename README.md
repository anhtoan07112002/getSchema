# GetScheme

GetScheme là một ứng dụng web giúp kết nối và quản lý cơ sở dữ liệu, tạo schema markdown và tương tác với AI.

## Tính năng chính

- Kết nối với nhiều loại database:
  - MySQL
  - PostgreSQL
  - SQL Server
- Xem thông tin schema
- Tạo schema markdown
- Tương tác với AI để hỏi về schema
- Quản lý kết nối database

## Công nghệ sử dụng

- Backend:
  - Java 17
  - Spring Boot
  - Spring Security
  - Spring JDBC
  - Thymeleaf

- Frontend:
  - HTML/CSS
  - JavaScript
  - Bootstrap 5
  - Font Awesome

## Cài đặt và chạy

1. Clone repository:
```bash
git clone [repository-url]
```

2. Cài đặt dependencies:
```bash
mvn clean install
```

3. Chạy ứng dụng:
```bash
mvn spring-boot:run
```

4. Truy cập ứng dụng:
```
http://localhost:8080
```

## Cấu trúc dự án

```
src/main/java/com/flowiseai/getscheme/
├── config/         # Cấu hình ứng dụng
├── controller/     # Xử lý request
├── dto/           # Data Transfer Objects
├── exception/     # Xử lý lỗi
├── model/         # Model classes
├── mysql/         # MySQL implementation
├── postgresql/    # PostgreSQL implementation
├── repository/    # Data access
├── security/      # Security configuration
├── service/       # Business logic
└── sqlserver/     # SQL Server implementation
```

## Hướng dẫn sử dụng

1. Đăng nhập vào hệ thống
2. Chọn "Kết nối Database mới"
3. Nhập thông tin kết nối
4. Chọn các bảng cần xem
5. Xem schema và tương tác với AI
