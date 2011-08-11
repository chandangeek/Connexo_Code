package com.energyict.smartmeterprotocolimpl.eict.ukhub.messaging;

import com.energyict.cbo.BusinessException;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.attributeobjects.RegisterZigbeeDeviceData;
import com.energyict.dlms.cosem.attributeobjects.ZigBeeIEEEAddress;
import com.energyict.genericprotocolimpl.common.GenericMessageExecutor;
import com.energyict.genericprotocolimpl.common.messages.MessageHandler;
import com.energyict.genericprotocolimpl.nta.messagehandling.NTAMessageHandler;
import com.energyict.mdw.core.*;
import com.energyict.mdw.shadow.UserFileShadow;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageResult;
import com.energyict.protocolimpl.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.protocolimpl.dlms.common.DlmsSession;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.sql.SQLException;
import java.util.TimeZone;
import java.util.logging.Level;

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

    private void changeHanSAS(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Sending message : Change Zigbee HAN SAS");
        String panId = messageHandler.getChangeHanSasPanId();
        String channel = messageHandler.getChangeHanSasPanId();

        String insecureJoin = messageHandler.getChangeHanSasInsecureJoin();

        // TODO: Implement this method. DLMS objects not implemented yet as well, so we'll have to do that first.

        throw new IOException("Not implemented yet!");
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

        Structure backUpData = new Structure(uf.loadFileInByteArray(), 0, 0);

        // write the externalPANID
        // write the linkkey
        // call the restore function
        // set MAC addresses?

    }

    private void backupZigBeeHanParameters(final MessageHandler messageHandler) throws IOException, BusinessException, SQLException {
        log(Level.INFO, "Sending message : Backup ZigBee Han Keys");
        ZigbeeHanManagement hanManagement = getCosemObjectFactory().getZigbeeHanManagement();
        hanManagement.backup();

        // TODO Check if we need to wait a certain period before the HUB created the Backup of the data

        // TODO this requires dataBase access, the message will not succeed in an HTTP ComServer environment
        Structure backUpData = hanManagement.readBackupData();
        UserFileShadow ufs = ProtocolTools.createUserFileShadow("ZigBeeBackUp_" + protocol.getDlmsSession().getProperties().getSerialNumber(), backUpData.getBEREncodedByteArray(), getFolderIdFromHub(), "bin");
        mw().getUserFileFactory().create(ufs);
        log(Level.INFO, "Backed-up ZigBee parameters in userFile : ZigBeeBackUp_" + protocol.getDlmsSession().getProperties().getSerialNumber());
    }

    private void joinZigBeeSlave(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Sending message : Join ZigBee slave");
        String address = messageHandler.getJoinZigBeeIEEEAddress();
        String key = messageHandler.getJoinZigBeeLinkKey();
        ZigBeeSETCControl zigBeeSETCControl = getCosemObjectFactory().getZigBeeSETCControl();
        zigBeeSETCControl.registerDevice(new RegisterZigbeeDeviceData(address, key));
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
        getDlmsSession().getLogger().log(level, msg);
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
}
