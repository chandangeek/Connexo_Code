package com.energyict.protocolimplv2.dlms.idis.am540;

import com.energyict.cbo.ConfigurationSupport;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.channels.serial.optical.rxtx.RxTxOpticalConnectionType;
import com.energyict.mdc.channels.serial.optical.serialio.SioOpticalConnectionType;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.DeviceProtocolCache;
import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdc.tasks.SerialDeviceProtocolDialect;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dlms.AbstractMeterTopology;
import com.energyict.protocolimplv2.dlms.idis.am130.AM130;
import com.energyict.protocolimplv2.dlms.idis.am130.registers.AM130RegisterFactory;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessaging;
import com.energyict.protocolimplv2.dlms.idis.am540.messages.AM540Messaging;
import com.energyict.protocolimplv2.dlms.idis.am540.properties.AM540ConfigurationSupport;
import com.energyict.protocolimplv2.dlms.idis.am540.properties.AM540Properties;
import com.energyict.protocolimplv2.dlms.idis.am540.registers.AM540RegisterFactory;
import com.energyict.protocolimplv2.dlms.idis.topology.IDISMeterTopology;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * The AM540 is a PLC E-meter designed according to IDIS package 2 specifications <br/>
 * The protocol is an extension of the AM130 protocol (which is the GPRS variant designed according to IDIS P2)
 *
 * @author sva
 * @since 11/08/2015 - 14:04
 */
public class AM540 extends AM130 {

    private AM540Cache am540Cache;

    @Override
    public String getProtocolDescription() {
        return "AM540 DLMS (IDIS P2)";
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }

    /**
     * Read out the serial number, this can either be of the module (equipment identifier) or of the connected e-meter.
     * Note that reading out this register from the mirror logical device in the Beacon, the obiscode must always be 0.0.96.1.0.255
     */
    @Override
    public String getSerialNumber() {
        if (getDlmsSessionProperties().useBeaconMirrorDeviceDialect() || !getDlmsSessionProperties().useEquipmentIdentifierAsSerialNumber()) {
            return getMeterInfo().getSerialNr();
        } else {
            return getMeterInfo().getEquipmentIdentifier();
        }
    }

    @Override
    public Date getTime() {
        if (getDlmsSessionProperties().useBeaconMirrorDeviceDialect()) {
            return new Date();  //Don't read out the clock of the mirror logical device, it does not know the actual meter time.
        } else {
            return super.getTime();
        }
    }

    @Override
    public void setTime(Date timeToSet) {
        if (getDlmsSessionProperties().useBeaconMirrorDeviceDialect()) {
            IOException cause = new IOException("When connected to the mirror logical device, writing of the clock is not allowed.");
            throw MdcManager.getComServerExceptionFactory().notAllowedToExecuteCommand("date/time change", cause);
        } else {
            super.setTime(timeToSet);
        }
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.asList((DeviceProtocolDialect) new SerialDeviceProtocolDialect());
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Arrays.asList((ConnectionType) new SioOpticalConnectionType(), new RxTxOpticalConnectionType());
    }

    protected ConfigurationSupport getNewInstanceOfConfigurationSupport() {
        return new AM540ConfigurationSupport();
    }

    /**
     * First read out the frame counter for the management client, using the public client.
     * Unless of course the whole session is done with the public client, then there's no need to read out the FC.
     */
    protected void readFrameCounter(ComChannel comChannel) {
        if (!getDlmsSessionProperties().usesPublicClient()) {
            super.readFrameCounter(comChannel);
        }
    }

    /**
     * There's 2 different ways to connect to the public client.
     * - on a mirror device, the public client has a pre-established association
     * - on an actual AM540 module, the public client requires a new association
     */
    protected void connectToPublicClient(DlmsSession publicDlmsSession) {
        if (getDlmsSessionProperties().useBeaconMirrorDeviceDialect()) {
            publicDlmsSession.assumeConnected(getDlmsSessionProperties().getMaxRecPDUSize(), getDlmsSessionProperties().getConformanceBlock());
        } else {
            super.connectToPublicClient(publicDlmsSession);
        }
    }

    /**
     * There's 2 different ways to disconnect from the public client.
     * - on a mirror device, the public client is pre-established, so no need to release the association
     * - on an actual AM540 module, the public client requires a new association
     */
    protected void disconnectFromPublicClient(DlmsSession publicDlmsSession) {
        if (!getDlmsSessionProperties().useBeaconMirrorDeviceDialect()) {
            super.disconnectFromPublicClient(publicDlmsSession);
        }
    }

    @Override
    public AM540Properties getDlmsSessionProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = getNewInstanceOfProperties();
        }
        return (AM540Properties) dlmsProperties;
    }

    @Override
    protected AM540Properties getNewInstanceOfProperties() {
        return new AM540Properties();
    }

    @Override
    public AM540Cache getDeviceCache() {
        if (am540Cache == null) {
            am540Cache = new AM540Cache(getDlmsSessionProperties().useBeaconMirrorDeviceDialect());
        }
        return am540Cache;
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        if ((deviceProtocolCache != null) && (deviceProtocolCache instanceof AM540Cache)) {
            am540Cache = (AM540Cache) deviceProtocolCache;
        }
    }

    /**
     * Method to check whether the cache needs to be read out or not, if so the read will be forced
     */
    protected void checkCacheObjects() {
        getDeviceCache().setConnectionToBeaconMirror(getDlmsSessionProperties().useBeaconMirrorDeviceDialect());

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
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        if (getDlmsSessionProperties().useBeaconMirrorDeviceDialect()) {
            IOException cause = new IOException("When connected to the mirror logical device, execution of device commands is not allowed.");
            throw MdcManager.getComServerExceptionFactory().notAllowedToExecuteCommand("send of device messages", cause);
        } else {
            return getIDISMessaging().executePendingMessages(pendingMessages);
        }
    }

    @Override
    protected AM130RegisterFactory getRegisterFactory() {
        if (registerFactory == null) {
            registerFactory = new AM540RegisterFactory(this);
        }
        return registerFactory;
    }

    @Override
    public AbstractMeterTopology getMeterTopology() {
        if (meterTopology == null) {
            meterTopology = new IDISMeterTopology(this);
        }
        return meterTopology;
    }

    protected IDISMessaging getIDISMessaging() {
        if (idisMessaging == null) {
            idisMessaging = new AM540Messaging(this);
        }
        return idisMessaging;
    }
}