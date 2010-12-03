package com.energyict.genericprotocolimpl.elster.ctr.messaging;

import com.energyict.cbo.BusinessException;
import com.energyict.genericprotocolimpl.common.GenericMessageExecutor;
import com.energyict.mdw.core.RtuMessage;

import java.sql.SQLException;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 3-dec-2010
 * Time: 14:41:00
 */
public class MTU155MessageExecutor extends GenericMessageExecutor {

    private Logger logger;

    @Override
    public void doMessage(RtuMessage rtuMessage) throws BusinessException, SQLException {
        boolean success = false;
        try {

            String content = rtuMessage.getContents();




        } finally {
            if (success) {
                rtuMessage.confirm();
                getLogger().info("Message " + rtuMessage.displayString() + " has finished successfully.");
            } else {
                rtuMessage.setFailed();
                getLogger().info("Message " + rtuMessage.displayString() + " has finished unsuccessfully.");
            }
        }
    }

    @Override
    protected TimeZone getTimeZone() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(getClass().getName());
        }
        return logger;
    }
}
