package io.hgc.exwhy.web.authentication;

import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class UserInterceptor extends HandlerInterceptorAdapter {

    private final UserRepository userRepository;

    private final UserCookieGenerator userCookieGenerator = new UserCookieGenerator();
    private final UsersConnectionRepository connectionRepository;

    public UserInterceptor(UsersConnectionRepository connectionRepository, UserRepository userRepository) {
        this.connectionRepository = connectionRepository;
        this.userRepository = userRepository;
    }

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        rememberUser(request, response);
        handleSignOut(request, response);
        return true;
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        SecurityContext.remove();
    }

    private void rememberUser(HttpServletRequest request, HttpServletResponse response) {
        String userId = userCookieGenerator.readCookieValue(request);
        if (userId == null) {
            return;
        }
        User user = userRepository.getUser(userId);
        if (user == null) {
            userCookieGenerator.removeCookie(response);
            return;
        }
        SecurityContext.setCurrentUser(user);
    }

    private void handleSignOut(HttpServletRequest request, HttpServletResponse response) {
        if (SecurityContext.userSignedIn() && request.getServletPath().startsWith("/signout")) {
            connectionRepository.createConnectionRepository(SecurityContext.getCurrentUser().getId()).removeConnections("facebook");
            userCookieGenerator.removeCookie(response);
            SecurityContext.remove();
        }
    }
}