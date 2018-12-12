package com.energyict.dlms.cosem.generalblocktransfer;

/**
 * @author sva
 * @since 26/03/2015 - 11:42
 */
public class BlockControl {

    private boolean lastBlock;
    private boolean streamingMode;
    private int windowSize;

    public BlockControl() {
    }

    public BlockControl(byte blockControlByte) {
        setLastBlock((blockControlByte & 0x80) == 0x80);
        setStreamingMode((blockControlByte & 0x40) == 0x40);
        setWindowSize(blockControlByte & 0x3F);
    }

    /**
     * Getter for the lastBlock field. This field indicates if the block is the last one (= true) or not (=false)<br/>
     * If lastBlock is set to false, then more blocks should follow after this one
     */
    public boolean isLastBlock() {
        return lastBlock;
    }

    public void setLastBlock(boolean lastBlock) {
        this.lastBlock = lastBlock;
    }

    /**
     * Getter for the sreamingMode field. This field indicates if steaming is in progress (= true) or finished (=false). <br/>
     * When streaming is finished, the remote party shall confirm the blocks received.<br/>
     */
    public boolean isStreamingMode() {
        return streamingMode;
    }

    public void setStreamingMode(boolean streamingMode) {
        this.streamingMode = streamingMode;
    }

    /**
     * Getter for the windowSize field. This field indicates the number of blocks that can be received before an acknowledgement should be send over.<br/>
     * E.g. set to 5 to indicate 5 consequent blocks can be send before requiring an acknowledgement from the other party.<br/>
     * This size can be set to a lower value (typically 1) during lost block recovery
     */
    public int getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }

    public byte[] getBytes() {
        int blockControl = (isLastBlock() ? 0x80 : 0)
                + (isStreamingMode() ? 0x40 : 0)
                + getWindowSize();
        return new byte[]{(byte) blockControl};
    }
}