package com.energyict.protocolimplv2.dlms.idis.am540;

import com.energyict.cbo.ConfigurationSupport;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.HHUSignOnV2;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.dlms.protocolimplv2.connection.FlagIEC1107Connection;
import com.energyict.mdc.channels.ComChannelType;
import com.energyict.mdc.channels.serial.Parities;
import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.mdc.channels.serial.optical.rxtx.RxTxOpticalConnectionType;
import com.energyict.mdc.channels.serial.optical.serialio.SioOpticalConnectionType;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.DeviceProtocolCache;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdc.tasks.SerialDeviceProtocolDialect;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocol.exceptions.*;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimplv2.dlms.AbstractMeterTopology;
import com.energyict.protocolimplv2.dlms.idis.am130.AM130;
import com.energyict.protocolimplv2.dlms.idis.am130.registers.AM130RegisterFactory;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessaging;
import com.energyict.protocolimplv2.dlms.idis.am540.messages.AM540Messaging;
import com.energyict.protocolimplv2.dlms.idis.am540.properties.AM540ConfigurationSupport;
import com.energyict.protocolimplv2.dlms.idis.am540.properties.AM540Properties;
import com.energyict.protocolimplv2.dlms.idis.am540.registers.AM540RegisterFactory;
import com.energyict.protocolimplv2.dlms.idis.topology.IDISMeterTopology;
import com.energyict.protocolimplv2.hhusignon.IEC1107HHUSignOn;

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
public class AM540 extends AM130 implements SerialNumberSupport {

    private AM540Cache am540Cache;
    private HHUSignOnV2 hhuSignOn;

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        getDlmsSessionProperties().setSerialNumber(offlineDevice.getSerialNumber());
        initDlmsSession(comChannel);
    }

    private void initDlmsSession(ComChannel comChannel) {
        setMeterToTransparentMode(comChannel);
        readFrameCounter(comChannel);
        setDlmsSession(new DlmsSession(comChannel, getDlmsSessionProperties()));
    }

    private void setMeterToTransparentMode(ComChannel comChannel) {
        if (getDlmsSessionProperties().useMeterInTransparentMode()){
            if (ComChannelType.SerialComChannel.is(comChannel) || ComChannelType.OpticalComChannel.is(comChannel)) {
                hhuSignOn = getHHUSignOn((SerialPortComChannel) comChannel);
                SerialPortConfiguration serialPortConfiguration = ((SerialPortComChannel) comChannel).getSerialPortConfiguration();
                int transparentConnectTime = getDlmsSessionProperties().getTransparentConnectTime();
                int transparentDatabits = serialPortConfiguration.getNrOfDataBits().getNrOfDataBits().intValue();
                int transparentStopbits = serialPortConfiguration.getNrOfStopBits().getNrOfStopBits().intValue();
                int transparentParity = getParityValue(serialPortConfiguration.getParity());
                int transparentBaudrate = serialPortConfiguration.getBaudrate().getBaudrate().intValue();
                int authenticationSecurityLevel = Integer.parseInt(getDlmsSessionProperties().getTransparentSecurityLevel().split(":")[0]);
                String strPassword = getDlmsSessionProperties().getTransparentPassword();
                FlagIEC1107Connection flagIEC1107Connection = null;
                try {
                    flagIEC1107Connection = new FlagIEC1107Connection((SerialPortComChannel) comChannel, transparentConnectTime, transparentBaudrate, transparentDatabits, transparentStopbits, transparentParity, authenticationSecurityLevel, strPassword, getLogger(), hhuSignOn);
                    flagIEC1107Connection.setMeterToTransparentMode();
                } catch (Exception e) {
                    getLogger().info("Failed to set the meter into transparent mode");
                    if(flagIEC1107Connection != null){
                        flagIEC1107Connection.switchBaudrate(transparentBaudrate, HHUSignOn.PROTOCOL_HDLC);
                    }
                }
                flagIEC1107Connection = null;//cleanUp this connection
            }
        }
    }

    private HHUSignOnV2 getHHUSignOn(SerialPortComChannel serialPortComChannel) {
        HHUSignOnV2 hhuSignOn = new IEC1107HHUSignOn(serialPortComChannel, getDlmsSessionProperties());
        hhuSignOn.setMode(HHUSignOn.MODE_PROGRAMMING);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_NORMAL);
        hhuSignOn.enableDataReadout(false);
        return hhuSignOn;
    }

    private int getParityValue(Parities parities){
        switch (parities) {
            case NONE:
                return 0;
            case ODD:
                return 2;
            case EVEN:
                return 1;
            default:
                return -1;
        }
    }

    @Override
    public String getProtocolDescription() {
        return "Elster AM540 DLMS (IDIS P2)";
    }

    @Override
    public String getVersion() {
        return "$Date: 2016-07-14 16:01:11 +0200 (Thu, 14 Jul 2016)$";
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
            throw DeviceConfigurationException.notAllowedToExecuteCommand("date/time change", cause);
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

    /**
     * Add extra retries to the association request.
     * If the request was rejected because by the meter the previous association was still open, this retry mechanism will solve the problem.
     *
     * @param dlmsSession
     */
    protected void connectWithRetries(DlmsSession dlmsSession) {
        int tries = 0;
        while (true) {
            ProtocolRuntimeException exception;
            try {
                dlmsSession.getDLMSConnection().setRetries(0);   //Temporarily disable retries in the connection layer, AARQ retries are handled here
                if (dlmsSession.getAso().getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_DISCONNECTED) {
                    dlmsSession.getDlmsV2Connection().connectMAC();
                    dlmsSession.createAssociation((int) getDlmsSessionProperties().getAARQTimeout());
                }
                return;
            } catch (ProtocolRuntimeException e) {
                if (e.getCause() != null && e.getCause() instanceof DataAccessResultException) {
                    throw e;        //Throw real errors, e.g. unsupported security mechanism, wrong password...
                } else if (e instanceof ConnectionCommunicationException) {
                    throw e;
                } else if (e instanceof DataEncryptionException) {
                    throw e;
                }
                exception = e;
            } finally {
                dlmsSession.getDLMSConnection().setRetries(getDlmsSessionProperties().getRetries());
            }

            //Release and retry the AARQ in case of ACSE exception
            if (++tries > getDlmsSessionProperties().getAARQRetries()) {
                getLogger().severe("Unable to establish association after [" + tries + "/" + (getDlmsSessionProperties().getAARQRetries() + 1) + "] tries.");
                throw CommunicationException.protocolConnectFailed(exception);
            } else {
                getLogger().info("Unable to establish association after [" + tries + "/" + (getDlmsSessionProperties().getAARQRetries() + 1) + "] tries. Sending RLRQ and retry ...");
                try {
                    dlmsSession.getAso().releaseAssociation();
                } catch (ProtocolRuntimeException e) {
                    dlmsSession.getAso().setAssociationState(ApplicationServiceObject.ASSOCIATION_DISCONNECTED);
                    // Absorb exception: in 99% of the cases we expect an exception here ...
                }
            }
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
            throw DeviceConfigurationException.notAllowedToExecuteCommand("send of device messages", cause);
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
            meterTopology.searchForSlaveDevices();
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