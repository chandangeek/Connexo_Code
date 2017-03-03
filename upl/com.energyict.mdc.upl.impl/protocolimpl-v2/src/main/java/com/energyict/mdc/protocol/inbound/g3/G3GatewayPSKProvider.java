package com.energyict.mdc.protocol.inbound.g3;

import com.energyict.cbo.TimePeriod;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.dlms.cosem.G3NetworkManagement;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.channels.ip.socket.TLSConnectionType;
import com.energyict.mdc.ports.InboundComPort;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.DeviceProtocol;
import com.energyict.mdc.protocol.DeviceProtocolCache;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.inbound.InboundDiscoveryContext;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.tasks.ConnectionTaskProperty;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdw.core.DeviceOfflineFlags;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.protocol.NotInObjectListException;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.exceptions.CommunicationException;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.protocol.exceptions.ConnectionSetupException;
import com.energyict.protocolimpl.dlms.g3.G3Properties;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.RtuPlusServer;
import com.energyict.protocolimplv2.identifiers.DialHomeIdDeviceIdentifier;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

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

    private Set<String> joiningMacAddresses = Collections.synchronizedSet(new HashSet<String>());
    private DeviceProtocol gatewayProtocol = null;

    public G3GatewayPSKProvider(DeviceIdentifier deviceIdentifier, InboundDiscoveryContext context) {
        this.deviceIdentifier = deviceIdentifier;
        this.context = context;
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
        }

        if (joiningMacAddresses.isEmpty()) {
            context.logOnAllLoggerHandlers("Successfully provided PSKs for all joining nodes, releasing the association and closing the TCP connection.", Level.INFO);
        } else {
            StringBuilder notJoinedMacAddresses = new StringBuilder();
            Iterator it = joiningMacAddresses.iterator();
            while(it.hasNext()){
                notJoinedMacAddresses.append(it.next());
                notJoinedMacAddresses.append(", ");
            }
            context.logOnAllLoggerHandlers("Unable to provide PSKs for following joining nodes: "+ notJoinedMacAddresses +" the association will be released.", Level.INFO);
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
    private DeviceProtocol getGatewayProtocol(DeviceProtocolSecurityPropertySet securityPropertySet) {
        if (gatewayProtocol == null) {
            gatewayProtocol = initializeGatewayProtocol(securityPropertySet);
        }
        return gatewayProtocol;
    }

    /**
     * Create a protocol instance that will setup a DLMS session to the RTU+Server
     * JUnit test overrides this
     */
    protected DeviceProtocol initializeGatewayProtocol(DeviceProtocolSecurityPropertySet securityPropertySet) {
        DeviceProtocol gatewayProtocol = newGatewayProtocol();
        final TypedProperties deviceProtocolProperties = context.getInboundDAO().getDeviceProtocolProperties(getDeviceIdentifier());
        TypedProperties protocolProperties = deviceProtocolProperties == null ? TypedProperties.empty() : deviceProtocolProperties;
        protocolProperties.setProperty(DlmsProtocolProperties.READCACHE_PROPERTY, false);
        TypedProperties dialectProperties = context.getInboundDAO().getDeviceDialectProperties(getDeviceIdentifier(), context.getComPort());
        if (dialectProperties == null) {
            dialectProperties = TypedProperties.empty();
        }
        addDefaultValuesIfNecessary(gatewayProtocol, dialectProperties);

        OfflineDevice offlineDevice = context.getInboundDAO().goOfflineDevice(getDeviceIdentifier(), new DeviceOfflineFlags());   //Empty flags means don't load any master data
        DeviceProtocolCache deviceCache = offlineDevice.getDeviceProtocolCache();
        createTcpComChannel();
        context.logOnAllLoggerHandlers("Creating a new DLMS session to Beacon device '" + getDeviceIdentifier().getIdentifier() + "', to provide the PSK key(s)", Level.INFO);
        gatewayProtocol.setDeviceCache(deviceCache);
        gatewayProtocol.addProperties(protocolProperties);
        gatewayProtocol.addDeviceProtocolDialectProperties(dialectProperties);
        gatewayProtocol.setSecurityPropertySet(securityPropertySet);
        gatewayProtocol.init(offlineDevice, tcpComChannel);
        gatewayProtocol.logOn();
        return gatewayProtocol;
    }

    protected DeviceProtocol newGatewayProtocol() {
        return new RtuPlusServer();
    }

    /**
     * For all properties who are not yet specified - but for which a default value exist - the default value will be added.
     */
    private void addDefaultValuesIfNecessary(DeviceProtocol gatewayProtocol, TypedProperties dialectProperties) {
        DeviceProtocolDialect theActualDialect = gatewayProtocol.getDeviceProtocolDialects().get(0);
        for (PropertySpec propertySpec : theActualDialect.getOptionalProperties()) {
            if (!dialectProperties.hasValueFor(propertySpec.getName()) && propertySpec.getPossibleValues() != null) {
                dialectProperties.setProperty(propertySpec.getName(), propertySpec.getPossibleValues().getDefault());
            }
        }
        for (PropertySpec propertySpec : theActualDialect.getRequiredProperties()) {
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
            context.logOnAllLoggerHandlers("Setting up a new TLS connection to Beacon device '" + getDeviceIdentifier().getIdentifier() + "', to provide the PSK key(s)", Level.INFO);
        }
        context.logOnAllLoggerHandlers("Setting up a new outbound TCP connection to Beacon device '" + getDeviceIdentifier().getIdentifier() + "', to provide the PSK key(s)", Level.INFO);
        List<ConnectionTaskProperty> connectionTaskProperties = toPropertySpecs(new Date(), connectionProperties);

        try {
            InboundComPort comPort = context.getComPort();         //Note that this is indeed the INBOUND comport, it is only used for logging purposes in the ComChannel
            tcpComChannel = tlsConnection ? new TLSConnectionType().connect(comPort, connectionTaskProperties) :
                    new OutboundTcpIpConnectionType().connect(comPort, connectionTaskProperties);
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
            g3NetworkManagement = getG3NetworkManagement(gatewayProtocol, dlmsSession);
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
                        final String psk = deviceProtocolProperties.getTypedProperty(G3Properties.PSK);
                        if (psk != null && psk.length() > 0) {
                            final byte[] pskBytes = parseKey(psk);
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
                            context.logOnAllLoggerHandlers("Device with MAC address " + macAddress + " does not have a PSK property in EIServer, skipping.", Level.WARNING);
                            joiningMacAddresses.remove(macAddress); //Cannot provide the PSK, remove it from the queue
                        }
                    } else {
                        context.logOnAllLoggerHandlers("No unique device with MAC address " + macAddress + " exists in EIServer, cannot provide PSK key. Skipping.", Level.WARNING);
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

    protected G3NetworkManagement getG3NetworkManagement(DeviceProtocol gatewayProtocol, DlmsSession dlmsSession) throws NotInObjectListException {
        return dlmsSession.getCosemObjectFactory().getG3NetworkManagement();
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

    private List<ConnectionTaskProperty> toPropertySpecs(Date now, TypedProperties typedProperties) {
        List<ConnectionTaskProperty> properties = new ArrayList<>();
        for (String propertyName : typedProperties.propertyNames()) {
            properties.add(new ConnectionTaskPropertyPlaceHolder(propertyName, typedProperties.getProperty(propertyName), new TimePeriod(now, null)));
        }
        return properties;
    }
}