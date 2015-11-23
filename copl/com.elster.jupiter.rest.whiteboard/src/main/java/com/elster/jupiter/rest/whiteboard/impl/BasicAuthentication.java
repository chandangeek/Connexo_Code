package com.elster.jupiter.rest.whiteboard.impl;

import java.io.IOException;

import com.elster.jupiter.http.whiteboard.SecurityToken;
import com.elster.jupiter.http.whiteboard.impl.WhiteBoard;
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
    public boolean handleSecurity(HttpServletRequest request,HttpServletResponse response) throws IOException {
        String authentication = request.getHeader("Authorization");
        Optional<User> user = Optional.empty();

        Optional<Cookie> xsrf = Arrays.asList(request.getCookies()).stream().filter(cookie -> cookie.getName().equals("X-CONNEXO-TOKEN")).findFirst();

        if (xsrf.isPresent()) {
         if (authentication != null && authentication.startsWith("Bearer ")) {
                if(!(authentication.substring(7)).equals(xsrf.get().getValue()))
                    return deny(response);
            }
            user = SecurityToken.verifyToken(xsrf.get().getValue(), request, response, userService);
        }else{
            if (authentication != null && authentication.startsWith("Basic "))
            user = userService.authenticateBase64(authentication.split(" ")[1]);
        }


        return user.isPresent() ? allow(request, response, user.get()) : deny(response);
    }

    private boolean allow(HttpServletRequest request, HttpServletResponse response, User user) {
        request.setAttribute(HttpContext.AUTHENTICATION_TYPE, HttpServletRequest.BASIC_AUTH);
        request.setAttribute(WhiteBoardConfiguration.USERPRINCIPAL, user);
        request.setAttribute(HttpContext.REMOTE_USER, user.getName());
        Optional<Cookie> xsrf = Arrays.asList(request.getCookies()).stream().filter(cookie -> cookie.getName().equals("X-CONNEXO-TOKEN")).findFirst();
        if (!xsrf.isPresent()) {
            String token = SecurityToken.createToken(user);
            response.setHeader("X-AUTH-TOKEN", token);
            response.setHeader("Authorization", "Bearer " + token);
            SecurityToken.createCookie("X-CONNEXO-TOKEN", token, "/", WhiteBoard.getTokenExpTime()+ WhiteBoard.getTimeout(), true, response);
            userService.addLoggedInUser(user);
        } else{
            response.setHeader("X-AUTH-TOKEN",xsrf.get().getValue());
            response.setHeader("Authorization", "Bearer " + xsrf.get().getValue());
        }
        return true;
    }

    private boolean deny(HttpServletResponse response) {
        String realm = userService.getRealm();
        response.addHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }

}
