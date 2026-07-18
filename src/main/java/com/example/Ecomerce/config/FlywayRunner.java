package com.example.Ecomerce.config;

import org.flywaydb.core.Flyway;

/**
 * Runs Flyway migrations before the Spring context starts.
 * <p>
 * This project's Spring Boot build does not ship a bundled Flyway auto-configuration module
 * (spring.flyway.* properties are inert here), so migrations are triggered explicitly and early —
 * guaranteeing the schema exists before Hibernate's schema *validation* runs.
 */
public final class FlywayRunner {

    private FlywayRunner() {
    }

    public static void migrate() {
        String url = System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/Ecomerce_DB");
        String username = System.getenv().getOrDefault("DB_USERNAME", "postgres");
        String password = System.getenv().getOrDefault("DB_PASSWORD", "root");

        Flyway.configure()
                .dataSource(url, username, password)
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }
}
