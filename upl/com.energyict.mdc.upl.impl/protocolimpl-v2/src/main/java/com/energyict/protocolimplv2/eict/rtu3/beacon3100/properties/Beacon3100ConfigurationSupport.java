package com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsConfigurationSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 10/06/2015 - 10:52
 */
public class Beacon3100ConfigurationSupport extends DlmsConfigurationSupport {

    public static final String READCACHE_PROPERTY = "ReadCache";
    public static final String DLMS_METER_KEK = "DlmsMeterKEK";
    public static final String PSK_ENCRYPTION_KEY = "PSKEncryptionKey";
    private static final String DLMS_WAN_KEK = "DlmsWanKEK";

    @Override
    public List<PropertySpec> getOptionalProperties() {
        List<PropertySpec> optionalProperties = new ArrayList<>(super.getOptionalProperties());
        optionalProperties.add(readCachePropertySpec());
        optionalProperties.add(dlmsKEKPropertySpec());
        optionalProperties.add(dlmsWANKEKPropertySpec());
        optionalProperties.add(pskEncryptionKeyPropertySpec());
        optionalProperties.remove(ntaSimulationToolPropertySpec());
        optionalProperties.remove(manufacturerPropertySpec());
        optionalProperties.remove(fixMbusHexShortIdPropertySpec());
        optionalProperties.remove(serverLowerMacAddressPropertySpec()); //Only TCP connection is supported, so no use for server lower mac address
        optionalProperties.remove(deviceId());
        return optionalProperties;
    }

    private PropertySpec dlmsWANKEKPropertySpec() {
        return PropertySpecFactory.encryptedStringPropertySpec(DLMS_WAN_KEK);
    }

    private PropertySpec readCachePropertySpec() {
        return PropertySpecFactory.notNullableBooleanPropertySpec(READCACHE_PROPERTY, false);
    }

    /**
     * A key used for to encrypt other DLMS keys (aka the key encryption key, KEK)
     */
    private PropertySpec dlmsKEKPropertySpec() {
        return PropertySpecFactory.encryptedStringPropertySpec(DLMS_METER_KEK);
    }

    /**
     * Key used to wrap PSK keys before sending them to the Beacon device.
     */
    private PropertySpec pskEncryptionKeyPropertySpec() {
        return PropertySpecFactory.encryptedStringPropertySpec(PSK_ENCRYPTION_KEY);
    }
}