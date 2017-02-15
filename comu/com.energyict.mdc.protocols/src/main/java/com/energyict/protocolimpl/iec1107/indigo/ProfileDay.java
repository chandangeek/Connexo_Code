/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ProfileDay.java
 *
 * Created on 12 juli 2004, 13:26
 */

package com.energyict.protocolimpl.iec1107.indigo;

import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author  Koen
 * Changes:
 * KV 01122004 ignore negative values...
 */
public class ProfileDay {

    private static final int DEBUG=0;

    private static final int STATUS_CHANNEL=11;

    int addressCode;
    int channelId;
    Date date;
    int dailyFlags; // XPMCBNNN, B=battery maintenance flag, C=clock failure flag, M=MD reset flag, P=24 hour power outage, NNN=number of successfull level 2 accesses (max7)
    Quantity totalRegister;
    List values=null; // of type Quantity
    boolean statusChannel;
    Unit channelUnit;
    int intervalsPerDay;

    /** Creates a new instance of ProfileDay */
    public ProfileDay(byte[] data, LogicalAddressFactory laf) throws IOException {

if (DEBUG>=1) {
    //ProtocolUtils.printResponseDataFormatted(ba);
    System.out.println("Received data:");
    System.out.println(new String(data));
}

data = removeDuplicateAddress(data);

if (DEBUG>=1) {
    //ProtocolUtils.printResponseDataFormatted(ba);
    System.out.println("Received data after removeduplicate:");
    System.out.println(new String(data));
}

        parse(data, laf);
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ProfileDay:\n");
        strBuff.append("addressCode="+getAddressCode()+", channelId="+getChannelId()+", date="+getDate().toString()+", totalRegister="+getTotalRegister()+"\n");
        strBuff.append("Values=\n");
        Iterator it = getValues().iterator();
        while(it.hasNext()) {
            Quantity quantity = (Quantity)it.next();
            strBuff.append(quantity.toString()+" ");
        }
        return strBuff.toString();
    }

    private byte[] removeDuplicateAddress(byte[] ba) {
        String address = new String(ProtocolUtils.getSubArray2(ba,0,4));
        String baStr = new String(ba);
        baStr = baStr.replaceAll("\\)"+address+"\\(", "");
        return baStr.getBytes();
    }

    private void parse(byte[] ba, LogicalAddressFactory laf) throws IOException {


        byte[] address=ProtocolUtils.convert2ascii(ProtocolUtils.getSubArray2(ba, 0, 4));
        setAddressCode(ProtocolUtils.getInt(address,0,2)>>4);
        setChannelId(ProtocolUtils.getInt(address,1,1)&0x0F);
        setStatusChannel(getChannelId() == STATUS_CHANNEL);
        if (getAddressCode() != 0xFFF) {
            byte[] data=ProtocolUtils.convert2ascii(ProtocolUtils.getSubArray2(ba, 5, ba.length-6));
            int val=ProtocolUtils.getInt(data,0,2);
            Calendar calendar = ProtocolUtils.getCleanCalendar(laf.getProtocolLink().getTimeZone());
            calendar.set(Calendar.YEAR,(val>>9)+2000);
            calendar.set(Calendar.MONTH,((val>>5)&0x000F)-1);
            calendar.set(Calendar.DATE,val&0x001F);
            setDate(calendar.getTime());

if (DEBUG>=1) System.out.println("0x"+Integer.toHexString(val)+" = "+getDate());

            setDailyFlags(ProtocolUtils.getInt(data,2,1));
            values = new ArrayList();
            setIntervalsPerDay((3600*24)/laf.getProtocolLink().getProfileInterval());
            int intervalCount=0;
            setChannelUnit(laf.getMeteringDefinition().getChannelUnit(getChannelId()));

            if (isStatusChannel()) {
                for (intervalCount=0;intervalCount<getIntervalsPerDay();intervalCount++) {
                   // Meter Bugfix 18/05/2005 Meter does not respond with status flag data...
                   if (data.length > (3+intervalCount)) {
                       values.add(new Quantity(new BigDecimal(ProtocolUtils.getInt(data, 3 + intervalCount, 1)), Unit.get("")));
                   } else {
                       values.add(new Quantity(BigDecimal.ZERO, Unit.get("")));
                   }
                }
            } // if (isStatusChannel())
            else {
                setTotalRegister(new Quantity(BigDecimal.valueOf(ProtocolUtils.getLong(data,3,4)),getChannelUnit().getVolumeUnit()));
                int offset=3+4;
                try {
                    int value,previousValue=0;
                    for (intervalCount=0;intervalCount<getIntervalsPerDay();intervalCount++) {
                        if (isCompressed(data[offset])) {
                            // compressed value
                            value = ProtocolUtils.getInt(data,offset,1);
                            value &= 0x3F;
                            if (isNegative(data[offset]))
                                value *= -1;
                            offset++;
                        }
                        else {
                            value = ProtocolUtils.getInt(data,offset,2);
                            value &= 0x3FFF;
                            if (isNegative(data[offset]))
                                value *= -1;
                            offset+=2;
                        }

                        if (DEBUG >= 1)
                            System.out.print("KV_DEBUG> previousValue="+previousValue+" + value="+value+" = ");

                        previousValue+=value;

                        if (DEBUG >= 1)
                            System.out.print(previousValue+", ");

                        // KV 01122004
                        if (previousValue < 0) previousValue=0;

                        // calculation
                        double dVal=previousValue*10; // cause each increment represents 10 pulses

                        if (DEBUG >= 1)
                            System.out.print("*10="+dVal+", ");


                        if (laf.getCTVT().isCTMeter()) {
                           dVal*=0.1;
                        }
                        else if (laf.getCTVT().isCTVTMeter()) {
                           dVal*=0.05;
                        }
                        else if (laf.getCTVT().isWholeCurrentMeter()) {
                           dVal*=1; // ??? KV TO DO 1?
                        }

                        if (DEBUG >= 1)
                            System.out.print("after ctvt="+dVal+", ");


                        dVal*=laf.getCTVT().getMultiplier();

                        if (DEBUG >= 1)
                            System.out.print("*multiplier="+dVal);

                        if (DEBUG >= 1)
                            System.out.println();

                        values.add(new Quantity(new BigDecimal(dVal),getChannelUnit().getFlowUnit()));
                    }
                }
                catch(ArrayIndexOutOfBoundsException e) {
                    throw new IOException("ProfileDay, parse, no more data to parse, probably wrong profileInterval, intervalCount="+intervalCount+", offset="+offset+", intervalsPerDay="+intervalsPerDay);
                }
            } //if (!isStatusChannel())
        }
        else {
            if (DEBUG >= 1)
                System.out.println("KV_DEBUG> No profile data available!");
        }

    } // parse()

    private boolean isCompressed(byte data) {
        int val = (int)data&0xFF;
        if ((val & 0x80) == 0x80)
            return true;
        else
            return false;
    }

    private boolean isNegative(byte data) {
        int val = (int)data&0xFF;
        if ((val & 0x40) == 0x40)
            return true;
        else
            return false;
    }

    /**
     * Getter for property date.
     * @return Value of property date.
     */
    public java.util.Date getDate() {
        return date;
    }

    /**
     * Setter for property date.
     * @param date New value of property date.
     */
    public void setDate(java.util.Date date) {
        this.date = date;
    }

    /**
     * Getter for property dailyFlags.
     * @return Value of property dailyFlags.
     */
    public int getDailyFlags() {
        return dailyFlags;
    }

    /**
     * Setter for property dailyFlags.
     * @param dailyFlags New value of property dailyFlags.
     */
    public void setDailyFlags(int dailyFlags) {
        this.dailyFlags = dailyFlags;
    }

    /**
     * Getter for property totalRegister.
     * @return Value of property totalRegister.
     */
    public Quantity getTotalRegister() {
        return totalRegister;
    }

    /**
     * Setter for property totalRegister.
     * @param totalRegister New value of property totalRegister.
     */
    public void setTotalRegister(Quantity totalRegister) {
        this.totalRegister = totalRegister;
    }

    /**
     * Getter for property values.
     * @return Value of property values.
     */
    public java.util.List getValues() {
        return values;
    }

    public int getIntValue(int interval) {
        Quantity quantity = (Quantity)getValues().get(interval);
        return quantity.getAmount().intValue();
    }

    public Quantity getQuantityValue(int interval) {
        Quantity quantity = (Quantity)getValues().get(interval);
        return quantity;
    }

    public BigDecimal getBigDecimalValue(int interval) {
        Quantity quantity = (Quantity)getValues().get(interval);
        return quantity.getAmount();
    }

    /**
     * Setter for property values.
     * @param values New value of property values.
     */
    public void setValues(java.util.List values) {
        this.values = values;
    }

    /**
     * Getter for property statusChannel.
     * @return Value of property statusChannel.
     */
    public boolean isStatusChannel() {
        return statusChannel;
    }

    /**
     * Setter for property statusChannel.
     * @param statusChannel New value of property statusChannel.
     */
    public void setStatusChannel(boolean statusChannel) {
        this.statusChannel = statusChannel;
    }

    /**
     * Getter for property addressCode.
     * @return Value of property addressCode.
     */
    public int getAddressCode() {
        return addressCode;
    }

    /**
     * Setter for property addressCode.
     * @param addressCode New value of property addressCode.
     */
    public void setAddressCode(int addressCode) {
        this.addressCode = addressCode;
    }

    /**
     * Getter for property channelId.
     * @return Value of property channelId.
     */
    public int getChannelId() {
        return channelId;
    }

    /**
     * Setter for property channelId.
     * @param channelId New value of property channelId.
     */
    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    /**
     * Getter for property channelUnit.
     * @return Value of property channelUnit.
     */
    public Unit getChannelUnit() {
        return channelUnit;
    }

    /**
     * Setter for property channelUnit.
     * @param channelUnit New value of property channelUnit.
     */
    public void setChannelUnit(Unit channelUnit) {
        this.channelUnit = channelUnit;
    }

    /**
     * Getter for property intervalsPerDay.
     * @return Value of property intervalsPerDay.
     */
    public int getIntervalsPerDay() {
        return intervalsPerDay;
    }

    /**
     * Setter for property intervalsPerDay.
     * @param intervalsPerDay New value of property intervalsPerDay.
     */
    public void setIntervalsPerDay(int intervalsPerDay) {
        this.intervalsPerDay = intervalsPerDay;
    }

}
