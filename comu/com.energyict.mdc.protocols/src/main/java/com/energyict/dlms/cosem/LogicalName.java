/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * LogicalName.java
 *
 * Created on 18 augustus 2004, 17:24
 */

package com.energyict.dlms.cosem;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.OctetString;

/**
 *
 * @author  Koen
 */
public class LogicalName {
    OctetString octetString;
    /** Creates a new instance of LogicalName */
    public LogicalName(OctetString octetString) {
        this.octetString=octetString;
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        for (int i=0;i<octetString.getArray().length;i++) {
            if (i!=0) {
				strBuff.append(".");
			}
            strBuff.append(Integer.toString(octetString.getArray()[i] & 0xFF));
        }
        return strBuff.toString();
    }

    public ObisCode getObisCode() {
        String codeString = ((int)getA()&0xFF)+"."+
                            ((int)getB()&0xFF)+"."+
                            ((int)getC()&0xFF)+"."+
                            ((int)getD()&0xFF)+"."+
                            ((int)getE()&0xFF)+"."+
                            ((int)getF()&0xFF)+".";
       return ObisCode.fromString(codeString);
    }

    public int getA() {
        return octetString.getArray()[0] & 0xFF;
    }
    public int getB() {
        return octetString.getArray()[1] & 0xFF;
    }
    public int getC() {
        return octetString.getArray()[2] & 0xFF;
    }
    public int getD() {
        return octetString.getArray()[3] & 0xFF;
    }
    public int getE() {
        return octetString.getArray()[4] & 0xFF;
    }
    public int getF() {
        return octetString.getArray()[5] & 0xFF;
    }


}
