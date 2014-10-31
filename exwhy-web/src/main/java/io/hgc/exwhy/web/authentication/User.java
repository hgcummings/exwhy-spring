package io.hgc.exwhy.web.authentication;

import org.springframework.social.connect.UserProfile;
import org.springframework.util.StringUtils;

import java.util.Map;

public final class User {
    private final String id;

    private final String username;

    private final String friendlyName;

    public User(String id, UserProfile userProfile) {
        this.id = id;
        this.username = userProfile.getUsername();
        this.friendlyName = StringUtils.isEmpty(userProfile.getFirstName()) ? this.username : userProfile.getFirstName();
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFriendlyName() {
        return friendlyName;
    }
}