package com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.message;

import com.energyict.dlms.GeneralCipheringKeyType;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.aso.SecurityPolicy;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.attributeobjects.ImageTransferStatus;
import com.energyict.dlms.cosem.attributeobjects.dataprotection.DataProtectionFactory;
import com.energyict.dlms.cosem.attributeobjects.dataprotection.ProtectionType;
import com.energyict.mdc.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineDeviceMessageAttribute;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NotInObjectListException;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.base.ActivityCalendarController;
import com.energyict.protocolimpl.dlms.common.DLMSActivityCalendarController;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am540.messages.AM540MessageExecutor;
import com.energyict.protocolimplv2.messages.*;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

/**
 * Created by cisac on 8/1/2016.
 */
public class T210DMessageExecutor extends AM540MessageExecutor{

    private static final ObisCode ALARM_BITS_OBISCODE_3 = ObisCode.fromString("0.0.97.98.2.255");
    private static final ObisCode ALARM_FILTER_OBISCODE_3 = ObisCode.fromString("0.0.97.98.12.255");
    private static final ObisCode ALARM_DESCRIPTOR_OBISCODE_3 = ObisCode.fromString("0.0.97.98.22.255");
    private static final ObisCode P1_PORT_VERSION_OBIS = ObisCode.fromString("1.3.0.2.8.255");
    private static final ObisCode CLOCK_OBIS = ObisCode.fromString("0.0.1.0.0.255");
    private static final ObisCode PUSH_ACTION_SCHEDULER_OBISCODE = ObisCode.fromString("0.0.15.0.4.255");
    private static final ObisCode WIRED_SCAN_SCRIPT_TABLE = ObisCode.fromString("0.0.10.1.101.255");
    private static final ObisCode WIRED_MBUS_SEARCH_RESULT = ObisCode.fromString("0.0.96.70.0.255");
    private static final ObisCode WIRELESS_MBUS_SEARCH_RESULT = ObisCode.fromString("0.1.96.70.0.255");
    private static final ObisCode WIRED_MBUS_PORT_REFERENCE = ObisCode.fromString("0.0.24.6.0.255");
    private static final ObisCode WIRELESS_MBUS_PORT_REFERENCE = ObisCode.fromString("0.1.24.6.0.255");
    private static final ObisCode TIMED_CONNECTOR_ACTION_OBISCODE = ObisCode.fromString("0.0.15.0.1.255");
    private static ObisCode ACTIVITY_CALENDAR_OBISCODE = ObisCode.fromString("0.0.13.0.0.255");
    private static ObisCode SPECIAL_DAYS_TABLE_OBISCODE = ObisCode.fromString("0.0.11.0.0.255");
    private static final long SUPERVISION_MAXIMUM_THRESHOLD_VALUE = 0x80000000l;
    private final String undefined_hour = "FF"; //not defined
    private final String undefined_minute = "FF"; //not defined
    private final String undefined_second = "FF"; //not defined
    private final String undefined_hundredths = "FF"; //not defined
    private final String undefined_year = "FFFF"; //not defined
    private final String undefined_month = "FF"; //not defined
    private final String undefined_dayOfMonth = "FF"; //not defined
    private final String undefined_dayOfWeek = "FF"; //not defined
    private final String disabledTime = undefined_hour + undefined_minute + undefined_second + undefined_hundredths;
    private final String disabledDate = undefined_year + undefined_month + undefined_dayOfMonth + undefined_dayOfWeek;
    private static final int MAX_MBUS_SLAVES = 4;
    private DataProtection dataProtection;
    private OfflineDeviceMessage pendingMessage;
    private CollectedMessage collectedMessage;
    private ActivityCalendarController activityCalendarController;

    public T210DMessageExecutor(AbstractDlmsProtocol protocol) {
        super(protocol);
    }

    @Override
    protected CollectedMessage executeMessage(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        this.pendingMessage = pendingMessage;
        this.collectedMessage = collectedMessage;

        if (pendingMessage.getSpecification().equals(AlarmConfigurationMessage.RESET_DESCRIPTOR_FOR_ALARM_REGISTER)) {
            resetAlarmDescriptor();
        }  else if (pendingMessage.getSpecification().equals(AlarmConfigurationMessage.RESET_BITS_IN_ALARM_REGISTER)) {
            resetAlarmBits();
        } else if (pendingMessage.getSpecification().equals(AlarmConfigurationMessage.WRITE_FILTER_FOR_ALARM_REGISTER)) {
            writeFilter();
        } else if (pendingMessage.getSpecification().equals(LoadBalanceDeviceMessage.CONFIGURE_SUPERVISION_MONITOR_FOR_IMPORT_EXPORT)) {
            configureSuperVisionMonitor();
        } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.ConfigureGeneralLocalPortReadout)) {
            configureConsumerP1port();
        } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.DISABLE_PUSH_ON_INSTALLATION)) {
            disablePushOnInstallation();
        } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.ENABLE_PUSH_ON_INTERVAL_OBJECTS)) {
            enablePushOnInterval();
        } else if (pendingMessage.getSpecification().equals(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_RESUME_AND_IMAGE_IDENTIFIER)) {
            firmwareUpgrade();
        } else if (pendingMessage.getSpecification().equals(MBusSetupDeviceMessage.ScanAndInstallWiredMbusDevices)) {
            scanAndInstallWiredMbusDevices();
        } else if (pendingMessage.getSpecification().equals(MBusSetupDeviceMessage.InstallWirelessMbusDevices)) {
            installWirelessMbusDevices();
        } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_DATA_PROTECTION)) {
            remoteDisconnectWithDataProtection();
        } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_DATA_PROTECTION)) {
            remoteReconnectWithDataProtection();
        } else if (pendingMessage.getSpecification().equals(FirmwareDeviceMessage.FIRMWARE_IMAGE_ACTIVATION_WITH_DATA_PROTECTION)) {
            firmwareImageActivationWithDataProtection();
        } else if (pendingMessage.getSpecification().equals(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_WITH_DATETIME_AND_DAY_PROFILE_DEFINITION)) {
            writeActivityCalendarOverconsumptions();
        } else if (pendingMessage.getSpecification().equals(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND_FOR_GIVEN_TABLE_OBIS)) {
            writeSpecialDaysForGivenTableObis();
        } else if (pendingMessage.getSpecification().equals(SecurityMessage.SET_REQUIRED_PROTECTION_FOR_DATA_PROTECTION_SETUP)) {
            setDataProtectionRequiredProtection();
        } else {
            collectedMessage = super.executeMessage(pendingMessage, collectedMessage);
        }
        return collectedMessage;
    }

    private void writeSpecialDaysForGivenTableObis() throws IOException {
        ObisCode specialDaysTableObisCode = ObisCode.fromString(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.specialDaysTableObiscodeAttributeName).getDeviceMessageAttributeValue());
        ACTIVITY_CALENDAR_OBISCODE.setB(specialDaysTableObisCode.getB());
        activityCalendarController = new DLMSActivityCalendarController(getCosemObjectFactory(), getProtocol().getTimeZone(), ACTIVITY_CALENDAR_OBISCODE, specialDaysTableObisCode);
        writeSpecialDays(pendingMessage);
    }

    private void writeActivityCalendarOverconsumptions() throws IOException {
        ObisCode activityCalendarObisCode = ObisCode.fromString(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.activityCalendarObiscodeAttributeName).getDeviceMessageAttributeValue());
        String dayProfileTableStringDefinition = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.dayProfileTableDefinitionAttributeName).getDeviceMessageAttributeValue();
        SPECIAL_DAYS_TABLE_OBISCODE.setB(activityCalendarObisCode.getB());
        activityCalendarController = new DLMSActivityCalendarController(getCosemObjectFactory(), getProtocol().getTimeZone(), activityCalendarObisCode, SPECIAL_DAYS_TABLE_OBISCODE, getDayProfileTable(dayProfileTableStringDefinition));
        writeActivityCalendar(pendingMessage);
    }

    /**
     *
     * @param dayProfileTableStringDefinition the string definition of the day_profile_table as described bellow
     *                                        day1_id, <start_time, script_logical_name, threshold><start_time, script_logical_name, threshold>etc;
     *                                        day2_id, <start_time, script_logical_name, threshold><start_time, script_logical_name, threshold>etc;
     *                                        etc
     * @return
     */
    private Array getDayProfileTable(String dayProfileTableStringDefinition){
        String[] dayProfilesStringDefinition = dayProfileTableStringDefinition.split(";");
        Array dayProfileTable = new Array();
        for(String dayProfileDefinition: dayProfilesStringDefinition){
            DayProfile dayProfile = new DayProfile(dayProfileDefinition);
            dayProfileTable.addDataType(dayProfile);
        }
        return dayProfileTable;
    }

    protected int getMaxMBusSlaves() {
        return MAX_MBUS_SLAVES;
    }

    private void resetAlarmDescriptor() throws IOException {
        int register = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.alarmRegisterAttributeName).getDeviceMessageAttributeValue());
        BigDecimal alarmBits = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.alarmBitMaskAttributeName).getDeviceMessageAttributeValue());
        ObisCode alarmDescriptorObisCode;
        switch (register) {
            case 1:
                alarmDescriptorObisCode = ALARM_DESCRIPTOR_OBISCODE_1;
                break;
            case 2:
                alarmDescriptorObisCode = ALARM_DESCRIPTOR_OBISCODE_2;
                break;
            case 3:
                alarmDescriptorObisCode = ALARM_DESCRIPTOR_OBISCODE_3;
                break;
            default:
                String msg = "Unsupported value '" + register + "' for attribute " + DeviceMessageConstants.alarmRegisterAttributeName + ", expected '1', '2' or '3'.";
                setIncompatibleFailedMessage(collectedMessage, pendingMessage, msg);
                throw new UnsupportedException(msg);
        }

        getCosemObjectFactory().getData(alarmDescriptorObisCode).setValueAttr(new Unsigned32(alarmBits.longValue()));
    }

    private void resetAlarmBits() throws IOException {
        int register = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.alarmRegisterAttributeName).getDeviceMessageAttributeValue());
        ObisCode alarmRegisterObisCode;
        switch (register) {
            case 1:
                alarmRegisterObisCode = ALARM_BITS_OBISCODE_1;
                break;
            case 2:
                alarmRegisterObisCode = ALARM_BITS_OBISCODE_2;
                break;
            case 3:
                alarmRegisterObisCode = ALARM_BITS_OBISCODE_3;
                break;
            default:
                String msg = "Unsupported value '" + register + "' for attribute " + DeviceMessageConstants.alarmRegisterAttributeName + ", expected '1', '2' or '3'.";
                setIncompatibleFailedMessage(collectedMessage, pendingMessage, msg);
                throw new UnsupportedException(msg);
        }

        resetAllAlarmBits(alarmRegisterObisCode);
        collectedMessage.setDeviceProtocolInformation("Alarm bits reset for " + alarmRegisterObisCode.toString());
    }

    private void writeFilter() throws IOException {
        int register = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.alarmRegisterAttributeName).getDeviceMessageAttributeValue());
        BigDecimal filter = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.alarmFilterAttributeName).getDeviceMessageAttributeValue());
        ObisCode alarmFilterObisCode;
        switch (register) {
            case 1:
                alarmFilterObisCode = ALARM_FILTER_OBISCODE_1;
                break;
            case 2:
                alarmFilterObisCode = ALARM_FILTER_OBISCODE_2;
                break;
            case 3:
                alarmFilterObisCode = ALARM_FILTER_OBISCODE_3;
                break;
            default:
                String msg = "Unsupported value '" + register + "' for attribute " + DeviceMessageConstants.alarmRegisterAttributeName + ", expected '1' or '2'.";
                setIncompatibleFailedMessage(collectedMessage, pendingMessage, msg);
                throw new UnsupportedException(msg);
        }

        writeAlarmFilter(alarmFilterObisCode, filter.longValue());
    }

    /*
        Example of expected objectDefinition: 0.0.42.0.0.255,2;0.0.96.65.0.255,2,3
        For the case when multiple attributes are present after the obis splited by "," there will be one entry for each attribute of the same obis code
    */
    @Override
    protected List<ObjectDefinition> composePushSetupObjectDefinitions(ObisCode pushSetupObisCode, String objectDefinitionsAttributeValue) throws ProtocolException {
        List<ObjectDefinition> objectDefinitions = new ArrayList<>();
        addObjectDefinitionsToConfig(objectDefinitions, pushSetupObisCode, DLMSClassId.PUSH_EVENT_NOTIFICATION_SETUP.getClassId(), 1);
        for (String definition : objectDefinitionsAttributeValue.trim().split(";")) {
            String[] obis_attribute = definition.split(",");
            ObisCode obisCode = ObisCode.fromString(obis_attribute[0].trim());
            int classId = getCosemObjectFactory().getProtocolLink().getMeterConfig().getClassId(obisCode);
            for(int i = 1; i < obis_attribute.length; i++) { //start from 1 as the first element is the obis code
                int attribute = Integer.parseInt(obis_attribute[i].trim());
                objectDefinitions.add(new ObjectDefinition(classId, obisCode, attribute, 0));
            }
        }
        return objectDefinitions;
    }

    private void configureSuperVisionMonitor() throws IOException {
        int phase = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, phaseAttributeName).getDeviceMessageAttributeValue()).intValue();
        int positiveThreshold = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, positiveThresholdInAmpereAttributeName).getDeviceMessageAttributeValue()).intValue();
        int negativeThreshold = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, negativeThresholdInAmpereAttributeName).getDeviceMessageAttributeValue()).intValue();
        updateThresholds(phase, positiveThreshold, negativeThreshold);
    }

    private void updateThresholds(int phase, int positiveThreshold, int negativeThreshold) throws IOException {
        if (positiveThreshold > SUPERVISION_MAXIMUM_THRESHOLD_VALUE || negativeThreshold < -SUPERVISION_MAXIMUM_THRESHOLD_VALUE) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            String msg = "Invalid threshold value, positive threshold should be smaller than 2147483648 and negative threshold greather than 2147483648";
            collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, msg));
            collectedMessage.setDeviceProtocolInformation(msg);
        } else {
            ObisCode obisCode = null;
            switch (phase) {
                case 1:
                    obisCode = ObisCode.fromString("1.1.31.4.0.255");
                    break;
                case 2:
                    obisCode = ObisCode.fromString("1.1.51.4.0.255");
                    break;
                case 3:
                    obisCode = ObisCode.fromString("1.1.71.4.0.255");
                    break;
            }
            RegisterMonitor registerMonitor = getCosemObjectFactory().getRegisterMonitor(obisCode);
            Array thresholds = new Array();
            thresholds.addDataType(new Integer16(positiveThreshold));
            thresholds.addDataType(new Integer16(negativeThreshold));
            registerMonitor.writeThresholds(thresholds);
        }
    }

    private void configureConsumerP1port() throws IOException {
        String objectDefinitionsAttributeValue = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.objectDefinitionsAttributeName).getDeviceMessageAttributeValue();
        List<ObjectDefinition> objectDefinitions = new ArrayList<>();
        //add first the P1 port version and clock obis as these two will always be present
        //When only these are present the GeneralLocalPortReadout object is considered disabled
        objectDefinitions.add(new ObjectDefinition(1, P1_PORT_VERSION_OBIS, 2, 0));
        objectDefinitions.add(new ObjectDefinition(8, CLOCK_OBIS, 2, 0));

        if(objectDefinitionsAttributeValue.trim().length() > 0){
            for (String definition : objectDefinitionsAttributeValue.trim().split(";")) {
                String[] obis_attribute = definition.trim().split(",");
                ObisCode obisCode = ObisCode.fromString(obis_attribute[0].trim());
                int classId = 0;
                try {
                    classId = getCosemObjectFactory().getProtocolLink().getMeterConfig().getClassId(obisCode);
                } catch (NotInObjectListException e) {
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    String msg = "The objectDefinition attribute contains at least one invalid value " + e.getMessage();
                    collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, msg));
                    collectedMessage.setDeviceProtocolInformation(msg);
                    break;
                }
                for(int i = 1; i < obis_attribute.length; i++) { //start from 1 as the first element is the obis code
                    int attribute = Integer.parseInt(obis_attribute[i].trim());
                    objectDefinitions.add(new ObjectDefinition(classId, obisCode, attribute, 0));
                }

            }
        }

        GeneralLocalPortReadout generalLocalPortReadout = getCosemObjectFactory().getGeneralLocalPortReadout();
        generalLocalPortReadout.writePushObjectList(objectDefinitions);

    }

    @Override
    protected CollectedMessage verifyAndActivateFirmware(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        ImageTransfer imageTransfer = getCosemObjectFactory().getImageTransfer();

        ImageTransferStatus imageTransferStatus = imageTransfer.readImageTransferStatus();
        if (imageTransferStatus.equals(ImageTransferStatus.TRANSFER_INITIATED)) {
            try {
                imageTransfer.verifyAndPollForSuccess();
            } catch (DataAccessResultException e) {
                String errorMsg = "Verification of image failed: " + e.getMessage();
                collectedMessage.setDeviceProtocolInformation(errorMsg);
                collectedMessage.setFailureInformation(ResultType.DataIncomplete, createMessageFailedIssue(pendingMessage, errorMsg));
                return collectedMessage;
            }
        }

        if (imageTransferStatus.equals(ImageTransferStatus.VERIFICATION_SUCCESSFUL)){
            try {
                imageTransfer.setUsePollingVerifyAndActivate(false);    //Don't use polling for the activation, the meter reboots immediately!
                imageTransfer.imageActivation();
                collectedMessage.setDeviceProtocolInformation("Image has been activated.");
            } catch (IOException e) {
                if (isTemporaryFailure(e) || isTemporaryFailure(e.getCause())) {
                    collectedMessage.setDeviceProtocolInformation("Image activation returned 'temporary failure'. The activation is in progress, moving on.");
                } else if (e.getMessage().toLowerCase().contains("timeout")) {
                    collectedMessage.setDeviceProtocolInformation("Image activation timed out, meter is rebooting. Moving on.");
                } else {
                    throw e;
                }
            }
        } else {
            String errorMsg = "The ImageTransfer is in an invalid state: expected state '3' (Image verification successful), but was '" +
                    imageTransferStatus.getValue() + "' (" + imageTransferStatus.getInfo() + "). " +
                    "The activation will not be executed.";
            collectedMessage.setDeviceProtocolInformation(errorMsg);
            collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, errorMsg));
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
        }

        return collectedMessage;
    }

    private void firmwareUpgrade() throws IOException {

        OfflineDeviceMessageAttribute imageAttribute = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateUserFileAttributeName);
        byte[] binaryImage = ProtocolTools.getBytesFromHexString(imageAttribute.getDeviceMessageAttributeValue(), "");
        boolean resume = Boolean.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, resumeFirmwareUpdateAttributeName).getDeviceMessageAttributeValue());
        String firmwareIdentifier = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateImageIdentifierAttributeName).getDeviceMessageAttributeValue();
        int length = binaryImage[0];
        ImageTransfer imageTransfer = getCosemObjectFactory().getImageTransfer();
        if (resume) {
            int lastTransferredBlockNumber = imageTransfer.readFirstNotTransferedBlockNumber().intValue();
            if (lastTransferredBlockNumber > 0) {
                imageTransfer.setStartIndex(lastTransferredBlockNumber - 1);
            }
        }

        imageTransfer.setUsePollingVerifyAndActivate(true);    //Poll verification
        imageTransfer.upgrade(binaryImage, false, firmwareIdentifier, true);
    }

    private void disablePushOnInstallation() {
        Array executionTimes = new Array();
        Structure timeDate = new Structure();
        timeDate.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(disabledTime, "")));
        timeDate.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(disabledDate, "")));
        executionTimes.addDataType(timeDate);
        ObisCode pushSetupObisCode = PUSH_ACTION_SCHEDULER_OBISCODE;
        pushSetupObisCode.setB(AlarmConfigurationMessage.PushType.On_Installation.getId());
        writeExecutionTime(executionTimes, pushSetupObisCode);
    }

    private void enablePushOnInterval() {
        String executionMinutesForEachHour = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.executionMinutesForEachHour).getDeviceMessageAttributeValue();
        String setupType = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.typeAttributeName).getDeviceMessageAttributeValue();
        ObisCode pushSetupObisCode = PUSH_ACTION_SCHEDULER_OBISCODE;
        pushSetupObisCode.setB(ConfigurationChangeDeviceMessage.PushType.valueOf(setupType).getId());
        List<String> executionMinutes = extractExecutionMinutesFromString(executionMinutesForEachHour);
        Array executionTimes = new Array();
        for(String minuteValue: executionMinutes){
            String time = undefined_hour + getMinuteValueInHex(minuteValue) + undefined_second + undefined_hundredths;
            Structure timeDate = new Structure();
            timeDate.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(time, "")));
            timeDate.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(disabledDate, "")));
            executionTimes.addDataType(timeDate);
        }
        writeExecutionTime(executionTimes, pushSetupObisCode);
    }

    private String getMinuteValueInHex(String minuteValue) {
        String minute =  Integer.toHexString(Integer.parseInt(minuteValue));
        return minute.length() == 1 ? "0"+minute : minute;
    }

    private List<String> extractExecutionMinutesFromString(String executionMinutesForEachHour) {
        List<String> executionMinutes = new ArrayList<>();
        for(String minute: executionMinutesForEachHour.trim().split(",")){
            try {
                int m = Integer.parseInt(minute);
                if (m < 0 || m > 59){
                    throw new NumberFormatException();
                }
                executionMinutes.add(minute);
            } catch (NumberFormatException e){
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                String msg = "The value: "+ minute +" is not a valid minute in an hour. Valid values are between 0 and 59 " + e.getMessage();
                collectedMessage.setFailureInformation(ResultType.ConfigurationError, createMessageFailedIssue(pendingMessage, msg));
                collectedMessage.setDeviceProtocolInformation(msg);
            }
        }
        return executionMinutes;
    }

    private void writeExecutionTime(Array executionTimes, ObisCode obisCode) {
        try {
            SingleActionSchedule singleActionSchedule = getCosemObjectFactory().getSingleActionSchedule(obisCode);
            singleActionSchedule.writeExecutionTime(executionTimes);
        } catch (NotInObjectListException niole){
            setNotInObjectListMessage(collectedMessage, obisCode.getValue(), pendingMessage, niole);
        } catch (IOException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            String msg = "Unable to write execution time array";
            collectedMessage.setFailureInformation(ResultType.NotSupported, createMessageFailedIssue(pendingMessage, msg));
            collectedMessage.setDeviceProtocolInformation(msg);
        }
    }

    private void installWirelessMbusDevices() throws IOException {
        //Mbus device scanning done automatically for wireless connection
        //extract data from MBus search result
        Array mbusSearchResult = getMbusSearchResult(WIRELESS_MBUS_SEARCH_RESULT);
        installSlave(mbusSearchResult, WIRELESS_MBUS_PORT_REFERENCE);
    }

    private void scanAndInstallWiredMbusDevices() throws IOException {
        scanWiredMBusDevices();
        //extract data from MBus search result
        Array mbusSearchResult = getMbusSearchResult(WIRED_MBUS_SEARCH_RESULT);
        installSlave(mbusSearchResult, WIRED_MBUS_PORT_REFERENCE);
    }

    /*
        Each mbusSearchResult entry is an octet-string[8], formatted as NNNNNNNNMMMMVVDD, where:
        - NNNNNNNN: Serial Number
        - MMMM: Manufacturer ID
        - VV: Version
        - DD: Device Type
    */
    private void installSlave(Array mbusSearchResult, ObisCode mbusPortReferenceObis) throws IOException {
        for(AbstractDataType mbusSearchResultEntry: mbusSearchResult){
            String entry = mbusSearchResultEntry.getOctetString().stringValue();
            String serialNumber = entry.substring(0, 8);
            String manufacturerId = entry.substring(8, 12);
            String version = entry.substring(12, 14);
            String deviceType = entry.substring(14, 16);

            for (int channel = 1; channel <= getMaxMBusSlaves(); channel++) {//Check the available 4 channels, install the slave meter on a free channel client.
                ObisCode obisCode = ProtocolTools.setObisCodeField(MBUS_CLIENT_OBISCODE, 1, (byte) channel);   //Find the right MBus client object
                MBusClient mbusClient = getMbusClientForVersion(version, obisCode);
                if (mbusClient.getPrimaryAddress().getValue() == 0) {     //Find a free channel client
                    mbusClient.setMBusPortReference(OctetString.fromObisCode(mbusPortReferenceObis));
                    mbusClient.setIdentificationNumber(new Unsigned32(Integer.parseInt(serialNumber, 16)));
                    mbusClient.setManufacturerID(new Unsigned16(Integer.parseInt(manufacturerId, 16)));
                    mbusClient.setDeviceType(new Unsigned8(Integer.parseInt(deviceType, 16)));
                    //install MBus device
                    mbusClient.invoke(1, new Unsigned8(1).getBEREncodedByteArray());
                    if(mbusClient.getPrimaryAddress().getValue() != 253){
                        String errorMsg = "Failed to install Mbus slave with identification number: "+ serialNumber;
                        setIncompatibleFailedMessage(collectedMessage, pendingMessage, errorMsg);
                    }
                }
            }
        }
    }

    private MBusClient getMbusClientForVersion(String version, ObisCode obisCode) throws NotInObjectListException {
        try {
            return getCosemObjectFactory().getMbusClient(obisCode, Integer.parseInt(version));
        } catch (NotInObjectListException e) {
            setNotInObjectListMessage(collectedMessage, obisCode.toString(), pendingMessage, e);
            throw e;
        }
    }

    private Array getMbusSearchResult(ObisCode searchResultObis) throws IOException {
        Array mbusSearchResult = null;
        try {
            mbusSearchResult = getCosemObjectFactory().getData(searchResultObis).getValueAttr().getArray();
        } catch (NotInObjectListException e) {
            setNotInObjectListMessage(collectedMessage, searchResultObis.toString(), pendingMessage, e);
            throw e;
        } catch (IOException e) {
            String errorMsg = "Unable to get the value for obis: "+ searchResultObis.toString() +". "+ e.getMessage();
            setIncompatibleFailedMessage(collectedMessage, pendingMessage, errorMsg);
            throw e;
        }
        return mbusSearchResult;
    }

    private void scanWiredMBusDevices() throws IOException {
        //on wired connection first do the scan
        try {
            getCosemObjectFactory().getData(WIRED_SCAN_SCRIPT_TABLE).invoke(1, new Unsigned16(1).getBEREncodedByteArray());
        } catch (NotInObjectListException e) {
            setNotInObjectListMessage(collectedMessage, WIRED_SCAN_SCRIPT_TABLE.toString(), pendingMessage, e);
            throw e;
        } catch (IOException e) {
            String errorMsg = "Exception occurred while trying to scan the wired MBus devices. " + e.getMessage();
            setIncompatibleFailedMessage(collectedMessage, pendingMessage, errorMsg);
            throw e;
        }
    }

    private void setDataProtectionRequiredProtection() throws IOException {
        int requiredProtection = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.requiredProtection).getDeviceMessageAttributeValue());
        getDataProtection().writeRequiredProtectionAttribute(new TypeEnum(requiredProtection));
    }

    private void remoteDisconnectWithDataProtection() throws IOException {
        executeDisconnectControlMethodWithDataProtection(Disconnector.METHOD_REMOTE_DISCONNECT);
    }

    private void remoteReconnectWithDataProtection() throws IOException {
        executeDisconnectControlMethodWithDataProtection(Disconnector.METHOD_REMOTE_RECONNECT);
    }

    private void executeDisconnectControlMethodWithDataProtection(int disconnectorMethodIndex) throws IOException {
        Unsigned16 classId = new Unsigned16(DLMSClassId.DISCONNECT_CONTROL.getClassId());
        OctetString logicalName = OctetString.fromObisCode(Disconnector.getDefaultObisCode());
        Integer8 methodIndex = new Integer8(disconnectorMethodIndex);

        invokeProtectedMethod(logicalName, classId, methodIndex, new Integer8(0));
    }

    private void firmwareImageActivationWithDataProtection() throws IOException {
        Unsigned16 classId = new Unsigned16(DLMSClassId.IMAGE_TRANSFER.getClassId());
        OctetString logicalName = OctetString.fromByteArray(ImageTransfer.LN);
        Integer8 methodIndex = new Integer8(ImageTransfer.IMAGE_ACTIVATION);
        invokeProtectedMethod(logicalName, classId, methodIndex, new Integer8(0));
    }

    private void invokeProtectedMethod(OctetString logicalName, Unsigned16 classId, Integer8 methodIndex, AbstractDataType methodParameter) throws IOException {
        //NOTE: this property will not be used if requiredProtection will be set to Digital signature(and that will be the case for T210D)
        int generalCipheringKeyTypeId = 0;

        Structure objectMethodDefinition = DataProtectionFactory.createObjectMethodDefinition(classId, logicalName, methodIndex);
        List<ProtectionType> protectionLayers = getRequestProtectionTypeLayers();
        SecurityContext securityContext = getSecurityContext();
        OctetString protectedMethodInvocationParameters = new OctetString(getEncryptedMethodInvocationParameters(protectionLayers, securityContext, methodParameter));

        try {
            Array protectionParameters = DataProtectionFactory.createProtectionParametersArray(securityContext, protectionLayers, GeneralCipheringKeyType.fromId(generalCipheringKeyTypeId));
            getDataProtection().invokeProtectedMethod(DataProtectionFactory.createInvokeProtectedMethodRequest(objectMethodDefinition, protectionParameters, protectedMethodInvocationParameters));
        } catch (IOException e) {
            String errorMessage = "Unable to invoke protected method for object with classId = " +classId.intValue()+ " obisCode = " + ObisCode.fromByteArray(logicalName.toByteArray())+ " methodIndex = "+methodIndex.intValue();
            setIncompatibleFailedMessage(collectedMessage, pendingMessage, errorMessage);
            throw e;
        }

    }

    private byte[] getEncryptedMethodInvocationParameters(List<ProtectionType> protectionLayers, SecurityContext securityContext, AbstractDataType parameter) throws UnsupportedException {
        byte[] dataToEncrypt = parameter.getBEREncodedByteArray();
        byte[] encryptedData = new byte[]{};
        for(ProtectionType protectionType: protectionLayers){
            encryptedData = securityContext.encryptProtectedMethodInvocationParameters(dataToEncrypt, protectionType);
            dataToEncrypt = encryptedData;
        }
        return encryptedData;
    }

    /**
     * NOTE: Currently the device supports only digital signing for data protection
     * @return
     * @throws IOException
     */
    private List<ProtectionType> getRequestProtectionTypeLayers() throws IOException {
        //read the required protection configured in Data Protection object

        List<ProtectionType> protectionLayers = new ArrayList<>();
        final int requiredProtection = getDataProtection().getRequiredProtectionAttribute().intValue();

        //First add the digital signing. It will be first security layer applied to the APDU
        if(ProtocolTools.isBitSet(requiredProtection, SecurityPolicy.REQUESTS_SIGNED_FLAG)){
            protectionLayers.add(ProtectionType.DIGITAL_SIGNATURE);
        }

        if(ProtocolTools.isBitSet(requiredProtection, SecurityPolicy.REQUESTS_AUTHENTICATED_FLAG)
                && ProtocolTools.isBitSet(requiredProtection, SecurityPolicy.REQUESTS_ENCRYPTED_FLAG)){
            protectionLayers.add(ProtectionType.AUTHENTICATION_AND_ENCRYPTION);
        } else if(ProtocolTools.isBitSet(requiredProtection, SecurityPolicy.REQUESTS_AUTHENTICATED_FLAG)){
            protectionLayers.add(ProtectionType.AUTHENTICATION);
        } else if(ProtocolTools.isBitSet(requiredProtection, SecurityPolicy.REQUESTS_ENCRYPTED_FLAG)){
            protectionLayers.add(ProtectionType.ENCRYPTION);
        }
        return protectionLayers;
    }

    private DataProtection getDataProtection() throws NotInObjectListException {
        try {
            if(dataProtection == null) {
                dataProtection = getCosemObjectFactory().getDataProtectionSetup();
            }
            return dataProtection;
        } catch (NotInObjectListException e) {
            setNotInObjectListMessage(collectedMessage, DataProtection.OBIS_CODE.toString(), pendingMessage, e);
            throw e;
        }
    }

    private SecurityContext getSecurityContext() {
        return getProtocol().getDlmsSession().getAso().getSecurityContext();
    }

    @Override
    protected ActivityCalendarController getActivityCalendarController() {
        if(activityCalendarController == null){
            activityCalendarController = new DLMSActivityCalendarController(getCosemObjectFactory(), getProtocol().getTimeZone());
        }
        return activityCalendarController;
    }

}
