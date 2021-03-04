# Reproducer of an unexpected issue with `spring.jpa.open-in-view` and `JDBCTemplate`

This repo is a minimal Spring Boot app to reproduce an issue I had in a real application with `spring.jpa.open-in-view` set to `true` (which is the default if the Spring Boot JPA Starter is in the classpath).

## The app

The `Controller` has these endpoints:

1. `/data-jdbc/{tenantId}/foo/{id}`: loads a `Tenant` and a `Foo` entity each from a Spring Data JDBC repository
2. `/jdbc-template/{tenantId}/foo/{id}`: loads a `Tenant` entity via a Spring Data JDBC repository and a `Foo` entity via `JDBCTemplate`

The application is configured with a database connection pool size of 5 and with `spring.jpa.open-in-view=true` (see in `application.yml`).


## The issue
After starting the app with `./gradlew bootrun`, you can run:

1. `./data-jdbc.sh` to hit the first endpoint, and
2. `./jdbc-template.sh` to hit the second endpoint

with 5 concurrent requests each.

The first endpoint will work fine, but the requests to the second endpoint will fail because they're waiting for database connections that are not available.

When you set `spring.jpa.open-in-view` to `false`, or increase the pool size to 6, or add the `@Transactional` annotation to the controller, the second endpoint will also work fine.

## The explanation

I'm happy to have this analysis corrected by people smarter than me :).

It seems that with the combination of:
* `spring.jpa.open-in-view=true`,
* a database query via `JDBCTemplate`, and 
* a database query via a Spring Data repository in the same request 

a thread requires 2 database connections instead of one, which was a bit surprising to me.

One database connection is reserved for the thread by `spring.jpa.open-in-view`. This connection is used by the Spring Data queries.

The `JDBCTemplate` queries, however, are not using the thread's database connection, but trying to get their own connection from the pool.

Since all 5 connections from the pool are already reserved for the 5 concurrent threads, each thread is waiting for a connection that would only be freed if one of threads completes, so all threads ultimately fail after the connection timeout.

The threads have effectively deadlocked over the connection resource.
