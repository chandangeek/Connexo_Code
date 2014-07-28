package com.energyict.protocols.messaging;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.device.BaseChannel;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.device.BaseRegister;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static com.elster.jupiter.util.Checks.is;

/**
 * Message builder class responsible of generating and parsing Partial LoadProfile request Messages for {@link DeviceProtocol}s.<br></br>
 * <b>Warning:</b> For {@link SmartMeterProtocol}s the legacy builder ({@link LegacyLoadProfileRegisterMessageBuilder}) should be used.
 * <p/>
 */
public class LoadProfileRegisterMessageBuilder extends AbstractMessageBuilder {

    private static final String MESSAGETAG = "LoadProfile";
    private static final String ProfileObisCodeTag = "LPObisCode";
    private static final String ProfileObisCodeTag_TYPO = "LPObiscode";
    private static final String MeterSerialNumberTag = "MSerial";
    private static final String LoadProfileIdTag = "LPId";
    private static final String RtuRegistersTag = "RtuRegs";
    private static final String RegisterTag = "Reg";
    private static final String RegisterObiscodeTag = "OC";
    private static final String RtuRegisterSerialNumber = "ID";

    private SimpleDateFormat formatter;

    /**
     * Holds the <CODE>ObisCode</CODE> from the <CODE>LoadProfile</CODE> to read
     */
    private ObisCode profileObisCode;
    /**
     * Holds the serialNumber of the meter for this LoadProfile
     */
    private String meterSerialNumber;

    /**
     * Holds the Date from where to start fetching data from the <CODE>LoadProfile</CODE>
     */
    private Date startReadingTime;

    /**
     * Represents the database ID of the {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile} to read.
     * We will need this to set in the {@link com.energyict.mdc.protocol.api.device.data.ProfileData} object.
     */
    private long loadProfileId;

    /**
     * Contains a list of Registers to read
     */
    private List<com.energyict.mdc.protocol.api.device.data.Register> registers;

    /**
     * The LoadProfile to read
     */
    private BaseLoadProfile loadProfile;

    public static String getMessageNodeTag() {
        return MESSAGETAG;
    }

    public void setProfileObisCode(final ObisCode profileObisCode) {
        this.profileObisCode = profileObisCode;
    }

    public void setMeterSerialNumber(final String meterSerialNumber) {
        this.meterSerialNumber = meterSerialNumber;
    }

    public void setStartReadingTime(final Date startReadingTime) {
        this.startReadingTime = startReadingTime;
    }

    public void setLoadProfileId(final long loadProfileId) {
        this.loadProfileId = loadProfileId;
    }

    public ObisCode getProfileObisCode() {
        return profileObisCode;
    }

    public String getMeterSerialNumber() {
        return meterSerialNumber;
    }

    public Date getStartReadingTime() {
        return startReadingTime;
    }

    public long getLoadProfileId() {
        return loadProfileId;
    }

    public List<com.energyict.mdc.protocol.api.device.data.Register> getRegisters() {
        return registers;
    }

    public void setRegisters(final List<com.energyict.mdc.protocol.api.device.data.Register> registers) {
        this.registers = registers;
    }

    /**
     * Returns the message content, this a the xml String containing the content for the message.
     *
     * @return the xml String containing the content for the message.
     */
    @Override
    protected String getMessageContent() throws BusinessException {
        if (this.loadProfileId == 0 || this.profileObisCode == null) {
            throw new BusinessException("needLoadProfile", "LoadProfile needed.");
        } else if (this.startReadingTime == null) {
            throw new BusinessException("emptyStartTime", "StartTime can not be empty.");
        } else if (is(this.meterSerialNumber).empty()) {
            throw new BusinessException("noDeviceSerialNumber", "Device Serial Number must be filled in.");
        }

        checkRtuRegistersForLoadProfile();

        StringBuilder builder = new StringBuilder();
        builder.append("<");
        builder.append(MESSAGETAG);

        this.initializeFormatter();

        addAttribute(builder, ProfileObisCodeTag, this.profileObisCode);
        addAttribute(builder, MeterSerialNumberTag, this.meterSerialNumber);
        addAttribute(builder, LoadProfileIdTag, this.loadProfileId);
        builder.append(">");
        if (!this.registers.isEmpty()) {
            builder.append("<");
            builder.append(RtuRegistersTag);
            builder.append(">");
            for (com.energyict.mdc.protocol.api.device.data.Register register : this.registers) {
                builder.append("<");
                builder.append(RegisterTag);
                addAttribute(builder, RegisterObiscodeTag, register.getObisCode());
                addAttribute(builder, RtuRegisterSerialNumber, register.getSerialNumber());
                builder.append(" />");
            }
            builder.append("</");
            builder.append(RtuRegistersTag);
            builder.append(">");
        }

        builder.append("</");
        builder.append(MESSAGETAG);
        builder.append(">");
        return builder.toString();
    }

    private void initializeFormatter() {
        formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");    // Copied the defaults from the old UserShadow
        formatter.setTimeZone(TimeZone.getDefault());
    }

    private void checkRtuRegistersForLoadProfile() throws BusinessException {
        BaseDevice device = this.loadProfile.getDevice();

        List<BaseRegister> allRegisters = device.getRegisters();
        List<BaseDevice> physicalConnectedDevices = device.getPhysicalConnectedDevices();
        for (BaseDevice dRtu : physicalConnectedDevices) {
            allRegisters.addAll(dRtu.getRegisters());
        }

        List<BaseChannel> channels = this.loadProfile.getAllChannels();
        for (BaseChannel channel : channels) {
            boolean contains = false;
            for (BaseRegister allRegister : allRegisters) {
                contains |= allRegister.getRegisterTypeObisCode().equals(channel.getRegisterTypeObisCode());
            }
            if (!contains) {
                throw new BusinessException("notAllRegisterMappingsDefined", "Not all RegisterMappings from {0} are defined on {1}", this.loadProfile, device);
            }
        }
    }

    @Override
    protected AdvancedMessageHandler getMessageHandler(final MessageBuilder builder) {
        return new LoadProfileRegisterMessageHandler((LoadProfileRegisterMessageBuilder) builder, getMessageNodeTag());
    }

    public static MessageBuilder fromXml(String xmlString) throws SAXException, IOException {
        MessageBuilder builder = new LoadProfileRegisterMessageBuilder();
        builder.initFromXml(xmlString);
        return builder;
    }

    /**
     * Return a readable description of the message being built
     *
     * @return The description of the message
     */
    public String getDescription() {
        this.initializeFormatter();
        StringBuilder descriptionBuilder = new StringBuilder(MESSAGETAG);
        descriptionBuilder.append(" ");
        descriptionBuilder.append("LoadProfileObisCode = '").append(profileObisCode).append("', ");
        descriptionBuilder.append("MeterSerialNumber = '").append(meterSerialNumber).append("', ");
        if (startReadingTime != null) {
            descriptionBuilder.append("StartReadingTime = '").append(formatter.format(startReadingTime)).append("', ");
        }
        descriptionBuilder.append("LoadProfileId = '").append(loadProfileId).append("', ");
        return descriptionBuilder.toString();
    }

    public BaseLoadProfile getLoadProfile() {
        return loadProfile;
    }

    /**
     * Set the value of the LoadProfile.
     * Find the <i>Master</i> Device of this LoadProfile and use his serialNumber in the XML.
     *
     * @param loadProfile the new LoadProfile to set
     */
    public void setLoadProfile(final BaseLoadProfile loadProfile) {
        BaseDevice currentRtu = loadProfile.getDevice();
        while (currentRtu.isLogicalSlave() && currentRtu.getPhysicalGateway() != null) {
            currentRtu = currentRtu.getPhysicalGateway();
        }
        setMeterSerialNumber(currentRtu.getSerialNumber());
        BaseLoadProfile currentLoadProfile = null;
        List<BaseLoadProfile> currentRtuLoadProfiles = currentRtu.getLoadProfiles();
        for (BaseLoadProfile lProfile : currentRtuLoadProfiles) {
            if (lProfile.getLoadProfileTypeId() == loadProfile.getLoadProfileTypeId()) {
                currentLoadProfile = lProfile;
            }
        }
        if (currentLoadProfile == null) {
            this.loadProfile = loadProfile;
        } else {
            this.loadProfile = currentLoadProfile;
        }
        setLoadProfileId(this.loadProfile.getId());
        setProfileObisCode(this.loadProfile.getLoadProfileTypeObisCode());
        setRegisters(createRegisterList(this.loadProfile));
    }

    /**
     * Create a <code>List</code> of <code>Register</code> for the given <code>LoadProfile</code>.
     *
     * @param loadProfile the given <code>LoadProfile</code>
     * @return the new Register List
     */
    private List<com.energyict.mdc.protocol.api.device.data.Register> createRegisterList(final BaseLoadProfile loadProfile) {
        List<com.energyict.mdc.protocol.api.device.data.Register> registers = new ArrayList<>();
        List<BaseChannel> allChannels = loadProfile.getAllChannels();
        for (BaseChannel channel : allChannels) {
            registers.add(new com.energyict.mdc.protocol.api.device.data.Register(-1, channel.getRegisterTypeObisCode(), channel.getDevice().getSerialNumber()));
        }
        return registers;
    }

    public LoadProfileReader getLoadProfileReader() {
        return new LoadProfileReader(this.profileObisCode, startReadingTime, startReadingTime, loadProfileId, meterSerialNumber, Collections.<ChannelInfo>emptyList());
    }


    public long getRtuRegisterIdForRegister(com.energyict.mdc.protocol.api.device.data.Register register) {
        BaseDevice device = null;
        List<BaseChannel> allChannels = getLoadProfile().getAllChannels();
        for (BaseChannel channel : allChannels) {
            if (channel.getDevice().getSerialNumber().equals(register.getSerialNumber())) {
                device = channel.getDevice();
                break;
            }
        }
        if (device != null) {
            List<BaseRegister> deviceRegisters = device.getRegisters();
            for (BaseRegister deviceRegister : deviceRegisters) {
                if (deviceRegister.getRegisterSpecObisCode().equalsIgnoreBChannel(register.getObisCode())) {
                    return deviceRegister.getRegisterSpecId();
                }
            }
        }
        return -1;
    }

    /**
     * Handler to parse the XML message to Advanced message objects.
     */
    private class LoadProfileRegisterMessageHandler extends AdvancedMessageHandler {

        private final LoadProfileRegisterMessageBuilder messageBuilder;

        private LoadProfileRegisterMessageHandler(final LoadProfileRegisterMessageBuilder legacyLoadProfileRegisterMessageBuilder, final String messageNodeTag) {
            super(messageNodeTag);
            this.messageBuilder = legacyLoadProfileRegisterMessageBuilder;
        }

        @Override
        protected AbstractMessageBuilder getMessageBuilder() {
            return this.messageBuilder;
        }

        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
            super.startElement(namespaceURI, localName, qName, atts);
            if (messageTagEncountered()) {
                if (getMessageTag().equals(localName)) {
                    if (atts != null) {
                        initializeFormatter();
                        String oc = atts.getValue(namespaceURI, ProfileObisCodeTag);
                        if (oc == null) {
                            // had a Capital typo in the name, could not just change it because workflows created the incorrect
                            oc = atts.getValue(namespaceURI, ProfileObisCodeTag_TYPO);
                        }
                        this.messageBuilder.setProfileObisCode(ObisCode.fromString(oc));
                        this.messageBuilder.setMeterSerialNumber(atts.getValue(namespaceURI, MeterSerialNumberTag));
                        this.messageBuilder.setLoadProfileId(Integer.valueOf(atts.getValue(namespaceURI, LoadProfileIdTag)));
                    }
                } else if (RtuRegistersTag.equals(localName)) {
                    registers = new ArrayList<>();
                } else if (RegisterTag.equals(localName)) {
                    registers.add(new com.energyict.mdc.protocol.api.device.data.Register(-1, ObisCode.fromString(atts.getValue(namespaceURI, RegisterObiscodeTag)), atts.getValue(namespaceURI, RtuRegisterSerialNumber)));
                }
            }
        }
    }

    public static LoadProfileRegisterMessageBuilder getInstance() {
        return new LoadProfileRegisterMessageBuilder();
    }
}
