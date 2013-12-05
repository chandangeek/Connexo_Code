/*
 * AbstractCommandResponse.java
 *
 * Created on 1 december 2006, 15:29
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import java.io.IOException;

/**
 *
 * @author Koen
 */
abstract public class AbstractCommandResponse {





    abstract protected void parse(byte[] data) throws IOException;

    /** Creates a new instance of AbstractCommandResponse */
    public AbstractCommandResponse() {
    }


    public void build(byte[] data) {


    }

    static public int getREAD_RESPONSE() {
        return 0x0c;
    }
    static public int getLOGGED_OFF() {
        return 0x0E;
    }
    static public int getWRITE_RESPONSE() {
        return 0x0D;
    }
    static public int getINITIATE_RESPONSE() {
        return 0x08;
    }
    static public int getCONFIRMED_SERVICE_RESPONSE() {
        return 0x07; // page 28 of the mini dlms protocoldoc. Initiate upload response confirmed service is 0x07 (see trace) instead of 0x00
    }
}
