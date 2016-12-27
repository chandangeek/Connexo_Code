package com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.gas;

import com.energyict.mdc.upl.SmartMeterProtocol;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.Link;
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
import com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.gas.composedobjects.ComposedMeterInfo;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.gas.events.ZigbeeGasEventProfiles;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.gas.messaging.ZigbeeGasMessaging;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.gas.messaging.ZigbeeMessageExecutor;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.gas.profile.ZigbeeGasLoadProfile;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.gas.registers.ZigbeeGasRegisterFactory;

import java.io.IOException;
import java.util.Date;
import java.util.List;
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

    public String getVersion() {
        return "$Date: 2015-08-26 14:01:32 +0200 (Wed, 26 Aug 2015) $";
    }

    @Override
    public boolean executeWakeUp(int communicationSchedulerId, Link link, Logger logger) throws BusinessException, IOException {
        //This method is not used anymore
        return true;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return getProperties().getPropertySpecs();
    }
}
