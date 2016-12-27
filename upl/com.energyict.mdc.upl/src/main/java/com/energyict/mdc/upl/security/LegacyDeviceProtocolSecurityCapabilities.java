package com.energyict.mdc.upl.security;

import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 9/10/2014 - 17:58
 */
public interface LegacyDeviceProtocolSecurityCapabilities extends DeviceProtocolSecurityCapabilities {

    /**
     * Returns a list of legacy security properties.
     * In 8.11, some security related properties had other names (e.g. DataTransportEncryptionKey instead of EncryptionKey).
     * These old keys should removed from the general properties and can be accessed here.
     */
    List<String> getLegacySecurityProperties();

}