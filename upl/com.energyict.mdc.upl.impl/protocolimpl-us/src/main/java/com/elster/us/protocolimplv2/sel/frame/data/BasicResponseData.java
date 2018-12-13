package com.elster.us.protocolimplv2.sel.frame.data;

import static com.elster.us.protocolimplv2.sel.Consts.RESPONSE_OK;
import static com.elster.us.protocolimplv2.sel.utility.ByteArrayHelper.getString;

import java.io.IOException;

import com.elster.us.protocolimplv2.sel.utility.ErrorTranslator;

public class BasicResponseData extends BasicData{
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
      return getCommandCode();
  }

  /**
   * Checks whether this is a positive response
   * @return true if a positive response, false if error
   * @throws IOException
   */
  public boolean isOK() {
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
