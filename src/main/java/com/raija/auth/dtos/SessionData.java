package com.raija.auth.dtos;

import java.io.Serializable;

public class SessionData implements Serializable {

    private String userId;
    private String refreshTokenId;

    private long createdAt;
    private long lastAccessedAt;

    // ? optional
    private String ipAddress;
    private String userAgent;
    private String deviceId;

    public SessionData() {}

    public SessionData(String userId, String refreshTokenId,
                       long createdAt, long lastAccessedAt,
                       String ipAddress, String userAgent, String deviceId) {
        this.userId = userId;
        this.refreshTokenId = refreshTokenId;
        this.createdAt = createdAt;
        this.lastAccessedAt = lastAccessedAt;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.deviceId = deviceId;
    }

    public String getUserId() {
        return userId;
    }

    public String getRefreshTokenId() {
        return refreshTokenId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getLastAccessedAt() {
        return lastAccessedAt;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setLastAccessedAt(long lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
    }
}