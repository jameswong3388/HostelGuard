package org.example.hvvs.modules.common.service;

import com.maxmind.geoip2.exception.GeoIp2Exception;
import org.example.hvvs.model.UserSessions;
import org.example.hvvs.model.Users;
import org.example.hvvs.modules.common.repository.SessionRepository;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Stateless
public class SessionServiceImpl implements SessionService {
    @Inject
    private SessionRepository sessionRepository;

    @Inject
    private GeoLocationService geoLocationService; // Assume this exists for IP geolocation

    @Override
    @Transactional
    public UserSessions createSession(Users user, String ipAddress, String userAgent, String deviceInfo) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Timestamp expiresAt = new Timestamp(System.currentTimeMillis() + (8 * 60 * 60 * 1000)); // 8 hours

        // Get geolocation data
        String city = "Unknown";
        String region = "Unknown";
        String country = "Unknown";

        try {
            String location = parseLocationFromIp(ipAddress);
            if (location != null) {
                String[] parts = location.split(", ");
                if (parts.length >= 3) {
                    city = parts[0];
                    region = parts[1];
                    country = parts[2];
                }
            }
        } catch (RuntimeException e) {
            // Log error but continue with default values
            e.printStackTrace();
        }

        UserSessions session = new UserSessions(
                user,
                ipAddress,
                city,
                region,
                country,
                userAgent,
                now,
                now,
                expiresAt,
                true,
                deviceInfo
        );

        return sessionRepository.create(session);
    }


    @Override
    @Transactional
    public void revokeSession(UUID sessionId) {
        sessionRepository.revokeSession(sessionId);
    }

    @Override
    @Transactional
    public void revokeAllSessions(Integer userId) {
        sessionRepository.revokeAllSessions(userId);
    }

    @Override
    public List<UserSessions> getActiveSessions(Users user) {
        return sessionRepository.findActiveSessionsByUser(user);
    }

    @Override
    @Transactional
    public void updateLastAccess(UUID sessionId) {
        sessionRepository.updateLastAccess(sessionId);
    }

    @Override
    public Optional<UserSessions> validateSession(UUID sessionId) {
        UserSessions session = sessionRepository.findBySessionId(sessionId);
        if (session != null && session.isActive() && session.getExpiresAt().after(new Timestamp(System.currentTimeMillis()))) {
            return Optional.of(session);
        }
        return Optional.empty();
    }

    @Override
    public String parseLocationFromIp(String ipAddress) {
        // Check for localhost IP addresses
        if (ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1") || ipAddress.equals("localhost")) {
            return "Kuala Lumpur, Kuala Lumpur, Malaysia";
        }

        // For other IPs, use GeoIP service
        try {
            return geoLocationService.getLocation(ipAddress); // Returns "City, Region, Country"
        } catch (IOException | GeoIp2Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String parseDeviceInfo(String userAgent) {
        // Implement user agent parsing logic
        if (userAgent.contains("Mobile")) {
            return "Mobile Device";
        } else if (userAgent.contains("Tablet")) {
            return "Tablet";
        }
        return "Desktop";
    }
}
