package com.energyict.protocolimplv2.dlms.idis.am130;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.ConnectionCommunicationException;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.MessageSeeds;
import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.legacy.dynamic.ConfigurationSupport;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.protocols.impl.channels.ip.InboundIpConnectionType;
import com.energyict.protocols.impl.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.protocols.mdc.protocoltasks.TcpDeviceProtocolDialect;

import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
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
import com.energyict.protocolimplv2.security.DsmrSecuritySupport;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    @Inject
    public AM130(Clock clock, Thesaurus thesaurus, PropertySpecService propertySpecService, SocketService socketService, SerialComponentService serialComponentService, IssueService issueService, TopologyService topologyService, MdcReadingTypeUtilService readingTypeUtilService, IdentificationService identificationService, CollectedDataFactory collectedDataFactory, MeteringService meteringService, LoadProfileFactory loadProfileFactory, Provider<DsmrSecuritySupport> dsmrSecuritySupportProvider) {
        super(clock, thesaurus, propertySpecService, socketService, serialComponentService, issueService, topologyService, readingTypeUtilService, identificationService, collectedDataFactory, meteringService, loadProfileFactory, dsmrSecuritySupportProvider);
    }

    /**
     * The protocol version date
     */
    @Override
    public String getVersion() {
        return "$Date: 2016-05-10 12:20:42 +0200 (Tue, 10 May 2016)$";
    }

    protected ConfigurationSupport getNewInstanceOfConfigurationSupport() {
        return new AM130ConfigurationSupport(propertySpecService);
    }

    protected IDISProperties getNewInstanceOfProperties() {
        return new AM130Properties(propertySpecService, thesaurus);
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        List<ConnectionType> result = new ArrayList<>();
        result.add(new OutboundTcpIpConnectionType(thesaurus, propertySpecService, getSocketService()));
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
        boolean readCache = getDlmsProperties().isReadCache();
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
        return Arrays.<DeviceProtocolDialect>asList(new TcpDeviceProtocolDialect(thesaurus, propertySpecService));
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        getDlmsProperties().setSerialNumber(offlineDevice.getSerialNumber());
        initDlmsSession(comChannel);
    }

    private void initDlmsSession(ComChannel comChannel) {
        readFrameCounter(comChannel, (int) getDlmsProperties().getTimeout());
        setDlmsSession(new DlmsSession(comChannel, getDlmsProperties()));
    }

    /**
     * First read out the frame counter for the management client, using the public client.
     */
    protected void readFrameCounter(ComChannel comChannel, int timeout) {
        TypedProperties clone = getDlmsProperties().getProperties().clone();
        clone.setProperty(DlmsProtocolProperties.CLIENT_MAC_ADDRESS, BigDecimal.valueOf(IDIS2_CLIENT_PUBLIC));
        IDISProperties publicClientProperties = getNewInstanceOfProperties();
        publicClientProperties.addProperties(clone);
        publicClientProperties.setSecurityPropertySet(new DeviceProtocolSecurityPropertySet() {
            @Override
            public int getAuthenticationDeviceAccessLevel() {
                return 0;
            }

            @Override
            public int getEncryptionDeviceAccessLevel() {
                return 0;
            }

            @Override
            public TypedProperties getSecurityProperties() {
                return clone;
            }
        });    //SecurityLevel 0:0
        long frameCounter;
        DlmsSession publicDlmsSession = new DlmsSession(comChannel, publicClientProperties);
        getLogger().info("Connecting to public client:" + IDIS2_CLIENT_PUBLIC);
        connectToPublicClient(publicDlmsSession);
        try {
            ObisCode frameCounterObisCode = getFrameCounterForClient(IDIS2_CLIENT_PUBLIC);
            getLogger().info("Public client connected, reading framecounter " + frameCounterObisCode.toString());
            frameCounter = publicDlmsSession.getCosemObjectFactory().getData(frameCounterObisCode).getValueAttr().longValue();
            getLogger().info("Frame counter received: " + frameCounter);
        } catch (DataAccessResultException | ProtocolException e) {
            final ProtocolException protocolException = new ProtocolException(e, "Error while reading out the framecounter, cannot continue! " + e.getMessage());
            throw new ConnectionCommunicationException(MessageSeeds.PROTOCOL_IO_PARSE_ERROR, protocolException);
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, publicDlmsSession.getProperties().getRetries() + 1);
        }
        getLogger().info("Disconnecting public client");
        disconnectFromPublicClient(publicDlmsSession);

        getDlmsProperties().getSecurityProvider().setInitialFrameCounter(frameCounter + 1);
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
            idisLogBookFactory = new AM130LogBookFactory(this);
        }
        return idisLogBookFactory;
    }

    protected IDISMessaging getIDISMessaging() {
        if (idisMessaging == null) {
            idisMessaging = new AM130Messaging(this);
        }
        return idisMessaging;
    }

    @Override
    public AbstractMeterTopology getMeterTopology() {
        if (meterTopology == null) {
            meterTopology = new AM130MeterTopology(this);
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
            this.registerFactory = new AM130RegisterFactory(this);
        }
        return registerFactory;
    }
}