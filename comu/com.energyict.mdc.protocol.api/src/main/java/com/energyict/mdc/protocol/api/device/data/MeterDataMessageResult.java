/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.data;

/**
 * A MessageResult created by the Protocol driver with additional MeterData information.
 */
public class MeterDataMessageResult extends MessageResult {

    private final MeterData meterData;

    /**
     * Creates a new instance of MessageEntry
     */
    private MeterDataMessageResult(MessageEntry messageEntry, int state, final String info, final MeterData meterData) {
        super(messageEntry, state, info);
        this.meterData = meterData;
    }

    /**
     * Creates a MeterDataMessageResult message with a SUCCESS state.
     *
     * @param messageEntry the MessageEntry for this result
     * @param protocolInfo the additional information from the protocol regarding this MessageEntry
     * @param meterData    the MeterData the protocol needed to fetch for this MessageEntry
     * @return a Successful MeterDataMessageResult
     */
    public static MeterDataMessageResult createSuccess(final MessageEntry messageEntry, final String protocolInfo, final MeterData meterData) {
        return new MeterDataMessageResult(messageEntry, MessageResult.getSUCCESS(), protocolInfo, meterData);
    }

    /**
     * Creates a MeterDataMessageResult message with a FAILED state.
     *
     * @param messageEntry the MessageEntry for this result
     * @param protocolInfo the additional information from the protocol regarding this MessageEntry
     * @param meterData    the MeterData the protocol needed to fetch for this MessageEntry
     * @return a Failed MeterDataMessageResult
     */
    public static MeterDataMessageResult createFailed(final MessageEntry messageEntry, final String protocolInfo, final MeterData meterData) {
        return new MeterDataMessageResult(messageEntry, MessageResult.getFAILED(), protocolInfo, meterData);
    }

    /**
     * Creates a MeterDataMessageResult message with a QUEUED state.
     *
     * @param messageEntry the MessageEntry for this result
     * @param protocolInfo the additional information from the protocol regarding this MessageEntry
     * @param meterData    the MeterData the protocol needed to fetch for this MessageEntry
     * @return a Queued MeterDataMessageResult
     */
    public static MeterDataMessageResult createQueued(final MessageEntry messageEntry, final String protocolInfo, final MeterData meterData) {
        return new MeterDataMessageResult(messageEntry, MessageResult.getQUEUED(), protocolInfo, meterData);
    }

    /**
     * Creates a MeterDataMessageResult message with a UNKNOWN state.
     *
     * @param messageEntry the MessageEntry for this result
     * @param protocolInfo the additional information from the protocol regarding this MessageEntry
     * @param meterData    the MeterData the protocol needed to fetch for this MessageEntry
     * @return a UnKnown MeterDataMessageResult
     */
    public static MeterDataMessageResult createUnknown(final MessageEntry messageEntry, final String protocolInfo, final MeterData meterData) {
        return new MeterDataMessageResult(messageEntry, MessageResult.getUNKNOWN(), protocolInfo, meterData);
    }

    public MeterData getMeterData() {
        return meterData;
    }
}
