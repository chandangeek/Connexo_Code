package com.energyict.smartmeterprotocolimpl.eict.AM110R;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.SmartMeterProtocol;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.Link;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dlms.ConnectionMode;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.IF2HHUSignon;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.protocol.BulkRegisterProtocol;
import com.energyict.protocol.LoadProfileConfiguration;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.Register;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.WakeUpProtocolSupport;
import com.energyict.protocolimpl.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.composedobjects.ComposedMeterInfo;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.events.AM110REventProfiles;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.messaging.AM110RMessageExecutor;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.messaging.AM110RMessaging;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
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
    protected BulkRegisterProtocol registerFactory;

    /**
     * * The used {@link com.energyict.smartmeterprotocolimpl.eict.AM110R.events.AM110REventProfiles} to read and manage the AM110R event logs
     */
    protected AM110REventProfiles eventProfiles;

    /**
     * The used {@link com.energyict.smartmeterprotocolimpl.eict.AM110R.messaging.AM110RMessaging} for messaging
     */
    protected MessageProtocol messageProtocol;

    /**
     * Boolean indicating whether the AM11R should reboot at the end of the communication session or not
     */
    private boolean reboot = false;
    private final PropertySpecService propertySpecService;

    public AM110R(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

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

    @Override
    public String getMeterSerialNumber()  {
            if (getProperties().isFirmwareUpdateSession()) {
                getLogger().severe("Using firmware update client. Skipping serial number check!");
                return getProperties().getSerialNumber();
            } else {
                try {
                    verifyFirmwareVersion();
                    return getMeterInfo().getSerialNumber();
                } catch (IOException e) {
                    throw DLMSIOExceptionHandler.handle(e, dlmsSession.getProperties().getRetries() + 1);
                }
            }
    }

    private void verifyFirmwareVersion() throws IOException {
        if (getProperties().verifyFirmwareVersion()) {
            String elsterFirmwareVersion = getMeterInfo().getElsterFirmwareVersion();
            if (!elsterFirmwareVersion.startsWith("ACH01.02.")) {
                throw new ProtocolException("Unsupported firmware version (" + elsterFirmwareVersion + ") - only firmware versions ACH01.02.XX can be read out with this protocol.");
            }
        }
    }

    @Override
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

    public RegisterInfo translateRegister(Register register) throws IOException {
        return getRegisterFactory().translateRegister(register);
    }

    @Override
    public List<RegisterValue> readRegisters(List<Register> registers) throws IOException {
        return getRegisterFactory().readRegisters(registers);
    }

    @Override
    public List<MeterEvent> getMeterEvents(Date lastLogbookDate) throws IOException {
        return getEventProfiles().getEvents(lastLogbookDate);
    }

    @Override
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) throws IOException {
        // AM110R has no loadProfiles
        return Collections.emptyList();
    }

    @Override
    public List<ProfileData> getLoadProfileData(List<LoadProfileReader> loadProfiles) throws IOException {
        // AM110R has no loadProfiles
        return Collections.emptyList();
    }

    @Override
    public void applyMessages(List messageEntries) throws IOException {
        getMessageProtocol().applyMessages(messageEntries);
    }

    @Override
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return getMessageProtocol().queryMessage(messageEntry);
    }

    @Override
    public List<MessageCategorySpec> getMessageCategories() {
        return getMessageProtocol().getMessageCategories();
    }

    @Override
    public String writeMessage(Message msg) {
        return getMessageProtocol().writeMessage(msg);
    }

    @Override
    public String writeTag(MessageTag tag) {
        return getMessageProtocol().writeTag(tag);
    }

    @Override
    public String writeValue(MessageValue value) {
        return getMessageProtocol().writeValue(value);
    }

    public DLMSMeterConfig getMeterConfig() {
        return getDlmsSession().getMeterConfig();
    }

    public ComposedMeterInfo getMeterInfo() {
        if (this.meterInfo == null) {
            this.meterInfo = new ComposedMeterInfo(getDlmsSession(), getProperties().isBulkRequest());
        }
        return this.meterInfo;
    }

    public BulkRegisterProtocol getRegisterFactory() {
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
            this.properties = new AM110RProperties(this.propertySpecService);
        }
        return this.properties;
    }

    @Override
    public String getVersion() {
        return "$Date: 2016-03-25 14:54:31 +0200 (Fri, 25 Mar 2016)$";
    }

    public void disConnect() throws IOException {
        super.disconnect();
    }

    @Override
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

    @Override
    public boolean executeWakeUp(int communicationSchedulerId, Link link, Logger logger) throws BusinessException, IOException {
        //This method is not used anymore
        return true;
    }

    protected void reInitDlmsSession(final Link link) throws ConnectionException {
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

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return getProperties().getPropertySpecs();
    }

}