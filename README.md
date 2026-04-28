# Smart Campus API

This repository contains the RESTful API for the Smart Campus initiative, developed using JAX-RS (Jersey) and running on a Grizzly lightweight HTTP server. The API allows campus facilities managers to interact with campus data, including Rooms, Sensors, and Sensor Readings.

## 1. Overview of API Design
The Smart Campus API is designed following RESTful principles:
- **Resource-Oriented:** The API exposes logical entities as resources (`/rooms`, `/sensors`, `/sensors/{sensorId}/readings`).
- **Standard HTTP Methods:** It utilizes standard GET, POST, PUT, and DELETE methods for CRUD operations.
- **HATEOAS / Discovery:** A root discovery endpoint (`/api/v1`) provides hypermedia links to the primary resource collections.
- **Sub-Resource Locators:** Complex nested resources, such as historical sensor readings, are managed through sub-resource locators, promoting modularity.
- **Advanced Error Handling:** Custom exception mappers return semantic HTTP status codes (e.g., 409 Conflict, 422 Unprocessable Entity, 403 Forbidden, 500 Internal Server Error) to clearly communicate business logic constraints and prevent internal stack traces from leaking to clients.
- **In-Memory Thread-Safe Data Store:** Data is managed in a thread-safe singleton in-memory structure without the use of external databases.

## 2. How to Build and Run the Project

### Prerequisites
- Java Development Kit (JDK) 17 or higher
- Maven 3.6+

### Build Instructions
1. Clone the repository and navigate to the root directory.
2. Build the project using Maven to create the executable fat JAR:
   ```bash
   mvn clean package
   ```

### Launching the Server
Once built, you can run the application directly from the target directory:
```bash
java -jar target/smart-campus-api-1.0-SNAPSHOT.jar
```
The server will start and be accessible at: `http://localhost:8080/api/v1`

## 3. Sample cURL Commands

Here are five sample cURL commands demonstrating successful interactions with the API:

**1. Discovery Endpoint**
```bash
curl -i -X GET http://localhost:8080/api/v1
```

**2. Create a Room**
```bash
curl -i -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id": "LIB-301", "name": "Library Quiet Study", "capacity": 50, "regulations": "Silence mandatory"}'
```

**3. Create a Sensor in the Room**
```bash
curl -i -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id": "TEMP-001", "type": "Temperature", "status": "ACTIVE", "roomId": "LIB-301"}'
```

**4. Filter Sensors by Type**
```bash
curl -i -X GET "http://localhost:8080/api/v1/sensors?type=Temperature"
```

**5. Append a Sensor Reading**
```bash
curl -i -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"id": "read-001", "timestamp": 1700000000000, "value": 22.5}'
```

---

## 4. Conceptual Report

**Part 1.1: In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.**
By default, a JAX-RS Resource class has a per-request lifecycle. This means the runtime instantiates a new resource object for every incoming HTTP request and discards it once the response is sent. Due to this design, we cannot store our in-memory data maps or lists as regular instance variables within the resource class, as they would be lost between requests. Instead, the data structures must be managed outside the resource's lifecycle, typically within a Singleton class (like our `InMemoryStore`). Because this shared singleton will be accessed concurrently by multiple request threads, we must use thread-safe data structures (e.g., `ConcurrentHashMap`) or synchronization mechanisms to prevent race conditions and ensure data integrity.

**Part 1.2: Why is the provision of ”Hypermedia” (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?**
HATEOAS (Hypermedia As The Engine Of Application State) allows an API to be self-descriptive. Instead of relying on hardcoded URLs or external static documentation, the API dynamically provides clients with the URIs needed to navigate the application state directly in its responses. This benefits client developers by decoupling their code from the server's specific URI structure. As long as the client understands the link relations, the server can safely evolve its URI design without breaking clients, reducing the cognitive load of reading documentation and preventing hardcoded dependencies.

**Part 2.1: When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.**
Returning only IDs minimizes the initial payload size, saving network bandwidth and reducing server-side serialization overhead. However, it shifts the burden to the client, which must make subsequent HTTP requests for each ID to fetch full details (the "N+1" problem), thereby increasing network latency. Conversely, returning full room objects increases the payload size for a single request but provides all necessary data immediately, eliminating extra round-trips and simplifying client-side processing.

**Part 2.2: Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.**
Yes, the `DELETE` operation is idempotent. An operation is idempotent if making multiple identical requests has the same effect on the server state as making a single request. When a client sends a `DELETE /{roomId}` request for the first time, the room is removed from the store and a 204 No Content is returned. If the client mistakenly sends the exact same request again, the room no longer exists. The data store simply ignores the deletion of a non-existent key, and the server continues to return a success status without further altering the system state.

**Part 3.1: We explicitly use the @Consumes(MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?**
The `@Consumes` annotation instructs the JAX-RS runtime that the method only accepts HTTP requests with the `Content-Type` header set to `application/json`. If a client sends data in a different format (e.g., `text/plain`), the JAX-RS runtime intercepts the request before it reaches the resource method. Since no method matches the provided media type, JAX-RS automatically rejects the request and returns an HTTP 415 Unsupported Media Type error to the client, ensuring strict type safety.

**Part 3.2: You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/v1/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?**
Using a path parameter (`/sensors/type/CO2`) implies that "CO2 sensors" are a distinct, hierarchical resource collection. In contrast, using a `@QueryParam` (`/sensors?type=CO2`) treats "sensors" as the primary collection and applies a filter to it. Query parameters are superior for filtering because they maintain the logical identity of the core resource, and they allow for flexible, optional, and easily combinable criteria (e.g., `?type=CO2&status=ACTIVE`) without creating deeply nested and explosive path permutations.

**Part 4.1: Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive controller class?**
The Sub-Resource Locator pattern promotes separation of concerns and modularity. In a massive controller class, defining every nested path leads to bloated code where unrelated operations are entangled. By delegating nested paths to dedicated resource classes (e.g., `SensorReadingResource`), we encapsulate the logic for specific sub-domains. This makes the code easier to read, test, and maintain, and prevents the main resource classes from becoming a monolithic bottleneck as the API scales.

**Part 5.2: Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?**
HTTP 404 Not Found means the target URI itself does not exist. However, when a client sends a structurally valid JSON payload to a valid URI, but the payload contains a semantic error—such as a `roomId` that doesn't exist in the database—the request is well-formed but cannot be processed. HTTP 422 Unprocessable Entity accurately conveys that the server understands the content type and the syntax is correct, but semantic errors (like referential integrity violations) prevented processing. Using 404 would be misleading, as it might suggest the API endpoint itself is incorrect.

**Part 5.4: From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?**
Exposing stack traces is a severe information disclosure vulnerability. Stack traces reveal the internal architecture, framework versions, class names, library dependencies, and precise file paths used by the application. An attacker can gather intelligence about the underlying technologies (e.g., Jersey versions), identify known vulnerabilities associated with those libraries, and craft targeted exploits based on the exposed code execution paths.

**Part 5.5: Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?**
Using JAX-RS filters centralizes cross-cutting concerns. Manually inserting `Logger.info()` in every method leads to code duplication, makes the application harder to maintain, and clutters business logic with infrastructure concerns. JAX-RS filters intercept all requests and responses globally, ensuring that logging is applied uniformly without explicit code in the resource methods. This adheres to the DRY principle and results in a much cleaner, more modular codebase.
