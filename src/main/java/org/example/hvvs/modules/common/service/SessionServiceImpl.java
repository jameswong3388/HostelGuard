package org.example.hvvs.modules.common.service;

import com.maxmind.geoip2.exception.GeoIp2Exception;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.annotation.Resource;
import jakarta.ejb.Asynchronous;
import jakarta.ejb.EJB;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import org.example.hvvs.model.UserSessions;
import org.example.hvvs.model.UserSessionsFacade;
import org.example.hvvs.model.Users;
import org.example.hvvs.model.UsersFacade;
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
    @EJB
    private UserSessionsFacade userSessionsFacade;

    @EJB
    private UsersFacade usersFacade;

    @EJB
    private GeoLocationService geoLocationService;

    @EJB
    private SessionCacheManager sessionCacheManager;

    @Resource
    private ManagedExecutorService executorService;

    @Override
    public UserSessions createSession(Users user, String ipAddress, String userAgent) {
        try {
            Timestamp now = new Timestamp(System.currentTimeMillis());
            Timestamp expiresAt = new Timestamp(System.currentTimeMillis() + (8 * 60 * 60 * 1000));

            UserSessions session = new UserSessions();
            session.setUser_id(user);
            session.setIpAddress(ipAddress);
            session.setUserAgent(userAgent);
            
            // Set device info synchronously first
            UserAgent agent = UserAgent.parseUserAgentString(userAgent);
            session.setDeviceInfo(agent.getOperatingSystem().getName() 
                                + " - " + agent.getBrowser().getName());
            
            session.setLoginTime(now);
            session.setLastAccess(now);
            session.setExpiresAt(expiresAt);

            // Create session with actual device info
            userSessionsFacade.create(session);

            user.setIsActive(true);
            user.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            usersFacade.edit(user);

            // Async updates for geolocation only
            executorService.submit(() -> {
                UserSessions freshSession = userSessionsFacade.find(session.getSession_id());
                if (freshSession != null) {
                    try {
                        String location = parseLocationFromIp(ipAddress);
                        String[] locParts = location.split(", ");
                        freshSession.setCity(locParts[0]);
                        freshSession.setRegion(locParts[1]);
                        freshSession.setCountry(locParts[2]);
                    } catch (Exception e) {
                        freshSession.setCity("Unknown");
                        freshSession.setRegion("Unknown");
                        freshSession.setCountry("Unknown");
                    }
                    userSessionsFacade.edit(freshSession);
                }
            });

            return session;
        } catch (Exception e) {
            // Rollback any changes if an error occurs
            user.setIsActive(false);
            user.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            usersFacade.edit(user);
            throw new RuntimeException("Failed to create session: " + e.getMessage(), e);
        }
    }


    @Override
    @Transactional
    public void revokeSession(UUID sessionId) {
        UserSessions session = userSessionsFacade.find(sessionId);
        if (session != null) {
            Users user = session.getUser_id();

            userSessionsFacade.revokeSession(sessionId);
            sessionCacheManager.invalidateSession(sessionId);

            // Check if user has any remaining active sessions
            List<UserSessions> activeSessions = getActiveSessions(user);
            if (activeSessions.isEmpty()) {
                user.setIsActive(false);
                user.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
                usersFacade.edit(user);
            }
        }
    }

    @Override
    @Transactional
    public void revokeAllSessions(Integer userId) {
        userSessionsFacade.revokeAllSessions(userId);
        Users user = usersFacade.find(userId);
        if (user != null) {
            user.setIsActive(false);
            user.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            usersFacade.edit(user);
        }
    }

    @Override
    public List<UserSessions> getActiveSessions(Users user) {
        return userSessionsFacade.findActiveSessionsByUser(user);
    }

    @Override
    @Transactional
    public void updateLastAccess(UUID sessionId) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Timestamp newExpiresAt = new Timestamp(now.getTime() + (8 * 60 * 60 * 1000));
        userSessionsFacade.updateSessionAccess(sessionId, now, newExpiresAt);
    }

    @Override
    @Asynchronous
    public void updateLastAccessAsync(UUID sessionId) {
        updateLastAccess(sessionId);
    }

    public void updateSessionExpiration(UUID sessionId, Timestamp newExpiresAt) {
        userSessionsFacade.updateSessionExpiration(sessionId, newExpiresAt);
    }

    @Override
    public Optional<UserSessions> validateSession(UUID sessionId) {
        UserSessions session = userSessionsFacade.find(sessionId);
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
