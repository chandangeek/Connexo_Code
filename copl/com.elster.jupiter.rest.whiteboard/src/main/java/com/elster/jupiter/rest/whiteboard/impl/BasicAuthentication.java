package com.elster.jupiter.rest.whiteboard.impl;

import java.io.IOException;

import com.elster.jupiter.http.whiteboard.SecurityToken;
import org.osgi.service.http.HttpContext;

import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import java.util.Arrays;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class BasicAuthentication implements Authentication {
    private final UserService userService;

    BasicAuthentication(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authentication = request.getHeader("Authorization");
        if (authentication != null && authentication.startsWith("Basic ")) {
            return doBasicAuthentication(request, response, authentication.split(" ")[1]);
        } else if (authentication != null && authentication.startsWith("Bearer ")) {
            return doBearerAuthorization(request, response, authentication);
        } else {
            return doCookieAuthorization(request, response);
        }
    }

    private boolean doCookieAuthorization(HttpServletRequest request, HttpServletResponse response) {
        Optional<User> user = Optional.empty();
        Optional<Cookie> xsrf = Arrays.stream(request.getCookies()).filter(cookie -> cookie.getName().equals("X-CONNEXO-TOKEN")).findFirst();
        if (xsrf.isPresent()) {
            user = SecurityToken.getInstance().verifyToken(xsrf.get().getValue(), request, response, userService);
        }

        return isAuthenticated(user) ? allow(request, response, user.get()) : deny(request, response);
    }

    private boolean doBearerAuthorization(HttpServletRequest request, HttpServletResponse response, String authentication) {
        String token = authentication.substring(authentication.lastIndexOf(" ") + 1);
        Optional<Cookie> xsrf = Arrays.stream(request.getCookies()).filter(cookie -> cookie.getName().equals("X-CONNEXO-TOKEN")).findFirst();
        if (xsrf.isPresent()){
            token = xsrf.get().getValue();
            if(!SecurityToken.getInstance().doComparison(token, authentication.substring(authentication.lastIndexOf(" ") + 1))) {
                return deny(request, response);
            }
        }

        // Since the cookie value can be updated without updating the authorization header, it should be used here instead of the header
        // The check before ensures the header is also valid syntactically, but it may be expires if only the cookie was updated (Facts, Flow)
        Optional<User> user = SecurityToken.getInstance().verifyToken(token, request, response, userService);
        return isAuthenticated(user) ? allow(request, response, user.get()) : deny(request, response);
    }

    private boolean doBasicAuthentication(HttpServletRequest request, HttpServletResponse response, String authentication) {
        Optional<User> user = userService.authenticateBase64(authentication);
        SecurityToken.getInstance().removeCookie(request, response);
        if(isAuthenticated(user)){
            SecurityToken.getInstance().createToken(request, response, user.get(), 0);
            return allow(request, response, user.get());
        }
        else {
            return deny(request, response);
        }
    }

    private boolean isAuthenticated(Optional<User> user) {
        return user.isPresent() && !user.get().getPrivileges().isEmpty();
    }

    private boolean allow(HttpServletRequest request, HttpServletResponse response, User user) {
        request.setAttribute(HttpContext.AUTHENTICATION_TYPE, HttpServletRequest.BASIC_AUTH);
        request.setAttribute(WhiteBoardConfiguration.USERPRINCIPAL, user);
        request.setAttribute(HttpContext.REMOTE_USER, user.getName());
        userService.addLoggedInUser(user);
        return true;
    }

    private boolean deny(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        SecurityToken.getInstance().removeCookie(request, response);
        SecurityToken.getInstance().invalidateSession(request);
        return false;
    }

}
