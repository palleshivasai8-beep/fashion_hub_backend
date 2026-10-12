package com.anurag.aiml;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DatabaseConfig {

    @Value("${spring.datasource.url:jdbc:mysql://localhost:3306/fashionhub}")
    private String defaultUrl;

    @Value("${spring.datasource.username:}")
    private String defaultUsername;

    @Value("${spring.datasource.password:}")
    private String defaultPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        String springDatasourceUrl = System.getenv("SPRING_DATASOURCE_URL");
        if (!StringUtils.hasText(springDatasourceUrl)) {
            String databaseUrl = System.getenv("DATABASE_URL");
            if (StringUtils.hasText(databaseUrl)) {
                if (databaseUrl.startsWith("jdbc:")) {
                    HikariDataSource ds = new HikariDataSource();
                    ds.setJdbcUrl(databaseUrl);
                    ds.setUsername(defaultUsername);
                    ds.setPassword(defaultPassword);
                    if (databaseUrl.startsWith("jdbc:postgresql:")) {
                        ds.setDriverClassName("org.postgresql.Driver");
                    } else if (databaseUrl.startsWith("jdbc:mysql:")) {
                        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
                    }
                    return ds;
                }

                if (databaseUrl.startsWith("postgres://") || databaseUrl.startsWith("postgresql://")) {
                    try {
                        URI uri = new URI(databaseUrl);
                        if (!StringUtils.hasText(uri.getHost())
                                || !StringUtils.hasText(uri.getRawPath())
                                || uri.getRawPath().length() < 2) {
                            throw new IllegalArgumentException("Database URL must include a host and database name");
                        }

                        String username = defaultUsername;
                        String password = defaultPassword;
                        String rawUserInfo = uri.getRawUserInfo();
                        if (rawUserInfo != null) {
                            int separator = rawUserInfo.indexOf(':');
                            String encodedUsername = separator < 0
                                     ? rawUserInfo
                                    : rawUserInfo.substring(0, separator);
                            username = decodeUserInfo(encodedUsername);
                            if (separator >= 0) {
                                password = decodeUserInfo(rawUserInfo.substring(separator + 1));
                            }
                        }

                        int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                        String databaseName = uri.getRawPath().substring(1);
                        String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + port + "/" + databaseName;
                        if (uri.getRawQuery() != null && !uri.getRawQuery().isBlank()) {
                            jdbcUrl += "?" + uri.getRawQuery();
                        }

                        HikariDataSource ds = new HikariDataSource();
                        ds.setJdbcUrl(jdbcUrl);
                        ds.setUsername(username);
                        ds.setPassword(password);
                        ds.setDriverClassName("org.postgresql.Driver");
                        return ds;
                    } catch (Exception e) {
                        throw new IllegalStateException(
                                "Unable to configure PostgreSQL from DATABASE_URL; verify its format and credentials", e);
                    }
                }

                if (databaseUrl.startsWith("mysql://")) {
                    try {
                        URI uri = new URI(databaseUrl);
                        if (!StringUtils.hasText(uri.getHost())
                                || !StringUtils.hasText(uri.getRawPath())
                                || uri.getRawPath().length() < 2) {
                            throw new IllegalArgumentException("Database URL must include a host and database name");
                        }

                        String username = defaultUsername;
                        String password = defaultPassword;
                        String rawUserInfo = uri.getRawUserInfo();
                        if (rawUserInfo != null) {
                            int separator = rawUserInfo.indexOf(':');
                            String encodedUsername = separator < 0
                                    ? rawUserInfo
                                    : rawUserInfo.substring(0, separator);
                            username = decodeUserInfo(encodedUsername);
                            if (separator >= 0) {
                                password = decodeUserInfo(rawUserInfo.substring(separator + 1));
                            }
                        }

                        int port = uri.getPort() == -1 ? 3306 : uri.getPort();
                        String databaseName = uri.getRawPath().substring(1);
                        String jdbcUrl = "jdbc:mysql://" + uri.getHost() + ":" + port + "/" + databaseName;
                        if (uri.getRawQuery() != null && !uri.getRawQuery().isBlank()) {
                            jdbcUrl += "?" + uri.getRawQuery();
                        }

                        HikariDataSource ds = new HikariDataSource();
                        ds.setJdbcUrl(jdbcUrl);
                        ds.setUsername(username);
                        ds.setPassword(password);
                        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
                        return ds;
                    } catch (Exception e) {
                        throw new IllegalStateException(
                                "Unable to configure MySQL from DATABASE_URL; verify its format and credentials", e);
                    }
                }

                throw new IllegalStateException(
                        "DATABASE_URL must be a JDBC URL, mysql:// URL, or postgres:// / postgresql:// URL");
            }
        }

        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(defaultUrl);
        ds.setUsername(defaultUsername);
        ds.setPassword(defaultPassword);
        if (defaultUrl.startsWith("jdbc:postgresql:")) {
            ds.setDriverClassName("org.postgresql.Driver");
        } else if (defaultUrl.startsWith("jdbc:mysql:")) {
            ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        }
        return ds;
    }

    private String decodeUserInfo(String value) {
        return java.net.URLDecoder.decode(
                value.replace("+", "%2B"),
                java.nio.charset.StandardCharsets.UTF_8);
    }
}
