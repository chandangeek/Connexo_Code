package com.energyict.dlms.cosem.generalblocktransfer;

import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.XdlmsApduTags;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.aso.SecurityContextV2EncryptionHandler;
import com.energyict.dlms.cosem.AbstractCosemObject;
import com.energyict.dlms.cosem.ExceptionResponseException;
import com.energyict.dlms.protocolimplv2.connection.DlmsV2Connection;
import com.energyict.dlms.protocolimplv2.connection.SecureConnection;
import com.energyict.mdc.exceptions.ComServerExecutionException;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Handler class that can handle the DLMS general block transfer process, as described in
 * the DLMS green book 8 section 9.4.6.13 (Protocol of general block transfer mechanism)
 *
 * @author sva
 * @since 25/03/2015 - 17:30
 */
public class GeneralBlockTransferHandler {

    private static final int LOCATION_SECURED_XDLMS_APDU_TAG = 0;
    private static final byte[] LEGACY_HDLC_HEADER_BYTES = new byte[]{0, 0, 0};
    private final Logger logger;
    /**
     * The head-end last send block number
     */
    private int blockNumber;
    /**
     * The head-end next block number
     */
    private int acknowledgedBlockNumber;
    /**
     * The block data that was kept from previous response packet
     */
    private byte[] storedBlockDataFromPreviousResponse;
    /**
     * The <b>full</b> block data
     */
    private byte[] responseData;
    /**
     * The DLMSConnection
     */
    private DlmsV2Connection dlmsConnection;
    /**
     * Boolean indicating whether or not general blcok transfer is enalbled/allowed or not
     */
    private boolean generalBlockTransferEnabled;
    /**
     * Field indicating the ComServers preferred general block transfer window size
     */
    private int comServerGBTWindowSize;
    /**
     * Field indicating the maximum number of retries
     */
    private int maxRetries;
    /**
     * Field indicating if the 3 bytes legacy header need to be stripped of each response /
     * need to be added to  each request or not
     */
    private boolean useLegacyHDLCHeader = true;

    public GeneralBlockTransferHandler(DlmsV2Connection dlmsConnection, Logger logger) {
        this.dlmsConnection = dlmsConnection;
        this.logger = logger;
        if (dlmsConnection != null) {
            this.comServerGBTWindowSize = (dlmsConnection != null) ? dlmsConnection.getGeneralBlockTransferWindowSize() : -1;
            this.generalBlockTransferEnabled = dlmsConnection.useGeneralBlockTransfer() && (dlmsConnection.getGeneralBlockTransferWindowSize() > 0);
            this.maxRetries = dlmsConnection.getMaxRetries();
        } else {
            this.generalBlockTransferEnabled = false;
            this.comServerGBTWindowSize = -1;
        }
    }

    /**
     * Handle the general block transfer (if necessary)
     *
     * @param rawResponse        the raw response of the device - this is the first response the device sends back after a ComServers GET-request.
     *                           This raw response can either be the full ADPU (in case the APDU content length is smaller than the max PDU size)
     *                           or a general block transfer APDU (in case the APDU content length exceeds the max PDU size).
     * @param retryRequest       The request, that can be used in case retries are needed
     * @param isAlreadyEncrypted Boolean indicating if the retryRequest is already encrypted or not
     * @return the full COSEM APDU, that can be passed on to the application layer
     * @throws IOException
     */
    public byte[] handleGeneralBlockTransfer(byte[] rawResponse, byte[] retryRequest, boolean isAlreadyEncrypted) {
        int axdrTagPosition = useLegacyHDLCHeader ? 3 : 0;
        if (generalBlockTransferEnabled
                && rawResponse != null
                && rawResponse.length > axdrTagPosition
                && rawResponse[axdrTagPosition] == DLMSCOSEMGlobals.COSEM_GENERAL_BLOCK_TRANSFER) {
            return doHandleGeneralBlockTransferWithRetries(rawResponse, retryRequest, isAlreadyEncrypted);
        } else {
            return rawResponse; // General block transfer not used, return data as-is
        }
    }

    /**
     * Do handle the general block transfer and retry in case of errors<br/>
     * If errors occur during the general block transfer (still blocks missing after lost-block recover, timeout, parsing error, ...),
     * the GBT process will be retried.<br/>
     * Note: Retries are implemented on COSEM APDU level, or in other words <b>the full general block transfer will be retried</b> (starting by again sending out the request
     * and then parsing the incoming blocks)
     *
     * @param rawResponse        the raw response of the device, already validated to be of GBT type
     * @param retryRequest       The request, that can be used in case retries are needed
     * @param isAlreadyEncrypted Boolean indicating if the retryRequest is already encrypted or not
     * @return the full COSEM APDU, that can be passed on to the application layer
     */
    private byte[] doHandleGeneralBlockTransferWithRetries(byte[] rawResponse, byte[] retryRequest, boolean isAlreadyEncrypted) {
        int currentRetryCount = 0;
        while (true) {
            try {
                rawResponse = removeLegacyHDLCHeadersFromRawResponse(rawResponse); // First 3 bytes are always legacy HDLC headers (these should be skipped)
                byte[] cosemResponse = doHandleGeneralBlockTransfer(rawResponse);
                return addLegacyHDLCHeadersToCosemApdu(cosemResponse);  // Re-add the 3 bytes, who represent the legacy HDLC header

            } catch (IOException | ComServerExecutionException e) {
                if (e instanceof ComServerExecutionException && !isConnectionCommunicationException(e)) {
                    throw (ComServerExecutionException) e;  //Throw exception, we're only interested in handling ConnectionCommunicationException and IOExceptions
                } else {
                    //Handle IOException and ConnectionCommunicationException
                    if (currentRetryCount++ >= this.maxRetries) {
                        IOException ioException = (e instanceof IOException) ? (IOException) e : new IOException(e.getMessage(), e);
                        throw MdcManager.getComServerExceptionFactory().createNumberOfRetriesReached(ioException, maxRetries + 1);
                    }

                    rawResponse = getDlmsV2Connection().sendRequest(retryRequest, isAlreadyEncrypted); // This call does take into account retries, so no need to catch & retry this call
                }
            }
        }
    }

    private byte[] doHandleGeneralBlockTransfer(byte[] rawResponse) throws IOException {
        GeneralBlockTransferFrame responseFrame = new GeneralBlockTransferFrame();

        // Parse the first general block transfer frame
        responseFrame.parseFrame(rawResponse);
        setBlockNumber(responseFrame.getAcknowledgedBlockNumber());
        setAcknowledgedBlockNumber(responseFrame.getBlockNumber());
        setResponseData(responseFrame.getBlockData());

        boolean isLastBlock = responseFrame.getBlockControl().isLastBlock();
        while (!isLastBlock) {
            // Flush the storedBlockDataFromPreviousResponse byte array
            flushStoredBlockDataFromPreviousResponse();

            // Compose and send out the request for next 'windowSize' number of blocks
            GeneralBlockTransferFrame requestFrame = composeRequestForNextBlocks();
            getDlmsV2Connection().sendUnconfirmedRequest(addLegacyHDLCHeadersToCosemApdu(requestFrame.getBytes()));

            // Receive the next 'windowSize' number of blocks (including retrieval of missing blocks)
            Map<Integer, GeneralBlockTransferFrame> receivedBlocks = receiveNextBlocksFromDevice(getComServerGeneralBlockTransferWindowSize(), true);

            // Add all block data from the individual blocks to the responseData byte array and update the block numbers
            for (int i = 1; i <= receivedBlocks.size(); i++) {
                GeneralBlockTransferFrame frame = receivedBlocks.get(getAcknowledgedBlockNumber() + 1);
                addNextBlockDataToResponseData(frame.getBlockData());

                setAcknowledgedBlockNumber(frame.getBlockNumber());
                setBlockNumber(frame.getAcknowledgedBlockNumber());
                isLastBlock = frame.getBlockControl().isLastBlock();

                if (isLastBlock) {
                    break;  // This is the last block, so not useful to continue the loop
                }
            }
        }

        decryptResponseData();
        return getResponseData();
    }

    /**
     * Method to receive the next 'windowSize' number of blocks (including the retrieval of missing blocks)
     *
     * @param comServerWindowSize   the window size, which determines the expected number of blocks
     * @param checkForMissingBlocks boolean indicating whether or not there should be checked for missing blocks
     */
    private Map<Integer, GeneralBlockTransferFrame> receiveNextBlocksFromDevice(int comServerWindowSize, boolean checkForMissingBlocks) throws IOException {
        Map<Integer, GeneralBlockTransferFrame> receivedBlocks = new HashMap<>();

        try {
            for (int i = 0; i < comServerWindowSize; i++) {
                GeneralBlockTransferFrame responseFrame = new GeneralBlockTransferFrame();
                byte[] receivedBytes = readAndValidateRawResponse();
                byte[] response = ProtocolTools.concatByteArrays(getStoredBlockDataFromPreviousResponse(), receivedBytes);

                int offset = responseFrame.parseFrame(response);
                if (responseFrame.getLengthOfBlockData() == 0) {
                    throw new ProtocolException("GeneralBlockTransferHandler, receiveNextBlocksFromDevice - Fetch of block " + responseFrame.getBlockNumber() + " failed, the block content was empty.");
                }

                setStoredBlockDataFromPreviousResponse(ProtocolTools.getSubArray(response, offset));
                receivedBlocks.put(responseFrame.getBlockNumber(), responseFrame);

                if (responseFrame.getBlockControl().isLastBlock() || !responseFrame.getBlockControl().isStreamingMode()) {
                    break;
                } else {
                    getDlmsV2Connection().prepareComChannelForReceiveOfNextPacket(); // To ensure logging of next received packet is correct
                }
            }
        } catch (ComServerExecutionException | IOException e) {
            if (e instanceof ComServerExecutionException && !isConnectionCommunicationException(e)) {
                throw (ComServerExecutionException) e;  //Throw exception, we're only interested in handling ConnectionCommunicationException and IOExceptions
            } else {
                //Handle IOException and ConnectionCommunicationException
                if (!checkForMissingBlocks) {
                    throw e;    // went wrong during missing block receive, do throw the error
                } else {
                    // Absorb exception, during missing block check - in method fetchAllMissingBlocks we will request the missing blocks again
                    getLogger().warning("GeneralBlockTransferHandler, receiveNextBlocksFromDevice failed, will switch to lost block recovery!");
                }
            }
        }

        if (checkForMissingBlocks) {
            receivedBlocks.putAll(fetchAllMissingBlocks(receivedBlocks));
        }
        return receivedBlocks;
    }

    private boolean isConnectionCommunicationException(Exception e) {
        return MdcManager.getComServerExceptionFactory().isConnectionCommunicationException(e);
    }

    /**
     * Read the next raw response from the DLMSConnection and validate it<br/>
     * If the raw response contains an error (either confirmed service error or exception response), then the corresponding error will be thrown.
     * Note: For this call, no retries will be done.
     *
     * @return the raw response (of which the legacy 3 byte HDLC header is already stripped of)
     * @throws IOException
     */
    private byte[] readAndValidateRawResponse() throws IOException {
        int oldNumberOfRetries = getDlmsV2Connection().getMaxRetries();
        try {
            getDlmsV2Connection().setRetries(0);  // Disable retries for this call
            byte[] dummyRetryRequest = new byte[20];
            dummyRetryRequest[3] = DLMSCOSEMGlobals.COSEM_GENERAL_BLOCK_TRANSFER;   // Should be the General-block-transfer tag
            byte[] rawResponse = getDlmsV2Connection().readResponseWithRetries(dummyRetryRequest); // RetryRequest is just a dummy byte array, as we actually will not do any retries
            validateRawResponse(rawResponse);
            return removeLegacyHDLCHeadersFromRawResponse(rawResponse); // First 3 bytes are always legacy HDLC headers, these should be stripped of
        } finally {
            getDlmsV2Connection().setRetries(oldNumberOfRetries);
        }
    }

    private void validateRawResponse(byte[] rawResponse) throws IOException {
        int i = useLegacyHDLCHeader ? DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET : 0;
        switch (rawResponse[i]) {
            case DLMSCOSEMGlobals.COSEM_CONFIRMEDSERVICEERROR:
                AbstractCosemObject.handleConfirmedServiceError(rawResponse);  // This call will parse and throw the appropriate error
            case DLMSCOSEMGlobals.COSEM_EXCEPTION_RESPONSE:
                throw new ExceptionResponseException(rawResponse[i + 1], rawResponse[i + 2]);
        }
    }

    private Map<Integer, GeneralBlockTransferFrame> fetchAllMissingBlocks(Map<Integer, GeneralBlockTransferFrame> receivedBlocks) throws IOException {
        flushStoredBlockDataFromPreviousResponse();
        Map<Integer, GeneralBlockTransferFrame> retrievedMissingBlocks = new HashMap<>(0);
        for (int i = 1; i <= getComServerGeneralBlockTransferWindowSize(); i++) {
            GeneralBlockTransferFrame frame = receivedBlocks.get(getAcknowledgedBlockNumber() + i);
            if (frame == null) {
                // The block is missing > request the block again
                frame = fetchMissingBlock(getAcknowledgedBlockNumber() + i);
                retrievedMissingBlocks.put(getAcknowledgedBlockNumber() + i, frame);
            }

            if (frame.getBlockControl().isLastBlock()) {
                break;  // This is the last block, so not useful to continue the loop
            }
        }
        return retrievedMissingBlocks;
    }

    private GeneralBlockTransferFrame fetchMissingBlock(int blockNumberOfMissingBlock) throws IOException {
        // Flush the storedBlockDataFromPreviousResponse byte array
        flushStoredBlockDataFromPreviousResponse();

        // Compose and send out the request for next 'windowSize' number of blocks
        GeneralBlockTransferFrame requestFrame = composeRequestForMissingBlock(blockNumberOfMissingBlock);
        getDlmsV2Connection().sendUnconfirmedRequest(addLegacyHDLCHeadersToCosemApdu(requestFrame.getBytes()));

        // Receive the missing block
        Map<Integer, GeneralBlockTransferFrame> receivedBlocks = receiveNextBlocksFromDevice(1, false); // During re-request of missing block, not useful to check for missing blocks
        GeneralBlockTransferFrame missingFrame = receivedBlocks.get(blockNumberOfMissingBlock);
        if (missingFrame != null) {
            return missingFrame;
        } else {
            // Didn't received the expected block, but we apparently got another block...
            throw new ProtocolException("GeneralBlockTransferHandler, fetchMissingBlock - Fetch of missing block " + blockNumberOfMissingBlock + " failed, received wrong block.");
        }
    }

    private GeneralBlockTransferFrame composeRequestForNextBlocks() {
        GeneralBlockTransferFrame requestFrame = new GeneralBlockTransferFrame();
        requestFrame.setBlockControl(getBlockControl(true, false, getComServerGeneralBlockTransferWindowSize()));
        requestFrame.setBlockNumber(increaseAndGetNextBlockNumber());
        requestFrame.setAcknowledgedBlockNumber(getAcknowledgedBlockNumber());
        return requestFrame;
    }

    private GeneralBlockTransferFrame composeRequestForMissingBlock(int blockNumberOfMissingBlock) throws IOException {
        GeneralBlockTransferFrame requestFrame = new GeneralBlockTransferFrame();
        requestFrame.setBlockControl(getBlockControl(true, false, 1));  // Use window-sze 1
        requestFrame.setBlockNumber(increaseAndGetNextBlockNumber());
        requestFrame.setAcknowledgedBlockNumber(blockNumberOfMissingBlock - 1);
        return requestFrame;
    }

    /**
     * Check if ciphering is applied, and if so, then decrypt the response data
     */
    private void decryptResponseData() {
        if (getDlmsV2Connection() instanceof SecureConnection) {
            SecureConnection secureConnection = (SecureConnection) getDlmsV2Connection();

            /* Check if security is applied or not */
            if (secureConnection.getAso().getSecurityContext().getSecurityPolicy() != SecurityContext.SECURITYPOLICY_NONE) {
                byte cipheredTag = getResponseData()[LOCATION_SECURED_XDLMS_APDU_TAG];
                if (XdlmsApduTags.contains(cipheredTag)) {
                    this.responseData = decrypt(secureConnection, getResponseData());
                } else if (cipheredTag == DLMSCOSEMGlobals.GENERAL_GLOBAL_CIPHERING || cipheredTag == DLMSCOSEMGlobals.GENERAL_DEDICATED_CIPTHERING) {
                    this.responseData = decryptGeneralCiphering(secureConnection, getResponseData());
                } else {
                    IOException ioException = new IOException("Unknown GlobalCiphering-Tag : " + getResponseData()[LOCATION_SECURED_XDLMS_APDU_TAG]);
                    throw MdcManager.getComServerExceptionFactory().createUnExpectedProtocolError(ioException);
                }
            }
        }
    }

    private byte[] decrypt(SecureConnection secureConnection, byte[] securedResponse) {
        return SecurityContextV2EncryptionHandler.dataTransportDecryption(secureConnection.getAso().getSecurityContext(), securedResponse);
    }

    private byte[] decryptGeneralCiphering(SecureConnection secureConnection, byte[] securedResponse) {
        return SecurityContextV2EncryptionHandler.dataTransportGeneralDecryption(secureConnection.getAso().getSecurityContext(), securedResponse);
    }

    /**
     * Getter for the {@link com.energyict.dlms.DLMSConnection}
     *
     * @return the DLMSConnection
     */
    private DlmsV2Connection getDlmsV2Connection() {
        return dlmsConnection;
    }

    /**
     * Getter for the head-end last send block number
     */
    private int getBlockNumber() {
        return blockNumber;
    }

    private void setBlockNumber(int blockNumber) {
        this.blockNumber = blockNumber;
    }

    /**
     * Getter for the head-end next block number<br/>
     * Warning: thie method will increase the blockNumber parameter
     *
     * @return
     */
    private int increaseAndGetNextBlockNumber() {
        return ++blockNumber;
    }

    /**
     * Getter for the head-end last received block number
     */
    private int getAcknowledgedBlockNumber() {
        return acknowledgedBlockNumber;
    }

    private void setAcknowledgedBlockNumber(int acknowledgedBlockNumber) {
        this.acknowledgedBlockNumber = acknowledgedBlockNumber;
    }

    /**
     * Getter for the block data that was kept from previous response packet *
     */
    private byte[] getStoredBlockDataFromPreviousResponse() {
        return storedBlockDataFromPreviousResponse != null ? storedBlockDataFromPreviousResponse : new byte[0];
    }

    private void setStoredBlockDataFromPreviousResponse(byte[] storedBlockDataFromPreviousResponse) {
        this.storedBlockDataFromPreviousResponse = storedBlockDataFromPreviousResponse;
    }

    private void flushStoredBlockDataFromPreviousResponse() {
        this.storedBlockDataFromPreviousResponse = new byte[0];
    }

    /**
     * Getter for the <b>full</b> block data
     */
    private byte[] getResponseData() {
        return responseData;
    }

    private void setResponseData(byte[] responseData) {
        this.responseData = responseData;
    }

    private void addNextBlockDataToResponseData(byte[] nextBlockData) {
        setResponseData(ProtocolTools.concatByteArrays(getResponseData(), nextBlockData));
    }

    private BlockControl getBlockControl(boolean lastBlock, boolean streamingMode, int windowSize) {
        BlockControl blockControl = new BlockControl();
        blockControl.setLastBlock(lastBlock);
        blockControl.setStreamingMode(streamingMode);
        blockControl.setWindowSize(windowSize);
        return blockControl;
    }

    private int getComServerGeneralBlockTransferWindowSize() {
        return comServerGBTWindowSize;
    }

    /**
     * If necessary, removes the legacy 3 byte HDLC header from the raw response
     */
    private byte[] removeLegacyHDLCHeadersFromRawResponse(byte[] rawResponse) {
        if (useLegacyHDLCHeader) {
            return ProtocolTools.getSubArray(rawResponse, 3);
        } else {
            return rawResponse;
        }
    }

    /**
     * If necessary, adds the legacy 3 byte HDLC header to the given cosemApdu
     */
    private byte[] addLegacyHDLCHeadersToCosemApdu(byte[] cosemApdu) {
        if (useLegacyHDLCHeader) {
            return ProtocolTools.concatByteArrays(LEGACY_HDLC_HEADER_BYTES, cosemApdu);
        } else {
            return cosemApdu;
        }
    }

    private Logger getLogger() {
        return logger;
    }

    /**
     * Setter for the useLegacyHDLCHeader field.
     *
     * @param useLegacyHDLCHeader If set to true, then for each request the 3 bytes legacy HDLC header will be added and
     *                            for each response, the 3 bytes HDLC header will be removed.
     *                            If set to false, the legacy header is not taken into account (or in other words, the raw request/response doesn't contain the legacy header)
     */
    public void setUseLegacyHDLCHeader(boolean useLegacyHDLCHeader) {
        this.useLegacyHDLCHeader = useLegacyHDLCHeader;
    }
}