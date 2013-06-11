package com.elster.jupiter.rest.whiteboard;

import java.io.IOException;
import java.net.URL;
import java.security.Principal;

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
			return denyDigest(response);
		} else {
			System.out.println(authentication);
			Principal user = verifyDigest(request.getMethod(), authentication.split(" ",2)[1]);
			if (user == null) {
			    return denyDigest(response);			    
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
	
	private boolean denyDigest(HttpServletResponse response) {
		String realm = Bus.getUserService().getRealm();
		StringBuilder header = new StringBuilder("Digest realm=");
		appendQuoted(header,realm);
		header.append(",nonce=");
		appendQuoted(header,createNonce());
		header.append(",qop=auth");
		response.addHeader("WWW-Authenticate", header.toString());
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		return false;
	}
	
	private void appendQuoted(StringBuilder builder , String value) {
		builder.append("\"");
		builder.append(value);
		builder.append("\"");		
	}
	
	private String createOpaque() {
		return "1234567890";
	}
	
	private String createNonce() {
		StringBuilder result = new StringBuilder("" + System.currentTimeMillis());
		result.append(":");
		result.append(System.nanoTime());
		return result.toString();
	}
	
	private Principal verifyDigest(String method, String in) {
		DigestResponse digest = new DigestResponse(method, in);
		if (digest.matchPassword("thesame")) {
			return digest.getPrincipal();
		} else {
			return null;
		}
	}
		
}
