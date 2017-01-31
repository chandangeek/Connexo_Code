/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RegisterSetFactory.java
 *
 * Created on 25 oktober 2004, 17:22
 */

package com.energyict.protocolimpl.iec1107.sdc;

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
        RegisterSet registerSet = new RegisterSet(data,getDataReadingCommandFactory().getSdc().getTimeZone());
        registersets[registerSet.getBillingPoint()] = registerSet;
    }
    
    public RegisterSet getRegisterSet(int billingPoint) throws java.io.IOException {
        if (registersets[billingPoint] == null)
        	retrieve ("HRR",Integer.toString(billingPoint));

        return registersets[billingPoint];
    }
    
}
