/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * BillingPeriodEndCounter.java
 *
 * Created on 2 november 2004, 13:32
 */

package com.energyict.protocolimpl.iec1107.enermete70x;

import com.energyict.protocolimpl.base.DataParser;

import java.io.IOException;

/**
 *
 * @author  Koen
 */
public class BillingPeriodEndCounter extends AbstractDataReadingCommand {
    private static final int NR_OF_REGISTERS=32;
    int[] billingPeriodEndCounter=null;
    int regId;
    /** Creates a new instance of BillingPeriodEndCounter */
    public BillingPeriodEndCounter(DataReadingCommandFactory drcf) {
        super(drcf);
        billingPeriodEndCounter = new int[NR_OF_REGISTERS];
        for (int i = 0; i< NR_OF_REGISTERS; i++) {
            billingPeriodEndCounter[i] = -1;
        }
    }

    public void parse(byte[] data, java.util.TimeZone timeZone) throws java.io.IOException {
        DataParser dp = new DataParser();
        String strExpression = new String(data);
        //for (int regId=0;regId<NR_OF_REGISTERS;regId++)
           billingPeriodEndCounter[regId] =
              Integer.parseInt(dp.parseBetweenBrackets(strExpression));
    }

    public int getBillingPeriodEndCounter(int regId) throws IOException {
        this.regId=regId-1;
        if (billingPeriodEndCounter[regId] == -1) {
            retrieve("BCR",Integer.toString(regId)); //,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32");
        }
        return billingPeriodEndCounter[regId-1];
    }

}
