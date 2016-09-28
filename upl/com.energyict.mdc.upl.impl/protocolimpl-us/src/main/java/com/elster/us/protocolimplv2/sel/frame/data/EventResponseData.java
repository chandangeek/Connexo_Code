package com.elster.us.protocolimplv2.sel.frame.data;

import static com.elster.us.protocolimplv2.sel.Consts.CONTROL_ETX;
import static com.elster.us.protocolimplv2.sel.Consts.RESPONSE_OK;
import static com.elster.us.protocolimplv2.sel.utility.ByteArrayHelper.getBytes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class EventResponseData extends BasicResponseData {
  private byte[] bytes;

  public EventResponseData(byte[] bytes) throws IOException {
      super(getBytes(RESPONSE_OK));
      parseBytes(bytes);
      this.bytes = bytes;
  }

  private void parseBytes(byte[] bytes) {
      // TODO
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
