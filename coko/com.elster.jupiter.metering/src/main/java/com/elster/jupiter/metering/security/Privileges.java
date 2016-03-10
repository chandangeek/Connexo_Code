package com.elster.jupiter.metering.security;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.conditions.Constant;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum Privileges implements TranslationKey {

    //Privileges
    VIEW_ANY_USAGEPOINT(Constants.VIEW_ANY_USAGEPOINT, "View any usage point"),
    ADMINISTER_ANY_USAGEPOINT(Constants.ADMINISTER_ANY_USAGEPOINT, "Administer any usage point"),
    VIEW_OWN_USAGEPOINT(Constants.VIEW_OWN_USAGEPOINT, "View own usage point"),
    ADMINISTER_OWN_USAGEPOINT(Constants.ADMINISTER_OWN_USAGEPOINT, "Administer own usage point"),
	ADMINISTER_USAGEPOINT_TIME_SLICED_CPS(Constants.ADMINISTER_USAGEPOINT_TIME_SLICED_CPS, "Administer usage point time-sliced CAS"),

	VIEW_READINGTYPE(Constants.VIEW_READINGTYPE, "View reading types"),
    ADMINISTER_READINGTYPE(Constants.ADMINISTER_READINGTYPE, "Administer reading types"),

	VIEW_SERVICECATEGORY(Constants.VIEW_SERVICECATEGORY, "View service categories"),

    ADMINISTER_METROLOGY_CONFIGURATION(Constants.ADMINISTER_METROLOGY_CONFIGURATION, "Administer metrology configuration"),
    VIEW_METROLOGY_CONFIGURATION(Constants.VIEW_METROLOGY_CONFIGURATION, "View metrology configuration")
	;

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
	@ProviderType
	public interface Constants {
        String VIEW_ANY_USAGEPOINT = "privilege.view.anyUsagePoint";
        String ADMINISTER_ANY_USAGEPOINT = "privilege.administer.anyUsagePoint";
        String VIEW_OWN_USAGEPOINT = "privilege.view.ownUsagePoint";
        String ADMINISTER_OWN_USAGEPOINT = "privilege.administer.ownUsagePoint";
        String VIEW_READINGTYPE = "privilege.view.readingType";
        String ADMINISTER_READINGTYPE = "privilege.administer.readingType";
        String VIEW_SERVICECATEGORY = "privilege.view.serviceCategory";
        String VIEW_METROLOGY_CONFIGURATION = "privilege.view.metrologyConfiguration";
        String ADMINISTER_METROLOGY_CONFIGURATION = "privilege.administer.metrologyConfiguration";
		String ADMINISTER_USAGEPOINT_TIME_SLICED_CPS = "privilege.administer.usage.point.time.sliced.cps";
	}
}