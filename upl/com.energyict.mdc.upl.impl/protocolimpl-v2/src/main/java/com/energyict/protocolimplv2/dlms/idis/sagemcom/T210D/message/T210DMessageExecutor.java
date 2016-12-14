package com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.message;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.attributeobjects.ImageTransferStatus;
import com.energyict.mdc.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineDeviceMessageAttribute;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NotInObjectListException;
import com.energyict.protocol.ProtocolException;
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

    public T210DMessageExecutor(AbstractDlmsProtocol protocol) {
        super(protocol);
    }

    @Override
    protected CollectedMessage executeMessage(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {

        if (pendingMessage.getSpecification().equals(AlarmConfigurationMessage.RESET_DESCRIPTOR_FOR_ALARM_REGISTER)) {
            collectedMessage = resetAlarmDescriptor(pendingMessage, collectedMessage);
        }  else if (pendingMessage.getSpecification().equals(AlarmConfigurationMessage.RESET_BITS_IN_ALARM_REGISTER)) {
            collectedMessage = resetAlarmBits(pendingMessage, collectedMessage);
        } else if (pendingMessage.getSpecification().equals(AlarmConfigurationMessage.WRITE_FILTER_FOR_ALARM_REGISTER)) {
            collectedMessage = writeFilter(pendingMessage, collectedMessage);
        } else if (pendingMessage.getSpecification().equals(LoadBalanceDeviceMessage.CONFIGURE_SUPERVISION_MONITOR_FOR_IMPORT_EXPORT)) {
            collectedMessage = configureSuperVisionMonitor(pendingMessage, collectedMessage);
        } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.ConfigureGeneralLocalPortReadout)) {
            collectedMessage = configureConsumerP1port(pendingMessage, collectedMessage);
        } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.DISABLE_PUSH_ON_INSTALLATION)) {
            collectedMessage = disablePushOnInstallation(pendingMessage, collectedMessage);
        } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.ENABLE_PUSH_ON_INTERVAL_OBJECTS)) {
            collectedMessage = enablePushOnInterval(pendingMessage, collectedMessage);
        } else if (pendingMessage.getSpecification().equals(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_RESUME_AND_IMAGE_IDENTIFIER)) {
            firmwareUpgrade(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(MBusSetupDeviceMessage.ScanAndInstallWiredMbusDevices)) {
            collectedMessage = scanAndInstallWiredMbusDevices(collectedMessage, pendingMessage);
        } else if (pendingMessage.getSpecification().equals(MBusSetupDeviceMessage.InstallWirelessMbusDevices)) {
            collectedMessage = installWirelessMbusDevices(collectedMessage, pendingMessage);
        } else {
            collectedMessage = super.executeMessage(pendingMessage, collectedMessage);
        }
        return collectedMessage;
    }

    private CollectedMessage resetAlarmDescriptor(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
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
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                String msg = "Unsupported value '" + register + "' for attribute " + DeviceMessageConstants.alarmRegisterAttributeName + ", expected '1' or '2'.";
                collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, msg));
                collectedMessage.setDeviceProtocolInformation(msg);
                return collectedMessage;
        }

        getCosemObjectFactory().getData(alarmDescriptorObisCode).setValueAttr(new Unsigned32(alarmBits.longValue()));
        return collectedMessage;
    }

    private CollectedMessage resetAlarmBits(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
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
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                String msg = "Unsupported value '" + register + "' for attribute " + DeviceMessageConstants.alarmRegisterAttributeName + ", expected '1' or '2'.";
                collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, msg));
                collectedMessage.setDeviceProtocolInformation(msg);
                return collectedMessage;
        }

        resetAllAlarmBits(alarmRegisterObisCode);
        collectedMessage.setDeviceProtocolInformation("Alarm bits reset for " + alarmRegisterObisCode.toString());
        return collectedMessage;
    }

    private CollectedMessage writeFilter(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
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
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                String msg = "Unsupported value '" + register + "' for attribute " + DeviceMessageConstants.alarmRegisterAttributeName + ", expected '1' or '2'.";
                collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, msg));
                collectedMessage.setDeviceProtocolInformation(msg);
                return collectedMessage;
        }

        writeAlarmFilter(alarmFilterObisCode, filter.longValue());
        return collectedMessage;
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

    private CollectedMessage configureSuperVisionMonitor(OfflineDeviceMessage offlineDeviceMessage, CollectedMessage collectedMessage) throws IOException {
        int phase = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, phaseAttributeName).getDeviceMessageAttributeValue()).intValue();
        int positiveThreshold = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, positiveThresholdInAmpereAttributeName).getDeviceMessageAttributeValue()).intValue();
        int negativeThreshold = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, negativeThresholdInAmpereAttributeName).getDeviceMessageAttributeValue()).intValue();
        return updateThresholds(offlineDeviceMessage, collectedMessage, phase, positiveThreshold, negativeThreshold);
    }

    private CollectedMessage updateThresholds(OfflineDeviceMessage offlineDeviceMessage, CollectedMessage collectedMessage, int phase, int positiveThreshold, int negativeThreshold) throws IOException {
        if (positiveThreshold > SUPERVISION_MAXIMUM_THRESHOLD_VALUE || negativeThreshold < -SUPERVISION_MAXIMUM_THRESHOLD_VALUE) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            String msg = "Invalid threshold value, positive threshold should be smaller than 2147483648 and negative threshold greather than 2147483648";
            collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(offlineDeviceMessage, msg));
            collectedMessage.setDeviceProtocolInformation(msg);
            return collectedMessage;
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
            return collectedMessage;
        }
    }

    private CollectedMessage configureConsumerP1port(OfflineDeviceMessage offlineDeviceMessage, CollectedMessage collectedMessage) throws IOException {
        String objectDefinitionsAttributeValue = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.objectDefinitionsAttributeName).getDeviceMessageAttributeValue();
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
                    collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(offlineDeviceMessage, msg));
                    collectedMessage.setDeviceProtocolInformation(msg);
                    return collectedMessage;
                }
                for(int i = 1; i < obis_attribute.length; i++) { //start from 1 as the first element is the obis code
                    int attribute = Integer.parseInt(obis_attribute[i].trim());
                    objectDefinitions.add(new ObjectDefinition(classId, obisCode, attribute, 0));
                }

            }
        }

        GeneralLocalPortReadout generalLocalPortReadout = getCosemObjectFactory().getGeneralLocalPortReadout();
        generalLocalPortReadout.writePushObjectList(objectDefinitions);

        return collectedMessage;
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

    protected void firmwareUpgrade(OfflineDeviceMessage offlineDeviceMessage) throws IOException {

        OfflineDeviceMessageAttribute imageAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, firmwareUpdateUserFileAttributeName);
        byte[] binaryImage = ProtocolTools.getBytesFromHexString(imageAttribute.getDeviceMessageAttributeValue(), "");
        boolean resume = Boolean.valueOf(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, resumeFirmwareUpdateAttributeName).getDeviceMessageAttributeValue());
        String firmwareIdentifier = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, firmwareUpdateImageIdentifierAttributeName).getDeviceMessageAttributeValue();
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

    private CollectedMessage disablePushOnInstallation(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) {
        Array executionTimes = new Array();
        Structure timeDate = new Structure();
        timeDate.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(disabledTime, "")));
        timeDate.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(disabledDate, "")));
        executionTimes.addDataType(timeDate);
        ObisCode pushSetupObisCode = PUSH_ACTION_SCHEDULER_OBISCODE;
        pushSetupObisCode.setB(AlarmConfigurationMessage.PushType.On_Installation.getId());
        return writeExecutionTime(pendingMessage, collectedMessage, executionTimes, pushSetupObisCode);
    }

    private CollectedMessage enablePushOnInterval(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) {
        String executionMinutesForEachHour = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.executionMinutesForEachHour).getDeviceMessageAttributeValue();
        String setupType = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.typeAttributeName).getDeviceMessageAttributeValue();
        ObisCode pushSetupObisCode = PUSH_ACTION_SCHEDULER_OBISCODE;
        pushSetupObisCode.setB(ConfigurationChangeDeviceMessage.PushType.valueOf(setupType).getId());
        List<String> executionMinutes = extractExecutionMinutesFromString(pendingMessage, collectedMessage, executionMinutesForEachHour);
        Array executionTimes = new Array();
        for(String minuteValue: executionMinutes){
            String time = undefined_hour + getMinuteValueInHex(minuteValue) + undefined_second + undefined_hundredths;
            Structure timeDate = new Structure();
            timeDate.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(time, "")));
            timeDate.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(disabledDate, "")));
            executionTimes.addDataType(timeDate);
        }
        return writeExecutionTime(pendingMessage, collectedMessage, executionTimes, pushSetupObisCode);
    }

    private String getMinuteValueInHex(String minuteValue) {
        String minute =  Integer.toHexString(Integer.parseInt(minuteValue));
        return minute.length() == 1 ? "0"+minute : minute;
    }

    private List<String> extractExecutionMinutesFromString(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage, String executionMinutesForEachHour) {
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

    private CollectedMessage writeExecutionTime(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage, Array executionTimes, ObisCode obisCode) {
        try {
            SingleActionSchedule singleActionSchedule = getCosemObjectFactory().getSingleActionSchedule(obisCode);
            singleActionSchedule.writeExecutionTime(executionTimes);
            return collectedMessage;
        } catch (NotInObjectListException niole){
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            String msg = "The obiscode: "+obisCode.getValue()+" was not found in device object list: " + niole.getMessage();
            collectedMessage.setFailureInformation(ResultType.NotSupported, createMessageFailedIssue(pendingMessage, msg));
            collectedMessage.setDeviceProtocolInformation(msg);
            return collectedMessage;
        } catch (IOException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            String msg = "Unable to write execution time array";
            collectedMessage.setFailureInformation(ResultType.NotSupported, createMessageFailedIssue(pendingMessage, msg));
            collectedMessage.setDeviceProtocolInformation(msg);
            return collectedMessage;
        }
    }

    private CollectedMessage installWirelessMbusDevices(CollectedMessage collectedMessage, OfflineDeviceMessage pendingMessage) throws IOException {
        //Mbus device scanning done automatically for wireless connection
        //extract data from MBus search result
        Array mbusSearchResult = getMbusSearchResult(collectedMessage, pendingMessage, WIRELESS_MBUS_SEARCH_RESULT);
        installSlave(collectedMessage, pendingMessage, mbusSearchResult, WIRELESS_MBUS_PORT_REFERENCE);
        return collectedMessage;
    }

    private CollectedMessage scanAndInstallWiredMbusDevices(CollectedMessage collectedMessage, OfflineDeviceMessage pendingMessage) throws IOException {
        scanWiredMBusDevices(collectedMessage, pendingMessage);
        //extract data from MBus search result
        Array mbusSearchResult = getMbusSearchResult(collectedMessage, pendingMessage, WIRED_MBUS_SEARCH_RESULT);
        installSlave(collectedMessage, pendingMessage, mbusSearchResult, WIRED_MBUS_PORT_REFERENCE);
        return collectedMessage;
    }

    /*
        Each mbusSearchResult entry is an octet-string[8], formatted as NNNNNNNNMMMMVVDD, where:
        - NNNNNNNN: Serial Number
        - MMMM: Manufacturer ID
        - VV: Version
        - DD: Device Type
    */
    private CollectedMessage installSlave(CollectedMessage collectedMessage, OfflineDeviceMessage pendingMessage, Array mbusSearchResult, ObisCode mbusPortReferenceObis) throws IOException {
        for(AbstractDataType mbusSearchResultEntry: mbusSearchResult){
            String entry = mbusSearchResultEntry.getOctetString().stringValue();
            String serialNumber = entry.substring(0, 8);
            String manufacturerId = entry.substring(8, 12);
            String version = entry.substring(12, 14);
            String deviceType = entry.substring(14, 16);

            for (int channel = 1; channel <= getMaxMBusSlaves(); channel++) {//Check the available 4 channels, install the slave meter on a free channel client.
                ObisCode obisCode = ProtocolTools.setObisCodeField(MBUS_CLIENT_OBISCODE, 1, (byte) channel);   //Find the right MBus client object
                MBusClient mbusClient = getMbusClientForVersion(collectedMessage, pendingMessage, version, obisCode);
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
        return collectedMessage;
    }

    private MBusClient getMbusClientForVersion(CollectedMessage collectedMessage, OfflineDeviceMessage pendingMessage, String version, ObisCode obisCode) throws NotInObjectListException {
        try {
            return getCosemObjectFactory().getMbusClient(obisCode, Integer.parseInt(version));
        } catch (NotInObjectListException e) {
            setNotInObjectListMessage(collectedMessage, obisCode.toString(), pendingMessage, e);
            throw e;
        }
    }

    private Array getMbusSearchResult(CollectedMessage collectedMessage, OfflineDeviceMessage pendingMessage, ObisCode searchResultObis) throws IOException {
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

    private void scanWiredMBusDevices(CollectedMessage collectedMessage, OfflineDeviceMessage pendingMessage) throws IOException {
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

    protected int getMaxMBusSlaves() {
        return MAX_MBUS_SLAVES;
    }

}
