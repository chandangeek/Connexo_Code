package com.energyict.dlms;

import java.io.IOException;

/**
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: EnergyICT</p>
 * @author Koenraad Vanderschaeve
 * @version 1.0
 */

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