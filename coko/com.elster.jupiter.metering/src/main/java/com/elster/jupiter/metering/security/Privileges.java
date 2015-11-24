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

	BROWSE_ANY(Constants.BROWSE_ANY, "Browse any usage point"),
	ADMIN_ANY(Constants.ADMIN_ANY, "Administrate any usage point"),
	BROWSE_OWN(Constants.BROWSE_OWN, "Browse own usage point"),
	ADMIN_OWN(Constants.ADMIN_OWN, "Administrate own usage point"),
	VIEW_READINGTYPE(Constants.VIEW_READINGTYPE, "View reading types"),
	ADMINISTRATE_READINGTYPE(Constants.ADMINISTRATE_READINGTYPE, "Administrate reading types");

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
		String VIEW_READINGTYPE = "privilege.view.readingType";
		String ADMINISTRATE_READINGTYPE = "privilege.administrate.readingType";
	}
}
