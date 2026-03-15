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

    private long jwtExpiresSeconds = 86400;

    @NotNull
    private final Im im = new Im();

    @NotNull
    private final Database database = new Database();

    @NotNull
    private final Admin admin = new Admin();

    @NotNull
    private final Mock mock = new Mock();

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public long getJwtExpiresSeconds() {
        return jwtExpiresSeconds;
    }

    public void setJwtExpiresSeconds(long jwtExpiresSeconds) {
        this.jwtExpiresSeconds = jwtExpiresSeconds;
    }

    public Im getIm() {
        return im;
    }

    public Database getDatabase() {
        return database;
    }

    public Admin getAdmin() {
        return admin;
    }

    public Mock getMock() {
        return mock;
    }

    public static class Im {
        private boolean enabled;

        @NotBlank
        private String host = "0.0.0.0";

        private int port = 9090;

        private int bossThreads = 1;

        private int workerThreads;

        private int readerIdleSeconds = 60;

        private int writerIdleSeconds = 25;

        private int maxFrameLength = 1024 * 1024;

        private int compressionThresholdBytes = 512;

        private int sessionTtlSeconds = 120;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
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

        public int getBossThreads() {
            return bossThreads;
        }

        public void setBossThreads(int bossThreads) {
            this.bossThreads = bossThreads;
        }

        public int getWorkerThreads() {
            return workerThreads;
        }

        public void setWorkerThreads(int workerThreads) {
            this.workerThreads = workerThreads;
        }

        public int getReaderIdleSeconds() {
            return readerIdleSeconds;
        }

        public void setReaderIdleSeconds(int readerIdleSeconds) {
            this.readerIdleSeconds = readerIdleSeconds;
        }

        public int getWriterIdleSeconds() {
            return writerIdleSeconds;
        }

        public void setWriterIdleSeconds(int writerIdleSeconds) {
            this.writerIdleSeconds = writerIdleSeconds;
        }

        public int getMaxFrameLength() {
            return maxFrameLength;
        }

        public void setMaxFrameLength(int maxFrameLength) {
            this.maxFrameLength = maxFrameLength;
        }

        public int getCompressionThresholdBytes() {
            return compressionThresholdBytes;
        }

        public void setCompressionThresholdBytes(int compressionThresholdBytes) {
            this.compressionThresholdBytes = compressionThresholdBytes;
        }

        public int getSessionTtlSeconds() {
            return sessionTtlSeconds;
        }

        public void setSessionTtlSeconds(int sessionTtlSeconds) {
            this.sessionTtlSeconds = sessionTtlSeconds;
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

    public static class Admin {
        @NotBlank
        private String username = "admin";

        @NotBlank
        private String password = "admin123456";

        @NotBlank
        private String nickname = "Administrator";

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

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }
    }

    public static class Mock {
        private boolean enabled;

        @NotBlank
        private String password = "mock123456";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
