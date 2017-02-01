package com.energyict.messaging;

import com.energyict.cbo.Unit;
import com.energyict.mdc.upl.SmartMeterProtocol;
import com.energyict.mdc.upl.meterdata.LoadProfile;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocolimplv2.messages.convertor.AbstractMessageConverter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Message builder class responsible of generating and parsing Partial LoadProfile request Messages for old {@link SmartMeterProtocol}s
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
     * Represents the database ID of the {@link LoadProfile} to read.
     * We will need this to set in the {@link com.energyict.protocol.ProfileData} object.
     */
    private int loadProfileId;

    /**
     * Contains a <CODE>List</CODE> of <b>necessary</b> channels to read from the meter.
     */
    private List<ChannelInfo> channelInfos;

    public static String getMessageNodeTag() {
        return MESSAGETAG;
    }

    public static LegacyPartialLoadProfileMessageBuilder fromXml(String xmlString) throws SAXException, IOException {
        LegacyPartialLoadProfileMessageBuilder builder = new LegacyPartialLoadProfileMessageBuilder();
        builder.initFromXml(xmlString);
        return builder;
    }

    private void setProfileObisCode(final ObisCode profileObisCode) {
        this.profileObisCode = profileObisCode;
    }

    public void setStartReadingTime(final Date startReadingTime) {
        this.startReadingTime = startReadingTime;
    }

    public void setEndReadingTime(final Date endReadingTime) {
        this.endReadingTime = endReadingTime;
    }

    private void setLoadProfileId(final int loadProfileId) {
        this.loadProfileId = loadProfileId;
    }

    @Override
    protected AdvancedMessageHandler getMessageHandler(final MessageBuilder builder) {
        return new PartialLoadProfileMessageHandler((LegacyPartialLoadProfileMessageBuilder) builder, getMessageNodeTag());
    }

    public LoadProfileReader getLoadProfileReader() {
        return new LoadProfileReader(this.profileObisCode, startReadingTime, endReadingTime, loadProfileId, meterSerialNumber, channelInfos);
    }

    public String getMeterSerialNumber() {
        return meterSerialNumber;
    }

    private void setMeterSerialNumber(final String meterSerialNumber) {
        this.meterSerialNumber = meterSerialNumber;
    }

    /**
     * Handler to parse the XML message to Advanced message objects.
     */
    private class PartialLoadProfileMessageHandler extends AdvancedMessageHandler {

        private final LegacyPartialLoadProfileMessageBuilder messageBuilder;

        PartialLoadProfileMessageHandler(final LegacyPartialLoadProfileMessageBuilder legacyPartialLoadProfileMessageBuilder, final String messageNodeTag) {
            super(messageNodeTag);
            this.messageBuilder = legacyPartialLoadProfileMessageBuilder;
        }

        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
            super.startElement(namespaceURI, localName, qName, atts);
            if (messageTagEncountered()) {
                if (getMessageTag().equals(localName)) {
                    if (atts != null) {

                        //This is the formatter that was used to create the XML message
                        SimpleDateFormat formatter = AbstractMessageConverter.dateTimeFormatWithTimeZone;

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
                    channelInfos = new ArrayList<>();
                } else if (ChannelTag.equals(localName)) {
                    channelInfos.add(new ChannelInfo(Integer.valueOf(atts.getValue(namespaceURI, ChannelIdTag)), atts.getValue(namespaceURI, ChannelNametag),
                            Unit.get(atts.getValue(namespaceURI, ChannelUnitTag)), atts.getValue(namespaceURI, ChannelMeterIdentifier)));
                }
            }
        }
    }
}