# AGENTS.md

## Build & Test Commands

This is a Spring Boot 3.5.9 project using Java 21 and Maven.

### Common Commands
- `mvn spring-boot:run` - Start development server on port 8080
- `mvn spring-boot:run "-Dspring-boot.run.arguments=--job=fetch"` - Fetch and process holiday data
- `mvn spring-boot:run "-Dspring-boot.run.arguments=--job=process"` - Process existing JSON data only
- `mvn clean install` - Build the project and install to local repository
- `mvn test` - Run all tests
- `mvn test -Dtest=ClassName` - Run all tests in a specific class
- `mvn test -Dtest=ClassName#methodName` - Run a single test method
- `mvn clean package` - Build JAR file (creates `target/*.jar`)

## Code Style Guidelines

### General Formatting
- Use 4-space indentation for Java files (see .editorconfig)
- Use 2-space indentation for YAML, JSON, CSS, JavaScript, HTML
- Line endings: LF (not CRLF)
- Insert final newline at end of files
- Trim trailing whitespace (except in Markdown files)
- Use UTF-8 charset

### Imports
- Organize imports: java.*, third-party libraries (org.*), project imports (com.example.*)
- No wildcard imports (e.g., avoid `import java.util.*;`)
- Each import on separate line

### Class Structure
1. Package declaration
2. Imports (organized as above)
3. Class-level Javadoc with `@author` and `@since` tags
4. Annotations (order: @Slf4j, other Spring annotations like @Service/@RestController, @RequiredArgsConstructor)
5. Class declaration
6. Static constants (SCREAMING_SNAKE_CASE)
7. Instance fields with Javadoc
8. Constructor (if needed)
9. Methods with Javadoc

### Naming Conventions
- Classes: PascalCase (e.g., `HolidayController`, `RealTimeHolidayService`)
- Methods: camelCase starting with lowercase (e.g., `getHolidaysByYear`, `isTaipeiCityAllArea`)
- Constants: SCREAMING_SNAKE_CASE (e.g., `NCDR_API_URL`, `TARGET_CITY_1`)
- Instance fields: camelCase (e.g., `holidayCache`, `objectMapper`)
- Test methods: `testMethodName_Scenario()` (e.g., `testIsTaipeiCityAllArea_Positive`)

### Lombok Usage
- Use `@Slf4j` for logging
- Use `@RequiredArgsConstructor` for constructor-based dependency injection
- Use `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` for model classes
- Mark final fields for dependency injection (Lombok generates constructor)

### Javadoc & Comments
- All public classes and methods require Javadoc comments
- Use Chinese for comments and Javadoc in this project
- Include `@param` and `@return` tags for non-void methods
- Write descriptive inline comments for complex logic
- Include `<p>` tags for paragraph separation in Javadoc

### Error Handling
- Use custom exception classes (e.g., `ResourceNotFoundException`)
- Throw exceptions with descriptive messages (in Chinese)
- Handle exceptions in service layer with try-catch
- Log errors using `@Slf4j`: `log.error("description: {}", e.getMessage(), e)`
- Use `@RestControllerAdvice` for global exception handling
- Return consistent error response format with timestamp, status, error, and message

### Service Layer Patterns
- Mark service classes with `@Service` annotation
- Use `@Cacheable` for methods that benefit from caching (include `unless` condition)
- Use Spring's `RestClient` for HTTP calls, configured via constructor
- Handle external API failures gracefully (return empty list, log error)
- Use stream API for collection processing (`.stream().filter(...).toList()`)

### Controller Layer Patterns
- Mark controllers with `@RestController` and `@RequestMapping`
- Inject dependencies via final fields + `@RequiredArgsConstructor`
- Use appropriate HTTP annotations (@GetMapping, @PostMapping, etc.)
- Validate input parameters before processing (regex for format validation)
- Throw `ResourceNotFoundException` for missing resources
- Use cache for frequently accessed data (e.g., `ConcurrentHashMap`)

### Testing Guidelines
- Use JUnit 5 (Jupiter)
- Use reflection to test private methods when necessary
- Group related test methods in descriptive test classes
- Use descriptive test method names: `testFeature_Scenario()`
- Use `@BeforeEach` for common test setup
- Test positive, negative, and edge cases

### Logging
- Use `@Slf4j` annotation
- Log at appropriate levels: `log.debug()` for detailed info, `log.warn()` for warnings, `log.error()` for errors
- Include context in log messages (what was attempted, what happened)
- Format: `log.error("description: {}", e.getMessage(), e)` to include stacktrace

### Spring Configuration
- Use `@NonNull` annotation for constructor parameters that should not be null
- Configure external services (like RestClient) in constructors
- Use `application.yml` for configuration properties
- Access config via `@ConfigurationProperties` classes (e.g., `OpendataProperties`)

### Model Classes
- Use Lombok annotations: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- Include field-level Javadoc comments
- Use appropriate data types (String for dates in specific formats, boolean for flags)
- Consider deserialization requirements when naming fields
