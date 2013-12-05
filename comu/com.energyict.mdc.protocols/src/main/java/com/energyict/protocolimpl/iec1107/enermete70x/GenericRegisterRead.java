/*
 * GenericRegisterRead.java
 *
 * Created on 8 november 2004, 12:00
 */

package com.energyict.protocolimpl.iec1107.enermete70x;

import com.energyict.protocolimpl.base.DataParser;

/**
 *
 * @author  Koen
 */
public class GenericRegisterRead extends AbstractDataReadingCommand {
    String regVal;
    /** Creates a new instance of GenericRegisterRead */
    public GenericRegisterRead(DataReadingCommandFactory drcf) {
        super(drcf);
    }

    public void parse(byte[] data, java.util.TimeZone timeZone) throws java.io.IOException {
        DataParser dp = new DataParser();
        regVal = dp.parseBetweenBrackets(new String(data));
    }

    public String getRegister(String reg) throws java.io.IOException {
        retrieve(reg);
        return regVal;
    }

}
