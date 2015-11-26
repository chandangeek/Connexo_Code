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

        Optional<Cookie> xsrf = Arrays.asList(request.getCookies()).stream().filter(cookie -> cookie.getName().equals("X-CONNEXO-TOKEN")).findFirst();
        boolean refreshCookie = false;

        if (authentication != null && authentication.startsWith("Basic ")) {
            user = userService.authenticateBase64(authentication.split(" ")[1]);
            SecurityToken.removeCookie(request, response);
            refreshCookie = true;
        } else{
            if (xsrf.isPresent()){
                refreshCookie = false;
                if (authentication != null && authentication.startsWith("Bearer ") && !authentication.startsWith("Bearer undefined")) {
                    if (!(authentication.split(" ")[1].equals(xsrf.get().getValue())))
                        return deny(request, response);
                }
                user = SecurityToken.verifyToken(xsrf.get().getValue(), request, response, userService);
            } else if (authentication != null && authentication.startsWith("Bearer ")){
                refreshCookie = false;
                user = SecurityToken.verifyToken(authentication.split(" ")[1], request, response, userService);
            }

        }
        return user.isPresent() ? allow(request, response, user.get(), refreshCookie) : deny(request, response);
    }

    private boolean allow(HttpServletRequest request, HttpServletResponse response, User user, boolean refreshCookie) {
        request.setAttribute(HttpContext.AUTHENTICATION_TYPE, HttpServletRequest.BASIC_AUTH);
        request.setAttribute(WhiteBoardConfiguration.USERPRINCIPAL, user);
        request.setAttribute(HttpContext.REMOTE_USER, user.getName());
        Optional<Cookie> xsrf = Arrays.asList(request.getCookies()).stream().filter(cookie -> cookie.getName().equals("X-CONNEXO-TOKEN")).findFirst();
        if (!xsrf.isPresent() || refreshCookie) {
            String token = SecurityToken.createToken(user);
            response.setHeader("X-AUTH-TOKEN", token);
            response.setHeader("Authorization", "Bearer " + token);
            SecurityToken.createCookie("X-CONNEXO-TOKEN", token, "/", -1, true, response);

        }else if(xsrf.isPresent() && response.getHeader("Authorization")!=null){
            response.setHeader("X-AUTH-TOKEN",response.getHeader("Authorization"));
            response.setHeader("Authorization", "Bearer " + response.getHeader("Authorization"));
        }else if(xsrf.isPresent()){
            response.setHeader("X-AUTH-TOKEN",xsrf.get().getValue());
            response.setHeader("Authorization", "Bearer " + xsrf.get().getValue());
        }
        userService.addLoggedInUser(user);
        return true;
    }

    private boolean deny(HttpServletRequest request, HttpServletResponse response) {
        //String realm = userService.getRealm();
        // response.addHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        Optional<Cookie> xsrf = Arrays.asList(request.getCookies()).stream().filter(cookie -> cookie.getName().equals("X-CONNEXO-TOKEN")).findFirst();
        if (xsrf.isPresent()) {
            SecurityToken.removeCookie(request, response);
            SecurityToken.invalidateSession(request);
        }
        return false;
    }

}
