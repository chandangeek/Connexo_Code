/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.mdc.inbound.g3;


import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.exceptions.DeviceConfigurationException;
import com.energyict.mdc.protocol.api.inbound.InboundDiscoveryContext;
import com.energyict.mdc.protocol.api.services.IdentificationService;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.Beacon3100;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100ConfigurationSupport;
import com.energyict.protocolimplv2.security.DsmrSecuritySupport;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Clock;

public class BeaconPSKProvider extends G3GatewayPSKProvider {

    private final boolean provideProtocolJavaClasName;
    private final Clock clock;
    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;
    private final SocketService socketService;
    private final SerialComponentService serialComponentService;
    private final IssueService issueService;
    private final TopologyService topologyService;
    private final MdcReadingTypeUtilService readingTypeUtilService;
    private final IdentificationService identificationService;
    private final CollectedDataFactory collectedDataFactory;
    private final MeteringService meteringService;
    private final LoadProfileFactory loadProfileFactory;
    private final Provider<DsmrSecuritySupport> dsmrSecuritySupportProvider;

    @Inject
    public BeaconPSKProvider(DeviceIdentifier deviceIdentifier, InboundDiscoveryContext context, boolean provideProtocolJavaClasName, Clock clock, Thesaurus thesaurus, PropertySpecService propertySpecService, SocketService socketService, SerialComponentService serialComponentService, IssueService issueService, TopologyService topologyService, MdcReadingTypeUtilService readingTypeUtilService, IdentificationService identificationService, CollectedDataFactory collectedDataFactory, MeteringService meteringService, LoadProfileFactory loadProfileFactory, Provider<DsmrSecuritySupport> dsmrSecuritySupportProvider) {
        super(deviceIdentifier, dsmrSecuritySupportProvider, thesaurus, propertySpecService, socketService, issueService, identificationService, collectedDataFactory, meteringService);
        this.provideProtocolJavaClasName = provideProtocolJavaClasName;
        this.clock = clock;
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
        this.socketService = socketService;
        this.serialComponentService = serialComponentService;
        this.issueService = issueService;
        this.topologyService = topologyService;
        this.readingTypeUtilService = readingTypeUtilService;
        this.identificationService = identificationService;
        this.collectedDataFactory = collectedDataFactory;
        this.meteringService = meteringService;
        this.loadProfileFactory = loadProfileFactory;
        this.dsmrSecuritySupportProvider = dsmrSecuritySupportProvider;
    }

    protected DeviceProtocol newGatewayProtocol() {
        return new Beacon3100(clock, thesaurus, propertySpecService, socketService, serialComponentService, issueService, topologyService, readingTypeUtilService, identificationService, collectedDataFactory, meteringService, loadProfileFactory, dsmrSecuritySupportProvider);
    }

    protected DlmsSession getDlmsSession(DeviceProtocol gatewayProtocol) {
        return ((Beacon3100) gatewayProtocol).getDlmsSession();
    }

    /**
     * AES wrap the PSK key with the PSK_ENCRYPTION_KEY
     */
    @Override
    protected OctetString wrap(TypedProperties properties, byte[] pskBytes) {
        final String pskEncryptionKey = properties.getStringProperty(Beacon3100ConfigurationSupport.PSK_ENCRYPTION_KEY);

        if (pskEncryptionKey == null || pskEncryptionKey.isEmpty()) {
            throw DeviceConfigurationException.missingProperty(Beacon3100ConfigurationSupport.PSK_ENCRYPTION_KEY);
        }

        final byte[] pskEncryptionKeyBytes = parseKey(pskEncryptionKey);

        if (pskEncryptionKeyBytes == null) {
            throw DeviceConfigurationException.unsupportedPropertyValue(Beacon3100ConfigurationSupport.PSK_ENCRYPTION_KEY, pskEncryptionKey);
        }

        return OctetString.fromByteArray(ProtocolTools.aesWrap(pskBytes, pskEncryptionKeyBytes));
    }

    /**
     * Extension of the structure: also add the protocol java class name of the slave device.
     * The Beacon3100 then uses this to read out the e-meter serial number using the public client.
     */
    @Override
    protected Structure createMacAndKeyPair(OctetString macAddressOctetString, OctetString wrappedPSKKey, DeviceIdentifier slaveDeviceIdentifier) {
        final Structure macAndKeyPair = super.createMacAndKeyPair(macAddressOctetString, wrappedPSKKey, slaveDeviceIdentifier);

//        //Only add the protcool java class name if it is indicated by the property.
//        if (provideProtocolJavaClasName) {
//            macAndKeyPair.addDataType(OctetString.fromString(context.getInboundDAO().getDeviceProtocol(slaveDeviceIdentifier)));
//        }

        return macAndKeyPair;
    }
}