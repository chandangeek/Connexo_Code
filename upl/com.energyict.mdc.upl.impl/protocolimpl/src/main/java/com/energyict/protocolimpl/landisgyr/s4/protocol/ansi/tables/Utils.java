/*
 * Utils.java
 *
 * Created on 5 juli 2006, 14:12
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4.protocol.ansi.tables;

import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 * @author Koen
 */
public class Utils {
    
    /** Creates a new instance of Utils */
    public Utils() {
    }
    
    
    static BigDecimal getS4FloatingPoint(byte[] data,int offset) throws IOException {
        byte temp = (byte)(data[offset+2] & 0x0F);
        int digit5= ProtocolUtils.BCD2hex(temp)*10000;
        int digit4to1=(int)ParseUtils.getBCD2LongLE(data, 0, 2); 
        BigDecimal bd = BigDecimal.valueOf(digit5+digit4to1,4);
        temp = (byte)((data[offset+2] >> 4)&0x0F);
        if ((temp&0x8) == 0x8) temp |= 0xF0;
        bd = bd.movePointRight(temp);
        return bd;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            int offset=0;
            byte[] data = new byte[]{0x55,0x46,(byte)0xF6};            
            System.out.println(getS4FloatingPoint(data,offset));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        // TODO code application logic here
    }
    
}
