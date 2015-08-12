package com.energyict.protocolimplv2.eict.rtuplusserver.rtu3;

import com.energyict.cbo.ConfigurationSupport;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.SAPAssignmentItem;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.channels.ip.InboundIpConnectionType;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.*;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.capabilities.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdc.tasks.GatewayTcpDeviceProtocolDialect;
import com.energyict.mdc.tasks.MirrorTcpDeviceProtocolDialect;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.g3.properties.AS330DConfigurationSupport;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.properties.G3GatewayProperties;
import com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.messages.RTU3Messaging;
import com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.properties.RTU3ConfigurationSupport;
import com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.properties.RTU3Properties;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierById;
import com.energyict.protocolimplv2.identifiers.DialHomeIdDeviceIdentifier;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import com.energyict.protocolimplv2.security.DeviceProtocolSecurityPropertySetImpl;
import com.energyict.protocolimplv2.security.RTU3SecuritySupport;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 18/06/2015 - 15:07
 */
public class RTU3 extends AbstractDlmsProtocol {    //TODO rename to Beacon 3100 ? also adjust release notes page title

    private static final ObisCode SERIAL_NUMBER_OBISCODE = ObisCode.fromString("0.0.96.1.0.255");
    private static final ObisCode FRAMECOUNTER_OBISCODE = ObisCode.fromString("0.0.43.1.1.255");

    private static final int MIRROR_DEVICES_START = 2;
    private static final int MIRROR_DEVICES_END = Short.MAX_VALUE;
    private RTU3Messaging rtu3Messaging;

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        getDlmsSessionProperties().setSerialNumber(offlineDevice.getSerialNumber());
        readFrameCounter(comChannel);
        setDlmsSession(new DlmsSession(comChannel, getDlmsSessionProperties()));
    }

    /**
     * First read out the frame counter for the management client, using the public client.
     * Note that this happens without setting up an association, since the it's pre-established for the public client.
     */
    protected void readFrameCounter(ComChannel comChannel) {
        TypedProperties clone = getDlmsSessionProperties().getProperties().clone();
        clone.setProperty(DlmsProtocolProperties.CLIENT_MAC_ADDRESS, BigDecimal.valueOf(16));
        G3GatewayProperties publicClientProperties = new G3GatewayProperties();
        publicClientProperties.addProperties(clone);
        publicClientProperties.setSecurityPropertySet(new DeviceProtocolSecurityPropertySetImpl(0, 0, clone));    //SecurityLevel 0:0

        DlmsSession publicDlmsSession = new DlmsSession(comChannel, publicClientProperties);
        publicDlmsSession.assumeConnected(publicClientProperties.getMaxRecPDUSize(), publicClientProperties.getConformanceBlock());
        long frameCounter;
        try {
            frameCounter = publicDlmsSession.getCosemObjectFactory().getData(FRAMECOUNTER_OBISCODE).getValueAttr().longValue();
        } catch (DataAccessResultException | ProtocolException e) {
            frameCounter = new Random().nextInt();
        } catch (IOException e) {
            throw IOExceptionHandler.handle(e, publicDlmsSession);
        }

        //Read out the frame counter using the public client, it has a pre-established association
        getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(frameCounter + 1);
    }

    @Override
    public String getSerialNumber() {
        try {
            return getDlmsSession().getCosemObjectFactory().getData(SERIAL_NUMBER_OBISCODE).getString();
        } catch (IOException e) {
            throw IOExceptionHandler.handle(e, getDlmsSession());
        }
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER, DeviceProtocolCapabilities.PROTOCOL_SESSION);
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Arrays.<ConnectionType>asList(new OutboundTcpIpConnectionType(), new InboundIpConnectionType());
    }

    @Override
    public String getProtocolDescription() {
        return "EnergyICT RTU3 G3 DLMS";
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        return Collections.emptyList(); //Not supported
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        return Collections.emptyList(); //Not supported
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return null;    //TODO get logbook obiscode and buffer entries description
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return getRtu3Messaging().getSupportedMessages();
    }

    private RTU3Messaging getRtu3Messaging() {
        if (rtu3Messaging == null) {
            rtu3Messaging = new RTU3Messaging(this);
        }
        return rtu3Messaging;
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getRtu3Messaging().executePendingMessages(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getRtu3Messaging().updateSentMessages(sentMessages);
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return getRtu3Messaging().format(propertySpec, messageAttribute);
    }

    /**
     * There's 2 dialects:
     * - 1 used to communicate to the mirrored device (= cached meter data) in the Beacon DC
     * - 1 used to communicate straight to the actual meter, using the Beacon as a gateway.
     */
    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.<DeviceProtocolDialect>asList(new MirrorTcpDeviceProtocolDialect(), new GatewayTcpDeviceProtocolDialect());
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        //TODO get documentation
        return null;
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        CollectedTopology deviceTopology = MdcManager.getCollectedDataFactory().createCollectedTopology(new DeviceIdentifierById(offlineDevice.getId()));

        List<SAPAssignmentItem> sapAssignmentList;      //List that contains the SAP id's and the MAC addresses of all logical devices (= gateway + slaves)
        try {
            sapAssignmentList = this.getDlmsSession().getCosemObjectFactory().getSAPAssignment().getSapAssignmentList();
        } catch (IOException e) {
            throw IOExceptionHandler.handle(e, getDlmsSession());
        }

        for (SAPAssignmentItem sapAssignmentItem : sapAssignmentList) {
            if (!isGatewayNode(sapAssignmentItem) && isMirrorLogicalDevice(sapAssignmentItem.getSap())) {
                //The name is the MAC address of the meter
                DialHomeIdDeviceIdentifier slaveDeviceIdentifier = new DialHomeIdDeviceIdentifier(sapAssignmentItem.getLogicalDeviceName());
                deviceTopology.addSlaveDevice(slaveDeviceIdentifier);   //Using callHomeId as a general property
                deviceTopology.addAdditionalCollectedDeviceInfo(
                        MdcManager.getCollectedDataFactory().createCollectedDeviceProtocolProperty(
                                slaveDeviceIdentifier,
                                AS330DConfigurationSupport.MIRROR_LOGICAL_DEVICE_ID,
                                BigDecimal.valueOf(sapAssignmentItem.getSap())
                        )
                );
                deviceTopology.addAdditionalCollectedDeviceInfo(
                        MdcManager.getCollectedDataFactory().createCollectedDeviceProtocolProperty(
                                slaveDeviceIdentifier,
                                AS330DConfigurationSupport.GATEWAY_LOGICAL_DEVICE_ID,
                                BigDecimal.valueOf(calculateGatewayLogicalDeviceId(sapAssignmentItem))
                        )
                );
            }
        }
        return deviceTopology;
    }

    private int calculateGatewayLogicalDeviceId(SAPAssignmentItem sapAssignmentItem) {
        return MIRROR_DEVICES_END + sapAssignmentItem.getSap(); //TODO -1 offset?
    }

    /**
     * A logical device represents a 'mirror' (cached meter data in the DC) if its SAP is between 2 and 32767
     */
    private boolean isMirrorLogicalDevice(int sap) {
        return sap > MIRROR_DEVICES_START && sap < MIRROR_DEVICES_END;
    }

    /**
     * True if the SAP item represents the DC itself, false if it represents a slave meter
     */
    private boolean isGatewayNode(SAPAssignmentItem sapAssignmentItem) {
        return sapAssignmentItem.getSap() == 1;
    }

    protected DeviceProtocolSecurityCapabilities getSecuritySupport() {
        if (dlmsSecuritySupport == null) {
            dlmsSecuritySupport = new RTU3SecuritySupport();
        }
        return dlmsSecuritySupport;
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }

    @Override
    public void logOn() {
        getDlmsSession().connect();
        checkCacheObjects();
        //No MBus slave devices
    }

    protected void checkCacheObjects() {
        if (getDeviceCache() == null) {
            setDeviceCache(new DLMSCache());
        }
        DLMSCache dlmsCache = getDeviceCache();
        if (dlmsCache.getObjectList() == null || getDlmsSessionProperties().isReadCache()) {
            readObjectList();
            dlmsCache.saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());  // save object list in cache
        } else {
            getDlmsSession().getMeterConfig().setInstantiatedObjectList(dlmsCache.getObjectList());
        }
    }

    public RTU3Properties getDlmsSessionProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new RTU3Properties();
        }
        return (RTU3Properties) dlmsProperties;
    }

    /**
     * A collection of general DLMS properties.
     * These properties are not related to the security or the protocol dialects.
     */
    protected ConfigurationSupport getDlmsConfigurationSupport() {
        if (dlmsConfigurationSupport == null) {
            dlmsConfigurationSupport = new RTU3ConfigurationSupport();
        }
        return dlmsConfigurationSupport;
    }
}