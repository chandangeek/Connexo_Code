package com.energyict.protocolimplv2.dlms.idis.am500.messages;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.GenericInvoke;
import com.energyict.dlms.cosem.GenericWrite;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.cosem.Limiter;
import com.energyict.dlms.cosem.MBusClient;
import com.energyict.dlms.cosem.RegisterMonitor;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.dlms.cosem.attributes.MbusClientAttributes;
import com.energyict.mdc.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineDeviceMessageAttribute;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocolimpl.base.ActivityCalendarController;
import com.energyict.protocolimpl.dlms.common.DLMSActivityCalendarController;
import com.energyict.protocolimpl.dlms.idis.xml.XMLParser;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.mbus.IDISMBusMessageExecutor;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.AlarmConfigurationMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.GeneralDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadBalanceDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.messages.PLCConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagWriter;
import com.energyict.protocolimplv2.messages.enums.LoadControlActions;
import com.energyict.protocolimplv2.messages.enums.MonitoredValue;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 6/01/2015 - 15:31
 */
public class IDISMessageExecutor extends AbstractMessageExecutor {

    protected static final ObisCode MBUS_CLIENT_OBISCODE = ObisCode.fromString("0.1.24.1.0.255");
    private static final ObisCode RELAY_CONTROL_OBISCODE = ObisCode.fromString("0.0.96.3.10.255");
    private static final ObisCode TIMED_CONNECTOR_ACTION_OBISCODE = ObisCode.fromString("0.0.15.0.1.255");
    private static final ObisCode DISCONNECTOR_SCRIPT_OBISCODE = ObisCode.fromString("0.0.10.0.106.255");
    private static final ObisCode ERROR_BITS_OBISCODE = ObisCode.fromString("0.0.97.97.0.255");
    private static final ObisCode ALARM_BITS_OBISCODE = ObisCode.fromString("0.0.97.98.0.255");
    private static final ObisCode ALARM_FILTER_OBISCODE = ObisCode.fromString("0.0.97.98.10.255");
    private static final int MAX_MBUS_SLAVES = 4;
    private static final long SUPERVISION_MAXIMUM_THRESHOLD_VALUE = 0xFFFFFFFFl;

    private IDISMBusMessageExecutor idisMBusMessageExecutor = null;

    public IDISMessageExecutor(AbstractDlmsProtocol protocol) {
        super(protocol);
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = MdcManager.getCollectedDataFactory().createCollectedMessageList(pendingMessages);

        for (OfflineDeviceMessage pendingMessage : pendingMessages) {

            int mBusChannelId = getProtocol().getPhysicalAddressFromSerialNumber(pendingMessage.getDeviceSerialNumber());

            CollectedMessage collectedMessage;
            if (mBusChannelId > 0) {
                collectedMessage = getIdisMBusMessageExecutor().executePendingMessages(Arrays.asList(pendingMessage)).getCollectedMessages().get(0);
            } else {
                collectedMessage = createCollectedMessage(pendingMessage);
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);   //Optimistic
                try {
                    collectedMessage = executeMessage(pendingMessage, collectedMessage);
                } catch (IOException e) {
                    if (IOExceptionHandler.isUnexpectedResponse(e, getProtocol().getDlmsSession())) {
                        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                        collectedMessage.setDeviceProtocolInformation(e.getMessage());
                        collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                    }   //Else: throw communication exception
                } catch (IndexOutOfBoundsException | NumberFormatException e) {
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setDeviceProtocolInformation(e.getMessage());
                    collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                }
            }
            result.addCollectedMessage(collectedMessage);
        }
        return result;
    }

    protected CollectedMessage executeMessage(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        if (pendingMessage.getSpecification().equals(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME)) {
            writeActivityCalendar(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND)) {
            writeSpecialDays(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(AlarmConfigurationMessage.RESET_ALL_ALARM_BITS)) {
            resetAllAlarmBits(ALARM_BITS_OBISCODE);
        } else if (pendingMessage.getSpecification().equals(AlarmConfigurationMessage.RESET_ALL_ERROR_BITS)) {
            resetAllErrorBits(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(AlarmConfigurationMessage.WRITE_ALARM_FILTER)) {
            long filter = new BigDecimal(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue()).longValue();
            writeAlarmFilter(ALARM_FILTER_OBISCODE, filter);
        } else if (pendingMessage.getSpecification().equals(GeneralDeviceMessage.WRITE_FULL_CONFIGURATION)) {
            configurationDownload(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CLOSE_RELAY)) {
            closeRelay(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.OPEN_RELAY)) {
            openRelay(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CONTACTOR_OPEN)) {
            remoteDisconnect();
        } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CONTACTOR_CLOSE)) {
            remoteConnect();
        } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE)) {
            timedAction(pendingMessage, 1);
        } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE)) {
            timedAction(pendingMessage, 2);
        } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE)) {
            setControlMode(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(LoadBalanceDeviceMessage.CONFIGURE_ALL_LOAD_LIMIT_PARAMETERS)) {
            loadControlledConnect(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(LoadBalanceDeviceMessage.CONFIGURE_SUPERVISION_MONITOR)) {
            collectedMessage = superVision(collectedMessage, pendingMessage);
        } else if (pendingMessage.getSpecification().equals(LoadProfileMessage.WRITE_CAPTURE_PERIOD_LP1)) {
            writeCapturePeriod(pendingMessage, 1);
        } else if (pendingMessage.getSpecification().equals(LoadProfileMessage.WRITE_CAPTURE_PERIOD_LP2)) {
            writeCapturePeriod(pendingMessage, 2);
        } else if (pendingMessage.getSpecification().equals(MBusSetupDeviceMessage.Commission)) {
            collectedMessage = commission(pendingMessage, collectedMessage);
        } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetTimeoutNotAddressed)) {
            setTimeoutNotAddressed(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_RESUME_OPTION)) {
            firmwareUpgrade(pendingMessage);
        } else {   //Unsupported message
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.NotSupported, createUnsupportedWarning(pendingMessage));
            collectedMessage.setDeviceProtocolInformation("Message currently not supported by the protocol");
        }
        return collectedMessage;
    }

    private IDISMBusMessageExecutor getIdisMBusMessageExecutor() {
        if (idisMBusMessageExecutor == null) {
            idisMBusMessageExecutor = new IDISMBusMessageExecutor(getProtocol());
        }
        return idisMBusMessageExecutor;
    }

    protected void firmwareUpgrade(OfflineDeviceMessage offlineDeviceMessage) throws IOException {

        OfflineDeviceMessageAttribute imageAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, firmwareUpdateUserFileAttributeName);
        byte[] binaryImage = ProtocolTools.getBytesFromHexString(imageAttribute.getDeviceMessageAttributeValue(), "");
        boolean resume = Boolean.valueOf(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, resumeFirmwareUpdateAttributeName).getDeviceMessageAttributeValue());

        String firmwareIdentifier;
        int length = binaryImage[0];
        firmwareIdentifier = new String(ProtocolTools.getSubArray(binaryImage, 1, 1 + length));   //The image_identifier is included in the header of the bin file

        ImageTransfer imageTransfer = getCosemObjectFactory().getImageTransfer();
        if (resume) {
            int lastTransferredBlockNumber = imageTransfer.readFirstNotTransferedBlockNumber().intValue();
            if (lastTransferredBlockNumber > 0) {
                imageTransfer.setStartIndex(lastTransferredBlockNumber - 1);
            }
        }

        imageTransfer.upgrade(binaryImage, false, firmwareIdentifier, false);
        imageTransfer.imageActivation();
    }

    protected void setTimeoutNotAddressed(OfflineDeviceMessage pendingMessage) throws IOException {
        int timeout = Integer.valueOf(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
        getCosemObjectFactory().getSFSKSyncTimeouts().setTimeoutNotAddressed(timeout);
    }

    private CollectedMessage commission(OfflineDeviceMessage offlineDeviceMessage, CollectedMessage collectedMessage) throws IOException {
        for (int channel = 1; channel <= getMaxMBusSlaves(); channel++) {                     //Check the available 4 (or 6 in case of am130) channels, install the slave meter on a free channel client.
            ObisCode obisCode = ProtocolTools.setObisCodeField(MBUS_CLIENT_OBISCODE, 1, (byte) channel);   //Find the right MBus client object
            try {
                MBusClient mbusClient = getCosemObjectFactory().getMbusClient(obisCode, MbusClientAttributes.VERSION10);
                if (mbusClient.getIdentificationNumber().getValue() == 0) {     //Find a free channel client
                    mbusClient.invoke(1, new Unsigned8(0).getBEREncodedByteArray());
                    return collectedMessage;
                }
            } catch (IOException e) {
                if (IOExceptionHandler.isNotSupportedDataAccessResultException(e)) {
                    continue;
                } else {
                    throw e;
                }
            }
        }

        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
        String msg = "Couldn't commission the new MBus meter, no free channels available.";
        collectedMessage.setFailureInformation(ResultType.ConfigurationError, createMessageFailedIssue(offlineDeviceMessage, msg));
        collectedMessage.setDeviceProtocolInformation(msg);
        return collectedMessage;
    }

    protected int getMaxMBusSlaves() {
        return MAX_MBUS_SLAVES;
    }

    protected void writeCapturePeriod(OfflineDeviceMessage offlineDeviceMessage, int dField) throws IOException {
        long captureTimeInSeconds = Long.valueOf(offlineDeviceMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
        ObisCode lpObisCode = ObisCode.fromString("1.0.99.1.0.255");
        lpObisCode = ProtocolTools.setObisCodeField(lpObisCode, 3, (byte) dField);  //1 or 2 in D-field, selects the LP
        getCosemObjectFactory().getProfileGeneric(lpObisCode).setCapturePeriodAttr(new Unsigned32(captureTimeInSeconds));
    }

    private CollectedMessage superVision(CollectedMessage collectedMessage, OfflineDeviceMessage offlineDeviceMessage) throws IOException {
        int phase = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, phaseAttributeName).getDeviceMessageAttributeValue()).intValue();
        long threshold = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, thresholdInAmpereAttributeName).getDeviceMessageAttributeValue()).longValue();

        if (threshold > SUPERVISION_MAXIMUM_THRESHOLD_VALUE) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            String msg = "Invalid threshold value, should be smaller than 4294967296";
            collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(offlineDeviceMessage, msg));
            collectedMessage.setDeviceProtocolInformation(msg);
            return collectedMessage;
        } else {
            ObisCode obisCode = null;
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
            }
            RegisterMonitor registerMonitor = getCosemObjectFactory().getRegisterMonitor(obisCode);
            Array thresholds = new Array();
            thresholds.addDataType(new Unsigned32(threshold));
            registerMonitor.writeThresholds(thresholds);
            return collectedMessage;
        }
    }

    private void loadControlledConnect(OfflineDeviceMessage offlineDeviceMessage) throws IOException {

        int monitoredValue = MonitoredValue.fromDescription(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, monitoredValueAttributeName).getDeviceMessageAttributeValue());
        long normalThreshold = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, normalThresholdAttributeName).getDeviceMessageAttributeValue()).longValue();
        long emergencyThreshold = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, emergencyThresholdAttributeName).getDeviceMessageAttributeValue()).longValue();
        int overThresholdDuration = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, overThresholdDurationAttributeName).getDeviceMessageAttributeValue());
        int underThresholdDuration = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, underThresholdDurationAttributeName).getDeviceMessageAttributeValue());
        int emergencyProfileId = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, emergencyProfileIdAttributeName).getDeviceMessageAttributeValue());
        Date emergencyProfileActivationDate = new Date(new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, emergencyProfileActivationDateAttributeName).getDeviceMessageAttributeValue()).longValue());
        int emergencyProfileDuration = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, emergencyProfileDurationAttributeName).getDeviceMessageAttributeValue());
        String emergencyProfileGroupIdList = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, emergencyProfileGroupIdListAttributeName).getDeviceMessageAttributeValue();
        int actionWhenUnderThreshold = LoadControlActions.fromDescription(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, actionWhenUnderThresholdAttributeName).getDeviceMessageAttributeValue());

        Limiter limiter = getCosemObjectFactory().getLimiter();
        setMonitoredValue(limiter, monitoredValue);
        writeNormalThreshold(monitoredValue, normalThreshold, limiter);
        writeEmergencyThreshold(monitoredValue, emergencyThreshold, limiter);
        limiter.writeMinOverThresholdDuration(new Unsigned32(overThresholdDuration));
        limiter.writeMinUnderThresholdDuration(new Unsigned32(underThresholdDuration));
        writeEmergencyProfile(emergencyProfileId, emergencyProfileActivationDate, emergencyProfileDuration, limiter);

        Array groupIdList = new Array();
        String[] profile_id_list = emergencyProfileGroupIdList.split(",");
        for (String id : profile_id_list) {
            groupIdList.addDataType(new Unsigned16(Integer.parseInt(id)));
        }
        limiter.writeEmergencyProfileGroupIdList(groupIdList);

        writeActions(actionWhenUnderThreshold, limiter);
    }

    private void writeActions(int actionUnderThreshold, Limiter limiter) throws IOException {
        Limiter.ActionItem action1 = limiter.new ActionItem(OctetString.fromByteArray(DISCONNECTOR_SCRIPT_OBISCODE.getLN(), 6), new Unsigned16(1));
        Limiter.ActionItem action2 = limiter.new ActionItem(OctetString.fromByteArray(DISCONNECTOR_SCRIPT_OBISCODE.getLN(), 6), new Unsigned16(actionUnderThreshold));

        Limiter.ActionType actions = limiter.new ActionType(action1, action2);
        limiter.writeActions(actions);
    }

    private void writeEmergencyProfile(int emergencyProfileId, Date date, int emergencyDuration, Limiter limiter) throws IOException {
        Limiter.EmergencyProfile emergencyProfile = limiter.new EmergencyProfile();
        emergencyProfile.addDataType(new Unsigned16(emergencyProfileId));
        emergencyProfile.addDataType(new OctetString(ProtocolTools.getSubArray(new AXDRDateTime(date, getProtocol().getTimeZone()).getBEREncodedByteArray(), 2)));
        emergencyProfile.addDataType(new Unsigned32(emergencyDuration));
        limiter.writeEmergencyProfile(emergencyProfile);
    }

    private void writeEmergencyThreshold(int monitoredValue, long activeThreshold, Limiter limiter) throws IOException {
        if (monitoredValue == 1) {
            limiter.writeThresholdEmergency(new Unsigned16((int) activeThreshold));
        } else {
            limiter.writeThresholdEmergency(new Unsigned32(activeThreshold));
        }
    }

    private void writeNormalThreshold(int monitoredValue, long activeThreshold, Limiter limiter) throws IOException {
        if (monitoredValue == 1) {
            limiter.writeThresholdNormal(new Unsigned16((int) activeThreshold));
        } else {
            limiter.writeThresholdNormal(new Unsigned32(activeThreshold));
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


    private void setControlMode(OfflineDeviceMessage offlineDeviceMessage) throws IOException {
        int controlMode = new BigDecimal(offlineDeviceMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue()).intValue();
        getCosemObjectFactory().getDisconnector().writeControlMode(new TypeEnum(controlMode));
    }

    protected void closeRelay(OfflineDeviceMessage offlineDeviceMessage) throws IOException {
        int relayControlNumber = new BigDecimal(offlineDeviceMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue()).intValue();
        ObisCode obisCode = RELAY_CONTROL_OBISCODE;
        obisCode = ProtocolTools.setObisCodeField(obisCode, 1, (byte) relayControlNumber);
        getProtocol().getDlmsSession().getCosemObjectFactory().getDisconnector(obisCode).remoteReconnect();
    }

    protected void remoteDisconnect() throws IOException {
        getProtocol().getDlmsSession().getCosemObjectFactory().getDisconnector().remoteDisconnect();
    }

    protected void timedAction(OfflineDeviceMessage offlineDeviceMessage, int action) throws IOException {
        Long epoch = Long.valueOf(offlineDeviceMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
        Date actionTime = new Date(epoch);  //EIServer system timezone

        SingleActionSchedule singleActionSchedule = getProtocol().getDlmsSession().getCosemObjectFactory().getSingleActionSchedule(TIMED_CONNECTOR_ACTION_OBISCODE);

        Structure scriptStruct = new Structure();
        scriptStruct.addDataType(new OctetString(DISCONNECTOR_SCRIPT_OBISCODE.getLN()));
        scriptStruct.addDataType(new Unsigned16(action));     // 1 = disconnect, 2 = connect

        singleActionSchedule.writeExecutedScript(scriptStruct);
        Calendar cal = Calendar.getInstance(getProtocol().getTimeZone());
        cal.setTime(actionTime);
        singleActionSchedule.writeExecutionTime(convertDateToDLMSArray(cal));
    }

    protected void remoteConnect() throws IOException {
        getProtocol().getDlmsSession().getCosemObjectFactory().getDisconnector().remoteReconnect();
    }

    protected void openRelay(OfflineDeviceMessage offlineDeviceMessage) throws IOException {
        int relayControlNumber = new BigDecimal(offlineDeviceMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue()).intValue();
        ObisCode obisCode = RELAY_CONTROL_OBISCODE;
        obisCode = ProtocolTools.setObisCodeField(obisCode, 1, (byte) relayControlNumber);
        getProtocol().getDlmsSession().getCosemObjectFactory().getDisconnector(obisCode).remoteDisconnect();
    }


    protected void resetAllAlarmBits(ObisCode obisCode) throws IOException {
        long alarmBits = getCosemObjectFactory().getData(obisCode).getValue();
        Data data = getCosemObjectFactory().getData(obisCode);
        data.setValueAttr(new Unsigned32(alarmBits));
    }

    private void resetAllErrorBits(OfflineDeviceMessage offlineDeviceMessage) throws IOException {
        Data data = getCosemObjectFactory().getData(ERROR_BITS_OBISCODE);
        long errorBits = data.getValueAttr().longValue();
        data.setValueAttr(new Unsigned32(errorBits));
    }

    protected void writeAlarmFilter(ObisCode obisCode, long filter) throws IOException {
        Data data = getProtocol().getDlmsSession().getCosemObjectFactory().getData(obisCode);
        data.setValueAttr(new Unsigned32(filter));
    }

    private void writeActivityCalendar(OfflineDeviceMessage offlineDeviceMessage) throws IOException {
        String name = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, activityCalendarNameAttributeName).getDeviceMessageAttributeValue();
        String activationDate = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, activityCalendarActivationDateAttributeName).getDeviceMessageAttributeValue();
        String codeTableDescription = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, activityCalendarCodeTableAttributeName).getDeviceMessageAttributeValue();
        String typeTag = "Activity_Calendar";

        //Insert attribute values in the XML description, this was not done in the format method (since we only have one message attribute at a time there...)
        long epoch = Long.valueOf(activationDate);
        epoch = Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime().before(new Date(epoch)) ? epoch : 1;  //Replace date in past with "1"
        codeTableDescription = codeTableDescription.replace("<ActivationDate>0</ActivationDate>", "<ActivationDate>" + String.valueOf(epoch) + "</ActivationDate>");
        codeTableDescription = codeTableDescription.replace("<CalendarName>0</CalendarName>", "<CalendarName>" + name + "</CalendarName>");

        //Encode the XML content
        String base64encodedXML = encode(codeTableDescription);
        MessageTag mainTag = new MessageTag(typeTag);
        MessageTag subTag = new MessageTag("RawContent");
        subTag.add(new MessageValue(base64encodedXML));
        mainTag.add(subTag);
        String xmlContent = SimpleTagWriter.writeTag(mainTag);

        //Now provide the full code table description to the controller
        ActivityCalendarController activityCalendarController = new DLMSActivityCalendarController(getCosemObjectFactory(), getProtocol().getTimeZone());
        activityCalendarController.parseContent(xmlContent);
        activityCalendarController.writeCalendarName("");
        activityCalendarController.writeCalendar();
    }

    private void writeSpecialDays(OfflineDeviceMessage offlineDeviceMessage) throws IOException {
        String codeTableDescription = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, specialDaysCodeTableAttributeName).getDeviceMessageAttributeValue();
        String type = "Special_Days";

        MessageTag mainTag = new MessageTag(type);
        MessageTag subTag = new MessageTag("RawContent");
        subTag.add(new MessageValue(encode(codeTableDescription)));
        mainTag.add(subTag);
        String xmlContent = SimpleTagWriter.writeTag(mainTag);

        ActivityCalendarController activityCalendarController = new DLMSActivityCalendarController(getCosemObjectFactory(), getProtocol().getTimeZone());
        activityCalendarController.parseContent(xmlContent);
        activityCalendarController.writeSpecialDaysTable();
    }

    /**
     * Base64 encode a given xml string
     */
    private String encode(String codeTableDescription) {
        try {
            return ProtocolTools.compress(codeTableDescription);
        } catch (IOException e) {
            throw MdcManager.getComServerExceptionFactory().createGeneralParseException(e);
        }
    }

    private void configurationDownload(OfflineDeviceMessage offlineDeviceMessage) throws IOException {

        String xmlData = new String(ProtocolTools.getBytesFromHexString(offlineDeviceMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue(), ""));
        XMLParser parser = new XMLParser(getProtocol().getLogger(), getProtocol().getDlmsSession().getCosemObjectFactory());

        parser.parseXML(xmlData);
        List<Object[]> parsedObjects = parser.getParsedObjects();

        IOException ioException = null;
        getProtocol().getLogger().info("Transferring the objects to the device.");
        for (Object[] each : parsedObjects) {
            AbstractDataType value = (AbstractDataType) each[1];
            if (each[0].getClass().equals(GenericWrite.class)) {
                GenericWrite genericWrite = (GenericWrite) each[0];
                try {
                    genericWrite.write(value.getBEREncodedByteArray());
                } catch (DataAccessResultException e) {
                    ioException = e;
                    getProtocol().getLogger().severe("ERROR: Failed to write DLMS object " + genericWrite.getObjectReference() + " , attribute " + genericWrite.getAttr() + " : " + e);
                }
            } else if (each[0].getClass().equals(GenericInvoke.class)) {
                GenericInvoke genericInvoke = (GenericInvoke) each[0];
                try {
                    genericInvoke.invoke(value.getBEREncodedByteArray());
                } catch (DataAccessResultException e) {
                    ioException = e;
                    getProtocol().getLogger().severe("ERROR: Failed to execute action on DLMS object " + genericInvoke.getObjectReference() + " , method " + genericInvoke.getMethod() + " : " + e);
                }
            }
        }

        getProtocol().getLogger().log(Level.INFO, "Configuration download message finished.");
        if (ioException != null) {
            throw ioException;
        }
    }
}