package com.energyict.smartmeterprotocolimpl.eict.ukhub.messaging;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.BusinessException;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.attributeobjects.RegisterZigbeeDeviceData;
import com.energyict.dlms.cosem.attributeobjects.ZigBeeIEEEAddress;
import com.energyict.genericprotocolimpl.common.GenericMessageExecutor;
import com.energyict.genericprotocolimpl.common.ParseUtils;
import com.energyict.genericprotocolimpl.common.messages.GenericMessaging;
import com.energyict.genericprotocolimpl.common.messages.MessageHandler;
import com.energyict.genericprotocolimpl.nta.messagehandling.NTAMessageHandler;
import com.energyict.genericprotocolimpl.webrtu.common.csvhandling.CSVParser;
import com.energyict.genericprotocolimpl.webrtu.common.csvhandling.TestObject;
import com.energyict.mdw.core.*;
import com.energyict.mdw.shadow.RtuMessageShadow;
import com.energyict.mdw.shadow.UserFileShadow;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageResult;
import com.energyict.protocolimpl.base.Base64EncoderDecoder;
import com.energyict.protocolimpl.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.ObisCodeProvider;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.UkHub;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.energyict.protocolimpl.utils.ProtocolTools.getBytesFromHexString;

/**
 * Copyrights EnergyICT
 * Date: 20-jul-2011
 * Time: 17:15:18
 */
public class UkHubMessageExecutor extends GenericMessageExecutor {

    public static ObisCode KEYS_LOCK_DOWN_SWITCH_OBIS = ObisCode.fromString("0.128.0.0.1.255");
    public static ObisCode GPRS_MODEM_PING_SETUP_OBIS = ObisCode.fromString("0.0.93.44.17.255");
    private final AbstractSmartDlmsProtocol protocol;

    private boolean success;

    public UkHubMessageExecutor(final AbstractSmartDlmsProtocol protocol) {
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
            boolean modemPingSetup = messageHandler.getType().equals(RtuMessageConstant.GPRS_MODEM_PING_SETUP);
            boolean firmwareUpdate = messageHandler.getType().equals(RtuMessageConstant.FIRMWARE_UPDATE);
            boolean testMessage = messageHandler.getType().equals(RtuMessageConstant.TEST_MESSAGE);
            boolean xmlCOnfig = messageHandler.getType().equals(RtuMessageConstant.XMLCONFIG);
            boolean enableWebserver = messageHandler.getType().equals(RtuMessageConstant.WEBSERVER_ENABLE);
            boolean disableWebserver = messageHandler.getType().equals(RtuMessageConstant.WEBSERVER_DISABLE);
            boolean reboot = messageHandler.getType().endsWith(RtuMessageConstant.REBOOT);

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
                backupZigBeeHanParameters(messageHandler);
            } else if (restoreZigBeeParameters) {
                restoreZigBeeHanParameters(messageHandler);
            } else if (readZigBeeStatus) {
                readZigBeeStatus();
            } else if (modemPingSetup) {
                modemPingSetup(messageHandler, content);
            } else if (firmwareUpdate) {
                firmwareUpdate(messageHandler, content);
            } else if (testMessage) {
                testMessage(messageHandler);
            } else if (xmlCOnfig) {
                xmlConfigMessage(messageHandler, content);
            } else if (enableWebserver) {
                enableWebserver();
            } else if (disableWebserver) {
                disableWebserver();
            } else if (reboot) {
                reboot();
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
        }

        if (success) {
            log(Level.INFO, "Message has finished.");
            return MessageResult.createSuccess(messageEntry);
        } else {
            return MessageResult.createFailed(messageEntry);
        }
    }

    private void xmlConfigMessage(MessageHandler messageHandler, String fullContent) throws IOException {

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

     private void reboot() throws IOException {
        getLogger().info("Executing Reboot message.");
        getLogger().info("Warning: Device will reboot at the end of the communication session.");
        ((UkHub) protocol).setReboot(true);
    }

    private void enableWebserver() throws IOException {
        getLogger().info("Executing Enable_Webserver message");
        GenericWrite genericWrite = getCosemObjectFactory().getGenericWrite(KEYS_LOCK_DOWN_SWITCH_OBIS, 3, DLMSClassId.KEYS_LOCK_DOWN_SWITCH.getClassId());

        BooleanObject bool = new BooleanObject(false);
        genericWrite.write(bool.getBEREncodedByteArray());
        ((UkHub)protocol).setReboot(true);
    }

    private void disableWebserver() throws IOException {
        getLogger().info("Executing Disable_Webserver message");
        GenericWrite genericWrite = getCosemObjectFactory().getGenericWrite(KEYS_LOCK_DOWN_SWITCH_OBIS, 3, DLMSClassId.KEYS_LOCK_DOWN_SWITCH.getClassId());

        BooleanObject bool = new BooleanObject(true);
        genericWrite.write(bool.getBEREncodedByteArray());
        ((UkHub)protocol).setReboot(true);
    }

    private void modemPingSetup(MessageHandler messageHandler, String content) throws IOException {
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

    private void firmwareUpdate(MessageHandler messageHandler, String content) throws IOException {
        getLogger().info("Executing firmware update message");
        try {
            String base64Encoded = getIncludedContent(content);
            byte[] imageData = new Base64EncoderDecoder().decode(base64Encoded);
            ImageTransfer it = getCosemObjectFactory().getImageTransfer(ObisCodeProvider.FIRMWARE_UPDATE);
            it.upgrade(imageData);
            it.imageActivation();
        } catch (InterruptedException e) {
            String msg = "Firmware upgrade failed! " + e.getClass().getName() + " : " + e.getMessage();
            getLogger().severe(msg);
            throw new IOException(msg);
        }
    }

    private String getIncludedContent(final String content) {
        int begin = content.indexOf(GenericMessaging.INCLUDED_USERFILE_TAG) + GenericMessaging.INCLUDED_USERFILE_TAG.length() + 1;
        int end = content.indexOf(GenericMessaging.INCLUDED_USERFILE_TAG, begin) - 2;
        return content.substring(begin, end);
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

    private void backupZigBeeHanParameters(final MessageHandler messageHandler) throws IOException, BusinessException, SQLException {
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
                throw new BusinessException(e);
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

    private void log(final Level level, final String msg) {
        getLogger().log(level, msg);
    }

    private Logger getLogger() {
        return getDlmsSession().getLogger();
    }

    @Override
    public void doMessage(final RtuMessage rtuMessage) throws BusinessException, SQLException {
        // nothing to do
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

    private Rtu getRtuFromDatabaseBySerialNumber() {
        String serial = this.protocol.getDlmsSession().getProperties().getSerialNumber();
        return mw().getRtuFactory().findBySerialNumber(serial).get(0);
    }

    private int getFolderIdFromHub() {
        return getRtuFromDatabaseBySerialNumber().getFolderId();
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
                    CSVParser csvParser = new CSVParser();
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
                                        RtuMessageShadow rms = new RtuMessageShadow();
                                        rms.setContents(csvParser.getTestObject(i).getData());
                                        rms.setRtuId(getRtuFromDatabaseBySerialNumber().getId());
                                        RtuMessage rm = mw().getRtuMessageFactory().create(rms);
                                        doMessage(rm);
                                        if (rm.getState().getId() == rm.getState().CONFIRMED.getId()) {
                                            to.setResult("OK");
                                        } else {
                                            to.setResult("MESSAGE failed, current state " + rm.getState().getId());
                                        }
                                        hasWritten = true;
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
                    mw().getUserFileFactory().create(csvParser.convertResultToUserFile(uf, getRtuFromDatabaseBySerialNumber().getFolderId()));
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
