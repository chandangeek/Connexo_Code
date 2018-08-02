package com.energyict.mdc.protocol.inbound.g3;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 10/11/2016 - 17:23
 */
public class CryptoBeaconPSKProviderFactory {

    private static CryptoBeaconPSKProviderFactory instance;

    private final boolean provideProtocolJavaClasName;

    /**
     * A list of PSK providers mapped to unique identifier strings.
     */
    private Map<DeviceIdentifier, CryptoBeaconPSKProvider> providers = new HashMap<>();

    private CryptoBeaconPSKProviderFactory(boolean provideProtocolJavaClasName) {
        this.provideProtocolJavaClasName = provideProtocolJavaClasName;
    }

    public static synchronized CryptoBeaconPSKProviderFactory getInstance(boolean provideProtocolJavaClasName) {
        if (instance == null) {
            instance = new CryptoBeaconPSKProviderFactory(provideProtocolJavaClasName);
        }
        return instance;
    }

    /**
     * Note that this method is synchronized so only one thread at a time can fetch a PSK provider.
     * This is to avoid troubles when 2 threads simultaneously would trigger the creation of a new PSK provider twice.
     */
    public synchronized CryptoBeaconPSKProvider getPSKProvider(DeviceIdentifier deviceIdentifier) {
        if (!providers.containsKey(deviceIdentifier)) {
            providers.put(deviceIdentifier, new CryptoBeaconPSKProvider(deviceIdentifier, provideProtocolJavaClasName));
        }
        return providers.get(deviceIdentifier);
    }
}