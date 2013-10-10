package com.elster.jupiter.rest.whiteboard.impl;

import java.io.IOException;

import javax.servlet.http.*;

import org.osgi.service.http.HttpContext;

import com.elster.jupiter.users.User;
import com.google.common.base.Optional;

public class BasicAuthentication implements Authentication {

	@Override
	public boolean handleSecurity(HttpServletRequest request,HttpServletResponse response) throws IOException {
		String authentication = request.getHeader("Authorization");
        if (authentication == null) {
            return deny(response);
        }
        Optional<User> user = Bus.getUserService().authenticateBase64(authentication.split(" ")[1]);
        return user.isPresent() ? allow(request, user.get()) : deny(response);
    }

    private boolean allow(HttpServletRequest request, User user) {
        request.setAttribute(HttpContext.AUTHENTICATION_TYPE, HttpServletRequest.BASIC_AUTH);
        request.setAttribute(ServiceLocator.USERPRINCIPAL, user);
        request.setAttribute(HttpContext.REMOTE_USER, user.getName());
        return true;
    }

    private boolean deny(HttpServletResponse response) {
        String realm = Bus.getUserService().getRealm();
        response.addHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }

}
