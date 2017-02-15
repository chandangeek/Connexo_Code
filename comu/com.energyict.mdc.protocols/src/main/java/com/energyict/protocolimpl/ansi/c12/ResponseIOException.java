/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ResponseProtocolException.java
 *
 * Created on 17 oktober 2005, 17:06
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ResponseIOException extends IOException {

  private int reason;

  public int getReason() {
     return reason;
  }

  public ResponseIOException(String str)
  {
      super(str);
      this.reason = -1;
  } // public ResponseProtocolException(String str)

  public ResponseIOException()
  {
      super();
      this.reason = -1;

  } // public ResponseProtocolException()

  public ResponseIOException(String str, int reason)
  {
      super(str);
      this.reason = reason;

  } // public ResponseProtocolException(String str)

}