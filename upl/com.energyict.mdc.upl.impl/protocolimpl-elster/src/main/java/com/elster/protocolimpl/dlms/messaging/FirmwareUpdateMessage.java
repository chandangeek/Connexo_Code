package com.elster.protocolimpl.dlms.messaging;

import com.elster.dlms.cosem.simpleobjectmodel.Ek280Defs;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleCosemObjectManager;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleImageTransferObject;
import com.elster.protocols.streams.TimeoutIOException;
import com.energyict.cbo.BusinessException;
import com.energyict.protocol.MessageEntry;
import sun.misc.BASE64Decoder;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;

/**
 * User: heuckeg
 * Date: 23.09.11
 * Time: 13:33
 */
public abstract class FirmwareUpdateMessage extends AbstractDlmsMessage {

    public static final String MESSAGE_TAG = "FirmwareUpdate";
    public static final String ATTR_CODE_FIRMWAREFILE = "IncludedFile";

    public FirmwareUpdateMessage(DlmsMessageExecutor messageExecutor) {
        super(messageExecutor);
    }

    @Override
    public boolean canExecuteThisMessage(MessageEntry messageEntry) {
        return isMessageTag(MESSAGE_TAG, messageEntry.getContent());
    }

    @Override
    public void executeMessage(MessageEntry messageEntry) throws BusinessException {

        int start = messageEntry.getContent().indexOf("<" + ATTR_CODE_FIRMWAREFILE + ">");
        int end = messageEntry.getContent().indexOf("</" + ATTR_CODE_FIRMWAREFILE + ">");

        String firmwareFile = messageEntry.getContent().substring(start + 2 + ATTR_CODE_FIRMWAREFILE.length(), end);

        try {
            BASE64Decoder decoder = new BASE64Decoder();
            byte[] decodedBytes = decoder.decodeBuffer(firmwareFile);

            String identifier = getFirmwareIdentifier(decodedBytes);

            writeFirmware(decodedBytes, identifier);
        } catch (FWUpdateTimeoutException te) {
            throw new FirmwareUpdateTimeoutException(te.getMessage());
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg == null) {
                msg = e.getClass().getSimpleName();
                e.printStackTrace();
            }
            throw new BusinessException("Firmwareupdate: " + msg);
        }
    }

    public abstract String getFirmwareIdentifier(byte[] decodedBytes) throws NoSuchAlgorithmException;

    public abstract boolean isSameImage(final SimpleImageTransferObject imageTransferObject, final int imageSize,
                                        final String identifier) throws IOException;

    private void writeFirmware(byte[] image, String identifier) throws IOException {

        int imageSize = image.length;
        InputStream imageStream = new ByteArrayInputStream(image);

        SimpleCosemObjectManager objectManager = getExecutor().getDlms().getObjectManager();

        final SimpleImageTransferObject imageTransferObject =
                objectManager.getSimpleCosemObject(Ek280Defs.IMAGE_TRANSFER, SimpleImageTransferObject.class);

        //---- Image transfer muss enabled sein -----
        if (!imageTransferObject.isImageTransferEnabled()) {
            throw new IOException("Image transfer not enabled");
        }

        if (isSameImage(imageTransferObject, imageSize, identifier)   )
        {
            getLogger().info("continuing transfer image");
            switch (imageTransferObject.getImageTransferStatus()) {
                case IMAGE_ACTIVATION_FAILED:
                    throw new IOException("Activation failed (Status)");
                case IMAGE_VERIFICATION_FAILED:
                    throw new IOException("Verification failed (Status)");
                case IMAGE_TRANSFER_NOT_INITIATED:
                    throw new IOException("Device should not provide image data in IMAGE_TRANSFER_NOT_INITIATED state");
                case IMAGE_ACTIVATION_INITIATED:
                    throw new IOException("Unexpected status for device " + imageTransferObject.getImageTransferStatus());
                case IMAGE_VERIFICATION_INITIATED:
                    throw new IOException("Unexpected status for device" + imageTransferObject.getImageTransferStatus());
                case IMAGE_ACTIVATION_SUCCESSFUL:
                    getLogger().info("activation was successful");
                    break;
                case IMAGE_VERIFICATION_SUCCESSFUL:
                    getLogger().info("activate image");
                    imageTransferObject.activateImage();
                    break;
                case IMAGE_TRANSFER_INITIATED:
                    if (!imageTransferObject.isTransferComplete(identifier, imageSize)) {
                        try {
                            getLogger().info("transfer image");
                            imageTransferObject.continueImageTransfer(identifier, imageStream, imageSize);

                            getLogger().info("verify image");
                            imageTransferObject.verifyImage();

                            getLogger().info("activate image");
                            imageTransferObject.activateImage();
                        } catch (TimeoutIOException e) {
                            throw new FWUpdateTimeoutException("Timeout during firmware update");
                        } catch (SocketException se) {
                            throw new FWUpdateTimeoutException("Socket error during firmware update");
                        } catch (EOFException eofe) {
                            String s = "Eof of file exception during firmware update";
                            throw new FWUpdateTimeoutException(s);
                        } catch (Exception ex) {
                            String s = ex.getMessage();
                            if ((s == null) || (s.length() == 0)) {
                                s = "Exception by class " + ex.getClass().getCanonicalName();
                            }
                            throw new IOException(s);
                        }
                    }
                    break;
                default:
                    throw new IOException("Unexpected image transfer status: " + imageTransferObject.getImageTransferStatus());
            }
        } else {
            try {
                getLogger().info("transfer image");
                imageTransferObject.initiateAndTransferImage(identifier, imageStream, imageSize);

                getLogger().info("verify image");
                imageTransferObject.verifyImage();

                getLogger().info("activate image");
                imageTransferObject.activateImage();
            } catch (TimeoutIOException e) {
                throw new FWUpdateTimeoutException("Timeout during firmware update");
            } catch (SocketException se) {
                throw new FWUpdateTimeoutException("Socket error during firmware update");
            } catch (EOFException eofe) {
                String s = "Eof of file exception during firmware update";
                throw new FWUpdateTimeoutException(s);
            } catch (Exception ex) {
                String s = ex.getMessage();
                if ((s == null) || (s.length() == 0)) {
                    s = "Exception by class " + ex.getClass().getCanonicalName();
                }
                throw new IOException(s);
            }
        }
    }
}

