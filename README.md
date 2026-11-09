# AamirCache

A simple in-memory key-value cache built with Java. The project is inspired by Redis and is intended as a learning project to understand how caching systems work.

## Current Features

- Store key-value pairs using `SET`.
- Retrieve values using `GET`.
- Delete entries using `DELETE`.
- Check whether a key exists using `EXISTS`.
- Get the number of stored entries.
- Set an expiration time for a key using `EXPIRE`.
- Check the remaining lifetime of a key using `TTL`.

## Development

The project started as a basic in-memory cache using Java's `HashMap` to store key-value pairs.

After implementing the basic operations, TTL (Time To Live) support was added. Keys can now be assigned an expiration time. For the initial implementation, a key can be set to expire after 10 seconds.

Expired keys are removed when accessed or during cache size checks.

## Technologies Used

- Java
- Maven
- Java Collections Framework (`HashMap`)

## Running the Project

Clone the repository:

```bash
git clone https://github.com/AamirAhmadDon/aamir-redis.git
```

Navigate into the project directory:

```bash
cd aamir-cache
```

Compile the project:

```bash
mvn clean compile
```

Run the application:

```bash
java -cp target/classes com.aamir.cache.Main
```

## Project Status

This is an ongoing learning project. The current implementation supports basic in-memory storage and key expiration.

Future improvements may include data persistence, TCP networking, and support for multiple clients.
