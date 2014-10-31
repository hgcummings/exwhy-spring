package io.hgc.exwhy.web.authentication;

import org.springframework.social.connect.Connection;
import org.springframework.social.connect.web.SignInAdapter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

@Component
public final class SimpleSignInAdapter implements SignInAdapter {

    private final UserRepository userRepository;

    private final UserCookieGenerator userCookieGenerator;

    @Inject
    public SimpleSignInAdapter(UserRepository userRepository, UserCookieGenerator userCookieGenerator) {
        this.userRepository = userRepository;
        this.userCookieGenerator = userCookieGenerator;
    }

    public String signIn(String userId, Connection<?> connection, NativeWebRequest request) {
        User user = new User(userId, connection.fetchUserProfile());
        userRepository.addUser(user);
        SecurityContext.setCurrentUser(user);
        userCookieGenerator.addCookie(userId, request.getNativeResponse(HttpServletResponse.class));
        return null;
    }
}