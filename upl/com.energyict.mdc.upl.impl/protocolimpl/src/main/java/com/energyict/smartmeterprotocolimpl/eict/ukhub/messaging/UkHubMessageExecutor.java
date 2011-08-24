package com.energyict.smartmeterprotocolimpl.eict.ukhub.messaging;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.BusinessException;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.attributeobjects.RegisterZigbeeDeviceData;
import com.energyict.dlms.cosem.attributeobjects.ZigBeeIEEEAddress;
import com.energyict.dlms.cosem.ZigBeeSASStartup;
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
import com.energyict.protocolimpl.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.protocolimpl.dlms.common.DlmsSession;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.ObisCodeProvider;
import com.energyict.smartmeterprotocolimpl.elster.apollo.messaging.AS300FirmwareUpdateMessageBuilder;
import com.jidesoft.filter.NextWeekFilter;
import org.apache.axis.encoding.Base64;

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
            boolean removeZigBeeSlave = messageHandler.getType().equals(RtuMessageConstant.REMOVE_ZIGBEE_SLAVE);
            boolean removeAllZigBeeSlaves = messageHandler.getType().equals(RtuMessageConstant.REMOVE_ALL_ZIGBEE_SLAVES);
            boolean backupZigBeeHanParameters = messageHandler.getType().equals(RtuMessageConstant.BACKUP_ZIGBEE_HAN_PARAMETERS);
            boolean restoreZigBeeParameters = messageHandler.getType().equals(RtuMessageConstant.RESTORE_ZIGBEE_HAN_PARAMETERS);
            boolean firmwareUpdate = messageHandler.getType().equals(RtuMessageConstant.FIRMWARE_UPDATE);
            boolean testMessage = messageHandler.getType().equals(RtuMessageConstant.TEST_MESSAGE);

            if (changeHanSAS) {
                changeHanSAS(messageHandler);
            } else if (createHan) {
                createHanNetwork(messageHandler);
            } else if (removeHan) {
                removeHanNetwork(messageHandler);
            } else if (joinZigBeeSlave) {
                joinZigBeeSlave(messageHandler);
            } else if (removeZigBeeSlave) {
                removeZigBeeSlave(messageHandler);
            } else if (removeAllZigBeeSlaves) {
                removeAllZigBeeSlaves(messageHandler);
            } else if (backupZigBeeHanParameters) {
                backupZigBeeHanParameters(messageHandler);
            } else if (restoreZigBeeParameters) {
                restoreZigBeeHanParameters(messageHandler);
            } else if (firmwareUpdate) {
                firmwareUpdate(messageHandler, content);
            } else if (testMessage){
                testMessage(messageHandler);
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

    private void firmwareUpdate(MessageHandler messageHandler, String content) throws IOException {
        getLogger().info("Executing firmware update message");
        try {
            String base64Encoded = getIncludedContent(content);
            byte[] imageData = Base64.decode(base64Encoded);
            System.out.println("Raw firmware content: [" + ProtocolTools.getHexStringFromBytes(imageData) + "]");
            ImageTransfer it = getCosemObjectFactory().getImageTransfer(ObisCodeProvider.FIRMWARE_UPDATE);
            System.out.println("ImageTransfer: [" + it + "]");
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

    private boolean isFirmwareUpdateMessage(String messageContent) {
        return (messageContent != null) && messageContent.contains(AS300FirmwareUpdateMessageBuilder.getMessageNodeTag());
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
            OctetString extendedPanIdObject = new OctetString(extendedPanIdValue);
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

    private void restoreZigBeeHanParameters(final MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Sending message : Backup ZigBee Han Keys");
        int userFileId = messageHandler.getRestoreHanParametersUserFileId();
        if (userFileId == -1) {
            throw new IOException("Invalid UserFileId value : " + userFileId);
        }

        UserFile uf = mw().getUserFileFactory().find(userFileId);
        if (uf == null) {
            throw new IOException("No UserFile found with ID : " + userFileId);
        }

        HanBackupRestoreData hanBackUpData = new HanBackupRestoreData(uf.loadFileInByteArray(), 0, 0);
        ZigBeeSASStartup sasStartup = getCosemObjectFactory().getZigBeeSASStartup();
        log(Level.FINE, "Writing : ExtendedPanId");
        sasStartup.writeExtendedPanId(hanBackUpData.getExtendedPanId());
        log(Level.FINE, "Writing : LinkKey");
        sasStartup.writeLinkKey(hanBackUpData.getLinkKey());
        log(Level.FINE, "Writing : NetworkKey");
        sasStartup.writeNetworkKey(hanBackUpData.getNetworkKey());

        ZigbeeHanManagement hanManagement = getCosemObjectFactory().getZigbeeHanManagement();
        log(Level.FINE, "Writing : RestoreData Structure");
        hanManagement.restore(hanBackUpData.getRestoreData());
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
        RegisterZigbeeDeviceData zigbeeDeviceData = new RegisterZigbeeDeviceData(address, key);

        System.out.println("\n\n");
        System.out.println(zigbeeDeviceData);
        System.out.println(ProtocolTools.getHexStringFromBytes(zigbeeDeviceData.getBEREncodedByteArray(), " "));

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
        ZigBeeIEEEAddress ieeeAddress = new ZigBeeIEEEAddress(getBytesFromHexString(address));
        getCosemObjectFactory().getZigBeeSETCControl().unRegisterDevice(ieeeAddress);
    }

    private void removeAllZigBeeSlaves(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Sending message : Remove all ZigBee slaves");
        getCosemObjectFactory().getZigBeeSETCControl().unRegisterAllDevices();
    }

    private void createHanNetwork(final MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Sending message : Create HAN Network");
        ZigbeeHanManagement hanManagement = getCosemObjectFactory().getZigbeeHanManagement();
        hanManagement.createHan(new Integer8(0));
    }

    private void removeHanNetwork(final MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Sending message : Remove HAN Network");
        throw new IOException("Not implemented yet. Remove HAN not available (yet?) in ZigBee DLMS objects.");
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
