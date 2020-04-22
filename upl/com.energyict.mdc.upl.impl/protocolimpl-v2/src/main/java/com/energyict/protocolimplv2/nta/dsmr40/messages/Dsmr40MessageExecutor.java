package com.energyict.protocolimplv2.nta.dsmr40.messages;

import com.energyict.dlms.aso.*;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.BitString;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.*;
import com.energyict.genericprotocolimpl.webrtu.common.MbusProvider;
import com.energyict.mdc.upl.NotInObjectListException;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.ResultType;

import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.ActivityCalendarController;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.*;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;
import com.energyict.protocolimplv2.nta.dsmr23.messages.Dsmr23MessageExecutor;
import com.energyict.protocolimplv2.nta.esmr50.common.loadprofiles.ESMR50LoadProfileBuilder;
import com.energyict.sercurity.*;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.MBusSetupDeviceMessage_ChangeMBusClientDeviceType;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.MBusSetupDeviceMessage_ChangeMBusClientIdentificationNumber;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.MBusSetupDeviceMessage_ChangeMBusClientManufacturerId;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.MBusSetupDeviceMessage_ChangeMBusClientVersion;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.MBusSetupDeviceMessage_mBusClientShortId;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.authenticationLevelAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.mbusChannel;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newAuthenticationKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newEncryptionKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.whiteListPhoneNumbersAttributeName;

/**
 * @author sva
 * @since 6/01/2015 - 13:30
 */
public class Dsmr40MessageExecutor extends Dsmr23MessageExecutor {

    protected static final ObisCode OBISCODE_CONFIGURATION_OBJECT = ObisCode.fromString("0.1.94.31.3.255");
    protected static final char CONFIGURATION_OBJECT_FLAGS_DISCOVER_ON_POWER_ON = 1;
    protected static final char CONFIGURATION_OBJECT_FLAGS_DYNAMIC_MBUS_ADDRESS = 2;
    protected static final char CONFIGURATION_OBJECT_FLAGS_P0_ENABLE = 3;
    
    private static final ObisCode OBISCODE_PUSH_SCRIPT = ObisCode.fromString("0.0.10.0.108.255");
    private static final ObisCode OBISCODE_GLOBAL_RESET = ObisCode.fromString("0.1.94.31.5.255");
    private Dsmr40MbusMessageExecutor mbusMessageExecutor;

    public Dsmr40MessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(protocol, collectedDataFactory, issueFactory, keyAccessorTypeExtractor);
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
            getProtocol().journal("DSMR40 Message executor processing  " + pendingMessage.getSpecification().getName());
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
                } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY)) {
                    changeEncryptionKeyAndUseNewKey(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY)) {
                    changeAuthenticationKeyAndUseNewKey(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_IMAGE_IDENTIFIER)) {
                    upgradeFirmwareWithActivationDateAndImageIdentifier(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE_AND_IMAGE_IDENTIFIER)) {
                    upgradeFirmwareWithActivationDateAndImageIdentifier(pendingMessage);
                } else  if (pendingMessage.getSpecification().equals(LoadProfileMessage.CONFIGURE_CAPTURE_DEFINITION)) {
                    collectedMessage = writeCaptureDefinition(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(LoadProfileMessage.CONFIGURE_CAPTURE_PERIOD)) {
                    collectedMessage = writeCapturePeriod(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.ENABLE_DISCOVERY_ON_POWER_UP)) {
                    changeConfigurationObjectFlag(CONFIGURATION_OBJECT_FLAGS_DISCOVER_ON_POWER_ON, true );
                } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.DISABLE_DISCOVERY_ON_POWER_UP)) {
                    changeConfigurationObjectFlag(CONFIGURATION_OBJECT_FLAGS_DISCOVER_ON_POWER_ON, false );
                } else if (pendingMessage.getSpecification().equals(MBusSetupDeviceMessage.MBusClientRemoteCommission)) {
                    mBusClientRemoteCommissioning(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(MBusSetupDeviceMessage.ChangeMBusAttributes)) {
                    changeMBusClientAttributes(pendingMessage);
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
                getProtocol().journal(Level.SEVERE,"Error while executing message " + pendingMessage.getSpecification().getName()+": " + e.getLocalizedMessage());
            }
            if (collectedMessage != null) {
                result.addCollectedMessage(collectedMessage);
            }
        }

        // Then delegate all other messages to the Dsmr 2.3 message executor
        result.addCollectedMessages(super.executePendingMessages(notExecutedDeviceMessages));
        return result;
    }

    private Unsigned16 getManufacturerId(String manufacturerId) {
        char[] chars = manufacturerId.toCharArray();
        int id = Integer.parseInt("" + ((chars[2] - 64) + (chars[1] - 64) * 32 + (chars[0] - 64) * 32 * 32));
        return new Unsigned16(id);
    }

    private Unsigned32 getIdentificationNumber(String indentificationNumber, boolean fixMbusHexShortId) {
        if (fixMbusHexShortId) {
            return new Unsigned32(Integer.parseInt(indentificationNumber));
        } else {
            return new Unsigned32(Integer.parseInt(indentificationNumber, 16));
        }
    }

    private Unsigned8 getVersion(String version) {
        return new Unsigned8(Integer.parseInt(version));
    }

    private Unsigned8 getDeviceType(String deviceType) {
        return new Unsigned8(Integer.parseInt(deviceType, 16));
    }


    private void mBusClientRemoteCommissioning(OfflineDeviceMessage pendingMessage) throws IOException {
        int installChannel = getIntegerAttribute(pendingMessage, mbusChannel);
        int physicalAddress = getMBusPhysicalAddress(installChannel);
        MBusClient mbusClient = getCosemObjectFactory().getMbusClient(getMeterConfig().getMbusClient(installChannel).getObisCode(), MBusClient.VERSION.VERSION0_BLUE_BOOK_10TH_EDITION);
        String shortId = getDeviceMessageAttributeValue(pendingMessage, MBusSetupDeviceMessage_mBusClientShortId);
        MbusProvider mbusProvider = new MbusProvider(getCosemObjectFactory(), getProtocol().getDlmsSessionProperties().getFixMbusHexShortId());
        mbusClient.setManufacturerID(mbusProvider.getManufacturerID(shortId));
        mbusClient.setIdentificationNumber(mbusProvider.getIdentificationNumber(shortId));
        mbusClient.setVersion(mbusProvider.getVersion(shortId));
        mbusClient.setDeviceType(mbusProvider.getDeviceType(shortId));
        mbusClient.installSlave(physicalAddress);
    }

    private void changeMBusClientAttributes(OfflineDeviceMessage pendingMessage) throws IOException {
        int installChannel = getIntegerAttribute(pendingMessage, mbusChannel);
        int physicalAddress = getMBusPhysicalAddress(installChannel);
        ObisCode mbusClientObisCode = getMeterConfig().getMbusClient(installChannel).getObisCode();

        getProtocol().journal("Changing MBus attributes for device installed on channel "+installChannel+" with physical address "+physicalAddress + " with obis code "+mbusClientObisCode);

        MBusClient mbusClient = getCosemObjectFactory().getMbusClient(mbusClientObisCode, MBusClient.VERSION.VERSION0_BLUE_BOOK_10TH_EDITION);
        mbusClient.setManufacturerID(getManufacturerId(getDeviceMessageAttributeValue(pendingMessage, MBusSetupDeviceMessage_ChangeMBusClientManufacturerId)));
        mbusClient.setIdentificationNumber(getIdentificationNumber(getDeviceMessageAttributeValue(pendingMessage, MBusSetupDeviceMessage_ChangeMBusClientIdentificationNumber), getProtocol().getDlmsSessionProperties()
                .getFixMbusHexShortId()));
        mbusClient.setDeviceType(getDeviceType(getDeviceMessageAttributeValue(pendingMessage, MBusSetupDeviceMessage_ChangeMBusClientDeviceType)));
        mbusClient.setVersion(getVersion(getDeviceMessageAttributeValue(pendingMessage, MBusSetupDeviceMessage_ChangeMBusClientVersion)));
    }


    private void changeConfigurationObjectFlag(int bit, boolean state) throws IOException {
        getProtocol().journal("Setting configuration object " + OBISCODE_CONFIGURATION_OBJECT+" bit "+bit+" to "+state);

        Data config = getCosemObjectFactory().getData(OBISCODE_CONFIGURATION_OBJECT);
        Structure existingStructure;
        BitString flags;
        try {
            existingStructure = (Structure) config.getValueAttr();
            int flagsIndex = getConfigurationObjectFlagsIndex();

            try {
                flags = (BitString) existingStructure.getDataType(flagsIndex);
                flags.set(bit, state);
            } catch (ClassCastException e) {
                throw new ProtocolException("Couldn't write configuration. Expected element "+flagsIndex+
                        " of structure to be of type 'BitString', but was of type '" + existingStructure.getDataType(flagsIndex).getClass().getSimpleName() + "'.");
            }

            existingStructure.setDataType(flagsIndex, flags);

            config.setValueAttr(existingStructure);
        } catch (Exception e) {
            getProtocol().journal(Level.SEVERE, "Couldn't write configuration: " +e.getLocalizedMessage());
            throw new ProtocolException(e, "Couldn't write configuration.");
        }

    }

    /**
     * DSMR 4.x:
            Value ::= structure {
                GPRS_operation_mode enum
                Flags bitstring (16)
            }

     */
    protected int getConfigurationObjectFlagsIndex(){
        return 1;
    }

    protected void changeAuthenticationKeyAndUseNewKey(OfflineDeviceMessage pendingMessage) throws IOException {
        KeyRenewalInfo keyRenewalInfo = KeyRenewalInfo.fromJson(getDeviceMessageAttributeValue(pendingMessage, newAuthenticationKeyAttributeName));
        byte[] newSymmetricKey = ProtocolTools.getBytesFromHexString(keyRenewalInfo.keyValue, "");
        byte[] wrappedKey = ProtocolTools.getBytesFromHexString(keyRenewalInfo.wrappedKeyValue, "");
        renewKey(wrappedKey, SecurityMessage.KeyID.AUTHENTICATION_KEY.getId());

        //Update the key in the security provider, it is used instantly
        getProtocol().getDlmsSession().getProperties().getSecurityProvider().changeAuthenticationKey(newSymmetricKey);
    }

    protected void changeEncryptionKeyAndUseNewKey(OfflineDeviceMessage pendingMessage) throws IOException {
        KeyRenewalInfo keyRenewalInfo = KeyRenewalInfo.fromJson(getDeviceMessageAttributeValue(pendingMessage, newEncryptionKeyAttributeName));
        byte[] newSymmetricKey = ProtocolTools.getBytesFromHexString(keyRenewalInfo.keyValue, "");
        byte[] wrappedKey = ProtocolTools.getBytesFromHexString(keyRenewalInfo.wrappedKeyValue, "");
        byte[] oldKey = getProtocol().getDlmsSession().getProperties().getSecurityProvider().getGlobalKey();

        renewKey(wrappedKey, SecurityMessage.KeyID.GLOBAL_UNICAST_ENCRYPTION_KEY.getId());

        //Update the key in the security provider, it is used instantly
        getProtocol().getDlmsSession().getProperties().getSecurityProvider().changeEncryptionKey(newSymmetricKey);

        //Reset frame counter, only if a different key has been written
        if (Arrays.equals(oldKey, newSymmetricKey)) {
            SecurityContext securityContext = getProtocol().getDlmsSession().getAso().getSecurityContext();
            securityContext.setFrameCounter(1);
            securityContext.getSecurityProvider().getRespondingFrameCounterHandler().setRespondingFrameCounter(-1);
        }
    }

    @Override
    protected void activateWakeUp() throws IOException {
        getProtocol().journal("Opening SMS wake-up window");
        getCosemObjectFactory().getSMSWakeupConfiguration().writeListeningWindow(new Array());
    }

    @Override
    protected void deactivateWakeUp() throws IOException {
        AXDRDateTime axdrDateTimeStart = convertUnixToDateTime(String.valueOf(946684800), getProtocol().getTimeZone());  //Jan 1st, 2000 00:00
        AXDRDateTime axdrDateTimeEnd = convertUnixToDateTime(String.valueOf(946684900), getProtocol().getTimeZone());  //Jan 1st, 2000 00:01
        OctetString startTime = new OctetString(axdrDateTimeStart.getBEREncodedByteArray(), 0);
        OctetString endTime = new OctetString(axdrDateTimeEnd.getBEREncodedByteArray(), 0);
        getProtocol().journal("Closing SMS wake-up window");
        getCosemObjectFactory().getSMSWakeupConfiguration().writeListeningWindow(startTime, endTime);   //Closed window, no SMSes are allowed
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

    protected void changeAuthenticationLevel(OfflineDeviceMessage pendingMessage, int type, boolean enable) throws IOException {
        int newAuthLevel = getIntegerAttribute(pendingMessage, authenticationLevelAttributeName);
        if (newAuthLevel != -1) {
            int bit = 4 - type + newAuthLevel;
            changeConfigurationObjectFlag(bit, enable);
        }
    }

    @Override
    protected AXDRDateTime getEmergencyProfileActivationAXDRDateTime(String emergencyProfileActivationDate) {
        return convertUnixToDateTime(emergencyProfileActivationDate, getProtocol().getTimeZone());
    }

    /**
     * DSMR4.0 adds support for load profiles with channels that have the same obiscode but a different unit.
     * E.g.: gas value (attr 2) and gas capture time (attr 5), both come from the same extended register but are stored in 2 individual channels.
     * <p>
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
    protected void activityCalendar(String calendarName, String activityCalendarContents) throws IOException {
        if (calendarName.length() > 8) {
            calendarName = calendarName.substring(0, 8);
        }

        ActivityCalendarController activityCalendarController = getActivityCalendarController();
        activityCalendarController.parseContent(activityCalendarContents);
        getProtocol().journal("Writing calendar name: "+calendarName);
        activityCalendarController.writeCalendarName(calendarName);
        getProtocol().journal("Writing calendar content");
        activityCalendarController.writeCalendar(); //Does not activate it yet
        getProtocol().journal("Writing null activation date - i.e. activate now");
        activityCalendarController.writeCalendarActivationTime(null);   //Activate now
    }

    @Override
    protected void activityCalendarWithActivationDate(String calendarName, String epoch, String activityCalendarContents) throws IOException {
        if (calendarName.length() > 8) {
            calendarName = calendarName.substring(0, 8);
        }

        ActivityCalendarController activityCalendarController = getActivityCalendarController();
        activityCalendarController.parseContent(activityCalendarContents);
        getProtocol().journal("Writing calendar name: "+calendarName);
        activityCalendarController.writeCalendarName(calendarName);
        getProtocol().journal("Writing calendar content");
        activityCalendarController.writeCalendar(); //Does not activate it yet
        Calendar activationCal = Calendar.getInstance(getProtocol().getTimeZone());
        activationCal.setTimeInMillis(Long.parseLong(epoch));
        getProtocol().journal("Writing calendar activation date:"+activationCal.getTime().toString());
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

    /**
     * Not supported in DSMR4.0, subclasses can override
     */
    protected boolean isResume(OfflineDeviceMessage pendingMessage) {
        return false;
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


    protected CollectedMessage writeCaptureDefinition(OfflineDeviceMessage pendingMessage) throws IOException {
        CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
        String captureObjects = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.captureObjectListAttributeName);

        String[] rawCapturedObjectDefinitions = captureObjects.split(";");

        List <String> filteredCaptureObjects = new ArrayList<>();
        for(String capturedObject : rawCapturedObjectDefinitions){
            filteredCaptureObjects.add( normalizeDLMSObjectDefinition(capturedObject));
        }
        if (!filteredCaptureObjects.isEmpty()) {
            ProfileGeneric profileGeneric = null;
            try {
                profileGeneric = getCosemObjectFactory().getProfileGeneric(ESMR50LoadProfileBuilder.DEFINABLE_LOAD_PROFILE);
            } catch (NotInObjectListException e) {
                getProtocol().journal(Level.SEVERE, e.getLocalizedMessage());
            }

            if (profileGeneric == null) {
                getProtocol().journal(Level.SEVERE, "Profile for obis code " + ESMR50LoadProfileBuilder.DEFINABLE_LOAD_PROFILE.toString() + " not found in object list");
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                return collectedMessage;
            }

            Array capturedObjects = new Array();
            for (String capturedObjectDefinition : filteredCaptureObjects) {
                getProtocol().journal("Adding capture object: "+capturedObjectDefinition);
                String[] definitionParts = capturedObjectDefinition.split(",");
                try {
                    int dlmsClassId = Integer.parseInt(definitionParts[0]);
                    ObisCode obisCode = ObisCode.fromString(definitionParts[1]);
                    int attribute = Integer.parseInt(definitionParts[2]);
                    int dataIndex = Integer.parseInt(definitionParts[3]);
                    Structure definition = new Structure();
                    definition.addDataType(new Unsigned16(dlmsClassId));
                    definition.addDataType(OctetString.fromObisCode(obisCode));
                    definition.addDataType(new Integer8(attribute));
                    definition.addDataType(new Unsigned16(dataIndex));
                    capturedObjects.addDataType(definition);

                } catch (Exception e) {
                    getProtocol().journal(Level.SEVERE, e.getMessage());
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                }
            }
            getProtocol().journal("Setting definable profile capture objects");
            profileGeneric.setCaptureObjectsAttr(capturedObjects);
            getProtocol().journal("Successfully set definable load profile capture objects.");
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        } else {
            getProtocol().journal("Parsed an empty list of objects - the list must be in format: {8,0-0:1.0.0.255,2,0};{1,0-0:96.10.2.255,2,0};{3,1-0:1.8.0.255,2,0}...");
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
        }
        return collectedMessage;
    }

    private String normalizeDLMSObjectDefinition(String capturedObject) {
        return capturedObject
                .replace("{", "")
                .replace("}", "")
                .replace("\n", "")
                .replace("\t", "")
                .replace(" ", "")
                .replace(":", ".")
                .replace("-", ".");
    }

    protected CollectedMessage writeCapturePeriod(OfflineDeviceMessage pendingMessage) {
        CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
        ObisCode obisCode = ESMR50LoadProfileBuilder.DEFINABLE_LOAD_PROFILE;
        String messageAttribute = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.capturePeriodAttributeName).getValue();
        Duration duration = Duration.parse(messageAttribute);
        int period  = (int) duration.getSeconds();
        getProtocol().journal("Writing load profile capture period " + messageAttribute + " - parsed as "+period+" seconds");
        try {
            getProtocol().getDlmsSession().getCosemObjectFactory().getProfileGeneric(obisCode).setCapturePeriodAttr(new Unsigned32(period));
            getProtocol().journal("Successfully set definable load profile capture period to " + period);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        } catch (Exception ex){
            getProtocol().journal(Level.SEVERE, "Cannot write load profile capture period to "+period+": "+ ex.getLocalizedMessage());
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
        }
        return collectedMessage;
    }
}