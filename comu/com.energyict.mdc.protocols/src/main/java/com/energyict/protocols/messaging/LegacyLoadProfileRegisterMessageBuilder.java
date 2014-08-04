package com.energyict.protocols.messaging;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.device.BaseChannel;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.device.BaseRegister;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Message builder class responsible of generating and parsing Partial LoadProfile request Messages for old {@link SmartMeterProtocol}s
 * <p/>
 * TODO some changes have to be made so the registers are given in the xml form
 */
public class LegacyLoadProfileRegisterMessageBuilder extends AbstractMessageBuilder {

    private static final String MESSAGETAG = "LoadProfileRegister";
    private static final String ProfileObisCodeTag = "LPObisCode";
    private static final String ProfileObisCodeTag_TYPO = "LPObiscode";
    private static final String MeterSerialNumberTag = "MSerial";
    private static final String StartReadingTimeTag = "StartTime";
    private static final String LoadProfileIdTag = "LPId";
    private static final String RtuRegistersTag = "RtuRegs";
    private static final String RegisterTag = "Reg";
    private static final String RegisterObiscodeTag = "OC";
    private static final String RtuRegisterSerialNumber = "ID";

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
    private int loadProfileId;

    /**
     * Contains a list of Registers to read
     */
    private List<com.energyict.mdc.protocol.api.device.data.Register> registers;

    /**
     * The LoadProfile to read
     */
    private BaseLoadProfile<BaseChannel> loadProfile;

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

    public void setLoadProfileId(final int loadProfileId) {
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

    public int getLoadProfileId() {
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
        } else if (this.meterSerialNumber.equalsIgnoreCase("")) {
            throw new BusinessException("noDeviceSerialNumber", "Device Serial Number must be filled in.");
        }

        checkRtuRegistersForLoadProfile();

        StringBuilder builder = new StringBuilder();
        builder.append("<");
        builder.append(MESSAGETAG);
        addAttribute(builder, ProfileObisCodeTag, this.profileObisCode);
        addAttribute(builder, MeterSerialNumberTag, this.meterSerialNumber);
        addAttribute(builder, StartReadingTimeTag, this.formatter.format(this.startReadingTime));
        addAttribute(builder, LoadProfileIdTag, this.loadProfileId);
        builder.append(">");
        if (this.registers.size() > 0) {
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

    private void checkRtuRegistersForLoadProfile() throws BusinessException {
        BaseDevice<BaseChannel, BaseLoadProfile<BaseChannel>, BaseRegister> device = this.loadProfile.getDevice();

        List<BaseRegister> allRegisters = device.getRegisters();
        for (BaseDevice dRtu : device.getPhysicalConnectedDevices()) {
            allRegisters.addAll(dRtu.getRegisters());
        }

        for (BaseChannel channel : this.loadProfile.getAllChannels()) {
            boolean contains = false;
            for (BaseRegister register : allRegisters) {
                contains |= register.getRegisterTypeObisCode().equals(channel.getRegisterTypeObisCode());
            }
            if (!contains) {
                throw new BusinessException("notAllRegisterMappingsDefined", "Not all RegisterMappings from {0} are defined on {1}", this.loadProfile, device);
            }
        }
    }

    @Override
    protected AdvancedMessageHandler getMessageHandler(final MessageBuilder builder) {
        return new LoadProfileRegisterMessageHandler((LegacyLoadProfileRegisterMessageBuilder) builder, getMessageNodeTag());
    }

    public static MessageBuilder fromXml(String xmlString) throws SAXException, IOException {
        MessageBuilder builder = new LegacyLoadProfileRegisterMessageBuilder();
        builder.initFromXml(xmlString);
        return builder;
    }

    /**
     * Return a readable description of the message being built
     *
     * @return The description of the message
     */
    public String getDescription() {
        StringBuilder builder = new StringBuilder(MESSAGETAG);
        builder.append(" ");
        builder.append("LoadProfileObisCode = '").append(profileObisCode).append("', ");
        builder.append("MeterSerialNumber = '").append(meterSerialNumber).append("', ");
        if (startReadingTime != null) {
            builder.append("StartReadingTime = '").append(formatter.format(startReadingTime)).append("', ");
        }
        builder.append("LoadProfileId = '").append(loadProfileId).append("', ");
        return builder.toString();
    }

    public BaseLoadProfile<BaseChannel> getLoadProfile() {
        if (this.loadProfile == null) {
            this.loadProfile = this.findLoadProfile(this.loadProfileId);
        }
        return loadProfile;
    }

    private BaseLoadProfile<BaseChannel> findLoadProfile(int loadProfileId) {
        List<LoadProfileFactory> modulesImplementing = Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(LoadProfileFactory.class);
        if (modulesImplementing.isEmpty()) {
            return null;
        } else {
            return modulesImplementing.get(0).findLoadProfileById(loadProfileId);
        }
    }

    /**
     * Set the value of the LoadProfile.
     * Find the <i>Master</i> Device of this LoadProfile and use his serialNumber in the XML.
     *
     * @param loadProfile the new LoadProfile to set
     */
    public void setLoadProfile(final BaseLoadProfile loadProfile) {
        BaseDevice<BaseChannel, BaseLoadProfile<BaseChannel>, BaseRegister> currentRtu = loadProfile.getDevice();
        while (currentRtu.isLogicalSlave() && currentRtu.getPhysicalGateway() != null) {
            currentRtu = currentRtu.getPhysicalGateway();
        }
        setMeterSerialNumber(currentRtu.getSerialNumber());
        BaseLoadProfile currentLoadProfile = null;
        for (BaseLoadProfile lProfile : currentRtu.getLoadProfiles()) {
            if (lProfile.getLoadProfileTypeId() == loadProfile.getLoadProfileTypeId()) {
                currentLoadProfile = lProfile;
            }
        }
        if (currentLoadProfile == null) {
            this.loadProfile = loadProfile;
        } else {
            this.loadProfile = currentLoadProfile;
        }
        setLoadProfileId((int) this.loadProfile.getId());
        setProfileObisCode(this.loadProfile.getLoadProfileTypeObisCode());
        setRegisters(createRegisterList(this.loadProfile));
    }

    /**
     * Create a <code>List</code> of <code>Register</code> for the given <code>LoadProfile</code>.
     *
     * @param loadProfile the given <code>LoadProfile</code>
     * @return the new Register List
     */
    private List<com.energyict.mdc.protocol.api.device.data.Register> createRegisterList(final BaseLoadProfile<?> loadProfile) {
        List<com.energyict.mdc.protocol.api.device.data.Register> registers = new ArrayList<>();
        for (BaseChannel channel : loadProfile.getAllChannels()) {
            registers.add(new com.energyict.mdc.protocol.api.device.data.Register(-1, channel.getRegisterTypeObisCode(), channel.getDevice().getSerialNumber()));
        }
        return registers;
    }

    public LoadProfileReader getLoadProfileReader() {
        return new LoadProfileReader(this.profileObisCode, startReadingTime, startReadingTime, loadProfileId, new DeviceIdentifier<BaseDevice<?, ?, ?>>() {
            @Override
            public String getIdentifier() {
                return meterSerialNumber;
            }

            @Override
            public BaseDevice<?, ?, ?> findDevice() {
                throw new IllegalArgumentException("This placeholder identifier can not provide you with a proper Device ...");
            }
        }, Collections.<ChannelInfo>emptyList(), meterSerialNumber, new LoadProfileIdentifier() {
            @Override
            public BaseLoadProfile findLoadProfile() {
                throw new IllegalArgumentException("This placeholder identifier can not provide you with a proper LoadProfile ...");
            }
        });
    }


    public int getRegisterSpecIdForRegister(com.energyict.mdc.protocol.api.device.data.Register register) {
        BaseDevice<BaseChannel, BaseLoadProfile<BaseChannel>, BaseRegister> device = null;
        for (BaseChannel channel : getLoadProfile().getAllChannels()) {
            if (channel.getDevice().getSerialNumber().equals(register.getSerialNumber())) {
                device = channel.getDevice();
                break;
            }
        }
        if (device != null) {
            for (BaseRegister rtuRegister : device.getRegisters()) {
                if (rtuRegister.getRegisterSpecObisCode().equalsIgnoreBChannel(register.getObisCode())) {
                    return (int) rtuRegister.getRegisterSpecId();
                }
            }
        }
        return -1;
    }

    /**
     * Handler to parse the XML message to Advanced message objects.
     */
    private class LoadProfileRegisterMessageHandler extends AdvancedMessageHandler {

        private final LegacyLoadProfileRegisterMessageBuilder messageBuilder;

        public LoadProfileRegisterMessageHandler(final LegacyLoadProfileRegisterMessageBuilder legacyLoadProfileRegisterMessageBuilder, final String messageNodeTag) {
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
                        String oc = atts.getValue(namespaceURI, ProfileObisCodeTag);
                        if (oc == null) {
                            // had a Capital typo in the name, could not just change it because workflows created the incorrect
                            oc = atts.getValue(namespaceURI, ProfileObisCodeTag_TYPO);
                        }
                        this.messageBuilder.setProfileObisCode(ObisCode.fromString(oc));
                        this.messageBuilder.setMeterSerialNumber(atts.getValue(namespaceURI, MeterSerialNumberTag));
                        try {
                            String startReadingTime = atts.getValue(namespaceURI, StartReadingTimeTag);
                            this.messageBuilder.setStartReadingTime(formatter.parse(startReadingTime));
                        } catch (ParseException e) {
                            throw new SAXException(e);
                        }
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
}
