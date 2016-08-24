package com.elster.us.protocolimplv2.sel.frame.data;

import static com.elster.us.protocolimplv2.sel.Consts.CONTROL_ETX;
import static com.elster.us.protocolimplv2.sel.Consts.RESPONSE_OK;
import static com.elster.us.protocolimplv2.sel.utility.ByteArrayHelper.getBytes;
import static com.elster.us.protocolimplv2.sel.utility.ByteArrayHelper.getString;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class MultiReadResponseData extends BasicResponseData{
  List<String> responses = new ArrayList<String>();
  private byte[] bytes;

  public MultiReadResponseData(byte[] bytes) throws IOException {
      super(getBytes(RESPONSE_OK));
      parseBytes(bytes);
      this.bytes = bytes;
  }

  private void parseBytes(byte[] bytes) {
      String str = getString(bytes);
      StringTokenizer st = new StringTokenizer(str, ",");
      while (st.hasMoreTokens()) {
          responses.add(st.nextToken().trim());
      }
  }

  public String getResponse(int index) {
      return responses.get(index);
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
