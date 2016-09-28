package com.elster.us.protocolimplv2.sel.frame.data;

import static com.elster.us.protocolimplv2.sel.Consts.CONTROL_ETX;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ExtendedData extends BasicData{
  private byte[] commandParams;

  public ExtendedData(byte[] commandCode, byte[] commandParams) {
      super(commandCode);
      this.commandParams = commandParams;
  }

  public byte[] getCommandParams() {
      return commandParams;
  }

  @Override
  public byte[] toByteArray(boolean includeEtx) throws IOException {
      byte[] basic = super.toByteArray(false);
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      bos.write(basic);
      bos.write(getCommandParams());
      if (includeEtx) {
          bos.write(CONTROL_ETX);
      }
      return bos.toByteArray();
  }

}
