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
        Optional<User> user = Optional.empty();

        boolean refreshCookie = false;
        if (authentication != null && authentication.startsWith("Basic ")) {
            refreshCookie = true;

            user = userService.authenticateBase64(authentication.split(" ")[1]);
            SecurityToken.getInstance().removeCookie(request, response);
        } else {
            Optional<Cookie> xsrf = Arrays.stream(request.getCookies()).filter(cookie -> cookie.getName().equals("X-CONNEXO-TOKEN")).findFirst();
            if (xsrf.isPresent()) {
                if (authentication != null && authentication.startsWith("Bearer ")) {
                    if (!SecurityToken.getInstance().doComparison(xsrf.get(), authentication.substring(authentication.lastIndexOf(" ") + 1))) {
                        return deny(request, response);
                    }
                }
                user = SecurityToken.getInstance().verifyToken(xsrf.get().getValue(), request, response, userService);
            } else if (authentication != null && authentication.startsWith("Bearer ")) {
                user = SecurityToken.getInstance().verifyToken(authentication.substring(authentication.lastIndexOf(" ") + 1), request, response, userService);
            }
        }
        return user.isPresent() ? allow(request, response, user.get(), refreshCookie) : deny(request, response);
    }

    private boolean allow(HttpServletRequest request, HttpServletResponse response, User user, boolean refreshCookie) {
        request.setAttribute(HttpContext.AUTHENTICATION_TYPE, HttpServletRequest.BASIC_AUTH);
        request.setAttribute(WhiteBoardConfiguration.USERPRINCIPAL, user);
        request.setAttribute(HttpContext.REMOTE_USER, user.getName());
        Optional<Cookie> xsrf = Arrays.stream(request.getCookies()).filter(cookie -> cookie.getName().equals("X-CONNEXO-TOKEN")).findFirst();
        if (!xsrf.isPresent() || refreshCookie) {
            SecurityToken.getInstance().createToken(request, response, user, 0);
        }
        userService.addLoggedInUser(user);
        return true;
    }

    private boolean deny(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        Optional<Cookie> xsrf = Arrays.stream(request.getCookies()).filter(cookie -> cookie.getName().equals("X-CONNEXO-TOKEN")).findFirst();
        if (xsrf.isPresent())
            SecurityToken.getInstance().removeCookie(request, response);
        SecurityToken.getInstance().invalidateSession(request);
        return false;
    }

}
