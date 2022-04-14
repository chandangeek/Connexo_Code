/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.systemadmin.rest.imp.response;

import org.osgi.framework.BundleContext;

import javax.inject.Inject;

public class ConnexoConfigPropertiesFactory {

    private final BundleContext bundleContext;
    private static final String CONNEXO_TIMEOUT = "com.elster.jupiter.systemadmin.timeout";
    private static final Integer CONNEXO_DEFAULT_TIMEOUT = 60000;

    @Inject
    public ConnexoConfigPropertiesFactory(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public Integer getTimeoutFromConfigProperties() {
        String timeout = bundleContext.getProperty(CONNEXO_TIMEOUT);
        try {
            return Integer.parseInt(timeout);
        } catch (NumberFormatException exception) {
            return CONNEXO_DEFAULT_TIMEOUT;
        }
    }
}
