package com.energyict.protocols.messaging;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.protocol.api.LoadProfileReader;
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
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Message builder class responsible of generating and parsing Partial LoadProfile request Messages
 * for old {@link SmartMeterProtocol}s.
 * <p/>
 * TODO some changes have to be made so the registers are given in the xml form
 */
public class LegacyLoadProfileRegisterMessageBuilder extends AbstractMessageBuilder {

    public static final String MESSAGETAG = "LoadProfileRegister";
    private static final String ProfileObisCodeTag = "LPObisCode";
    private static final String ProfileObisCodeTag_TYPO = "LPObiscode";
    private static final String MeterSerialNumberTag = "MSerial";
    private static final String StartReadingTimeTag = "StartTime";
    private static final String LoadProfileIdTag = "LPId";
    private static final String RtuRegistersTag = "RtuRegs";
    private static final String RegisterTag = "Reg";
    private static final String RegisterObiscodeTag = "OC";
    private static final String RtuRegisterSerialNumber = "ID";

    private final TopologyService topologyService;
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
    private LoadProfile loadProfile;

    public LegacyLoadProfileRegisterMessageBuilder(TopologyService topologyService) {
        super();
        this.topologyService = topologyService;
    }

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
        } else if ("".equalsIgnoreCase(this.meterSerialNumber)) {
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

    private void checkRtuRegistersForLoadProfile() throws BusinessException {
        Device device = this.loadProfile.getDevice();

        List<Register> allRegisters = device.getRegisters();
        allRegisters.addAll(
                this.topologyService
                    .findPhysicalConnectedDevices(device)
                    .stream()
                    .flatMap(slave -> slave.getRegisters().stream())
                    .collect(Collectors.toList()));

        for (Channel channel : this.topologyService.getAllChannels(this.loadProfile)) {
            boolean contains = false;
            for (Register register : allRegisters) {
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

    public void fromXml(String xmlString) throws SAXException, IOException {
        this.initFromXml(xmlString);
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

    public LoadProfile getLoadProfile() {
        if (this.loadProfile == null) {
            this.loadProfile = this.findLoadProfile(this.loadProfileId);
        }
        return loadProfile;
    }

    private LoadProfile findLoadProfile(int loadProfileId) {
        List<LoadProfileFactory> modulesImplementing = Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(LoadProfileFactory.class);
        if (modulesImplementing.isEmpty()) {
            return null;
        } else {
            return (LoadProfile) modulesImplementing.get(0).findLoadProfileById(loadProfileId);
        }
    }

    /**
     * Set the value of the LoadProfile.
     * Find the <i>Master</i> Device of this LoadProfile and use his serialNumber in the XML.
     *
     * @param loadProfile the new LoadProfile to set
     */
    public void setLoadProfile(final LoadProfile loadProfile) {
        Device currentRtu = this.getDevice(loadProfile);
        setMeterSerialNumber(currentRtu.getSerialNumber());
        LoadProfile currentLoadProfile = null;
        for (LoadProfile lProfile : currentRtu.getLoadProfiles()) {
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

    @SuppressWarnings("unchecked")
    private Device getDevice(LoadProfile loadProfile) {
        boolean notAtTopOfTopology = true;
        Device currentRtu = loadProfile.getDevice();
        while (currentRtu.isLogicalSlave() && notAtTopOfTopology) {
            Optional<Device> physicalGateway = this.topologyService.getPhysicalGateway(currentRtu);
            if (physicalGateway.isPresent()) {
                currentRtu = physicalGateway.get();
            }
            else {
                notAtTopOfTopology = false;
            }
        }
        return currentRtu;
    }

    /**
     * Create a <code>List</code> of <code>Register</code> for the given <code>LoadProfile</code>.
     *
     * @param loadProfile the given <code>LoadProfile</code>
     * @return the new Register List
     */
    private List<com.energyict.mdc.protocol.api.device.data.Register> createRegisterList(final LoadProfile loadProfile) {
        return this.topologyService
                .getAllChannels(loadProfile)
                .stream()
                .map(channel -> new com.energyict.mdc.protocol.api.device.data.Register(-1, channel.getRegisterTypeObisCode(), channel.getDevice().getSerialNumber()))
                .collect(Collectors.toList());
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
        Device device = null;
        for (Channel channel : this.topologyService.getAllChannels(this.getLoadProfile())) {
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

        private LoadProfileRegisterMessageHandler(final LegacyLoadProfileRegisterMessageBuilder legacyLoadProfileRegisterMessageBuilder, final String messageNodeTag) {
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
