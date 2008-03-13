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

    static public int calcCheckSum(byte[] tableData) {
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
    
    static public void main(String[] args) {
        
        byte[] data = {(byte)0x00,(byte)0x0a,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x0e,(byte)0x05,(byte)0x13,(byte)0x00};
        int checksum = TableData.calcCheckSum(data);
        System.out.println(Integer.toHexString(checksum));
        
        byte[] data2 = {(byte)0x00,(byte)0x0a,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x11,(byte)0x89,(byte)0x2a,(byte)0x00};
        int checksum2 = TableData.calcCheckSum(data2);
        System.out.println(Integer.toHexString(checksum2));
        
        
    }
    
}
