package com.energyict.protocols.messaging;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.device.BaseChannel;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Message builder class responsible of generating and parsing Partial LoadProfile request Messages for {@link DeviceProtocol}s.<br></br>
 * <b>Warning:</b> For {@link SmartMeterProtocol}s the legacy builder ({@link LegacyLoadProfileRegisterMessageBuilder}) should be used.
 * <p/>
 */
public class PartialLoadProfileMessageBuilder extends AbstractMessageBuilder {

    private static final String MESSAGETAG = "LoadProfile";
    private static final String ProfileObisCodeTag = "LPObisCode";
    private static final String MeterSerialNumberTag = "MSerial";
    private static final String LoadProfileIdTag = "LPId";
    private static final String ChannelInfosTag = "Channels";
    private static final String ChannelTag = "Ch";
    private static final String ChannelIdTag = "Id";
    private static final String ChannelNametag = "Name";
    private static final String ChannelMeterIdentifier = "ID";
    private static final String ChannelUnitTag = "Unit";

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
     * Holds the Date from the <i>LAST</i> interval to fetch in the <CODE>LoadProfile</CODE>
     */
    private Date endReadingTime;

    /**
     * Represents the database ID of the {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile} to read.
     * We will need this to set in the {@link com.energyict.mdc.protocol.api.device.data.ProfileData} object.
     */
    private long loadProfileId;

    /**
     * Contains a <CODE>List</CODE> of <b>necessary</b> channels to read from the meter.
     */
    private List<ChannelInfo> channelInfos;

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

    public void setEndReadingTime(final Date endReadingTime) {
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

    public Date getStartReadingTime() {
        return startReadingTime;
    }

    public Date getEndReadingTime() {
        return endReadingTime;
    }

    public long getLoadProfileId() {
        return loadProfileId;
    }

    public List<ChannelInfo> getChannelInfos() {
        return channelInfos;
    }

    private void initializeFormatter() {
        formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");    // Copied the defaults from the old UserShadow
        formatter.setTimeZone(TimeZone.getDefault());
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
        } else if (this.endReadingTime == null) {
            throw new BusinessException("emptyEndTime", " EndTime can not be empty.");
        } else if (this.meterSerialNumber.equalsIgnoreCase("")) {
            throw new BusinessException("noDeviceSerialNumber", "Device Serial Number must be filled in.");
        }

        StringBuilder builder = new StringBuilder();
        builder.append("<");
        builder.append(MESSAGETAG);

        this.initializeFormatter();

        addAttribute(builder, ProfileObisCodeTag, this.profileObisCode);
        addAttribute(builder, MeterSerialNumberTag, this.meterSerialNumber);
        addAttribute(builder, LoadProfileIdTag, this.loadProfileId);
        builder.append(">");
        if (this.channelInfos.size() > 0) {
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
        return new PartialLoadProfileMessageHandler((PartialLoadProfileMessageBuilder) builder, getMessageNodeTag());
    }

    public static MessageBuilder fromXml(String xmlString) throws SAXException, IOException {
        MessageBuilder builder = new PartialLoadProfileMessageBuilder();
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
        if (endReadingTime != null) {
            descriptionBuilder.append("EndReadingTime = '").append(formatter.format(endReadingTime)).append("', ");
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
        setLoadProfileId((int) this.loadProfile.getId());
        setProfileObisCode(this.loadProfile.getLoadProfileTypeObisCode());

        setChannelInfos(createChannelInfos(this.loadProfile));
    }

    /**
     * Create a <CODE>List</CODE> of <CODE>ChannelInfos</CODE> for the given <CODE>LoadProfile</CODE>.
     * If the channel has the com.energyict.mdw.coreimpl.ChannelImpl#storeData boolean is checked, then we can add it.
     * If it is not checked then it is not required for the protocol read the channel.
     *
     * @param lpt the given <CODE>LoadProfile</CODE>
     * @return the new List
     */
    private static List<ChannelInfo> createChannelInfos(BaseLoadProfile lpt) {
        List<ChannelInfo> channelInfos = new ArrayList<>();
        List<BaseChannel> allChannels = lpt.getAllChannels();
        for (BaseChannel lpChannel : allChannels) {
            channelInfos.add(new ChannelInfo(channelInfos.size(), lpChannel.getRegisterTypeObisCode().toString(), lpChannel.getUnit(), lpChannel.getDevice().getSerialNumber(), null)); //TODO add the readingType
        }
        return channelInfos;
    }

    public LoadProfileReader getLoadProfileReader() {
        return new LoadProfileReader(this.profileObisCode, startReadingTime, endReadingTime, loadProfileId,
                new DeviceIdentifier<BaseDevice<?, ?, ?>>() {
                    @Override
                    public String getIdentifier() {
                        return meterSerialNumber;
                    }

                    @Override
                    public BaseDevice<?, ?, ?> findDevice() {
                        throw new IllegalArgumentException("This placeholder identifier can not provide you with a proper Device ...");
                    }
                }, getChannelInfos(), meterSerialNumber, new LoadProfileIdentifier() {
            @Override
            public BaseLoadProfile findLoadProfile() {
                throw new IllegalArgumentException("This placeholder identifier can not provide you with a proper LoadProfile ...");
            }
        });
    }

    /**
     * Handler to parse the XML message to Advanced message objects.
     */
    private class PartialLoadProfileMessageHandler extends AdvancedMessageHandler {

        private final PartialLoadProfileMessageBuilder messageBuilder;

        public PartialLoadProfileMessageHandler(final PartialLoadProfileMessageBuilder legacyPartialLoadProfileMessageBuilder, final String messageNodeTag) {
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
                        initializeFormatter();
                        this.messageBuilder.setProfileObisCode(ObisCode.fromString(atts.getValue(namespaceURI, ProfileObisCodeTag)));
                        this.messageBuilder.setMeterSerialNumber(atts.getValue(namespaceURI, MeterSerialNumberTag));
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

    public static PartialLoadProfileMessageBuilder getInstance() {
        return new PartialLoadProfileMessageBuilder();
    }
}
