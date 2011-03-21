package com.energyict.genericprotocolimpl.elster.AM100R.Apollo.messages;

import com.energyict.cbo.BusinessException;
import com.energyict.genericprotocolimpl.common.GenericMessageExecutor;
import com.energyict.genericprotocolimpl.common.messages.MessageHandler;
import com.energyict.genericprotocolimpl.elster.AM100R.Apollo.ApolloMeter;
import com.energyict.mdw.core.RtuMessage;
import com.energyict.protocolimpl.base.ActivityCalendarController;
import com.energyict.protocolimpl.messages.RtuMessageConstant;

import java.io.IOException;
import java.sql.SQLException;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 17-mrt-2011
 * Time: 8:51:14
 */
public class MessageExecutor extends GenericMessageExecutor {

    private final ApolloMeter protocol;

    private ApolloActivityCalendarController activityCalendarController;

    public MessageExecutor(final ApolloMeter protocol) {
        this.protocol = protocol;
    }

    @Override
    public void doMessage(final RtuMessage rtuMessage) throws BusinessException, SQLException {
        boolean success = false;
        String content = rtuMessage.getContents();
        MessageHandler messageHandler = new MessageHandler();


        try {
            importMessage(content, messageHandler);

            boolean timeOfUseMessage = messageHandler.getType().equals(RtuMessageConstant.TOU_ACTIVITY_CAL);
            if (timeOfUseMessage) {
                getLogger().log(Level.INFO, "Received update ActivityCalendar message.");
                getLogger().log(Level.FINEST, "Parsing the content of the CodeTable.");
                getActivityCalendarController().parseContent(content);
                getLogger().log(Level.FINEST, "Setting the new Passive Calendar Name.");
                getActivityCalendarController().writeCalendarName("");
                getLogger().log(Level.FINEST, "Sending out the new Passive Calendar objects.");
                getActivityCalendarController().writeCalendar();
                success = true;
            }
        } catch (IOException e) {
            getLogger().log(Level.INFO, "Message " + rtuMessage.displayString() + " has failed. " + e.getMessage());
        } finally {
            if (success) {
                rtuMessage.confirm();
                getLogger().log(Level.INFO, "Message " + rtuMessage.displayString() + " has finished.");
            } else {
                rtuMessage.setFailed();
            }
        }

        //TODO implement proper functionality.
    }

    @Override
    protected TimeZone getTimeZone() {
        return this.protocol.getTimeZone();
    }

    private ApolloActivityCalendarController getActivityCalendarController() {
        return this.protocol.getActivityCalendarController();
    }

    private Logger getLogger() {
        return this.protocol.getLogger();
    }
}
