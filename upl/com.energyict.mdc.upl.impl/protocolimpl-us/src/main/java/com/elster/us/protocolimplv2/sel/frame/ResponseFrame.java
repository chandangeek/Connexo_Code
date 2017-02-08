package com.elster.us.protocolimplv2.sel.frame;

import com.elster.us.protocolimplv2.sel.frame.data.BasicResponseData;
import com.elster.us.protocolimplv2.sel.frame.data.DeviceIDReadResponseData;
import com.elster.us.protocolimplv2.sel.frame.data.EventResponseData;
import com.elster.us.protocolimplv2.sel.frame.data.RegisterReadResponseData;
import com.elster.us.protocolimplv2.sel.frame.data.SingleReadResponseData;
import com.elster.us.protocolimplv2.sel.frame.data.TimeReadResponseData;
import com.energyict.protocol.exception.ConnectionCommunicationException;

import java.io.IOException;

import static com.elster.us.protocolimplv2.sel.Consts.COMMAND_DATE;
import static com.elster.us.protocolimplv2.sel.Consts.COMMAND_EM;
import static com.elster.us.protocolimplv2.sel.Consts.COMMAND_ID;
import static com.elster.us.protocolimplv2.sel.Consts.COMMAND_LP;
import static com.elster.us.protocolimplv2.sel.Consts.COMMAND_RD;
import static com.elster.us.protocolimplv2.sel.Consts.COMMAND_REG;
import static com.elster.us.protocolimplv2.sel.Consts.COMMAND_SF;
import static com.elster.us.protocolimplv2.sel.Consts.COMMAND_SN;
import static com.elster.us.protocolimplv2.sel.Consts.COMMAND_TIME;

//import com.elster.us.protocolimplv2.sel.frame.data.EventResponseDataRE;

public class ResponseFrame {
  private BasicResponseData data;

  public void setData(BasicResponseData data) {
      this.data = data;
  }

  public BasicResponseData getData() {
      return data;
  }

  public ResponseFrame(byte[] bytes, String lastCommandSent) {
      parseBytes(bytes, lastCommandSent);
  }

  protected void parseBytes(byte[] bytes, String lastCommandSent) {
      // The "|| bytes.length == 2" covers the case where we sent a command
      // expecting a more detailed response, but we get back a basic response
      // containing an error...
      try {
          if (COMMAND_SN.equals(lastCommandSent) || COMMAND_SF.equals(lastCommandSent)
                  || COMMAND_LP.equals(lastCommandSent) || bytes.length == 2) {
              this.setData(new BasicResponseData(bytes));
          } else if (COMMAND_RD.equals(lastCommandSent) || COMMAND_DATE.equals(lastCommandSent)) {
              this.setData(new SingleReadResponseData(bytes));
          } else if (COMMAND_TIME.equals(lastCommandSent)) {
            this.setData(new TimeReadResponseData(bytes));
          } else if (COMMAND_ID.equals(lastCommandSent)) {
            this.setData(new DeviceIDReadResponseData(bytes));
          } else if (COMMAND_REG.equals(lastCommandSent)) {
              this.setData(new RegisterReadResponseData(bytes));
          } else if (COMMAND_EM.equals(lastCommandSent)) {
              this.setData(new EventResponseData(bytes));
          //} else if (COMMAND_RE.equals(lastCommandSent)) {
          //    this.setData(new EventResponseDataRE(bytes));
          } else {
              // TODO: handle the case where we cannot determine what type of data to construct
          }
      } catch (IOException ioe) {
          // TODO: which exception?
          throw ConnectionCommunicationException.unExpectedProtocolError(ioe);
      }

  }

  public boolean isOK() {
      return getData().isOK();
  }

  public String getError() {
      return getData().getError();
  }

  public byte[] generateCRC() {
      return getData().generateCRC();
  }

}
