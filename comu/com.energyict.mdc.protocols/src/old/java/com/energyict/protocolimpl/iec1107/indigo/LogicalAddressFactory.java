/*
 * LogicalAddressFactory.java
 *
 * Created on 6 juli 2004, 19:07
 */

package com.energyict.protocolimpl.iec1107.indigo;

import com.energyict.mdc.protocol.api.MeterExceptionInfo;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.io.IOException;
import java.util.Date;
/**
 *
 * @author  Koen
 */
public class LogicalAddressFactory {

    private ProtocolLink protocolLink=null;
    private MeterExceptionInfo meterExceptionInfo=null;

    // cached registers
    MeterIdentity meterIdentity=null;
    MeterStatus meterstatus=null;
    ClockDefinition clockDefinition=null;
    DefaultStatus defaultStatus=null;
    CustomerNotes customerNotes=null;
    MeteringDefinition meteringDefinition=null;
    CTVT ctvt=null;
    HistoricalData[] historicalData = new HistoricalData[NR_OF_REGISTER_SETS];
    BillingPeriodDefinition billingPeriodDefinition=null;
    GeneralMeterData generalMeterData=null;

    /** Creates a new instance of LogicalAddressFactory */
    public LogicalAddressFactory(ProtocolLink protocolLink,MeterExceptionInfo meterExceptionInfo) {
        this.protocolLink=protocolLink;
        this.meterExceptionInfo=meterExceptionInfo;
    }

    static public final int NR_OF_REGISTER_SETS=17; // 0=current, 1..16=historical

    public MeterIdentity getMeterIdentity() throws IOException {
        if (meterIdentity==null) {
            meterIdentity = new MeterIdentity(0xC100,30,this); // 30
            meterIdentity.retrieve();
        }
        return meterIdentity;
    }
    public MeterStatus getMeterStatus() throws IOException {
        if (meterstatus==null) {
            meterstatus = new MeterStatus(0xC200,7,this);
            meterstatus.retrieve();
        }
        return meterstatus;
    }
    public ClockDefinition getClockDefinition() throws IOException {
        if (clockDefinition==null) {
            clockDefinition = new ClockDefinition(0xDD00,44,this);
            clockDefinition.retrieve();
        }
        return clockDefinition;
    }
    public DefaultStatus getDefaultStatus() throws IOException {
        if (defaultStatus==null) {
            defaultStatus = new DefaultStatus(0xC400,50,this);
            defaultStatus.retrieve();
        }
        return defaultStatus;
    }
    public CustomerNotes getCustomerNotes() throws IOException {
        if (customerNotes==null) {
            customerNotes = new CustomerNotes(0xC500,10,this);
            customerNotes.retrieve();
        }
        return customerNotes;
    }

    public void setDateTimeGMT(Date date) throws IOException {
        DateTimeGMT ald = new DateTimeGMT(0xC700,12,this,date);
        ald.write();
    }

    public DateTimeGMT getDateTimeGMT() throws IOException {
        DateTimeGMT ald = new DateTimeGMT(0xC700,12,this);
        ald.retrieve();
        return ald;
    }
    public DateTimeLocal getDateTimeLocal() throws IOException {
        DateTimeLocal ald = new DateTimeLocal(0xC701,12,this);
        ald.retrieve();
        return ald;
    }
    public MeteringDefinition getMeteringDefinition() throws IOException {
        if (meteringDefinition==null) {
            meteringDefinition = new MeteringDefinition(0xD000,15,this);
            meteringDefinition.retrieve();
        }
        return meteringDefinition;
    }

    public BillingPeriodDefinition getBillingPeriodDefinition() throws IOException {
        if (billingPeriodDefinition==null) {
            billingPeriodDefinition = new BillingPeriodDefinition(0xD800,60,this);
            billingPeriodDefinition.retrieve();
        }
        return billingPeriodDefinition;
    }

    public GeneralMeterData getGeneralMeterData() throws IOException {
        if (generalMeterData==null) {
            generalMeterData = new GeneralMeterData(0xC900,38,this);
            generalMeterData.retrieve();
        }
        return generalMeterData;
    }

    public CTVT getCTVT() throws IOException {
//        throw new IOException("LogicalAddressFactory, getCTVT() is not implemented...");
        if (ctvt==null) {
            ctvt = new CTVT(0xD300,23,this);
            ctvt.retrieve();
        }
        return ctvt;
    }

    public TotalRegisters getTotalRegisters() throws IOException {
        return getTotalRegisters(0);
    }
    public TotalRegisters getTotalRegisters(int set) throws IOException {
        if (set >= NR_OF_REGISTER_SETS)
            throw new IOException("LogicalAddressFactory, getTotalRegisters, wrong register set "+set);
        TotalRegisters ald = new TotalRegisters(0xCA00+set,36,this);
        ald.retrieve();
        return ald;
    }
    public RateRegisters getRateRegisters() throws IOException {
        return getRateRegisters(0);
    }
    public RateRegisters getRateRegisters(int set) throws IOException {
        if (set >= NR_OF_REGISTER_SETS)
            throw new IOException("LogicalAddressFactory, getRateRegisters, wrong register set "+set);
        RateRegisters ald = new RateRegisters(0xCB00+set,64,this);
        ald.retrieve();
        return ald;
    }
    public DemandRegisters getDemandRegisters() throws IOException {
        return getDemandRegisters(0);
    }
    public DemandRegisters getDemandRegisters(int set) throws IOException {
        if (set >= NR_OF_REGISTER_SETS)
            throw new IOException("LogicalAddressFactory, getDemandRegisters, wrong register set "+set);
        DemandRegisters ald = new DemandRegisters(0xCC00+set,64,this);
        ald.retrieve();
        return ald;
    }
    public DefaultRegisters getDefaultRegisters() throws IOException {
        return getDefaultRegisters(0);
    }
    public DefaultRegisters getDefaultRegisters(int set) throws IOException {
        if (set >= NR_OF_REGISTER_SETS)
            throw new IOException("LogicalAddressFactory, getDefaultRegisters, wrong register set "+set);
        DefaultRegisters ald = new DefaultRegisters(0xCD00+set,12,this);
        ald.retrieve();
        return ald;
    }
    public HistoricalData getHistoricalData() throws IOException {
        return getHistoricalData(0);
    }
    public HistoricalData getHistoricalData(int set) throws IOException {
        if (historicalData[set] == null) {
            if (set >= NR_OF_REGISTER_SETS)
                throw new IOException("LogicalAddressFactory, getHistoricalData, wrong register set "+set);
            historicalData[set] = new HistoricalData(0xCE00+set,33,this);
            historicalData[set].retrieve();
        }
        return historicalData[set];
    }

    /**
     * Getter for property protocolLink.
     * @return Value of property protocolLink.
     */
    public com.energyict.protocolimpl.iec1107.ProtocolLink getProtocolLink() {
        return protocolLink;
    }

    /**
     * Getter for property meterExceptionInfo.
     * @return Value of property meterExceptionInfo.
     */
    public MeterExceptionInfo getMeterExceptionInfo() {
        return meterExceptionInfo;
    }

 }