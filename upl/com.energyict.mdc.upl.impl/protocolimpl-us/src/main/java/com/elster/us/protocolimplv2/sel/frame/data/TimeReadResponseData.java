package com.elster.us.protocolimplv2.sel.frame.data;

import static com.elster.us.protocolimplv2.sel.Consts.CONTROL_ETX;
import static com.elster.us.protocolimplv2.sel.Consts.RESPONSE_OK;
import static com.elster.us.protocolimplv2.sel.utility.ByteArrayHelper.getBytes;
import static com.elster.us.protocolimplv2.sel.utility.ByteArrayHelper.getString;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TimeReadResponseData extends BasicResponseData {
  private String responseValue = null;
  private byte[] bytes;

  public TimeReadResponseData(byte[] bytes) throws IOException {
    super(getBytes(RESPONSE_OK));
    parseBytes(bytes);
    this.bytes = bytes;
  }

  private void parseBytes(byte[] bytes) {
    //ignore first two bytes hh:mm:ss
    responseValue = getString(bytes).substring(2,10);
  }

  public String getValue() {
    return responseValue;
  }

  @Override
  public byte[] toByteArray(boolean includeEtx) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream(bytes.length+1);
    bos.write(bytes);
    if (includeEtx) {
        bos.write(CONTROL_ETX);
    }
    return bos.toByteArray();
  }

}
