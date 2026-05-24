package com.personalfinance.manager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.jdbc.DataSourceBuilder;
import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
@Profile("prod")
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.trim().isEmpty()) {
            throw new IllegalStateException("DATABASE_URL environment variable is missing or empty!");
        }

        try {
            // Render database URLs start with postgres:// or postgresql://
            String formattedUrl = databaseUrl;
            if (formattedUrl.startsWith("postgres://")) {
                formattedUrl = formattedUrl.replace("postgres://", "postgresql://");
            }

            URI dbUri = new URI(formattedUrl);

            String userInfo = dbUri.getUserInfo();
            if (userInfo == null || !userInfo.contains(":")) {
                throw new IllegalArgumentException("Invalid DATABASE_URL user info segment");
            }

            String username = userInfo.split(":")[0];
            String password = userInfo.split(":")[1];
            
            // Build the JDBC URL from the parts
            String host = dbUri.getHost();
            int port = dbUri.getPort();
            String path = dbUri.getPath();
            
            StringBuilder jdbcUrlBuilder = new StringBuilder("jdbc:postgresql://");
            jdbcUrlBuilder.append(host);
            if (port != -1) {
                jdbcUrlBuilder.append(":").append(port);
            }
            jdbcUrlBuilder.append(path);

            String query = dbUri.getQuery();
            if (query != null) {
                jdbcUrlBuilder.append("?").append(query);
            }

            String jdbcUrl = jdbcUrlBuilder.toString();

            return DataSourceBuilder.create()
                    .url(jdbcUrl)
                    .username(username)
                    .password(password)
                    .driverClassName("org.postgresql.Driver")
                    .build();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to parse DATABASE_URL: " + databaseUrl, e);
        }
    }
}
