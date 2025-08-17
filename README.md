# Pay-Later Service

A Java-based microservice application built with Spring Boot and MongoDB, designed to provide deferred payment functionality for e-commerce platforms.

## 🚀 Current Status

**Phase 1 Complete**: MongoDB domain models and repositories implemented
- ✅ Domain models with Bean Validation
- ✅ Reactive MongoDB repositories
- ✅ MongoDB configuration
- ✅ Project structure setup

## 🏗️ Architecture

- **Backend**: Spring Boot 3.2.0 + Spring WebFlux
- **Database**: MongoDB (Reactive)
- **Validation**: JSR-303 Bean Validation
- **Build Tool**: Maven
- **Java Version**: 17

## 📊 Domain Models

### Collections Implemented

1. **Users** - Customer information and credit limits
2. **Merchants** - Business partners and fee structures
3. **Transactions** - Payment transactions and status tracking
4. **Paybacks** - Customer repayment records

## 🛠️ Setup & Running

### Prerequisites
- Java 17 or higher
- Maven (for dependency management)
- MongoDB Atlas account (credentials configured)

### Configuration
- MongoDB connection: Configured in `application.yml`
- Database: `paylater_db`
- Test Database: `paylater_db_test`

### Running the Application
```bash
# Install dependencies
mvn clean install

# Run the application
mvn spring-boot:run
```

### Testing
```bash
# Run tests
mvn test
```

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/lazypay/
│   │   ├── domain/          # MongoDB entities
│   │   ├── repository/       # Reactive repositories
│   │   ├── config/          # Configuration classes
│   │   └── PayLaterServiceApplication.java
│   └── resources/
│       └── application.yml   # MongoDB configuration
└── test/
    ├── java/com/lazypay/     # Test classes
    └── resources/
        └── application-test.yml
```

## 🔄 Next Steps

- **Phase 2**: Implement REST API endpoints
- **Phase 3**: Add authentication and security
- **Phase 4**: Write integration tests
- **Phase 5**: Build React frontend

## 📝 Documentation

- `prompt.md` - Project phases and rules
- `log.md` - Development log and credentials
- `pom.xml` - Maven dependencies

## 🔐 MongoDB Credentials

- **URI**: Configured in application.yml
- **Database**: paylater_db
- **Test Database**: paylater_db_test

## 🧪 Testing

The application includes bootstrap tests that:
1. Insert sample user and merchant data
2. Test MongoDB connection
3. Verify data retrieval operations

Run with: `mvn test` or start the application to see bootstrap output.
