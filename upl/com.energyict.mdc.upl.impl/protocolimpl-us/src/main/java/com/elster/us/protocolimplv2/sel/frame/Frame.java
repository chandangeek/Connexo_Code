package com.elster.us.protocolimplv2.sel.frame;

import com.elster.us.protocolimplv2.sel.frame.data.BasicData;
import com.energyict.protocol.exception.ConnectionCommunicationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.elster.us.protocolimplv2.sel.Consts.CR;
import static com.elster.us.protocolimplv2.sel.utility.ByteArrayHelper.getBytes;

public class Frame {
  
  private BasicData data;
  
  public Frame() {}

  public Frame(BasicData data) {
      this.data = data;
  }

  public BasicData getData() {
      return data;
  }

  public void setData(BasicData data) {
      this.data = data;
  }

  public byte[] toByteArray() {
      try {
          ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
          outputStream.write(data.toByteArray(true));
          outputStream.write(getBytes(CR));
          return outputStream.toByteArray();
      } catch (IOException ioe) {
          // TODO: is this thw correct exception for here?
          throw ConnectionCommunicationException.unexpectedIOException(ioe);
      }
  }
  
  

}
