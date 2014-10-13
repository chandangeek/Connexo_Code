package com.elster.jupiter.rest.whiteboard.impl;

import java.io.IOException;

import javax.servlet.http.*;

import org.osgi.service.http.HttpContext;

import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import java.util.Optional;

public class BasicAuthentication implements Authentication {
	private final UserService userService;
	
	BasicAuthentication(UserService userService) {
		this.userService = userService;
	}

	@Override
	public boolean handleSecurity(HttpServletRequest request,HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        String authentication = request.getHeader("Authorization");

        if(session != null){
            User currentSessionUser = getUserAuthenticated(session);
            if (currentSessionUser != null){
                return allow(request, currentSessionUser);
            }
        }
        else {
            if (request.getHeader("referer") != null){
                return deny(response);
            }
        }

        if (authentication == null){
            return deny(response);
        }

        Optional<User> user = userService.authenticateBase64(authentication.split(" ")[1]);
        return user.isPresent() ? allow(request, user.get()) : deny(response);
    }

    private boolean allow(HttpServletRequest request, User user) {
        request.setAttribute(HttpContext.AUTHENTICATION_TYPE, HttpServletRequest.BASIC_AUTH);
        request.setAttribute(WhiteBoardConfiguration.USERPRINCIPAL, user);
        request.setAttribute(HttpContext.REMOTE_USER, user.getName());
        request.getSession(true).setAttribute("user", user);
        return true;
    }

    private boolean deny(HttpServletResponse response) {
        String realm = userService.getRealm();
        response.addHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }

    private User getUserAuthenticated(HttpSession session) {
        return (User)session.getAttribute("user");
    }

}
