# Spring Boot Camel REST with restlet component

This example demonstrates how to use `camel-restlet` component along with Camel's REST DSL to expose a RESTful API running on Spring Boot.
It has two endpoints:

 * `/camel-rest/hello/{your name}` - just to print a hello message
 * `/camel-rest/slow/waitfor/{how long in ms}` - to simulate a slow transaction. For this case we configured the `restlet` engine to change it's default `connectionTimeout` setting (which has a default value of 30s)

### Building

> NOTE: use the `configuration/settings.xml` to get the right maven repos

The example can be built with:

    $ mvn install

This automatically generates the application resource descriptors and builds the Docker image, so it requires access to a Docker daemon, relying on the `DOCKER_HOST` environment variable by default.

### Running the example locally

The example can be run locally using the following Maven goal:

    $ mvn spring-boot:run -DskipTests

Alternatively, you can run the application locally using the executable JAR produced:

    $ java -jar target/camel-sb-restlet-demo-0.0.1-SNAPSHOT.jar

You can then access the REST API directly from your Web browser, e.g.:

- <http://localhost:8080/camel-rest/hello/your_name_here>
- <http://localhost:8080/camel-rest/slow/waitfor/70000>


### Swagger API

The example provides API documentation of the service using Swagger using the _context-path_ `camel-rest/api-doc`. 

