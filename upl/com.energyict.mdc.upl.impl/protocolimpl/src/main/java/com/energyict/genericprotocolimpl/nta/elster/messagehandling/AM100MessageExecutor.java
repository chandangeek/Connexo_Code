package com.energyict.genericprotocolimpl.nta.elster.messagehandling;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.Data;
import com.energyict.genericprotocolimpl.common.messages.MessageHandler;
import com.energyict.genericprotocolimpl.common.messages.RtuMessageConstant;
import com.energyict.genericprotocolimpl.nta.abstractnta.AbstractNTAProtocol;
import com.energyict.genericprotocolimpl.nta.messagehandling.MessageExecutor;
import com.energyict.mdw.core.RtuMessage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * <p>
 * Copyrights EnergyICT
 * Date: 27-jul-2010
 * Time: 11:02:04
 * </p>
 */
public class AM100MessageExecutor extends MessageExecutor {

    public AM100MessageExecutor(AbstractNTAProtocol webRTUKP) {
        super(webRTUKP);
    }

    @Override
    public void doMessage(RtuMessage rtuMessage) throws BusinessException, SQLException {
        boolean success = false;
        String content = rtuMessage.getContents();
        MessageHandler messageHandler = new MessageHandler();
        try {
            importMessage(content, messageHandler);

            boolean mbusInstall = messageHandler.getType().equals(RtuMessageConstant.MBUS_INSTALL);
            if (mbusInstall) {
                log(Level.INFO, "Handling Message " + rtuMessage.displayString() + ": Installing MBus device");

                byte[] serial = "00000000000000000".getBytes();
                System.arraycopy(messageHandler.getMbusInstallEquipmentId().getBytes(), 0, serial, serial.length -
                        messageHandler.getMbusInstallEquipmentId().length() - 1, messageHandler.getMbusInstallEquipmentId().length());

                Data serialNumb = getCosemObjectFactory().getData(getMeterConfig().getMbusSerialNumber(messageHandler.getMbusInstallChannel()).getObisCode());
                serialNumb.setValueAttr(OctetString.fromString(new String (serial)));
//                getCosemObjectFactory().getGenericWrite(getMeterConfig().getMbusSerialNumber(messageHandler.getMbusInstallChannel()))
//                        .write(OctetString.fromString(messageHandler.getMbusInstallEquipmentId()).getBEREncodedByteArray());
                success = true;

            } else {
                super.doMessage(rtuMessage);    //To change body of overridden methods use File | Settings | File Templates.
            }

        } catch (BusinessException e) {
            e.printStackTrace();
            log(Level.INFO, "Message " + rtuMessage.displayString() + " has failed. " + e.getMessage());
        } catch (
                ConnectionException e) {
            e.printStackTrace();
            log(Level.INFO, "Message " + rtuMessage.displayString() + " has failed. " + e.getMessage());
        } catch (
                IOException e) {
            e.printStackTrace();
            log(Level.INFO, "Message " + rtuMessage.displayString() + " has failed. " + e.getMessage());
        } finally {
            if (success) {
                rtuMessage.confirm();
                log(Level.INFO, "Message " + rtuMessage.displayString() + " has finished.");
            } else {
                rtuMessage.setFailed();
            }
        }
    }
}
