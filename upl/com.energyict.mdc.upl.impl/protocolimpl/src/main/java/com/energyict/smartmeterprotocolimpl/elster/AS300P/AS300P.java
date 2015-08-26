package com.energyict.smartmeterprotocolimpl.elster.AS300P;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.Link;
import com.energyict.dialer.coreimpl.IPDialer;
import com.energyict.dialer.coreimpl.SocketStreamConnection;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.Message;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocolimpl.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.common.AM110RSecurityProvider;
import com.energyict.smartmeterprotocolimpl.elster.AS300P.eventhandling.AS300PEventProfiles;
import com.energyict.smartmeterprotocolimpl.elster.AS300P.messaging.AS300PMessageExecutor;
import com.energyict.smartmeterprotocolimpl.elster.AS300P.messaging.AS300PMessaging;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * GB Smart Enhanced Credit - AS300P E-meter protocol
 *
 * @author sva
 * @since 8/05/13 - 10:25
 */
public class AS300P extends AbstractSmartDlmsProtocol implements MessageProtocol {

    /**
     * The properties to use for this protocol
     */
    protected AS300PProperties properties;

    /**
     * The used {@link AS300PObjectFactory} which provides and serves all objects required for the protocol.
     */
    private AS300PObjectFactory objectFactory;

    /**
     * The used {@link RegisterReader} to read and manage the AS300P registers
     */
    private RegisterReader registerReader;

    /**
     * The used {@link AS300PLoadProfileBuilder} to read and manage the load profiles
     */
    private AS300PLoadProfileBuilder loadProfileBuilder;

    /**
     * The used {@link AS300PEventProfiles} to read the logbooks
     */
    private AS300PEventProfiles eventProfiles;

    /**
     * The used {@link AS300PMessaging} for messaging
     */
    protected AS300PMessaging messageProtocol;

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
            return getObjectFactory().getClock().getDateTime();
        }
    }

    @Override
    public void setTime(Date newMeterTime) throws IOException {
        if (getProperties().isFirmwareUpdateSession()) {
            getLogger().severe("Using firmware update client. Skipping clock set!");
        } else {
            getObjectFactory().getClock().presetAdjustingTime(new AXDRDateTime(newMeterTime));
            getObjectFactory().getClock().adjustToPresetTime();
        }
    }

    public String getMeterSerialNumber() throws IOException {
        if (getProperties().isFirmwareUpdateSession()) {
            getLogger().severe("Using firmware update client. Skipping serial number check!");
            return getProperties().getSerialNumber();
        } else {
            verifyFirmwareVersion();
            return getObjectFactory().getSerialNumber().getString();
        }
    }

    private void verifyFirmwareVersion() throws IOException {
        if (getProperties().verifyFirmwareVersion()) {
            String elsterFirmwareVersion = new String(getObjectFactory().getActiveFirmwareIdACOR().getAttrbAbstractDataType(-1).getContentByteArray());
            if (!elsterFirmwareVersion.startsWith("ASP04.03.")) {
                throw new IOException("Unsupported firmware version (" + elsterFirmwareVersion + ") - only firmware versions ASP04.03.XX can be read out with this protocol.");
            }
        }
    }

    @Override
    protected void checkCacheObjects() throws IOException {
        getDlmsSession().getMeterConfig().setInstantiatedObjectList(AS300PObjectList.OBJECT_LIST);
    }

    public String getFirmwareVersion() throws IOException {
        if (getProperties().isFirmwareUpdateSession()) {
            getLogger().severe("Using firmware update client. Skipping firmware version readout!");
            return "";
        } else {
            String firmwareVersion = getObjectFactory().getFirmwareVersion().getString();
            String elsterFirmwareVersion;
            try {
                elsterFirmwareVersion = new String(getObjectFactory().getActiveFirmwareIdACOR().getAttrbAbstractDataType(-1).getContentByteArray());
            } catch (IOException e) {
                elsterFirmwareVersion = "unknown";
            }
            return firmwareVersion + " (" + elsterFirmwareVersion + ")";
        }
    }

    //---------- REGISTERS ----------//
    public RegisterInfo translateRegister(Register register) throws IOException {
        return getRegisterReader().translateRegister(register);
    }

    public List<RegisterValue> readRegisters(List<Register> registers) throws IOException {
        return getRegisterReader().readRegisters(registers);
    }

    //---------- EVENTS ----------//
    public List<MeterEvent> getMeterEvents(Date lastLogbookDate) throws IOException {
        return getEventProfiles().getEventLog(lastLogbookDate);
    }

    //---------- LOAD PROFILES ----------//
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) throws IOException {
        return getLoadProfileBuilder().fetchLoadProfileConfiguration(loadProfilesToRead);
    }

    public List<ProfileData> getLoadProfileData(List<LoadProfileReader> loadProfiles) throws IOException {
        return getLoadProfileBuilder().getLoadProfileData(loadProfiles);
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
        return writeValue(value);
    }

    //---------- END OF MESSAGING ----------//

    public String getVersion() {
        return "$Date: 2014-02-21 10:33:20 +0100 (Fri, 21 Feb 2014) $";
    }

    public AS300PObjectFactory getObjectFactory() {
        if (objectFactory == null) {
            objectFactory = new AS300PObjectFactory(getDlmsSession());
        }
        return objectFactory;
    }

    private RegisterReader getRegisterReader() {
        if (registerReader == null) {
            registerReader = new RegisterReader(this);
        }
        return registerReader;
    }

    private AS300PLoadProfileBuilder getLoadProfileBuilder() {
        if (loadProfileBuilder == null) {
            loadProfileBuilder = new AS300PLoadProfileBuilder(this);
        }
        return loadProfileBuilder;
    }

    private AS300PEventProfiles getEventProfiles() {
        if (eventProfiles == null) {
            eventProfiles = new AS300PEventProfiles(this);
        }
        return eventProfiles;
    }

    public AS300PMessaging getMessageProtocol() {
        if (this.messageProtocol == null) {
            this.messageProtocol = new AS300PMessaging(new AS300PMessageExecutor(this));
        }
        return messageProtocol;
    }

    @Override
    public AS300PProperties getProperties() {
        if (properties == null) {
            properties = new AS300PProperties();
        }
        return properties;
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

            getProperties().getProtocolProperties().setProperty(AS300PProperties.CLIENT_MAC_ADDRESS, "16");
            getProperties().getProtocolProperties().setProperty(AS300PProperties.SECURITY_LEVEL, "0:0");

            getDlmsSession().connect();
            long initialFrameCounter = getDlmsSession().getCosemObjectFactory().getData(getObjectFactory().getObisCodeProvider().getFrameCounterObisCode(backupClientId)).getValue();
            getDlmsSession().disconnect();

            getProperties().getProtocolProperties().setProperty(AS300PProperties.CLIENT_MAC_ADDRESS, Integer.toString(backupClientId));
            getProperties().getProtocolProperties().setProperty(AS300PProperties.SECURITY_LEVEL, backupSecurityLevel);
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
            this.objectFactory = null;
        } else {
            this.dlmsSession = null;
        }
    }

    private void reInitDlmsSession(final Link link) {
        this.dlmsSession = new DlmsSession(link.getInputStream(), link.getOutputStream(), getLogger(), getProperties(), getTimeZone());
    }
}
