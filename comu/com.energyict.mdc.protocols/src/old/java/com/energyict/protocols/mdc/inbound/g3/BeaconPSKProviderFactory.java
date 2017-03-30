/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.mdc.inbound.g3;


import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.inbound.InboundDiscoveryContext;
import com.energyict.mdc.protocol.api.services.IdentificationService;

import com.energyict.protocolimplv2.security.DsmrSecuritySupport;

import javax.inject.Provider;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

public class BeaconPSKProviderFactory {

    private static BeaconPSKProviderFactory instance;

    private final boolean provideProtocolJavaClasName;

    /**
     * A list of PSK providers mapped to unique identifier strings.
     */
    private Map<String, BeaconPSKProvider> providers = new HashMap<>();

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
    public synchronized BeaconPSKProvider getPSKProvider(DeviceIdentifier deviceIdentifier, InboundDiscoveryContext context, Clock clock, Thesaurus thesaurus, PropertySpecService propertySpecService, SocketService sockerService, SerialComponentService serialComponentService, IssueService issueService, TopologyService topologyService, MdcReadingTypeUtilService readingTypeUtilService, IdentificationService identificationService, CollectedDataFactory collectedDataFactory, MeteringService meteringService, LoadProfileFactory loadProfileFactory, Provider<DsmrSecuritySupport> dsmrSecuritySupportProvider) {
        if (!providers.containsKey(deviceIdentifier.getIdentifier())) {
            providers.put(deviceIdentifier.getIdentifier(), new BeaconPSKProvider(deviceIdentifier, context, provideProtocolJavaClasName, clock, thesaurus, propertySpecService, sockerService, serialComponentService, issueService, topologyService, readingTypeUtilService, identificationService, collectedDataFactory, meteringService, loadProfileFactory, dsmrSecuritySupportProvider));
        }
        return providers.get(deviceIdentifier.getIdentifier());
    }
}