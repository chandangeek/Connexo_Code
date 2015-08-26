package com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.gas;

import com.energyict.cbo.BusinessException;
import com.energyict.cpo.Environment;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.Link;
import com.energyict.dialer.coreimpl.IPDialer;
import com.energyict.dialer.coreimpl.SocketStreamConnection;
import com.energyict.dlms.DlmsSession;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.Message;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocolimpl.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.common.AM110RSecurityProvider;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.common.MultipleClientRelatedObisCodes;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.wakeup.SmsWakeup;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.gas.composedobjects.ComposedMeterInfo;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.gas.events.ZigbeeGasEventProfiles;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.gas.messaging.ZigbeeGasMessaging;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.gas.messaging.ZigbeeMessageExecutor;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.gas.profile.ZigbeeGasLoadProfile;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.gas.registers.ZigbeeGasRegisterFactory;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * GB Smart Enhanced Credit - BK-G4E gMeter
 *
 * @author sva
 * @since 7/05/13 - 10:39
 */
public class ZigbeeGas extends AbstractSmartDlmsProtocol implements SmartMeterProtocol, MessageProtocol, WakeUpProtocolSupport {

    /**
     * The properties to use for this protocol
     */
    private ZigbeeGasProperties properties;

    /**
     * The used ComposedMeterInfo
     */
    private ComposedMeterInfo meterInfo;

    /**
     * The used {@link com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.gas.registers.ZigbeeGasRegisterFactory} to read and manage the registers
     */
    private ZigbeeGasRegisterFactory registerFactory;

    /**
     * The used {@link com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.gas.profile.ZigbeeGasLoadProfile} to read and manage the load profiles
     */
    private ZigbeeGasLoadProfile zigbeeGasLoadProfile;

    /**
     * * The used {@link ZigbeeGasMessaging} to read and manage the event logs
     */
    private ZigbeeGasEventProfiles zigbeeGasEventProfiles;

    /**
     * The used {@link ZigbeeGasMessaging} for messaging
     */
    private ZigbeeGasMessaging zigbeeGasMessaging;

    @Override
    protected void initAfterConnect() throws ConnectionException {
        if (this.dlmsSession != null) {
            // We need to update the correct TimeZone!!
            this.dlmsSession.updateTimeZone(getTimeZone());
        }
    }

    public Date getTime() throws IOException {
        if (getProperties().isFirmwareUpdateSession()) {
            getLogger().severe("Using firmware update client. Skipping clock readout!");
            return new Date();
        } else {
            return super.getTime();
        }
    }

    @Override
    public void setTime(Date newMeterTime) throws IOException {
        getLogger().info("TimeSet not applied, meter is synchronized by the E-meter.");
    }

    public String getMeterSerialNumber() throws IOException {
        if (getProperties().isFirmwareUpdateSession()) {
            getLogger().severe("Using firmware update client. Skipping serial number check!");
            return getProperties().getSerialNumber();
        } else {
            verifyFirmwareVersion();
            try {
                return getMeterInfo().getSerialNumber();
            } catch (IOException e) {
                String message = "Could not retrieve the SerialNumber of the meter. " + e.getMessage();
                getLogger().finest(message);
                throw new IOException(message);
            }
        }
    }

    private void verifyFirmwareVersion() throws IOException {
        if (getProperties().verifyFirmwareVersion()) {
            String elsterFirmwareVersion = getMeterInfo().getElsterFirmwareVersion();
            if (!elsterFirmwareVersion.startsWith("AGM01.02.")) {
                throw new IOException("Unsupported firmware version (" + elsterFirmwareVersion + ") - only firmware versions AGM01.02.XX can be read out with this protocol.");
            }
        }
    }

    public String getFirmwareVersion() throws IOException {
        if (getProperties().isFirmwareUpdateSession()) {
            getLogger().severe("Using firmware update client. Skipping firmware version readout!");
            return "";
        } else {
            try {
                return getMeterInfo().getFirmwareVersion() + " (" + getMeterInfo().getElsterFirmwareVersion() + ")";
            } catch (IOException e) {
                getLogger().finest("Could not fetch the firmwareVersion. " + e.getMessage());
                return "UnKnown version";
            }
        }
    }

    //---------- REGISTERS ----------//
    public RegisterInfo translateRegister(final Register register) throws IOException {
        return getRegisterFactory().translateRegister(register);
    }

    public List<RegisterValue> readRegisters(final List<Register> registers) throws IOException {
        return getRegisterFactory().readRegisters(registers);
    }

    //---------- EVENTS ----------//
    public List<MeterEvent> getMeterEvents(final Date lastLogbookDate) throws IOException {
        return getZigbeeGasEventProfiles().getEvents(lastLogbookDate);
    }

    //---------- LOAD PROFILES ----------//
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(final List<LoadProfileReader> loadProfilesToRead) throws IOException {
        return getZigbeeGasLoadProfile().fetchLoadProfileConfiguration(loadProfilesToRead);
    }

    public List<ProfileData> getLoadProfileData(final List<LoadProfileReader> loadProfilesToRead) throws IOException {
        return getZigbeeGasLoadProfile().getLoadProfileData(loadProfilesToRead);
    }

    //---------- MESSAGING ----------//
    public void applyMessages(final List messageEntries) throws IOException {
        getMessageProtocol().applyMessages(messageEntries);
    }

    public MessageResult queryMessage(final MessageEntry messageEntry) throws IOException {
        return getMessageProtocol().queryMessage(messageEntry);
    }

    public List getMessageCategories() {
        return getMessageProtocol().getMessageCategories();
    }

    public String writeMessage(final Message msg) {
        return getMessageProtocol().writeMessage(msg);
    }

    public String writeTag(final MessageTag tag) {
        return getMessageProtocol().writeTag(tag);
    }

    public String writeValue(final MessageValue value) {
        return getMessageProtocol().writeValue(value);
    }

    //---------- END OF MESSAGING ----------//

    private ComposedMeterInfo getMeterInfo() {
        if (this.meterInfo == null) {
            this.meterInfo = new ComposedMeterInfo(getDlmsSession(), getProperties().isBulkRequest());
        }
        return this.meterInfo;
    }

    public ZigbeeGasRegisterFactory getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new ZigbeeGasRegisterFactory(this);
        }
        return registerFactory;
    }

    public ZigbeeGasLoadProfile getZigbeeGasLoadProfile() {
        if (zigbeeGasLoadProfile == null) {
            this.zigbeeGasLoadProfile = new ZigbeeGasLoadProfile(this);
        }
        return zigbeeGasLoadProfile;
    }

    public ZigbeeGasEventProfiles getZigbeeGasEventProfiles() {
        if (zigbeeGasEventProfiles == null) {
            zigbeeGasEventProfiles = new ZigbeeGasEventProfiles(this);
        }
        return zigbeeGasEventProfiles;
    }

    public ZigbeeGasMessaging getMessageProtocol() {
        if (zigbeeGasMessaging == null) {
            this.zigbeeGasMessaging = new ZigbeeGasMessaging(new ZigbeeMessageExecutor(this));
        }
        return this.zigbeeGasMessaging;
    }

    @Override
    public ZigbeeGasProperties getProperties() {
        if (this.properties == null) {
            this.properties = new ZigbeeGasProperties();
        }
        return this.properties;
    }

    /**
     * The protocol version
     */
    public String getVersion() {
        return "$Date: 2013-11-06 10:42:43 +0100 (Wed, 06 Nov 2013) $";
    }

    public boolean executeWakeUp(int communicationSchedulerId, Link link, Logger logger) throws BusinessException, IOException {
        //This method is not used anymore
        return true;
    }

    /**
     * Initializes the security provider and if necessary reads out the frame counter
     */
    private void initializeSecurityProvider(Link link, Logger logger) throws IOException {
        init(link.getInputStream(), link.getOutputStream(), TimeZone.getDefault(), logger);
        if (getProperties().getDataTransportSecurityLevel() != 0 || getProperties().getAuthenticationSecurityLevel() == 5) {
            int backupClientId = getProperties().getClientMacAddress();
            String backupSecurityLevel = getProperties().getSecurityLevel();
            String password = getProperties().getPassword();

            getProperties().getProtocolProperties().setProperty(ZigbeeGasProperties.CLIENT_MAC_ADDRESS, "16");
            getProperties().getProtocolProperties().setProperty(ZigbeeGasProperties.SECURITY_LEVEL, "0:0");

            getDlmsSession().connect();
            long initialFrameCounter = getDlmsSession().getCosemObjectFactory().getData(MultipleClientRelatedObisCodes.frameCounterForClient(backupClientId)).getValue();
            getDlmsSession().disconnect();

            getProperties().getProtocolProperties().setProperty(ZigbeeGasProperties.CLIENT_MAC_ADDRESS, Integer.toString(backupClientId));
            getProperties().getProtocolProperties().setProperty(ZigbeeGasProperties.SECURITY_LEVEL, backupSecurityLevel);
            getProperties().getProtocolProperties().setProperty(SmartMeterProtocol.PASSWORD, password);

            if (link instanceof IPDialer) {
                String ipAddress = link.getStreamConnection().getSocket().getInetAddress().getHostAddress();
                link.getStreamConnection().serverClose();
                link.setStreamConnection(new SocketStreamConnection(ipAddress + ":4059"));
                link.getStreamConnection().serverOpen();
            }

            getProperties().setSecurityProvider(new AM110RSecurityProvider(getProperties().getProtocolProperties()));
            ((AM110RSecurityProvider) (getProperties().getSecurityProvider())).setInitialFrameCounter(initialFrameCounter + 1);

            reInitDlmsSession(link);
        } else {
            this.dlmsSession = null;
        }
    }

    private void reInitDlmsSession(final Link link) {
        this.dlmsSession = new DlmsSession(link.getInputStream(), link.getOutputStream(), getLogger(), getProperties(), getTimeZone());
    }
}
