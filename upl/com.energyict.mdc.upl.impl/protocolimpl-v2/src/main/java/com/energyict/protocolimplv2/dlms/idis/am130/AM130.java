package com.energyict.protocolimplv2.dlms.idis.am130;

import com.energyict.cbo.ConfigurationSupport;
import com.energyict.cpo.TypedProperties;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.channels.ip.InboundIpConnectionType;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdc.tasks.TcpDeviceProtocolDialect;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimplv2.MdcManager;
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
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import com.energyict.protocolimplv2.security.DeviceProtocolSecurityPropertySetImpl;

import java.io.IOException;
import java.math.BigDecimal;
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

    protected static final ObisCode FRAMECOUNTER_OBISCODE = ObisCode.fromString("0.0.43.1.0.255");

    protected AM130RegisterFactory registerFactory;

    /**
     * The protocol version date
     */
    @Override
    public String getVersion() {
        return "$Date$";
    }

    protected ConfigurationSupport getNewInstanceOfConfigurationSupport() {
        return new AM130ConfigurationSupport();
    }

    protected IDISProperties getNewInstanceOfProperties() {
        return new AM130Properties();
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        List<ConnectionType> result = new ArrayList<>();
        result.add(new OutboundTcpIpConnectionType());
        result.add(new InboundIpConnectionType());
        return result;
    }

    @Override
    public String getProtocolDescription() {
        return "AM130 DLMS (IDIS P2)";
    }

    /**
     * Method to check whether the cache needs to be read out or not, if so the read will be forced
     */
    protected void checkCacheObjects() {
        boolean readCache = getDlmsSessionProperties().isReadCache();
        if ((getDeviceCache().getObjectList() == null) || (readCache)) {
            getLogger().info("ReadCache property is true, reading cache!");
            readObjectList();
            getDeviceCache().saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());
        } else {
            getLogger().info("Cache exist, will not be read!");
        }
        getDlmsSession().getMeterConfig().setInstantiatedObjectList(getDeviceCache().getObjectList());
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.<DeviceProtocolDialect>asList(new TcpDeviceProtocolDialect());
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        getDlmsSessionProperties().setSerialNumber(offlineDevice.getSerialNumber());
        initDlmsSession(comChannel);
    }

    private void initDlmsSession(ComChannel comChannel) {
        readFrameCounter(comChannel);
        setDlmsSession(new DlmsSession(comChannel, getDlmsSessionProperties()));
    }

    /**
     * First read out the frame counter for the management client, using the public client.
     */
    protected void readFrameCounter(ComChannel comChannel) {
        TypedProperties clone = getDlmsSessionProperties().getProperties().clone();
        clone.setProperty(DlmsProtocolProperties.CLIENT_MAC_ADDRESS, BigDecimal.valueOf(16));
        IDISProperties publicClientProperties = getNewInstanceOfProperties();
        publicClientProperties.addProperties(clone);
        publicClientProperties.setSecurityPropertySet(new DeviceProtocolSecurityPropertySetImpl(0, 0, clone));    //SecurityLevel 0:0

        long frameCounter;
        DlmsSession publicDlmsSession = new DlmsSession(comChannel, publicClientProperties);
        connectToPublicClient(publicDlmsSession);
        try {
            frameCounter = publicDlmsSession.getCosemObjectFactory().getData(FRAMECOUNTER_OBISCODE).getValueAttr().longValue();
        } catch (DataAccessResultException | ProtocolException e) {
            final ProtocolException protocolException = new ProtocolException(e, "Error while reading out the framecounter, cannot continue! " + e.getMessage());
            throw MdcManager.getComServerExceptionFactory().createUnExpectedProtocolError(protocolException);
        } catch (IOException e) {
            throw IOExceptionHandler.handle(e, publicDlmsSession);
        }
        disconnectFromPublicClient(publicDlmsSession);

        getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(frameCounter + 1);
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