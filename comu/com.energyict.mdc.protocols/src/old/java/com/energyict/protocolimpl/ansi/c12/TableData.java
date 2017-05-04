/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * TableData.java
 *
 * Created on 19 oktober 2005, 16:38
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12;

/**
 *
 * @author Koen
 */
public class TableData {

    /** Creates a new instance of TableData */
    public TableData() {

    }

    public static int calcCheckSum(byte[] tableData) {
        // calculate checksum...
        int check = 0;
        for (int i=0;i<tableData.length;i++) {
            check += ((int)tableData[i]&0xFF);
        }
        check ^= 0xFF;
        check += 1;
        check &= 0xFF;
        return check;
    }

}
