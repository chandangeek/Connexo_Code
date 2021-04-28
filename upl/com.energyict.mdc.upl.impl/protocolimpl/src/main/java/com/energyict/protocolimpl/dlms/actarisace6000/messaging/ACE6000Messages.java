package com.energyict.protocolimpl.dlms.actarisace6000.messaging;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.messages.legacy.*;
import com.energyict.messaging.TimeOfUseMessageBuilder;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageResult;
import com.energyict.protocolimpl.base.ActivityCalendarController;
import com.energyict.protocolimpl.dlms.actarisace6000.ACE6000;
import com.energyict.protocolimpl.dlms.common.DLMSActivityCalendarController;
import com.energyict.protocolimpl.messages.ProtocolMessageCategories;
import com.energyict.protocolimpl.messages.ProtocolMessages;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.messages.codetableparsing.CodeTableXmlParsing;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.DisplayDeviceParametersMessage;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;
import static com.energyict.protocolimplv2.messages.convertor.ACE6000MessageConverter.VOLTAGE_AND_CURRENT_PARAMS;

/**
 * ACE6000 Messages
 */
public class ACE6000Messages extends ProtocolMessages  {

    private static final ObisCode DisplayGeneralParameters = ObisCode.fromString("0.0.141.0.1.255");
    private static final ObisCode DisplayReadOutTableParameters = ObisCode.fromString("0.0.21.0.0.255");
    private static final ObisCode SecondaryMetrologyInstallationParameters = ObisCode.fromString("0.0.148.2.2.255");

    private static final String SET_DISPLAY_MESSAGE = "Write a message to the LCD of the meter";
    public static final String RAW_CONTENT = "RawContent";
    private static final String SET_DISPLAY_MESSAGE_TAG = "SET_DISPLAY_MESSAGE";
    private static final String SELECTION_OF_12_LINES_IN_TOU_TABLE = RtuMessageConstant.SELECTION_OF_12_LINES_IN_TOU_TABLE;
    private static final String SELECTION_OF_12_LINES_IN_TOU_TABLE_DISPLAY = "Selection of 12 lines in TOU table display";
    private static final String WRITE_VOLTAGE_AND_CURRENT_RATIOS = "Write voltage and current ratios";
    private static final String BILLINGRESET = RtuMessageConstant.BILLINGRESET;
    private static final String BILLINGRESET_DISPLAY = "Billing reset";

    private final ACE6000 protocol;
    private final TariffCalendarExtractor tariffCalendarExtractor;
    private final TariffCalendarFinder tariffCalendarFinder;
    private final DeviceMessageFileFinder messageFileFinder;
    private final DeviceMessageFileExtractor deviceMessageFileExtractor;
    private int currentLineCalendarNumber = 0;

    public ACE6000Messages(ACE6000 ace6000, TariffCalendarExtractor tariffCalendarExtractor, TariffCalendarFinder tariffCalendarFinder, DeviceMessageFileFinder messageFileFinder, DeviceMessageFileExtractor deviceMessageFileExtractor) {
        this.protocol = ace6000;
        this.tariffCalendarExtractor = tariffCalendarExtractor;
        this.tariffCalendarFinder = tariffCalendarFinder;
        this.messageFileFinder = messageFileFinder;
        this.deviceMessageFileExtractor = deviceMessageFileExtractor;
    }

    @Override
    public void applyMessages(List<MessageEntry> messageEntries) throws IOException {

    }

    @Override
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        try {
            if (isItThisMessage(messageEntry, TimeOfUseMessageBuilder.getMessageNodeTag())) {
                infoLog("Sending TimeOfUse message.");
                updateTimeOfUse(messageEntry);
                infoLog("TimeOfUse message successful");
                return MessageResult.createSuccess(messageEntry);
            } else if (messageEntry.getContent() != null) {
                if (messageEntry.getContent().contains(RtuMessageConstant.TOU_ACTIVITY_CAL)) {
                    return writeActivityCalendar(messageEntry);
                } else if (messageEntry.getContent().contains(RtuMessageConstant.TOU_SPECIAL_DAYS)) {
                    return writeSpecialDays(messageEntry);
                } else if (messageEntry.getContent().contains(RtuMessageConstant.SELECTION_OF_12_LINES_IN_TOU_TABLE)) {
                    return write12LinesInActivityCalendar(messageEntry);
                } else if (isItThisMessage(messageEntry, DisplayDeviceParametersMessage.DISPLAY_GENERAL_PARAMETERS.toString())) {
                    infoLog(String.format("Sending %s message.", DisplayDeviceParametersMessage.DISPLAY_GENERAL_PARAMETERS));
                    displayGeneralParameters(messageEntry);
                    infoLog(String.format("%s message succesfull.", DisplayDeviceParametersMessage.DISPLAY_GENERAL_PARAMETERS));
                    return MessageResult.createSuccess(messageEntry);
                } else if (isItThisMessage(messageEntry, DisplayDeviceParametersMessage.DISPLAY_READOUT_TABLE_PARAMETERS.toString())) {
                    infoLog(String.format("Sending %s message.", DisplayDeviceParametersMessage.DISPLAY_READOUT_TABLE_PARAMETERS));
                    displayReadoutTableParameters(messageEntry);
                    infoLog(String.format("%s message succesfull.", DisplayDeviceParametersMessage.DISPLAY_READOUT_TABLE_PARAMETERS));
                    return MessageResult.createSuccess(messageEntry);
                } else if (messageEntry.getContent().contains(BILLINGRESET)) {
                    infoLog("Sending message Billing reset.");
                    this.protocol.billingReset();
                    infoLog("Billing reset message successful.");
                    return MessageResult.createSuccess(messageEntry);
                } else if (messageEntry.getContent().contains(VoltageRatioDenominatorAttributeName)) {
                    infoLog("Sending " + WRITE_VOLTAGE_AND_CURRENT_RATIOS + " message.");
                    setWriteVoltageAndCurrentRatios(getContentBetweenTags(VOLTAGE_AND_CURRENT_PARAMS, messageEntry.getContent()));
                    infoLog(WRITE_VOLTAGE_AND_CURRENT_RATIOS + " message successful");
                    return MessageResult.createSuccess(messageEntry);
                }
            } else if (isItThisMessage(messageEntry, TimeOfUseMessageBuilder.getMessageNodeTag())) {
                infoLog("Sending TimeOfUse message.");
                updateTimeOfUse(messageEntry);
                infoLog("TimeOfUse message successful");
                return MessageResult.createSuccess(messageEntry);
            } else {
                protocol.getLogger().log(Level.SEVERE, "Error executing message - the message content is empty, probably wrong user file id specified.");
                return MessageResult.createFailed(messageEntry);
            }
        } catch (DataAccessResultException e) {
            protocol.getLogger().log(Level.SEVERE, "Error executing message: " + e.getMessage());
            return MessageResult.createFailed(messageEntry, e.getMessage());
        } catch (NumberFormatException e) {
            protocol.getLogger().log(Level.SEVERE, "Error executing message: " + e.getMessage());
            return MessageResult.createFailed(messageEntry, e.getMessage());
        } catch (SAXException e) {
            String msg = "Cannot process ActivityCalendar upgrade message due to an XML parsing error [" + e.getMessage() + "]";
            infoLog("Message failed - " + msg);
        }
        protocol.getLogger().log(Level.SEVERE, "Unexpected message: " + messageEntry.getContent());
        return MessageResult.createFailed(messageEntry);
    }

    /**
     * Checks if the given MessageEntry contains the corresponding MessageTag
     *
     * @param messageEntry the given messageEntry
     * @param messageTag   the tag to check
     * @return true if this is the message, false otherwise
     */
    protected boolean isItThisMessage(MessageEntry messageEntry, String messageTag) {
        return messageEntry.getContent().contains(messageTag);
    }

    private MessageResult writeActivityCalendar(MessageEntry messageEntry) throws IOException {
        try {
            ActivityCalendarController activityCalendarController = new ACE6000ActivityCalendarController(protocol);
            activityCalendarController.parseContent(messageEntry.getContent());
            activityCalendarController.writeCalendarName("");
            activityCalendarController.writeCalendar();
            protocol.getLogger().log(Level.INFO, "Activity calendar was successfully written");
            return MessageResult.createSuccess(messageEntry);
        } catch (DataAccessResultException e) {
            protocol.getLogger().severe("Writing of the activity calendar failed: " + e.getMessage());
            return MessageResult.createFailed(messageEntry, e.getMessage());   //Meter did not accept the specified code table, set message to failed
        }
    }

    private MessageResult writeSpecialDays(MessageEntry messageEntry) throws IOException {
        try {
            ActivityCalendarController activityCalendarController = new DLMSActivityCalendarController(protocol.getCosemObjectFactory(), protocol.getTimeZone());
            activityCalendarController.parseContent(messageEntry.getContent());
            activityCalendarController.writeSpecialDaysTable();
            protocol.getLogger().log(Level.INFO, "Special days were successfully written");
            return MessageResult.createSuccess(messageEntry);
        } catch (DataAccessResultException e) {
            protocol.getLogger().severe("Writing of the special days table failed: " + e.getMessage());
            return MessageResult.createFailed(messageEntry, e.getMessage());   //Meter did not accept the specified code table, set message to failed
        }
    }

    private MessageResult write12LinesInActivityCalendar(MessageEntry messageEntry) throws IOException {
        try {
            ACE6000ActivityCalendarController activityCalendarController = new ACE6000ActivityCalendarController(protocol);
            activityCalendarController.parseContent(messageEntry.getContent());
            activityCalendarController.writeCalendarName("");
            if (currentLineCalendarNumber == 3) {
                currentLineCalendarNumber = 0;
            }
            activityCalendarController.write12LinesCalendar(currentLineCalendarNumber);
            currentLineCalendarNumber++;
            protocol.getLogger().log(Level.INFO, "Activity calendar was successfully written");
            return MessageResult.createSuccess(messageEntry);
        } catch (DataAccessResultException e) {
            protocol.getLogger().severe("Writing of the activity calendar failed: " + e.getMessage());
            return MessageResult.createFailed(messageEntry, e.getMessage());   //Meter did not accept the specified code table, set message to failed
        }
    }

    @Override
    public List getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList<>();
        categories.add(ProtocolMessageCategories.getDemandResetCategory());

        MessageCategorySpec catBilling = new MessageCategorySpec("Billing");
        MessageSpec msgSpec = addBasicMsg(BILLINGRESET_DISPLAY, BILLINGRESET, false);
        catBilling.addMessageSpec(msgSpec);
        categories.add(catBilling);

        MessageCategorySpec categorySelection12LinesTOU = new MessageCategorySpec(SELECTION_OF_12_LINES_IN_TOU_TABLE_DISPLAY);
        MessageSpec msgSpecSelection12LinesTOU = addBasicMsg(SELECTION_OF_12_LINES_IN_TOU_TABLE_DISPLAY, SELECTION_OF_12_LINES_IN_TOU_TABLE, false);
        catBilling.addMessageSpec(msgSpecSelection12LinesTOU);
        categories.add(categorySelection12LinesTOU);

        MessageCategorySpec voltageAndCurrentRatios = new MessageCategorySpec(WRITE_VOLTAGE_AND_CURRENT_RATIOS);
        MessageSpec writeVoltageAndCurrentParameters = addBasicMsg(WRITE_VOLTAGE_AND_CURRENT_RATIOS, WRITE_VOLTAGE_AND_CURRENT_RATIOS, false);
        voltageAndCurrentRatios.addMessageSpec( writeVoltageAndCurrentParameters );
        categories.add(voltageAndCurrentRatios);

        return categories;
    }

    private MessageSpec addBasicMsg(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    private void displayGeneralParameters(MessageEntry messageEntry) throws IOException {
        Structure structure = new Structure();
        structure.addDataType(new BooleanObject(Boolean.parseBoolean(getContentBetweenTags(DeviceMessageConstants.DisplayLeadingZero, messageEntry.getContent()))));
        structure.addDataType(new BooleanObject(Boolean.parseBoolean(getContentBetweenTags(DeviceMessageConstants.DisplayBacklight, messageEntry.getContent()))));
        structure.addDataType(new BooleanObject(Boolean.parseBoolean(getContentBetweenTags(DeviceMessageConstants.DisplayEOB_Confirm, messageEntry.getContent()))));
        structure.addDataType(new VisibleString(getContentBetweenTags(DeviceMessageConstants.DisplayString_EOB_Confirm, messageEntry.getContent()), 8));
        structure.addDataType(new BooleanObject(Boolean.parseBoolean(getContentBetweenTags(DeviceMessageConstants.DisplaySeparators_Display, messageEntry.getContent()))));
        structure.addDataType(new BooleanObject(Boolean.parseBoolean(getContentBetweenTags(DeviceMessageConstants.DisplayTime_Format, messageEntry.getContent()))));
        structure.addDataType(new Unsigned8(Integer.parseInt(getContentBetweenTags(DeviceMessageConstants.DisplayDate_Format, messageEntry.getContent()))));
        structure.addDataType(new Unsigned8(Integer.parseInt(getContentBetweenTags(DeviceMessageConstants.DisplayTimeOut_For_Set_Mode, messageEntry.getContent()))));
        structure.addDataType(new Unsigned8(Integer.parseInt(getContentBetweenTags(DeviceMessageConstants.Display_On_TimeOut, messageEntry.getContent()))));
        structure.addDataType(new Unsigned8(Integer.parseInt(getContentBetweenTags(DeviceMessageConstants.Display_Off_TimeOut, messageEntry.getContent()))));
        structure.addDataType(new Unsigned8(Integer.parseInt(getContentBetweenTags(DeviceMessageConstants.DisplayNb_DisplayedHisto_Sets_Normal_Mode, messageEntry.getContent()))));
        structure.addDataType(new BooleanObject(Boolean.parseBoolean(getContentBetweenTags(DeviceMessageConstants.DisplayExistence_Of_EndOfText, messageEntry.getContent()))));
        structure.addDataType(new VisibleString(getContentBetweenTags(DeviceMessageConstants.DisplayEndOfTextString, messageEntry.getContent()), 8));
        structure.addDataType(new Unsigned8(Integer.parseInt(getContentBetweenTags(DeviceMessageConstants.DisplayNb_DisplayedHisto_Sets_Altmode1, messageEntry.getContent()))));
        structure.addDataType(new Unsigned8(Integer.parseInt(getContentBetweenTags(DeviceMessageConstants.DisplayNb_DisplayedHisto_Sets_Altmode2, messageEntry.getContent()))));
        structure.addDataType(new Unsigned8(Integer.parseInt(getContentBetweenTags(DeviceMessageConstants.DisplayTimeout_For_AltMode, messageEntry.getContent()))));
        structure.addDataType(new BooleanObject(Boolean.parseBoolean(getContentBetweenTags(DeviceMessageConstants.DisplayAutorized_EOB, messageEntry.getContent()))));
        structure.addDataType(new Unsigned8(Integer.parseInt(getContentBetweenTags(DeviceMessageConstants.DisplayTimeout_Load_Profile, messageEntry.getContent()))));
        structure.addDataType(new BooleanObject(Boolean.parseBoolean(getContentBetweenTags(DeviceMessageConstants.Displaying_Of_LPMenus, messageEntry.getContent()))));
        structure.addDataType(new BooleanObject(Boolean.parseBoolean(getContentBetweenTags(DeviceMessageConstants.Display_ButtonEmulation_By_Optical_Head, messageEntry.getContent()))));
        Data data = protocol.getCosemObjectFactory().getData(DisplayGeneralParameters);
        data.setValueAttr(structure);
    }

    private void displayReadoutTableParameters(MessageEntry messageEntry) throws IOException {
        Structure structure = new Structure();
        structure.addDataType(new Unsigned16(Integer.parseInt(getContentBetweenTags(DeviceMessageConstants.DisplayInternal_Identifier, messageEntry.getContent()))));
        structure.addDataType(new BitString(Integer.parseInt(getContentBetweenTags(DeviceMessageConstants.DisplaySequence_Indicator, messageEntry.getContent())), 8));
        structure.addDataType(new VisibleString(getContentBetweenTags(DeviceMessageConstants.DisplayIdentification_Code, messageEntry.getContent()), 5));
        structure.addDataType(new Unsigned8(Integer.parseInt(getContentBetweenTags(DeviceMessageConstants.DisplayScaler, messageEntry.getContent()))));
        structure.addDataType(new Unsigned8(Integer.parseInt(getContentBetweenTags(DeviceMessageConstants.DisplayNumber_Of_Decimal, messageEntry.getContent()))));
        structure.addDataType(new Unsigned8(Integer.parseInt(getContentBetweenTags(DeviceMessageConstants.DisplayNumber_Of_Display_Historical_Data, messageEntry.getContent()))));
        structure.addDataType(new Unsigned8(Integer.parseInt(getContentBetweenTags(DeviceMessageConstants.DisplayNumber_Of_Displayable_Digit, messageEntry.getContent()))));
        Array array = new Array();
        array.addDataType(structure);
        Data data = protocol.getCosemObjectFactory().getData(DisplayReadOutTableParameters);
        data.setValueAttr(array);
    }

    @Override
    public String writeMessage(Message msg) {
        return msg.write(this);
    }



    @Override
    public String writeTag(MessageTag msgTag) {
        if (msgTag.getName().equalsIgnoreCase(RtuMessageConstant.TOU_ACTIVITY_CAL) || msgTag.getName().equalsIgnoreCase(RtuMessageConstant.TOU_SPECIAL_DAYS)) {
            StringBuilder builder = new StringBuilder();

            // a. Opening tag
            builder.append("<");
            builder.append(msgTag.getName());
            builder.append(">");

            String name = "";
            String activationDate = "1";
            int codeId = 0;

            // b. Attributes
            for (Object o1 : msgTag.getAttributes()) {
                MessageAttribute att = (MessageAttribute) o1;
                if (RtuMessageConstant.TOU_ACTIVITY_NAME.equalsIgnoreCase(att.getSpec().getName())) {
                    if (att.getValue() != null) {
                        name = att.getValue();
                    }
                } else if (RtuMessageConstant.TOU_ACTIVITY_DATE.equalsIgnoreCase(att.getSpec().getName())) {
                    if (att.getValue() != null) {
                        activationDate = att.getValue();
                    }
                } else if (RtuMessageConstant.TOU_ACTIVITY_CODE_TABLE.equalsIgnoreCase(att.getSpec().getName())) {
                    if (att.getValue() != null) {
                        codeId = Integer.valueOf(att.getValue());
                    }
                }
            }

            Date actDate = new Date(Long.valueOf(activationDate));
            if (codeId > 0) {
                try {
                    String xmlContent = new CodeTableXmlParsing(tariffCalendarFinder, tariffCalendarExtractor).parseActivityCalendarAndSpecialDayTable(String.valueOf(codeId), Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime().before(actDate) ? actDate.getTime() : 1, name);
                    addChildTag(builder, RAW_CONTENT, ProtocolTools.compress(xmlContent));
                } catch (ParserConfigurationException e) {
                    protocol.getLogger().severe(e.getMessage());
                } catch (IOException e) {
                    protocol.getLogger().severe(e.getMessage());
                }
            }

            // d. Closing tag
            builder.append("</");
            builder.append(msgTag.getName());
            builder.append(">");
            return builder.toString();
        } else {
            StringBuilder buf = new StringBuilder();

            // a. Opening tag
            buf.append("<");
            buf.append(msgTag.getName());

            // b. Attributes
            for (Object o1 : msgTag.getAttributes()) {
                MessageAttribute att = (MessageAttribute) o1;
                if (att.getValue() == null || att.getValue().length() == 0) {
                    continue;
                }
                buf.append(" ").append(att.getSpec().getName());
                buf.append("=").append('"').append(att.getValue()).append('"');
            }
            buf.append(">");

            // c. sub elements
            for (Object o : msgTag.getSubElements()) {
                MessageElement elt = (MessageElement) o;
                if (elt.isTag()) {
                    buf.append(writeTag((MessageTag) elt));
                } else if (elt.isValue()) {
                    String value = writeValue((MessageValue) elt);
                    if (value == null || value.length() == 0) {
                        return "";
                    }
                    buf.append(value);
                }
            }

            // d. Closing tag
            buf.append("</");
            buf.append(msgTag.getName());
            buf.append(">");

            return buf.toString();
        }
    }

    /**
     * Adds a child tag to the given {@link StringBuffer}.
     *
     * @param buf     The string builder to whose contents the child tag needs to be added.
     * @param tagName The name of the child tag to add.
     * @param value   The contents (value) of the tag.
     */
    protected void addChildTag(StringBuilder buf, String tagName, Object value) {
        buf.append(System.getProperty("line.separator"));
        buf.append("<");
        buf.append(tagName);
        buf.append(">");
        buf.append(value);
        buf.append("</");
        buf.append(tagName);
        buf.append(">");
    }

    private void updateTimeOfUse(MessageEntry messageEntry) throws IOException, SAXException {
        final TimeOfUseMessageBuilder builder = new TimeOfUseMessageBuilder(messageFileFinder, deviceMessageFileExtractor);
        ActivityCalendarController activityCalendarController = new ACE6000ActivityCalendarController(this.protocol);
        builder.initFromXml(messageEntry.getContent());
        if (!builder.getCalendarId().isEmpty() && Integer.parseInt(builder.getCalendarId()) > 0) { // codeTable implementation
            infoLog("Parsing the content of the CodeTable.");
            activityCalendarController.parseContent(messageEntry.getContent());
            infoLog("Setting the new Passive Calendar Name.");
            activityCalendarController.writeCalendarName("");
            infoLog("Sending out the new Passive Calendar objects.");
            activityCalendarController.writeCalendar();
        }
    }

    private void setWriteVoltageAndCurrentRatios(String ratiosXml) throws IOException {
        final Integer currentNumerator   = Integer.parseInt(
                getContentBetweenTags(CurrentRatioNumeratorAttributeName, ratiosXml) );
        final Integer currentDenominator = Integer.parseInt(
                getContentBetweenTags(CurrentRatioDenominatorAttributeName, ratiosXml) );
        final Integer voltageNumerator   = Integer.parseInt(
                getContentBetweenTags(VoltageRatioNumeratorAttributeName, ratiosXml) );
        final Integer voltageDenominator = Integer.parseInt(
                getContentBetweenTags(VoltageRatioDenominatorAttributeName, ratiosXml) );

        Structure structure = new Structure();
        structure.addDataType( new Unsigned16(currentNumerator) );   // current numerator
        structure.addDataType( new Unsigned8(currentDenominator) );  // current denominator
        structure.addDataType( new Unsigned32(voltageNumerator) );   // voltage numerator
        structure.addDataType( new Unsigned16(voltageDenominator) ); // voltage denominator

        Data data = protocol.getCosemObjectFactory().getData(SecondaryMetrologyInstallationParameters);
        data.setValueAttr(structure);
    }

    /**
     * Log the given message to the logger with the INFO level
     *
     * @param messageToLog
     */
    private void infoLog(String messageToLog) {
        this.protocol.getLogger().info(messageToLog);
    }

    private static String getContentBetweenTags(String tagName, String content) throws ProtocolException {
        final Pattern pattern = Pattern.compile("<" + tagName + ">(.+?)</" + tagName + ">", Pattern.DOTALL);
        final Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new ProtocolException("Error while trying to extract tag: " + tagName + ", from content: " + content);
    }
}
