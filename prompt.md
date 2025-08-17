# Pay-Later Service Project - Baseline Documentation

## Project Overview
**Pay-Later Service** is a Java-based microservice application built with Spring Boot and MongoDB, designed to provide deferred payment functionality for e-commerce platforms. The service allows customers to purchase items and pay for them later in installments.

## Architecture Approach
- **Modular Monolithic Architecture**: Services separated, reusable, and testable
- **Reactive Stack**: Spring WebFlux + Reactive MongoDB for non-blocking operations
- **Bean Validation**: JSR-303 validation for data models
- **Security**: Value-based authentication (API key or JWT)

## Phase Execution Plan

### Phase 0 – Architect (Current)
- **Goal**: Define baseline rules and project structure
- **Deliverables**: Project documentation, architecture decisions
- **Status**: ✅ In Progress

### Phase 1 – Database Engineer
- **Goal**: Design MongoDB collections and Java domain models
- **Technology**: Spring Data Reactive or Mongo Reactive Streams
- **Deliverables**: Entity models, repositories, database schemas
- **Status**: ✅ Complete

### Phase 2 – API Developer
- **Goal**: Implement REST endpoints using Spring WebFlux
- **Technology**: Controllers + service classes with modular segregation
- **Deliverables**: REST API endpoints, service layer, business logic
- **Status**: ⏳ Pending

### Phase 3 – Security Engineer
- **Goal**: Add value-based authentication and authorization
- **Technology**: API key or JWT implementation
- **Deliverables**: Security middleware, unauthorized request rejection
- **Status**: ⏳ Pending

### Phase 4 – QA Engineer
- **Goal**: Write comprehensive integration tests
- **Technology**: JUnit 5 + WebTestClient
- **Deliverables**: Test suite, test files moved to `/tests/`, cleanup
- **Status**: ⏳ Pending

### Phase 5 – Frontend Engineer
- **Goal**: Build React UI with backend integration
- **Technology**: React + axios + optional three.js visualizations
- **Deliverables**: User interface, API integration, visualizations
- **Status**: ⏳ Pending

## Core Rules Summary

### Development Rules
1. **Error Handling**: Always return JSON `{ "status": "error", "reason": "..." }` format
2. **Validation**: Use Bean Validation (JSR-303) for all data models
3. **Database**: Use Reactive MongoDB driver (`spring-boot-starter-data-mongodb-reactive`)
4. **Modularity**: Services separated, reusable, and testable
5. **Testing**: Move final test files to `/tests/`, delete temporary files

### Git Workflow Rules
1. **Commit Strategy**: Ask before committing after each feature/phase
2. **Branching**: Option for feature branch or direct main branch commit
3. **Merge Process**: Confirm testing and stability before merging to main
4. **Commit Messages**: Follow conventional format (feat:, fix:, test:, docs:, chore:)

### Documentation Rules
1. **Prompt Updates**: Update `prompt.md` after every phase completion
2. **Log Maintenance**: Keep `log.md` updated with credentials, configs, and notes
3. **Chat Backup**: Export chat after major phases to `/backups/` directory

### Technology Stack
- **Backend**: Java + Spring Boot + Spring WebFlux
- **Database**: MongoDB (Reactive)
- **Testing**: JUnit 5 + WebTestClient
- **Frontend**: React + axios + three.js (optional)
- **Security**: API Key or JWT authentication

## Next Steps
- ✅ Phase 0: Documentation completed
- ✅ Phase 1: Database models and repositories completed
- Begin Phase 2: REST API endpoints implementation
- Set up project structure and dependencies
