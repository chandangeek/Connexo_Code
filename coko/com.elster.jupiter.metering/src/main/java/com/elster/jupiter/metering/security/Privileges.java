package com.elster.jupiter.metering.security;

/**
 * Not an enum, otherwise it won't work for RolesAllowed.
 */
public interface Privileges {
	String BROWSE_ANY = "MTR_BROWSE_ANYUSAGEPOINT";
	String ADMIN_ANY = "MTR_ADMIN_ANYUSAGEPOINT";
	String BROWSE_OWN = "MTR_BROWSE_OWNUSAGEPOINT";
	String ADMIN_OWN = "MTR_ADMIN_OWN";
}
