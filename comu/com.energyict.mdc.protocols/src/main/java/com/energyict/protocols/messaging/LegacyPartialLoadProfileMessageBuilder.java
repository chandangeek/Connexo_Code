package com.energyict.protocols.messaging;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifierType;
import com.energyict.mdc.upl.meterdata.identifiers.Introspector;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifierType;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Message builder class responsible of generating and parsing Partial LoadProfile request Messages for old {@link SmartMeterProtocol}s
 */
public class LegacyPartialLoadProfileMessageBuilder extends AbstractMessageBuilder {

    public static final String MESSAGETAG = "PartialLoadProfile";
    private static final String ProfileObisCodeTag = "LPObisCode";
    private static final String MeterSerialNumberTag = "MSerial";
    private static final String StartReadingTimeTag = "StartTime";
    private static final String EndReadingTimeTag = "EndTime";
    private static final String LoadProfileIdTag = "LPId";
    private static final String ChannelInfosTag = "Channels";
    private static final String ChannelTag = "Ch";
    private static final String ChannelIdTag = "Id";
    private static final String ChannelNametag = "Name";
    private static final String ChannelMeterIdentifier = "ID";
    private static final String ChannelUnitTag = "Unit";

    private final Clock clock;
    private final TopologyService topologyService;
    private final LoadProfileFactory loadProfileFactory;

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-DD-mm HH:mm:ss");
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
    private Instant startReadingTime;

    /**
     * Holds the Date from the <i>LAST</i> interval to fetch in the <CODE>LoadProfile</CODE>
     */
    private Instant endReadingTime;

    /**
     * Represents the database ID of the {@link com.energyict.mdc.upl.meterdata.LoadProfile} to read.
     * We will need this to set in the {@link com.energyict.mdc.protocol.api.device.data.ProfileData} object.
     */
    private int loadProfileId;

    /**
     * Contains a <CODE>List</CODE> of <b>necessary</b> channels to read from the meter.
     */
    private List<ChannelInfo> channelInfos;
    /**
     * The LoadProfile to read
     */
    private LoadProfile loadProfile;

    public LegacyPartialLoadProfileMessageBuilder(Clock clock, TopologyService topologyService, LoadProfileFactory loadProfileFactory) {
        super();
        this.clock = clock;
        this.topologyService = topologyService;
        this.loadProfileFactory = loadProfileFactory;
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

    public void setStartReadingTime(final Instant startReadingTime) {
        this.startReadingTime = startReadingTime;
    }

    public void setEndReadingTime(final Instant endReadingTime) {
        this.endReadingTime = endReadingTime;
    }

    public void setLoadProfileId(final int loadProfileId) {
        this.loadProfileId = loadProfileId;
    }

    public void setChannelInfos(final List<ChannelInfo> channelInfos) {
        this.channelInfos = channelInfos;
    }

    public ObisCode getProfileObisCode() {
        return profileObisCode;
    }

    public String getMeterSerialNumber() {
        return meterSerialNumber;
    }

    public Instant getStartReadingTime() {
        return startReadingTime;
    }

    public Instant getEndReadingTime() {
        return endReadingTime;
    }

    public int getLoadProfileId() {
        return loadProfileId;
    }

    public List<ChannelInfo> getChannelInfos() {
        return channelInfos;
    }

    /**
     * Returns the message content, this a the xml String containing the content for the message.
     *
     * @return the xml String containing the content for the message.
     */
    @Override
    protected String getMessageContent() {
        if (this.loadProfileId == 0 || this.profileObisCode == null) {
            throw new IllegalArgumentException("LoadProfile needed.");
        } else if (this.startReadingTime == null) {
            throw new IllegalArgumentException("StartTime can not be empty.");
        } else if (this.endReadingTime == null) {
            throw new IllegalArgumentException("EndTime can not be empty.");
        } else if ("".equalsIgnoreCase(this.meterSerialNumber)) {
            throw new IllegalArgumentException("Device Serial Number must be filled in.");
        }

        StringBuilder builder = new StringBuilder();
        builder.append("<");
        builder.append(MESSAGETAG);
        addAttribute(builder, ProfileObisCodeTag, this.profileObisCode);
        addAttribute(builder, MeterSerialNumberTag, this.meterSerialNumber);
        addAttribute(builder, StartReadingTimeTag, this.formatter.format(this.startReadingTime));
        addAttribute(builder, EndReadingTimeTag, this.formatter.format(this.endReadingTime));
        addAttribute(builder, LoadProfileIdTag, this.loadProfileId);
        builder.append(">");
        if (!this.channelInfos.isEmpty()) {
            builder.append("<");
            builder.append(ChannelInfosTag);
            builder.append(">");
            for (ChannelInfo channelInfo : this.channelInfos) {
                builder.append("<");
                builder.append(ChannelTag);
                addAttribute(builder, ChannelIdTag, channelInfo.getId());
                addAttribute(builder, ChannelNametag, channelInfo.getName());
                addAttribute(builder, ChannelUnitTag, channelInfo.getUnit());
                addAttribute(builder, ChannelMeterIdentifier, channelInfo.getMeterIdentifier());
                builder.append(" />");
            }
            builder.append("</");
            builder.append(ChannelInfosTag);
            builder.append(">");
        }

        builder.append("</");
        builder.append(MESSAGETAG);
        builder.append(">");
        return builder.toString();
    }

    @Override
    protected AdvancedMessageHandler getMessageHandler(final MessageBuilder builder) {
        return new PartialLoadProfileMessageHandler((LegacyPartialLoadProfileMessageBuilder) builder, getMessageNodeTag());
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
        StringBuilder sb = new StringBuilder(MESSAGETAG);
        sb.append(" ");
        sb.append("LoadProfileObisCode = '").append(profileObisCode).append("', ");
        sb.append("MeterSerialNumber = '").append(meterSerialNumber).append("', ");
        if (startReadingTime != null) {
            sb.append("StartReadingTime = '").append(formatter.format(startReadingTime)).append("', ");
        }
        if (endReadingTime != null) {
            sb.append("EndReadingTime = '").append(formatter.format(endReadingTime)).append("', ");
        }
        sb.append("LoadProfileId = '").append(loadProfileId).append("', ");
        return sb.toString();
    }

    public LoadProfile getLoadProfile() {
        if (this.loadProfile == null) {
            this.loadProfile = this.findLoadProfile(this.loadProfileId);
        }
        return loadProfile;
    }

    private LoadProfile findLoadProfile(int loadProfileId) {
        return (LoadProfile) this.loadProfileFactory.findLoadProfileById(loadProfileId);
    }

    /**
     * Set the value of the LoadProfile.
     * Find the <i>Master</i> Device of this LoadProfile and use his serialNumber in the XML.
     *
     * @param loadProfile the new LoadProfile to set
     */
    public void setLoadProfile(LoadProfile loadProfile) {
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

        setChannelInfos(createChannelInfos(this.loadProfile));
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
     * Create a <CODE>List</CODE> of <CODE>ChannelInfos</CODE> for the given <CODE>LoadProfile</CODE>.
     * If the channel has the com.energyict.mdw.coreimpl.ChannelImpl#storeData boolean is checked, then we can add it.
     * If it is not checked then it is not required for the protocol read the channel.
     *
     * @param loadProfile the given <CODE>LoadProfile</CODE>
     * @return the new List
     */
    private List<ChannelInfo> createChannelInfos(LoadProfile loadProfile) {
        //TODO there should be a readingType in here...
        return this.topologyService
                .getAllChannels(loadProfile)
                .stream()
                .map(channel -> ChannelInfo.ChannelInfoBuilder
                        .fromObisCode(channel.getRegisterTypeObisCode())
                        .unit(channel.getUnit())
                        .meterIdentifier(channel.getDevice().getSerialNumber())
                        .build())
                .collect(Collectors.toList());
    }

    public LoadProfileReader getLoadProfileReader() {
        return new LoadProfileReader(this.clock, this.profileObisCode, startReadingTime, endReadingTime, loadProfileId, new DeviceIdentifier<Device<?,?,?>>() {
            @Override
            public Introspector forIntrospection() {
                return new SerialNumberIntrospector(meterSerialNumber);
            }

            @Override
            public String getIdentifier() {
                return meterSerialNumber;
            }

            @Override
            public DeviceIdentifierType getDeviceIdentifierType() {
                return DeviceIdentifierType.SerialNumber;
            }

            @Override
            public String getXmlType() {
                return null;
            }

            @Override
            public void setXmlType(String ignore) {

            }

            @Override
            public Device<?, ?, ?> findDevice() {
                throw new IllegalArgumentException("This placeholder identifier can not provide you with a proper Device ...");
            }
        }, getChannelInfos(), meterSerialNumber, new LoadProfileIdentifier() {
            @Override
            public Introspector forIntrospection() {
                return new NullIntrospector();
            }

            @Override
            public LoadProfile getLoadProfile() {
                throw new IllegalArgumentException("This placeholder identifier can not provide you with a proper LoadProfile ...");
            }

            @Override
            public LoadProfileIdentifierType getLoadProfileIdentifierType() {
                return LoadProfileIdentifierType.Other;
            }

            @Override
            public List<Object> getIdentifier() {
                return null;
            }

            @Override
            public String getXmlType() {
                return null;
            }

            @Override
            public void setXmlType(String ignore) {

            }

            @Override
            public DeviceIdentifier getDeviceIdentifier() {
                throw new IllegalArgumentException("This placeholder identifier can not provide you with a proper deviceidentifier ...");
            }

            @Override
            public ObisCode getProfileObisCode() {
                return null;
            }
        });
    }

    /**
     * Handler to parse the XML message to Advanced message objects.
     */
    private class PartialLoadProfileMessageHandler extends AdvancedMessageHandler {

        private final LegacyPartialLoadProfileMessageBuilder messageBuilder;

        private PartialLoadProfileMessageHandler(final LegacyPartialLoadProfileMessageBuilder legacyPartialLoadProfileMessageBuilder, final String messageNodeTag) {
            super(messageNodeTag);
            this.messageBuilder = legacyPartialLoadProfileMessageBuilder;
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
                        this.messageBuilder.setProfileObisCode(ObisCode.fromString(atts.getValue(namespaceURI, ProfileObisCodeTag)));
                        this.messageBuilder.setMeterSerialNumber(atts.getValue(namespaceURI, MeterSerialNumberTag));
                        try {
                            String startReadingTime = atts.getValue(namespaceURI, StartReadingTimeTag);
                            this.messageBuilder.setStartReadingTime(formatter.parse(startReadingTime).toInstant());
                            String endReadingTime = atts.getValue(namespaceURI, EndReadingTimeTag);
                            this.messageBuilder.setEndReadingTime(formatter.parse(endReadingTime).toInstant());
                        } catch (ParseException e) {
                            throw new SAXException(e);
                        }
                        this.messageBuilder.setLoadProfileId(Integer.valueOf(atts.getValue(namespaceURI, LoadProfileIdTag)));
                    }
                } else if (ChannelInfosTag.equals(localName)) {
                    channelInfos = new ArrayList<>();
                } else if (ChannelTag.equals(localName)) {
                    channelInfos.add(new ChannelInfo(Integer.valueOf(atts.getValue(namespaceURI, ChannelIdTag)), atts.getValue(namespaceURI, ChannelNametag),
                            Unit.get(atts.getValue(namespaceURI, ChannelUnitTag)), atts.getValue(namespaceURI, ChannelMeterIdentifier), null));
                }
            }
        }
    }

    private static class SerialNumberIntrospector implements Introspector {
        private final String serialNumber;

        private SerialNumberIntrospector(String serialNumber) {
            this.serialNumber = serialNumber;
        }

        @Override
        public String getTypeName() {
            return "SerialNumber";
        }

        @Override
        public Object getValue(String role) {
            if ("serialNumber".equals(role)) {
                return this.serialNumber;
            } else {
                throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
            }
        }
    }

    private static class NullIntrospector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "Null";
        }

        @Override
        public Object getValue(String role) {
            throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
        }
    }

}