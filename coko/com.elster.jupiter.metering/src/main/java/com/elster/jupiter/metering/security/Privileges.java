package com.elster.jupiter.metering.security;

/**
 * Not an enum, otherwise it won't work for RolesAllowed.
 */
/*public interface Privileges {

}*/

import com.elster.jupiter.nls.TranslationKey;
import java.util.Arrays;
import java.util.stream.Collectors;

public enum Privileges implements TranslationKey {

	BROWSE_ANY(Constants.BROWSE_ANY, "Browse Any Usage Point"),
	ADMIN_ANY(Constants.ADMIN_ANY, "Administrate Any Usage Point"),
	BROWSE_OWN(Constants.BROWSE_OWN, "Browse Own Usage Point"),
	ADMIN_OWN(Constants.ADMIN_OWN, "Administrate Own Usage Point");

	private final String key;
	private final String description;

	Privileges(String key, String description) {
		this.key = key;
		this.description = description;
	}

	public String getKey() {
		return key;
	}

	@Override
	public String getDefaultFormat() {
		return getDescription();
	}

	public String getDescription() {
		return description;
	}

	public static String[] keys() {
		return Arrays.stream(Privileges.values())
				.map(Privileges::getKey)
				.collect(Collectors.toList())
				.toArray(new String[Privileges.values().length]);
	}

	public interface Constants {
		String BROWSE_ANY = "MTR_BROWSE_ANYUSAGEPOINT";
		String ADMIN_ANY = "MTR_ADMIN_ANYUSAGEPOINT";
		String BROWSE_OWN = "MTR_BROWSE_OWNUSAGEPOINT";
		String ADMIN_OWN = "MTR_ADMIN_OWN";
	}
}
