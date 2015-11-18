package com.elster.insight.usagepoint.config.security;

import com.elster.jupiter.nls.TranslationKey;
import java.util.Arrays;
import java.util.stream.Collectors;

public enum Privileges implements TranslationKey {

	ADMIN_ANY_METROLOGY_CONFIG(Constants.ADMIN_ANY_METROLOGY_CONFIG, "Administrate any metrology configuration"),
	BROWSE_ANY_METROLOGY_CONFIG(Constants.BROWSE_ANY_METROLOGY_CONFIG, "Browse any metrology configuration");


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
        String ADMIN_ANY_METROLOGY_CONFIG = "UCR_ADMIN_ANY_METROLOGY_CONFIG";
        String BROWSE_ANY_METROLOGY_CONFIG = "UCR_BROWSE_ANY_METROLOGY_CONFIG";
	}
}
