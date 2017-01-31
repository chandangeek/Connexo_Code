/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine;

import com.elster.jupiter.nls.NlsService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.engine.impl.cache.DeviceCache;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.services.IdentificationService;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

@ProviderType
public interface EngineService {

    public static String COMPONENTNAME = "CES";

    public IdentificationService identificationService();

    public NlsService nlsService();

    public DeviceCache newDeviceCache(Device device, DeviceProtocolCache deviceProtocolCache);

    public Optional<DeviceCache> findDeviceCacheByDevice(Device device);

    /**
     * Registers a component that is interested to receive
     * notification when this EngineService deactivates.
     *
     * @param deactivationNotificationListener The component
     * @see DeactivationNotificationListener
     */
    public void register (DeactivationNotificationListener deactivationNotificationListener);

    /**
     * Unregisters a component that was interested to receive
     * notification when this EngineService deactivates.
     *
     * @param deactivationNotificationListener The component that no longer wants to receive notifications
     * @see DeactivationNotificationListener
     */
    public void unregister (DeactivationNotificationListener deactivationNotificationListener);

    /**
     * A DeactivationNotificationListener gets notified when this EngineService
     * is being deactivated by the OSGi framework, most likely because
     * it is being upgraded, its dependents have been deactivated
     * or the entire platform is being shutdown.
     */
    public interface DeactivationNotificationListener {

        public void engineServiceDeactivationStarted();

    }

}