/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * SEVCRegister.java
 *
 * Created on 16 januari 2003, 16:28
 */

package com.energyict.protocolimpl.actarissevc;

import com.energyict.mdc.common.Unit;

import java.io.IOException;

/**
 *
 * @author  Koen
 */
public class SEVCRegister extends SEVCDataParse {

    protected String frame;
    protected int offset;
    protected int length;
    protected int format;
    protected int code;
    protected Unit unit;

    Number value=null;

    private SEVCRegisterFactory sevcRegisterFactory = null;

    protected void setSEVCRegisterFactory(SEVCRegisterFactory sevcRegisterFactory) {
        this.sevcRegisterFactory = sevcRegisterFactory;
    }

    protected SEVCRegisterFactory getSEVCRegisterFactory() {
        return sevcRegisterFactory;
    }

        /*
         * offset = offset from start of APD in nibbles
         * length = length of data in nibbles
         **/
    protected SEVCRegister(String frame, int code, int offset, int length, int format, Unit unit) {
        this.frame = frame;
        this.offset = offset;
        this.format = format;
        this.length = length;
        this.code = code;
        this.unit = unit;
        this.value=null;
    }

    protected int getOffset() {
      return offset;
    }
    protected int getLength() {
      return length;
    }
    protected int getFormat() {
      return format;
    }
    protected Unit getUnit() {
        return unit;
    }


    protected Number getValue(SEVCIEC1107Connection sevciec1107Connection) throws IOException {
        return doGetMeterRegisterValue(this,sevciec1107Connection);
    }

    protected Number doGetMeterRegisterValue(SEVCRegister register,SEVCIEC1107Connection sevciec1107Connection) throws IOException {
       if (value == null) {
           try {
              sevciec1107Connection.sendReadFrame((byte)register.code);
              value = getValue(sevciec1107Connection.receiveData());
           }
           catch(SEVCIEC1107ConnectionException e) {
              throw new IOException("getMeterReading() error, "+e.getMessage());
           }
       } // if (value == null)
       return value;

    } // protected Number doGetMeterRegisterValue(...)

} // public class SEVCRegister
