package com.energyict.protocolimplv2.nta.dsmr23.eict;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.HHUSignOnV2;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.channels.ComChannelType;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.channels.serial.optical.rxtx.RxTxOpticalConnectionType;
import com.energyict.mdc.channels.serial.optical.serialio.SioOpticalConnectionType;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.CollectedLoadProfile;
import com.energyict.mdc.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.meterdata.CollectedLogBook;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.CollectedTopology;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdc.protocol.capabilities.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdc.tasks.OpticalDeviceProtocolDialect;
import com.energyict.mdc.tasks.TcpDeviceProtocolDialect;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimplv2.hhusignon.IEC1107HHUSignOn;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractNtaProtocol;
import com.energyict.protocolimplv2.security.DlmsSecuritySupport;
import com.energyict.protocolimplv2.security.DsmrSecuritySupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
public class WebRTUKP extends AbstractNtaProtocol {

    private DlmsSecuritySupport dlmsSecuritySupport;

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        getDlmsSessionProperties().setSerialNumber(offlineDevice.getSerialNumber());

        HHUSignOnV2 hhuSignOn = null;
        if (ComChannelType.SerialComChannel.is(comChannel)) {
            hhuSignOn = getHHUSignOn((SerialPortComChannel) comChannel);
        }
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
    public String getSerialNumber() {
        return getMeterInfo().getSerialNr();
    }

    @Override
    public void setTime(Date timeToSet) {
        try {
            getDlmsSession().getCosemObjectFactory().getClock().setAXDRDateTimeAttr(new AXDRDateTime(timeToSet, getTimeZone()));
        } catch (IOException e) {
            throw IOExceptionHandler.handle(e, getDlmsSession());
        }
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        return getLoadProfileBuilder().fetchLoadProfileConfiguration(loadProfilesToRead);
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        return getLoadProfileBuilder().getLoadProfileData(loadProfiles);
    }

    @Override
    public Date getTime() {
        try {
            return getDlmsSession().getCosemObjectFactory().getClock().getDateTime();
        } catch (IOException e) {
            throw IOExceptionHandler.handle(e, getDlmsSession());
        }
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return getDeviceLogBookFactory().getLogBookData(logBooks);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return getDsmr23Messaging().getSupportedMessages();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getDsmr23Messaging().executePendingMessages(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getDsmr23Messaging().updateSentMessages(sentMessages);
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return getDsmr23Messaging().format(propertySpec, messageAttribute);
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.<DeviceProtocolDialect>asList(new OpticalDeviceProtocolDialect(), new TcpDeviceProtocolDialect());
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
        getDlmsSessionProperties().addProperties(dialectProperties);
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        getDlmsSessionProperties().addProperties(deviceProtocolSecurityPropertySet.getSecurityProperties());
        getDlmsSessionProperties().setSecurityPropertySet(deviceProtocolSecurityPropertySet);
    }

    private DeviceProtocolSecurityCapabilities getSecuritySupport() {
        if (dlmsSecuritySupport == null) {
            dlmsSecuritySupport = new DsmrSecuritySupport();
        }
        return dlmsSecuritySupport;
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return getSecuritySupport().getSecurityProperties();
    }

    @Override
    public String getSecurityRelationTypeName() {
        return getSecuritySupport().getSecurityRelationTypeName();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return getSecuritySupport().getAuthenticationAccessLevels();
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return getSecuritySupport().getEncryptionAccessLevels();
    }

    @Override
    public PropertySpec getSecurityPropertySpec(String name) {
        return getSecuritySupport().getSecurityPropertySpec(name);
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return getRegisterFactory().readRegisters(registers);
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        return getMeterTopology().getDeviceTopology();
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }

    @Override
    public void addProperties(TypedProperties properties) {
        this.getDlmsSessionProperties().addProperties(properties);
    }

    @Override
    public String getProtocolDescription() {
        return "EnergyICT WebRTU KP NTA DSMR 2.3 (protocolimpl V2)";
    }
}
