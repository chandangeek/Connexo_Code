package com.energyict.protocolimpl.dlms.idis;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.*;
import com.energyict.genericprotocolimpl.common.messages.GenericMessaging;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.base.ActivityCalendarController;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.messages.codetableparsing.CodeTableXmlParsing;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.apache.axis.encoding.Base64;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 20/09/11
 * Time: 17:22
 */
public class IDISMessageHandler extends GenericMessaging implements MessageProtocol {

    private static final ObisCode RELAY_CONTROL_OBISCODE = ObisCode.fromString("0.0.96.3.10.255");
    private static final ObisCode TIMED_CONNECTOR_ACTION_OBISCODE = ObisCode.fromString("0.0.15.0.1.255");
    private static final ObisCode DISCONNECTOR_SCRIPT_OBISCODE = ObisCode.fromString("0.0.10.0.106.255");
    public static final String RAW_CONTENT = "RawContent";
    public static final String INCLUDED_USERFILE_TAG = "IncludedFile";
    protected IDIS idis;


    public IDISMessageHandler(IDIS idis) {
        this.idis = idis;
    }

    public void applyMessages(List messageEntries) throws IOException {
    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        try {
            if (messageEntry.getContent().contains("<RemoteDisconnect")) {
                return remoteDisconnect(messageEntry);
            } else if (messageEntry.getContent().contains("<RemoteConnect")) {
                return remoteConnect(messageEntry);
            } else if (messageEntry.getContent().contains("<OpenRelay")) {
                return openRelay(messageEntry);
            } else if (messageEntry.getContent().contains("<CloseRelay")) {
                return closeRelay(messageEntry);
            } else if (messageEntry.getContent().contains("<TimedReconnect")) {
                return timedAction(messageEntry, 2);
            } else if (messageEntry.getContent().contains("<TimedDisconnect")) {
                return timedAction(messageEntry, 1);
            } else if (messageEntry.getContent().contains("<FirmwareUpdate")) {
                return firmwareUpgrade(messageEntry);
            } else if (messageEntry.getContent().contains("<LoadControlledConnect")) {
                return loadControlledConnect(messageEntry);
            } else if (messageEntry.getContent().contains("<SuperVision")) {
                return superVision(messageEntry);
            } else if (messageEntry.getContent().contains(RtuMessageConstant.TOU_ACTIVITY_CAL)) {
                return writeActivityCalendar(messageEntry);
            } else if (messageEntry.getContent().contains(RtuMessageConstant.TOU_SPECIAL_DAYS)) {
                return writeSpecialDays(messageEntry);
            }
        } catch (NumberFormatException e) {
            idis.getLogger().log(Level.SEVERE, "Error executing message: " + e.getMessage());
            return MessageResult.createFailed(messageEntry);
        } catch (InterruptedException e) {
            throw new IOException(e.getMessage());
        }
        idis.getLogger().log(Level.SEVERE, "Unexpected message: " + messageEntry.getContent());
        return MessageResult.createFailed(messageEntry);
    }

    private MessageResult writeActivityCalendar(MessageEntry messageEntry) throws IOException {
        ActivityCalendarController activityCalendarController = new IDISActivityCalendarController(idis);
        activityCalendarController.parseContent(messageEntry.getContent());
        activityCalendarController.writeCalendarName("");
        activityCalendarController.writeCalendar();
        idis.getLogger().log(Level.INFO, "Activity calendar was successfully written");
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult writeSpecialDays(MessageEntry messageEntry) throws IOException {
        ActivityCalendarController activityCalendarController = new IDISActivityCalendarController(idis);
        activityCalendarController.parseContent(messageEntry.getContent());
        activityCalendarController.writeSpecialDaysTable();
        idis.getLogger().log(Level.INFO, "Special days were successfully written");
        return MessageResult.createSuccess(messageEntry);
    }

    protected MessageResult remoteDisconnect(MessageEntry messageEntry) throws IOException {
        idis.getCosemObjectFactory().getDisconnector().remoteDisconnect();
        idis.getLogger().log(Level.INFO, "Remote disconnect message was successful");
        return MessageResult.createSuccess(messageEntry);
    }

    protected MessageResult remoteConnect(MessageEntry messageEntry) throws IOException {
        idis.getCosemObjectFactory().getDisconnector().remoteReconnect();
        idis.getLogger().log(Level.INFO, "Remote connect message was successful");
        return MessageResult.createSuccess(messageEntry);
    }

    protected MessageResult openRelay(MessageEntry messageEntry) throws IOException {
        String[] parts = messageEntry.getContent().split("=");
        int relayControlNumber = Integer.parseInt(parts[1].substring(1).split("\"")[0]);
        ObisCode obisCode = RELAY_CONTROL_OBISCODE;
        obisCode = ProtocolTools.setObisCodeField(obisCode, 1, (byte) relayControlNumber);
        idis.getCosemObjectFactory().getDisconnector(obisCode).remoteDisconnect();
        idis.getLogger().log(Level.INFO, "Open relay message was successful");
        return MessageResult.createSuccess(messageEntry);
    }

    protected MessageResult closeRelay(MessageEntry messageEntry) throws IOException {
        String[] parts = messageEntry.getContent().split("=");
        int relayControlNumber = Integer.parseInt(parts[1].substring(1).split("\"")[0]);
        ObisCode obisCode = RELAY_CONTROL_OBISCODE;
        obisCode = ProtocolTools.setObisCodeField(obisCode, 1, (byte) relayControlNumber);
        idis.getCosemObjectFactory().getDisconnector(obisCode).remoteReconnect();
        idis.getLogger().log(Level.INFO, "Close relay message was successful");
        return MessageResult.createSuccess(messageEntry);
    }

    private String getIncludedContent(final String content) {
        int begin = content.indexOf(INCLUDED_USERFILE_TAG) + INCLUDED_USERFILE_TAG.length() + 1;
        int end = content.indexOf(INCLUDED_USERFILE_TAG, begin) - 2;
        return content.substring(begin, end);
    }

    protected MessageResult firmwareUpgrade(MessageEntry messageEntry) throws IOException, InterruptedException {
        String imageData = getIncludedContent(messageEntry.getContent());
        byte[] binaryImage = Base64.decode(imageData);
        String firmwareIdentifier;
        int length = binaryImage[0];
        firmwareIdentifier = new String(ProtocolTools.getSubArray(binaryImage, 1, 1 + length));   //The image_identifier is included in the header of the bin file

        ImageTransfer imageTransfer = idis.getCosemObjectFactory().getImageTransfer();
        imageTransfer.upgrade(binaryImage, false, firmwareIdentifier, true);
        imageTransfer.imageActivation();
        idis.getLogger().log(Level.INFO, "Firmware upgrade message was successful");
        return MessageResult.createSuccess(messageEntry);
    }

    protected MessageResult timedAction(MessageEntry messageEntry, int action) throws IOException {
        String[] parts = messageEntry.getContent().split("=");
        String dateString = parts[1].substring(1).split("\"")[0];
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date date;
        try {
            date = formatter.parse(dateString);
        } catch (ParseException e) {
            idis.getLogger().log(Level.SEVERE, "Error parsing the given date: " + e.getMessage());
            return MessageResult.createFailed(messageEntry);
        }
        SingleActionSchedule singleActionSchedule = idis.getCosemObjectFactory().getSingleActionSchedule(TIMED_CONNECTOR_ACTION_OBISCODE);

        Structure scriptStruct = new Structure();
        scriptStruct.addDataType(new OctetString(DISCONNECTOR_SCRIPT_OBISCODE.getLN()));
        scriptStruct.addDataType(new Unsigned16(action));     // 1 = disconnect, 2 = connect

        singleActionSchedule.writeExecutedScript(scriptStruct);
        singleActionSchedule.writeExecutionTime(convertDateToDLMSArray(date));
        idis.getLogger().log(Level.INFO, "Timed re/connect message was successful");
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult loadControlledConnect(MessageEntry messageEntry) throws IOException {
        String[] parts = messageEntry.getContent().split("=");
        int monitoredValue = Integer.parseInt(parts[1].substring(1).split("\"")[0]);
        int normalThreshold = Integer.parseInt(parts[2].substring(1).split("\"")[0]);
        int emergencyThreshold = Integer.parseInt(parts[3].substring(1).split("\"")[0]);
        int overDuration = Integer.parseInt(parts[4].substring(1).split("\"")[0]);
        int underDuration = Integer.parseInt(parts[5].substring(1).split("\"")[0]);
        int emergencyProfileId = Integer.parseInt(parts[6].substring(1).split("\"")[0]);
        String emergencyActivationTime = parts[7].substring(1).split("\"")[0];

        Date date;
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        try {
            date = formatter.parse(emergencyActivationTime);
        } catch (ParseException e) {
            idis.getLogger().log(Level.SEVERE, "Error parsing the given date: " + e.getMessage());
            return MessageResult.createFailed(messageEntry);
        }

        int emergencyDuration = Integer.parseInt(parts[8].substring(1).split("\"")[0]);
        String[] profile_id_list = parts[9].substring(1).split("\"")[0].split(",");
        int actionUnderThreshold = Integer.parseInt(parts[10].substring(1).split("\"")[0]);

        Limiter limiter = idis.getCosemObjectFactory().getLimiter();
        setMonitoredValue(limiter, monitoredValue);
        writeNormalThreshold(monitoredValue, normalThreshold, limiter);
        writeEmergencyThreshold(monitoredValue, emergencyThreshold, limiter);
        limiter.writeMinOverThresholdDuration(new Unsigned32(overDuration));
        limiter.writeMinUnderThresholdDuration(new Unsigned32(underDuration));
        writeEmergencyProfile(emergencyProfileId, date, emergencyDuration, limiter);

        Array groupIdList = new Array();
        for (String id : profile_id_list) {
            try {
                groupIdList.addDataType(new Unsigned16(Integer.parseInt(id)));
            } catch (NumberFormatException e) {
                idis.getLogger().log(Level.INFO, "Error parsing the profile id list: " + e.getMessage());
                return MessageResult.createFailed(messageEntry);
            }
        }
        limiter.writeEmergencyProfileGroupIdList(groupIdList);

        writeActions(actionUnderThreshold, limiter);

        idis.getLogger().log(Level.INFO, "Load controlled connect message was successful");
        return MessageResult.createSuccess(messageEntry);
    }

    private void writeEmergencyProfile(int emergencyProfileId, Date date, int emergencyDuration, Limiter limiter) throws IOException {
        Limiter.EmergencyProfile emergencyProfile = limiter.new EmergencyProfile();
        emergencyProfile.addDataType(new Unsigned16(emergencyProfileId));
        emergencyProfile.addDataType(new OctetString(ProtocolTools.getSubArray(new AXDRDateTime(date).getBEREncodedByteArray(), 2)));
        emergencyProfile.addDataType(new Unsigned32(emergencyDuration));
        limiter.writeEmergencyProfile(emergencyProfile);
    }

    private MessageResult superVision(MessageEntry messageEntry) throws IOException {
        String[] parts = messageEntry.getContent().split("=");
        int phase = Integer.parseInt(parts[1].substring(1).split("\"")[0]);
        int threshold = Integer.parseInt(parts[2].substring(1).split("\"")[0]);
        ObisCode obisCode;
        switch (phase) {
            case 1:
                obisCode = ObisCode.fromString("1.0.31.4.0.255");
                break;
            case 2:
                obisCode = ObisCode.fromString("1.0.51.4.0.255");
                break;
            case 3:
                obisCode = ObisCode.fromString("1.0.71.4.0.255");
                break;
            default:
                idis.getLogger().log(Level.SEVERE, "Unexpected phase number, should be 1, 2 or 3");
                return MessageResult.createFailed(messageEntry);
        }
        RegisterMonitor registerMonitor = idis.getCosemObjectFactory().getRegisterMonitor(obisCode);
        Array thresholds = new Array();
        thresholds.addDataType(new Unsigned32(threshold));
        registerMonitor.writeThresholds(thresholds);

        idis.getLogger().log(Level.INFO, "Phase supervision message was successful");
        return MessageResult.createSuccess(messageEntry);
    }

    private void writeActions(int actionUnderThreshold, Limiter limiter) throws IOException {
        Limiter.ActionItem action1 = limiter.new ActionItem(OctetString.fromByteArray(DISCONNECTOR_SCRIPT_OBISCODE.getLN(), 6), new Unsigned16(1));
        Limiter.ActionItem action2 = limiter.new ActionItem(OctetString.fromByteArray(DISCONNECTOR_SCRIPT_OBISCODE.getLN(), 6), new Unsigned16(actionUnderThreshold));

        Limiter.ActionType actions = limiter.new ActionType(action1, action2);
        limiter.writeActions(actions);
    }

    private void writeNormalThreshold(int monitoredValue, int activeThreshold, Limiter limiter) throws IOException {
        if (monitoredValue == 1) {
            limiter.writeThresholdNormal(new Unsigned16(activeThreshold));
        } else {
            limiter.writeThresholdNormal(new Unsigned32(activeThreshold));
        }
    }

    private void writeEmergencyThreshold(int monitoredValue, int activeThreshold, Limiter limiter) throws IOException {
        if (monitoredValue == 1) {
            limiter.writeThresholdEmergency(new Unsigned16(activeThreshold));
        } else {
            limiter.writeThresholdEmergency(new Unsigned32(activeThreshold));
        }
    }

    private void setMonitoredValue(Limiter limiter, int monitoredValue) throws IOException {
        byte[] monitoredAttribute = new byte[]{1, 0, 15, 24, 0, (byte) 255};
        int classId = DLMSClassId.DEMAND_REGISTER.getClassId();
        if (monitoredValue == 1) {
            monitoredAttribute = new byte[]{1, 0, 90, 7, 0, (byte) 255};
            classId = DLMSClassId.REGISTER.getClassId();
        }
        if (monitoredValue == 2) {
            monitoredAttribute = new byte[]{1, 0, 1, 24, 0, (byte) 255};
        }

        Limiter.ValueDefinitionType vdt = limiter.new ValueDefinitionType();
        vdt.addDataType(new Unsigned16(classId));
        OctetString os = new OctetString(monitoredAttribute);
        vdt.addDataType(os);
        vdt.addDataType(new Integer8(2));
        limiter.writeMonitoredValue(vdt);
    }


    private byte getDLMSDayOfWeek(Calendar cal) {
        int dow = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (dow == 0) {
            dow = 7;
        }
        return (byte) dow;
    }

    protected Array convertDateToDLMSArray(Date executionDate) throws IOException {
        Calendar cal = Calendar.getInstance(idis.getTimeZone());
        cal.setTime(executionDate);
        byte[] dateBytes = new byte[5];
        dateBytes[0] = (byte) ((cal.get(Calendar.YEAR) >> 8) & 0xFF);
        dateBytes[1] = (byte) (cal.get(Calendar.YEAR) & 0xFF);
        dateBytes[2] = (byte) ((cal.get(Calendar.MONTH) & 0xFF) + 1);
        dateBytes[3] = (byte) (cal.get(Calendar.DAY_OF_MONTH) & 0xFF);
        dateBytes[4] = getDLMSDayOfWeek(cal);
        OctetString date = new OctetString(dateBytes);
        byte[] timeBytes = new byte[4];
        timeBytes[0] = (byte) cal.get(Calendar.HOUR_OF_DAY);
        timeBytes[1] = (byte) cal.get(Calendar.MINUTE);
        timeBytes[2] = (byte) 0x00;
        timeBytes[3] = (byte) 0x00;
        OctetString time = new OctetString(timeBytes);

        Array dateTimeArray = new Array();
        Structure strDateTime = new Structure();
        strDateTime.addDataType(time);
        strDateTime.addDataType(date);
        dateTimeArray.addDataType(strDateTime);
        return dateTimeArray;
    }

    public List getMessageCategories() {
        List<MessageCategorySpec> theCategories = new ArrayList<MessageCategorySpec>();             //TODO configure emergency profile on limiter ??
        MessageCategorySpec cat1 = new MessageCategorySpec("Disconnection and reconnection");
        cat1.addMessageSpec(addBasicMsg("Remote controlled disconnection", "RemoteDisconnect", false));
        cat1.addMessageSpec(addBasicMsg("Remote controlled reconnection", "RemoteConnect", false));
        cat1.addMessageSpec(addBasicMsgWithAttributes("Open relay", "OpenRelay", false, "Relay number (1 or 2)"));
        cat1.addMessageSpec(addBasicMsgWithAttributes("Close relay", "CloseRelay", false, "Relay number (1 or 2)"));
        cat1.addMessageSpec(addBasicMsgWithAttributes("Load control configuration (limiter)", "LoadControlledConnect", false, "Monitored value (1: Total inst. current, 2: Avg A+ (sliding demand), 3: Avg total A (sliding demand))", "Normal threshold", "Emergency threshold", "Minimal over threshold duration (seconds)", "Minimal under threshold duration (seconds)", "Emergency profile ID", "Emergency activation time (dd/mm/yyyy hh:mm:ss)", "Emergency duration (seconds)", "Emergency profile group id list (comma separated, e.g. 1,2,3)", "Action when under threshold (0: nothing, 2: reconnect)"));
        cat1.addMessageSpec(addBasicMsgWithAttributes("Supervision monitor", "SuperVision", false, "Phase (1, 2 or 3)", "Threshold (ampere)"));
        cat1.addMessageSpec(addBasicMsgWithAttributes("Time controlled reconnection", "TimedReconnect", false, "Date (dd/mm/yyyy hh:mm)"));
        cat1.addMessageSpec(addBasicMsgWithAttributes("Time controlled disconnection", "TimedDisconnect", false, "Date (dd/mm/yyyy hh:mm)"));

        theCategories.add(getActivityCalendarCategory());
        theCategories.add(cat1);

        return theCategories;
    }

    protected MessageSpec addBasicMsg(final String keyId, final String tagName, final boolean advanced) {
        final MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        final MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    protected MessageSpec addBasicMsgWithAttributes(final String keyId, final String tagName, final boolean advanced, String... attr) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        for (String attribute : attr) {
            tagSpec.add(new MessageAttributeSpec(attribute, true));
        }
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" "); //Disable this field
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

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
                    String xmlContent = CodeTableXmlParsing.parseActivityCalendarAndSpecialDayTable(codeId, Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime().before(actDate) ? actDate.getTime() : 1, name);
                    addChildTag(builder, RAW_CONTENT, ProtocolTools.compress(xmlContent));
                } catch (ParserConfigurationException e) {
                    idis.getLogger().severe(e.getMessage());
                } catch (IOException e) {
                    idis.getLogger().severe(e.getMessage());
                }
            }

            // d. Closing tag
            builder.append("</");
            builder.append(msgTag.getName());
            builder.append(">");
            return builder.toString();
        } else {
            StringBuffer buf = new StringBuffer();

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
}