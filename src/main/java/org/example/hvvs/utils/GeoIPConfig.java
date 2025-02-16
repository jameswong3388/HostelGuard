package org.example.hvvs.utils;

import com.maxmind.geoip2.DatabaseReader;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;

import java.io.File;
import java.io.IOException;
import jakarta.annotation.PostConstruct;

@ApplicationScoped
public class GeoIPConfig {
    
    @Inject
    private ServletContext servletContext;
    
    private DatabaseReader databaseReader;

    @PostConstruct
    public void init() {
        try {
            String path = servletContext.getRealPath("/WEB-INF/GeoLite2-City.mmdb");
            File database = new File(path);
            this.databaseReader = new DatabaseReader.Builder(database).build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize GeoIP database", e);
        }
    }

    @Produces
    public DatabaseReader createDatabaseReader() {
        return databaseReader;
    }
}