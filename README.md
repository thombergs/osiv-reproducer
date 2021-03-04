# Reproducer of a Problem with `spring.jpa.open-in-view`

This repo is a minimal Spring Boot app to reproduce an issue I had in a real application with `spring.jpa.open-in-view` set to `true` (which is the default if the Spring Boot JPA Starter is in the classpath).

The `Controller` has these endpoints:

1. `/data-jdbc/{tenantId}/foo/{id}`: loads a `Tenant` and a `Foo` entity each from a Spring Data JDBC repository
2. `/jdbc-template/{tenantId}/foo/{id}`: loads a `Tenant` entity via a Spring Data JDBC repository and a `Foo` entity via `JDBCTemplate`

The application is configured with a database connection pool size of 5 and with `spring.jpa.open-in-view=true` (see in `application.yml`).

After starting the app with `./gradlew bootrun`, you can run:

1. `./data-jdbc.sh` to hit the first endpoint, and
2. `./jdbc-template.sh` to hit the second endpoint

with 5 concurrent requests each.

The first endpoint will work fine, but the requests to the second endpoint will fail because they're waiting for database connections that are not available.

When you set `spring.jpa.open-in-view` to `false`, or increase the pool size to 6, the second endpoint will also work fine.

It seems that with the combination of:
* `spring.jpa.open-in-view=true`,
* a database query via `JDBCTemplate`, and 
* a database query via a Spring Data repository in the same request 

a thread requires 2 database connections instead of one (which I would have expected).

My guess is that `JDBCTemplate` doesn't use the database connection that is reserved for the thread via the `open-in-view` pattern, but instead tries to get its own connection from the pool. Since each of the 5 threads already holds a connection for the `open-in-view` pattern, `JDBCTemplate` waits for a connection to be freed. That doesn't happen, because all 5 available connections are busy and `open-in-view` only frees a connection when the thread has done its work. The threads have effectively deadlocked over the connection resource.

Happy to have the above analysis corrected by people smarter than me :).