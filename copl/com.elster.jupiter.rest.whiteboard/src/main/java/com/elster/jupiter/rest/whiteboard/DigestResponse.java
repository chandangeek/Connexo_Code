package com.elster.jupiter.rest.whiteboard;

import java.security.*;
import java.util.*;

import javax.xml.bind.DatatypeConverter;

public class DigestResponse {
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
	
	boolean matchPassword(String password) {		
		String ha1 = md5(attributes.get("username"),attributes.get("realm"),password);
		return matchHa1(ha1);		
	}
	
	boolean matchHa1(String ha1) {
		String ha2 = md5(method,attributes.get("uri"));			
		String calculated = md5(ha1,attributes.get("nonce"),attributes.get("nc"),attributes.get("cnonce"),attributes.get("qop"),ha2); 	
		return calculated.equals(attributes.get("response"));			
	}
	
	public Principal getPrincipal() {
		return new Principal() {
			
			@Override
			public String getName() {
				return attributes.get("username");
			}
		};
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
	
