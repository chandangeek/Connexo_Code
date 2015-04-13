package com.energyict.dlms.cosem.generalblocktransfer;

import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DLMSUtils;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * @author sva
 * @since 25/03/2015 - 17:27
 */
public class GeneralBlockTransferFrame {

    private BlockControl blockControl;
    private int blockNumber;
    private int acknowledgedBlockNumber;
    private int lengthOfBlockData;
    private byte[] blockData;

    public GeneralBlockTransferFrame() {
    }

    /**
     * Parse the next GBT frame from the given byteArray
     *
     * @param bytes  The byteArray containing the GBT frame
     * @return the new offset in the byteArray (~ original offset increased with the length of the parsed GBT frame)
     */
    public int parseFrame(byte[] bytes) throws ProtocolException {
        return parseFrame(bytes, 0);
    }

    /**
     * Parse the next GBT frame from the given byteArray
     *
     * @param bytes  The byteArray containing the GBT frame
     * @param offset The offset in the byteArray from which to start reading the length
     * @return the new offset in the byteArray (~ original offset increased with the length of the parsed GBT frame)
     */
    public int parseFrame(byte[] bytes, int offset) throws ProtocolException {
        try {
            int ptr = offset;
            if (bytes[ptr] != DLMSCOSEMGlobals.COSEM_GENERAL_BLOCK_TRANSFER) {
                throw new ProtocolException("Didn't receive a valid general-block-transfer frame: the general-block-transfer APDU should start with tag 0xE0, but was " +
                        ProtocolTools.getHexStringFromBytes(new byte[]{bytes[offset]}, "0x"));
            }
            ptr++;  // Skip the tag (0xE0)

            setBlockControl(new BlockControl(bytes[ptr]));
            ptr++;

            parseBlockNumber(bytes, ptr);
            ptr += 2;

            parseAcknowledgedBlockNumber(bytes, ptr);
            ptr += 2;

            setLengthOfBlockData(DLMSUtils.getAXDRLength(bytes, ptr));
            ptr += DLMSUtils.getAXDRLengthOffset(bytes, ptr);

            parseBlockData(bytes, ptr);
            ptr += getBlockData().length;

            return ptr;
        } catch (IndexOutOfBoundsException e) {
            throw new ProtocolException(e, "Failed to parse the general-block-transfer APDU: " + e);
        }
    }

    /**
     * Getter for blockControl field. This field holds lastBlock/streamingMode/window size information.
     */
    public BlockControl getBlockControl() {
        return blockControl;
    }

    public void setBlockControl(BlockControl blockControl) {
        this.blockControl = blockControl;
    }

    /**
     * Getter for blockNumber field. This field indicates the number of blocks that have been sent out by the party.<br/>
     * Take into account this number is 1-based (so the first block sent shall have block-number 1)<br/>
     * The block-number should be increased for each GBT frame that is sent, even is the block-data is empty;
     * only in case of lost block recovery a block number may be repeated.
     */
    public int getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(int blockNumber) {
        this.blockNumber = blockNumber;
    }

    private void parseBlockNumber(byte[] bytes, int offset) {
        this.blockNumber = ((bytes[offset] & 0xFF) << 8) + (bytes[offset + 1] & 0xFF);
    }

    /**
     * Getter for the acknowledgedBlockNumber. This field indicates the number of the last successful received block.<br/>
     * If no blocks have been lost, this will be equal to the nuber of the last received block;<br/>
     * If blocks are missing, this will be equal to the number of the  block up to which no blocks are missing.
     *
     * @return
     */
    public int getAcknowledgedBlockNumber() {
        return acknowledgedBlockNumber;
    }

    public void setAcknowledgedBlockNumber(int acknowledgedBlockNumber) {
        this.acknowledgedBlockNumber = acknowledgedBlockNumber;
    }

    private void parseAcknowledgedBlockNumber(byte[] bytes, int offset) {
        this.acknowledgedBlockNumber = ((bytes[offset] & 0xFF) << 8) + (bytes[offset + 1] & 0xFF);
    }

    /**
     * Getter fot eh lengthOfBlockData. This field indicates the length of the block data.<br/>
     * Take into account that the transmitted length (the bytes) should be encoded according to common AXDR length encoding rules
     */
    public int getLengthOfBlockData() {
        return lengthOfBlockData;
    }

    public void setLengthOfBlockData(int lengthOfBlockData) {
        this.lengthOfBlockData = lengthOfBlockData;
    }

    /**
     * Getter for blockData field. This field indicates the block data.<br/>
     * In case there was no need to send any data, this field will contain an empty array.
     */
    public byte[] getBlockData() {
        return blockData;
    }

    public void setBlockData(byte[] blockData) {
        this.blockData = blockData;
    }

    private void parseBlockData(byte[] bytes, int offset) throws ProtocolException {
        int from = offset;
        int to = offset + getLengthOfBlockData();
        if (ProtocolTools.isArrayIndexInRange(bytes, from) && ProtocolTools.isArrayIndexInRange(bytes, to - 1) && (from < to)) {
            this.blockData = ProtocolTools.getSubArray(bytes, from, to);
        } else {
            throw new ProtocolException("Failed to parse the general-block-transfer APDU: Could not parse the block data, " +
                    "the indicated block length (" + getLengthOfBlockData() + ") doesn't match actual block data length");
        }
    }

    public byte[] getBytes() {
        byte[] tagBytes = new byte[]{DLMSCOSEMGlobals.COSEM_GENERAL_BLOCK_TRANSFER};
        byte[] blockControlBytes = getBlockControl().getBytes();
        byte[] blockNumberBytes = ProtocolTools.getBytesFromInt(getBlockNumber(), 2);
        byte[] acknowledgedBlockNumberBytes = ProtocolTools.getBytesFromInt(getAcknowledgedBlockNumber(), 2);
        byte[] lengthOfBlockDataBytes = DLMSUtils.getAXDRLengthEncoding(getLengthOfBlockData());
        byte[] blockDataBytes = getBlockData();

        return ProtocolTools.concatByteArrays(
                tagBytes,
                blockControlBytes,
                blockNumberBytes,
                acknowledgedBlockNumberBytes,
                lengthOfBlockDataBytes,
                blockDataBytes
        );
    }
}