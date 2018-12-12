/*
 * RegisterSetFactory.java
 *
 * Created on 25 oktober 2004, 17:22
 */

package com.energyict.protocolimpl.iec1107.enermete70x;

/**
 *
 * @author  Koen
 */
public class RegisterSetFactory extends AbstractDataReadingCommand {
    
    static public final int NR_OF_BILLINGPOINTS = 16;
    RegisterSet[] registersets = new RegisterSet[NR_OF_BILLINGPOINTS];
    
    /** Creates a new instance of RegisterSetFactory */
    public RegisterSetFactory(DataReadingCommandFactory drcf) {
        super(drcf);
    }
    
    
    public void parse(byte[] data, java.util.TimeZone timeZone) throws java.io.IOException {
        RegisterSet registerSet = new RegisterSet(data,getDataReadingCommandFactory().getEnermet().getTimeZone());
        registersets[registerSet.getBillingPoint()] = registerSet;
    }
    
    public RegisterSet getRegisterSet(int billingPoint) throws java.io.IOException {
        if (registersets[billingPoint] == null)
            retrieve("GTR",billingPoint+",1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32");
        return registersets[billingPoint];
    }
    
}
