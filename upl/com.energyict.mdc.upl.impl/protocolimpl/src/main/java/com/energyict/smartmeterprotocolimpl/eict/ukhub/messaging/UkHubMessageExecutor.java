package com.energyict.smartmeterprotocolimpl.eict.ukhub.messaging;

import com.energyict.mdc.io.NestedIOException;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.properties.DeviceMessageFile;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.BusinessException;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.BitString;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.GenericInvoke;
import com.energyict.dlms.cosem.GenericRead;
import com.energyict.dlms.cosem.GenericWrite;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.dlms.cosem.ZigBeeSASStartup;
import com.energyict.dlms.cosem.ZigBeeSETCControl;
import com.energyict.dlms.cosem.ZigbeeHanManagement;
import com.energyict.dlms.cosem.attributeobjects.RegisterZigbeeDeviceData;
import com.energyict.dlms.cosem.attributeobjects.ZigBeeIEEEAddress;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.MeteringWarehouseFactory;
import com.energyict.mdw.core.UserFile;
import com.energyict.mdw.shadow.UserFileShadow;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocolimpl.base.Base64EncoderDecoder;
import com.energyict.protocolimpl.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.generic.MessageParser;
import com.energyict.protocolimpl.generic.ParseUtils;
import com.energyict.protocolimpl.generic.csvhandling.CSVParser;
import com.energyict.protocolimpl.generic.csvhandling.TestObject;
import com.energyict.protocolimpl.generic.messages.MessageHandler;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.eict.NTAMessageHandler;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.ObisCodeProvider;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.UkHub;

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
public class UkHubMessageExecutor extends MessageParser {

    public static ObisCode KEYS_LOCK_DOWN_SWITCH_OBIS = ObisCode.fromString("0.128.0.0.1.255");
    public static ObisCode GPRS_MODEM_PING_SETUP_OBIS = ObisCode.fromString("0.0.93.44.17.255");
    private static final String RESUME = "resume";

    private final AbstractSmartDlmsProtocol protocol;

    private boolean success;
    private final DeviceMessageFileFinder messageFileFinder;
    private final DeviceMessageFileExtractor messageFileExtractor;

    public UkHubMessageExecutor(final AbstractSmartDlmsProtocol protocol, DeviceMessageFileFinder messageFileFinder, DeviceMessageFileExtractor messageFileExtractor) {
        this.protocol = protocol;
        this.messageFileExtractor = messageFileExtractor;
        this.messageFileFinder = messageFileFinder;
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
        success = true;
        try {
            importMessage(content, messageHandler);

            boolean changeHanSAS = messageHandler.getType().equals(RtuMessageConstant.CHANGE_HAN_SAS);
            boolean createHan = messageHandler.getType().equals(RtuMessageConstant.CREATE_HAN_NETWORK);
            boolean removeHan = messageHandler.getType().equals(RtuMessageConstant.REMOVE_HAN_NETWORK);
            boolean joinZigBeeSlave = messageHandler.getType().equals(RtuMessageConstant.JOIN_ZIGBEE_SLAVE);
            boolean removeZigBeeMirror = messageHandler.getType().equals(RtuMessageConstant.REMOVE_ZIGBEE_MIRROR);
            boolean removeZigBeeSlave = messageHandler.getType().equals(RtuMessageConstant.REMOVE_ZIGBEE_SLAVE);
            boolean removeAllZigBeeSlaves = messageHandler.getType().equals(RtuMessageConstant.REMOVE_ALL_ZIGBEE_SLAVES);
            boolean backupZigBeeHanParameters = messageHandler.getType().equals(RtuMessageConstant.BACKUP_ZIGBEE_HAN_PARAMETERS);
            boolean restoreZigBeeParameters = messageHandler.getType().equals(RtuMessageConstant.RESTORE_ZIGBEE_HAN_PARAMETERS);
            boolean readZigBeeStatus = messageHandler.getType().equals(RtuMessageConstant.READ_ZIGBEE_STATUS);
            boolean zigbeeNCPFirmwareUpgrade = messageHandler.getType().equals(RtuMessageConstant.ZIGBEE_NCP_FIRMWARE_UPGRADE);
            boolean modemPingSetup = messageHandler.getType().equals(RtuMessageConstant.GPRS_MODEM_PING_SETUP);
            boolean firmwareUpdate = messageHandler.getType().equals(RtuMessageConstant.FIRMWARE_UPGRADE);
            boolean testMessage = messageHandler.getType().equals(RtuMessageConstant.TEST_MESSAGE);
            boolean xmlCOnfig = messageHandler.getType().equals(RtuMessageConstant.XMLCONFIG);
            boolean enableWebserver = messageHandler.getType().equals(RtuMessageConstant.WEBSERVER_ENABLE);
            boolean disableWebserver = messageHandler.getType().equals(RtuMessageConstant.WEBSERVER_DISABLE);
            boolean reboot = messageHandler.getType().endsWith(RtuMessageConstant.REBOOT);
            boolean readDebugLogbook = messageHandler.getType().equals(RtuMessageConstant.DEBUG_LOGBOOK);
            boolean readElsterLogbook = messageHandler.getType().equals(RtuMessageConstant.ELSTER_SPECIFIC_LOGBOOK);

            if (changeHanSAS) {
                changeHanSAS(messageHandler);
            } else if (createHan) {
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
                backupZigBeeHanParameters();
            } else if (restoreZigBeeParameters) {
                restoreZigBeeHanParameters(messageHandler);
            } else if (readZigBeeStatus) {
                readZigBeeStatus();
            } else if (modemPingSetup) {
                modemPingSetup(messageHandler);
            } else if (firmwareUpdate) {
                firmwareUpdate(messageHandler, content, trackingId);
            } else if (zigbeeNCPFirmwareUpgrade) {
                zigbeeNCPFirmwareUpdate(messageHandler, content);
            } else if (testMessage) {
                testMessage(messageHandler);
            } else if (xmlCOnfig) {
                xmlConfigMessage(content);
            } else if (enableWebserver) {
                enableWebserver();
            } else if (disableWebserver) {
                disableWebserver();
            } else if (reboot) {
                reboot();
            } else if (readDebugLogbook) {
                readDebugLogbook(messageHandler);
            } else if (readElsterLogbook) {
                readElsterLogbook(messageHandler);
            } else {
                log(Level.INFO, "Message not supported : " + content);
                success = false;
            }
        } catch (IOException | BusinessException | SQLException e) {
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

    private void xmlConfigMessage(String fullContent) throws IOException {

        String firstTag = "<XMLConfig>";
        String lastTag = "</XMLConfig>";
        int firstIndex = fullContent.indexOf(firstTag) + firstTag.length();
        int lastIndex = fullContent.indexOf(lastTag);
        String content = fullContent.substring(firstIndex, lastIndex);

        getLogger().info("Executing XML config update message");

        ObisCode obisCode = ObisCode.fromString("0.129.0.0.0.255");
        Data data = getCosemObjectFactory().getData(obisCode);

        getLogger().severe("[" + content + "]");
        getLogger().severe("Writing plain XML to device [" + content + "]...");
        OctetString plainXML = OctetString.fromString(content);
        data.setValueAttr(plainXML);

    }

    private void reboot() {
        getLogger().info("Executing Reboot message.");
        getLogger().info("Warning: Device will reboot at the end of the communication session.");
        ((UkHub) protocol).setReboot(true);
    }

    private void enableWebserver() throws IOException {
        getLogger().info("Executing Enable_Webserver message");
        GenericWrite genericWrite = getCosemObjectFactory().getGenericWrite(KEYS_LOCK_DOWN_SWITCH_OBIS, 3, DLMSClassId.MANUFACTURER_SPECIFIC_8193.getClassId());

        BooleanObject bool = new BooleanObject(false);
        genericWrite.write(bool.getBEREncodedByteArray());
        ((UkHub) protocol).setReboot(true);
    }

    private void disableWebserver() throws IOException {
        getLogger().info("Executing Disable_Webserver message");
        GenericWrite genericWrite = getCosemObjectFactory().getGenericWrite(KEYS_LOCK_DOWN_SWITCH_OBIS, 3, DLMSClassId.MANUFACTURER_SPECIFIC_8193.getClassId());

        BooleanObject bool = new BooleanObject(true);
        genericWrite.write(bool.getBEREncodedByteArray());
        ((UkHub) protocol).setReboot(true);
    }

    private void modemPingSetup(MessageHandler messageHandler) throws IOException {
        getLogger().info("Executing GPRS Modem Ping Setup message");

        int pingInterval = messageHandler.getPingInterval();
        if (pingInterval == -1) {
            throw new IOException("Invalid Ping Interval: " + pingInterval);
        }
        String pingIP = messageHandler.getPingIP().trim();
        if (pingIP.getBytes().length > 32) {
            throw new IOException("Invalid Ping IP - the IP should be less than 32 characters.");
        }

        Data setupObject = getCosemObjectFactory().getData(GPRS_MODEM_PING_SETUP_OBIS);
        Structure pingStruct = new Structure();
        pingStruct.addDataType(new Unsigned32(pingInterval));
        OctetString dataType = new OctetString(pingIP.getBytes());
        pingStruct.addDataType(dataType);

        setupObject.setValueAttr(pingStruct);
        log(Level.INFO, "GPRS Modem Ping Setup message successful");
    }

    private void firmwareUpdate(MessageHandler messageHandler, String content, String trackingId) throws IOException {
        log(Level.INFO, "Handling message Firmware upgrade");

        String userFileID = messageHandler.getUserFileId();
        boolean resume = false;
        if ((trackingId != null) && trackingId.toLowerCase().contains(RESUME)) {
            resume = true;
        }

        if (!ParseUtils.isInteger(userFileID)) {
            String str = "Not a valid entry for the userFile.";
            throw new IOException(str);
        }
        DeviceMessageFile deviceMessageFile = this.messageFileFinder.from(userFileID).orElseThrow(() -> new IllegalArgumentException("Not a valid entry for the userfileID " + userFileID));

        String[] parts = content.split("=");
        Date date = null;
        try {
            if (parts.length > 2) {
                String dateString = parts[2].substring(1).split("\"")[0];

                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                date = formatter.parse(dateString);
            }
        } catch (ParseException e) {
            log(Level.SEVERE, "Error while parsing the activation date: " + e.getMessage());
            throw new NestedIOException(e);
        } catch (NumberFormatException e) {
            log(Level.SEVERE, "Error while parsing the time duration: " + e.getMessage());
            throw new NestedIOException(e);
        }

        byte[] imageData = new Base64EncoderDecoder().decode(this.messageFileExtractor.binaryContents(deviceMessageFile));
        ImageTransfer it = getCosemObjectFactory().getImageTransfer(ObisCodeProvider.FIRMWARE_UPDATE);
        if (resume) {
            int lastTransferredBlockNumber = it.readFirstNotTransferedBlockNumber().intValue();
            if (lastTransferredBlockNumber > 0) {
                it.setStartIndex(lastTransferredBlockNumber);
            }
        }
        it.upgrade(imageData);
        if (date != null) {
            SingleActionSchedule sas = getCosemObjectFactory().getSingleActionSchedule(ObisCodeProvider.IMAGE_ACTIVATION_SCHEDULER);
            Array dateArray = convertUnixToDateTimeArray(String.valueOf(date.getTime() / 1000));
            sas.writeExecutionTime(dateArray);
        } else {
            it.imageActivation();
        }
    }

    private void zigbeeNCPFirmwareUpdate(MessageHandler messageHandler, String content) throws IOException {
        getLogger().info("Executing Zigbee NCP firmware update message");
        int userFileId = messageHandler.getZigbeeNCPFirmwareUpgradeUserFileId();
        if (userFileId == -1) {
            throw new IOException("Invalid UserFileId value : " + userFileId);
        }

        UserFile uf = mw().getUserFileFactory().find(userFileId);
        if (uf == null) {
            throw new IOException("No UserFile found with ID : " + userFileId);
        }

        String[] parts = content.split("=");
        Date date = null;
        try {
            if (parts.length > 2) {
                String dateString = parts[2].substring(1).split("\"")[0];

                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                date = formatter.parse(dateString);
            }
        } catch (ParseException e) {
            log(Level.SEVERE, "Error while parsing the activation date: " + e.getMessage());
            throw new NestedIOException(e);
        } catch (NumberFormatException e) {
            log(Level.SEVERE, "Error while parsing the time duration: " + e.getMessage());
            throw new NestedIOException(e);
        }

        byte[] imageData = new Base64EncoderDecoder().decode(uf.loadFileInByteArray());
        ImageTransfer it = getCosemObjectFactory().getImageTransfer(ObisCodeProvider.ZIGBEE_NCP_FIRMWARE_UPDATE);
        it.upgrade(imageData);
        if (date != null) {
            SingleActionSchedule sas = getCosemObjectFactory().getSingleActionSchedule(ObisCodeProvider.IMAGE_ACTIVATION_SCHEDULER);
            Array dateArray = convertUnixToDateTimeArray(String.valueOf(date.getTime() / 1000));
            sas.writeExecutionTime(dateArray);
        } else {
            it.imageActivation();
        }
    }

    private void changeHanSAS(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Sending message : Change Zigbee HAN SAS");
        String extendedPanId = messageHandler.getChangeHanSasExtendedPanId();
        String panId = messageHandler.getChangeHanSasPanId();
        String channelMask = messageHandler.getChangeHanSasChannel();
        String insecureJoin = messageHandler.getChangeHanSasInsecureJoin();

        // TODO: Implement this method. DLMS objects not implemented yet as well, so we'll have to do that first.

        ZigBeeSASStartup zigBeeSASStartup = getCosemObjectFactory().getZigBeeSASStartup();

        // Write the EXTENDED PAN ID
        if (extendedPanId != null) {
            getLogger().info("Writing extended pan id [" + extendedPanId + "]");
            byte[] extendedPanIdValue = ProtocolTools.getBytesFromHexString(extendedPanId, "");
            if (extendedPanIdValue.length > 8) {
                throw new IOException("Invalid value for extended Pan ID:" + extendedPanId + ", length should be equal or less than 8 bytes");
            }
            extendedPanIdValue = ProtocolTools.concatByteArrays(new byte[8 - extendedPanIdValue.length], extendedPanIdValue);
            OctetString extendedPanIdObject = OctetString.fromByteArray(extendedPanIdValue);
            zigBeeSASStartup.writeExtendedPanId(extendedPanIdObject);
        }

        // Write the PAN ID
        if (panId != null) {
            getLogger().info("Writing pan id [" + panId + "]");
            try {
                int panIdValue = Integer.valueOf(panId);
                Unsigned16 panIdObject = new Unsigned16(panIdValue);
                zigBeeSASStartup.writePanId(panIdObject);
            } catch (NumberFormatException e) {
                getLogger().severe("Unable to write pan id [" + panId + "]! Invalid value: " + e.getMessage());
            }
        }

        // Write the channel mask
        if (channelMask != null) {
            getLogger().info("Writing channel mask [" + channelMask + "]");
            try {
                long channelMaskValue = Long.valueOf(channelMask);
                Unsigned32 channelMaskObject = new Unsigned32(channelMaskValue);
                zigBeeSASStartup.writeChannelMask(channelMaskObject);
            } catch (NumberFormatException e) {
                getLogger().severe("Unable to write channel mask [" + channelMask + "]! Invalid value: " + e.getMessage());
            }
        }

        // Write the use insecure join attribute
        if (insecureJoin != null) {
            getLogger().info("Writing insecure join [" + insecureJoin + "]");
            boolean insecureJoinValue = ProtocolTools.getBooleanFromString(insecureJoin);
            BooleanObject insecureJoinObject = new BooleanObject(insecureJoinValue);
            zigBeeSASStartup.writeUseInsecureJoin(insecureJoinObject);
        }

    }

    private void readZigBeeStatus() throws IOException, BusinessException, SQLException {
        ZigBeeStatus zigBeeStatus = new ZigBeeStatus(getCosemObjectFactory());
        String status = zigBeeStatus.readStatus();
        String fileName = "ZigBeeStatus_" + protocol.getDlmsSession().getProperties().getSerialNumber() + "_" + ProtocolTools.getFormattedDate("yyyy-MM-dd_HH.mm.ss");
        System.out.println("\n");
        System.out.println(status);
        System.out.println("\n");

        UserFileShadow ufs = ProtocolTools.createUserFileShadow(fileName, status.getBytes("UTF-8"), getFolderIdFromHub(), "txt");
        mw().getUserFileFactory().create(ufs);

        log(Level.INFO, "Stored ZigBee status parameters in userFile: " + fileName);
    }

    private void restoreZigBeeHanParameters(final MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Sending message : Restore ZigBee Han Keys");
        int userFileId = messageHandler.getRestoreHanParametersUserFileId();
        if (userFileId == -1) {
            throw new IOException("Invalid UserFileId value : " + userFileId);
        }

        UserFile uf = mw().getUserFileFactory().find(userFileId);
        if (uf == null) {
            throw new IOException("No UserFile found with ID : " + userFileId);
        }

        HanBackupRestoreData hanBackUpData = new HanBackupRestoreData(uf.loadFileInByteArray(), 0, 0);
        ZigbeeHanManagement hanManagement = getCosemObjectFactory().getZigbeeHanManagement();
        log(Level.FINE, "Writing : RestoreData Structure");
        hanManagement.restore(hanBackUpData.getRestoreData());

        log(Level.INFO, "Create HAN Network");
        hanManagement.createHan();

//        getLogger().info("Enable joining");
//        ZigBeeSETCControl zigBeeSETCControl = getCosemObjectFactory().getZigBeeSETCControl();
//        zigBeeSETCControl.writeEnableDisableJoining(true);

        log(Level.INFO, "Restore ZigBee Han Keys successful");
    }

    private void backupZigBeeHanParameters() throws IOException, BusinessException, SQLException {
        log(Level.INFO, "Sending message : Backup ZigBee Han Keys");
        ZigbeeHanManagement hanManagement = getCosemObjectFactory().getZigbeeHanManagement();
        hanManagement.backup();

        boolean backedUp = false;
        int counter = 0;
        while (!backedUp) {
            try {
                Thread.sleep(1000);
                Long eventValue = getCosemObjectFactory().getData(ObisCodeProvider.HanManagementEventObject).getValue();
                if (eventValue == 0x0126) {    //Should be event 'HAN Backup Performed'
                    backedUp = true;
                } else if (counter++ >= 30) {
                    throw new IOException("ZigBee backup probably failed, takes to long (30s) before 'HAN Backup Performed'-event is written.");
                }
            } catch (InterruptedException e) {
                log(Level.SEVERE, "Interrupted while sleeping : " + e.getMessage());
                Thread.currentThread().interrupt();
                throw ConnectionCommunicationException.communicationInterruptedException(e);
            }
        }

        // TODO this requires dataBase access, the message will not succeed in an HTTP ComServer environment
        Structure backUpData = hanManagement.readBackupData();
        ZigBeeSASStartup sasStartup = getCosemObjectFactory().getZigBeeSASStartup();
        OctetString extendedPanId = sasStartup.readExtendedPanId();
        OctetString linkKey = sasStartup.readLinkKey();
        OctetString networkKey = sasStartup.readNetworkKey();

        HanBackupRestoreData hanBackupData = new HanBackupRestoreData();
        hanBackupData.setBackupData(backUpData);
        hanBackupData.setExtendedPanId(extendedPanId);
        hanBackupData.setLinkKey(linkKey);
        hanBackupData.setNetworkKey(networkKey);

        UserFileShadow ufs = ProtocolTools.createUserFileShadow("ZigBeeBackUp_" + protocol.getDlmsSession().getProperties().getSerialNumber(), hanBackupData.getBEREncodedByteArray(), getFolderIdFromHub(), "bin");
        mw().getUserFileFactory().create(ufs);
        log(Level.INFO, "Backed-up ZigBee parameters in userFile : ZigBeeBackUp_" + protocol.getDlmsSession().getProperties().getSerialNumber());
    }

    private void joinZigBeeSlave(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Sending message : Join ZigBee slave");
        String address = messageHandler.getJoinZigBeeIEEEAddress();
        String key = messageHandler.getJoinZigBeeLinkKey();
        address = ZigBeeMessagingUtils.validateAndFormatIeeeAddress(address);
        key = ZigBeeMessagingUtils.validateAndFormatLinkKey(key);
        RegisterZigbeeDeviceData zigbeeDeviceData = new RegisterZigbeeDeviceData(address, key);

        ZigBeeSETCControl zigBeeSETCControl = getCosemObjectFactory().getZigBeeSETCControl();
        getLogger().info("Writing MAC and LinkKey.");
        zigBeeSETCControl.registerDevice(zigbeeDeviceData);

        getLogger().info("Writing join timeout.");
        Unsigned16 timeout = new Unsigned16(120);
        zigBeeSETCControl.writeJoinTimeout(timeout);

        getLogger().info("Enable joining.");
        zigBeeSETCControl.writeEnableDisableJoining(true);
    }

    private void removeZigBeeSlave(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Sending message : Remove ZigBee slave");
        String address = messageHandler.getRemoveZigBeeIEEEAddress();
        address = ZigBeeMessagingUtils.validateAndFormatIeeeAddress(address);
        ZigBeeIEEEAddress ieeeAddress = new ZigBeeIEEEAddress(getBytesFromHexString(address, ""));
        getCosemObjectFactory().getZigBeeSETCControl().unRegisterDevice(ieeeAddress);
    }

    private void removeZigBeeMirror(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Sending message : Remove ZigBee mirror");
        String address = messageHandler.getRemoveZigBeeIEEEAddress();
        String forceAttribute = messageHandler.getForceRemovalZigBeeMirror();

        address = ZigBeeMessagingUtils.validateAndFormatIeeeAddress(address);
        boolean force = ProtocolTools.getBooleanFromString(forceAttribute);

        getLogger().severe("Removing mirror for device with address [" + address + "] " + (force ? "Forced remove!" : "Normal remove."));

        ZigBeeIEEEAddress ieeeAddress = new ZigBeeIEEEAddress(getBytesFromHexString(address, ""));
        BitString bitString = new BitString(force ? 0x01 : 0x00, 16);
        Structure structure = new Structure(ieeeAddress, bitString);

        ZigbeeHanManagement hanManagement = getCosemObjectFactory().getZigbeeHanManagement();
        hanManagement.removeMirror(structure);

    }

    private void removeAllZigBeeSlaves(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Sending message : Remove all ZigBee slaves");
        getCosemObjectFactory().getZigBeeSETCControl().unRegisterAllDevices();
    }

    private void createHanNetwork(final MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Sending message : Create HAN Network");
        ZigbeeHanManagement hanManagement = getCosemObjectFactory().getZigbeeHanManagement();
        hanManagement.createHan();
    }

    private void removeHanNetwork(final MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Sending message : Remove HAN Network");
        ZigbeeHanManagement hanManagement = getCosemObjectFactory().getZigbeeHanManagement();
        hanManagement.removeHan();
    }

    private void readDebugLogbook(final MessageHandler messageHandler) throws IOException, BusinessException, SQLException {
        log(Level.INFO, "Sending message : Read Debug logbook");

        Calendar from = extractDate(messageHandler.getLogbookFromTimeString());
        Calendar to = extractDate(messageHandler.getLogbookToTimeString());

        ProfileGeneric profileGeneric = getProtocol().getDlmsSession().getCosemObjectFactory().getProfileGeneric(ObisCodeProvider.DEBUG_EVENT_LOG);

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
    }

    private void readElsterLogbook(final MessageHandler messageHandler) throws IOException, BusinessException, SQLException {
        log(Level.INFO, "Sending message : Read Elster logbook");

        Calendar from = extractDate(messageHandler.getLogbookFromTimeString());
        Calendar to = extractDate(messageHandler.getLogbookToTimeString());

        ProfileGeneric profileGeneric = getProtocol().getDlmsSession().getCosemObjectFactory().getProfileGeneric(ObisCodeProvider.ELSTER_SPECIFIC_EVENT_LOG);

        // Readout the Debug event logbook.
        Array eventArray = getEventArray(profileGeneric, from, to);
        List<AbstractDataType> allDataTypes = eventArray.getAllDataTypes();

        // Write the eventArray to a UserFile.
        StringBuffer buffer = new StringBuffer();
        buffer.append(" -- Elster Logbook [" + from.getTime() + " - " + to.getTime() + "] --");
        buffer.append("\r\n");
        buffer.append("\r\n");
        buffer.append("<Date and time> <Firmware event ID> <Counter index>");
        buffer.append("\r\n");
        buffer.append("    Firmware version on all HAN devices:");
        buffer.append("\r\n");
        buffer.append("    <IEEE address> <application version> <hardware version> <stack version>");
        buffer.append("\r\n");
        buffer.append("\r\n");

        for (int i = 0; i < allDataTypes.size(); i++) {
            Date time = ((Structure) allDataTypes.get(i)).getDataType(0).getOctetString().getDateTime(getTimeZone()).getValue().getTime();

            int eventID = ((Structure) allDataTypes.get(i)).getDataType(1).getUnsigned16().intValue();
            int counterIndex = ((Structure) allDataTypes.get(i)).getDataType(2).getUnsigned16().intValue();
            Array HANdeviceOTA = (Array) ((Structure) allDataTypes.get(i)).getDataType(3);

            buffer.append(time);
            buffer.append("  ");
            buffer.append(String.format("%05d", eventID));
            buffer.append("  ");
            buffer.append(String.format("%05d", counterIndex));
            buffer.append("\r\n");
            buffer.append(getHANDeviceOTAStatus(HANdeviceOTA));
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

    private String getHANDeviceOTAStatus(Array array) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < array.nrOfDataTypes(); i++) {
            byte[] macAddressBytes = ((Structure) array.getDataType(i)).getDataType(0).getOctetString().getContentByteArray();
            int applicationFirmwareVersion = ((Structure) array.getDataType(i)).getDataType(1).getUnsigned8().intValue();
            int hardwareFirmwareVersion = ((Structure) array.getDataType(i)).getDataType(2).getUnsigned8().intValue();
            int stackFirmwareVersion = ((Structure) array.getDataType(i)).getDataType(3).getUnsigned8().intValue();

            buffer.append("    ");
            buffer.append(getMacAddress(macAddressBytes));
            buffer.append("  ");
            buffer.append(String.format("%03d", applicationFirmwareVersion));
            buffer.append("  ");
            buffer.append(String.format("%03d", hardwareFirmwareVersion));
            buffer.append("  ");
            buffer.append(String.format("%03d", stackFirmwareVersion));
            buffer.append("\r\n");
        }

        return buffer.toString();
    }

    private String getMacAddress(byte[] macAddressBytes) {
        StringBuffer mac = new StringBuffer();
        for (byte each : macAddressBytes) {
            String temp = Integer.toHexString((int) each & 0xFF).toUpperCase();
            mac.append((temp.length() < 2) ? "0" + temp : temp);
            mac.append("-");
        }

        return mac.substring(0, mac.length() - 1);
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

    private void testMessage(MessageHandler messageHandler) throws IOException, BusinessException, SQLException {
        log(Level.INFO, "Handling message TestMessage");
        int failures = 0;
        String userFileId = messageHandler.getTestUserFileId();
        Date currentTime;
        if (!userFileId.equalsIgnoreCase("")) {
            if (ParseUtils.isInteger(userFileId)) {
                UserFile uf = mw().getUserFileFactory().find(Integer.parseInt(userFileId));
                if (uf != null) {
                    byte[] data = uf.loadFileInByteArray();
                    CSVParser csvParser = new CSVParser(this.messageFileExtractor);
                    csvParser.parse(data);
                    boolean hasWritten;
                    TestObject to = new TestObject("");
                    for (int i = 0; i < csvParser.size(); i++) {
                        to = csvParser.getTestObject(i);
                        if (csvParser.isValidLine(to)) {
                            currentTime = new Date(System.currentTimeMillis());
                            hasWritten = false;
                            try {
                                switch (to.getType()) {
                                    case 0: { // GET
                                        GenericRead gr = getCosemObjectFactory().getGenericRead(to.getObisCode(), DLMSUtils.attrLN2SN(to.getAttribute()), to.getClassId());
                                        to.setResult("0x" + ParseUtils.decimalByteToString(gr.getResponseData()));
                                        hasWritten = true;
                                    }
                                    break;
                                    case 1: { // SET
                                        GenericWrite gw = getCosemObjectFactory().getGenericWrite(to.getObisCode(), to.getAttribute(), to.getClassId());
                                        gw.write(ParseUtils.hexStringToByteArray(to.getData()));
                                        to.setResult("OK");
                                        hasWritten = true;
                                    }
                                    break;
                                    case 2: { // ACTION
                                        GenericInvoke gi = getCosemObjectFactory().getGenericInvoke(to.getObisCode(), to.getClassId(), to.getMethod());
                                        if (to.getData().equalsIgnoreCase("")) {
                                            gi.invoke();
                                        } else {
                                            gi.invoke(ParseUtils.hexStringToByteArray(to.getData()));
                                        }
                                        to.setResult("OK");
                                        hasWritten = true;
                                    }
                                    break;
                                    case 3: { // MESSAGE
                                        //Do nothing, no longer supported
                                        //OldDeviceMessageShadow rms = new OldDeviceMessageShadow();
                                        //rms.setContents(csvParser.getTestObject(i).getData());
                                        //rms.setRtuId(getRtuFromDatabaseBySerialNumberAndClientMac().getId());
                                        //OldDeviceMessage rm = mw().getRtuMessageFactory().create(rms);
                                        //doMessage(rm);
                                        //if (rm.getState().getId() == rm.getState().CONFIRMED.getId()) {
                                        //    to.setResult("OK");
                                        //} else {
                                        //    to.setResult("MESSAGE failed, current state " + rm.getState().getId());
                                        //}
                                        //hasWritten = true;
                                    }
                                    break;
                                    case 4: { // WAIT
                                        waitCyclus(Integer.parseInt(to.getData()));
                                        to.setResult("OK");
                                        hasWritten = true;
                                    }
                                    break;
                                    case 5: {
                                        // do nothing, it's no valid line
                                    }
                                    break;
                                    default: {
                                        throw new ApplicationException("Row " + i + " of the CSV file does not contain a valid type.");
                                    }
                                }
                                to.setTime(currentTime.getTime());

                                // Check if the expected value is the same as the result
                                if ((to.getExpected() == null) || (!to.getExpected().equalsIgnoreCase(to.getResult()))) {
                                    to.setResult("Failed - " + to.getResult());
                                    failures++;
                                    log(Level.INFO, "Test " + i + " has successfully finished, but the result didn't match the expected value.");
                                } else {
                                    log(Level.INFO, "Test " + i + " has successfully finished.");
                                }

                            } catch (Exception e) {
                                if (!hasWritten) {
                                    if ((to.getExpected() != null) && (e.getMessage().indexOf(to.getExpected()) != -1)) {
                                        to.setResult(e.getMessage());
                                        log(Level.INFO, "Test " + i + " has successfully finished.");
                                        hasWritten = true;
                                    } else {
                                        log(Level.INFO, "Test " + i + " has failed.");
                                        String eMessage;
                                        if (e.getMessage().indexOf("\r\n") != -1) {
                                            eMessage = e.getMessage().substring(0, e.getMessage().indexOf("\r\n")) + "...";
                                        } else {
                                            eMessage = e.getMessage();
                                        }
                                        to.setResult("Failed. " + eMessage);
                                        hasWritten = true;
                                        failures++;
                                    }
                                    to.setTime(currentTime.getTime());
                                }
                            } finally {
                                if (!hasWritten) {
                                    to.setResult("Failed - Unknow exception ...");
                                    failures++;
                                    to.setTime(currentTime.getTime());
                                }
                            }
                        }
                    }
                    if (failures == 0) {
                        csvParser.addLine("All the tests are successfully finished.");
                    } else {
                        csvParser.addLine("" + failures + " of the " + csvParser.getValidSize() + " tests " + ((failures == 1) ? "has" : "have") + " failed.");
                    }
                    mw().getUserFileFactory().create(csvParser.convertResultToUserFile(uf, getFolderIdFromHub()));
                } else {
                    throw new ApplicationException("Userfile with ID " + userFileId + " does not exist.");
                }
            } else {
                throw new IOException("UserFileId is not a valid number");
            }
        } else {
            throw new IOException("No userfile id is given.");
        }
    }

    private void waitCyclus(int delay) throws IOException {
        try {
            int nrOfPolls = (delay / (20)) + (delay % (20) == 0 ? 0 : 1);
            for (int i = 0; i < nrOfPolls; i++) {
                if (i < nrOfPolls - 1) {
                    ProtocolTools.delay(20000);
                } else {
                    ProtocolTools.delay((delay - (i * (20))) * 1000);
                }
                log(Level.INFO, "Keeping connection alive");
                getCosemObjectFactory().getClock().getDateTime();
            }
        } catch (IOException e) {
            throw new IOException("Could not keep connection alive." + e.getMessage());
        }
    }
}
