package io.hgc.exwhy.web.authentication;

import org.springframework.stereotype.Component;
import org.springframework.web.util.CookieGenerator;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
final class UserCookieGenerator {

    private final CookieGenerator userCookieGenerator = new CookieGenerator();

    public UserCookieGenerator() {
        userCookieGenerator.setCookieName("exwhy_user");
    }

    public void addCookie(String userId, HttpServletResponse response) {
        userCookieGenerator.addCookie(response, userId);
    }

    public void removeCookie(HttpServletResponse response) {
        userCookieGenerator.addCookie(response, "");
    }

    public String readCookieValue(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(userCookieGenerator.getCookieName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

}