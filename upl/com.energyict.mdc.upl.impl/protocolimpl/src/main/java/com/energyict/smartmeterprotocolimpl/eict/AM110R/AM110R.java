package com.energyict.smartmeterprotocolimpl.eict.AM110R;

import com.energyict.cbo.BusinessException;
import com.energyict.cpo.Environment;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.Link;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dialer.coreimpl.IPDialer;
import com.energyict.dialer.coreimpl.NullDialer;
import com.energyict.dialer.coreimpl.SocketStreamConnection;
import com.energyict.dlms.*;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.Message;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocolimpl.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.common.AM110RSecurityProvider;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.common.MultipleClientRelatedObisCodes;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.composedobjects.ComposedMeterInfo;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.events.AM110REventProfiles;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.messaging.AM110RMessageExecutor;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.messaging.AM110RMessaging;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.wakeup.SmsWakeup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * GB Smart Enhanced Credit - AM110R HUB protocol
 *
 * @author sva
 * @since 7/05/13 - 10:39
 */
public class AM110R extends AbstractSmartDlmsProtocol implements MessageProtocol, SmartMeterProtocol, WakeUpProtocolSupport {

    /**
     * The properties to use for this protocol
     */
    private AM110RProperties properties;

    /**
     * The used ComposedMeterInfo
     */
    private ComposedMeterInfo meterInfo;

    /**
     * The used {@link AM110RRegisterFactory} to read and manage the AM110R registers
     */
    private AM110RRegisterFactory registerFactory;

    /**
     * * The used {@link com.energyict.smartmeterprotocolimpl.eict.AM110R.events.AM110REventProfiles} to read and manage the AM110R event logs
     */
    private AM110REventProfiles eventProfiles;

    /**
     * The used {@link com.energyict.smartmeterprotocolimpl.eict.AM110R.messaging.AM110RMessaging} for messaging
     */
    protected MessageProtocol messageProtocol;

    /**
     * Boolean indicating whether the AM11R should reboot at the end of the communication session or not
     */
    private boolean reboot = false;

    @Override
    protected void initAfterConnect() throws ConnectionException {
        if (this.dlmsSession != null) {
           // We need to update the correct TimeZone!!
            this.dlmsSession.updateTimeZone(getTimeZone());
        }
    }

    @Override
    public Date getTime() throws IOException {
        if (getProperties().isFirmwareUpdateSession()) {
            getLogger().severe("Using firmware update client. Skipping clock readout!");
            return new Date();
        } else {
            return super.getTime();
        }
    }

    @Override
    public void setTime(final Date newMeterTime) throws IOException {
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
                String message = "Could not retrieve the SerialNumber. " + e.getMessage();
                getLogger().finest(message);
                throw new IOException(message);
            }
        }
    }

    private void verifyFirmwareVersion() throws IOException {
        if (getProperties().verifyFirmwareVersion()) {
            String elsterFirmwareVersion = getMeterInfo().getElsterFirmwareVersion();
            if (!elsterFirmwareVersion.startsWith("ACH01.02.")) {
                throw new IOException("Unsupported firmware version (" + elsterFirmwareVersion + ") - only firmware versions ACH01.02.XX can be read out with this protocol.");
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
                String message = "Could not fetch the firmwareVersion. " + e.getMessage();
                getLogger().finest(message);
                return "Unknown version";
            }
        }
    }

    //---------- REGISTERS ----------//
    public RegisterInfo translateRegister(Register register) throws IOException {
        return getRegisterFactory().translateRegister(register);
    }

    public List<RegisterValue> readRegisters(List<Register> registers) throws IOException {
        return getRegisterFactory().readRegisters(registers);
    }

    //---------- EVENTS ----------//
    public List<MeterEvent> getMeterEvents(Date lastLogbookDate) throws IOException {
        return getEventProfiles().getEvents(lastLogbookDate);
    }

    //---------- LOAD PROFILES ----------//
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) throws IOException {
        // AM110R has no loadProfiles
        return new ArrayList<LoadProfileConfiguration>();
    }

    public List<ProfileData> getLoadProfileData(List<LoadProfileReader> loadProfiles) throws IOException {
        // AM110R has no loadProfiles
        return new ArrayList<ProfileData>();
    }

    //---------- MESSAGING ----------//
    public void applyMessages(List messageEntries) throws IOException {
        getMessageProtocol().applyMessages(messageEntries);
    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return getMessageProtocol().queryMessage(messageEntry);
    }

    public List getMessageCategories() {
        return getMessageProtocol().getMessageCategories();
    }

    public String writeMessage(Message msg) {
        return getMessageProtocol().writeMessage(msg);
    }

    public String writeTag(MessageTag tag) {
        return getMessageProtocol().writeTag(tag);
    }

    public String writeValue(MessageValue value) {
        return getMessageProtocol().writeValue(value);
    }
    //---------- END OF MESSAGING ----------//

    public DLMSMeterConfig getMeterConfig() {
        return getDlmsSession().getMeterConfig();
    }

    public ComposedMeterInfo getMeterInfo() {
        if (this.meterInfo == null) {
            this.meterInfo = new ComposedMeterInfo(getDlmsSession(), getProperties().isBulkRequest());
        }
        return this.meterInfo;
    }

    public AM110RRegisterFactory getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new AM110RRegisterFactory(this);
        }
        return registerFactory;
    }

    public AM110REventProfiles getEventProfiles() {
        if (eventProfiles == null) {
            this.eventProfiles = new AM110REventProfiles(this);
        }
        return eventProfiles;
    }

    public MessageProtocol getMessageProtocol() {
        if (this.messageProtocol == null) {
            this.messageProtocol = new AM110RMessaging(new AM110RMessageExecutor(this));
        }
        return messageProtocol;
    }

    public CosemObjectFactory getCosemObjectFactory() {
        return getDlmsSession().getCosemObjectFactory();
    }

    @Override
    public AM110RProperties getProperties() {
        if (this.properties == null) {
            this.properties = new AM110RProperties();
        }
        return this.properties;
    }

    /**
     * The protocol version
     */
    public String getVersion() {
        return "$Date: 2013-12-10 11:05:50 +0100 (Tue, 10 Dec 2013) $";
    }

    public void disConnect() throws IOException {
        super.disconnect();
    }

    /**
     * Disconnect from the physical device.
     * Close the association and check if we need to close the underlying connection
     */
    public void disconnect() throws IOException {
        if (reboot) {
            getCosemObjectFactory().getGenericInvoke(AM110RRegisterFactory.REBOOT_OBISCODE, DLMSClassId.SCRIPT_TABLE.getClassId(), 1).invoke();
        } else {
            getDlmsSession().disconnect();
        }
    }

    public void setReboot(boolean reboot) {
        this.reboot = reboot;
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
        enableHHUSignOn(link.getSerialCommunicationChannel(), false);

        if (getProperties().getDataTransportSecurityLevel() != 0 || getProperties().getAuthenticationSecurityLevel() == 5) {
            int backupClientId = getProperties().getClientMacAddress();
            String backupSecurityLevel = getProperties().getSecurityLevel();
            String password = getProperties().getPassword();
            CipheringType backUpCipheringType = getProperties().getCipheringType();

            getProperties().getProtocolProperties().setProperty(AM110RProperties.CLIENT_MAC_ADDRESS, "16");
            getProperties().getProtocolProperties().setProperty(AM110RProperties.SECURITY_LEVEL, "0:0");
            getProperties().getProtocolProperties().setProperty(AM110RProperties.CIPHERING_TYPE, "0");

            getDlmsSession().connect();
            long initialFrameCounter = getDlmsSession().getCosemObjectFactory().getData(MultipleClientRelatedObisCodes.frameCounterForClient(backupClientId)).getValue();
            getDlmsSession().disconnect();

            getProperties().getProtocolProperties().setProperty(AM110RProperties.CLIENT_MAC_ADDRESS, Integer.toString(backupClientId));
            getProperties().getProtocolProperties().setProperty(AM110RProperties.SECURITY_LEVEL, backupSecurityLevel);
            getProperties().getProtocolProperties().setProperty(SmartMeterProtocol.PASSWORD, password);
            getProperties().getProtocolProperties().setProperty(AM110RProperties.CIPHERING_TYPE, backUpCipheringType.getTypeString());

            if (link instanceof IPDialer || link instanceof NullDialer) {
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

    private void reInitDlmsSession(final Link link) throws ConnectionException {
        this.dlmsSession = new DlmsSession(link.getInputStream(), link.getOutputStream(), getLogger(), getProperties(), getTimeZone());
        enableHHUSignOn(link.getSerialCommunicationChannel(), false);
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean enableDataReadout) throws ConnectionException {
        if (commChannel != null) {
            if (getProperties().getConnectionMode() == ConnectionMode.IF2) {
                getDlmsSession().setHhuSignOn(new IF2HHUSignon(commChannel, getLogger()));
            }
        }
    }
}
