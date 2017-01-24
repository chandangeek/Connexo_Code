/*
 * RegisterStatusRead.java
 *
 * Created on 2 november 2004, 16:36
 */

package com.energyict.protocolimpl.iec1107.sdc;

import com.energyict.protocolimpl.base.DataParser;
/**
 *
 * @author  Koen
 */
public class RegisterStatusRead extends AbstractDataReadingCommand {
    
    boolean retrieved=false;
    long status=0;
    
    /** Creates a new instance of RegisterStatusRead */
    public RegisterStatusRead(DataReadingCommandFactory drcf) {
        super(drcf);
    }
    
    public void parse(byte[] data, java.util.TimeZone timeZone) throws java.io.IOException {
        DataParser dp = new DataParser();
        status = Long.parseLong(dp.parseBetweenBrackets(new String(data)),16);
    }
    
    public boolean isRegisterActive(int regId) throws java.io.IOException {
        if (retrieved==false) {
            retrieve("RSR");
            retrieved=true;
        }
        return (status & (1L << (regId-1))) != 0;
    }
    
}
