package com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.ihd.messaging;

import com.energyict.cbo.BusinessException;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageResult;
import com.energyict.protocolimpl.base.Base64EncoderDecoder;
import com.energyict.protocolimpl.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.protocolimpl.generic.MessageParser;
import com.energyict.protocolimpl.generic.messages.GenericMessaging;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.AM110RRegisterFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides functionality to process messages for the InHomeDisplay
 */
public class InHomeDisplayMessageExecutor extends MessageParser {

    private static final String RESUME = "resume";

    private AbstractSmartDlmsProtocol protocol;

    public InHomeDisplayMessageExecutor(AbstractSmartDlmsProtocol protocol) {
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

    /**
     * Indicates that each message has to be executed by the protocol.
     *
     * @param messageEntry a definition of which message needs to be sent
     * @return a state of the message which was just sent
     * @throws java.io.IOException if a logical error occurs
     */
    public MessageResult queryMessage(final MessageEntry messageEntry) throws IOException {
        String content = messageEntry.getContent();
        boolean success = true;

        try {
            boolean firmwareUpdate = ((content != null) && content.contains(RtuMessageConstant.FIRMWARE_UPGRADE));

            if (firmwareUpdate) {
                updateFirmware(messageEntry);
            } else {
                log(Level.INFO, "Message not supported : " + content);
                success = false;
            }
        } catch (IOException e) {
            log(Level.SEVERE, "Message failed : " + e.getMessage());
            success = false;
        } catch (InterruptedException e) {
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

    private void updateFirmware(MessageEntry messageEntry) throws IOException, InterruptedException {
        log(Level.INFO, "Upgrade firmware message received.");

        String userFileContent = getIncludedContent(messageEntry.getContent());

        Date activationDate = null;
        String activationDateString = getValueFromXMLTag(RtuMessageConstant.ACTIVATE_DATE, messageEntry.getContent());
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        try {
            if (!activationDateString.equalsIgnoreCase("")) {
                activationDate = formatter.parse(activationDateString);
            }
        } catch (ParseException e) {
            throw new IOException("Error parsing the given activation date: " + e.getMessage());
        }

        byte[] imageData = new Base64EncoderDecoder().decode(userFileContent);
        ImageTransfer it = getCosemObjectFactory().getImageTransfer(AM110RRegisterFactory.IHD_FIRMWARE_UPDATE);
        if ((messageEntry.getTrackingId() != null) && messageEntry.getTrackingId().toLowerCase().contains(RESUME)) {
            int lastTransferredBlockNumber = it.readFirstNotTransferedBlockNumber().intValue();
            if (lastTransferredBlockNumber > 0) {
                it.setStartIndex(lastTransferredBlockNumber);
            }
        }
        it.upgrade(imageData);
        if (activationDate != null && activationDate.after(new Date())) {
            log(Level.INFO, "Writing the upgrade activation date.");
            SingleActionSchedule sas = getCosemObjectFactory().getSingleActionSchedule(AM110RRegisterFactory.IHD_IMAGE_ACTIVATION_SCHEDULER);
            Array dateArray = convertUnixToDateTimeArray(String.valueOf(activationDate.getTime() / 1000));
            sas.writeExecutionTime(dateArray);
        } else {
            log(Level.INFO, "Immediately activating the image.");
            it.imageActivation();
        }
        log(Level.INFO, "Upgrade firmware message finished.");
    }

    private String getIncludedContent(final String content) {
        int begin = content.indexOf(GenericMessaging.INCLUDED_USERFILE_TAG) + GenericMessaging.INCLUDED_USERFILE_TAG.length() + 1;
        int end = content.indexOf(GenericMessaging.INCLUDED_USERFILE_TAG, begin) - 2;
        return content.substring(begin, end);
    }

    private String getValueFromXMLTag(String tag, String content) {
        int startIndex = content.indexOf("<" + tag);
        if (startIndex == -1) {
            return "";  // Optional value is not specified
        }
        int endIndex = content.indexOf("</" + tag);
        try {
            return content.substring(startIndex + tag.length() + 2, endIndex);
        } catch (IndexOutOfBoundsException e) {
            return "";
        }
    }

    private void log(final Level level, final String msg) {
        getLogger().log(level, msg);
    }

    private Logger getLogger() {
        return protocol.getDlmsSession().getLogger();
    }

    @Override
    protected TimeZone getTimeZone() {
        return this.protocol.getTimeZone();
    }
}