package com.energyict.smartmeterprotocolimpl.eict.ukhub.messaging;

import com.energyict.cbo.BusinessException;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.attributeobjects.RegisterZigbeeDeviceData;
import com.energyict.genericprotocolimpl.common.GenericMessageExecutor;
import com.energyict.genericprotocolimpl.common.messages.MessageHandler;
import com.energyict.genericprotocolimpl.nta.messagehandling.NTAMessageHandler;
import com.energyict.mdw.core.RtuMessage;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageResult;
import com.energyict.protocolimpl.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.protocolimpl.dlms.common.DlmsSession;
import com.energyict.protocolimpl.messages.RtuMessageConstant;

import java.io.IOException;
import java.sql.SQLException;
import java.util.TimeZone;
import java.util.logging.Level;

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

            boolean createHan = messageHandler.getType().equals(RtuMessageConstant.CREATE_HAN_NETWORK);
            boolean joinZigbeeSlave = messageHandler.getType().equals(RtuMessageConstant.JOIN_ZIGBEE_SLAVE);

            if (createHan) {
                createHanNetwork(messageHandler);
            } else if (joinZigbeeSlave) {
                joinZigbeeSlave(messageHandler);
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
        }

        if (success) {
            log(Level.INFO, "Message has finished.");
            return MessageResult.createSuccess(messageEntry);
        } else {
            return MessageResult.createFailed(messageEntry);
        }
    }

    private void joinZigbeeSlave(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Sending message : Join Zigbee slave");
        String address = messageHandler.getJoinZigBeeIEEEAddress();
        String key = messageHandler.getJoinZigBeeLinkKey();
        ZigBeeSETCControl zigBeeSETCControl = getCosemObjectFactory().getZigBeeSETCControl();
        zigBeeSETCControl.registerDevice(new RegisterZigbeeDeviceData(address, key));
        zigBeeSETCControl.writeEnableDisableJoining(true);
    }

    private void createHanNetwork(final MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Sending message : Create HAN Network");
        ZigbeeHanManagement hanManagement = getCosemObjectFactory().getZigbeeHanManagement();
        hanManagement.createHan(new Integer8(0));
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

}
