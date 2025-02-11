package org.example.hvvs.modules.common.service;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import jakarta.ejb.Stateless;

import java.io.IOException;
import java.net.InetAddress;

@Stateless
public class GeoLocationService {
    private DatabaseReader dbReader;

    // No-args constructor for CDI proxying
    GeoLocationService() {
    }

    GeoLocationService(DatabaseReader dbReader) {
        this.dbReader = dbReader;
    }

    public String getLocation(String ipAddress) throws IOException, GeoIp2Exception {
        InetAddress ip = InetAddress.getByName(ipAddress);
        CityResponse response = dbReader.city(ip);

        String city = response.getCity().getName();
        String region = response.getMostSpecificSubdivision().getName();
        String country = response.getCountry().getName();

        return String.format("%s, %s, %s", city, region, country);
    }
}
