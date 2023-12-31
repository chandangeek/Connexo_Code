/*
 * MultiplierFactory.java
 *
 * Created on 19 oktober 2007, 10:04
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.socomec.a40;

import java.io.*;
import java.math.*;

/**
 *
 * @author kvds
 */
public class MultiplierFactory {

    private BigDecimal ct;
    private BigDecimal voltageShifter; // we use reg 257 (productcode) to determine A20 or A40
                                       // however, the doc of the A20 does not support the register altough we read a stable value... 141 for a A20 meter

    A40 a40;
    
    static public final int CT=0;
    static public final int VSHIFT=1;
    
    /** Creates a new instance of MultiplierFactory */
    public MultiplierFactory(A40 a40) {
        this.a40=a40;
        ct=null;
    }
   
    public BigDecimal getMultiplier(int address) throws IOException {
        if (address == CT)
            return getCt();
        else if (address == VSHIFT)
            return getVoltageShifter();
        else
            return BigDecimal.valueOf(1);
    }
    
    private BigDecimal getCt() throws IOException {
        
        if (ct==null) {
            BigDecimal ctPrim = a40.getRegisterFactory().findRegister("ctPrim").quantityValue().getAmount();
            BigDecimal ctSec = a40.getRegisterFactory().findRegister("ctSec").quantityValue().getAmount();
            ct = ctPrim.divide(ctSec,BigDecimal.ROUND_HALF_UP);
        }
        return ct;
    }
    private BigDecimal getVoltageShifter() throws IOException {
        if (voltageShifter==null) {
            if (a40.getSocomecType() == null) {
                int productCode = a40.getRegisterFactory().findRegister("productcode").quantityValue().getAmount().intValue();
                if (productCode==141)
                    voltageShifter = BigDecimal.valueOf(2);
                else
                    voltageShifter = BigDecimal.valueOf(1);
            }
            else if (a40.getSocomecType().compareTo("A20") == 0) {
                voltageShifter = BigDecimal.valueOf(2);
            }
            else if (a40.getSocomecType().compareTo("A40") == 0) {
                voltageShifter = BigDecimal.valueOf(1);
            }
        }
        return voltageShifter;
    }
}
