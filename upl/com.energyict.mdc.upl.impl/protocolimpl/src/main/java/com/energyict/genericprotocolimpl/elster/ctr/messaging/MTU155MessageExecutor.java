package com.energyict.genericprotocolimpl.elster.ctr.messaging;

import com.energyict.cbo.BusinessException;
import com.energyict.genericprotocolimpl.common.GenericMessageExecutor;
import com.energyict.genericprotocolimpl.elster.ctr.GprsRequestFactory;
import com.energyict.mdw.core.RtuMessage;
import com.energyict.protocol.MessageEntry;

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
    private GprsRequestFactory factory;

    public MTU155MessageExecutor(Logger logger, GprsRequestFactory factory) {
        this.factory = factory;
        this.logger = logger;
    }

    @Override
    public void doMessage(RtuMessage rtuMessage) throws BusinessException, SQLException {
        boolean success = false;
        try {
            String content = rtuMessage.getContents();
            String trackingId = rtuMessage.getTrackingId();
            MessageEntry messageEntry = new MessageEntry(content, trackingId);

            if (new ApnSetupMessage(this).canExecuteThisMessage(messageEntry)) {
                new ApnSetupMessage(this).executeMessage(messageEntry);
                success = true;
            } else if (new SMSCenterSetupMessage(this).canExecuteThisMessage(messageEntry)) {
                new SMSCenterSetupMessage(this).executeMessage(messageEntry);
                success = true;
            } else {
                throw new BusinessException("Received unknown message: " + rtuMessage.toString());
            }
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
        return getFactory().getTimeZone();
    }

    public GprsRequestFactory getFactory() {
        return factory;
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(getClass().getName());
        }
        return logger;
    }
}
