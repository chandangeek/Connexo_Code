/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms;

import java.io.IOException;

public class DataContainerException extends IOException {

  public DataContainerException(String str) {
      super(str);
  } // public DataContainerException(String str)

  public DataContainerException() {
      super();
  } // public HDLCConnectionException()

  public DataContainerException(String str, short sReason) {
      super(str);
  } // public HDLCConnectionException(String str)
}