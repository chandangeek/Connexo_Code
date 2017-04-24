/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.mdc.inbound.g3;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.inbound.InboundDiscoveryContext;
import com.energyict.mdc.protocol.api.services.IdentificationService;

import com.energyict.protocolimplv2.security.DsmrSecuritySupport;

import javax.inject.Provider;
import java.util.HashMap;
import java.util.Map;

public class G3GatewayPSKProviderFactory {

    private static G3GatewayPSKProviderFactory instance;

    /**
     * A list of PSK providers mapped to unique identifier strings.
     */
    private Map<String, G3GatewayPSKProvider> providers = new HashMap<>();

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
    public synchronized G3GatewayPSKProvider getPSKProvider(DeviceIdentifier deviceIdentifier, InboundDiscoveryContext context, Provider<DsmrSecuritySupport> securityProvider, Thesaurus thesaurus, PropertySpecService propertySpecService, SocketService socketService, IssueService issueService, IdentificationService identificationService, CollectedDataFactory collectedDataFactory, MeteringService meteringService) {
        if (!providers.containsKey(deviceIdentifier.getIdentifier())) {
            providers.put(deviceIdentifier.getIdentifier(), new G3GatewayPSKProvider(deviceIdentifier, securityProvider, thesaurus, propertySpecService, socketService, issueService, identificationService, collectedDataFactory, meteringService));
        }
        return providers.get(deviceIdentifier.getIdentifier());
    }
}
