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

    @Value("${spring.datasource.username:root}")
    private String defaultUsername;

    @Value("${spring.datasource.password:root}")
    private String defaultPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        String springDatasourceUrl = System.getenv("SPRING_DATASOURCE_URL");
        if (!StringUtils.hasText(springDatasourceUrl)) {
            String databaseUrl = System.getenv("DATABASE_URL");
            if (StringUtils.hasText(databaseUrl) && (databaseUrl.startsWith("postgres://") || databaseUrl.startsWith("postgresql://"))) {
                try {
                    URI uri = new URI(databaseUrl);
                    String userInfo = uri.getUserInfo();
                    String username = defaultUsername;
                    String password = defaultPassword;
                    if (userInfo != null && userInfo.contains(":")) {
                        String[] parts = userInfo.split(":", 2);
                        username = parts[0];
                        password = parts[1];
                    }
                    int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                    String path = uri.getPath();
                    if (path != null && path.startsWith("/")) {
                        path = path.substring(1);
                    }
                    String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + port + "/" + path;
                    if (uri.getQuery() != null) {
                        jdbcUrl += "?" + uri.getQuery();
                    }

                    HikariDataSource ds = new HikariDataSource();
                    ds.setJdbcUrl(jdbcUrl);
                    ds.setUsername(username);
                    ds.setPassword(password);
                    ds.setDriverClassName("org.postgresql.Driver");
                    return ds;
                } catch (Exception e) {
                    System.err.println("Failed to parse DATABASE_URL, using default configuration: " + e.getMessage());
                }
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
}
