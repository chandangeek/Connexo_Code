package com.energyict.protocolimpl.generic.messages;

import com.energyict.dlms.DLMSUtils;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocols.messaging.LegacyLoadProfileRegisterMessageBuilder;
import com.energyict.protocols.messaging.LegacyPartialLoadProfileMessageBuilder;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Generic messageHandler. The xml-OldDeviceMessage is parsed using the {@link org.xml.sax.helpers.DefaultHandler} and the relative
 * variables are set.
 *
 * @author gna
 */
public class MessageHandler extends DefaultHandler {

    /**
     * Represents the current Message type
     */
    private String type = "";

    /**
     * Helper to indicate whether the OldDeviceMessage content contains xml
     */
    private boolean isXmlInContent = false;

    /**
     * {@inheritDoc}
     */
    public void startElement(String uri, String lName, String qName, Attributes attrbs) throws SAXException {
        if (RtuMessageConstant.XMLCONFIG.equals(qName)) {
            setType(RtuMessageConstant.XMLCONFIG);
            isXmlInContent = true;
        } else if (RtuMessageConstant.FIRMWARE_UPGRADE.equals(qName)) {
            setType(RtuMessageConstant.FIRMWARE_UPGRADE);
            handleFirmWareUpgrade(attrbs);
        } else if (RtuMessageConstant.CHANGE_ADMINISTRATIVE_STATUS.equals(qName)) {
            setType(RtuMessageConstant.CHANGE_ADMINISTRATIVE_STATUS);
            handleAdministrativeStatus(attrbs);
        } else if (RtuMessageConstant.ENABLE_DISCOVERY_ON_POWER_UP.equals(qName)) {
            setType(RtuMessageConstant.ENABLE_DISCOVERY_ON_POWER_UP);
        } else if (RtuMessageConstant.DISABLE_DISCOVERY_ON_POWER_UP.equals(qName)) {
            setType(RtuMessageConstant.DISABLE_DISCOVERY_ON_POWER_UP);
        } else if (RtuMessageConstant.FIRMWARE_UPDATE.equals(qName)) {
            setType(RtuMessageConstant.FIRMWARE_UPDATE);
            isXmlInContent = true;
        } else if (RtuMessageConstant.RF_FIRMWARE_UPGRADE.equals(qName)) {
            setType(RtuMessageConstant.RF_FIRMWARE_UPGRADE);
            handleFirmWareUpgrade(attrbs);
        } else if (RtuMessageConstant.P1CODEMESSAGE.equals(qName)) {
            setType(RtuMessageConstant.P1CODEMESSAGE);
            handleP1Code(attrbs);
        } else if (RtuMessageConstant.P1TEXTMESSAGE.equals(qName)) {
            setType(RtuMessageConstant.P1TEXTMESSAGE);
            handleP1Text(attrbs);
        } else if (RtuMessageConstant.CONNECT_LOAD.equals(qName)) {
            setType(RtuMessageConstant.CONNECT_LOAD);
            handleConnectLoad(attrbs);
        } else if (RtuMessageConstant.DISCONNECT_LOAD.equals(qName)) {
            setType(RtuMessageConstant.DISCONNECT_LOAD);
            handleDisconnectLoad(attrbs);
        } else if (RtuMessageConstant.REMOTE_CONNECT.equals(qName)) {
            setType(RtuMessageConstant.CONNECT_LOAD);
        } else if (RtuMessageConstant.REMOTE_DISCONNECT.equals(qName)) {
            setType(RtuMessageConstant.DISCONNECT_LOAD);
        } else if (RtuMessageConstant.CONNECT_CONTROL_MODE.equals(qName)) {
            setType(RtuMessageConstant.CONNECT_CONTROL_MODE);
            handleConnectControlMode(attrbs);
        } else if (RtuMessageConstant.LOAD_LIMIT_CONFIGURE.equals(qName)) {
            setType(RtuMessageConstant.LOAD_LIMIT_CONFIGURE);
            handleLoadLimitConfiguration(attrbs);
        } else if (RtuMessageConstant.LOAD_LIMIT_DISABLE.equals(qName)) {
            setType(RtuMessageConstant.LOAD_LIMIT_DISABLE);
        } else if (RtuMessageConstant.LOAD_LIMIT_EMERGENCY_PROFILE_GROUP_ID_LIST.equals(qName)) {
            setType(RtuMessageConstant.LOAD_LIMIT_EMERGENCY_PROFILE_GROUP_ID_LIST);
            handleLoadLimitEPGroupIDList(attrbs);
        } else if (RtuMessageConstant.LOAD_LIMIT_EMERGENCY_PROFILE.equals(qName)) {
            handleLoadLimitEmergencyProfile(attrbs);
        } else if (RtuMessageConstant.TOU_ACTIVITY_CAL.equals(qName)) {
            setType(RtuMessageConstant.TOU_ACTIVITY_CAL);
            handleTOUMessage(attrbs);
            isXmlInContent = true;  // for certain protocols (ApolloMeter), we put in the xmlParsed CodeTable
        } else if (RtuMessageConstant.TOU_ACTIVATE_CALENDAR.equals(qName)) {
            setType(RtuMessageConstant.TOU_ACTIVATE_CALENDAR);
            handleTOUActivation(attrbs);
        } else if (RtuMessageConstant.TOU_SPECIAL_DAYS.equals(qName)) {
            setType(RtuMessageConstant.TOU_SPECIAL_DAYS);
            handleSpecialDays(attrbs);
            isXmlInContent = true;  // for certain protocols (ApolloMeter), we put in the xmlParsed CodeTable
        } else if (RtuMessageConstant.TOU_SPECIAL_DAYS_DELETE.equals(qName)) {
            setType(RtuMessageConstant.TOU_SPECIAL_DAYS_DELETE);
            handleSpecialDaysDelete(attrbs);
        } else if (RtuMessageConstant.MBUS_DECOMMISSION.equals(qName)) {
            setType(RtuMessageConstant.MBUS_DECOMMISSION);
        } else if (RtuMessageConstant.MBUS_ENCRYPTION_KEYS.equals(qName)) {
            setType(RtuMessageConstant.MBUS_ENCRYPTION_KEYS);
            handleMbusEncryptionKeys(attrbs);
        } else if (RtuMessageConstant.SET_TIME.equals(qName)) {
            setType(RtuMessageConstant.SET_TIME);
            handleSetTime(attrbs);
        } else if (RtuMessageConstant.ME_MAKING_ENTRIES.equals(qName)) {
            setType(RtuMessageConstant.ME_MAKING_ENTRIES);
            handleMakingEntries(attrbs);
        } else if (RtuMessageConstant.GPRS_MODEM_SETUP.equals(qName)) {
            setType(RtuMessageConstant.GPRS_MODEM_SETUP);
            handleGrpsModemSetup(attrbs);
        } else if (RtuMessageConstant.GPRS_MODEM_CREDENTIALS.equals(qName)) {
            setType(RtuMessageConstant.GPRS_MODEM_CREDENTIALS);
            handleGprsModemCredentials(attrbs);
        } else if (RtuMessageConstant.TEST_MESSAGE.equals(qName)) {
            setType(RtuMessageConstant.TEST_MESSAGE);
            handleTestMessage(attrbs);
        } else if (RtuMessageConstant.TEST_SECURITY_MESSAGE.equals(qName)) {
            setType(RtuMessageConstant.TEST_SECURITY_MESSAGE);
            handleTestMessage(attrbs);
        } else if (RtuMessageConstant.GLOBAL_METER_RESET.equals(qName)) {
            setType(RtuMessageConstant.GLOBAL_METER_RESET);
        } else if (RtuMessageConstant.RESTORE_FACTORY_SETTINGS.equals(qName)) {
            setType(RtuMessageConstant.RESTORE_FACTORY_SETTINGS);
        } else if (RtuMessageConstant.WAKEUP_ADD_WHITELIST.equals(qName)) {
            setType(RtuMessageConstant.WAKEUP_ADD_WHITELIST);
            handleWakeUpWhiteList(attrbs);
        } else if (RtuMessageConstant.WAKEUP_ACTIVATE.equals(qName)) {
            setType(RtuMessageConstant.WAKEUP_ACTIVATE);
        } else if (RtuMessageConstant.WAKEUP_DEACTIVATE.equals(qName)) {
            setType(RtuMessageConstant.WAKEUP_DEACTIVATE);
        } else if (RtuMessageConstant.AEE_CHANGE_GLOBAL_KEY.equals(qName)) {
            setType(RtuMessageConstant.AEE_CHANGE_GLOBAL_KEY);
        } else if (RtuMessageConstant.AEE_CHANGE_AUTHENTICATION_KEY.equals(qName)) {
            setType(RtuMessageConstant.AEE_CHANGE_AUTHENTICATION_KEY);
        } else if (RtuMessageConstant.AEE_ACTIVATE_SECURITY.equals(qName)) {
            setType(RtuMessageConstant.AEE_ACTIVATE_SECURITY);
            handleActivateSecurityLevel(attrbs);
        } else if (RtuMessageConstant.MBUS_CORRECTED_VALUES.equals(qName)) {
            setType(RtuMessageConstant.MBUS_CORRECTED_VALUES);
        } else if (RtuMessageConstant.MBUS_UNCORRECTED_VALUES.equals(qName)) {
            setType(RtuMessageConstant.MBUS_UNCORRECTED_VALUES);
        } else if (RtuMessageConstant.MBUS_INSTALL.equals(qName)) {
            setType(RtuMessageConstant.MBUS_INSTALL);
            handleMbusInstall(attrbs);
        } else if (RtuMessageConstant.AEE_CHANGE_AUTHENTICATION_LEVEL.equals(qName)) {
            setType(RtuMessageConstant.AEE_CHANGE_AUTHENTICATION_LEVEL);
            handleChangeAuthentication(attrbs);
        } else if (RtuMessageConstant.AEE_ENABLE_AUTHENTICATION_LEVEL_P0.equals(qName)) {
            setType(RtuMessageConstant.AEE_ENABLE_AUTHENTICATION_LEVEL_P0);
            handleChangeAuthentication(attrbs);
        } else if (RtuMessageConstant.AEE_DISABLE_AUTHENTICATION_LEVEL_P0.equals(qName)) {
            setType(RtuMessageConstant.AEE_DISABLE_AUTHENTICATION_LEVEL_P0);
            handleChangeAuthentication(attrbs);
        } else if (RtuMessageConstant.AEE_ENABLE_AUTHENTICATION_LEVEL_P3.equals(qName)) {
            setType(RtuMessageConstant.AEE_ENABLE_AUTHENTICATION_LEVEL_P3);
            handleChangeAuthentication(attrbs);
        } else if (RtuMessageConstant.AEE_DISABLE_AUTHENTICATION_LEVEL_P3.equals(qName)) {
            setType(RtuMessageConstant.AEE_DISABLE_AUTHENTICATION_LEVEL_P3);
            handleChangeAuthentication(attrbs);
        } else if (RtuMessageConstant.NTA_AEE_CHANGE_DATATRANSPORT_AUTHENTICATION_KEY.equals(qName)) {
            setType(RtuMessageConstant.NTA_AEE_CHANGE_DATATRANSPORT_AUTHENTICATION_KEY);
            handleChangeAuthenticationKey(attrbs);
        } else if (RtuMessageConstant.NTA_AEE_CHANGE_DATATRANSPORT_ENCRYPTION_KEY.equals(qName)) {
            setType(RtuMessageConstant.NTA_AEE_CHANGE_DATATRANSPORT_ENCRYPTION_KEY);
            handleChangeEncryptionKey(attrbs);
        } else if (RtuMessageConstant.AEE_CHANGE_HLS_SECRET.equals(qName)) {
            setType(RtuMessageConstant.AEE_CHANGE_HLS_SECRET);
            handleChangeHLSSecret(attrbs);
        } else if (RtuMessageConstant.AEE_CHANGE_LLS_SECRET.equals(qName)) {
            setType(RtuMessageConstant.AEE_LLS_SECRET);
            handleChangeLLSSecret(attrbs);
        } else if (RtuMessageConstant.CHANGE_HAN_SAS.equalsIgnoreCase(qName)) {
            setType(RtuMessageConstant.CHANGE_HAN_SAS);
            handleChangeHanSas(attrbs);
        } else if (RtuMessageConstant.CREATE_HAN_NETWORK.equalsIgnoreCase(qName)) {
            setType(RtuMessageConstant.CREATE_HAN_NETWORK);
        } else if (RtuMessageConstant.REMOVE_HAN_NETWORK.equalsIgnoreCase(qName)) {
            setType(RtuMessageConstant.REMOVE_HAN_NETWORK);
        } else if (RtuMessageConstant.JOIN_ZIGBEE_SLAVE.equalsIgnoreCase(qName)) {
            setType(RtuMessageConstant.JOIN_ZIGBEE_SLAVE);
            handleJoinZigBeeSlave(attrbs);
        } else if (RtuMessageConstant.JOIN_ZIGBEE_SLAVE_FROM_DEVICE_TYPE.equalsIgnoreCase(qName)) {
            setType(RtuMessageConstant.JOIN_ZIGBEE_SLAVE_FROM_DEVICE_TYPE);
            handleJoinZigBeeSlaveFromDeviceType(attrbs);
        } else if (RtuMessageConstant.REMOVE_ZIGBEE_SLAVE.equalsIgnoreCase(qName)) {
            setType(RtuMessageConstant.REMOVE_ZIGBEE_SLAVE);
            handleRemoveZigBeeSlave(attrbs);
        } else if (RtuMessageConstant.REMOVE_ZIGBEE_MIRROR.equalsIgnoreCase(qName)) {
            setType(RtuMessageConstant.REMOVE_ZIGBEE_MIRROR);
            handleRemoveZigBeeMirror(attrbs);
        } else if (RtuMessageConstant.REMOVE_ALL_ZIGBEE_SLAVES.equalsIgnoreCase(qName)) {
            setType(RtuMessageConstant.REMOVE_ALL_ZIGBEE_SLAVES);
        } else if (RtuMessageConstant.BACKUP_ZIGBEE_HAN_PARAMETERS.equalsIgnoreCase(qName)) {
            setType(RtuMessageConstant.BACKUP_ZIGBEE_HAN_PARAMETERS);
        } else if (RtuMessageConstant.RESTORE_ZIGBEE_HAN_PARAMETERS.equalsIgnoreCase(qName)) {
            setType(RtuMessageConstant.RESTORE_ZIGBEE_HAN_PARAMETERS);
            handleRestoreHANParameters(attrbs);
        } else if (RtuMessageConstant.UPDATE_HAN_LINK_KEY.equalsIgnoreCase(qName)) {
            setType(RtuMessageConstant.UPDATE_HAN_LINK_KEY);
            handleUpdateHANLinkKeyParameters(attrbs);
        } else if (RtuMessageConstant.ZIGBEE_NCP_FIRMWARE_UPGRADE.equals(qName)) {
            setType(RtuMessageConstant.ZIGBEE_NCP_FIRMWARE_UPGRADE);
            handleZigbeeNCPFirmwareUpgradeParameters(attrbs);
        } else if (RtuMessageConstant.CHANGE_OF_TENANT.equalsIgnoreCase(qName)) {
            setType(RtuMessageConstant.CHANGE_OF_TENANT);
            handleChangeOfTenantParameters(attrbs);
        } else if (RtuMessageConstant.READ_ZIGBEE_STATUS.equalsIgnoreCase(qName)) {
            setType(RtuMessageConstant.READ_ZIGBEE_STATUS);
        } else if (RtuMessageConstant.CHANGE_OF_SUPPLIER.equalsIgnoreCase(qName)) {
            setType(RtuMessageConstant.CHANGE_OF_SUPPLIER);
            handleChangeOfSupplierParameters(attrbs);
        } else if (LegacyPartialLoadProfileMessageBuilder.getMessageNodeTag().equalsIgnoreCase(qName)) {
            setType(LegacyPartialLoadProfileMessageBuilder.getMessageNodeTag());
            isXmlInContent = true;
        } else if (LegacyLoadProfileRegisterMessageBuilder.getMessageNodeTag().equalsIgnoreCase(qName)) {
            setType(LegacyLoadProfileRegisterMessageBuilder.getMessageNodeTag());
            isXmlInContent = true;
        } else if (RtuMessageConstant.CHANGE_DEFAULT_RESET_WINDOW.equalsIgnoreCase(qName)) {
            setType(RtuMessageConstant.CHANGE_DEFAULT_RESET_WINDOW);
            handleChangeOfDefaultResetWindowParameters(attrbs);
        } else if (RtuMessageConstant.GPRS_MODEM_PING_SETUP.equalsIgnoreCase(qName)) {
            setType(RtuMessageConstant.GPRS_MODEM_PING_SETUP);
            handleGPRSModemPingSetup(attrbs);
        } else if (RtuMessageConstant.CONNECTION_MODE.equalsIgnoreCase(qName)) {
            setType(RtuMessageConstant.CONNECTION_MODE);
            handleGPRSConnectionModeParameters(attrbs);
        } else if (RtuMessageConstant.WAKEUP_PARAMETERS.equalsIgnoreCase(qName)) {
            setType(RtuMessageConstant.WAKEUP_PARAMETERS);
            handeGPRSWakeupParameters(attrbs);
        } else if (RtuMessageConstant.PREFERRED_NETWORK_OPERATORS_LIST.equalsIgnoreCase(qName)) {
            setType(RtuMessageConstant.PREFERRED_NETWORK_OPERATORS_LIST);
            handePreferredNetworkOperatorsListParameters(attrbs);
        } else {
            if (!isXmlInContent) { // If there is XML in the content, then the protocol will parse it himself ...
                throw new SAXException("Unknown messageContent : " + qName);
            }
        }
    }

    /**
     * Setter for the {@link MessageHandler#type}
     *
     * @param type - the message
     */
    protected void setType(String type) {
        this.type = type;
    }

    /**
     * Getter fo the {@link MessageHandler#type}
     *
     * @return
     */
    public String getType() {
        return this.type;
    }

    private int administrativeStatus;

    private void handleAdministrativeStatus(Attributes attrbs) {
        this.administrativeStatus = Integer.parseInt(attrbs.getValue(RtuMessageConstant.ADMINISTRATIVE_STATUS).trim());
    }

    public int getAdministrativeStatus() {
        return administrativeStatus;
    }

	/* FirmwareUpload Related messages
    /**********************************************/


    private String firmwareContent;
    private String activationDate;
    private String imageIdentifier;

    private void handleFirmWareUpgrade(Attributes attrbs) {
        this.firmwareContent = attrbs.getValue(RtuMessageConstant.FIRMWARE_CONTENT);
        this.activationDate = ProtocolTools.getEpochTimeFromString(attrbs.getValue(RtuMessageConstant.FIRMWARE_ACTIVATE_DATE));
        this.imageIdentifier = attrbs.getValue(RtuMessageConstant.FIRMWARE_IMAGE_IDENTIFIER);
    }

    public String getImageIdentifier() {
        return imageIdentifier;
    }

    public String getFirmwareContent() {
        return this.firmwareContent;
    }

    public String getActivationDate() {
        if (this.activationDate == null) {
            this.activationDate = "";
        }
        return this.activationDate;
    }

/* P1 port Related messages
    /**********************************************/


    private String code;
    private String text;

    private void handleP1Code(Attributes attrbs) {
        this.code = attrbs.getValue(RtuMessageConstant.P1CODE);
    }

    private void handleP1Text(Attributes attrbs) {
        this.text = attrbs.getValue(RtuMessageConstant.P1TEXT);
    }

    public String getP1Code() {
        return this.code;
    }

    public String getP1Text() {
        return this.text;
    }

	/* Disconnect Control Related messages
    /**********************************************/

    private String connectDate;
    private String disconnectDate;
    private String mode;
    private String outputId;

    private void handleConnectLoad(Attributes attrbs) {
        this.connectDate = ProtocolTools.getEpochTimeFromString(attrbs.getValue(RtuMessageConstant.DISCONNECT_CONTROL_ACTIVATE_DATE));
        this.outputId = attrbs.getValue(RtuMessageConstant.DISCONNECTOR_OUTPUT_ID);
    }

    private void handleDisconnectLoad(Attributes attrbs) {
        this.disconnectDate = ProtocolTools.getEpochTimeFromString(attrbs.getValue(RtuMessageConstant.DISCONNECT_CONTROL_ACTIVATE_DATE));
        this.outputId = attrbs.getValue(RtuMessageConstant.DISCONNECTOR_OUTPUT_ID);
    }

    private void handleConnectControlMode(Attributes attrbs) {
        this.mode = attrbs.getValue(RtuMessageConstant.CONNECT_MODE);
        this.outputId = attrbs.getValue(RtuMessageConstant.DISCONNECTOR_OUTPUT_ID);
    }

    public String getConnectControlMode() {
        return this.mode;
    }

    public String getOutputId() {
        if (this.outputId == null) {
            outputId = "";
        }
        return outputId;
    }

    public String getConnectDate() {
        if (this.connectDate == null) {
            this.connectDate = "";
        }
        return this.connectDate;
    }

    public String getDisconnectDate() {
        if (this.disconnectDate == null) {
            this.disconnectDate = "";
        }
        return this.disconnectDate;
    }

	/* LoadLimit Related messages
    /**********************************************/


    private String normalThreshold = "";
    private String emergencyThreshold = "";
    private String overThresholdDurtion = "";
    private String epProfileId = "";
    private String epActivationTime = "";
    private String epDuration = "";
    private String epGroupIdListLookupTableId = "";

    private void handleLoadLimitEPGroupIDList(Attributes attrbs) {
        this.epGroupIdListLookupTableId = attrbs.getValue(RtuMessageConstant.LOAD_LIMIT_EP_GRID_LOOKUP_ID);

    }

    private void handleLoadLimitConfiguration(Attributes attrbs) {
        this.normalThreshold = attrbs.getValue(RtuMessageConstant.LOAD_LIMIT_NORMAL_THRESHOLD);
        this.emergencyThreshold = attrbs.getValue(RtuMessageConstant.LOAD_LIMIT_EMERGENCY_THRESHOLD);
        this.overThresholdDurtion = attrbs.getValue(RtuMessageConstant.LOAD_LIMIT_MIN_OVER_THRESHOLD_DURATION);
    }

    private void handleLoadLimitEmergencyProfile(Attributes attrbs) {
        this.epProfileId = attrbs.getValue(RtuMessageConstant.LOAD_LIMIT_EP_PROFILE_ID);
        this.epActivationTime = ProtocolTools.getEpochTimeFromString(attrbs.getValue(RtuMessageConstant.LOAD_LIMIT_EP_ACTIVATION_TIME));
        this.epDuration = attrbs.getValue(RtuMessageConstant.LOAD_LIMIT_EP_DURATION);
    }

    public String getNormalThreshold() {
        return normalThreshold;
    }

    public String getEmergencyThreshold() {
        return emergencyThreshold;
    }

    public String getOverThresholdDurtion() {
        return overThresholdDurtion;
    }

    public String getEpProfileId() {
        return epProfileId;
    }

    public String getEpActivationTime() {
        return epActivationTime;
    }

    public String getEpDuration() {
        return epDuration;
    }

    public String getEpGroupIdListLookupTableId() {
        return epGroupIdListLookupTableId;
    }

	/* Activity Calendar Related messages
    /**********************************************/


    private String touActivationDate = "";
    private String touCalendarName = "";
    private String touCodeTable = "";
    private String touUserFile = "";
    private String touSpecialDaysCodeTable = "";
    private String deleteEntry = "";

    private void handleTOUMessage(Attributes attrbs) {
        this.touActivationDate = ProtocolTools.getEpochTimeFromString(attrbs.getValue(RtuMessageConstant.TOU_ACTIVITY_DATE));
        this.touCalendarName = attrbs.getValue(RtuMessageConstant.TOU_ACTIVITY_NAME);
        this.touCodeTable = attrbs.getValue(RtuMessageConstant.TOU_ACTIVITY_CODE_TABLE);
        this.touUserFile = attrbs.getValue(RtuMessageConstant.TOU_ACTIVITY_USER_FILE);
    }

    private void handleTOUActivation(Attributes attrbs) {
        this.touActivationDate = ProtocolTools.getEpochTimeFromString(attrbs.getValue(RtuMessageConstant.TOU_ACTIVITY_DATE));
    }

    private void handleSpecialDays(Attributes attrbs) {
        this.touSpecialDaysCodeTable = attrbs.getValue(RtuMessageConstant.TOU_SPECIAL_DAYS_CODE_TABLE);
    }

    private void handleSpecialDaysDelete(Attributes attrbs) {
        this.deleteEntry = attrbs.getValue(RtuMessageConstant.TOU_SPECIAL_DAYS_DELETE_ENTRY);
    }

    public String getTOUActivationDate() {
        return this.touActivationDate;
    }

    public String getTOUCalendarName() {
        return this.touCalendarName;
    }

    public String getTOUCodeTable() {
        return this.touCodeTable;
    }

    public String getTOUUserFile() {
        return this.touUserFile;
    }

    public String getSpecialDaysCodeTable() {
        return this.touSpecialDaysCodeTable;
    }

    public String getSpecialDayDeleteEntry() {
        return this.deleteEntry;
    }

	/* Mbus encryption keys Related messages
     /**********************************************/


    private String openKey = "";
    private String transferKey = "";

    private void handleMbusEncryptionKeys(Attributes attrbs) {
        this.openKey = attrbs.getValue(RtuMessageConstant.MBUS_OPEN_KEY);
        this.transferKey = attrbs.getValue(RtuMessageConstant.MBUS_TRANSFER_KEY);
    }

    public String getOpenKey() {
        return this.openKey;
    }

    public String getTransferKey() {
        return this.transferKey;
    }

	/* SetTime Related messages
    /**********************************************/


    private String epochTime = "";

    private void handleSetTime(Attributes attrbs) {
        this.epochTime = ProtocolTools.getEpochTimeFromString(attrbs.getValue(RtuMessageConstant.SET_TIME_VALUE));
    }

    public String getEpochTime() {
        return this.epochTime;
    }

	/* Making entries Related messages
    /***********************************************/


    private String startDate = "";
    private String entries = "";
    private String interval = "";
    private String syncClock = "";

    private void handleMakingEntries(Attributes attrbs) {
        this.startDate = ProtocolTools.getEpochTimeFromString(attrbs.getValue(RtuMessageConstant.ME_START_DATE));
        this.entries = attrbs.getValue(RtuMessageConstant.ME_NUMBER_OF_ENTRIES);
        this.interval = attrbs.getValue(RtuMessageConstant.ME_INTERVAL);
        this.syncClock = attrbs.getValue(RtuMessageConstant.ME_SET_CLOCK_BACK);
    }

    public String getMEStartDate() {
        return this.startDate;
    }

    public int getMEEntries() throws IOException {
        try {
            return Integer.parseInt(this.entries);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new IOException("Number of entries does not contain a non numeric value: " + this.entries);
        }
    }

    public String getMEInterval() throws IOException {
        if (!this.interval.equalsIgnoreCase("15") && !this.interval.equalsIgnoreCase("day") && !this.interval.equalsIgnoreCase("month")) {
            throw new IOException("Only '15 - day - month' is alowed in the interval field. (value: " + this.interval);
        }
        return this.interval;
    }

    public boolean getMESyncAtEnd() {
        if (this.syncClock != null) {
            return !this.syncClock.equalsIgnoreCase("0");
        } else {
            return false;
        }
    }

	/* Changing GPRS modem parameters Related messages
    /***********************************************/


    private String gprsApn = "";
    private String gprsUsername = "";
    private String gprsPassword = "";

    private void handleGrpsModemSetup(Attributes attrbs) {
        this.gprsApn = attrbs.getValue(RtuMessageConstant.GPRS_APN);
        this.gprsUsername = attrbs.getValue(RtuMessageConstant.GPRS_USERNAME);
        this.gprsPassword = attrbs.getValue(RtuMessageConstant.GPRS_PASSWORD);
    }

    private void handleGprsModemCredentials(Attributes attrbs) {
        this.gprsUsername = attrbs.getValue(RtuMessageConstant.GPRS_USERNAME);
        this.gprsPassword = attrbs.getValue(RtuMessageConstant.GPRS_PASSWORD);
    }

    public String getGprsApn() {
        if (this.gprsApn != null) {
            return this.gprsApn;
        } else {
            return "";
        }
    }

    public String getGprsUsername() {
        if (this.gprsUsername != null) {
            return this.gprsUsername;
        } else {
            return "";
        }
    }

    public String getGprsPassword() {
        if (this.gprsPassword != null) {
            return this.gprsPassword;
        } else {
            return "";
        }
    }

	/* Handle TestMessage Related messages
	/***********************************************/


    private String ufId = "";

    private void handleTestMessage(Attributes attrbs) {
        this.ufId = attrbs.getValue(RtuMessageConstant.TEST_FILE);
    }

    public String getTestUserFileId() {
        return (this.ufId != null) ? this.ufId : "";
    }

	/* WakeUp functionality Related messages
	/**********************************************/


    private String nr1 = "";
    private String nr2 = "";
    private String nr3 = "";
    private String nr4 = "";
    private String nr5 = "";

    private void handleWakeUpWhiteList(Attributes attrbs) {
        this.nr1 = attrbs.getValue(RtuMessageConstant.WAKEUP_NR1);
        this.nr2 = attrbs.getValue(RtuMessageConstant.WAKEUP_NR2);
        this.nr3 = attrbs.getValue(RtuMessageConstant.WAKEUP_NR3);
        this.nr4 = attrbs.getValue(RtuMessageConstant.WAKEUP_NR4);
        this.nr5 = attrbs.getValue(RtuMessageConstant.WAKEUP_NR5);
    }

    public String getNr1() {
        return (this.nr1 != null) ? this.nr1 : "";
    }

    public String getNr2() {
        return (this.nr2 != null) ? this.nr2 : "";
    }

    public String getNr3() {
        return (this.nr3 != null) ? this.nr3 : "";
    }

    public String getNr4() {
        return (this.nr4 != null) ? this.nr4 : "";
    }

    public String getNr5() {
        return (this.nr5 != null) ? this.nr5 : "";
    }

	/* Authentication and Encryption functionality Related messages
	/***********************************************/


    private String securityLevel = "";

    private void handleActivateSecurityLevel(Attributes attrbs) {
        this.securityLevel = attrbs.getValue(RtuMessageConstant.AEE_SECURITYLEVEL);
    }

    public int getSecurityLevel() {
        return Integer.parseInt(this.securityLevel);
    }

    /* Change the authenticationLevel */

    private String authenticationLevel = "";

    private void handleChangeAuthentication(Attributes attrbs) {
        this.authenticationLevel = attrbs.getValue(RtuMessageConstant.AEE_AUTHENTICATIONLEVEL);
    }

    /**
     * Return the authenticationLevel the user gave in.
     * If the value is not a number, then return -1
     *
     * @return the value the user gave in
     */
    public int getAuthenticationLevel() {
        try {
            return Integer.parseInt(this.authenticationLevel);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /* Change the authentication key */

    private byte[] wrappedAuthenticationKey = new byte[0];
    private byte[] plainAuthenticationKey = new byte[0];

    protected void handleChangeAuthenticationKey(Attributes attrbs) {
        this.wrappedAuthenticationKey = DLMSUtils.hexStringToByteArray(attrbs.getValue(RtuMessageConstant.AEE_NEW_AUTHENTICATION_KEY));
        if (attrbs.getValue(RtuMessageConstant.AEE_PLAIN_NEW_AUTHENTICATION_KEY) != null) {
            this.plainAuthenticationKey = DLMSUtils.hexStringToByteArray(attrbs.getValue(RtuMessageConstant.AEE_PLAIN_NEW_AUTHENTICATION_KEY));
        }
    }

    public byte[] getNewAuthenticationKey() {
        return wrappedAuthenticationKey;
    }

    public byte[] getPlainAuthenticationKey() {
        return plainAuthenticationKey;
    }

    /* Change the encryption key */

    private byte[] wrappedEncryptionKey = new byte[0];
    private byte[] plainEncryptionKey = new byte[0];

    protected void handleChangeEncryptionKey(Attributes attrbs) {
        this.wrappedEncryptionKey = DLMSUtils.hexStringToByteArray(attrbs.getValue(RtuMessageConstant.AEE_NEW_ENCRYPTION_KEY));
        if (attrbs.getValue(RtuMessageConstant.AEE_PLAIN_NEW_ENCRYPTION_KEY) != null) {
            this.plainEncryptionKey = DLMSUtils.hexStringToByteArray(attrbs.getValue(RtuMessageConstant.AEE_PLAIN_NEW_ENCRYPTION_KEY));
        }
    }

    public byte[] getNewEncryptionKey() {
        return wrappedEncryptionKey;
    }

    public byte[] getPlainEncryptionKey() {
        return plainEncryptionKey;
    }

    /* Change the HLS secret */

    private byte[] hlsSecret = new byte[0];

    protected void handleChangeHLSSecret(Attributes attrbs) {
        this.hlsSecret = DLMSUtils.hexStringToByteArray(attrbs.getValue(RtuMessageConstant.AEE_HLS_SECRET));
    }

    public byte[] getHLSSecret() {
        return hlsSecret;
    }

    /* Change the LLS secret */

    private byte[] llsSecret = new byte[0];

    protected void handleChangeLLSSecret(Attributes attrbs) {
        this.llsSecret = DLMSUtils.hexStringToByteArray(attrbs.getValue(RtuMessageConstant.AEE_LLS_SECRET));
    }

    public byte[] getLLSSecret() {
        return llsSecret;
    }

/* Mbus installation related messages
    */

    private String mbusEquipmentId = "";
    private String mbusChannelToInstall = "";
    private String mbusEncryptionKey = "";

    private void handleMbusInstall(Attributes attrbs) {
        this.mbusEquipmentId = attrbs.getValue(RtuMessageConstant.MBUS_EQUIPMENT_ID);
        this.mbusChannelToInstall = attrbs.getValue(RtuMessageConstant.MBUS_INSTALL_CHANNEL);
        this.mbusEncryptionKey = attrbs.getValue(RtuMessageConstant.MBUS_DEFAULT_ENCRYPTION_KEY);
    }

    /**
     * Getter for the MbusInstall EquipmentIdentifier (for the AM100 this is the RF-address of the IZAR module)
     *
     * @return the equipmentId the user gave in
     */
    public String getMbusInstallEquipmentId() {
        return mbusEquipmentId;
    }

    /**
     * Getter for the MbusInstall channel
     *
     * @return the channel the user gave in
     */
    public int getMbusInstallChannel() {
        return Integer.parseInt(mbusChannelToInstall);
    }

    /**
     * Getter for the MbusInstall Encryption Key
     *
     * @return the encryption Key the user gave in
     */
    public String getMbusInstallEncryptionKey() {
        return mbusEncryptionKey;
    }

    private String joinZigBeeIEEEAddress = "";
    private String joinZigBeeLinkKey = "";
    private String joinZigBeeDeviceType = "";

    private void handleJoinZigBeeSlave(Attributes attrbs) {
        this.joinZigBeeIEEEAddress = attrbs.getValue(RtuMessageConstant.JOIN_ZIGBEE_SLAVE_IEEE_ADDRESS);
        this.joinZigBeeLinkKey = attrbs.getValue(RtuMessageConstant.JOIN_ZIGBEE_SLAVE_LINK_KEY);
    }

    private void handleJoinZigBeeSlaveFromDeviceType(Attributes attrbs) {
        this.joinZigBeeIEEEAddress = attrbs.getValue(RtuMessageConstant.JOIN_ZIGBEE_SLAVE_IEEE_ADDRESS);
        this.joinZigBeeLinkKey = attrbs.getValue(RtuMessageConstant.JOIN_ZIGBEE_SLAVE_LINK_KEY);
        this.joinZigBeeDeviceType = attrbs.getValue(RtuMessageConstant.JOIN_ZIGBEE_SLAVE_DEVICE_TYPE);
    }

    public String getJoinZigBeeIEEEAddress() {
        return joinZigBeeIEEEAddress;
    }

    public String getJoinZigBeeLinkKey() {
        return joinZigBeeLinkKey;
    }

    public String getJoinZigBeeDeviceType() {
        return joinZigBeeDeviceType;
    }

    private String removeZigBeeIEEEAddress = "";

    private void handleRemoveZigBeeSlave(Attributes attrbs) {
        this.removeZigBeeIEEEAddress = attrbs.getValue(RtuMessageConstant.REMOVE_ZIGBEE_SLAVE_IEEE_ADDRESS);
    }

    public String getRemoveZigBeeIEEEAddress() {
        return removeZigBeeIEEEAddress;
    }

    private String forceRemovalZigBeeMirror = "";

    private void handleRemoveZigBeeMirror(Attributes attrbs) {
        this.removeZigBeeIEEEAddress = attrbs.getValue(RtuMessageConstant.REMOVE_ZIGBEE_MIRROR_IEEE_ADDRESS);
        this.forceRemovalZigBeeMirror = attrbs.getValue(RtuMessageConstant.REMOVE_ZIGBEE_MIRROR_FORCE);
    }

    public String getForceRemovalZigBeeMirror() {
        return forceRemovalZigBeeMirror;
    }

    private String restoreHanParametersUserFileID = "";

    private void handleRestoreHANParameters(final Attributes attrbs) {
        this.restoreHanParametersUserFileID = attrbs.getValue(RtuMessageConstant.RESTORE_ZIGBEE_PARAMETERS_USERFILE_ID);
    }

    public int getRestoreHanParametersUserFileId() {
        try {
            return Integer.valueOf(this.restoreHanParametersUserFileID);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String updateHanLinkKeyZigBeeIEEEAddress = "";

    private void handleUpdateHANLinkKeyParameters(Attributes attrbs) {
        this.updateHanLinkKeyZigBeeIEEEAddress = attrbs.getValue(RtuMessageConstant.UPDATE_HAN_LINK_KEY_SLAVE_IEEE_ADDRESS);
    }

    public String getUpdateHanLinkKeyZigBeeIEEEAddress() {
        return updateHanLinkKeyZigBeeIEEEAddress;
    }

    private String zigbeeNCPFirmwareUpgradeUserFileID = "";

    private void handleZigbeeNCPFirmwareUpgradeParameters(final Attributes attrbs) {
        this.zigbeeNCPFirmwareUpgradeUserFileID = attrbs.getValue(RtuMessageConstant.ZIGBEE_NCP_FIRMWARE_USERFILE_ID);
        if (this.zigbeeNCPFirmwareUpgradeUserFileID == null) {
            this.zigbeeNCPFirmwareUpgradeUserFileID = attrbs.getValue(RtuMessageConstant.ZIGBEE_NCP_FIRMWARE_FILE_ID);
        }
    }

    public int getZigbeeNCPFirmwareUpgradeUserFileId() {
        try {
            return Integer.valueOf(this.zigbeeNCPFirmwareUpgradeUserFileID);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String changeHanSasExtendedPanId = "";
    private String changeHanSasPanId = "";
    private String changeHanSasChannel = "";
    private String changeHanSasInsecureJoin = "";

    private void handleChangeHanSas(Attributes attrbs) {
        this.changeHanSasExtendedPanId = attrbs.getValue(RtuMessageConstant.HAN_SAS_EXTENDED_PAN_ID);
        this.changeHanSasPanId = attrbs.getValue(RtuMessageConstant.HAN_SAS_PAN_ID);
        this.changeHanSasChannel = attrbs.getValue(RtuMessageConstant.HAN_SAS_CHANNEL);
        this.changeHanSasInsecureJoin = attrbs.getValue(RtuMessageConstant.HAN_SAS_INSECURE_JOIN);
    }

    public String getChangeHanSasExtendedPanId() {
        return changeHanSasExtendedPanId;
    }

    public String getChangeHanSasPanId() {
        return changeHanSasPanId;
    }

    public String getChangeHanSasChannel() {
        return changeHanSasChannel;
    }

    public String getChangeHanSasInsecureJoin() {
        return changeHanSasInsecureJoin;
    }


    private String tenantValue = "";
    private String tenantActivationDate = "";

    private void handleChangeOfTenantParameters(final Attributes attrbs) {
        this.tenantValue = attrbs.getValue(RtuMessageConstant.CHANGE_OF_TENANT_VALUE);
        this.tenantActivationDate = attrbs.getValue(RtuMessageConstant.CHANGE_OF_TENANT_ACTIATION_DATE);
    }

    public String getTenantValue() {
        return tenantValue;
    }

    public String getTenantActivationDate() {
        return tenantActivationDate;
    }


    private String supplierName = "";
    private String supplierId = "";
    private String supplierActivationDate = "";

    private void handleChangeOfSupplierParameters(final Attributes attrbs) {
        this.supplierName = attrbs.getValue(RtuMessageConstant.CHANGE_OF_SUPPLIER_NAME);
        this.supplierId = attrbs.getValue(RtuMessageConstant.CHANGE_OF_SUPPLIER_ID);
        this.supplierActivationDate = attrbs.getValue(RtuMessageConstant.CHANGE_OF_SUPPLIER_ACTIATION_DATE);
    }

    public String getSupplierName() {
        return supplierName;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public String getSupplierActivationDate() {
        return supplierActivationDate;
    }

    private String logbookFromTimeString = "";
    private String logbookToTimeString = "";

    protected void handleLogbookParameters(final Attributes attrbs) {
        logbookFromTimeString = attrbs.getValue(RtuMessageConstant.LOGBOOK_FROM);
        logbookToTimeString = attrbs.getValue(RtuMessageConstant.LOGBOOK_TO);
    }

    public String getLogbookToTimeString() {
        return logbookToTimeString;
    }

    public String getLogbookFromTimeString() {
        return logbookFromTimeString;
    }

    private int pingInterval = 0;
    private String pingIP = "";

    private void handleGPRSModemPingSetup(final Attributes attrbs) {
        try {
            String pingIntervalStr = attrbs.getValue(RtuMessageConstant.PING_INTERVAl);
            this.pingInterval = Integer.parseInt(pingIntervalStr);
        } catch (NumberFormatException e) {
            this.pingInterval = -1;
        }

        this.pingIP = attrbs.getValue(RtuMessageConstant.PING_IP);
    }

    public String getPingIP() {
        return pingIP;
    }

    public int getPingInterval() {
        return pingInterval;
    }

    private int connectionMode = 0;

    private void handleGPRSConnectionModeParameters(final Attributes attrbs) {
        try {
            String modeStr = attrbs.getValue(RtuMessageConstant.CONNECT_MODE);
            this.connectionMode = Integer.parseInt(modeStr);
        } catch (NumberFormatException e) {
            this.connectionMode = -1;
        }
    }

    public int getGPRSConnectionMode() {
        return connectionMode;
    }

    private int wakeupCallingWindowLength = 0;
    private int wakeupIdleTimeout = 0;

    private void handeGPRSWakeupParameters(final Attributes attrbs) {
        try {
            String callingWindowLengthStr = attrbs.getValue(RtuMessageConstant.WAKEUP_CALLING_WINDOW_LENGTH);
            this.wakeupCallingWindowLength = Integer.parseInt(callingWindowLengthStr);
        } catch (NumberFormatException e) {
            this.wakeupCallingWindowLength = -1;
        }
        try {
            String idleTimeoutStr = attrbs.getValue(RtuMessageConstant.WAKEUP_IDLE_TIMEOUT);
            this.wakeupIdleTimeout = Integer.parseInt(idleTimeoutStr);
        } catch (NumberFormatException e) {
            this.wakeupIdleTimeout = -1;
        }
    }

    public int getWakeupCallingWindowLength() {
        return wakeupCallingWindowLength;
    }

    public int getWakeupIdleTimeout() {
        return wakeupIdleTimeout;
    }

    List<String> preferredNetworkOperators = new ArrayList<String>();

    private void handePreferredNetworkOperatorsListParameters(final Attributes attrbs) {
        for (int i = 1; i < 11; i++) {
            String networkOperatorStr = attrbs.getValue(RtuMessageConstant.NETWORK_OPERATOR + "_" + i);
            if (networkOperatorStr != null) {
                preferredNetworkOperators.add(networkOperatorStr);
            }
        }
    }

    public List<String> getPreferredNetworkOperators() {
        return preferredNetworkOperators;
    }

    private int defaultResetWindow = 0;

    private void handleChangeOfDefaultResetWindowParameters(final Attributes attrbs) throws SAXException {
        String defaultResetWindowStr = attrbs.getValue(RtuMessageConstant.CHANGE_DEFAULT_RESET_WINDOW);
        try {
            defaultResetWindow = Integer.parseInt(defaultResetWindowStr);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new SAXException("Default reset window attribute contains a non numeric value: " + defaultResetWindowStr);
        }
    }

    public int getDefaultResetWindow() {
        return defaultResetWindow;
    }
}
