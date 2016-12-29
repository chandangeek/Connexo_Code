package com.energyict.mdc.upl.migration;

import com.energyict.mdc.upl.security.DeviceProtocolSecurityCapabilities;

/**
 * Used by the ProtocolSecurityRelationTypeUpgrader.
 * <p/>
 * DeviceProtocols should implement this if they have changed their implementation of {@link DeviceProtocolSecurityCapabilities}
 * (e.g. change from DlmsSecuritySupport to DlmsSecuritySuite1And2Support) and support the migration of old, existing security
 * properties (e.g. Password, EncryptionKey, etc) that were created for the previous security set, to the security set of the new type.
 * <p/>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 3/02/2016 - 13:46
 */
public interface MigratePropertiesFromPreviousSecuritySet {

    /**
     * Returns the previous security relation capabilities.
     * <p/>
     * For example, if a protocol has recently changed its implementation of {@link DeviceProtocolSecurityCapabilities}
     * from DlmsSecuritySupport to DlmsSecuritySuite1And2Support, then this should return DlmsSecuritySupport.
     * <p/>
     */
    DeviceProtocolSecurityCapabilities getPreviousSecuritySupport();

}