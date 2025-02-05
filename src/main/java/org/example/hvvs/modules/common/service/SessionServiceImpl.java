package org.example.hvvs.modules.common.service;

import com.maxmind.geoip2.exception.GeoIp2Exception;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.annotation.Resource;
import jakarta.ejb.Asynchronous;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import org.example.hvvs.model.UserSessions;
import org.example.hvvs.model.Users;
import org.example.hvvs.modules.common.repository.SessionRepository;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.example.hvvs.utils.SessionCacheManager;

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
    private GeoLocationService geoLocationService;

    @Inject
    private SessionCacheManager sessionCacheManager;

    @Resource
    private ManagedExecutorService executorService;

    @Override
    @Transactional
    public UserSessions createSession(Users user, String ipAddress, String userAgent) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Timestamp expiresAt = new Timestamp(System.currentTimeMillis() + (8 * 60 * 60 * 1000)); // 8 hours

        UserSessions session = new UserSessions();
        session.setUser_id(user);
        session.setIpAddress(ipAddress);
        session.setUserAgent(userAgent);
        session.setDeviceInfo("Pending"); // Temporary value
        session.setLoginTime(now);
        session.setLastAccess(now);
        session.setExpiresAt(expiresAt);

        // Async geolocation lookup
        executorService.submit(() -> {
            try {
                String location = parseLocationFromIp(ipAddress);
                String[] locParts = location.split(", ");
                session.setCity(locParts[0]);
                session.setRegion(locParts[1]);
                session.setCountry(locParts[2]);
            } catch (Exception e) {
                // Set default values
                session.setCity("Unknown");
                session.setRegion("Unknown");
                session.setCountry("Unknown");
            }

            // Device detection with proper library
            UserAgent agent = UserAgent.parseUserAgentString(userAgent);
            session.setDeviceInfo(agent.getOperatingSystem().getName()
                    + " - " + agent.getBrowser().getName());

            sessionRepository.update(session);
        });

        return sessionRepository.create(session);
    }


    @Override
    @Transactional
    public void revokeSession(UUID sessionId) {
        sessionRepository.revokeSession(sessionId);
        sessionCacheManager.invalidateSession(sessionId);
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
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Timestamp newExpiresAt = new Timestamp(now.getTime() + (8 * 60 * 60 * 1000));
        sessionRepository.updateSessionAccess(sessionId, now, newExpiresAt);
    }

    @Override
    @Asynchronous
    public void updateLastAccessAsync(UUID sessionId) {
        updateLastAccess(sessionId);
    }

    public void updateSessionExpiration(UUID sessionId, Timestamp newExpiresAt) {
        sessionRepository.updateSessionExpiration(sessionId, newExpiresAt);
    }

    @Override
    public Optional<UserSessions> validateSession(UUID sessionId) {
        UserSessions session = sessionRepository.findBySessionId(sessionId);
        if (session != null && session.getExpiresAt().after(new Timestamp(System.currentTimeMillis()))) {
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
}
