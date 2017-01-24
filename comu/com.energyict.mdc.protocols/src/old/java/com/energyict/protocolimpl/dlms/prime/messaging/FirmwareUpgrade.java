package com.energyict.protocolimpl.dlms.prime.messaging;

import com.energyict.mdc.protocol.api.DeviceMessageFileService;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.protocols.messaging.FirmwareUpdateMessageBuilder;

import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.Base64EncoderDecoder;
import com.energyict.protocolimpl.dlms.prime.PrimeProperties;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 5/29/12
 * Time: 8:38 AM
 */
public class FirmwareUpgrade extends PrimeMessageExecutor {

    private static final ObisCode IMAGE_TRANSFER_OBIS = ObisCode.fromString("0.0.44.0.0.255");
    private static final String FIRMWARE_UPDATE = "FirmwareUpdate";

    private final DeviceMessageFileService deviceMessageFileService;

    protected FirmwareUpgrade(DlmsSession session, PrimeProperties properties, DeviceMessageFileService deviceMessageFileService) {
        super(session, properties);
        this.deviceMessageFileService = deviceMessageFileService;
    }

    @Override
    public boolean canHandle(MessageEntry messageEntry) {
        return isMessageTag(FIRMWARE_UPDATE, messageEntry);
    }

    @Override
    public MessageResult execute(MessageEntry messageEntry) throws IOException {
        if (!canHandle(messageEntry)) {
            getLogger().severe("FirmwareUpgrade message executor cannot execute this message [" + messageEntry.getContent() + "]");
            return MessageResult.createFailed(messageEntry);
        }

        getLogger().info("Received a firmware upgrade message, using firmware message builder...");

        try {
            final FirmwareUpdateMessageBuilder builder = new FirmwareUpdateMessageBuilder();
            builder.initFromXml(messageEntry.getContent());

            if (builder.getFirmwareBytes() == null) {
                String message = "The message did not contain a file to use for the upgrade, message fails...";
                getLogger().severe(message);
                throw new IOException(message);
            }

            getLogger().info("Pulling out user file and dispatching to the device...");
            final byte[] base64Data = builder.getFirmwareBytes();

            getLogger().info("Decoding BASE64 content ...");
            final Base64EncoderDecoder b64 = new Base64EncoderDecoder();
            final byte[] data = b64.decode(base64Data);

            if (data.length == 0) {
                final String message = "Length of the upgrade file is not valid [" + data.length + " bytes], failing message.";
                getLogger().severe(message);
                throw new IOException(message);
            }

            final ImageTransfer imageTransfer = getSession().getCosemObjectFactory().getImageTransfer(IMAGE_TRANSFER_OBIS);
            imageTransfer.setUsePollingVerifyAndActivate(true);
            imageTransfer.setPollingDelay(getProperties().getPollingDelay());
            imageTransfer.setPollingRetries(getProperties().getPollingRetries());
            getLogger().info("Upgrading firmware, using '" + getProperties().getFWImageNameWithoutExtension() + "' as image_identifier");
            imageTransfer.upgrade(data, false, getProperties().getFWImageNameWithoutExtension(), false);
            imageTransfer.imageActivation();

            getLogger().info("Firmware upgrade was successful.");
            return MessageResult.createSuccess(messageEntry);

        } catch (SAXException e) {
            getLogger().severe("Unable to get firmware image contents from xml: " + e.getMessage());
            return MessageResult.createFailed(messageEntry);
        }

    }

}
