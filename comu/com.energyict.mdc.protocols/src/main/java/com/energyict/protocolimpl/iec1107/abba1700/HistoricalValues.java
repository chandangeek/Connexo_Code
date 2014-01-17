/*
 * HistoricalValues.java
 *
 * Created on 10 juni 2004, 15:54
 */

package com.energyict.protocolimpl.iec1107.abba1700;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
/**
 *
 * @author  Koen
 */
public class HistoricalValues {

    private static final int BILLING_SET_INFO_LENGTH = 15;

    //int billingSetLength; // = 10*8+meterType.getNrOfTariffRegisters()*8+4*8+8*9+24*12+15;
    byte[] identity507; // = new byte[12][10*8]; // cumulative
    byte[] identity508; // = new byte[12][meterType.getNrOfTariffRegisters()*8]; // cumulative tariff
    byte[] identity516; // = new byte[12][4*8];  // multi utility
    byte[] identity509; // = new byte[12][8*9];  // cumulative max demand
    byte[] identity510; // = new byte[12][24*12]; // maximum demand
    byte[] identity511; // Coincident maximum demand
    HistoricalValueSetInfo historicalValueSetInfo;

    Map map = new HashMap();
    TimeZone timeZone;
    ABBA1700MeterType meterType;

    /** Creates a new instance of HistoricalValues */
    public HistoricalValues(byte[] data, TimeZone timeZone, ABBA1700MeterType meterType) throws IOException {
        this.meterType=meterType;
        this.timeZone=timeZone;

        //billingSetLength = 10*8+meterType.getNrOfTariffRegisters()*8+4*8+8*9+24*12+15;
        identity507 = new byte[10*8]; // cumulative
        identity508 = new byte[meterType.getNrOfTariffRegisters()*8]; // cumulative tariff
        identity516 = new byte[4*8];  // multi utility
        identity509 = new byte[8*9];  // cumulative max demand
        identity510 = new byte[24*12]; // maximum demand
        identity511 = new byte[5*24];   // coincident maximum demand
        parse(data);
    }

    static public boolean has(String dataId) {
       return (("507".compareTo(dataId) == 0) ||
               ("508".compareTo(dataId) == 0) ||
               ("516".compareTo(dataId) == 0) ||
               ("509".compareTo(dataId) == 0) ||
               ("510".compareTo(dataId) == 0) ||
                ("511".compareTo(dataId) == 0));
    }

    private void parse(byte[] data) throws IOException {
       identity507 = ProtocolUtils.getSubArray2(data,0,identity507.length);
       identity508 = ProtocolUtils.getSubArray2(data,identity507.length,identity508.length);
       identity516 = ProtocolUtils.getSubArray2(data,identity507.length+identity508.length,identity516.length);
       identity509 = ProtocolUtils.getSubArray2(data,identity507.length+identity508.length+identity516.length,identity509.length);
       identity510 = ProtocolUtils.getSubArray2(data,identity507.length+identity508.length+identity516.length+identity509.length,identity510.length);
       int offset = identity507.length+identity508.length+identity516.length+identity509.length+identity510.length+(meterType.hasExtendedCustomerRegisters()?identity511.length:0);
       historicalValueSetInfo = new HistoricalValueSetInfo(ProtocolUtils.getSubArray2(data,offset,BILLING_SET_INFO_LENGTH),timeZone);
       map.put("507", identity507);
       map.put("508", identity508);
       map.put("516", identity516);
       map.put("509", identity509);
       map.put("510", identity510);

    }

    protected byte[] getData(String dataId) {
       byte[] dataRaw = (byte[])map.get(dataId);
       return dataRaw;
    }

    /**
     * Getter for property historicalValueSetInfos.
     * @return Value of property historicalValueSetInfos.
     */
    public HistoricalValueSetInfo getHistoricalValueSetInfo() {
        return this.historicalValueSetInfo;
    }

    public ABBA1700MeterType getMeterType() {
        return meterType;
    }
}
