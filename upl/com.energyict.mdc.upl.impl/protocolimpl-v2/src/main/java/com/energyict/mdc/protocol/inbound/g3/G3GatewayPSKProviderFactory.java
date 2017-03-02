package com.energyict.mdc.protocol.inbound.g3;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 23/11/2015 - 9:39
 */
public class G3GatewayPSKProviderFactory {

    private static G3GatewayPSKProviderFactory instance;

    /**
     * A list of PSK providers mapped to unique identifier strings.
     */
    private Map<DeviceIdentifier, G3GatewayPSKProvider> providers = new HashMap<>();

    private G3GatewayPSKProviderFactory() {
    }

    public static synchronized G3GatewayPSKProviderFactory getInstance() {
        if (instance == null) {
            instance = new G3GatewayPSKProviderFactory();
        }
        return instance;
    }

    /**
     * Note that this method is synchronized so only one thread at a time can fetch a PSK provider.
     * This is to avoid troubles when 2 threads simultaneously would trigger the creation of a new PSK provider twice.
     */
    public synchronized G3GatewayPSKProvider getPSKProvider(DeviceIdentifier deviceIdentifier) {
        if (!providers.containsKey(deviceIdentifier)) {
            providers.put(deviceIdentifier, new G3GatewayPSKProvider(deviceIdentifier));
        }
        return providers.get(deviceIdentifier);
    }
}