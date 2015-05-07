package com.energyict.messaging;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Unit;
import com.energyict.mdw.core.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.LoadProfileReader;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Message builder class responsible of generating and parsing Partial LoadProfile request Messages for old {@link com.energyict.protocol.SmartMeterProtocol}s
 */
public class LegacyPartialLoadProfileMessageBuilder extends AbstractMessageBuilder {

    private static final String MESSAGETAG = "PartialLoadProfile";
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
     * Represents the database ID of the {@link com.energyict.mdw.core.LoadProfile} to read.
     * We will need this to set in the {@link com.energyict.protocol.ProfileData} object.
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


        User user = MeteringWarehouse.getCurrentUser();
        formatter = new SimpleDateFormat(user.getDateFormat() + " " + user.getLongTimeFormat());
        formatter.setTimeZone(MeteringWarehouse.getCurrent().getSystemTimeZone());

        addAttribute(builder, ProfileObisCodeTag, this.profileObisCode);
        addAttribute(builder, MeterSerialNumberTag, this.meterSerialNumber);
        addAttribute(builder, StartReadingTimeTag, this.formatter.format(this.startReadingTime));
        addAttribute(builder, EndReadingTimeTag, this.formatter.format(this.endReadingTime));
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
        return new PartialLoadProfileMessageHandler((LegacyPartialLoadProfileMessageBuilder) builder, getMessageNodeTag());
    }

    public static MessageBuilder fromXml(String xmlString) throws SAXException, IOException {
        MessageBuilder builder = new LegacyPartialLoadProfileMessageBuilder();
        builder.initFromXml(xmlString);
        return builder;
    }

    /**
     * Return a readable description of the message being built
     *
     * @return The description of the message
     */
    public String getDescription() {
        User user = MeteringWarehouse.getCurrentUser();
        formatter = new SimpleDateFormat(user.getDateFormat() + " " + user.getLongTimeFormat());
        formatter.setTimeZone(MeteringWarehouse.getCurrent().getSystemTimeZone());

        StringBuffer buf = new StringBuffer(MESSAGETAG);
        buf.append(" ");
        buf.append("LoadProfileObisCode = '").append(profileObisCode).append("', ");
        buf.append("MeterSerialNumber = '").append(meterSerialNumber).append("', ");
        if (startReadingTime != null) {
            buf.append("StartReadingTime = '").append(formatter.format(startReadingTime)).append("', ");
        }
        if (endReadingTime != null) {
            buf.append("EndReadingTime = '").append(formatter.format(endReadingTime)).append("', ");
        }
        buf.append("LoadProfileId = '").append(loadProfileId).append("', ");
        return buf.toString();
    }

    public LoadProfile getLoadProfile() {
        if (this.loadProfile == null) {
            this.loadProfile = MeteringWarehouse.getCurrent().getLoadProfileFactory().find(this.loadProfileId);
        }
        return loadProfile;
    }

    /**
     * Set the value of the LoadProfile.
     * Find the <i>Master</i> Device of this LoadProfile and use his serialNumber in the XML.
     *
     * @param loadProfile the new LoadProfile to set
     */
    public void setLoadProfile(final LoadProfile loadProfile) {
        Device currentRtu = loadProfile.getRtu();
        while (currentRtu.getDeviceType().isLogicalSlave() && currentRtu.getGateway() != null) {
            currentRtu = currentRtu.getGateway();
        }
        setMeterSerialNumber(currentRtu.getSerialNumber());
        LoadProfile currentLoadProfile = null;
        for (LoadProfile lProfile : currentRtu.getLoadProfiles()) {
            if (lProfile.getLoadProfileType().equals(loadProfile.getLoadProfileType())) {
                currentLoadProfile = lProfile;
            }
        }
        if (currentLoadProfile == null) {
            this.loadProfile = loadProfile;
        } else {
            this.loadProfile = currentLoadProfile;
        }
        setLoadProfileId(this.loadProfile.getId());
        setProfileObisCode(this.loadProfile.getLoadProfileType().getObisCode());

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
    private static List<ChannelInfo> createChannelInfos(LoadProfile lpt) {
        List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
        for (Channel lpChannel : lpt.getAllChannels()) {
            if (lpChannel.isStoreData()) {
                channelInfos.add(new ChannelInfo(channelInfos.size(), lpChannel.getDeviceRegisterMapping().getObisCode().toString(), lpChannel.getDeviceRegisterMapping().getUnit(), lpChannel.getDevice().getSerialNumber()));
            }
        }
        return channelInfos;
    }

    public LoadProfileReader getLoadProfileReader() {
        return new LoadProfileReader(this.profileObisCode, startReadingTime, endReadingTime, loadProfileId, meterSerialNumber, getChannelInfos());
    }

    /**
     * Handler to parse the XML message to Advanced message objects.
     */
    private class PartialLoadProfileMessageHandler extends AdvancedMessageHandler {

        private final LegacyPartialLoadProfileMessageBuilder messageBuilder;

        public PartialLoadProfileMessageHandler(final LegacyPartialLoadProfileMessageBuilder legacyPartialLoadProfileMessageBuilder, final String messageNodeTag) {
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

                        User user = MeteringWarehouse.getCurrentUser();
                        formatter = new SimpleDateFormat(user.getDateFormat() + " " + user.getLongTimeFormat());
                        formatter.setTimeZone(MeteringWarehouse.getCurrent().getSystemTimeZone());

                        this.messageBuilder.setProfileObisCode(ObisCode.fromString(atts.getValue(namespaceURI, ProfileObisCodeTag)));
                        this.messageBuilder.setMeterSerialNumber(atts.getValue(namespaceURI, MeterSerialNumberTag));
                        try {
                            String startReadingTime = atts.getValue(namespaceURI, StartReadingTimeTag);
                            this.messageBuilder.setStartReadingTime(formatter.parse(startReadingTime));
                            String endReadingTime = atts.getValue(namespaceURI, EndReadingTimeTag);
                            this.messageBuilder.setEndReadingTime(formatter.parse(endReadingTime));
                        } catch (ParseException e) {
                            throw new SAXException(e);
                        }
                        this.messageBuilder.setLoadProfileId(Integer.valueOf(atts.getValue(namespaceURI, LoadProfileIdTag)));
                    }
                } else if (ChannelInfosTag.equals(localName)) {
                    channelInfos = new ArrayList<ChannelInfo>();
                } else if (ChannelTag.equals(localName)) {
                    channelInfos.add(new ChannelInfo(Integer.valueOf(atts.getValue(namespaceURI, ChannelIdTag)), atts.getValue(namespaceURI, ChannelNametag),
                            Unit.get(atts.getValue(namespaceURI, ChannelUnitTag)), atts.getValue(namespaceURI, ChannelMeterIdentifier)));
                }
            }
        }
    }
}
