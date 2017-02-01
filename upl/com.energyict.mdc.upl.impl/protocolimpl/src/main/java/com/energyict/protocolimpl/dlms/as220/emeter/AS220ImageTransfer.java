/**
 *
 */
package com.energyict.protocolimpl.dlms.as220.emeter;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.messaging.FirmwareUpdateMessageBuilder;
import com.energyict.protocolimpl.base.Base64EncoderDecoder;
import com.energyict.protocolimpl.dlms.as220.AS220;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;

/**
 * @author gna
 * @since 12-mrt-2010
 */
public class AS220ImageTransfer {

    private final AS220Messaging messaging;
    private final MessageEntry messageEntry;
    private final DeviceMessageFileFinder deviceMessageFileFinder;
    private final DeviceMessageFileExtractor deviceMessageFileExtractor;

    private final ImageTransfer imageTransfer;

    private Unsigned32 size = null;    // the size of the image
    private byte[] data = null; // the complete image in byte
    private int blockCount = -1; // the amount of block numbers

    private final int maxBlockRetryCount = 3;
    private final int maxTotalRetryCount = 500;

    /**
     * @param messaging
     * @param messageEntry
     * @param deviceMessageFileFinder
     *@param deviceMessageFileExtractor @throws IOException
     */
    public AS220ImageTransfer(AS220Messaging messaging, MessageEntry messageEntry, DeviceMessageFileFinder deviceMessageFileFinder, DeviceMessageFileExtractor deviceMessageFileExtractor) throws IOException {
        this.messaging = messaging;
        this.messageEntry = messageEntry;
        this.deviceMessageFileFinder = deviceMessageFileFinder;
        this.deviceMessageFileExtractor = deviceMessageFileExtractor;
//		this.imageTransfer = getAs220().getCosemObjectFactory().getImageTransferSN();
        this.imageTransfer = getAs220().getCosemObjectFactory().getImageTransferSN();
    }


    /**
     * @throws IOException
     */
    public void initiate() throws IOException {
        getAs220().getLogger().info("Received a firmware upgrade message, using firmware message builder...");
        String errorMessage = "";
        final FirmwareUpdateMessageBuilder builder = new FirmwareUpdateMessageBuilder(deviceMessageFileFinder, deviceMessageFileExtractor);

        try {
            builder.initFromXml(messageEntry.getContent());
        } catch (final IOException e) {
            errorMessage = "Got an IO error when loading firmware message content [" + e.getMessage() + "]";
            if (getAs220().getLogger().isLoggable(Level.SEVERE)) {
                getAs220().getLogger().log(Level.SEVERE, errorMessage, e);
            }
            throw new IOException(errorMessage + e.getMessage());
        } catch (SAXException e) {
            errorMessage = "Cannot process firmware upgrade message due to an XML parsing error [" + e.getMessage() + "]";
            getAs220().getLogger().log(Level.SEVERE, errorMessage, e);
            throw new IOException(errorMessage + e.getMessage());
        }

        // We requested an inlined file...
        if (builder.getUserFileContent() != null) {
            getAs220().getLogger().info("Pulling out user file and dispatching to the device...");

            this.data = builder.getUserFileContent().getBytes();

            if (this.data.length == 0) {
                errorMessage = "Length of the upgrade file is not valid [" + this.data.length + " bytes], failing message.";
                if (getAs220().getLogger().isLoggable(Level.WARNING)) {
                    getAs220().getLogger().log(Level.WARNING, errorMessage);
                }
                throw new IOException(errorMessage);

            }
        } else {
            errorMessage = "The message did not contain a user file to use for the upgrade, message fails...";
            getAs220().getLogger().log(Level.WARNING, errorMessage);

            throw new IOException(errorMessage);
        }

        getAs220().getLogger().info("Converting received image to binary using a Base64 decoder...");
        final Base64EncoderDecoder decoder = new Base64EncoderDecoder();
        this.data = decoder.decode(new String(this.data));

    }

    /**
     * @throws IOException
     * @throws InterruptedException
     */
    public void upgrade() throws IOException {
        this.size = new Unsigned32(data.length);

        getAs220().getLogger().info("Upgrading AM500 module with new firmware image of size [" + this.size + "] bytes");

        // Set the imageTransferEnabledState to true (otherwise the upgrade can not be performed)
        imageTransfer.writeImageTransferEnabledState(true);

        if (imageTransfer.getImageTransferEnabledState().getState()) {

            // Step1: Get the maximum image block size
            // and calculate the amount of blocks in one step
            this.blockCount = (int) (this.size.getValue() / imageTransfer.readImageBlockSize().getValue()) + (((this.size.getValue() % imageTransfer.readImageBlockSize().getValue()) == 0) ? 0 : 1);

            // Step2: Initiate the image transfer
            Structure imageInitiateStructure = new Structure();
            imageInitiateStructure.addDataType(OctetString.fromString("NewImage"));    // it's a default name for the new image
            imageInitiateStructure.addDataType(this.size);

            imageTransfer.imageTransferInitiate(imageInitiateStructure);

            // Step3: Transfer image blocks
            transferImageBlocks();
            getAs220().getLogger().log(Level.INFO, "All blocks are sent at : " + new Date(System.currentTimeMillis()));

            // Step4: Check completeness of the image and transfer missing blocks
            //TODO - Checking for missings is not necessary at the moment because we have a guaranteed connection,
            // Every block is confirmed by the meter
            checkAndSendMissingBlocks();

            // Step5: Verify image
            imageTransfer.verifyAndRetryImage();
            getAs220().getLogger().log(Level.INFO, "Verification of the image was succesfull at : " + new Date(System.currentTimeMillis()));

            // Step6: Check image before activation
            // Skip this step

            // Step7: Activate image
            // This step is done in the ProtocolCode!


        } else {
            throw new IOException("Could not perform the upgrade because meter does not allow it.");
        }
    }

    protected void transferImageBlocks() throws IOException {

        byte[] octetStringData = null;
        OctetString os = null;
        Structure imageBlockTransfer;
        for (int i = 0; i < blockCount; i++) {
            if (i < blockCount - 1) {
                octetStringData = new byte[(int) this.imageTransfer.readImageBlockSize().getValue()];
                System.arraycopy(this.data, (int) (i * this.imageTransfer.readImageBlockSize().getValue()), octetStringData, 0,
                        (int) this.imageTransfer.readImageBlockSize().getValue());
            } else {
                long blockSize = this.size.getValue() - (i * this.imageTransfer.readImageBlockSize().getValue());
                octetStringData = new byte[(int) blockSize];
                System.arraycopy(this.data, (int) (i * this.imageTransfer.readImageBlockSize().getValue()), octetStringData, 0,
                        (int) blockSize);

            }
            os = OctetString.fromByteArray(octetStringData);
            imageBlockTransfer = new Structure();
            imageBlockTransfer.addDataType(new Unsigned32(i));
            imageBlockTransfer.addDataType(os);

            try {
                this.imageTransfer.imageBlockTransfer(imageBlockTransfer);
            } catch (DataAccessResultException e) {
                if (e.getDataAccessResult() == 2) { //"Temporary failure"
                    getAs220().getLogger().log(Level.INFO, "Received a temporary failure during verification, will send next block.");
                } else {
                    throw e;
                }
            }

            if (i % 50 == 0) { // i is multiple of 50
                getAs220().getLogger().log(Level.INFO, "ImageTransfer: " + i + " of " + blockCount + " blocks are sent to the device");
            }
        }
    }

    /**
     * Check if there are missing blocks, if so, resent them
     *
     * @throws IOException
     */
    public void checkAndSendMissingBlocks() throws IOException {
        Structure imageBlockTransfer;
        byte[] octetStringData = null;
        OctetString os = null;
        long previousMissingBlock = -1;
        int retryBlock = 0;
        int totalRetry = 0;
        while (this.imageTransfer.readFirstNotTransferedBlockNumber().getValue() < this.blockCount) {

            if (previousMissingBlock == this.imageTransfer.getImageFirstNotTransferedBlockNumber().getValue()) {
                if (retryBlock++ == this.maxBlockRetryCount) {
                    throw new IOException("Exceeding the maximum retry for block " + this.imageTransfer.getImageFirstNotTransferedBlockNumber().getValue() + ", Image transfer is canceled.");
                } else if (totalRetry++ == this.maxTotalRetryCount) {
                    throw new IOException("Exceeding the total maximum retry count, Image transfer is canceled.");
                }
            } else {
                previousMissingBlock = this.imageTransfer.getImageFirstNotTransferedBlockNumber().getValue();
                retryBlock = 0;
            }

            if (this.imageTransfer.getImageFirstNotTransferedBlockNumber().getValue() < this.blockCount - 1) {
                octetStringData = new byte[(int) this.imageTransfer.readImageBlockSize().getValue()];
                System.arraycopy(this.data, (int) (this.imageTransfer.getImageFirstNotTransferedBlockNumber().getValue() * this.imageTransfer.readImageBlockSize().getValue()), octetStringData, 0,
                        (int) this.imageTransfer.readImageBlockSize().getValue());
            } else {
                long blockSize = this.size.getValue() - (this.imageTransfer.getImageFirstNotTransferedBlockNumber().getValue() * this.imageTransfer.readImageBlockSize().getValue());
                octetStringData = new byte[(int) blockSize];
                System.arraycopy(this.data, (int) (this.imageTransfer.getImageFirstNotTransferedBlockNumber().getValue() * this.imageTransfer.readImageBlockSize().getValue()), octetStringData, 0,
                        (int) blockSize);
            }

            os = OctetString.fromByteArray(octetStringData);
            imageBlockTransfer = new Structure();
            imageBlockTransfer.addDataType(new Unsigned32((int) this.imageTransfer.getImageFirstNotTransferedBlockNumber().getValue()));
            imageBlockTransfer.addDataType(os);
            try {
                this.imageTransfer.imageBlockTransfer(imageBlockTransfer);
            } catch (DataAccessResultException e) {
                if (e.getDataAccessResult() == 2) { //"Temporary failure"
                    getAs220().getLogger().log(Level.INFO, "Received a temporary failure during verification, will send next block.");
                } else {
                    throw e;
                }
            }

        }
    }

    private AS220 getAs220() {
        return this.messaging.getAs220();
    }


    /**
     * Active the passive image
     *
     * @throws IOException
     */
    public void activate() throws IOException {
        getAs220().getLogger().log(Level.INFO, "Activating image ...");
        this.imageTransfer.imageActivation();
        getAs220().getLogger().log(Level.INFO, "Activation of the image was succesfull at : " + new Date());
    }

}

