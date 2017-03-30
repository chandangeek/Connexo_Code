/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * EndDeviceStdStatus2Bitfield.java
 *
 * Created on 23 februari 2006, 15:34
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class EndDeviceStdStatus2Bitfield {

    /** Creates a new instance of EndDeviceStdStatus2Bitfield */
    public EndDeviceStdStatus2Bitfield(byte[] data,int offset,TableFactory tableFactory) throws IOException {
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("EndDeviceStdStatus2Bitfield: reserved\n");
        return strBuff.toString();
    }


    static public int getSize(TableFactory tableFactory) throws IOException {
        return 1;
    }

}
