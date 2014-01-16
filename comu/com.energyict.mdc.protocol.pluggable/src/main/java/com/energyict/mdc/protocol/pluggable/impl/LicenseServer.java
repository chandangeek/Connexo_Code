package com.energyict.mdc.protocol.pluggable.impl;

import com.energyict.mdc.common.license.License;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-16 (17:21)
 */
public final class LicenseServer {

    public static final AtomicReference<License> licenseHolder = new AtomicReference<>();

}