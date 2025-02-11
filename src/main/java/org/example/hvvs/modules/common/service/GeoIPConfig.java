package org.example.hvvs.modules.common.service;

import com.maxmind.geoip2.DatabaseReader;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;

import java.io.File;
import java.io.IOException;

@ApplicationScoped
public class GeoIPConfig {
    
    @Inject
    private ServletContext servletContext;
    
    @Produces
    @ApplicationScoped
    public GeoLocationService createGeoLocationService() throws IOException {
        String path = servletContext.getRealPath("/WEB-INF/GeoLite2-City.mmdb");
        File database = new File(path);
        DatabaseReader reader = new DatabaseReader.Builder(database).build();
        return new GeoLocationService(reader);
    }
}