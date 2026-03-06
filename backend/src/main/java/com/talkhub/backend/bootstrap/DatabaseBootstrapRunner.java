package com.talkhub.backend.bootstrap;

import com.talkhub.backend.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(prefix = "app.database", name = "auto-create-if-missing", havingValue = "true", matchIfMissing = true)
public class DatabaseBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseBootstrapRunner.class);

    private final AppProperties appProperties;

    public DatabaseBootstrapRunner(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        AppProperties.Database db = appProperties.getDatabase();
        String targetDb = db.getName();
        if (targetDb == null || targetDb.isBlank()) {
            throw new IllegalStateException("app.database.name (or PG_DATABASE) is blank");
        }

        String bootstrapUrl = String.format(
            "jdbc:postgresql://%s:%d/%s?sslmode=%s",
            db.getHost(),
            db.getPort(),
            db.getBootstrapDatabase(),
            db.getSslmode()
        );

        try (Connection conn = DriverManager.getConnection(bootstrapUrl, db.getUsername(), db.getPassword())) {
            if (databaseExists(conn, targetDb)) {
                log.info("PostgreSQL database '{}' already exists.", targetDb);
                return;
            }

            String sql = "CREATE DATABASE " + quoteIdentifier(targetDb);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
                log.info("Created PostgreSQL database '{}'.", targetDb);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                String.format("Failed to ensure PostgreSQL database '%s' exists: %s", targetDb, e.getMessage()),
                e
            );
        }
    }

    private boolean databaseExists(Connection conn, String dbName) throws SQLException {
        String sql = "SELECT 1 FROM pg_database WHERE datname = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dbName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private String quoteIdentifier(String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }
}
