package com.energyict.mdc.protocol.pluggable;

import com.energyict.mdc.common.license.License;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Temporary fix to publish the License to the protocol classes.
 * Will change once license management is moved to the Jupiter Kore.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-16 (17:21)
 */
public final class LicenseServer {

    public static final AtomicReference<License> licenseHolder = new AtomicReference<>();

}