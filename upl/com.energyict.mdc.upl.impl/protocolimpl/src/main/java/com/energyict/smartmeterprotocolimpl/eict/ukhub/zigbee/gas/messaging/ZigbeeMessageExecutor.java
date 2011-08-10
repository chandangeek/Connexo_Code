package com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.messaging;

import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.messaging.TimeOfUseMessageBuilder;
import com.energyict.protocolimpl.base.ActivityCalendarController;
import com.energyict.protocolimpl.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.protocolimpl.dlms.common.DlmsSession;
import com.energyict.smartmeterprotocolimpl.elster.apollo.messaging.AS300TimeOfUseMessageBuilder;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 10-aug-2011
 * Time: 15:02:34
 */
public class ZigbeeMessageExecutor {

    private final AbstractSmartDlmsProtocol protocol;
    private ActivityCalendarController activityCalendarController;

    private boolean success;

    public ZigbeeMessageExecutor(final AbstractSmartDlmsProtocol protocol) {
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

        try {
            if (isTimeOfUseMessage(content)) {
                updateTimeOfUse(content);
            } else {
                log(Level.INFO, "Message not supported : " + content);
                success = false;
            }
        } catch (IOException e) {
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

    private void updateTimeOfUse(final String content) throws IOException {
        log(Level.INFO, "Received update ActivityCalendar message.");
        final ZigbeeTimeOfUseMessageBuilder builder = new ZigbeeTimeOfUseMessageBuilder();

        try {
            builder.initFromXml(content);

            if (builder.getCodeId() > 0) { // codeTable implementation
                log(Level.FINEST, "Parsing the content of the CodeTable.");
                getActivityCalendarController().parseContent(content);
                log(Level.FINEST, "Setting the new Passive Calendar Name.");
                getActivityCalendarController().writeCalendarName("");
                log(Level.FINEST, "Sending out the new Passive Calendar objects.");
                getActivityCalendarController().writeCalendar();
            } else if (builder.getUserFile() != null) { // userFile implementation
                log(Level.FINEST, "Getting UserFile from message");
                final byte[] userFileData = builder.getUserFile().loadFileInByteArray();
                if (userFileData.length > 0) {
                    log(Level.WARNING, "Currently No UserFile Support, message will fail.");
                    success = false;
                } else {
                    log(Level.WARNING, "Length of the ActivityCalendar UserFile is not valid [" + userFileData + " bytes], failing message.");
                    success = false;
                }
            }

        } catch (SAXException e) {
            log(Level.SEVERE, "Cannot process ActivityCalendar upgrade message due to an XML parsing error [" + e.getMessage() + "]");
            success = false;
        }

    }

    private boolean isTimeOfUseMessage(final String messageContent) {
        return (messageContent != null) && messageContent.contains(TimeOfUseMessageBuilder.getMessageNodeTag());
    }

    public ActivityCalendarController getActivityCalendarController() {
        if (this.activityCalendarController == null) {
            this.activityCalendarController = new ZigbeeActivityCalendarController(this.protocol);
        }
        return activityCalendarController;
    }

    private void log(Level level, String message) {
        this.protocol.getLogger().log(level, message);
    }
}

