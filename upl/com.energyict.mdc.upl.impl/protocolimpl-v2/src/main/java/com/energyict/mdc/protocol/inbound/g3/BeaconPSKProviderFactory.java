package com.energyict.mdc.protocol.inbound.g3;

import com.energyict.mdc.protocol.inbound.InboundDiscoveryContext;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 23/11/2015 - 9:39
 */
public class BeaconPSKProviderFactory {

    private static BeaconPSKProviderFactory instance;

    private final boolean provideProtocolJavaClasName;

    /**
     * A list of PSK providers mapped to unique identifier strings.
     */
    private Map<DeviceIdentifier, BeaconPSKProvider> providers = new HashMap<>();

    private BeaconPSKProviderFactory(boolean provideProtocolJavaClasName) {
        this.provideProtocolJavaClasName = provideProtocolJavaClasName;
    }

    public static synchronized BeaconPSKProviderFactory getInstance(boolean provideProtocolJavaClasName) {
        if (instance == null) {
            instance = new BeaconPSKProviderFactory(provideProtocolJavaClasName);
        }
        return instance;
    }

    /**
     * Note that this method is synchronized so only one thread at a time can fetch a PSK provider.
     * This is to avoid troubles when 2 threads simultaneously would trigger the creation of a new PSK provider twice.
     */
    public synchronized BeaconPSKProvider getPSKProvider(DeviceIdentifier deviceIdentifier, InboundDiscoveryContext context) {
        if (!providers.containsKey(deviceIdentifier)) {
            providers.put(deviceIdentifier, new BeaconPSKProvider(deviceIdentifier, context, provideProtocolJavaClasName));
        }
        return providers.get(deviceIdentifier);
    }
}