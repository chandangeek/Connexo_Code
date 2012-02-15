package com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.ihd.messaging;

import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.genericprotocolimpl.common.messages.GenericMessaging;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.ihd.InHomeDisplay;
import sun.misc.BASE64Decoder;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides functionality to process messages for the InHomeDisplay
 */
public class InHomeDisplayMessaging extends GenericMessaging implements MessageProtocol{

    public static ObisCode IMAGE_TRANSFER_OBIS = ObisCode.fromString("0.2.44.0.0.255");
    public static String FIRMWARE_UPGRADE_TAG = "FirmwareUpdate";

    private MessageProtocol protocol;

    public InHomeDisplayMessaging(MessageProtocol protocol) {
        this.protocol = protocol;
    }

    /**
     * Abstract method to define your message categories *
     */
    @Override
    public List getMessageCategories() {
        return Collections.emptyList();
    }

    /**
     * Provides the full list of outstanding messages to the protocol.
     * If for any reason certain messages have to be grouped before they are sent to a device, then this is the place to do it.
     * At a later timestamp the framework will query each {@link com.energyict.protocol.MessageEntry} (see {@link #queryMessage(com.energyict.protocol.MessageEntry)}) to actually
     * perform the message.
     *
     * @param messageEntries a list of {@link com.energyict.protocol.MessageEntry}s
     * @throws java.io.IOException if a logical error occurs
     */
    public void applyMessages(final List messageEntries) throws IOException {
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
           if (isFirmwareUpdateMessage(content)) {
                updateFirmware(content);
           }else {
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

    private void log(final Level level, final String msg) {
        getLogger().log(level, msg);
    }

    private Logger getLogger() {
        return ((InHomeDisplay) protocol).getDlmsSession().getLogger();
    }

    private boolean isFirmwareUpdateMessage(String messageContent) {
        return (messageContent != null) && messageContent.contains(FIRMWARE_UPGRADE_TAG);
    }

    private void updateFirmware(String content) throws IOException {
        getLogger().info("Executing firmware update message");
        try {
            String base64Encoded = getIncludedContent(content);
            byte[] imageData = new BASE64Decoder().decodeBuffer(base64Encoded);
            ImageTransfer it = ((InHomeDisplay) protocol).getDlmsSession().getCosemObjectFactory().getImageTransfer(IMAGE_TRANSFER_OBIS);
            it.upgrade(imageData);
            it.imageActivation();
        } catch (InterruptedException e) {
            String msg = "Firmware upgrade failed! " + e.getClass().getName() + " : " + e.getMessage();
            getLogger().severe(msg);
            throw new IOException(msg);
        }
    }

     private String getIncludedContent(final String content) {
        int begin = content.indexOf(GenericMessaging.INCLUDED_USERFILE_TAG) + GenericMessaging.INCLUDED_USERFILE_TAG.length() + 1;
        int end = content.indexOf(GenericMessaging.INCLUDED_USERFILE_TAG, begin) - 2;
        return content.substring(begin, end);
    }
}