package com.elster.us.protocolimplv2.mercury.minimax.frame.data;

import com.elster.us.protocolimplv2.mercury.minimax.utility.ErrorTranslator;

import java.io.IOException;

import static com.elster.us.protocolimplv2.mercury.minimax.Consts.RESPONSE_OK;
import static com.elster.us.protocolimplv2.mercury.minimax.utility.ByteArrayHelper.getString;

/**
 * Represents the data returned in a basic response frame from the device
 * This just contains a two byte error code - 00 represents no error and all
 * other values indicate an error condition
 *
 * @author James Fox
 */
public class BasicResponseData extends BasicData {
    /**
     * Construct a new BasicResponse data
     * @param errorCode a 2-byte error code (
     */
    public BasicResponseData(byte[] errorCode) throws IOException {
        super(errorCode);
    }

    /**
     * Gets the error code returned from the instrument
     * @return a 2-byte error code
     */
    public byte[] getErrorCode() {
        return getCommand();
    }

    /**
     * Checks whether this is a positive response
     * @return true if a positive response, false if error
     * @throws IOException
     */
    public boolean isOK() throws IOException {
        return RESPONSE_OK.equals(getString(getErrorCode()));
    }

    /**
     * Gets the error code contained in the data
     * @return a string representation of the error message
     * @see ErrorTranslator
     * * @throws IOException
     */
    public String getError() {
        return ErrorTranslator.translateError(getErrorCode());
    }
}
