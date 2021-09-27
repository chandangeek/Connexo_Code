package com.energyict.mdc.protocol.inbound.g3;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import org.osgi.framework.FrameworkUtil;


import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;


/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 10/11/2016 - 17:23
 */
public class CryptoBeaconPSKProviderFactory {

    private static final String PROVIDERS_MAX_CAPACITY_ATTRIBUTE_NAME = "com.energyict.mdc.protocol.inbound.g3.beaconPSKProviderFactory.providersMaxCapacity";
    private static final int providersMaxCapacity;

    private static CryptoBeaconPSKProviderFactory instance;

    private final boolean provideProtocolJavaClasName;

    Logger logger = Logger.getLogger(this.getClass().getName());


    /**
     * A list of PSK providers mapped to unique identifier strings.
     */
    private LinkedHashMap<DeviceIdentifier, CryptoBeaconPSKProvider> providers;

    static {
        providersMaxCapacity = Integer.parseInt(getSystemProperty(PROVIDERS_MAX_CAPACITY_ATTRIBUTE_NAME, "250"));
    }

    private CryptoBeaconPSKProviderFactory(boolean provideProtocolJavaClasName) {
        this.provideProtocolJavaClasName = provideProtocolJavaClasName;
        this.providers =
                new LinkedHashMap<DeviceIdentifier, CryptoBeaconPSKProvider>(providersMaxCapacity, 0.75f, true) {
                    @Override
                    protected boolean removeEldestEntry(Map.Entry eldest) {
                        boolean willRemove = size() > providersMaxCapacity;
                        logger.info("[PSK] Provider capacity "+ getUsageStatus()+". Will remove eldest: "+willRemove);
                        return willRemove;
                    }
                };
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
            logger.info("[PSK]["+deviceIdentifier+"] Creating new PSK-provider "+ getUsageStatus());
            providers.put(deviceIdentifier, new CryptoBeaconPSKProvider(deviceIdentifier, provideProtocolJavaClasName));
            logger.info("[PSK] Creating new PSK-provider for "+deviceIdentifier.toString());
        } else {
            logger.info("[PSK] Reusing PSK-provider for "+deviceIdentifier.toString());
        }

        return providers.get(deviceIdentifier);
    }


    private String getUsageStatus() {
        if (providers != null) {
            return " - using " + providers.size() + "/" + providersMaxCapacity;
        } else {
            return " providers not-initialized";
        }
    }


    private static String getSystemProperty(String propertyName, String defaultValue) {
        try {
            String property = FrameworkUtil.getBundle(CryptoBeaconPSKProviderFactory.class).getBundleContext().getProperty(propertyName);

            if (property != null) {
                return property;
            } else {
                System.err.println("System configuration property [" + propertyName + "] is not defined, using default value of " + defaultValue);
            }
        } catch (Exception ex) {
            System.err.println("Cannot get system configuration property [" + propertyName + "] using default value of " + defaultValue + ": " + ex.getLocalizedMessage());
        }
        return defaultValue;
    }
}