package com.energyict.messaging;

import com.energyict.mdc.upl.SmartMeterProtocol;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocolimplv2.messages.convertor.AbstractMessageConverter;
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
    private static final String RtuRegisterId = "RegID";

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
     * Contains a list of Registers to read
     */
    private List<com.energyict.protocol.Register> registers = new ArrayList<>();
    private int loadProfileId;

    public static String getMessageNodeTag() {
        return MESSAGETAG;
    }

    public static LegacyLoadProfileRegisterMessageBuilder fromXml(String xmlString) throws SAXException, IOException {
        LegacyLoadProfileRegisterMessageBuilder builder = new LegacyLoadProfileRegisterMessageBuilder();
        builder.initFromXml(xmlString);
        return builder;
    }

    public ObisCode getProfileObisCode() {
        return profileObisCode;
    }

    private void setProfileObisCode(final ObisCode profileObisCode) {
        this.profileObisCode = profileObisCode;
    }

    public String getMeterSerialNumber() {
        return meterSerialNumber;
    }

    private void setMeterSerialNumber(final String meterSerialNumber) {
        this.meterSerialNumber = meterSerialNumber;
    }

    public Date getStartReadingTime() {
        return startReadingTime;
    }

    private void setStartReadingTime(final Date startReadingTime) {
        this.startReadingTime = startReadingTime;
    }

    public List<com.energyict.protocol.Register> getRegisters() {
        return registers;
    }

    public LoadProfileReader getLoadProfileReader() {
        return new LoadProfileReader(this.profileObisCode, startReadingTime, startReadingTime, loadProfileId, meterSerialNumber, Collections.emptyList());
    }

    @Override
    protected AdvancedMessageHandler getMessageHandler(final MessageBuilder builder) {
        return new LoadProfileRegisterMessageHandler((LegacyLoadProfileRegisterMessageBuilder) builder, getMessageNodeTag());
    }

    private void setLoadProfileId(String loadProfileId) {
        this.loadProfileId = Integer.parseInt(loadProfileId);
    }

    /**
     * Handler to parse the XML message to Advanced message objects.
     */
    private class LoadProfileRegisterMessageHandler extends AdvancedMessageHandler {

        private final LegacyLoadProfileRegisterMessageBuilder messageBuilder;

        LoadProfileRegisterMessageHandler(final LegacyLoadProfileRegisterMessageBuilder legacyLoadProfileRegisterMessageBuilder, final String messageNodeTag) {
            super(messageNodeTag);
            this.messageBuilder = legacyLoadProfileRegisterMessageBuilder;
        }

        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
            super.startElement(namespaceURI, localName, qName, atts);
            if (messageTagEncountered()) {
                if (getMessageTag().equals(localName)) {
                    if (atts != null) {

                        //This is the formatter that was used to create the XML message
                        SimpleDateFormat formatter = AbstractMessageConverter.dateTimeFormatWithTimeZone;

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
                        this.messageBuilder.setLoadProfileId(atts.getValue(namespaceURI, LoadProfileIdTag));
                    }
                } else if (RtuRegistersTag.equals(localName)) {
                    registers = new ArrayList<>();
                } else if (RegisterTag.equals(localName)) {
                    String serialNumber = atts.getValue(namespaceURI, RtuRegisterSerialNumber);
                    String id = atts.getValue(namespaceURI, RtuRegisterId);
                    ObisCode obisCode = ObisCode.fromString(atts.getValue(namespaceURI, RegisterObiscodeTag));
                    registers.add(new com.energyict.protocol.Register(Integer.valueOf(id), obisCode, serialNumber));
                }
            }
        }
    }
}