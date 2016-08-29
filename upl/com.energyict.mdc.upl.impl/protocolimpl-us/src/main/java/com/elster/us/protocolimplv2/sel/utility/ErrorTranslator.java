package com.elster.us.protocolimplv2.sel.utility;

import static com.elster.us.protocolimplv2.sel.Consts.ERROR_CRC;
import static com.elster.us.protocolimplv2.sel.Consts.ERROR_FORMAT;
import static com.elster.us.protocolimplv2.sel.Consts.ERROR_FRAMING;
import static com.elster.us.protocolimplv2.sel.Consts.ERROR_TIMEOUT_BEFORE_SIGN_ON;
import static com.elster.us.protocolimplv2.sel.Consts.ERROR_UNKNOWN_COMMAND;
import static com.elster.us.protocolimplv2.sel.Consts.RESPONSE_OK;
import static com.elster.us.protocolimplv2.sel.utility.ByteArrayHelper.getString;

import java.util.HashMap;
import java.util.Map;

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
  }

  private ErrorTranslator() {}

  public static String translateError(byte[] errorCode) {
      String errorCodeStr = getString(errorCode);
      if (errorCodeStrings.containsKey(errorCodeStr)) {
          return errorCodeStrings.get(errorCodeStr);
      } else {
          return ERROR_UNDEFINED + errorCodeStr;
      }
  }

}
