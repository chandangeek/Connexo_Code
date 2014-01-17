package com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.ihd.messaging;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.genericprotocolimpl.common.GenericMessageExecutor;
import com.energyict.genericprotocolimpl.common.ParseUtils;
import com.energyict.genericprotocolimpl.common.messages.MessageHandler;
import com.energyict.genericprotocolimpl.nta.messagehandling.NTAMessageHandler;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.UserFile;
import com.energyict.mdc.protocol.api.UserFileFactory;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.exceptions.CommunicationException;
import com.energyict.protocolimpl.base.Base64EncoderDecoder;
import com.energyict.protocolimpl.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.ihd.InHomeDisplay;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides functionality to process messages for the InHomeDisplay
 */
public class InHomeDisplayMessageExecutor extends GenericMessageExecutor {

    public static ObisCode IMAGE_TRANSFER_OBIS = ObisCode.fromString("0.2.44.0.0.255");
    public static ObisCode IMAGE_ACTIVATION_SCHEDULER = ObisCode.fromString("0.0.15.0.2.255");

    private AbstractSmartDlmsProtocol protocol;

    public InHomeDisplayMessageExecutor(AbstractSmartDlmsProtocol protocol) {
        this.protocol = protocol;
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
        } catch (IOException e) {
            log(Level.SEVERE, "Message failed : " + e.getMessage());
            success = false;
        } catch (BusinessException e) {
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

    private void updateFirmware(MessageHandler messageHandler, String content) throws IOException, InterruptedException {
        log(Level.INFO, "Handling message Firmware upgrade");

        String userFileID = messageHandler.getUserFileId();
        if (!ParseUtils.isInteger(userFileID)) {
            throw new IOException("Not a valid entry for the userFile.");
        }
        UserFile uf = this.findUserFile(Integer.parseInt(userFileID));

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

        byte[] imageData = new Base64EncoderDecoder().decode(uf.loadFileInByteArray());
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

    private UserFile findUserFile(int userFileId) {
        return this.getUserFileFactory().findUserFile(userFileId);
    }

    private void log(final Level level, final String msg) {
        getLogger().log(level, msg);
    }

    private Logger getLogger() {
        return ((InHomeDisplay) protocol).getDlmsSession().getLogger();
    }

    @Override
    protected TimeZone getTimeZone() {
        return ((InHomeDisplay) this.protocol).getTimeZone();
    }

    private UserFileFactory getUserFileFactory() {
        List<UserFileFactory> factories = Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(UserFileFactory.class);
        if (factories.isEmpty()) {
            throw CommunicationException.missingModuleException(UserFileFactory.class);
        }
        else {
            return factories.get(0);
        }
    }

}