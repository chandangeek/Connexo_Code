package com.energyict.mdc.protocol.inbound.g3;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import org.osgi.framework.FrameworkUtil;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 23/11/2015 - 9:39
 */
public class BeaconPSKProviderFactory {

    private static final String PROVIDERS_MAX_CAPACITY_ATTRIBUTE_NAME = "com.energyict.mdc.protocol.inbound.g3.beaconPSKProviderFactory.providersMaxCapacity";
    private static final int providersMaxCapacity;

    private static BeaconPSKProviderFactory instance;

    private final boolean provideProtocolJavaClasName;

    /**
     * A list of PSK providers mapped to unique identifier strings.
     */
    private LinkedHashMap<DeviceIdentifier, BeaconPSKProvider> providers;

    static {
        providersMaxCapacity = Integer.parseInt(getSystemProperty(PROVIDERS_MAX_CAPACITY_ATTRIBUTE_NAME, "250"));
    }

    private BeaconPSKProviderFactory(boolean provideProtocolJavaClasName) {
        this.provideProtocolJavaClasName = provideProtocolJavaClasName;
        this.providers =
                new LinkedHashMap<DeviceIdentifier, BeaconPSKProvider>(providersMaxCapacity, 0.75f, true) {
                    @Override
                    protected boolean removeEldestEntry(Map.Entry eldest) {
                        return size() > providersMaxCapacity;
                    }
                };
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
    public synchronized BeaconPSKProvider getPSKProvider(DeviceIdentifier deviceIdentifier) {
        if (!providers.containsKey(deviceIdentifier)) {
            providers.put(deviceIdentifier, new BeaconPSKProvider(deviceIdentifier, provideProtocolJavaClasName));
        }
        return providers.get(deviceIdentifier);
    }

    private static String getSystemProperty(String propertyName, String defaultValue) {
        try {
            String property = FrameworkUtil.getBundle(BeaconPSKProviderFactory.class).getBundleContext().getProperty(propertyName);

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