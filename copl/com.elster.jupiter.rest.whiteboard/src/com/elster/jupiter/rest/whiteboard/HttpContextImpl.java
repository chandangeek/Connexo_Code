package com.elster.jupiter.rest.whiteboard;

import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.http.*;

import com.elster.jupiter.users.User;

public class HttpContextImpl implements HttpContext {

	@Override
	public String getMimeType(String name) {
		return null;
	}

	@Override
	public URL getResource(String name) {
		return getClass().getResource(name);
	}

	@Override
	public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String authentication = request.getHeader("Authorization");
		if (authentication == null) {
			return deny(response);
		} else {
			User user = Bus.getUserService().authenticateBase64(authentication.split(" ")[1]);
			if (user == null) {
			    return deny(response);			    
			}	
			request.setAttribute(AUTHENTICATION_TYPE, HttpServletRequest.BASIC_AUTH);
			request.setAttribute(ServiceLocator.USERPRINCIPAL,user);
			request.setAttribute(REMOTE_USER,user.getName());
			return true;
		}
	}
	
	private boolean deny(HttpServletResponse response) {
		 String realm = Bus.getUserService().getRealm();
		 response.addHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
		 response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		 return false;
	}
	
}
