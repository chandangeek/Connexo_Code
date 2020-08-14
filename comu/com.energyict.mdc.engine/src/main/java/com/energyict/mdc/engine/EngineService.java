/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine;

import com.elster.jupiter.nls.NlsService;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.engine.impl.cache.DeviceCache;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

/**
 * Provides services that relate to {@link Device}s.
 * <p>
 * <p>
 * Date: 08/05/14
 * Time: 12:01
 */
@ProviderType
public interface EngineService {

    String COMPONENTNAME = "CES";

    boolean isOnlineMode();

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

    void activateComServer();

    boolean isAdaptiveQuery();

    long getPrefetchComTaskTimeDelta();

    long getPrefetchComTaskDelay();

    long getPrefetchComTaskLimit();

    boolean isPrefetchBalanced();

    boolean isPrefetchEnabled();

//    OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> newOnlineComServerBuilder();
//
//    ComServer.ComServerBuilder<? extends OfflineComServer, ? extends ComServer.ComServerBuilder> newOfflineComServerBuilder();
//
//    RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> newRemoteComServerBuilder();

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