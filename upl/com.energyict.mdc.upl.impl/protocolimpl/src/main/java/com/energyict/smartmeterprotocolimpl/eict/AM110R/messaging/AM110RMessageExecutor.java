package com.energyict.smartmeterprotocolimpl.eict.AM110R.messaging;

import com.energyict.cbo.BusinessException;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.attributeobjects.EnhancedCreditRegisterZigbeeDeviceData;
import com.energyict.dlms.cosem.attributeobjects.ZigBeeIEEEAddress;
import com.energyict.mdw.core.*;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.mdw.shadow.UserFileShadow;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageResult;
import com.energyict.protocolimpl.base.Base64EncoderDecoder;
import com.energyict.protocolimpl.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.generic.MessageParser;
import com.energyict.protocolimpl.generic.messages.GenericMessaging;
import com.energyict.protocolimpl.generic.messages.MessageHandler;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.AM110R;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.AM110RProperties;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.AM110RRegisterFactory;
import com.energyict.smartmeterprotocolimpl.eict.NTAMessageHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.energyict.protocolimpl.utils.ProtocolTools.getBytesFromHexString;

/**
 * Copyrights EnergyICT
 * Date: 20-jul-2011
 * Time: 17:15:18
 */
public class AM110RMessageExecutor extends MessageParser {

    private static final String NORESUME = "noresume";

    private final AbstractSmartDlmsProtocol protocol;

    public AM110RMessageExecutor(final AbstractSmartDlmsProtocol protocol) {
        this.protocol = protocol;
    }

    private CosemObjectFactory getCosemObjectFactory() {
        return getDlmsSession().getCosemObjectFactory();
    }

    private DlmsSession getDlmsSession() {
        return getProtocol().getDlmsSession();
    }

    public AbstractSmartDlmsProtocol getProtocol() {
        return this.protocol;
    }

    public MessageResult executeMessageEntry(final MessageEntry messageEntry) {
        String content = messageEntry.getContent();
        String trackingId = messageEntry.getTrackingId();
        MessageHandler messageHandler = new NTAMessageHandler();
        boolean success = true;
        try {

            boolean zigbeeNCPFirmwareUpgrade = ((content != null) && content.contains(RtuMessageConstant.ZIGBEE_NCP_FIRMWARE_UPGRADE));
            boolean firmwareUpdate = ((content != null) && content.contains(RtuMessageConstant.FIRMWARE_UPGRADE) && !zigbeeNCPFirmwareUpgrade);

            if (!firmwareUpdate && !zigbeeNCPFirmwareUpgrade) {
                importMessage(content, messageHandler);
            }

            boolean createHan = messageHandler.getType().equals(RtuMessageConstant.CREATE_HAN_NETWORK);
            boolean removeHan = messageHandler.getType().equals(RtuMessageConstant.REMOVE_HAN_NETWORK);
            boolean joinZigBeeSlave = messageHandler.getType().equals(RtuMessageConstant.JOIN_ZIGBEE_SLAVE_FROM_DEVICE_TYPE);
            boolean removeZigBeeMirror = messageHandler.getType().equals(RtuMessageConstant.REMOVE_ZIGBEE_MIRROR);
            boolean removeZigBeeSlave = messageHandler.getType().equals(RtuMessageConstant.REMOVE_ZIGBEE_SLAVE);
            boolean removeAllZigBeeSlaves = messageHandler.getType().equals(RtuMessageConstant.REMOVE_ALL_ZIGBEE_SLAVES);
            boolean backupZigBeeHanParameters = messageHandler.getType().equals(RtuMessageConstant.BACKUP_ZIGBEE_HAN_PARAMETERS);
            boolean restoreZigBeeParameters = messageHandler.getType().equals(RtuMessageConstant.RESTORE_ZIGBEE_HAN_PARAMETERS);
            boolean updateHANLinkKey = messageHandler.getType().equals(RtuMessageConstant.UPDATE_HAN_LINK_KEY);
            boolean readDebugLogbook = messageHandler.getType().equals(RtuMessageConstant.DEBUG_LOGBOOK);
            boolean readElsterLogbook = messageHandler.getType().equals(RtuMessageConstant.ELSTER_SPECIFIC_LOGBOOK);
            boolean configureConnectionMode = messageHandler.getType().equals(RtuMessageConstant.CONNECTION_MODE);
            boolean configureWakeupParameters = messageHandler.getType().equals(RtuMessageConstant.WAKEUP_PARAMETERS);
            boolean configurePreferredNetworkOperatorsList = messageHandler.getType().equals(RtuMessageConstant.PREFERRED_NETWORK_OPERATORS_LIST);
            boolean enableWebserver = messageHandler.getType().equals(RtuMessageConstant.WEBSERVER_ENABLE);
            boolean disableWebserver = messageHandler.getType().equals(RtuMessageConstant.WEBSERVER_DISABLE);

            if (createHan) {
                createHanNetwork(messageHandler);
            } else if (removeHan) {
                removeHanNetwork(messageHandler);
            } else if (joinZigBeeSlave) {
                joinZigBeeSlave(messageHandler);
            } else if (removeZigBeeMirror) {
                removeZigBeeMirror(messageHandler);
            } else if (removeZigBeeSlave) {
                removeZigBeeSlave(messageHandler);
            } else if (removeAllZigBeeSlaves) {
                removeAllZigBeeSlaves(messageHandler);
            } else if (backupZigBeeHanParameters) {
                backupZigBeeHanParameters(messageHandler);
            } else if (restoreZigBeeParameters) {
                restoreZigBeeHanParameters(messageHandler);
            } else if (updateHANLinkKey) {
                updateHanLinkKey(messageHandler);
            } else if (firmwareUpdate) {
                firmwareUpdate(messageEntry);
            } else if (zigbeeNCPFirmwareUpgrade) {
                zigbeeNCPFirmwareUpdate(messageEntry);
            } else if (readDebugLogbook) {
                readDebugLogbook(messageHandler);
            } else if (readElsterLogbook) {
                readElsterLogbook(messageHandler);
            } else if (configureConnectionMode) {
                configureConnectionMode(messageHandler);
            } else if (configureWakeupParameters) {
                configureWakeupParameters(messageHandler);
            } else if (configurePreferredNetworkOperatorsList) {
                configurePreferredNetworkOperatorsList(messageHandler);
            } else if (enableWebserver) {
                enableWebserver();
            } else if (disableWebserver) {
                disableWebserver();
            } else {
                log(Level.INFO, "Message not supported : " + content);
                success = false;
            }
        } catch (IOException e) {
            log(Level.SEVERE, "Message failed : " + e.getMessage());
            success = false;
        } catch (BusinessException e) {
            log(Level.SEVERE, "Message failed : " + e.getMessage());
            success = false;
        } catch (SQLException e) {
            log(Level.SEVERE, "Message failed : " + e.getMessage());
            success = false;
        } catch (InterruptedException e) {
            log(Level.SEVERE, "Message failed : " + e.getMessage());
            success = false;
        }

        if (success) {
            log(Level.INFO, "Message has finished.");
            return MessageResult.createSuccess(messageEntry);
        } else {
            return MessageResult.createFailed(messageEntry);
        }
    }

    private void firmwareUpdate(MessageEntry messageEntry) throws IOException, InterruptedException {
        log(Level.INFO, "Upgrade firmware message received.");

        String userFileContent = getIncludedContent(messageEntry.getContent());

        Date activationDate = null;
        String activationDateString = getValueFromXMLTag(RtuMessageConstant.ACTIVATE_DATE, messageEntry.getContent());
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        try {
            if (!activationDateString.equalsIgnoreCase("")) {
                activationDate = formatter.parse(activationDateString);
            }
        } catch (ParseException e) {
            throw new IOException("Error parsing the given activation date: " + e.getMessage());
        }

        byte[] imageData = new Base64EncoderDecoder().decode(userFileContent);
        ImageTransfer it = getCosemObjectFactory().getImageTransfer(AM110RRegisterFactory.FIRMWARE_UPDATE);
        if ((messageEntry.getTrackingId() != null) && !messageEntry.getTrackingId().toLowerCase().contains(NORESUME)) {
            int lastTransferredBlockNumber = it.readFirstNotTransferedBlockNumber().intValue();
            if (lastTransferredBlockNumber > 0) {
                it.setStartIndex(lastTransferredBlockNumber);
            }
        }
        it.upgrade(imageData);
        if (activationDate != null && activationDate.after(new Date())) {
            log(Level.INFO, "Writing the upgrade activation date.");
            SingleActionSchedule sas = getCosemObjectFactory().getSingleActionSchedule(AM110RRegisterFactory.IMAGE_ACTIVATION_SCHEDULER);
            Array dateArray = convertUnixToDateTimeArray(String.valueOf(activationDate.getTime() / 1000));
            sas.writeExecutionTime(dateArray);
        } else {
            log(Level.INFO, "Immediately activating the image.");
             it.imageActivation();
        }
        log(Level.INFO, "Upgrade firmware message finished.");
    }

    private void zigbeeNCPFirmwareUpdate(MessageEntry messageEntry) throws IOException, InterruptedException {
        log(Level.INFO, "Zigbee NCP firmware upgrade message received.");

        String userFileContent = getIncludedContent(messageEntry.getContent());

        Date activationDate = null;
        String activationDateString = getValueFromXMLTag(RtuMessageConstant.ACTIVATE_DATE, messageEntry.getContent());
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        try {
            if (!activationDateString.equalsIgnoreCase("")) {
                activationDate = formatter.parse(activationDateString);
            }
        } catch (ParseException e) {
            throw new IOException("Error parsing the given activation date: " + e.getMessage());
        }

        byte[] imageData = new Base64EncoderDecoder().decode(userFileContent);
        ImageTransfer it = getCosemObjectFactory().getImageTransfer(AM110RRegisterFactory.ZIGBEE_NCP_FIRMWARE_UPDATE);
        if ((messageEntry.getTrackingId() != null) && !messageEntry.getTrackingId().toLowerCase().contains(NORESUME)) {
            int lastTransferredBlockNumber = it.readFirstNotTransferedBlockNumber().intValue();
            if (lastTransferredBlockNumber > 0) {
                it.setStartIndex(lastTransferredBlockNumber);
            }
        }
        it.upgrade(imageData);
        if (activationDate != null  && activationDate.after(new Date())) {
            log(Level.INFO, "Writing the upgrade activation date.");
            SingleActionSchedule sas = getCosemObjectFactory().getSingleActionSchedule(AM110RRegisterFactory.ZIGBEE_NCP_IMAGE_ACTIVATION_SCHEDULER);
            Array dateArray = convertUnixToDateTimeArray(String.valueOf(activationDate.getTime() / 1000));
            sas.writeExecutionTime(dateArray);
        } else {
            log(Level.INFO, "Immediately activating the image.");
            it.imageActivation();
        }
        log(Level.INFO, "Zigbee NCP firmware upgrade message finished.");
    }

    private void restoreZigBeeHanParameters(final MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Restore ZigBee Han Parameters message received.");
        int userFileId = messageHandler.getRestoreHanParametersUserFileId();
        if (userFileId == -1) {
            throw new IOException("Invalid UserFileId value : " + userFileId);
        }

        UserFile uf = mw().getUserFileFactory().find(userFileId);
        if (uf == null) {
            throw new IOException("No UserFile found with ID : " + userFileId);
        }

        Structure backupStructure = getBackupRestoreData(uf.loadFileInByteArray());
        ZigBeeSETCControl zigBeeSETCControl = getCosemObjectFactory().getZigBeeSETCControl();
        log(Level.INFO, "Restoring HAN backup");
        try {
            zigBeeSETCControl.restoreHAN(backupStructure);
        } catch (DataAccessResultException e) {
            if (e.getCode().equals(DataAccessResultCode.TEMPORARY_FAILURE)) {
                log(Level.SEVERE, "Restoring HAN backup failed, most likely because the HAN network was already formed. Make sure HAN was removed first.");
                throw e;
            }
        }
        log(Level.INFO, "Restore ZigBee Han Parameters message finished.");
    }

    private void updateHanLinkKey(final MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Update HAN link key message received.");
        String address = messageHandler.getUpdateHanLinkKeyZigBeeIEEEAddress();
        address = ZigBeeMessagingUtils.validateAndFormatIeeeAddress(address);
        ZigBeeIEEEAddress ieeeAddress = new ZigBeeIEEEAddress(getBytesFromHexString(address, ""));

        getLogger().severe("Updating HAN link key for device with address [" + address + "].");
        getCosemObjectFactory().getZigBeeSETCControl().updateLinkKey(ieeeAddress);
        log(Level.INFO, "Update HAN link key message finished.");
    }

    /**
     * The data to restore can not contain the DateTime OctetString
     *
     * @return the BackupData Structure but without the DateTime OctetString
     */
    private Structure getBackupRestoreData(byte[] data) throws IOException {
        Structure backupData = AXDRDecoder.decode(data, Structure.class);

        if (backupData.nrOfDataTypes() == 3) {
            Structure restoreData = new Structure();
            if (backupData.getDataType(1).isOctetString() && backupData.getDataType(2).isArray()) {
                restoreData.addDataType(backupData.getDataType(1));
                restoreData.addDataType(backupData.getDataType(2));
                return restoreData;
            }
        }
        throw new IOException("Failed to parse the backup data - the UserFile did not contain a valid HAN backup.");
    }

    private void backupZigBeeHanParameters(final MessageHandler messageHandler) throws IOException, BusinessException, SQLException {
        log(Level.INFO, "Backup ZigBee Han Parameters message received.");
        ZigBeeSETCControl zigBeeSETCControl = getCosemObjectFactory().getZigBeeSETCControl();
        byte[] backupBytes = zigBeeSETCControl.backupHAN();
        Structure backupStructure = AXDRDecoder.decode(backupBytes, Structure.class);

        StringBuffer fileName = new StringBuffer("ZigBeeBackUp");
        fileName.append("_");
        fileName.append(protocol.getDlmsSession().getProperties().getSerialNumber());
        fileName.append("_");
        fileName.append(ProtocolTools.getFormattedDate("yyyy-MM-dd_HH.mm.ss"));

        UserFileShadow ufs = ProtocolTools.createUserFileShadow(fileName.toString(), backupStructure.getBEREncodedByteArray(), getFolderIdFromHub(), "bin");
        mw().getUserFileFactory().create(ufs);
        log(Level.INFO, "Backed-up ZigBee parameters in userFile : ZigBeeBackUp_" + protocol.getDlmsSession().getProperties().getSerialNumber());
        log(Level.INFO, "Backup ZigBee Han Parameters message finished.");
    }

    private void joinZigBeeSlave(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Join ZigBee slave device message received.");
        int deviceType = 0;
        String address = messageHandler.getJoinZigBeeIEEEAddress();
        String key = messageHandler.getJoinZigBeeLinkKey();
        String deviceTypeString = messageHandler.getJoinZigBeeDeviceType();

        address = ZigBeeMessagingUtils.validateAndFormatIeeeAddress(address);
        key = ZigBeeMessagingUtils.validateAndFormatLinkKey(key);
        try {
            deviceType = Integer.parseInt(deviceTypeString);
        } catch (NumberFormatException e) {
            throw new IOException("Error parsing the given deviceType: " + deviceType + " is not a valid number.");
        }

        EnhancedCreditRegisterZigbeeDeviceData zigbeeDeviceData = new EnhancedCreditRegisterZigbeeDeviceData(address, key, deviceType);

        ZigBeeSETCControl zigBeeSETCControl = getCosemObjectFactory().getZigBeeSETCControl();
        getLogger().info("Writing MAC and LinkKey.");
        zigBeeSETCControl.registerDevice(zigbeeDeviceData);

        getLogger().info("Writing join timeout.");
        Unsigned16 timeout = new Unsigned16(120);
        zigBeeSETCControl.writeJoinTimeout(timeout);

        getLogger().info("Enable joining.");
        zigBeeSETCControl.writeEnableDisableJoining(true);
        log(Level.INFO, "Join ZigBee slave device message finished.");
    }

    private void removeZigBeeSlave(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Remove ZigBee slave device message received.");
        String address = messageHandler.getRemoveZigBeeIEEEAddress();
        address = ZigBeeMessagingUtils.validateAndFormatIeeeAddress(address);
        ZigBeeIEEEAddress ieeeAddress = new ZigBeeIEEEAddress(getBytesFromHexString(address, ""));

        getLogger().severe("Unregistering zigBee device with address [" + address + "].");
        getCosemObjectFactory().getZigBeeSETCControl().unRegisterDevice(ieeeAddress);
        log(Level.INFO, "Remove ZigBee slave device message finished.");
    }

    private void removeZigBeeMirror(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Remove ZigBee mirror message received.");
        String address = messageHandler.getRemoveZigBeeIEEEAddress();
        String forceAttribute = messageHandler.getForceRemovalZigBeeMirror();

        address = ZigBeeMessagingUtils.validateAndFormatIeeeAddress(address);
        boolean force = ProtocolTools.getBooleanFromString(forceAttribute);

        getLogger().severe("Removing mirror for device with address [" + address + "] " + (force ? "Forced remove!" : "Normal remove."));

        ZigBeeIEEEAddress ieeeAddress = new ZigBeeIEEEAddress(getBytesFromHexString(address, ""));
        BitString bitString = new BitString(force ? 0xC000 : 0x4000, 16);
        Structure structure = new Structure(ieeeAddress, bitString);

        ZigBeeSETCControl zigBeeSETCControl = getCosemObjectFactory().getZigBeeSETCControl();
        zigBeeSETCControl.removeMirror(structure);
        log(Level.INFO, "Remove ZigBee mirror message finished.");
    }

    private void removeAllZigBeeSlaves(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Remove all ZigBee slaves messages received.");
        getLogger().severe("Unregistering all zigBee devices.");
        getCosemObjectFactory().getZigBeeSETCControl().unRegisterAllDevices();
        log(Level.INFO, "Remove all ZigBee slaves messages finished.");
    }

    private void createHanNetwork(final MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Create HAN Network message received.");
        ZigBeeSETCControl zigBeeSETCControl = getCosemObjectFactory().getZigBeeSETCControl();
        zigBeeSETCControl.createHAN();
        log(Level.INFO, "Create HAN Network message finished.");
    }

    private void removeHanNetwork(final MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Remove HAN Network message received.");
        ZigBeeSETCControl zigBeeSETCControl = getCosemObjectFactory().getZigBeeSETCControl();
        zigBeeSETCControl.removeHAN();
        log(Level.INFO, "Remove HAN Network message finished.");
    }

    private void readDebugLogbook(final MessageHandler messageHandler) throws IOException, BusinessException, SQLException {
        log(Level.INFO, "Read Debug logbook message received.");

        Calendar from = extractDate(messageHandler.getLogbookFromTimeString());
        Calendar to = extractDate(messageHandler.getLogbookToTimeString());

        ProfileGeneric profileGeneric = getProtocol().getDlmsSession().getCosemObjectFactory().getProfileGeneric(AM110RRegisterFactory.DEBUG_EVENT_LOG);

        // Readout the Debug event logbook.
        Array eventArray = getEventArray(profileGeneric, from, to);
        List<AbstractDataType> allDataTypes = eventArray.getAllDataTypes();

        // Write the eventArray to a UserFile.
        StringBuffer buffer = new StringBuffer();
        buffer.append(" -- Debug Logbook [" + from.getTime() + " - " + to.getTime() + "] --");
        buffer.append("\r\n");
        buffer.append("\r\n");

        for (int i = 0; i < allDataTypes.size(); i++) {
            Date time = ((Structure) allDataTypes.get(i)).getDataType(0).getOctetString().getDateTime(getTimeZone()).getValue().getTime();
            int index = ((Structure) allDataTypes.get(i)).getDataType(1).getUnsigned16().intValue();
            String message = ((Structure) allDataTypes.get(i)).getDataType(2).getOctetString().stringValue().trim();

            buffer.append(time);
            buffer.append("  ");
            buffer.append(String.format("%05d", index));
            buffer.append("  ");
            buffer.append(message);
            buffer.append("\r\n");
        }

        StringBuffer fileName = new StringBuffer("DebugLogbook");
        fileName.append("_");
        fileName.append(protocol.getDlmsSession().getProperties().getSerialNumber());
        fileName.append("_");
        fileName.append("[");
        fileName.append(ProtocolTools.getFormattedDate("yyyy-MM-dd_HH.mm.ss", from.getTime()));
        fileName.append("-");
        fileName.append(ProtocolTools.getFormattedDate("yyyy-MM-dd_HH.mm.ss", to.getTime()));
        fileName.append("]");
        fileName.append("_");
        fileName.append(ProtocolTools.getFormattedDate("yyyy-MM-dd_HH.mm.ss"));

        UserFileShadow ufs = ProtocolTools.createUserFileShadow(fileName.toString(), buffer.toString().trim().getBytes("UTF-8"), getFolderIdFromHub(), "txt");
        mw().getUserFileFactory().create(ufs);

        log(Level.INFO, "Stored readout of debug logbook in userFile: " + fileName);
        log(Level.INFO, "Read Debug logbook message finished.");
    }

    private void readElsterLogbook(final MessageHandler messageHandler) throws IOException, BusinessException, SQLException {
        log(Level.INFO, "Read Elster logbook message received.");

        Calendar from = extractDate(messageHandler.getLogbookFromTimeString());
        Calendar to = extractDate(messageHandler.getLogbookToTimeString());

        ProfileGeneric profileGeneric = getProtocol().getDlmsSession().getCosemObjectFactory().getProfileGeneric(AM110RRegisterFactory.ELSTER_FIRMWARE_EVENT_LOG);

        // Readout the Debug event logbook.
        Array eventArray = getEventArray(profileGeneric, from, to);
        List<AbstractDataType> allDataTypes = eventArray.getAllDataTypes();

        // Write the eventArray to a UserFile.
        StringBuffer buffer = new StringBuffer();
        buffer.append(" -- Elster Logbook [" + from.getTime() + " - " + to.getTime() + "] --");
        buffer.append("\r\n");
        buffer.append("\r\n");
        buffer.append("<Date and time> <Firmware event ID> <Sequence number>");
        buffer.append("\r\n");
        buffer.append("    Firmware version on all HAN devices:");
        buffer.append("\r\n");
        buffer.append("    <IEEE address> <application version> <hardware version> <stack version> <file version> <ZigBee stack version>");
        buffer.append("\r\n");
        buffer.append("\r\n");

        for (int i = 0; i < allDataTypes.size(); i++) {
            Date time = ((Structure) allDataTypes.get(i)).getDataType(0).getOctetString().getDateTime(getTimeZone()).getValue().getTime();
            int eventID = ((Structure) allDataTypes.get(i)).getDataType(1).getUnsigned16().intValue();
            int sequenceNumber = ((Structure) allDataTypes.get(i)).getDataType(2).getUnsigned16().intValue();
            Structure firmwareVersions = (Structure) ((Structure) allDataTypes.get(i)).getDataType(3);

            buffer.append(time);
            buffer.append("  ");
            buffer.append(String.format("%05d", eventID));
            buffer.append("  ");
            buffer.append(String.format("%05d", sequenceNumber));
            buffer.append("\r\n");
            buffer.append(getHANDeviceOTAStatus(firmwareVersions));
            buffer.append("\r\n");
        }

        StringBuffer fileName = new StringBuffer("ElsterLogbook");
        fileName.append("_");
        fileName.append(protocol.getDlmsSession().getProperties().getSerialNumber());
        fileName.append("_");
        fileName.append("[");
        fileName.append(ProtocolTools.getFormattedDate("yyyy-MM-dd_HH.mm.ss", from.getTime()));
        fileName.append("-");
        fileName.append(ProtocolTools.getFormattedDate("yyyy-MM-dd_HH.mm.ss", to.getTime()));
        fileName.append("]");
        fileName.append("_");
        fileName.append(ProtocolTools.getFormattedDate("yyyy-MM-dd_HH.mm.ss"));

        UserFileShadow ufs = ProtocolTools.createUserFileShadow(fileName.toString(), buffer.toString().trim().getBytes("UTF-8"), getFolderIdFromHub(), "txt");
        mw().getUserFileFactory().create(ufs);

        log(Level.INFO, "Stored readout of Elster logbook in userFile: " + fileName);
        log(Level.INFO, "Read Elster logbook message finished.");
    }

    private void configureConnectionMode(final MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Configure connection mode message received.");
        int connectionMode = messageHandler.getGPRSConnectionMode();
        if (connectionMode == -1) {
            throw new IOException("Could not parse the given mode.");
        }

        TypeEnum modeEnum;
        String mode = "";
        if (connectionMode == 1) {
            mode = "Always on";
            modeEnum = new TypeEnum(101);
        } else if (connectionMode == 2) {
            mode = "Always on within calling window - no wakuep";
            modeEnum = new TypeEnum(102);
        } else if (connectionMode == 3) {
            mode = "Always on within calling window - wakeup allowed";
            modeEnum = new TypeEnum(103);
        } else if (connectionMode == 4) {
            mode = "Wakeup mode";
            modeEnum = new TypeEnum(104);
        } else {
            throw new IOException("The specified mode (" + connectionMode + ") is not supported.");
        }

        AutoConnect autoConnect = getCosemObjectFactory().getAutoConnect();
        log(Level.INFO, "Writing the new connection mode: " + mode);
        autoConnect.writeMode(modeEnum);
        log(Level.INFO, "Configure connection mode message finished.");
    }

    private void configureWakeupParameters(final MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Configure wakeup parameters message received.");
        int callingWindowLength = messageHandler.getWakeupCallingWindowLength();
        int idleTimeout = messageHandler.getWakeupIdleTimeout();
        if (callingWindowLength == -1) {
            throw new IOException("Could not parse the given calling window length.");
        }
        if (idleTimeout == -1) {
            throw new IOException("Could not parse the given idle timeout.");
        }

        AutoConnect autoConnect = getCosemObjectFactory().getAutoConnect();
        log(Level.INFO, "Writing the new calling window length.");
        autoConnect.writeWakeupCallingWindowLength(new Unsigned32(callingWindowLength));
        log(Level.INFO, "Writing the new idle timeout.");
        autoConnect.writeWakeupIdleTimeoutLength(new Unsigned32(idleTimeout));

        log(Level.INFO, "Configure wakeup parameters message finished.");
    }

    private void configurePreferredNetworkOperatorsList(final MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Configure preferred network operator list message received.");
        List<String> preferredNetworkOperators = messageHandler.getPreferredNetworkOperators();
        Array preferredNetworkOperatorsArray = new Array();

        for (String operator : preferredNetworkOperators) {
            String[] split = operator.split(",");
            if (split.length != 2) {
                throw new IOException("Failed to parse the given network operator.");
            }
            Structure operatorStuct = new Structure();
            operatorStuct.addDataType(new OctetString(split[0].trim().getBytes()));
            operatorStuct.addDataType(new OctetString(split[1].trim().getBytes()));

            preferredNetworkOperatorsArray.addDataType(operatorStuct);
        }

        log(Level.INFO, "Writing the new preferred network operators list.");
        GenericWrite genericWrite = getCosemObjectFactory().getGenericWrite(AM110RRegisterFactory.GPRS_MANUAL_ROAMING_SETUP, 3, DLMSClassId.GPRS_MANUAL_ROAMING_SETUP.getClassId());
        genericWrite.write(preferredNetworkOperatorsArray.getBEREncodedByteArray());
        log(Level.INFO, "Configure preferred network operator list message finished.");
    }

    private void enableWebserver() throws IOException {
        getLogger().info("Executing Enable_Webserver message");
        GenericWrite genericWrite = getCosemObjectFactory().getGenericWrite(AM110RRegisterFactory.KEYS_LOCK_DOWN_SWITCH_OBIS, 3, DLMSClassId.MANUFACTURER_SPECIFIC_8193.getClassId());

        BooleanObject bool = new BooleanObject(false);
        genericWrite.write(bool.getBEREncodedByteArray());
        ((AM110R) protocol).setReboot(true);
        log(Level.INFO, "Webserver has been enabled - Note: AM110R will reboot at the end of this session.");
    }

    private void disableWebserver() throws IOException {
        getLogger().info("Executing Disable_Webserver message");
        GenericWrite genericWrite = getCosemObjectFactory().getGenericWrite(AM110RRegisterFactory.KEYS_LOCK_DOWN_SWITCH_OBIS, 3, DLMSClassId.MANUFACTURER_SPECIFIC_8193.getClassId());

        BooleanObject bool = new BooleanObject(true);
        genericWrite.write(bool.getBEREncodedByteArray());
        ((AM110R) protocol).setReboot(true);
        log(Level.INFO, "Webserver has been disabled - Note: AM110R will reboot at the end of this session.");
    }

    private String getIncludedContent(final String content) {
        int begin = content.indexOf(GenericMessaging.INCLUDED_USERFILE_TAG) + GenericMessaging.INCLUDED_USERFILE_TAG.length() + 1;
        int end = content.indexOf(GenericMessaging.INCLUDED_USERFILE_TAG, begin) - 2;
        return content.substring(begin, end);
    }

    private String getValueFromXMLTag(String tag, String content) {
        int startIndex = content.indexOf("<" + tag);
        if (startIndex == -1) {
            return "";  // Optional value is not specified
        }
        int endIndex = content.indexOf("</" + tag);
        try {
            return content.substring(startIndex + tag.length() + 2, endIndex);
        } catch (IndexOutOfBoundsException e) {
            return ""; // Optional value is empty
        }
    }

    private Calendar extractDate(String timeString) throws IOException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = null;
        try {
            if (timeString != null && !timeString.equalsIgnoreCase("0") && !timeString.equals("")) {
                date = formatter.parse(timeString);
            }
        } catch (ParseException e) {
            protocol.getLogger().log(Level.SEVERE, "Error parsing the given date: " + e.getMessage());
            throw new IOException(e.getMessage());
        }
        Calendar cal = Calendar.getInstance(getTimeZone());
        if (date != null) {
            cal.setTime(date);
        }
        return cal;
    }

    private Array getEventArray(ProfileGeneric profileGeneric, Calendar from, Calendar to) throws IOException {
        byte[] rawData = profileGeneric.getBufferData(from, to);
        AbstractDataType abstractData = AXDRDecoder.decode(rawData);
        if (!abstractData.isArray()) {
            throw new IOException("Expected Array of events, but received [" + abstractData.getClass().getName() + "]");
        }
        return abstractData.getArray();
    }

    private String getHANDeviceOTAStatus(Structure structure) {
        StringBuffer buffer = new StringBuffer();
        byte[] macAddressBytes = structure.getDataType(0).getOctetString().getContentByteArray();
        int applicationFirmwareVersion = structure.getDataType(1).getUnsigned8().intValue();
        int hardwareFirmwareVersion = structure.getDataType(2).getUnsigned8().intValue();
        int stackFirmwareVersion = structure.getDataType(3).getUnsigned8().intValue();
        int fileVersion = structure.getDataType(4).getUnsigned32().intValue();
        int zigbeeStackVersion = structure.getDataType(5).getUnsigned16().intValue();

        buffer.append("    ");
        buffer.append(getMacAddress(macAddressBytes));
        buffer.append("  ");
        buffer.append(String.format("%03d", applicationFirmwareVersion));
        buffer.append("  ");
        buffer.append(String.format("%03d", hardwareFirmwareVersion));
        buffer.append("  ");
        buffer.append(String.format("%03d", stackFirmwareVersion));
        buffer.append("  ");
        buffer.append(String.format("%010d", fileVersion));
        buffer.append("  ");
        buffer.append(String.format("%05d", zigbeeStackVersion));
        buffer.append("\r\n");

        return buffer.toString();
    }

    private String getMacAddress(byte[] macAddressBytes) {
        StringBuffer mac = new StringBuffer();
        for (byte each : macAddressBytes) {
            String temp = Integer.toHexString((int) each & 0xFF).toUpperCase();
            mac.append(String.format("%02X", (int) each & 0xFF));
            mac.append("-");
        }

        return mac.substring(0, mac.length() -1);
    }


    private void log(final Level level, final String msg) {
        getLogger().log(level, msg);
    }

    private Logger getLogger() {
        return getDlmsSession().getLogger();
    }

    @Override
    protected TimeZone getTimeZone() {
        return getDlmsSession().getTimeZone();
    }

    /*****************************************************************************/
    /* These methods require database access ...
    /*****************************************************************************/

    /**
     * Short notation for MeteringWarehouse.getCurrent()
     */
    public MeteringWarehouse mw() {
        MeteringWarehouse result = MeteringWarehouse.getCurrent();
        if (result == null) {
            return new MeteringWarehouseFactory().getBatch(false);
        } else {
            return result;
        }
    }

    private Device getRtuFromDatabaseBySerialNumberAndClientMac() throws IOException {
        String serial = this.protocol.getDlmsSession().getProperties().getSerialNumber();
        List<Device> rtusWithSameSerialNumber = mw().getDeviceFactory().findBySerialNumber(serial);
        for (Device each : rtusWithSameSerialNumber) {
            if (((String) each.getProtocolProperties().getProperty(DlmsProtocolProperties.CLIENT_MAC_ADDRESS)).equalsIgnoreCase("" + this.protocol.getDlmsSession().getProperties().getClientMacAddress())) {
                return each;
            }
        }
        throw new IOException("Could not find the EiServer rtu.");
    }

    private int getFolderIdFromHub() throws IOException {
        return getRtuFromDatabaseBySerialNumberAndClientMac().getFolderId();
    }
}
