package com.elster.us.protocolimplv2.mercury.minimax.utility;

import static com.elster.us.protocolimplv2.mercury.minimax.Consts.*;
import static com.elster.us.protocolimplv2.mercury.minimax.utility.ByteArrayHelper.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Translates error codes from the protocol into human-readable strings
 *
 * @author James Fox
 */
public final class ErrorTranslator {

    private static final Map<String,String> errorCodeStrings = new HashMap<String,String>();

    private static final String ERROR_UNDEFINED = "Error code is not mapped: ";

    static {
        errorCodeStrings.put(RESPONSE_OK, "No error, completed successfully");
        errorCodeStrings.put(ERROR_TIMEOUT_BEFORE_SIGN_ON, "Timeout before sign on packet was sent");
        errorCodeStrings.put(ERROR_FRAMING, "Framing error: too many bytes received before ETX");
        errorCodeStrings.put(ERROR_FORMAT, "Format error: incorrectly formatted frame sent to device");
        errorCodeStrings.put(ERROR_CRC, "CRC error: frame with incorrect checksum sent to device");
        errorCodeStrings.put(ERROR_UNKNOWN_COMMAND, "Unknown command sent to device");
        errorCodeStrings.put(ERROR_INVALID_ENQUIRY, "Invalid enquiry - device probably already linked");
        errorCodeStrings.put(ERROR_NO_AUDIT_TRAIL_RECORDS_AVAILABLE, "No audit trail records available for period");
    }

    // Prevent instatiation of class
    private ErrorTranslator() {}

    /**
     * Translates an error code from the protocol into a readable string
     * @param errorCode two byte error code from the device
     * @return String representation of the error code
     */
    public static String translateError(byte[] errorCode) {
        String errorCodeStr = getString(errorCode);
        if (errorCodeStrings.containsKey(errorCodeStr)) {
            return errorCodeStrings.get(errorCodeStr);
        } else {
            return ERROR_UNDEFINED + errorCodeStr;
        }
    }
}
