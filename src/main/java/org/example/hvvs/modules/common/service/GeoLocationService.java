package org.example.hvvs.modules.common.service;

import com.maxmind.geoip2.exception.GeoIp2Exception;

import java.io.IOException;

public interface GeoLocationService {
    /**
     * Retrieves the location information for a given IP address.
     *
     * @param ipAddress The IP address to look up
     * @return A string containing the city, region, and country of the IP address
     * @throws IOException If there is an error reading from the database
     * @throws GeoIp2Exception If there is an error processing the GeoIP lookup
     */
    String getLocation(String ipAddress) throws IOException, GeoIp2Exception;
}