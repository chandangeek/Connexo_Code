/*
 * ModbusException.java
 *
 * Created on 20 september 2005, 10:01
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.core;

import java.io.IOException;

/**
 *
 * @author Koen
 */


public class ModbusException extends IOException {

  static public final short GENERAL_ERROR=0;
  static public final short PARSE_ERROR=1;
  static public final short PARSE_LENGTH_ERROR=2;
  static public final short WRITE_ERROR=3;

  private short sReason;
  private int functionErrorCode;
  private int exceptionCode;

  public short getReason() {
     return sReason;
  }

  public ModbusException(String str)
  {
      super(str);
      this.sReason = -1;
  } // public ConnectionException(String str)

  public ModbusException()
  {
      super();
      this.sReason = -1;

  } // public ConnectionException()

  public ModbusException(String str, short sReason) {
      this(str,sReason,-1,-1);
  }

  public ModbusException(String str, short sReason, int functionErrorCode, int exceptionCode) {
      super(str);
      this.sReason = sReason;
      this.functionErrorCode=functionErrorCode;
      this.exceptionCode=exceptionCode;

  } // public ConnectionException(String str)

    public int getFunctionErrorCode() {
        return functionErrorCode;
    }

    public void setFunctionErrorCode(int functionErrorCode) {
        this.functionErrorCode = functionErrorCode;
    }

    public int getExceptionCode() {
        return exceptionCode;
    }

    public void setExceptionCode(int exceptionCode) {
        this.exceptionCode = exceptionCode;
    }
}
