package com.energyict.dlms.cosem;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.BitString;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.cosem.attributeobjects.ImageTransferStatus;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

/**
 * @author gna
 *         <pre>
 *         The Image transfer takes place in several steps:
 *         Step 1:   The client gets the ImageBlockSize from each server individually;
 *         Step 2:   The client initiates the Image transfer process individually or using broadcast;
 *         Step 3:   The client transfers ImageBlocks to (a group of) server(s) individually or using  broadcast;
 *         Step 4:   The client checks the completeness of the Image in each server individually and transfers any ImageBlocks not (yet) transferred;
 *         Step 5:   The Image is verified;
 *         Step 6:   Before activation, the Image is checked;
 *         Step 7:   The Image(s) is(/are) activated.
 *         </pre>
 */

public class ImageTransfer extends AbstractCosemObject {

    public static final int REPORT_STATUS_EVERY_X_BLOCKS = 1;
    public static final String DEFAULT_IMAGE_NAME = "NewImage";
    public static final int ATTRIBUTE_PREVIOUS_UPGRADE_STATE = -1;
    public static final int ATTRIBUTE_PROTOCOL_CONFIGURATION = -2;
    public static final int ATTRIBUTE_UPGRADE_PROGRESS = -3;
    public static final byte[] LN = new byte[]{0, 0, 44, 0, 0, (byte) 255};
    private static final String TIMEOUT_MESSAGE = "timeout";
    /* Attribute numbers */
    private static final int ATTRB_IMAGE_BLOCK_SIZE = 2;
    private static final int ATTRB_IMAGE_TRANSFER_BLOCK_STATUS = 3;
    private static final int ATTRB_IMAGE_FIRST_NOT_TRANSFERED_BLOCK = 4;
    private static final int ATTRB_IMAGE_TRANSFER_ENABLED = 5;
    private static final int ATTRB_IMAGE_TRANSFER_STATUS = 6;
    private static final int ATTRB_IMAGE_TO_ACTIVATE_INFO = 7;
    /* Method invoke */
    private static final int IMAGE_TRANSFER_INITIATE = 1;
    private static final int IMAGE_BLOCK_TRANSFER = 2;
    private static final int IMAGE_VERIFICATION = 3;
    public static final int IMAGE_ACTIVATION = 4;
    /* Method writes SN */
    private static final int IMAGE_TRANSFER_INITIATE_SN = 0x40;
    private static final int IMAGE_BLOCK_TRANSFER_SN = 0x48;
    private static final int IMAGE_VERIFICATION_SN = 0x50;
    private static final int IMAGE_ACTIVATION_SN = 0x58;
    private int pollingDelay = 10000;
    private int pollingRetries = 20;      //Poll status for 5 minutes
    private int maxBlockRetryCount = 3;
    private int maxTotalRetryCount = 500;
    private boolean usePollingVerifyAndActivate = false;
    private ProtocolLink protocolLink;
    private ImageTransferCallBack callBack;
    /* Attributes */
    private Unsigned32 imageMaxBlockSize = null; // holds the max size of the imageblocks to be sent to the server(meter)
    private BitString imageTransferBlocksStatus = null; // Provides information about the transfer status of each imageBlock (1=Transfered, 0=NotTransfered)
    private Unsigned32 imageFirstNotTransferedBlockNumber = null; // Provides the blocknumber of the first not transfered imageblock
    private BooleanObject imageTransferEnabled = null; // Controls enabling the image_transfer_proces
    private ImageTransferStatus imageTransferStatus = null; // Holds the status of the image transfer process
    private Array imageToActivateInfo = null;    // Provides information on the image(s) ready for activation
    /* Image info */
    private Unsigned32 size = null;     // the size of the image
    private ImageBlockSupplier dataSupplier = null; // the complete image in byte
    private int blockCount = -1; // the amount of block numbers
    private OctetString imageIdentification = null;
    private OctetString imageSignature = null;
    private int startIndex = 0;
    private long delayBeforeSendingBlocks = 0;
    private int booleanValue = 0xFF;        //Default byte value representing boolean TRUE is 0xFF
    private boolean checkNumberOfBlocksInPreviousSession = true;
    private boolean enableImageTransfer = true;
    private boolean initiateImageTransfer = true;
    private boolean transferBlocks = true;
    private boolean verifyImage = true;
    private boolean activateImage = false;
    private Charset charSet;
    private boolean startIndexOverride = false;

    public ImageTransfer(ProtocolLink protocolLink) {
        super(protocolLink, new ObjectReference(LN));
        this.protocolLink = protocolLink;
    }
    public ImageTransfer(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
        this.protocolLink = protocolLink;
    }

    /**
     * The default ObisCode
     */
    public static ObisCode getDefaultObisCode() {
        return ObisCode.fromByteArray(LN);
    }

    /**
     * The number of the first block that should be transferred.
     * If you want to transfer the whole image, this number should be 0. (default)
     * If you want to resume a previous session (that timed out), this value can be used by calling getLastTransferredBlockNumber()
     */
    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    /**
     * Some meters don't follow the AXDR spec where boolean TRUE is represented by all byte values different from 0x00.
     */
    public void setBooleanValue(int booleanValue) {
        this.booleanValue = booleanValue;
    }

    public void setDelayBeforeSendingBlocks(long delayBeforeSendingBlocks) {
        this.delayBeforeSendingBlocks = delayBeforeSendingBlocks;
    }

    public void setCharSet(Charset charSet) {
        this.charSet = charSet;
    }

    /**
     * If disabled (set to false) during resume process, the check if number of blocks in previous session equals
     * number of blocks in current session will be skipped. <br/>
     * <b>Warning:</b> Only disable in some specific cases, by default this check should be enabled!
     *
     * @param checkNumberOfBlocksInPreviousSession
     */
    public void setCheckNumberOfBlocksInPreviousSession(boolean checkNumberOfBlocksInPreviousSession) {
        this.checkNumberOfBlocksInPreviousSession = checkNumberOfBlocksInPreviousSession;
    }

    public boolean checkNumberOfBlocksInPreviousSession() {
        return checkNumberOfBlocksInPreviousSession;
    }

    public void setPollingDelay(int pollingDelay) {
        this.pollingDelay = pollingDelay;       //Set the delay between the status polls during image verification / activation
    }

    public void setPollingRetries(int pollingRetries) {
        this.pollingRetries = pollingRetries;   //Set the number of status polls during image verification / activation
    }

    /**
     * @return the classId of the ImageTransfer object, should always be 18
     */
    protected int getClassId() {
        return DLMSClassId.IMAGE_TRANSFER.getClassId();
    }

    /**
     * Start the automatic upgrade procedure. If the last block is not a multiple of the blockSize, then additional zeros will be padded at the end.
     * If you don't want this behavior then use {{@link #upgrade(byte[], boolean)} instead.
     *
     * @param data - the image to transfer
     * @throws java.io.IOException if something went wrong during the upgrade.
     */
    public void upgrade(byte[] data) throws IOException {
        this.upgrade(data, true);
    }

    /**
     * Start the automatic upgrade procedure. You may choose to add additional zeros at in the last block to match the blockSize for each block.
     *
     * @param data            - the image to transfer
     * @param additionalZeros - indicate whether you need to add zeros to the last block to match the blockSize
     * @throws java.io.IOException when something went wrong during the upgrade
     */
    public void upgrade(byte[] data, boolean additionalZeros) throws IOException {
        upgrade(data, additionalZeros, DEFAULT_IMAGE_NAME, false);
    }

    /**
     * Check the number of the block that was last transferred (in the previous session that timed out)
     * Call this method if you want to RESUME a block transfer in this session
     * <p/>
     * Returns -1 if all blocks were already sent in the previous session
     */
    public int getLastTransferredBlockNumber() throws IOException {
        BitString bitString = getImageTransferBlocksStatus();

        for (int index = 0; index < bitString.getNrOfBits(); index++) {
            if (!bitString.asBitSet().get(index)) {
                return index;
            }
        }
        return -1;
    }

    public void enableImageTransfer() throws IOException {
        if (!isResume()) {
            writeImageTransferEnabledState(true);
        }
    }

    public void enableImageTransfer(ImageBlockSupplier dataSupplier, String imageIdentifier) throws IOException {
        // Set the imageTransferEnabledState to true (otherwise the upgrade can not be performed)
        updateState(ImageTransferCallBack.ImageTransferState.ENABLE_IMAGE_TRANSFER, imageIdentifier, 0, dataSupplier.getSize(), 0);
        if (!isResume()) {
            writeImageTransferEnabledState(true);
        }
    }

    public void initiateImageTransfer(String imageIdentifier) throws IOException {
        int imageSize = (size != null) ? size.intValue() : 0;
        updateState(ImageTransferCallBack.ImageTransferState.INITIATE, imageIdentifier, blockCount, imageSize, 0);
        Structure imageInitiateStructure = new Structure();
        imageInitiateStructure.addDataType(OctetString.fromString(imageIdentifier, charSet));
        imageInitiateStructure.addDataType(this.size);
        if (!isResume()) {
            getLogger().info("Image transfer initialization parameters:" +
                    "\n - image identifier: " + imageIdentifier +
                    "\n - block count: " + blockCount +
                    "\n - image size: " + imageSize);
            imageTransferInitiate(imageInitiateStructure);
        } else {
            getLogger().info("Skipping image transfer initialization because we're resuming ... ");

        }
    }

    private void initiateImageTransfer(byte[] imageIdentifier) throws IOException {
        int imageSize = (size != null) ? size.intValue() : 0;
        Structure imageInitiateStructure = new Structure();
        imageInitiateStructure.addDataType(OctetString.fromByteArray(imageIdentifier));
        imageInitiateStructure.addDataType(this.size);
        if (!isResume()) {
            getLogger().info("Image transfer initialization parameters:" +
                    "\n - image identifier: " + ProtocolTools.getHexStringFromBytes(imageIdentifier) +
                    "\n - block count: " + blockCount +
                    "\n - image size: " + imageSize);
            imageTransferInitiate(imageInitiateStructure);
        } else {
            getLogger().info("Skipping image transfer initialization because we're resuming ...");

        }
    }

    /**
     * Start the automatic upgrade procedure. You may choose to add additional zeros at in the last block to match the blockSize for each block.
     *
     * @param dataSupplier          - supplier of the image to transfer
     * @param additionalZeros       - indicate whether you need to add zeros to the last block to match the blockSize
     * @param imageIdentifier       - the name of the file. Default is NewImage
     * @param checkForMissingBlocks - whether or not to resend lost blocks
     * @throws java.io.IOException when something went wrong during the upgrade
     */
    public final void upgrade(final ImageBlockSupplier dataSupplier, final boolean additionalZeros, final String imageIdentifier, final boolean checkForMissingBlocks) throws IOException {
        configurableUpgrade(dataSupplier, additionalZeros, imageIdentifier, checkForMissingBlocks);
    }

    /**
     * Start the automatic upgrade procedure. You may choose to add additional zeros at in the last block to match the blockSize for each block.
     *
     * @param dataSupplier          - supplier of the image to transfer
     * @param additionalZeros       - indicate whether you need to add zeros to the last block to match the blockSize
     * @param imageIdentifier       - the name of the file. Default is NewImage
     * @param checkForMissingBlocks - whether or not to resend lost blocks
     * @throws java.io.IOException when something went wrong during the upgrade
     */
    public final void configurableUpgrade(final ImageBlockSupplier dataSupplier, final boolean additionalZeros, final String imageIdentifier, final boolean checkForMissingBlocks) throws IOException {

        if (isEnableImageTransfer()) {
            enableImageTransfer(dataSupplier, imageIdentifier);
            getLogger().finest("Image transfer enabled for imageIdentifier: " + imageIdentifier);
        }
        getLogger().finest("Add additional zeros to match the last blocksize (additionalZeros): " + additionalZeros);
        getLogger().finest("checkForMissingBlocks: " + checkForMissingBlocks);
        if (getImageTransferEnabledState().getState()) {
            initializeAndTransferBlocks(dataSupplier, additionalZeros, imageIdentifier);

            // Step4: Check completeness of the image and transfer missing blocks
            // Every block is confirmed by the meter
            if (checkForMissingBlocks) {
                updateState(ImageTransferCallBack.ImageTransferState.CHECK_MISSING_BLOCKS, imageIdentifier, blockCount, dataSupplier.getSize(), 0);
                checkAndSendMissingBlocks();
            }

            // Step5: Verify image
            if (isVerifyImage()) {
                updateState(ImageTransferCallBack.ImageTransferState.VERIFY_IMAGE, imageIdentifier, blockCount, dataSupplier.getSize(), 0);
                if (isUsePollingVerifyAndActivate()) {
                    getLogger().log(Level.INFO, "Verification of image using polling method ...");
                    verifyAndPollForSuccess();
                } else {
                    verifyAndRetryImage();
                }
                getLogger().log(Level.INFO, "Verification of the image was successful at : " + new Date(System.currentTimeMillis()));
            }

            // Step6: Check image before activation
            // Skip this step

            // Step7: Activate image
            // This step is done in the ProtocolCode!
            if (isActivateImage()) {
                try {
                    setUsePollingVerifyAndActivate(false);   //Don't use polling for the activation (the meter will immediately reboot)!
                    imageActivation();
                } catch (DataAccessResultException e) {
                    if (isTemporaryFailure(e)) {
                        getLogger().log(Level.INFO, "Received temporary failure. Meter will activate the image when this communication session is closed, moving on.");
                    } else {
                        throw e;
                    }
                }
            }

        } else {
            throw new ProtocolException("Could not perform the upgrade because meter does not allow it.");
        }

    }

    /**
     * Start the automatic upgrade procedure. You may choose to add additional zeros at in the last block to match the blockSize for each block.
     *
     * @param data                  - the image to transfer
     * @param additionalZeros       - indicate whether you need to add zeros to the last block to match the blockSize
     * @param imageIdentifier       - the name of the file. Default is NewImage
     * @param checkForMissingBlocks - whether or not to resend lost blocks
     * @throws java.io.IOException when something went wrong during the upgrade
     */
    public void upgrade(byte[] data, boolean additionalZeros, String imageIdentifier, boolean checkForMissingBlocks) throws IOException {
        this.upgrade(new ByteArrayImageBlockSupplier(data), additionalZeros, imageIdentifier, checkForMissingBlocks);
    }

    /**
     * Initialize the {@link ImageTransfer} objects and transfer the blocks to the target device.
     *
     * @param 	dataSupplier			Provides the image data.
     * @param 	additionalZeros			Indicates whether or not to pad the last block with zeroes up to block size.
     * @param 	imageIdentifier			The image identifier.
     *
     * @throws 	IOException				If an IO error occurs whilst transferring blocks.
     */
    @Deprecated
    public final void initializeAndTransferBlocks(final ImageBlockSupplier dataSupplier, final boolean additionalZeros, final String imageIdentifier) throws IOException {
        this.dataSupplier = dataSupplier;
        this.size = new Unsigned32(dataSupplier.getSize());
        getLogger().info("Firmware Image size: " + this.size);
        getLogger().info("Reading first not transferred block (so we can check resume)");
        int lastTransferredBlockNumber = readFirstNotTransferedBlockNumber().intValue();
        if (lastTransferredBlockNumber > 0) {
            getLogger().log(Level.INFO, "Resuming session, starting from block " + (lastTransferredBlockNumber-1));
            setStartIndex(lastTransferredBlockNumber - 1);
        } else {
            getLogger().info("Resume not applicable, starting from zero.");
        }

        // Override start index if necessary
        if (startIndexOverride) {
            getLogger().log(Level.INFO, "Overriding start block to 0.");
            startIndex = 0;
        }

        // Step1: Get the maximum image block size
        // and calculate the amount of blocks in one step
        final long blockSize = readImageBlockSize().getValue();
        getLogger().log(Level.INFO, "ImageTransfer block size = [" + blockSize + "] bytes");

        this.blockCount = (int) (this.size.getValue() / blockSize) + (((this.size.getValue() % blockSize) == 0) ? 0 : 1);
        getLogger().info("ImageTransfer block count = [" + blockCount + "] blocks");

        if (isResume()) {
            getLogger().info("Resuming image transfer from "+startIndex);
            readImageTransferStatus();
            if (imageTransferStatus.getValue() < 1 || imageTransferStatus.getValue() > 3) {
                getLogger().warning("Cannot resume the image transfer. The current transfer state (" + imageTransferStatus.getValue() + ") should be 'Image transfer initiated (1)', 'Image verification initiated (2)' or 'Image verification successful (3)'. Will start from block 0.");
                startIndex = 0;
            }
            if (checkNumberOfBlocksInPreviousSession() && getNumberOfBlocksInPreviousSession() != blockCount) {
                getLogger().warning("Cannot resume the image transfer. The number of blocks is different since the last image transfer session. Will start from block 0.");
                startIndex = 0;
            }
        }

        // Step2: Initiate the image transfer
        if (isInitiateImageTransfer()) {
            getLogger().info("Initiating image transfer");
            initiateImageTransfer(imageIdentifier);
            getLogger().info("ImageTransfer initiated");
        }

        // add delay
        initializationBeforeSendingOfBlocks();

        // Step3: Transfer image blocks
        if (isTransferBlocks()) {
            getLogger().info("Transferring blocks ...");
            transferImageBlocks(additionalZeros);
            getLogger().info("All blocks are sent at: " + new Date(System.currentTimeMillis()));
        }
    }

    /**
     * Initialize the {@link ImageTransfer} objects and transfer the blocks to the target device.
     *
     * I duplicated this method because the imageIdentifier SHOULD NOT BE A STRING it should be a byte[] !!!
     * The image identifier is transmitted as an OctetString to the device.
     * The encoding of String's in Java is highly dependent on the ENCODING of the String!
     * You are sending HEX bytes to the device. You MUST NEVER USER Strings to encode bytes!
     *
     * @param dataSupplier      Provides the image data.
     * @param additionalZeros   Indicates whether or not to pad the last block with zeroes up to block size.
     * @param imageIdentifier   The image identifier.
     * @throws IOException      If an IO error occurs whilst transferring blocks.
     */
    public final void initializeAndTransferBlocks(final ImageBlockSupplier dataSupplier, final boolean additionalZeros, final byte[] imageIdentifier) throws IOException {
        this.dataSupplier = dataSupplier;
        this.size = new Unsigned32(dataSupplier.getSize());
        getLogger().info("Firmware Image size: " + this.size);
        getLogger().info("Reading first not transferred block (so we can check resume)");
        int lastTransferredBlockNumber = readFirstNotTransferedBlockNumber().intValue();
        if (lastTransferredBlockNumber > 0) {
            getLogger().info("Resuming session, starting from block " + (lastTransferredBlockNumber-1));
            setStartIndex(lastTransferredBlockNumber - 1);
        } else {
            getLogger().info("Resume not applicable, starting from zero.");
        }

        // Override start index if necessary
        if (startIndexOverride) {
            getLogger().info("Overriding start block to 0.");
            startIndex = 0;
        }

        // Step 1: Get the maximum image block size and calculate the amount of blocks in one step
        final long blockSize = readImageBlockSize().getValue();
        getLogger().info("ImageTransfer block size = [" + blockSize + "] bytes");

        this.blockCount = (int) (this.size.getValue() / blockSize) + (((this.size.getValue() % blockSize) == 0) ? 0 : 1);
        getLogger().info("ImageTransfer block count = [" + blockCount + "] blocks");

        if (isResume()) {
            getLogger().info("Resuming image transfer from "+startIndex);
            readImageTransferStatus();
            if (imageTransferStatus.getValue() < 1 || imageTransferStatus.getValue() > 3) {
                getLogger().warning("Cannot resume the image transfer. The current transfer state (" + imageTransferStatus.getValue() + ") should be 'Image transfer initiated (1)', 'Image verification initiated (2)' or 'Image verification successful (3)'. Will start from block 0.");
                startIndex = 0;
            }
            if (checkNumberOfBlocksInPreviousSession() && getNumberOfBlocksInPreviousSession() != blockCount) {
                getLogger().warning("Cannot resume the image transfer. The number of blocks is different since the last image transfer session. Will start from block 0.");
                startIndex = 0;
            }
        }

        // Step 2: Initiate the image transfer
        if (isInitiateImageTransfer()) {
            getLogger().info("Initiating image transfer");
            initiateImageTransfer(imageIdentifier);
            getLogger().info("ImageTransfer initiated");
        }

        // Add delay
        initializationBeforeSendingOfBlocks();

        // Step 3: Transfer image blocks
        if (isTransferBlocks()) {
            getLogger().info("Transferring blocks ...");
            transferImageBlocks(additionalZeros);
            getLogger().info("All blocks are sent at: " + new Date(System.currentTimeMillis()));
        }
    }

    /**
     * Initialize the {@link ImageTransfer} objects and transfer the blocks to the target device.
     *
     * @param 	data					The image data already loaded in memory.
     * @param 	additionalZeros			Indicates whether or not to pad the last block with zeroes up to block size.
     * @param 	imageIdentifier			The image identifier.
     *
     * @throws 	IOException				If an IO error occurs whilst transferring blocks.
     */
    public final void initializeAndTransferBlocks(final byte[] data, final boolean additionalZeros, final String imageIdentifier) throws IOException {
        this.initializeAndTransferBlocks(new ByteArrayImageBlockSupplier(data), additionalZeros, imageIdentifier);
    }

    /**
     * Method containing extra initialization steps, which should be executed before the start of sending of image blocks.
     */
    protected void initializationBeforeSendingOfBlocks() throws IOException {
        if (delayBeforeSendingBlocks > 0) {
            DLMSUtils.delay(delayBeforeSendingBlocks);  //Wait a bit before sending the blocks
        }
    }

    /**
     * Indicates if this is a 'resume' session (sending the remaining blocks) or a normal session (send all blocks)
     * If it's a resume session, there's no need to set the enabled state and to do the initiate.
     */
    public boolean isResume() {
        return startIndex > 0;
    }

    /**
     * Do the first 2 steps of a firmware upgrade.
     * Use this to allow more progress feedback to the user
     */
    public void initialize(byte[] data) throws IOException {
    	this.dataSupplier = new ByteArrayImageBlockSupplier(data);
        this.size = new Unsigned32(data.length);

        // Set the imageTransferEnabledState to true (otherwise the upgrade can not be performed)
        writeImageTransferEnabledState(true);

        if (getImageTransferEnabledState().getState()) {

            // Step1: Get the maximum image block size
            // and calculate the amount of blocks in one step
            this.blockCount = (int) (this.size.getValue() / readImageBlockSize().getValue()) + (((this.size.getValue() % readImageBlockSize().getValue()) == 0) ? 0 : 1);

            // Step2: Initiate the image transfer
            Structure imageInitiateStructure = new Structure();
            imageInitiateStructure.addDataType(OctetString.fromString("NewImage"));    // it's a default name for the new image
            imageInitiateStructure.addDataType(this.size);

            imageTransferInitiate(imageInitiateStructure);
        } else {
            throw new ProtocolException("Could not perform the upgrade because meter does not allow it.");
        }
    }

    /**
     *  LTE FW Upgrade initialization
     */
    public void initializeFOTA( boolean useEnexisInitFOTAInsteadStandard) throws IOException {
        // Set the imageTransferEnabledState to true (otherwise the upgrade can not be performed)
        writeImageTransferEnabledState(true);

        if (getImageTransferEnabledState().getState()) {
            // Instead octet-string + unsigned int Enexis use Int8 transfer to initiate update
            if( useEnexisInitFOTAInsteadStandard ) {
                imageTransferInitiate(new Integer8(0));
            }
            else {
                Structure imageInitiateStructure = new Structure();
                imageInitiateStructure.addDataType(OctetString.fromByteArray(new byte[]{0x1, 0xF, 0x0}));
                imageInitiateStructure.addDataType(new Unsigned32(1));
                imageTransferInitiate(imageInitiateStructure);
            }
        }
        else {
            throw new IOException("Could not perform the upgrade because meter does not allow it.");
        }
    }

    /**
     * Transfer all the image blocks to the meter.
     *
     * @param additionalZeros - add additional zeros to match the last blocksize to a multiple of the fileSize
     * @throws java.io.IOException if something went wrong during the upgrade
     */
    public void transferImageBlocks(boolean additionalZeros) throws IOException {

        final long startTime = System.currentTimeMillis();

        byte[] octetStringData = null;
        OctetString os = null;
        Structure imageBlockTransfer;
        if (isResume()) {
            getLogger().info("Resuming block transfer. Starting with block " + startIndex);
            if (startIndex > blockCount) {
                getLogger().info("All blocks were already sent in previous session, moving on to verification");
            }
        }

        if (this.dataSupplier == null){
            getLogger().severe("The image is empty, cannot transfer!");
        }

        getLogger().info("Confirming again the block size:");
        int blockSize = (int)this.readImageBlockSize().getValue();
        getLogger().info("Block size: " + blockSize);

        for (int i = startIndex; i < blockCount; i++) {
            getLogger().info("BlockIndex " + i);
            octetStringData = this.dataSupplier.getBlock(i, blockSize, additionalZeros);
            os = OctetString.fromByteArray(octetStringData);
            imageBlockTransfer = new Structure();
            imageBlockTransfer.addDataType(new Unsigned32(i));
            imageBlockTransfer.addDataType(os);
            getLogger().finest("BlockIndex " + i);
            getLogger().finest("BlockData " + os);
            updateState(ImageTransferCallBack.ImageTransferState.TRANSFER_BLOCKS, "", blockCount, size.intValue(), i);
            imageBlockTransfer(imageBlockTransfer);

            if (i % REPORT_STATUS_EVERY_X_BLOCKS == 0) { // i is multiple of 50
                final long elapsedTime = System.currentTimeMillis() - startTime;
                final long timeLeft = ((elapsedTime) * (blockCount - (i + 1))) / (i + 1);
                final long minutesLeft = timeLeft / 60000;
                final long secondsLeft = (timeLeft / 1000) % 60;
                String seconds = ((secondsLeft <= 9) ? "0" : "") + secondsLeft;

                getLogger().log(Level.INFO, "ImageTransfer: " + (i + 1) + " of " + blockCount + " blocks are sent to the device. Estimated time left until finished: [" + minutesLeft + ":" + seconds + "]");
            }
        }
//		fos.close();
    }

    public void transferNextImageBlocks(int counter, boolean additionalZeros) throws IOException {

        int numberOfBlocksPerStep = blockCount / 20 + (((blockCount % 20) == 0) ? 0 : 1);
        int startOffset = counter * numberOfBlocksPerStep;

        if ((startOffset + numberOfBlocksPerStep) > blockCount) {
            numberOfBlocksPerStep = blockCount - startOffset;
        }

        byte[] octetStringData;
        OctetString os;
        Structure imageBlockTransfer;
        for (int i = startOffset; i < startOffset + numberOfBlocksPerStep; i++) {
            octetStringData = this.dataSupplier.getBlock(i, (int)this.readImageBlockSize().getValue(), additionalZeros);
            os = OctetString.fromByteArray(octetStringData);
            imageBlockTransfer = new Structure();
            imageBlockTransfer.addDataType(new Unsigned32(i));
            imageBlockTransfer.addDataType(os);

            // without retries
            imageBlockTransfer(imageBlockTransfer);

            if (i % 50 == 0) { // i is multiple of 50
                getLogger().log(Level.INFO, "ImageTransfer: " + i + " of " + blockCount + " blocks are sent to the device");
            }
        }
//		fos.close();
    }

    /**
     * Check if there are missing blocks, if so, resent them
     *
     * @throws java.io.IOException
     */
    public void checkAndSendMissingBlocks() throws IOException {
        Structure imageBlockTransfer;
        byte[] octetStringData = null;
        OctetString os = null;
        long previousMissingBlock = -1;
        int retryBlock = 0;
        int totalRetry = 0;
        getLogger().info("Checking for missing blocks...");
        getLogger().info("First not transferred block is: " + readFirstNotTransferedBlockNumber().getValue() + ", block count is " + blockCount);
        while (readFirstNotTransferedBlockNumber().getValue() < this.blockCount) {

            if (previousMissingBlock == this.getImageFirstNotTransferedBlockNumber().getValue()) {
                if (retryBlock++ == this.maxBlockRetryCount) {
                    throw new ProtocolException("Exceeding the maximum retry for block " + this.getImageFirstNotTransferedBlockNumber().getValue() + ", Image transfer is canceled.");
                } else if (totalRetry++ == this.maxTotalRetryCount) {
                    throw new ProtocolException("Exceeding the total maximum retry count, Image transfer is canceled.");
                }
            } else {
                previousMissingBlock = this.getImageFirstNotTransferedBlockNumber().getValue();
                retryBlock = 0;
            }

            octetStringData = this.dataSupplier.getBlock((int)this.getImageFirstNotTransferedBlockNumber().getValue(), (int)this.readImageBlockSize().getValue(), false);
            os = OctetString.fromByteArray(octetStringData);
            imageBlockTransfer = new Structure();
            imageBlockTransfer.addDataType(new Unsigned32((int) this.getImageFirstNotTransferedBlockNumber().getValue()));
            imageBlockTransfer.addDataType(os);
            getLogger().info("Resending block " + this.getImageFirstNotTransferedBlockNumber().getValue());
            getLogger().finest("Resending BlockData: "+octetStringData);
            imageBlockTransfer(imageBlockTransfer);

        }
    }

    /**
     * Verify the image. If the result is a temporary failure, then wait a few seconds and retry it.
     *
     * @throws java.io.IOException
     */
    public void verifyAndRetryImage() throws IOException {
        int retry = 3;
        while (retry >= 0) {
            try {
                imageVerification();
                retry = -1;
            } catch (DataAccessResultException e) {
                if (isTemporaryFailure(e) && retry >= 1) {
                    getLogger().log(Level.INFO, "Received a temporary failure during verification, will retry.");
                    retry--;
                    DLMSUtils.delay(pollingDelay);
                } else {
                    throw e;
                }
            }
        }
    }

    protected boolean isTemporaryFailure(DataAccessResultException e) {
        return (e.getDataAccessResult() == DataAccessResultCode.TEMPORARY_FAILURE.getResultCode());
    }

    protected boolean isHardwareFailure(DataAccessResultException e) {
        return (e.getDataAccessResult() == DataAccessResultCode.HARDWARE_FAULT.getResultCode());
    }

    /**
     * Verify the image. If the result is a temporary failure, then inside this method a polling mechanism will be used
     * to check the verification status again.
     *
     * @throws java.io.IOException
     */
    public void verifyAndPollForSuccess() throws IOException {
        try {
            imageVerification();
        } catch (DataAccessResultException e) {
            if (isTemporaryFailure(e) || isHardwareFailure(e)) {
                getLogger().info("Received [" + e.getCode().getDescription() + "] while verifying image. Polling result ...");
                pollForImageVerificationStatus();
            } else {
                throw e;
            }
        }
    }

    /**
     * Wait until the image verification was successfully by polling the meter
     */
    protected final void pollForImageVerificationStatus() throws IOException {
        int tries = pollingRetries;
        while (--tries > 0) {
            try {
                Thread.sleep(pollingDelay);
                final ImageTransferStatus transferStatus = readImageTransferStatus();
                switch (transferStatus.getValue()) {
                    case 2:
                        getLogger().info("Image validation state: [Image verification initiated].");
                        break;
                    case 3:
                        getLogger().info("Image validation state: [Image verification successful].");
                        return;
                    case 4:
                        throw new ProtocolException("Image verification failed].");
                    default:
                        throw new ProtocolException("Invalid state [" + transferStatus.getValue() + "] while polling for verification status.");
                }
            } catch (DataAccessResultException e) {
                if (isTemporaryFailure(e) || isHardwareFailure(e)) {
                    getLogger().info("Received [" + e.getCode().getDescription() + "] while verifying image. [" + tries + "] polls left ....");
                } else {
                    throw e;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw ConnectionCommunicationException.communicationInterruptedException(e);
            }
        }
        throw new ProtocolException("Image verification failed, even after a few polls!");
    }

    /**
     * Wait until the image activation was successfully by polling the meter
     */
    private final void pollForImageActivationStatus() throws IOException {
        int tries = pollingRetries;
        while (--tries > 0) {
            try {
                Thread.sleep(pollingDelay);
                final ImageTransferStatus transferStatus = readImageTransferStatus();
                switch (transferStatus.getValue()) {
                    case 5:
                        getLogger().info("Image activation state: [Image activation initiated].");
                        break;
                    case 6:
                        getLogger().info("Image activation state: [Image activation successful].");
                        return;
                    case 7:
                        throw new ProtocolException("Image activation failed].");
                    default:
                        throw new ProtocolException("Invalid state [" + transferStatus.getValue() + "] while polling for activation status.");
                }
            } catch (DataAccessResultException e) {
                if (isTemporaryFailure(e) || isHardwareFailure(e)) {
                    getLogger().info("Received [" + e.getCode().getDescription() + "] while activating image. [" + tries + "] polls left ....");
                } else {
                    throw e;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw ConnectionCommunicationException.communicationInterruptedException(e);
            }
        }
        throw new ProtocolException("Image activation failed, even after a few polls!");
    }

    /**
     * Get the maximum block image size from the device
     *
     * @return The block size that should be used during the image transfer
     * @throws java.io.IOException
     */
    public Unsigned32 readImageBlockSize() throws IOException {
        if (this.imageMaxBlockSize == null) {
            final byte[] berEncodedData = getLNResponseData(ATTRB_IMAGE_BLOCK_SIZE);
            this.imageMaxBlockSize = AXDRDecoder.decode(berEncodedData, Unsigned32.class);
        }
        return this.imageMaxBlockSize;
    }

    /**
     * Write the block size of each block used to transfer the firmware image
     * (Not every meter supports this, most of the meters will return R/W denied)
     *
     * @param blockSize The new block size
     * @throws java.io.IOException If we could not write the new image block size
     */
    public void writeImageBlockSize(Unsigned32 blockSize) throws IOException {
        write(ATTRB_IMAGE_BLOCK_SIZE, blockSize.getBEREncodedByteArray());
    }

    /**
     * Set the protocol configuration:
     * Writes the upgrade details needed to perform the upgrade.
     * Note that this is a custom attribute, currently only implemented on the Beacon3100 firmware.
     * See https://confluence.eict.vpdc/display/G3IntBeacon3100/Meter+multicast+upgrade
     */
    public void writeMulticastProtocolConfiguration(Structure protocolConfiguration) throws IOException {
        write(ATTRIBUTE_PROTOCOL_CONFIGURATION, protocolConfiguration.getBEREncodedByteArray());
    }

    /**
     * Read out the detailed progress of the multicast upgrade.
     * This contains a list of information for all connected AM540 slave devices that are currently being upgraded.
     * See https://confluence.eict.vpdc/display/G3IntBeacon3100/Meter+multicast+upgrade
     */
    public Structure readMulticastUpgradeProgress() throws IOException {
        final byte[] berEncodedData = getLNResponseData(ATTRIBUTE_UPGRADE_PROGRESS);
        return AXDRDecoder.decode(berEncodedData, Structure.class);
    }

    /**
     * Provides information about the transfer status of each ImageBlock.
     * Each bit in the bit-string provides information about one individual
     * ImageBlock:
     * 0 = Not transferred,
     * 1 = Transferred
     *
     * @return
     * @throws java.io.IOException
     */
    public BitString readImageTransferedBlockStatus() throws IOException {
        final byte[] berEncodedData = getLNResponseData(ATTRB_IMAGE_TRANSFER_BLOCK_STATUS);
        this.imageTransferBlocksStatus = AXDRDecoder.decode(berEncodedData, BitString.class);
        return this.imageTransferBlocksStatus;
    }

    public BitString getImageTransferBlocksStatus() throws IOException {
        if (imageTransferBlocksStatus == null) {
            readImageTransferedBlockStatus();
        }
        return imageTransferBlocksStatus;
    }

    /**
     * Provides the ImageBlockNumber of the first ImageBlock not transferred.
     * NOTE:  If the Image is complete, the value returned should be above the number
     * of blocks calculated from the Image size and the ImageBlockSize.
     *
     * @return
     * @throws java.io.IOException
     */
    public Unsigned32 readFirstNotTransferedBlockNumber() throws IOException {
        final byte[] berEncodedData = getLNResponseData(ATTRB_IMAGE_FIRST_NOT_TRANSFERED_BLOCK);
        this.setImageFirstNotTransferedBlockNumber(AXDRDecoder.decode(berEncodedData, Unsigned32.class));
        return this.getImageFirstNotTransferedBlockNumber();
    }

    /**
     * Controls enabling the Image transfer process. The method can be
     * invoked successfully only if the value of this attribute is TRUE.
     * Boolean: FALSE = Disabled,
     * TRUE = Enabled
     *
     * @return
     * @throws java.io.IOException
     */
    public BooleanObject readImageTransferEnabledState() throws IOException {
        final byte[] berEncodedData = getLNResponseData(ATTRB_IMAGE_TRANSFER_ENABLED);
        this.imageTransferEnabled = AXDRDecoder.decode(berEncodedData, BooleanObject.class);
        return this.imageTransferEnabled;
    }

    /**
     * Write the given state to the imageTransfer enabled attribute
     *
     * @param state : true to indicate that in imageTransfer will be done, false otherwise
     * @throws java.io.IOException
     */
    public void writeImageTransferEnabledState(boolean state) throws IOException {
        final BooleanObject dlmsState = new BooleanObject(state);
        dlmsState.setTrueValue(booleanValue);
        final byte[] berEncodedByteArray = dlmsState.getBEREncodedByteArray();
        write(ATTRB_IMAGE_TRANSFER_ENABLED, berEncodedByteArray);
    }

    /**
     * Controls enabling the Image transfer process. The method can be
     * invoked successfully only if the value of this attribute is TRUE.
     * Boolean: FALSE = Disabled,
     * TRUE = Enabled
     *
     * @return
     * @throws java.io.IOException
     */
    public BooleanObject getImageTransferEnabledState() throws IOException {
        if (this.imageTransferEnabled == null) {
            return readImageTransferEnabledState();
        } else {
            return this.imageTransferEnabled;
        }
    }

    /**
     * <pre>
     * Holds the status of the Image transfer process.
     * enum:  (0)  Image transfer not initiated,
     * 		(1)  Image transfer initiated,
     * 		(2)  Image verification initiated,
     * 		(3)  Image verification successful,
     * 		(4)  Image verification failed,
     * 		(5)  Image activation initiated,
     * 		(6)  Image activation successful
     * 		(7)  Image activation failed
     * </pre>
     *
     * @return
     * @throws java.io.IOException
     */
    public ImageTransferStatus readImageTransferStatus() throws IOException {
        getLogger().info("Checking image transfer status");
        final byte[] berEncodedData = getLNResponseData(ATTRB_IMAGE_TRANSFER_STATUS);
        final TypeEnum typeEnum = AXDRDecoder.decode(berEncodedData, TypeEnum.class);
        this.imageTransferStatus = ImageTransferStatus.fromValue(typeEnum.getValue());
        getLogger().info("imageTransferStatus is " + this.imageTransferStatus.toString());
        return this.imageTransferStatus;
    }

    public TypeEnum getImageTransferStatus() throws IOException {
        final byte[] berEncodedData = getLNResponseData(ATTRB_IMAGE_TRANSFER_STATUS);
        final TypeEnum typeEnum = AXDRDecoder.decode(berEncodedData, TypeEnum.class);
        return typeEnum;
    }

    public int getNumberOfBlocksInPreviousSession() throws IOException {
        final byte[] berEncodedData = getLNResponseData(ATTRB_IMAGE_TRANSFER_BLOCK_STATUS);
        return AXDRDecoder.decode(berEncodedData, BitString.class).getNrOfBits();
    }

    /**
     * Provides information on the Image(s) ready for activation. It is
     * generated as the result of the Image verification. The client may
     * check this information before activating the Image(s).
     *
     * @return The list of image to activate infos.
     * @throws java.io.IOException
     */
    public final List<ImageToActivateInfo> readImageToActivateInfo() throws IOException {
        final byte[] berEncodedData = getLNResponseData(ATTRB_IMAGE_TO_ACTIVATE_INFO);

        final Array imagesToActivateArray = AXDRDecoder.decode(berEncodedData, Array.class);
        final List<ImageToActivateInfo> imagesToActivate = new ArrayList<ImageToActivateInfo>(imagesToActivateArray.nrOfDataTypes());

        for (int i = 0; i < imagesToActivateArray.nrOfDataTypes(); i++) {
            final Structure currentImage = imagesToActivateArray.getDataType(i, Structure.class);
            imagesToActivate.add(ImageToActivateInfo.fromStructure(currentImage));
        }

        return imagesToActivate;
    }

    public final List<Structure> readImageToActivateInfoStructure() throws IOException {
        final byte[] berEncodedData = getLNResponseData(ATTRB_IMAGE_TO_ACTIVATE_INFO);

        final Array imagesToActivateArray = AXDRDecoder.decode(berEncodedData, Array.class);
        final List<Structure> imagesToActivate = new ArrayList<>(imagesToActivateArray.nrOfDataTypes());

        for (int i = 0; i < imagesToActivateArray.nrOfDataTypes(); i++) {
            final Structure currentImage = imagesToActivateArray.getDataType(i, Structure.class);
            imagesToActivate.add(currentImage);
        }

        return imagesToActivate;
    }

    /**
     *
     * Initializes the Image transfer process.
     * @param imageInfo
     * @throws java.io.IOException
     */
    public void imageTransferInitiate(final AbstractDataType imageInfo) throws IOException {
        imageTransferInitiate(imageInfo.getBEREncodedByteArray());
    }

    /**
     * Initializes the Image transfer process.
     * @param berEncodedByteArray
     * @throws java.io.IOException
     */
    private void imageTransferInitiate(final byte[] berEncodedByteArray) throws IOException {
        if (getObjectReference().isLNReference()) {
            invoke(IMAGE_TRANSFER_INITIATE, berEncodedByteArray);
        } else { // SN referencing
            write(IMAGE_TRANSFER_INITIATE_SN, berEncodedByteArray);
        }
    }

    /**
     * <pre>
     * Transfers one block of the Image to the server.
     * The structure has the form of :
     * 		data ::= structure
     *         {
     * 		image_block_number:   double-long-unsigned,
     * 		image_block_value:   octet-string
     *         }
     * 		NOTE: the first ImageBlock sent is block 0.
     * </pre>
     *
     * @param imageData
     * @throws java.io.IOException
     */
    public void imageBlockTransfer(Structure imageData) throws IOException {
        final byte[] berEncodedByteArray = imageData.getBEREncodedByteArray();
        if (getObjectReference().isLNReference()) {
            invoke(IMAGE_BLOCK_TRANSFER, berEncodedByteArray);
        } else {
            write(IMAGE_BLOCK_TRANSFER_SN, berEncodedByteArray);
        }
    }

    /**
     * Verifies the integrity of the Image before activation.
     * <p/>
     * The result of the invocation of this method may be success,
     * temporary_failure or other_reason. If it is not success, then the
     * result of the verification can be learned by retrieving the value of
     * the image_transfer_status attribute.
     *
     * @throws java.io.IOException
     */
    public void imageVerification() throws IOException {
        final Integer8 value = new Integer8(0);
        final byte[] berEncodedByteArray = value.getBEREncodedByteArray();
        if (getObjectReference().isLNReference()) {
            invoke(IMAGE_VERIFICATION, berEncodedByteArray);
        } else {
            write(IMAGE_VERIFICATION_SN, berEncodedByteArray);
        }
    }

    /**
     * Activates the Image(s).
     * <p/>
     * If the Image transferred has not been verified before, then this is
     * done as part of the Image activation. The result of the invocation
     * of this method may be success, temporary-failure or other-reason.
     * If it is not success, then the result of the activation can be learned
     * by retrieving the value of the image_transfer_status attribute.
     *
     * @throws java.io.IOException
     */
    public void imageActivation() throws IOException {
        updateState(ImageTransferCallBack.ImageTransferState.ACTIVATE, "", blockCount, size != null ? size.intValue() : 0, 0);
        try {
            if (getObjectReference().isLNReference()) {
                invoke(IMAGE_ACTIVATION, new Integer8(0).getBEREncodedByteArray());
            } else {
                write(IMAGE_ACTIVATION_SN, new Integer8(0).getBEREncodedByteArray());
            }
        } catch (DataAccessResultException e) {
            if ((isTemporaryFailure(e) || isHardwareFailure(e)) && isUsePollingVerifyAndActivate()) { // Temporary failure
                getLogger().info("Received [" + e.getCode().getDescription() + "] while activating image. Polling result ...");
                try {
                    pollForImageActivationStatus();
                } catch (IOException e1) {
                    if (isTimeoutException(e)) {
                        //Means activation was successful and the meter is rebooting
                    } else {
                        throw e1;
                    }
                }
            } else {
                throw e;
            }
        }
    }

    private boolean isTimeoutException(DataAccessResultException e) {
        return e.getMessage().toLowerCase().contains(TIMEOUT_MESSAGE);
    }

    /**
     * @return the imageFirstNotTransferedBlockNumber
     */
    public Unsigned32 getImageFirstNotTransferedBlockNumber() {
        return imageFirstNotTransferedBlockNumber;
    }

    /**
     * @param imageFirstNotTransferedBlockNumber the imageFirstNotTransferedBlockNumber to set
     */
    public void setImageFirstNotTransferedBlockNumber(Unsigned32 imageFirstNotTransferedBlockNumber) {
        this.imageFirstNotTransferedBlockNumber = imageFirstNotTransferedBlockNumber;
    }

    /**
     * Use this method to register your callBack to receive status updates while the image transfer is in progress.
     *
     * @param callBack The callBack class that implements {@link com.energyict.dlms.cosem.ImageTransfer.ImageTransferCallBack}
     */
    public void setCallBack(ImageTransferCallBack callBack) {
        this.callBack = callBack;
    }

    /**
     * Post the new state of the image transfer if there is a callBack object available
     *
     * @param state        The new state
     * @param imageName    The firmware image name
     * @param blockCount   The number of blocks to send to the device (could be 0 if unknown)
     * @param dataSize     The number of bytes to send to the device (complete firmware size)
     * @param currentBlock The block number we're sending to the device (could be 0 if unknown, or if we're not sending blocks)
     * @throws java.io.IOException
     */
    private void updateState(ImageTransferCallBack.ImageTransferState state, String imageName, int blockCount, int dataSize, int currentBlock) throws IOException {
        if (callBack != null) {
            callBack.updateState(state, imageName, blockCount, dataSize, currentBlock);
        }
    }

    /**
     * Check if we're using the correct bluebook image verification method.
     * The verify method can sometimes result in a temporary failure message from the meter, meaning that the image verification
     * is still in progress. The blue book defines that we have to poll the image transfer status to see the result of the
     * verification instead of retrying the verification itself.
     * <p/>
     * The old implementation just retried the actual activation, and some meters fail in this case.
     *
     * @return True if we use verify the image as described in the dlms bluebook
     */
    public boolean isUsePollingVerifyAndActivate() {
        return this.usePollingVerifyAndActivate;
    }

    /**
     * @param usePollingVerifyAndActivate
     */
    public void setUsePollingVerifyAndActivate(boolean usePollingVerifyAndActivate) {
        this.usePollingVerifyAndActivate = usePollingVerifyAndActivate;
    }

    public boolean isEnableImageTransfer() {
        return enableImageTransfer;
    }

    public void setEnableImageTransfer(boolean enableImageTransfer) {
        this.enableImageTransfer = enableImageTransfer;
    }

    public boolean isInitiateImageTransfer() {
        return initiateImageTransfer;
    }

    public void setInitiateImageTransfer(boolean initiateImageTransfer) {
        this.initiateImageTransfer = initiateImageTransfer;
    }

    public boolean isTransferBlocks() {
        return transferBlocks;
    }

    public void setTransferBlocks(boolean transferBlocks) {
        this.transferBlocks = transferBlocks;
    }

    public boolean isVerifyImage() {
        return verifyImage;
    }

    public void setVerifyImage(boolean verifyImage) {
        this.verifyImage = verifyImage;
    }

    public boolean isActivateImage() {
        return activateImage;
    }

    public void setActivateImage(boolean activateImage) {
        this.activateImage = activateImage;
    }

    public void setStartIndexOverride(boolean startIndexOverride) {
        this.startIndexOverride = startIndexOverride;
    }

    /**
	 * Supplier for image blocks.
	 *
	 * @author alex
	 */
	public interface ImageBlockSupplier {

		/**
		 * Returns the given block. Returns <code>null</code> if there is no such block (past the end of the data).
		 *
		 * @param 	blockNumber		The block number.
		 * @param 	blockSize		The size of the blocks.
		 * @param	padToBlockSize	Indicates whether or not the data should be padded with zeroes up to block size.
		 *
		 * @return	The given block, <code>null</code> if we are past the end of the data.
		 *
		 * @throws	IOException		If an IO error occurs when fetching the given block.
		 */
		byte[] getBlock(final int blockNumber, final int blockSize, final boolean padToBlockSize) throws IOException;

		/**
		 * Returns the size of the data.
		 *
		 * @return	The size of the data.
		 */
		int getSize();
	}

    /**
     * Implement this class and use the {@link ImageTransfer#setCallBack(com.energyict.dlms.cosem.ImageTransfer.ImageTransferCallBack)} method
     * to register your callBack to receive status updates while the image transfer is in progress.
     */
    public interface ImageTransferCallBack {

        void updateState(ImageTransferState state, String imageName, int blockCount, int imageSize, int currentBlock) throws IOException;

        /**
         * The different states the image transfer uses
         */
        enum ImageTransferState {
            ENABLE_IMAGE_TRANSFER,
            INITIATE,
            TRANSFER_BLOCKS,
            CHECK_MISSING_BLOCKS,
            VERIFY_IMAGE,
            ACTIVATE
        }
    }

	/**
	 * Image block supplier that simply uses a byte array.
	 *
	 * @author alex
	 */
	public static final class ByteArrayImageBlockSupplier implements ImageBlockSupplier {

		/** The firmware data. */
		private final byte[] data;

		/**
		 * Create a new instance.
		 *
		 * @param 	data		The data.
		 */
		public ByteArrayImageBlockSupplier(final byte[] data) {
			this.data = data;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public final byte[] getBlock(final int blockNumber, final int blockSize, final boolean padToBlockSize) {
			if (blockSize <= 0) {
				throw new IllegalArgumentException("Block size should be at least [1], you specified [" + blockSize + "]");
			}

			final int numberOfBlocks = (this.data.length / blockSize) + 1;

			if (blockNumber < 0 || blockNumber >= numberOfBlocks) {
				throw new IllegalArgumentException("Invalid block number requested, valid blocks between [0] and [" + (numberOfBlocks - 1) + "] !");
			}

			final int startIndex = blockNumber * blockSize;
			int endIndex = (blockNumber + 1) * blockSize;

			if (endIndex > this.data.length) {
				endIndex = this.data.length;
			}

			final byte[] data = Arrays.copyOfRange(this.data, startIndex, endIndex);

			if (padToBlockSize && data.length < blockSize) {
				final byte[] paddedData = new byte[blockSize];
				System.arraycopy(data, 0, paddedData, 0, data.length);

				return paddedData;
			}

			return data;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public final int getSize() {
			return this.data.length;
		}
	}

	/**
	 * Image block supplier that uses a {@link RandomAccessFile}.
	 *
	 * @author alex
	 */
	public static final class RandomAccessFileImageBlockSupplier implements ImageBlockSupplier {

		/** The file. */
		private final RandomAccessFile file;

		/**
		 * Create a new instance.
		 *
		 * @param 	file		The file.
		 */
		public RandomAccessFileImageBlockSupplier(final RandomAccessFile file) {
			this.file = file;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public final byte[] getBlock(final int blockNumber, final int blockSize, final boolean padToBlockSize) throws IOException {
			if (blockSize <= 0) {
				throw new IllegalArgumentException("Block size should be at least [1], you specified [" + blockSize + "]");
			}

			final int numberOfBlocks = ((int)this.file.length() / blockSize) + 1;

			if (blockNumber < 0 || blockNumber >= numberOfBlocks) {
				throw new IllegalArgumentException("Invalid block number requested, valid blocks between [0] and [" + (numberOfBlocks - 1) + "] !");
			}

			final int startIndex = blockNumber * blockSize;
			int endIndex = (blockNumber + 1) * blockSize;

			if (endIndex > this.file.length()) {
				endIndex = (int)this.file.length();
			}

			final byte[] buffer = new byte[endIndex - startIndex];

			this.file.seek(startIndex);
			this.file.readFully(buffer);

			if (padToBlockSize && buffer.length < blockSize) {
				final byte[] paddedData = new byte[blockSize];
				System.arraycopy(buffer, 0, paddedData, 0, buffer.length);

				return paddedData;
			}

			return buffer;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public final int getSize() {
			try {
				return (int)this.file.length();
			} catch (IOException e) {
				throw new IllegalStateException("Cannot read size of file [" + this.file + "] : [" + e.getMessage() + "]", e);
			}
		}
	}

    /**
     * Models an ImageToActivateInfo structure.
     *
     * @author alex
     */
    public static final class ImageToActivateInfo {

        /**
         * The image identifier.
         */
        private final String imageIdentifier;

        /**
         * The signature of the image.
         */
        private final String imageSignature;

        /**
         * The size of the image.
         */
        private final int imageSize;

        /**
         * Create a new instance.
         *
         * @param imageIdentifier The image identifier.
         * @param imageSize       The image size.
         * @param imageSignature  The image signature.
         */
        private ImageToActivateInfo(final String imageIdentifier, final int imageSize, final String imageSignature) {
            this.imageIdentifier = imageIdentifier;
            this.imageSize = imageSize;
            this.imageSignature = imageSignature;
        }

        /**
         * Converts the given {@link com.energyict.dlms.axrdencoding.Structure} to an {@link ImageToActivateInfo} instance.
         *
         * @param structure The structure to convert.
         * @return The corresponding {@link ImageToActivateInfo}.
         */
        private static final ImageToActivateInfo fromStructure(final Structure structure) {
            if (structure.nrOfDataTypes() == 3) {
                final Unsigned32 imageSize = structure.getDataType(0).getUnsigned32();
                final OctetString imageIdentification = structure.getDataType(1).getOctetString();
                final OctetString imageSignature = structure.getDataType(1).getOctetString();

                return new ImageToActivateInfo(imageIdentification.stringValue(), imageSize.intValue(), imageSignature.stringValue());
            } else {
                throw new IllegalArgumentException("Could not parse Structure [" + structure + "] to a valid ImageToActivateInfo, was expecting 3 elements, but structure contains [" + structure.nrOfDataTypes() + "] elements !");
            }
        }

        /**
         * @return the imageIdentifier
         */
        public final String getImageIdentifier() {
            return this.imageIdentifier;
        }

        /**
         * @return the imageSignature
         */
        public final String getImageSignature() {
            return this.imageSignature;
        }

        /**
         * @return the imageSize
         */
        public final int getImageSize() {
            return this.imageSize;
        }
    }
}
