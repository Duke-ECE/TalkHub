package com.talkhub.backend.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    @NotBlank
    private String jwtSecret = "change-me";

    @NotNull
    private final Realtime realtime = new Realtime();

    @NotNull
    private final Database database = new Database();

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public Realtime getRealtime() {
        return realtime;
    }

    public Database getDatabase() {
        return database;
    }

    public static class Realtime {
        @NotBlank
        private String websocketPath = "/ws";

        public String getWebsocketPath() {
            return websocketPath;
        }

        public void setWebsocketPath(String websocketPath) {
            this.websocketPath = websocketPath;
        }
    }

    public static class Database {
        private boolean autoCreateIfMissing = true;

        @NotBlank
        private String host = "localhost";

        private int port = 5432;

        @NotBlank
        private String username = "postgres";

        @NotBlank
        private String password = "postgres";

        @NotBlank
        private String name = "echocenter";

        @NotBlank
        private String sslmode = "disable";

        @NotBlank
        private String bootstrapDatabase = "postgres";

        public boolean isAutoCreateIfMissing() {
            return autoCreateIfMissing;
        }

        public void setAutoCreateIfMissing(boolean autoCreateIfMissing) {
            this.autoCreateIfMissing = autoCreateIfMissing;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSslmode() {
            return sslmode;
        }

        public void setSslmode(String sslmode) {
            this.sslmode = sslmode;
        }

        public String getBootstrapDatabase() {
            return bootstrapDatabase;
        }

        public void setBootstrapDatabase(String bootstrapDatabase) {
            this.bootstrapDatabase = bootstrapDatabase;
        }
    }
}
