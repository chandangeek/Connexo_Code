package com.energyict.protocolimplv2.nta.dsmr40.messages;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.ResultType;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.BitString;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.DataAccessResultCode;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.cosem.Limiter;
import com.energyict.dlms.cosem.ScriptTable;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.ActivityCalendarController;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;
import com.energyict.protocolimplv2.nta.dsmr23.messages.Dsmr23MessageExecutor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarCodeTableAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarNameAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateImageIdentifierAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateUserFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.whiteListPhoneNumbersAttributeName;

/**
 * @author sva
 * @since 6/01/2015 - 13:30
 */
public class Dsmr40MessageExecutor extends Dsmr23MessageExecutor {

    private static final ObisCode OBISCODE_CONFIGURATION_OBJECT = ObisCode.fromString("0.1.94.31.3.255");
    private static final ObisCode OBISCODE_PUSH_SCRIPT = ObisCode.fromString("0.0.10.0.108.255");
    private static final ObisCode OBISCODE_GLOBAL_RESET = ObisCode.fromString("0.1.94.31.5.255");
    private Dsmr40MbusMessageExecutor mbusMessageExecutor;

    public Dsmr40MessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = this.getCollectedDataFactory().createCollectedMessageList(pendingMessages);

        List<OfflineDeviceMessage> masterMessages = getMessagesOfMaster(pendingMessages);
        List<OfflineDeviceMessage> mbusMessages = getMbusMessages(pendingMessages);
        if (!mbusMessages.isEmpty()) {
            // Execute messages for MBus devices
            result.addCollectedMessages(getMbusMessageExecutor().executePendingMessages(mbusMessages));
        }

        List<OfflineDeviceMessage> notExecutedDeviceMessages = new ArrayList<>();
        for (OfflineDeviceMessage pendingMessage : sortSecurityRelatedDeviceMessages(masterMessages)) {
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);   //Optimistic
            try {
                if (pendingMessage.getSpecification().equals(DeviceActionMessage.RESTORE_FACTORY_SETTINGS)) {
                    restoreFactorySettings();
                } else if (pendingMessage.getSpecification().equals(SecurityMessage.ENABLE_DLMS_AUTHENTICATION_LEVEL_P0)) {
                    changeAuthenticationLevel(pendingMessage, 0, true);
                } else if (pendingMessage.getSpecification().equals(SecurityMessage.DISABLE_DLMS_AUTHENTICATION_LEVEL_P0)) {
                    changeAuthenticationLevel(pendingMessage, 0, false);
                } else if (pendingMessage.getSpecification().equals(SecurityMessage.ENABLE_DLMS_AUTHENTICATION_LEVEL_P3)) {
                    changeAuthenticationLevel(pendingMessage, 3, true);
                } else if (pendingMessage.getSpecification().equals(SecurityMessage.DISABLE_DLMS_AUTHENTICATION_LEVEL_P3)) {
                    changeAuthenticationLevel(pendingMessage, 3, false);
                } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS)) {
                    changeEncryptionKeyAndUseNewKey(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS)) {
                    changeAuthenticationKeyAndUseNewKey(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_IMAGE_IDENTIFIER)) {
                    upgradeFirmwareWithActivationDateAndImageIdentifier(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE_AND_IMAGE_IDENTIFIER)) {
                    upgradeFirmwareWithActivationDateAndImageIdentifier(pendingMessage);
                } else {
                    collectedMessage = null;
                    notExecutedDeviceMessages.add(pendingMessage);  // These messages are not specific for Dsmr 4.0, but can be executed by the super (= Dsmr 2.3) messageExecutor
                }
            } catch (IOException e) {
                if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getProtocol().getDlmsSessionProperties().getRetries() + 1)) {
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                    collectedMessage.setDeviceProtocolInformation(e.getMessage());
                }
            }
            if (collectedMessage != null) {
                result.addCollectedMessage(collectedMessage);
            }
        }

        // Then delegate all other messages to the Dsmr 2.3 message executor
        result.addCollectedMessages(super.executePendingMessages(notExecutedDeviceMessages));
        return result;
    }

    protected void changeAuthenticationKeyAndUseNewKey(OfflineDeviceMessage pendingMessage) throws IOException {
        throw new ProtocolException("This message is not yet supported in DSMR4.0");
    }

    protected void changeEncryptionKeyAndUseNewKey(OfflineDeviceMessage pendingMessage) throws IOException {
        throw new ProtocolException("This message is not yet supported in DSMR4.0");
    }

    @Override
    protected void activateWakeUp() throws IOException {
        getCosemObjectFactory().getSMSWakeupConfiguration().writeListeningWindow(new Array());
    }

    @Override
    protected void deactivateWakeUp() throws IOException {
        AXDRDateTime axdrDateTime = convertUnixToDateTime(String.valueOf(946684800), getProtocol().getTimeZone());  //Jan 1st, 2000
        OctetString time = new OctetString(axdrDateTime.getBEREncodedByteArray(), 0);
        getCosemObjectFactory().getSMSWakeupConfiguration().writeListeningWindow(time, time);   //Closed window, no SMSes are allowed
    }

    protected void addPhoneNumberToWhiteList(OfflineDeviceMessage pendingMessage) throws IOException {
        //semicolon separated list of phone numbers
        List<Structure> senders = new ArrayList<>();
        String numbers = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, whiteListPhoneNumbersAttributeName).getValue();
        for (String number : numbers.split(SEPARATOR)) {
            senders.add(createSenderAndAction(number));
        }
        getCosemObjectFactory().getSMSWakeupConfiguration().writeAllowedSendersAndActions(senders);
    }

    private Structure createSenderAndAction(String telephoneNumber) {
        Structure senderAndAction = new Structure();
        Structure action = new Structure();
        action.addDataType(OctetString.fromObisCode(OBISCODE_PUSH_SCRIPT));
        action.addDataType(new Unsigned16(3));  //3rd script contains the SMS wakeup actions

        senderAndAction.addDataType(OctetString.fromString(telephoneNumber));
        senderAndAction.addDataType(action);
        return senderAndAction;
    }

    protected void restoreFactorySettings() throws IOException {
        ScriptTable globalResetST = getCosemObjectFactory().getScriptTable(OBISCODE_GLOBAL_RESET);
        globalResetST.execute(0);
    }

    @Override
    protected void clearLoadLimitConfiguration() throws IOException {
        Limiter clearLLimiter = getCosemObjectFactory().getLimiter();
        Structure emptyStruct = new Structure();
        emptyStruct.addDataType(new Unsigned16(0));
        emptyStruct.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString("999901010601000000FFC400", "")));   //set date far in the future...
        emptyStruct.addDataType(new Unsigned32(0));
        clearLLimiter.writeEmergencyProfile(clearLLimiter.new EmergencyProfile(emptyStruct.getBEREncodedByteArray(), 0, 0));
    }

    private void changeAuthenticationLevel(OfflineDeviceMessage pendingMessage, int type, boolean enable) throws IOException {
        int newAuthLevel = getIntegerAttribute(pendingMessage);
        if (newAuthLevel != -1) {
            Data config = getCosemObjectFactory().getData(OBISCODE_CONFIGURATION_OBJECT);
            Structure value;
            BitString flags;
            try {
                value = (Structure) config.getValueAttr();
                try {
                    AbstractDataType dataType = value.getDataType(1);
                    flags = (BitString) dataType;
                } catch (IndexOutOfBoundsException e) {
                    throw new ProtocolException("Couldn't write configuration. Expected structure value of [" + OBISCODE_CONFIGURATION_OBJECT.toString() + "] to have 2 elements.");
                } catch (ClassCastException e) {
                    throw new ProtocolException("Couldn't write configuration. Expected second element of structure to be of type 'Bitstring', but was of type '" + value.getDataType(1).getClass().getSimpleName() + "'.");
                }

                flags.set(4 - type + newAuthLevel, enable);    //HLS5_P0 = bit9, HLS4_P0 = bit8, HLS3_P0 = bit7, HLS5_P3 = bit6, HLS4_P3 = bit5, HLS3_P3 = bit4
                config.setValueAttr(value);
            } catch (ClassCastException e) {
                throw new ProtocolException("Couldn't write configuration. Expected value of [" + OBISCODE_CONFIGURATION_OBJECT.toString() + "] to be of type 'Structure', but was of type '" + config.getValueAttr().getClass().getSimpleName() + "'.");
            }
        } else {
            throw new ProtocolException("Message contained an invalid authenticationLevel.");
        }
    }

    @Override
    protected AXDRDateTime getEmergencyProfileActivationAXDRDateTime(String emergencyProfileActivationDate) {
        return convertUnixToDateTime(emergencyProfileActivationDate, getProtocol().getTimeZone());
    }

    /**
     * DSMR4.0 adds support for load profiles with channels that have the same obiscode but a different unit.
     * E.g.: gas value (attr 2) and gas capture time (attr 5), both come from the same extended register but are stored in 2 individual channels.
     * <p/>
     * They should be stored in 1 register only in EiServer, gas capture time is stored as event timestamp of this register.
     */
    @Override
    protected CollectedMessage loadProfileRegisterRequest(OfflineDeviceMessage pendingMessage) throws IOException {
        CollectedMessage collectedMessage = super.loadProfileRegisterRequest(pendingMessage);
        return new LoadProfileToRegisterParser().parse(collectedMessage);
    }

    /**
     * DSMR 4.0 implementation differs from 2.3, override.
     * Order is now: write day profiles, write week profiles, write season profiles.
     */
    @Override
    protected void activityCalendar(OfflineDeviceMessage pendingMessage) throws IOException {
        String calendarName = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, activityCalendarNameAttributeName).getValue();
        String activityCalendarContents = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, activityCalendarCodeTableAttributeName).getValue();
        if (calendarName.length() > 8) {
            calendarName = calendarName.substring(0, 8);
        }

        ActivityCalendarController activityCalendarController = getActivityCalendarController();
        activityCalendarController.parseContent(activityCalendarContents);
        activityCalendarController.writeCalendarName(calendarName);
        activityCalendarController.writeCalendar(); //Does not activate it yet
        activityCalendarController.writeCalendarActivationTime(null);   //Activate now
    }

    @Override
    protected void activityCalendarWithActivationDate(OfflineDeviceMessage pendingMessage) throws IOException {
        String calendarName = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, activityCalendarNameAttributeName).getValue();
        String epoch = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, activityCalendarActivationDateAttributeName).getValue();
        String activityCalendarContents = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, activityCalendarCodeTableAttributeName).getValue();
        if (calendarName.length() > 8) {
            calendarName = calendarName.substring(0, 8);
        }

        ActivityCalendarController activityCalendarController = getActivityCalendarController();
        activityCalendarController.parseContent(activityCalendarContents);
        activityCalendarController.writeCalendarName(calendarName);
        activityCalendarController.writeCalendar(); //Does not activate it yet
        Calendar activationCal = Calendar.getInstance(getProtocol().getTimeZone());
        activationCal.setTimeInMillis(Long.parseLong(epoch));
        activityCalendarController.writeCalendarActivationTime(activationCal);   //Activate now
    }

    protected DSMR40ActivityCalendarController getActivityCalendarController() {
        return new DSMR40ActivityCalendarController(getCosemObjectFactory(), getProtocol().getDlmsSession().getTimeZone());
    }

    @Override
    protected void upgradeFirmware(OfflineDeviceMessage pendingMessage) throws IOException {
        upgradeFirmwareWithActivationDateAndImageIdentifier(pendingMessage);
    }

    @Override
    protected void upgradeFirmwareWithActivationDate(OfflineDeviceMessage pendingMessage) throws IOException {
        upgradeFirmwareWithActivationDateAndImageIdentifier(pendingMessage);
    }

    protected void upgradeFirmwareWithActivationDateAndImageIdentifier(OfflineDeviceMessage pendingMessage) throws IOException {
        String userFile = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateUserFileAttributeName).getValue();
        String activationDate = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateActivationDateAttributeName).getValue();   // Will return empty string if the MessageAttribute could not be found
        String imageIdentifier = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateImageIdentifierAttributeName).getValue(); // Will return empty string if the MessageAttribute could not be found
        byte[] image = ProtocolTools.getBytesFromHexString(userFile, "");

        ImageTransfer it = getCosemObjectFactory().getImageTransfer();
        if (isResume(pendingMessage)) {
            int lastTransferredBlockNumber = it.readFirstNotTransferedBlockNumber().intValue();
            if (lastTransferredBlockNumber > 0) {
                it.setStartIndex(lastTransferredBlockNumber - 1);
            }
        }

        it.setBooleanValue(getBooleanValue());
        it.setUsePollingVerifyAndActivate(true);    //Poll verification
        it.setPollingDelay(10000);
        it.setPollingRetries(30);
        it.setDelayBeforeSendingBlocks(5000);
        if (imageIdentifier.isEmpty()) {
            it.upgrade(image, false);
        } else {
            it.upgrade(image, false, imageIdentifier, false);
        }

        if (activationDate.isEmpty()) {
            try {
                it.setUsePollingVerifyAndActivate(false);   //Don't use polling for the activation!
                it.imageActivation();
            } catch (DataAccessResultException e) {
                if (isTemporaryFailure(e)) {
                    getProtocol().getLogger().log(Level.INFO, "Received temporary failure. Meter will activate the image when this communication session is closed, moving on.");
                } else {
                    throw e;
                }
            }
        } else {
            SingleActionSchedule sas = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getImageActivationSchedule().getObisCode());
            Array dateArray = convertActivationDateEpochToDateTimeArray(activationDate);
            sas.writeExecutionTime(dateArray);
        }
    }

    /**
     * Convert the given epoch activation date to a proper DateTimeArray
     */
    protected Array convertActivationDateEpochToDateTimeArray(String strDate) {
        return super.convertEpochToDateTimeArray(strDate);
    }

    /**
     * Not supported in DSMR4.0, subclasses can override
     */
    protected boolean isResume(OfflineDeviceMessage pendingMessage) {
        return false;
    }

    protected boolean isTemporaryFailure(DataAccessResultException e) {
        return (e.getDataAccessResult() == DataAccessResultCode.TEMPORARY_FAILURE.getResultCode());
    }

    /**
     * Default value, subclasses can override. This value is used to set the image_transfer_enable attribute.
     */
    protected int getBooleanValue() {
        return 0xFF;
    }

    /**
     * Sort the given list of device messages cause security messages 'enable/disable P0/P3 level' should be executed in the right order.<br></br>
     * The security messages will be sorted & placed at the beginning of the list, so when afterwards looping over the list
     * they will be picked up/executed in the correct order.
     */
    private List<OfflineDeviceMessage> sortSecurityRelatedDeviceMessages(List<OfflineDeviceMessage> deviceMessages) {
        List<OfflineDeviceMessage> sortedDeviceMessages = new ArrayList<>(deviceMessages.size());

        for (OfflineDeviceMessage deviceMessage : deviceMessages) {
            if (isEnableP0(deviceMessage)) {
                sortedDeviceMessages.add(deviceMessage);
            }
        }
        for (OfflineDeviceMessage deviceMessage : deviceMessages) {
            if (isEnableP3(deviceMessage)) {
                sortedDeviceMessages.add(deviceMessage);
            }
        }
        for (OfflineDeviceMessage deviceMessage : deviceMessages) {
            if (isDisableP0(deviceMessage)) {
                sortedDeviceMessages.add(deviceMessage);
            }
        }
        for (OfflineDeviceMessage deviceMessage : deviceMessages) {
            if (isDisableP3(deviceMessage)) {
                sortedDeviceMessages.add(deviceMessage);
            }
        }

        // Add all other messages - the order of these is not important
        for (OfflineDeviceMessage deviceMessage : deviceMessages) {
            if (!sortedDeviceMessages.contains(deviceMessage)) {
                sortedDeviceMessages.add(deviceMessage);
            }
        }
        return sortedDeviceMessages;
    }

    private boolean isDisableP3(OfflineDeviceMessage deviceMessage) {
        return deviceMessage.getSpecification().equals(SecurityMessage.DISABLE_DLMS_AUTHENTICATION_LEVEL_P3);
    }

    private boolean isDisableP0(OfflineDeviceMessage deviceMessage) {
        return deviceMessage.getSpecification().equals(SecurityMessage.DISABLE_DLMS_AUTHENTICATION_LEVEL_P0);
    }

    private boolean isEnableP3(OfflineDeviceMessage deviceMessage) {
        return deviceMessage.getSpecification().equals(SecurityMessage.ENABLE_DLMS_AUTHENTICATION_LEVEL_P3);
    }

    private boolean isEnableP0(OfflineDeviceMessage deviceMessage) {
        return deviceMessage.getSpecification().equals(SecurityMessage.ENABLE_DLMS_AUTHENTICATION_LEVEL_P0);
    }

    @Override
    protected AbstractMessageExecutor getMbusMessageExecutor() {
        if (this.mbusMessageExecutor == null) {
            this.mbusMessageExecutor = new Dsmr40MbusMessageExecutor(getProtocol(), this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return this.mbusMessageExecutor;
    }
}