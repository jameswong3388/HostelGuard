package org.example.hvvs.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.sql.Timestamp;
import java.util.UUID;

@Cacheable
@Entity
@Table(name = "user_sessions")
public class UserSessions {

    @Id
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "session_id", columnDefinition = "CHAR(36)")
    private UUID session_id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user_id;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(name = "city", nullable = false, length = 512)
    private String city;

    @Column(name = "region", nullable = false, length = 512)
    private String region;

    @Column(name = "country", nullable = false, length = 512)
    private String country;

    @Column(name = "user_agent", nullable = false, length = 512)
    private String userAgent;

    @Column(name = "login_time", nullable = false)
    private Timestamp loginTime;

    @Column(name = "last_access", nullable = false)
    private Timestamp lastAccess;

    @Column(name = "expires_at", nullable = false)
    private Timestamp expiresAt;

    @Column(name = "device_info", length = 255)
    private String deviceInfo;

    public UserSessions() {
    }

    public UserSessions(Users user_id, String ipAddress, String city, String region, String country, String userAgent,
                        Timestamp loginTime, Timestamp lastAccess, Timestamp expiresAt, String deviceInfo) {
        this.user_id = user_id;
        this.ipAddress = ipAddress;
        this.city = city;
        this.region = region;
        this.country = country;
        this.userAgent = userAgent;
        this.loginTime = loginTime;
        this.lastAccess = lastAccess;
        this.expiresAt = expiresAt;
        this.deviceInfo = deviceInfo;
    }

    // Getters and Setters
    public UUID getSession_id() {
        return session_id;
    }

    public void setSession_id(UUID session_id) {
        this.session_id = session_id;
    }

    public Users getUser_id() {
        return user_id;
    }

    public void setUser_id(Users user_id) {
        this.user_id = user_id;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Timestamp getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Timestamp loginTime) {
        this.loginTime = loginTime;
    }

    public Timestamp getLastAccess() {
        return lastAccess;
    }

    public void setLastAccess(Timestamp lastAccess) {
        this.lastAccess = lastAccess;
    }

    public Timestamp getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Timestamp expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
}
