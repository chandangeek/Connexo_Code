package com.elster.jupiter.rest.whiteboard.impl;

import java.io.IOException;
import java.security.*;
import java.util.*;

import javax.servlet.http.*;
import javax.xml.bind.DatatypeConverter;

import org.osgi.service.http.HttpContext;

import com.elster.jupiter.users.User;
import com.google.common.base.Optional;

public class DigestAuthentication implements Authentication {

	@Override
	public boolean handleSecurity(HttpServletRequest request,HttpServletResponse response) throws IOException {
		String authentication = request.getHeader("Authorization");
		if (authentication == null) {
			return denyDigest(response);
		} else {
			System.out.println(authentication);
			Principal user = verifyDigest(request.getMethod(), authentication.split(" ",2)[1]);
			if (user == null) {
			    return denyDigest(response);			    
			}	else {
				return allow(request,user);
			}			
		}
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
	
	private String createNonce() {
		StringBuilder result = new StringBuilder("" + System.currentTimeMillis());
		result.append(":");
		result.append(System.nanoTime());
		return result.toString();
	}
	
	private Principal verifyDigest(String method, String in) {
		DigestResponse digest = new DigestResponse(method, in);
		return digest.getPrincipal();		
	}
		
    private boolean allow(HttpServletRequest request, Principal user) {
        request.setAttribute(HttpContext.AUTHENTICATION_TYPE, HttpServletRequest.BASIC_AUTH);
        request.setAttribute(ServiceLocator.USERPRINCIPAL, user);
        request.setAttribute(HttpContext.REMOTE_USER, user.getName());
        return true;
    }

    static private class DigestResponse {
    	final private String method;
    	final private String in;
    	final private Map<String,String> attributes = new HashMap<>();
    	
    	DigestResponse(String method, String in) {
    		this.method = method;
    		this.in = in;
    		parse();
    	}
    	
    	private void parse() {
    		for (String part : in.split(",")) {
    			String[] subParts = part.split("=",2);						
    			String value = subParts[1];
    			if (value.startsWith("\"")) {
    				value = value.substring(1,value.length()-1);
    			}
    			attributes.put(subParts[0].trim(),value);
    		}
    		for (Map.Entry<String,String> each : attributes.entrySet()) {
    			System.out.println(each.getKey() + ":" + each.getValue());
    		}
    	}
    	    	    	
    	boolean matchHa1(String ha1) {
    		String ha2 = md5(method,attributes.get("uri"));			
    		String calculated = md5(ha1,attributes.get("nonce"),attributes.get("nc"),attributes.get("cnonce"),attributes.get("qop"),ha2); 	
    		return calculated.equals(attributes.get("response"));			
    	}
    	
    	Principal getPrincipal() {
    		Optional<User> user = Bus.getUserService().findUser(attributes.get("username"));
    		if (!user.isPresent()) {
    			return null;
    		}
    		String ha1 = user.get().getDigestHa1();
    		if (ha1 == null || !matchHa1(ha1)) {
    			return null;
    		}
    		return user.get();		
    	}
    		
    	private String md5(String ...strings ) {
    		try {
    			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
    			String separator = null;
    			for (String each : strings) {
    				if (separator == null) {
    					separator = ":";
    				} else {
    					messageDigest.update(separator.getBytes());
    				}
    				messageDigest.update(each.getBytes());
    			}
    			byte[] md5 = messageDigest.digest();
    			return DatatypeConverter.printHexBinary(md5).toLowerCase();
    		} catch (NoSuchAlgorithmException e) {
    			throw new RuntimeException();
    		}
    	}
    }

}
