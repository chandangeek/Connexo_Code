/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.eictz3;

/**
 * Enumerates the different Authentication levels.
 * 
 * @author alex
 */
public enum AuthenticationLevel {

	NO_AUTHENTICATION(0), LOW_LEVEL(1), HIGH_LEVEL(2), HIGH_LEVEL_MD5(3), HIGH_LEVEL_SHA1(4), HIGH_LEVEL_GMAC(5);
	
	private final int authenticationValue;
	
	/**
	 * Create a new instance.
	 * 
	 * @param 	authenticationValue		The authentication value.
	 */
	private AuthenticationLevel(final int authenticationValue) {
		this.authenticationValue = authenticationValue;
	}
	
	/**
	 * Returns the security level that corresponds to the given property value.
	 * 
	 * @param 	propertyValue		The property value.
	 * 
	 * @return	The matching {@link AuthenticationLevel}, <code>null</code> if none matches.
	 */
	public static final AuthenticationLevel getByPropertyValue(final int propertyValue) {
		for (final AuthenticationLevel level : values()) {
			if (level.authenticationValue == propertyValue) {
				return level;
			}
		}
		
		return null;
	}

	/**
	 * Returns the property value associated with the level.
	 * 
	 * @return	The property value associated with the level.
	 */
	public final int getAuthenticationValue() {
		return this.authenticationValue;
	}
}
