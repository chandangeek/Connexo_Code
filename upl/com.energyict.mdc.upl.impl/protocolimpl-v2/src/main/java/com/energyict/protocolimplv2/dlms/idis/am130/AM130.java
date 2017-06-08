package com.energyict.protocolimplv2.dlms.idis.am130;

import com.energyict.mdc.channels.ip.InboundIpConnectionType;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.tasks.TcpDeviceProtocolDialect;
import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.ManufacturerInformation;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimplv2.dlms.AbstractMeterTopology;
import com.energyict.protocolimplv2.dlms.idis.am130.events.AM130LogBookFactory;
import com.energyict.protocolimplv2.dlms.idis.am130.messages.AM130Messaging;
import com.energyict.protocolimplv2.dlms.idis.am130.properties.AM130ConfigurationSupport;
import com.energyict.protocolimplv2.dlms.idis.am130.properties.AM130Properties;
import com.energyict.protocolimplv2.dlms.idis.am130.registers.AM130RegisterFactory;
import com.energyict.protocolimplv2.dlms.idis.am130.topology.AM130MeterTopology;
import com.energyict.protocolimplv2.dlms.idis.am500.AM500;
import com.energyict.protocolimplv2.dlms.idis.am500.events.IDISLogBookFactory;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessaging;
import com.energyict.protocolimplv2.dlms.idis.am500.properties.IDISProperties;
import com.energyict.protocolimplv2.security.DeviceProtocolSecurityPropertySetImpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Extension of the old AM500 protocol (IDIS package 1), adding extra features (IDIS package 2)
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 9/02/2015 - 13:43
 */
public class AM130 extends AM500 {

    protected static final int IDIS2_CLIENT_MANAGEMENT = 1;
    protected static final int IDIS2_CLIENT_PUBLIC = 16;
    protected static final int IDIS2_CLIENT_PRE_ESTABLISHED = 102;

    protected static final ObisCode FRAMECOUNTER_OBISCODE_MANAGEMENT = ObisCode.fromString("0.0.43.1.0.255");

    protected AM130RegisterFactory registerFactory;

    public AM130(PropertySpecService propertySpecService, NlsService nlsService, Converter converter, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(propertySpecService, collectedDataFactory, issueFactory, nlsService, converter, calendarExtractor, messageFileExtractor, keyAccessorTypeExtractor);
    }

    /**
     * The protocol version date
     */
    @Override
    public String getVersion() {
        return "$Date: Fri Sep 2 11:39:06 2016 +0300 $";
    }

    protected HasDynamicProperties getNewInstanceOfConfigurationSupport() {
        return new AM130ConfigurationSupport(this.getPropertySpecService());
    }

    protected IDISProperties getNewInstanceOfProperties() {
        return new AM130Properties();
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        List<ConnectionType> result = new ArrayList<>();
        result.add(new OutboundTcpIpConnectionType(this.getPropertySpecService()));
        result.add(new InboundIpConnectionType());
        return result;
    }

    @Override
    public String getProtocolDescription() {
        return "Elster AM130 DLMS (IDIS P2)";
    }

    /**
     * Method to check whether the cache needs to be read out or not, if so the read will be forced
     */
    protected void checkCacheObjects() {
        boolean readCache = getDlmsSessionProperties().isReadCache();
        if ((getDeviceCache().getObjectList() == null) || (readCache)) {
            getLogger().info(readCache ? "ReReadCache property is true, reading cache!" : "The cache was empty, reading out the object list!");
            readObjectList();
            getDeviceCache().saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());
        } else {
            getLogger().info("Cache exist, will not be read!");
        }
        getDlmsSession().getMeterConfig().setInstantiatedObjectList(getDeviceCache().getObjectList());
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Collections.singletonList(new TcpDeviceProtocolDialect(this.getPropertySpecService()));
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        getDlmsSessionProperties().setSerialNumber(offlineDevice.getSerialNumber());
        initDlmsSession(comChannel);
    }

    protected void initDlmsSession(ComChannel comChannel) {
        readFrameCounter(comChannel, (int) getDlmsSessionProperties().getTimeout());
        setDlmsSession(new DlmsSession(comChannel, getDlmsSessionProperties()));
    }

    /**
     * First read out the frame counter for the management client, using the public client.
     */
    protected void readFrameCounter(ComChannel comChannel, int timeout) {
        TypedProperties clone = TypedProperties.copyOf(getDlmsSessionProperties().getProperties());
        clone.setProperty(DlmsProtocolProperties.CLIENT_MAC_ADDRESS, BigDecimal.valueOf(IDIS2_CLIENT_PUBLIC));
        IDISProperties publicClientProperties = getNewInstanceOfProperties();
        publicClientProperties.addProperties(clone);
        publicClientProperties.setSecurityPropertySet(new DeviceProtocolSecurityPropertySetImpl(BigDecimal.valueOf(IDIS2_CLIENT_MANAGEMENT), 0, 0, 0, 0, 0, clone));    //SecurityLevel 0:0

        long frameCounter;
        DlmsSession publicDlmsSession = new DlmsSession(comChannel, publicClientProperties);
        getLogger().info("Connecting to public client:" + IDIS2_CLIENT_PUBLIC);
        connectToPublicClient(publicDlmsSession);
        try {
            ObisCode frameCounterObisCode = getFrameCounterForClient(getDlmsSessionProperties().getClientMacAddress());
            getLogger().info("Public client connected, reading framecounter " + frameCounterObisCode.toString() + ", corresponding to client " + getDlmsSessionProperties().getClientMacAddress());
            frameCounter = publicDlmsSession.getCosemObjectFactory().getData(frameCounterObisCode).getValueAttr().longValue();
            getLogger().info("Frame counter received: " + frameCounter);
        } catch (DataAccessResultException | ProtocolException e) {
            final ProtocolException protocolException = new ProtocolException(e, "Error while reading out the framecounter, cannot continue! " + e.getMessage());
            throw ConnectionCommunicationException.unExpectedProtocolError(protocolException);
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, publicDlmsSession.getProperties().getRetries() + 1);
        }
        getLogger().info("Disconnecting public client");
        disconnectFromPublicClient(publicDlmsSession);

        getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(frameCounter + 1);
    }

    protected ObisCode getFrameCounterForClient(int clientId) {
        switch (clientId) {
            case IDIS2_CLIENT_MANAGEMENT:
            case IDIS2_CLIENT_PRE_ESTABLISHED:
            case IDIS2_CLIENT_PUBLIC:
            default:
                return FRAMECOUNTER_OBISCODE_MANAGEMENT;
        }
    }


    /**
     * Actually create an association to the public client, it is not pre-established
     */
    protected void connectToPublicClient(DlmsSession publicDlmsSession) {
        connectWithRetries(publicDlmsSession);
    }

    /**
     * Actually release from the public client, it is not pre-established
     */
    protected void disconnectFromPublicClient(DlmsSession publicDlmsSession) {
        publicDlmsSession.disconnect();
    }

    protected IDISLogBookFactory getIDISLogBookFactory() {
        if (idisLogBookFactory == null) {
            idisLogBookFactory = new AM130LogBookFactory(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return idisLogBookFactory;
    }

    protected IDISMessaging getIDISMessaging() {
        if (idisMessaging == null) {
            idisMessaging = new AM130Messaging(this, this.getCollectedDataFactory(), this.getIssueFactory(), this.getPropertySpecService(), this.getNlsService(), this.getConverter(), this.getCalendarExtractor(), this.getMessageFileExtractor(), this.getKeyAccessorTypeExtractor());
        }
        return idisMessaging;
    }

    @Override
    public AbstractMeterTopology getMeterTopology() {
        if (meterTopology == null) {
            meterTopology = new AM130MeterTopology(this, this.getCollectedDataFactory());
            meterTopology.searchForSlaveDevices();
        }
        return meterTopology;
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return getRegisterFactory().readRegisters(registers);
    }

    protected AM130RegisterFactory getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new AM130RegisterFactory(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return registerFactory;
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return getIDISMessaging().prepareMessageContext(device, offlineDevice, deviceMessage);
    }

    @Override
    public ManufacturerInformation getManufacturerInformation() {
        return null;
    }

    @Override
    public DeviceFunction getDeviceFunction() {
        return DeviceFunction.NONE;
    }
}