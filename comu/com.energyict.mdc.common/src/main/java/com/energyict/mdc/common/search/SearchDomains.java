/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.search;

/**
 * Provides common constants that relate to the search domains
 * that are supported by the MultiSense application bundles.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-20 (09:02)
 */
public enum SearchDomains {

    DEVICE;

    /**
     * The String that should be used by all search domains
     * that can only be within MultiSense and should
     * therefore not appear in UI components of other applications.
     */
    public static final String SEARCH_DOMAIN_APPLICATION_KEY = "COMU";

}