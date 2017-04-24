/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * LogicalAddressFactory.java
 *
 * Created on 6 juli 2004, 19:07
 */

package com.energyict.protocolimpl.gmc.u1600;

import com.energyict.mdc.protocol.api.MeterExceptionInfo;

import java.io.IOException;
/**
 *
 * @author  Koen
 */
public class LogicalAddressFactory {

    private U1600 u1600 =null;
    private MeterExceptionInfo meterExceptionInfo=null;

    // cached registers
   /*MeterIdentity meterIdentity=null;
    MeterStatus meterstatus=null;
    ClockDefinition clockDefinition=null;
    DefaultStatus defaultStatus=null;
    CustomerNotes customerNotes=null;
    MeteringDefinition meteringDefinition=null;
    CTVT ctvt=null;
    HistoricalData[] historicalData = new HistoricalData[NR_OF_REGISTER_SETS];
    BillingPeriodDefinition billingPeriodDefinition=null;
    GeneralMeterData generalMeterData=null;*/

    /** Creates a new instance of LogicalAddressFactory */
    public LogicalAddressFactory(U1600 u1600,MeterExceptionInfo meterExceptionInfo) {
        this.u1600=u1600;
        this.meterExceptionInfo=meterExceptionInfo;
    }


    //static public final int NR_OF_REGISTER_SETS=17; // 0=current, 1..16=historical
    public static final int NR_OF_REGISTER_SETS=65; // 0=current, 1..16=historical  KV 18052006 changed

    public RateRegisters getRateRegisters() throws IOException {
        return getRateRegisters(0);
    }

     public RateRegisters getRateRegisters(int set) throws IOException {
        if (set >= NR_OF_REGISTER_SETS) {
            throw new IOException("LogicalAddressFactory, getRateRegisters, wrong register set " + set);
        }
        return new RateRegisters(0xCB00+set,64, this);
    }

    public TotalRegisters getTotalRegisters() throws IOException {
     return getTotalRegisters(0);
    }
    public TotalRegisters getTotalRegisters(int set) throws IOException {
        if (set >= NR_OF_REGISTER_SETS) {
            throw new IOException("LogicalAddressFactory, getTotalRegisters, wrong register set " + set);
        }
        TotalRegisters ald = new TotalRegisters(0+set,36,this);
        ald.retrieve();
        return ald;
    }

    /**
     * Getter for property IndigoPlus.
     * @return Value of property IndigoPlus.
     */
    public U1600 getU1600() {
        return u1600;
    }

    /**
     * Getter for property meterExceptionInfo.
     * @return Value of property meterExceptionInfo.
     */
    public MeterExceptionInfo getMeterExceptionInfo() {
        return meterExceptionInfo;
    }

 }