package com.energyict.protocolimplv2.nta.dsmr23.eict;

import com.energyict.mdc.channels.ComChannelType;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.channels.serial.optical.rxtx.RxTxOpticalConnectionType;
import com.energyict.mdc.channels.serial.optical.serialio.SioOpticalConnectionType;
import com.energyict.mdc.messages.DeviceMessage;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdc.protocol.capabilities.DeviceProtocolCapabilities;
import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdc.tasks.SerialDeviceProtocolDialect;
import com.energyict.mdc.tasks.TcpDeviceProtocolDialect;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;

import com.energyict.cpo.PropertySpec;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.HHUSignOnV2;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.hhusignon.IEC1107HHUSignOn;
import com.energyict.protocolimplv2.nta.dsmr23.logbooks.Dsmr23LogBookFactory;
import com.energyict.protocolimplv2.nta.dsmr23.messages.Dsmr23MessageExecutor;
import com.energyict.protocolimplv2.nta.dsmr23.messages.Dsmr23Messaging;
import com.energyict.protocolimplv2.nta.dsmr23.profiles.LoadProfileBuilder;
import com.energyict.protocolimplv2.nta.dsmr23.registers.Dsmr23RegisterFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * General error handling principle:
 * A DataAccessResultException, a ProtocolException or an ExceptionResponseException indicate we received an error from the meter.
 * E.g. the requested object does not exist, or it is not allowed to be read/written, etc.
 * These exceptions need to be caught and handled first! They all extend from IOException.
 * After that, all remaining IOExceptions are related to communication problems (e.g. timeout, connection broken,...).
 * <p/>
 * Copyrights EnergyICT
 * Date: 18/10/13
 * Time: 11:40
 * Author: khe
 */
public class WebRTUKP extends AbstractDlmsProtocol {

    protected Dsmr23Messaging dsmr23Messaging;
    private Dsmr23LogBookFactory logBookFactory;
    private LoadProfileBuilder loadProfileBuilder;
    private Dsmr23RegisterFactory registerFactory;
    protected ComChannel comChannel;
    protected HHUSignOnV2 hhuSignOn;

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.comChannel = comChannel;
        this.offlineDevice = offlineDevice;
        getDlmsSessionProperties().setSerialNumber(offlineDevice.getSerialNumber());

        if (ComChannelType.SerialComChannel.is(comChannel) || ComChannelType.OpticalComChannel.is(comChannel)) {
            hhuSignOn = getHHUSignOn((SerialPortComChannel) comChannel);
        }
        initDlmsSession();
    }

    protected void initDlmsSession() {
        setDlmsSession(new DlmsSession(comChannel, getDlmsSessionProperties(), hhuSignOn, getProperDeviceId()));
    }

    private HHUSignOnV2 getHHUSignOn(SerialPortComChannel serialPortComChannel) {
        HHUSignOnV2 hhuSignOn = new IEC1107HHUSignOn(serialPortComChannel, getDlmsSessionProperties());
        hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(false);
        return hhuSignOn;
    }

    private String getProperDeviceId() {
        String deviceId = getDlmsSessionProperties().getDeviceId();
        if (deviceId != null && !deviceId.equalsIgnoreCase("")) {
            return deviceId;
        } else {
            return "!"; // the Kamstrup device requires a '!' sign in the IEC1107 signOn
        }
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER, DeviceProtocolCapabilities.PROTOCOL_SESSION);
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        List<ConnectionType> result = new ArrayList<>();
        result.add(new OutboundTcpIpConnectionType());
        result.add(new SioOpticalConnectionType());
        result.add(new RxTxOpticalConnectionType());
        return result;
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        return getLoadProfileBuilder().fetchLoadProfileConfiguration(loadProfilesToRead);
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        return getLoadProfileBuilder().getLoadProfileData(loadProfiles);
    }

    private LoadProfileBuilder getLoadProfileBuilder() {
        if (this.loadProfileBuilder == null) {
            this.loadProfileBuilder = new LoadProfileBuilder(this);
        }
        return loadProfileBuilder;
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return getDeviceLogBookFactory().getLogBookData(logBooks);
    }

    private Dsmr23LogBookFactory getDeviceLogBookFactory() {
        if (logBookFactory == null) {
            logBookFactory = new Dsmr23LogBookFactory(this);
        }
        return logBookFactory;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return getDsmr23Messaging().getSupportedMessages();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getDsmr23Messaging().executePendingMessages(pendingMessages);
    }

    protected Dsmr23Messaging getDsmr23Messaging() {
        if (dsmr23Messaging == null) {
            dsmr23Messaging = new Dsmr23Messaging(new Dsmr23MessageExecutor(this));
        }
        return dsmr23Messaging;
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getDsmr23Messaging().updateSentMessages(sentMessages);
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        return getDsmr23Messaging().format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }

    @Override
    public String prepareMessageContext(OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return "";
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.<DeviceProtocolDialect>asList(new SerialDeviceProtocolDialect(), new TcpDeviceProtocolDialect());
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return getRegisterFactory().readRegisters(registers);
    }

    private Dsmr23RegisterFactory getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new Dsmr23RegisterFactory(this);
        }
        return registerFactory;
    }

    @Override
    public String getVersion() {
        return "$Date: 2015-11-06 14:27:09 +0100 (Fri, 06 Nov 2015) $";
    }

    @Override
    public String getProtocolDescription() {
        return "EnergyICT WebRTU KP DLMS (NTA DSMR2.3) V2";
    }

}
