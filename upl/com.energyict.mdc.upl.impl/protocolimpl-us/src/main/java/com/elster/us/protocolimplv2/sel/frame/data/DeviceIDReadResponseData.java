package com.elster.us.protocolimplv2.sel.frame.data;

import static com.elster.us.protocolimplv2.sel.Consts.CONTROL_ETX;
import static com.elster.us.protocolimplv2.sel.Consts.RESPONSE_OK;
import static com.elster.us.protocolimplv2.sel.utility.ByteArrayHelper.getBytes;
import static com.elster.us.protocolimplv2.sel.utility.ByteArrayHelper.getString;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.StringTokenizer;

public class DeviceIDReadResponseData extends BasicResponseData {
  private String responseValue = null;
  private byte[] bytes;

  public DeviceIDReadResponseData(byte[] bytes) throws IOException {
      super(getBytes(RESPONSE_OK));
      parseBytes(bytes);
      this.bytes = bytes;
  }

  private void parseBytes(byte[] bytes) {
      String str = getString(bytes);
      StringTokenizer st = new StringTokenizer(str, "\r");
      while (st.hasMoreTokens()) {
        String token = st.nextToken();
        if(token.contains("DEVID")) {
          responseValue = token.substring(token.indexOf("=") +1, token.indexOf("\"", token.indexOf("=")));
        }
      }
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
