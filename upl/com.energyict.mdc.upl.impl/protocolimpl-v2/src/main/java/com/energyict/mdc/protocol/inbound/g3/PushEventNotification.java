package com.energyict.mdc.protocol.inbound.g3;

import com.energyict.cbo.HexString;
import com.energyict.cbo.TimePeriod;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.dlms.cosem.G3NetworkManagement;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.meterdata.CollectedData;
import com.energyict.mdc.meterdata.CollectedLogBook;
import com.energyict.mdc.meterdata.CollectedTopology;
import com.energyict.mdc.ports.InboundComPort;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ConnectionException;
import com.energyict.mdc.protocol.inbound.BinaryInboundDeviceProtocol;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.inbound.InboundDiscoveryContext;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.tasks.ConnectionTaskProperty;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.RtuPlusServer;
import com.energyict.protocolimplv2.identifiers.DialHomeIdDeviceIdentifier;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import com.energyict.protocolimplv2.nta.dsmr50.elster.am540.Dsmr50Properties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 12/03/2015 - 10:27
 */
public class PushEventNotification implements BinaryInboundDeviceProtocol {

    private static final int METER_HAS_JOINED = 0xC2;
    private static final int METER_HAS_LEFT = 0xC3;
    private static final int METER_JOIN_ATTEMPT = 0xC5;
    protected ComChannel tcpComChannel;
    private ComChannel comChannel;
    private InboundDiscoveryContext context;
    private CollectedLogBook collectedLogBook;
    private CollectedTopology collectedTopology;
    private EventPushNotificationParser parser;

    @Override
    public void initComChannel(ComChannel comChannel) {
        this.comChannel = comChannel;
    }

    @Override
    public String getAdditionalInformation() {
        return ""; //No additional info available
    }

    @Override
    public void initializeDiscoveryContext(InboundDiscoveryContext context) {
        this.context = context;
    }

    @Override
    public InboundDiscoveryContext getContext() {
        return context;
    }

    @Override
    public DiscoverResultType doDiscovery() {
        parser = new EventPushNotificationParser(comChannel, getContext());
        parser.parseInboundFrame();
        collectedLogBook = parser.getCollectedLogBook();

        if (isJoinAttempt() || isSuccessfulJoin() || isMeterLeft()) {
            RtuPlusServer gatewayProtocol = null;
            try {
                gatewayProtocol = initializeGatewayProtocol(parser.getSecurityPropertySet());
                DlmsSession dlmsSession = gatewayProtocol.getDlmsSession();
                if (isJoinAttempt()) {
                    providePSK(dlmsSession);
                } else if (isSuccessfulJoin() || isMeterLeft()) {
                    collectedTopology = gatewayProtocol.getDeviceTopology();
                }
            } finally {
                if (gatewayProtocol != null) {
                    gatewayProtocol.logOff();
                    gatewayProtocol.terminate();
                }
                if (tcpComChannel != null) {
                    tcpComChannel.close();
                }
            }
        }

        return DiscoverResultType.DATA;
    }

    /**
     * Create a protocol instance that will setup a DLMS session to the RTU+Server
     * JUnit test overrides this
     */
    protected RtuPlusServer initializeGatewayProtocol(DeviceProtocolSecurityPropertySet securityPropertySet) {
        RtuPlusServer gatewayProtocol = new RtuPlusServer();
        TypedProperties protocolProperties = context.getInboundDAO().getDeviceProtocolProperties(getDeviceIdentifier());
        protocolProperties.setProperty(DlmsProtocolProperties.READCACHE_PROPERTY, false);
        TypedProperties dialectProperties = context.getInboundDAO().getDeviceDialectProperties(getDeviceIdentifier(), context.getComPort());
        if (dialectProperties == null) {
            dialectProperties = TypedProperties.empty();
        }

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

        DLMSCache dummyCache = new DLMSCache(new UniversalObject[0], 0);     //Empty cache, prevents that the protocol will read out the object list
        OfflineDevice offlineDevice = context.getInboundDAO().findOfflineDevice(getDeviceIdentifier());
        createTcpComChannel();
        gatewayProtocol.setDeviceCache(dummyCache);
        gatewayProtocol.addProperties(protocolProperties);
        gatewayProtocol.addDeviceProtocolDialectProperties(dialectProperties);
        gatewayProtocol.setSecurityPropertySet(securityPropertySet);
        gatewayProtocol.init(offlineDevice, tcpComChannel);
        gatewayProtocol.logOn();
        return gatewayProtocol;
    }

    private void createTcpComChannel() {
        TypedProperties connectionProperties = context.getInboundDAO().getOutboundConnectionTypeProperties(getDeviceIdentifier());
        List<ConnectionTaskProperty> connectionTaskProperties = toPropertySpecs(new Date(), connectionProperties);

        try {
            InboundComPort comPort = context.getComPort();         //Note that this is indeed the INBOUND comport, it is only used for logging purposes in the ComChannel
            tcpComChannel = new OutboundTcpIpConnectionType().connect(comPort, connectionTaskProperties);
        } catch (ConnectionException e) {
            throw MdcManager.getComServerExceptionFactory().createConnectionSetupException(e);
        }
    }

    protected void providePSK(DlmsSession dlmsSession) {
        context.getLogger().info("Received joining attempt notification, will create a DLMS session to provide the PSK key(s)");
        G3NetworkManagement g3NetworkManagement;
        try {
            g3NetworkManagement = dlmsSession.getCosemObjectFactory().getG3NetworkManagement();
        } catch (ProtocolException e) {
            throw MdcManager.getComServerExceptionFactory().createUnExpectedProtocolError(e);
        }
        try {
            Array joiningNodes = g3NetworkManagement.getJoiningNodes();
            Array macKeyPairs = new Array();
            for (AbstractDataType joiningNode : joiningNodes) {
                OctetString macAddressOctetString = joiningNode.getOctetString();
                if (macAddressOctetString != null) {
                    String macAddress = ProtocolTools.getHexStringFromBytes((macAddressOctetString).getOctetStr(), "");
                    OfflineDevice offlineDevice = context.getInboundDAO().findOfflineDevice(new DialHomeIdDeviceIdentifier(macAddress));

                    if (offlineDevice != null) {
                        Dsmr50Properties g3MeterProperties = new Dsmr50Properties();
                        g3MeterProperties.addProperties(offlineDevice.getAllProperties());
                        HexString psk = g3MeterProperties.getPSK();
                        if (psk != null && psk.getContent().length() > 0) {
                            Structure macAndKeyPair = new Structure();
                            macAndKeyPair.addDataType(macAddressOctetString);
                            macAndKeyPair.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(psk.getContent(), "")));
                            macKeyPairs.addDataType(macAndKeyPair);
                        } else {
                            context.getLogger().warning("Device with MAC address " + macAddress + " does not have a PSK property in EIServer, skipping.");
                        }
                    } else {
                        context.getLogger().warning("No unique device with MAC address " + macAddress + " exists in EIServer, cannot provide PSK key. Skipping.");
                    }
                }
            }
            g3NetworkManagement.provideKeyPairs(macKeyPairs);
        } catch (ProtocolException e) {
            throw MdcManager.getComServerExceptionFactory().createUnExpectedProtocolError(e);
        } catch (IOException e) {
            throw IOExceptionHandler.handle(e, dlmsSession);
        }
    }

    private List<ConnectionTaskProperty> toPropertySpecs(Date now, TypedProperties typedProperties) {
        List<ConnectionTaskProperty> properties = new ArrayList<>();
        for (String propertyName : typedProperties.propertyNames()) {
            properties.add(new ConnectionTaskPropertyPlaceHolder(propertyName, typedProperties.getProperty(propertyName), new TimePeriod(now, null)));
        }
        return properties;
    }

    private boolean isMeterLeft() {
        return collectedLogBook.getCollectedMeterEvents().get(0).getProtocolCode() == METER_HAS_LEFT;
    }

    private boolean isSuccessfulJoin() {
        return collectedLogBook.getCollectedMeterEvents().get(0).getProtocolCode() == METER_HAS_JOINED;
    }

    private boolean isJoinAttempt() {
        return collectedLogBook.getCollectedMeterEvents().get(0).getProtocolCode() == METER_JOIN_ATTEMPT;
    }

    @Override
    public void provideResponse(DiscoverResponseType responseType) {
        //Nothing to do here
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return parser != null ? parser.getDeviceIdentifier() : null;
    }

    @Override
    public List<CollectedData> getCollectedData() {
        List<CollectedData> collectedDatas = new ArrayList<>();
        collectedDatas.add(collectedLogBook);
        if (collectedTopology != null) {
            collectedDatas.add(collectedTopology);
        }
        return collectedDatas;
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }

    @Override
    public void addProperties(TypedProperties properties) {
        //No properties
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Collections.emptyList();
    }
}