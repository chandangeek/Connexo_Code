/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.garnet.frame.field;

/**
 * @author sva
 * @since 23/05/2014 - 13:41
 */
public enum FunctionCode {

    LOGBOOK_EVENT_REQUEST(0x10, 14, "LogBookEventRequestStructure", FrameFormat.REGULAR_FRAME_FORMAT, EncryptionMode.MANUFACTURER_KEY),
    LOGBOOK_EVENT_RESPONSE(0x11, 38, "LogBookEventResponseStructure", FrameFormat.REGULAR_FRAME_FORMAT, EncryptionMode.MANUFACTURER_KEY),
    CONCENTRATOR_VERSION_REQUEST(0x12, 6, "ConcentratorVersionRequestStructure", FrameFormat.REGULAR_FRAME_FORMAT, EncryptionMode.MANUFACTURER_KEY),
    CONCENTRATOR_VERSION_RESPONSE(0x13, 22, "ConcentratorVersionResponseStructure", FrameFormat.REGULAR_FRAME_FORMAT, EncryptionMode.MANUFACTURER_KEY),
    OPEN_SESSION_REQUEST(0x20,14, "OpenSessionRequestStructure", FrameFormat.REGULAR_FRAME_FORMAT, EncryptionMode.CUSTOMER_KEY),
    OPEN_SESSION_RESPONSE(0x21, 22, "OpenSessionResponseStructure", FrameFormat.REGULAR_FRAME_FORMAT, EncryptionMode.CUSTOMER_KEY),
    POOLING_REQUEST(0x22, 6, "PoolingRequestStructure", FrameFormat.REGULAR_FRAME_FORMAT, EncryptionMode.SESSION_KEY),
    POOLING_RESPONSE_WITHOUT_LOGS(0x23, 14, "PoolingResponseWithoutLogsStructure", FrameFormat.REGULAR_FRAME_FORMAT, EncryptionMode.SESSION_KEY),
    POOLING_RESPONSE_WITH_LOGS(0x24, 38, "PoolingResponseWithLogsStructure", FrameFormat.REGULAR_FRAME_FORMAT, EncryptionMode.SESSION_KEY),
    CONFIRM_LOG_RECEIVE(0x25, 14, "ConfirmLogReceiveStructure", FrameFormat.REGULAR_FRAME_FORMAT, EncryptionMode.SESSION_KEY),
    DISCOVER_METERS_REQUEST(0x26, 6, "DiscoverMetersRequestStructure", FrameFormat.REGULAR_FRAME_FORMAT, EncryptionMode.SESSION_KEY),
    DISCOVER_METERS_RESPONSE(0x27, 118, "DiscoverMetersResponseStructure", FrameFormat.REGULAR_FRAME_FORMAT, EncryptionMode.SESSION_KEY),
    CHECKPOINT_READING_REQUEST(0x28, 14, "CheckPointReadingRequestStructure", FrameFormat.REGULAR_FRAME_FORMAT, EncryptionMode.SESSION_KEY),
    CHECKPOINT_READING_RESPONSE(0x29, 118, "CheckPointReadingResponseStructure", FrameFormat.REGULAR_FRAME_FORMAT, EncryptionMode.SESSION_KEY),
    ONLINE_READING_REQUEST(0x2A, 14, "OnlineReadingRequestStructure", FrameFormat.REGULAR_FRAME_FORMAT, EncryptionMode.SESSION_KEY),
    ONLINE_READING_RESPONSE(0x2B, 118, "OnlineReadingResponseStructure", FrameFormat.REGULAR_FRAME_FORMAT, EncryptionMode.SESSION_KEY),
    CONCENTRATOR_STATUS_REQUEST(0x30, 6, "ConcentratorStatusRequestStructure", FrameFormat.REGULAR_FRAME_FORMAT, EncryptionMode.SESSION_KEY),
    CONCENTRATOR_STATUS_RESPONSE(0x31, 14, "ConcentratorStatusResponseStructure", FrameFormat.REGULAR_FRAME_FORMAT, EncryptionMode.SESSION_KEY),
    CONTACTOR_REQUEST(0x42, 22, "ContactorRequestStructure", FrameFormat.REGULAR_FRAME_FORMAT, EncryptionMode.SESSION_KEY),
    CONTACTOR_RESPONSE(0x43, 22, "ContactorResponseStructure", FrameFormat.REGULAR_FRAME_FORMAT, EncryptionMode.SESSION_KEY),
    NOT_EXECUTED_RESPONSE(0x49, 6, "NotExecutedResponseStructure", FrameFormat.REGULAR_FRAME_FORMAT, EncryptionMode.MANUFACTURER_KEY),
    DISCOVER_REPEATERS_REQUEST(0x4E, 6, "DiscoverRepeatersRequestStructure", FrameFormat.EXTENDED_FRAME_FORMAT, EncryptionMode.SESSION_KEY),
    DISCOVER_REPEATERS_RESPONSE(0x4F, 126, "DiscoverRepeatersResponseStructure", FrameFormat.EXTENDED_FRAME_FORMAT, EncryptionMode.SESSION_KEY),
    RADIO_PARAMETERS_REQUEST(0xE2, 3, "RadioParametersRequestStructure", FrameFormat.REGULAR_FRAME_FORMAT, EncryptionMode.NO_ENCRYPTION),
    RADIO_PARAMETERS_RESPONSE(0xD4, 24, "RadioParametersResponseStructure", FrameFormat.SHORT_FRAME_FORMAT, EncryptionMode.NO_ENCRYPTION),
    UNKNOWN(0x0, 0, "UnknownStructure", FrameFormat.REGULAR_FRAME_FORMAT, EncryptionMode.NO_ENCRYPTION);

    public enum EncryptionMode {
        MANUFACTURER_KEY,
        CUSTOMER_KEY,
        SESSION_KEY,
        NO_ENCRYPTION;
    }

    public enum FrameFormat {
        REGULAR_FRAME_FORMAT,
        SHORT_FRAME_FORMAT, // SourceId is not included in the frame
        EXTENDED_FRAME_FORMAT
    }

    public enum SOURCE_ID_INCLUDED {
        SOURCE_ID_INCLUDED,
        SOURCE_ID_EXCLUDED,
    }

    /**
     * The int function code of the function
     */
    private final int functionCode;

    /**
     * The textual description of the function
     */
    private final String name;

    /**
     * The length of data field of a frame of type Function
     */
    private final int dataLength;

    /**
     * Enum value indicating whether or not this function required encryption and which key has to be used
     */
    private final EncryptionMode encryptionMode;

    /**
     * Enum value indicating whether or not this function requires extended frame format
     */
    private final FrameFormat frameFormat;

    private FunctionCode(int functionCode, int dataLength, String name, FrameFormat frameFormat, EncryptionMode encryptionMode) {
        this.functionCode = functionCode;
        this.dataLength = dataLength;
        this.name = name;
        this.frameFormat = frameFormat;
        this.encryptionMode = encryptionMode;
    }

    public int getFunctionCode() {
        return functionCode;
    }

    public int getDataLength() {
        return dataLength;
    }

    public String getName() {
        return name;
    }

    public FrameFormat getFrameFormat() {
        return frameFormat;
    }

    public boolean usesExtendedFrameFormat() {
        return frameFormat.equals(FrameFormat.EXTENDED_FRAME_FORMAT);
    }

    public boolean usesShortFrameFormat() {
        return frameFormat.equals(FrameFormat.SHORT_FRAME_FORMAT);
    }

    public EncryptionMode getEncryptionMode() {
        return encryptionMode;
    }

    public boolean needsEncryption() {
        return encryptionMode.equals(EncryptionMode.MANUFACTURER_KEY) ||
                encryptionMode.equals(EncryptionMode.CUSTOMER_KEY) ||
                encryptionMode.equals(EncryptionMode.SESSION_KEY);
    }

    public static FunctionCode fromCode(int code) {
        for (FunctionCode functionCode : FunctionCode.values()) {
            if (functionCode.getFunctionCode() == code) {
                return functionCode;
            }
        }
        return FunctionCode.UNKNOWN;
    }
}