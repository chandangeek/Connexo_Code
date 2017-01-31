/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.mdc.inbound.g3;


import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.HexString;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.io.ConnectionCommunicationException;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.MessageSeeds;
import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.offline.DeviceOfflineFlags;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.mdc.protocol.api.exceptions.ConnectionSetupException;
import com.energyict.mdc.protocol.api.inbound.InboundDiscoveryContext;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.protocols.impl.channels.ip.socket.OutboundTcpIpConnectionType;

import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.G3NetworkManagement;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.dlms.g3.G3Properties;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.RtuPlusServer;
import com.energyict.protocolimplv2.security.DsmrSecuritySupport;
import com.google.common.collect.Range;

import javax.inject.Provider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

public class G3GatewayPSKProvider {

    private final DeviceIdentifier deviceIdentifier;
    protected ComChannel tcpComChannel;

    private Set<String> joiningMacAddresses = Collections.synchronizedSet(new HashSet<String>());
    private DeviceProtocol gatewayProtocol = null;
    private final Provider<DsmrSecuritySupport> securityProvider;
    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;
    private final SocketService socketService;
    private final IssueService issueService;
    private final IdentificationService identificationService;
    private final CollectedDataFactory collectedDataFactory;
    private final MeteringService meteringService;

    public G3GatewayPSKProvider(DeviceIdentifier deviceIdentifier, Provider<DsmrSecuritySupport> securityProvider, Thesaurus thesaurus, PropertySpecService propertySpecService, SocketService socketService, IssueService issueService, IdentificationService identificationService, CollectedDataFactory collectedDataFactory, MeteringService meteringService) {
        this.deviceIdentifier = deviceIdentifier;
        this.securityProvider = securityProvider;
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
        this.socketService = socketService;
        this.issueService = issueService;
        this.identificationService = identificationService;
        this.collectedDataFactory = collectedDataFactory;
        this.meteringService = meteringService;
    }

    protected DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    /**
     * Add a mac address to the list of joining mac addresses.
     * This is done when receiving an inbound 'join attempt' notification from the beacon for a certain AM540 node.
     */
    public synchronized void addJoiningMacAddress(String macAddress) {
        joiningMacAddresses.add(macAddress);
    }

    /**
     * This method is synchronized, so that only 1 thread at a time can call it.
     * The other threads that want to call this method (on the same instance of this class) will automatically wait.
     */
    public synchronized void providePSK(String macAddress, DeviceProtocolSecurityPropertySet securityPropertySet, InboundDiscoveryContext context) {

        if (!joiningMacAddresses.contains(macAddress)) {
            //Another thread already provided the PSK for this MAC address, cool! Let's move on.
            return;
        }

        try {
            DeviceProtocol gatewayProtocol = getGatewayProtocol(securityPropertySet, context);
            providePSK(gatewayProtocol, context);
            joiningMacAddresses.remove(macAddress);
        } catch (CommunicationException e) {
            communicationError("Unexpected CommunicationException occurred while trying to provide PSKs to the Beacon. Closing the TCP connection.", context);
            throw e;
        }

        if (joiningMacAddresses.isEmpty()) {
            try {
                //Our job is done here (for now), release the association and close the TCP connection
                context.logOnAllLoggerHandlers("Successfully provided PSKs for all joining nodes, releasing the association and closing the TCP connection.", Level.INFO);
                if (tcpComChannel != null && gatewayProtocol != null) {
                    this.gatewayProtocol.logOff();
                    this.gatewayProtocol.terminate();
                }
            } finally {
                try {
                    if (tcpComChannel != null) {
                        this.tcpComChannel.close();
                    }
                } finally {
                    this.gatewayProtocol = null;
                    this.tcpComChannel = null;
                }
            }
        }
    }

    /**
     * This method is synchronized, so that only 1 thread at a time can call it.
     * The other threads that want to call this method (on the same instance of this class) will automatically wait.
     * Close the TCP connection, this will also release the current association to the beacon.
     * The next inbound frame will set it up again.
     *
     * @param errorMessage
     * @param context
     */
    public synchronized void provideError(String errorMessage, InboundDiscoveryContext context) {
        communicationError(errorMessage, context);
    }

    /**
     * Close the TCP connection, this will also release the current association to the beacon.
     * The next inbound frame will set it up again.
     *
     * @param errorMessage
     * @param context
     */
    private void communicationError(String errorMessage, InboundDiscoveryContext context) {
        try {
            context.logOnAllLoggerHandlers(errorMessage, Level.WARNING);
            if (tcpComChannel != null) {
                this.tcpComChannel.close();
            }
        } finally {
            this.gatewayProtocol = null;
            this.tcpComChannel = null;
        }
    }

    protected DlmsSession getDlmsSession(DeviceProtocol gatewayProtocol) {
        return ((RtuPlusServer) gatewayProtocol).getDlmsSession();
    }

    /**
     * Lazy initialization.
     * No need to associate again to the Beacon if the protocol instance already exists.
     */
    private DeviceProtocol getGatewayProtocol(DeviceProtocolSecurityPropertySet securityPropertySet, InboundDiscoveryContext context) {
        if (gatewayProtocol == null) {
            gatewayProtocol = initializeGatewayProtocol(securityPropertySet, context);
        }
        return gatewayProtocol;
    }

    /**
     * Create a protocol instance that will setup a DLMS session to the RTU+Server
     * JUnit test overrides this
     */
    protected DeviceProtocol initializeGatewayProtocol(DeviceProtocolSecurityPropertySet securityPropertySet, InboundDiscoveryContext context) {
        DeviceProtocol gatewayProtocol = newGatewayProtocol();
        final TypedProperties deviceProtocolProperties = context.getDeviceProtocolProperties(getDeviceIdentifier());
        TypedProperties protocolProperties = deviceProtocolProperties == null ? TypedProperties.empty() : deviceProtocolProperties;
        protocolProperties.setProperty(DlmsProtocolProperties.READCACHE_PROPERTY, false);
        TypedProperties dialectProperties = TypedProperties.empty();
        addDefaultValuesIfNecessary(gatewayProtocol, dialectProperties);

        DLMSCache dummyCache = new DLMSCache(new UniversalObject[0], 0);     //Empty cache, prevents that the protocol will read out the object list
        Optional<OfflineDevice> offlineDevice = context.getOfflineDevice(getDeviceIdentifier(), new DeviceOfflineFlags());   //Empty flags means don't load any master data
        if (offlineDevice.isPresent()) {

            createTcpComChannel(context);
            context.logOnAllLoggerHandlers("Creating a new DLMS session to Beacon device '" + getDeviceIdentifier().getIdentifier() + "', to provide the PSK key(s)", Level.INFO);
            gatewayProtocol.setDeviceCache(dummyCache);
            gatewayProtocol.copyProperties(protocolProperties);
            gatewayProtocol.addDeviceProtocolDialectProperties(dialectProperties);
            gatewayProtocol.setSecurityPropertySet(securityPropertySet);
            gatewayProtocol.init(offlineDevice.get(), tcpComChannel);
            gatewayProtocol.logOn();
        }
        return gatewayProtocol;
    }

    protected DeviceProtocol newGatewayProtocol() {
        return new RtuPlusServer(thesaurus, propertySpecService, socketService, issueService, identificationService, collectedDataFactory, meteringService, securityProvider);
    }

    /**
     * For all properties who are not yet specified - but for which a default value exist - the default value will be added.
     */
    private void addDefaultValuesIfNecessary(DeviceProtocol gatewayProtocol, TypedProperties dialectProperties) {
        DeviceProtocolDialect theActualDialect = gatewayProtocol.getDeviceProtocolDialects().get(0);
        theActualDialect.getPropertySpecs()
                .stream()
                .filter(propertySpec -> !dialectProperties.hasValueFor(propertySpec.getName()) && propertySpec.getPossibleValues() != null)
                .forEach(propertySpec -> {
                    dialectProperties.setProperty(propertySpec.getName(), propertySpec.getPossibleValues().getDefault());
                });
    }

    private void createTcpComChannel(InboundDiscoveryContext context) {
        boolean tlsConnection = false;
        TypedProperties connectionProperties = context.getOutboundConnectionTypeProperties(getDeviceIdentifier());
//        if(connectionProperties.getProperty(TLSConnectionType.TLS_VERSION_PROPERTY_NAME) != null){
//            tlsConnection = true;
//            context.logOnAllLoggerHandlers("Setting up a new TLS connection to Beacon device '" + getDeviceIdentifier().getIdentifier() + "', to provide the PSK key(s)", Level.INFO);
//        }
        context.logOnAllLoggerHandlers("Setting up a new outbound TCP connection to Beacon device '" + getDeviceIdentifier().getIdentifier() + "', to provide the PSK key(s)", Level.INFO);
        List<ConnectionProperty> connectionTaskProperties = toPropertySpecs(new Date(), connectionProperties);

        try {
            tcpComChannel = new OutboundTcpIpConnectionType(thesaurus, propertySpecService, socketService).connect(connectionTaskProperties);
        } catch (ConnectionException e) {
            throw new ConnectionSetupException(MessageSeeds.UNEXPECTED_PROTOCOL_ERROR, e);
        }
    }

    /**
     * Read out attribute 'joining_slaves' of object G3NetworkManagement. It is the list of slaves that are joining and need a PSK.
     * Find their PSK properties in EIServer and provide them to the Rtu+Server.
     */
    protected void providePSK(DeviceProtocol gatewayProtocol, InboundDiscoveryContext context) {
        DlmsSession dlmsSession = getDlmsSession(gatewayProtocol);
        G3NetworkManagement g3NetworkManagement;
        try {
            g3NetworkManagement = dlmsSession.getCosemObjectFactory().getG3NetworkManagement();
        } catch (ProtocolException e) {
            throw new ConnectionCommunicationException(MessageSeeds.UNEXPECTED_PROTOCOL_ERROR, e);
        }

        try {
            Array macKeyPairs = new Array();

            Array joiningNodes = g3NetworkManagement.getJoiningNodes();

            List<String> finishedNodes = new ArrayList<>();
            for (AbstractDataType joiningNode : joiningNodes) {
                OctetString macAddressOctetString = joiningNode.getOctetString();
                if (macAddressOctetString != null) {
                    String macAddress = ProtocolTools.getHexStringFromBytes((macAddressOctetString).getOctetStr(), "");

                    DeviceIdentifier slaveDeviceIdentifier = identificationService.createDeviceIdentifierByCallHomeId(macAddress);
                    final TypedProperties deviceProtocolProperties = context.getDeviceProtocolProperties(slaveDeviceIdentifier);
                    if (deviceProtocolProperties != null) {
                        final HexString psk = deviceProtocolProperties.<HexString>getTypedProperty(G3Properties.PSK);
                        if (psk != null && psk.getContent() != null && psk.getContent().length() > 0) {
                            final byte[] pskBytes = parseKey(psk.getContent());
                            if (pskBytes != null) {
                                final OctetString wrappedPSKKey = wrap(dlmsSession.getProperties().getProperties(), pskBytes);
                                Structure macAndKeyPair = createMacAndKeyPair(macAddressOctetString, wrappedPSKKey, slaveDeviceIdentifier);
                                macKeyPairs.addDataType(macAndKeyPair);
                                finishedNodes.add(macAddress);
                                context.logOnAllLoggerHandlers("Providing PSK key for joining module '" + macAddress + "'", Level.INFO);
                            } else {
                                context.logOnAllLoggerHandlers("Device with MAC address " + macAddress + " has an invalid PSK property: '" + psk + "'. Should be 32 hex characters. Skipping.", Level.WARNING);
                                joiningMacAddresses.remove(macAddress); //Cannot provide the PSK, remove it from the queue
                            }
                        } else {
                            context.logOnAllLoggerHandlers("Device with MAC address " + macAddress + " does not have a PSK property in our system, skipping.", Level.WARNING);
                            joiningMacAddresses.remove(macAddress); //Cannot provide the PSK, remove it from the queue
                        }
                    } else {
                        context.logOnAllLoggerHandlers("No unique device with MAC address " + macAddress + " exists in our system, cannot provide PSK key. Skipping.", Level.WARNING);
                        joiningMacAddresses.remove(macAddress); //Cannot provide the PSK, remove it from the queue
                    }
                }
            }

            g3NetworkManagement.provideKeyPairs(macKeyPairs);

            this.joiningMacAddresses.removeAll(finishedNodes); //PSKs were provided for these nodes.

        } catch (ProtocolException e) {
            throw new ConnectionCommunicationException(MessageSeeds.UNEXPECTED_PROTOCOL_ERROR, e);
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, dlmsSession.getProperties().getRetries());
        }
    }

    protected Structure createMacAndKeyPair(OctetString macAddressOctetString, OctetString wrappedPSKKey, DeviceIdentifier slaveDeviceIdentifier) {
        Structure macAndKeyPair = new Structure();
        macAndKeyPair.addDataType(macAddressOctetString);
        macAndKeyPair.addDataType(wrappedPSKKey);
        return macAndKeyPair;
    }

    /**
     * The PSK key is sent plain in this implementation.
     * No wrapping here, subclasses can override.
     */
    protected OctetString wrap(TypedProperties properties, byte[] pskBytes) {
        return OctetString.fromByteArray(pskBytes);
    }

    protected byte[] parseKey(String key) {
        if (key.length() != 32) {
            return null;
        }
        try {
            return ProtocolTools.getBytesFromHexString(key, "");
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            return null;
        }
    }

    private List<ConnectionProperty> toPropertySpecs(Date now, TypedProperties typedProperties) {
        List<ConnectionProperty> properties = new ArrayList<>();
        for (String propertyName : typedProperties.propertyNames()) {
            properties.add(new ConnectionTaskPropertyPlaceHolder(propertyName, typedProperties.getProperty(propertyName), Range.atLeast(now.toInstant())));
        }
        return properties;
    }
}