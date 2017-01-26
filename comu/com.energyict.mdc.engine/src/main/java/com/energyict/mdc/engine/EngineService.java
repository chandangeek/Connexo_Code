package com.energyict.mdc.engine;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.nls.NlsService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.engine.impl.cache.DeviceCache;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import java.util.Optional;

/**
 * Provides services that relate to {@link com.energyict.mdc.device.data.Device}s.
 * <p>
 * Copyrights EnergyICT
 * Date: 08/05/14
 * Time: 12:01
 */
@ProviderType
public interface EngineService {

    String COMPONENTNAME = "CES";

    IdentificationService identificationService();

    NlsService nlsService();

    DeviceCache newDeviceCache(DeviceIdentifier device, DeviceProtocolCache deviceProtocolCache);

    Optional<DeviceCache> findDeviceCacheByDevice(Device device);

    Optional<DeviceCache> findDeviceCacheByDeviceIdentifier(DeviceIdentifier device);

    /**
     * Registers a component that is interested to receive
     * notification when this EngineService deactivates.
     *
     * @param deactivationNotificationListener The component
     * @see DeactivationNotificationListener
     */
    void register(DeactivationNotificationListener deactivationNotificationListener);

    /**
     * Unregisters a component that was interested to receive
     * notification when this EngineService deactivates.
     *
     * @param deactivationNotificationListener The component that no longer wants to receive notifications
     * @see DeactivationNotificationListener
     */
    void unregister(DeactivationNotificationListener deactivationNotificationListener);

    /**
     * A DeactivationNotificationListener gets notified when this EngineService
     * is being deactivated by the OSGi framework, most likely because
     * it is being upgraded, its dependents have been deactivated
     * or the entire platform is being shutdown.
     */
    interface DeactivationNotificationListener {

        void engineServiceDeactivationStarted();

    }

}