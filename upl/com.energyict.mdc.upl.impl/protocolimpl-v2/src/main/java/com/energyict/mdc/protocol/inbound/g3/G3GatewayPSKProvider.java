package com.energyict.mdc.protocol.inbound.g3;

import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.channels.ip.socket.TLSConnectionType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.DeviceProtocol;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.InboundDiscoveryContext;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.DeviceOfflineFlags;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.HexString;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;

import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.dlms.cosem.G3NetworkManagement;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.protocol.exceptions.CommunicationException;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.protocol.exceptions.ConnectionSetupException;
import com.energyict.protocolimpl.dlms.g3.G3Properties;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.RtuPlusServer;
import com.energyict.protocolimplv2.identifiers.DialHomeIdDeviceIdentifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 10/11/2015 - 17:35
 */
public class G3GatewayPSKProvider {

    private final DeviceIdentifier deviceIdentifier;
    protected InboundDiscoveryContext context;
    protected ComChannel tcpComChannel;

    private Set<String> joiningMacAddresses = Collections.synchronizedSet(new HashSet<>());
    private DeviceProtocol gatewayProtocol = null;

    public G3GatewayPSKProvider(DeviceIdentifier deviceIdentifier, InboundDiscoveryContext context) {
        this.deviceIdentifier = deviceIdentifier;
        this.context = context;
    }

    public InboundDiscoveryContext getContext() {
        return context;
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
    public synchronized void providePSK(String macAddress, DeviceProtocolSecurityPropertySet securityPropertySet) {

        if (!joiningMacAddresses.contains(macAddress)) {
            //Another thread already provided the PSK for this MAC address, cool! Let's move on.
            return;
        }

        try {
            DeviceProtocol gatewayProtocol = getGatewayProtocol(securityPropertySet);
            providePSK(gatewayProtocol);
            joiningMacAddresses.remove(macAddress);
        } catch (CommunicationException e) {
            communicationError("Unexpected CommunicationException occurred while trying to provide PSKs to the Beacon. Closing the TCP connection.");
            throw e;
        } catch (PropertyValidationException e) {
            communicationError("Unexpected property validation exception occurred while trying to provide PSKs to the Beacon. Closing the TCP connection.");
            throw CommunicationException.protocolConnectFailed(e);
        }

        if (joiningMacAddresses.isEmpty()) {
            context.getLogger().info(() -> "Successfully provided PSKs for all joining nodes, releasing the association and closing the TCP connection.");
        } else {
            context.getLogger().info(() -> "Unable to provide PSKs for following joining nodes: " + joiningMacAddresses.stream().collect(Collectors.joining(", ")) + " the association will be released.");
        }
        closeConnection();
    }

    private void closeConnection() {
        try {
            //Our job is done here (for now), release the association and close the TCP connection
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

    /**
     * This method is synchronized, so that only 1 thread at a time can call it.
     * The other threads that want to call this method (on the same instance of this class) will automatically wait.
     * Close the TCP connection, this will also release the current association to the beacon.
     * The next inbound frame will set it up again.
     *
     * @param errorMessage
     */
    public synchronized void provideError(String errorMessage) {
        communicationError(errorMessage);
    }

    /**
     * Close the TCP connection, this will also release the current association to the beacon.
     * The next inbound frame will set it up again.
     *
     * @param errorMessage
     */
    private void communicationError(String errorMessage) {
        try {
            context.getLogger().warning(errorMessage);
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
    private DeviceProtocol getGatewayProtocol(DeviceProtocolSecurityPropertySet securityPropertySet) throws PropertyValidationException {
        if (gatewayProtocol == null) {
            gatewayProtocol = initializeGatewayProtocol(securityPropertySet);
        }
        return gatewayProtocol;
    }

    /**
     * Create a protocol instance that will setup a DLMS session to the RTU+Server
     * JUnit test overrides this
     */
    protected DeviceProtocol initializeGatewayProtocol(DeviceProtocolSecurityPropertySet securityPropertySet) throws PropertyValidationException {
        DeviceProtocol gatewayProtocol = newGatewayProtocol();
        final TypedProperties deviceProtocolProperties = context.getInboundDAO().getDeviceProtocolProperties(getDeviceIdentifier());
        TypedProperties protocolProperties = deviceProtocolProperties == null ? com.energyict.protocolimpl.properties.TypedProperties.empty() : deviceProtocolProperties;
        protocolProperties.setProperty(DlmsProtocolProperties.READCACHE_PROPERTY, false);
        TypedProperties dialectProperties = context.getDeviceDialectProperties(getDeviceIdentifier()).orElseGet(com.energyict.protocolimpl.properties.TypedProperties::empty);
        addDefaultValuesIfNecessary(gatewayProtocol, dialectProperties);

        DLMSCache dummyCache = new DLMSCache(new UniversalObject[0], 0);     //Empty cache, prevents that the protocol will read out the object list
        OfflineDevice offlineDevice = context.getInboundDAO().getOfflineDevice(getDeviceIdentifier(), new DeviceOfflineFlags());   //Empty flags means don't load any master data
        createTcpComChannel();
        context.getLogger().info(() -> "Creating a new DLMS session to Beacon device '" + getDeviceIdentifier().toString() + "', to provide the PSK key(s)");
        gatewayProtocol.setDeviceCache(dummyCache);
        gatewayProtocol.setUPLProperties(protocolProperties);
        gatewayProtocol.addDeviceProtocolDialectProperties(dialectProperties);
        gatewayProtocol.setSecurityPropertySet(securityPropertySet);
        gatewayProtocol.init(offlineDevice, tcpComChannel);
        gatewayProtocol.logOn();
        return gatewayProtocol;
    }

    protected DeviceProtocol newGatewayProtocol() {
        return new RtuPlusServer(this.context.getCollectedDataFactory(), this.context.getIssueFactory(), this.context.getPropertySpecService(), this.context.getNlsService(), this.context.getConverter(), this.context.getMessageFileExtractor(), this.context.getDeviceGroupExtractor(), this.context.getDeviceExtractor());
    }

    /**
     * For all properties who are not yet specified - but for which a default value exist - the default value will be added.
     */
    private void addDefaultValuesIfNecessary(DeviceProtocol gatewayProtocol, TypedProperties dialectProperties) {
        DeviceProtocolDialect theActualDialect = gatewayProtocol.getDeviceProtocolDialects().get(0);
        for (PropertySpec propertySpec : theActualDialect.getUPLPropertySpecs()) {
            if (!dialectProperties.hasValueFor(propertySpec.getName()) && propertySpec.getPossibleValues() != null) {
                dialectProperties.setProperty(propertySpec.getName(), propertySpec.getPossibleValues().getDefault());
            }
        }
    }

    private void createTcpComChannel() {
        boolean tlsConnection = false;
        TypedProperties connectionProperties = context.getInboundDAO().getOutboundConnectionTypeProperties(getDeviceIdentifier());
        if (connectionProperties.getProperty(TLSConnectionType.TLS_VERSION_PROPERTY_NAME) != null) {
            tlsConnection = true;
            context.getLogger().info(() -> "Setting up a new TLS connection to Beacon device '" + getDeviceIdentifier().toString() + "', to provide the PSK key(s)");
        }
        context.getLogger().info(() -> "Setting up a new outbound TCP connection to Beacon device '" + getDeviceIdentifier().toString() + "', to provide the PSK key(s)");

        try {
            PropertySpecService propertySpecService = getContext().getPropertySpecService();
            NlsService nlsService = getContext().getNlsService();
            if (tlsConnection) {
                tcpComChannel = new TLSConnectionType(propertySpecService, nlsService).connect();
            } else {
                tcpComChannel = new OutboundTcpIpConnectionType(propertySpecService).connect();
            }
        } catch (ConnectionException e) {
            throw ConnectionSetupException.connectionSetupFailed(e);
        }
    }

    /**
     * Read out attribute 'joining_slaves' of object G3NetworkManagement. It is the list of slaves that are joining and need a PSK.
     * Find their PSK properties in EIServer and provide them to the Rtu+Server.
     */
    protected void providePSK(DeviceProtocol gatewayProtocol) {
        DlmsSession dlmsSession = getDlmsSession(gatewayProtocol);
        G3NetworkManagement g3NetworkManagement;
        try {
            g3NetworkManagement = dlmsSession.getCosemObjectFactory().getG3NetworkManagement();
        } catch (ProtocolException e) {
            throw ConnectionCommunicationException.unExpectedProtocolError(e);
        }

        try {
            Array macKeyPairs = new Array();

            Array joiningNodes = g3NetworkManagement.getJoiningNodes();

            List<String> finishedNodes = new ArrayList<>();
            for (AbstractDataType joiningNode : joiningNodes) {
                OctetString macAddressOctetString = joiningNode.getOctetString();
                if (macAddressOctetString != null) {
                    String macAddress = ProtocolTools.getHexStringFromBytes((macAddressOctetString).getOctetStr(), "");

                    final DialHomeIdDeviceIdentifier slaveDeviceIdentifier = new DialHomeIdDeviceIdentifier(macAddress);
                    final TypedProperties deviceProtocolProperties = context.getInboundDAO().getDeviceProtocolProperties(slaveDeviceIdentifier);
                    if (deviceProtocolProperties != null) {
                        final HexString psk = deviceProtocolProperties.getTypedProperty(G3Properties.PSK);
                        if (psk != null && psk.getContent() != null && psk.getContent().length() > 0) {
                            final byte[] pskBytes = parseKey(psk.getContent());
                            if (pskBytes != null) {
                                final OctetString wrappedPSKKey = wrap(dlmsSession.getProperties().getProperties(), pskBytes);
                                Structure macAndKeyPair = createMacAndKeyPair(macAddressOctetString, wrappedPSKKey, slaveDeviceIdentifier);
                                macKeyPairs.addDataType(macAndKeyPair);
                                finishedNodes.add(macAddress);
                                context.getLogger().info(() -> "Providing PSK key for joining module '" + macAddress + "'");
                            } else {
                                context.getLogger().warning(() -> "Device with MAC address " + macAddress + " has an invalid PSK property: '" + psk + "'. Should be 32 hex characters. Skipping.");
                                joiningMacAddresses.remove(macAddress); //Cannot provide the PSK, remove it from the queue
                            }
                        } else {
                            context.getLogger().warning(() -> "Device with MAC address " + macAddress + " does not have a PSK property in EIServer, skipping.");
                            joiningMacAddresses.remove(macAddress); //Cannot provide the PSK, remove it from the queue
                        }
                    } else {
                        context.getLogger().warning(() -> "No unique device with MAC address " + macAddress + " exists in EIServer, cannot provide PSK key. Skipping.");
                        joiningMacAddresses.remove(macAddress); //Cannot provide the PSK, remove it from the queue
                    }
                }
            }

            g3NetworkManagement.provideKeyPairs(macKeyPairs);

            this.joiningMacAddresses.removeAll(finishedNodes); //PSKs were provided for these nodes.

        } catch (ProtocolException e) {
            throw ConnectionCommunicationException.unExpectedProtocolError(e);
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

}