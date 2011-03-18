package com.energyict.genericprotocolimpl.nta.elster.messagehandling;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.Data;
import com.energyict.genericprotocolimpl.common.messages.MessageHandler;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.genericprotocolimpl.nta.abstractnta.AbstractMbusDevice;
import com.energyict.genericprotocolimpl.nta.messagehandling.MbusMessageExecutor;
import com.energyict.mdw.core.RtuMessage;
import com.energyict.mdw.shadow.RtuShadow;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 5-jan-2011
 * Time: 11:29:45
 */
public class  AM100MbusMessageExecutor extends MbusMessageExecutor {

    /**
     * Constructor matching super
     *
     * @param mbusDevice the messageProvider class
     */
    public AM100MbusMessageExecutor(AbstractMbusDevice mbusDevice) {
        super(mbusDevice);
    }

    /**
     * Execute the given RtuMessage
     *
     * @param rtuMessage the RtuMessage to execute
     * @throws BusinessException
     * @throws SQLException
     */
    @Override
    public void doMessage(RtuMessage rtuMessage) throws BusinessException, SQLException {
        boolean success = false;
        String content = rtuMessage.getContents();
        MessageHandler messageHandler = new MessageHandler();
        try {
            importMessage(content, messageHandler);

            boolean mbusDeinstall = messageHandler.getType().equals(RtuMessageConstant.MBUS_DECOMMISSION);
            if (mbusDeinstall) {
                log(Level.INFO, "Handling Message " + rtuMessage.displayString() + ": Deinstalling MBus device");

                Data serialNumb = getCosemObjectFactory().getData(getMeterConfig().getMbusSerialNumber(getMbusDevice().getPhysicalAddress()).getObisCode());
                serialNumb.setValueAttr(OctetString.fromString(new String("")));

                RtuShadow shadow = getMbusDevice().getMbus().getShadow();
                shadow.setGatewayId(0);
                getMbusDevice().getMbus().update(shadow);

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
