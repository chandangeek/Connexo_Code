package com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.ihd.messaging;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.mdc.upl.io.NestedIOException;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageResult;
import com.energyict.protocolimpl.base.Base64EncoderDecoder;
import com.energyict.protocolimpl.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.protocolimpl.generic.MessageParser;
import com.energyict.protocolimpl.generic.messages.MessageHandler;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.smartmeterprotocolimpl.eict.NTAMessageHandler;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.ihd.InHomeDisplay;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides functionality to process messages for the InHomeDisplay
 */
public class InHomeDisplayMessageExecutor extends MessageParser {

    public static ObisCode IMAGE_TRANSFER_OBIS = ObisCode.fromString("0.2.44.0.0.255");
    public static ObisCode IMAGE_ACTIVATION_SCHEDULER = ObisCode.fromString("0.0.15.0.2.255");

    private final DeviceMessageFileFinder messageFileFinder;
    private final DeviceMessageFileExtractor messageFileExtractor;
    private AbstractSmartDlmsProtocol protocol;

    public InHomeDisplayMessageExecutor(AbstractSmartDlmsProtocol inHomeDisplay, DeviceMessageFileFinder messageFileFinder, DeviceMessageFileExtractor messageFileExtractor) {
        this.protocol = inHomeDisplay;
        this.messageFileFinder = messageFileFinder;
        this.messageFileExtractor = messageFileExtractor;
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
        MessageHandler messageHandler = new NTAMessageHandler();
        boolean success = true;

        try {
            importMessage(content, messageHandler);
            boolean firmwareUpdate = messageHandler.getType().equals(RtuMessageConstant.FIRMWARE_UPGRADE);

            if (firmwareUpdate) {
                updateFirmware(messageHandler, content);
            } else {
                log(Level.INFO, "Message not supported : " + content);
                success = false;
            }
        } catch (IOException | IllegalArgumentException e) {
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

    private void updateFirmware(MessageHandler messageHandler, String content) throws IOException {
        log(Level.INFO, "Handling message Firmware upgrade");

        String userFileID = messageHandler.getUserFileId();
        Optional<DeviceMessageFile> deviceMessageFile = messageFileFinder.from(userFileID);
        if (!deviceMessageFile.isPresent()) {
            String str = "Not a valid entry for the userfileID " + userFileID;
            throw new IOException(str);
        }
        byte[] bytes = messageFileExtractor.binaryContents(deviceMessageFile.get());

        String[] parts = content.split("=");
        Date date = null;
        try {
            if (parts.length > 2) {
                String dateString = parts[2].substring(1).split("\"")[0];

                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                date = formatter.parse(dateString);
            }
        } catch (ParseException e) {
            log(Level.SEVERE, "Error while parsing the activation date: " + e.getMessage());
            throw new NestedIOException(e);
        } catch (NumberFormatException e) {
            log(Level.SEVERE, "Error while parsing the time duration: " + e.getMessage());
            throw new NestedIOException(e);
        }

        byte[] imageData = new Base64EncoderDecoder().decode(bytes);
        ImageTransfer it = ((InHomeDisplay) protocol).getCosemObjectFactory().getImageTransfer(IMAGE_TRANSFER_OBIS);
        it.upgrade(imageData);
        if (date != null) {
            SingleActionSchedule sas = ((InHomeDisplay) protocol).getCosemObjectFactory().getSingleActionSchedule(IMAGE_ACTIVATION_SCHEDULER);
            Array dateArray = convertUnixToDateTimeArray(String.valueOf(date.getTime() / 1000));
            sas.writeExecutionTime(dateArray);
        } else {
            it.imageActivation();
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