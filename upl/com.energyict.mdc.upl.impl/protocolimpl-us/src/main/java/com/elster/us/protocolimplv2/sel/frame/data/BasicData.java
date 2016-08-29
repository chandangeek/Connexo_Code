package com.elster.us.protocolimplv2.sel.frame.data;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import static com.elster.us.protocolimplv2.sel.utility.ByteArrayHelper.*;

public class BasicData extends Data{
  private byte[] asciiCommand;

  /**
   * Construct a new BasicData
   * @param SEL ascii command
   */
  public BasicData(byte[] asciiCommand) {
      this.asciiCommand = asciiCommand;
  }
  
  public BasicData(String asciiCommand) {
    this.asciiCommand = getBytes(asciiCommand);
}

  public byte[] getCommandCode() {
      return asciiCommand;
  }

  @Override
  public byte[] toByteArray(boolean includeEtx) throws IOException {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      bos.write(getCommandCode());
      return bos.toByteArray();
  }

}
