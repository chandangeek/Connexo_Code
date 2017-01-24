/*
 * MonthInfoTable.java
 *
 * Created on 22 juni 2006, 13:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarancje.core;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class MonthInfoTable extends AbstractTable {

    private Date timestamp;
    private Calendar timestampCalendar;
    private int tarif;
    private int month;

    private final int PEAK=0;
    private final int NORMAL=1;
    private final int LOW=2;

    private long[] activeEnergy = new long[3]; // kWh
    private long[] reactiveEnergy = new long[3]; // kvarh
    private int[] nrOf10inuteIntervals = new int[3];
    private int[] squareExceed = new int[3]; // kW
    private int[] nrOfExceeds = new int[3];
    private int[] maxDemand = new int[3]; // kW

    private int subscribedPowerPeak; // kW
    private int subscribedPowerNormalWinter; // kW
    private int subscribedPowerLowWinter; // kW
    private int subscribedPowerNormalSummer; // kW
    private int subscribedPowerLowSummer; // kW
    private int subscribedPowerMobile; // kW
    private int subscribedPowerNormalHalfSeason; // kW
    private int subscribedPowerLowHalfSeason; // kW
    private int subscribedPowerLowLowSeason; // kW

    private int rapport; // TCxTT
    private int exceededEnergy; // kWh
    private int code;

    /** Creates a new instance of MonthInfoTable */
    public MonthInfoTable(DataFactory dataFactory) {
        super(dataFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("MonthInfoTable:\n");
        strBuff.append("   LOW="+getLOW()+"\n");
        strBuff.append("   NORMAL="+getNORMAL()+"\n");
        strBuff.append("   PEAK="+getPEAK()+"\n");
        for (int i=0;i<getActiveEnergy().length;i++) {
            strBuff.append("       activeEnergy["+i+"]="+getActiveEnergy()[i]+"\n");
        }
        strBuff.append("   exceededEnergy="+getExceededEnergy()+"\n");
        for (int i=0;i<getMaxDemand().length;i++) {
            strBuff.append("       maxDemand["+i+"]="+getMaxDemand()[i]+"\n");
        }
        strBuff.append("   month="+getMonth()+"\n");
        for (int i=0;i<getNrOf10inuteIntervals().length;i++) {
            strBuff.append("       nrOf10inuteIntervals["+i+"]="+getNrOf10inuteIntervals()[i]+"\n");
        }
        for (int i=0;i<getNrOfExceeds().length;i++) {
            strBuff.append("       nrOfExceeds["+i+"]="+getNrOfExceeds()[i]+"\n");
        }
        strBuff.append("   rapport="+getRapport()+"\n");
        for (int i=0;i<getReactiveEnergy().length;i++) {
            strBuff.append("       reactiveEnergy["+i+"]="+getReactiveEnergy()[i]+"\n");
        }
        for (int i=0;i<getSquareExceed().length;i++) {
            strBuff.append("       squareExceed["+i+"]="+getSquareExceed()[i]+"\n");
        }
        strBuff.append("   subscribedPowerLowHalfSeason="+getSubscribedPowerLowHalfSeason()+"\n");
        strBuff.append("   subscribedPowerLowLowSeason="+getSubscribedPowerLowLowSeason()+"\n");
        strBuff.append("   subscribedPowerLowSummer="+getSubscribedPowerLowSummer()+"\n");
        strBuff.append("   subscribedPowerLowWinter="+getSubscribedPowerLowWinter()+"\n");
        strBuff.append("   subscribedPowerMobile="+getSubscribedPowerMobile()+"\n");
        strBuff.append("   subscribedPowerNormalHalfSeason="+getSubscribedPowerNormalHalfSeason()+"\n");
        strBuff.append("   subscribedPowerNormalSummer="+getSubscribedPowerNormalSummer()+"\n");
        strBuff.append("   subscribedPowerNormalWinter="+getSubscribedPowerNormalWinter()+"\n");
        strBuff.append("   subscribedPowerPeak="+getSubscribedPowerPeak()+"\n");
        strBuff.append("   tarif="+getTarif()+"\n");
        strBuff.append("   timestamp="+getTimestamp()+"\n");
        return strBuff.toString();
    }

    protected int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code=code;
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;
        Calendar cal = ProtocolUtils.getCleanCalendar(getDataFactory().getTrimaran().getTimeZone());
        cal.set(Calendar.DAY_OF_MONTH,ProtocolUtils.BCD2hex(data[offset++]));
        cal.set(Calendar.MONTH,ProtocolUtils.BCD2hex(data[offset++])-1);
		int year = ProtocolUtils.BCD2hex(data[offset++]) % 10;	// we only need the unit (eenheid in dutch)
		cal.set(Calendar.YEAR, getDecenniumYearTable()[year]);
        cal.set(Calendar.HOUR_OF_DAY,ProtocolUtils.BCD2hex(data[offset++]));
        cal.set(Calendar.MINUTE,ProtocolUtils.BCD2hex(data[offset++]));
        setTimestampCalendar(cal);
        setTimestamp(cal.getTime());

        setTarif(data[offset++]);
        setMonth(ProtocolUtils.BCD2hex(data[offset++]));
        for (int i=0;i<3;i++) {
            getActiveEnergy()[i] = ProtocolUtils.getLongLE(data,offset,4); offset+=4;
            getReactiveEnergy()[i] = ProtocolUtils.getLongLE(data,offset,4); offset+=4;
            getNrOf10inuteIntervals()[i] = ProtocolUtils.getIntLE(data,offset,2); offset+=2;
            getSquareExceed()[i] = ProtocolUtils.getIntLE(data,offset,3); offset+=3;
            getNrOfExceeds()[i] = ProtocolUtils.getIntLE(data,offset,2); offset+=2;
            getMaxDemand()[i] = ProtocolUtils.getIntLE(data,offset,2); offset+=2;
        }
        setSubscribedPowerPeak(ProtocolUtils.getIntLE(data,offset,2)); offset+=2;
        setSubscribedPowerNormalWinter(ProtocolUtils.getIntLE(data,offset,2)); offset+=2;
        setSubscribedPowerLowWinter(ProtocolUtils.getIntLE(data,offset,2)); offset+=2;
        setSubscribedPowerNormalSummer(ProtocolUtils.getIntLE(data,offset,2)); offset+=2;
        setSubscribedPowerLowSummer(ProtocolUtils.getIntLE(data,offset,2)); offset+=2;
        setSubscribedPowerMobile(ProtocolUtils.getIntLE(data,offset,2)); offset+=2;
        setSubscribedPowerNormalHalfSeason(ProtocolUtils.getIntLE(data,offset,2)); offset+=2;
        setSubscribedPowerLowHalfSeason(ProtocolUtils.getIntLE(data,offset,2)); offset+=2;
        setSubscribedPowerLowLowSeason(ProtocolUtils.getIntLE(data,offset,2)); offset+=2;

        setRapport(ProtocolUtils.getIntLE(data,offset,2)); offset+=2;
        setExceededEnergy(ProtocolUtils.getIntLE(data,offset,3)); offset+=3;

    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getTarif() {
        return tarif;
    }

    public void setTarif(int tarif) {
        this.tarif = tarif;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getPEAK() {
        return PEAK;
    }

    public int getNORMAL() {
        return NORMAL;
    }

    public int getLOW() {
        return LOW;
    }


    public Quantity getActiveQuantity(int index) {
        return new Quantity(BigDecimal.valueOf(getActiveEnergy()[index]),Unit.get("kWh"));
    }
    public Quantity getReactiveQuantity(int index) {
        return new Quantity(BigDecimal.valueOf(getReactiveEnergy()[index]),Unit.get("kvarh"));
    }
    public Quantity getSquareExceedQuantity(int index) {
        return new Quantity(BigDecimal.valueOf(getSquareExceed()[index]),Unit.get("kW"));
    }
    public Quantity getMaxDemandQuantity(int index) {
        return new Quantity(BigDecimal.valueOf(getMaxDemand()[index]),Unit.get("kW"));
    }
    public Quantity getNrOf10inuteIntervalsQuantity(int index) {
        return new Quantity(BigDecimal.valueOf(getNrOf10inuteIntervals()[index]),Unit.get(""));
    }
    public Quantity getNrOfExceedsQuantity(int index) {
        return new Quantity(BigDecimal.valueOf(getNrOfExceeds()[index]),Unit.get(""));
    }
    public Quantity getExceededEnergyQuantity() {
        return new Quantity(BigDecimal.valueOf(getExceededEnergy()),Unit.get("kWh"));
    }

    public Quantity getSubscribedPowerPeakQuantity() {
        return new Quantity(BigDecimal.valueOf(getSubscribedPowerPeak()),Unit.get("kW"));
    }
    public Quantity getSubscribedPowerNormalWinterQuantity() {
        return new Quantity(BigDecimal.valueOf(getSubscribedPowerNormalWinter()),Unit.get("kW"));
    }
    public Quantity getSubscribedPowerLowWinterQuantity() {
        return new Quantity(BigDecimal.valueOf(getSubscribedPowerLowWinter()),Unit.get("kW"));
    }
    public Quantity getSubscribedPowerNormalSummerQuantity() {
        return new Quantity(BigDecimal.valueOf(getSubscribedPowerNormalSummer()),Unit.get("kW"));
    }
    public Quantity getSubscribedPowerLowSummerQuantity() {
        return new Quantity(BigDecimal.valueOf(getSubscribedPowerLowSummer()),Unit.get("kW"));
    }
    public Quantity getSubscribedPowerMobileQuantity() {
        return new Quantity(BigDecimal.valueOf(getSubscribedPowerMobile()),Unit.get("kW"));
    }
    public Quantity getSubscribedPowerNormalHalfSeasonQuantity() {
        return new Quantity(BigDecimal.valueOf(getSubscribedPowerNormalHalfSeason()),Unit.get("kW"));
    }
    public Quantity getSubscribedPowerLowHalfSeasonQuantity() {
        return new Quantity(BigDecimal.valueOf(getSubscribedPowerLowHalfSeason()),Unit.get("kW"));
    }
    public Quantity getSubscribedPowerLowLowSeasonQuantity() {
        return new Quantity(BigDecimal.valueOf(getSubscribedPowerLowLowSeason()),Unit.get("kW"));
    }
    public Quantity getRapportQuantity() {
        return new Quantity(BigDecimal.valueOf(getRapport()),Unit.get(""));
    }

    public long[] getActiveEnergy() {
        return activeEnergy;
    }

    public void setActiveEnergy(long[] activeEnergy) {
        this.activeEnergy = activeEnergy;
    }

    public long[] getReactiveEnergy() {
        return reactiveEnergy;
    }

    public void setReactiveEnergy(long[] reactiveEnergy) {
        this.reactiveEnergy = reactiveEnergy;
    }

    public int[] getNrOf10inuteIntervals() {
        return nrOf10inuteIntervals;
    }

    public void setNrOf10inuteIntervals(int[] nrOf10inuteIntervals) {
        this.nrOf10inuteIntervals = nrOf10inuteIntervals;
    }

    public int[] getSquareExceed() {
        return squareExceed;
    }

    public void setSquareExceed(int[] squareExceed) {
        this.squareExceed = squareExceed;
    }

    public int[] getNrOfExceeds() {
        return nrOfExceeds;
    }

    public void setNrOfExceeds(int[] nrOfExceeds) {
        this.nrOfExceeds = nrOfExceeds;
    }

    public int[] getMaxDemand() {
        return maxDemand;
    }

    public void setMaxDemand(int[] maxDemand) {
        this.maxDemand = maxDemand;
    }

    public int getSubscribedPowerPeak() {
        return subscribedPowerPeak;
    }

    private void setSubscribedPowerPeak(int subscribedPowerPeak) {
        this.subscribedPowerPeak = subscribedPowerPeak;
    }

    public int getSubscribedPowerNormalWinter() {
        return subscribedPowerNormalWinter;
    }

    private void setSubscribedPowerNormalWinter(int subscribedPowerNormalWinter) {
        this.subscribedPowerNormalWinter = subscribedPowerNormalWinter;
    }

    public int getSubscribedPowerLowWinter() {
        return subscribedPowerLowWinter;
    }

    private void setSubscribedPowerLowWinter(int subscribedPowerLowWinter) {
        this.subscribedPowerLowWinter = subscribedPowerLowWinter;
    }

    public int getSubscribedPowerNormalSummer() {
        return subscribedPowerNormalSummer;
    }

    private void setSubscribedPowerNormalSummer(int subscribedPowerNormalSummer) {
        this.subscribedPowerNormalSummer = subscribedPowerNormalSummer;
    }

    public int getSubscribedPowerLowSummer() {
        return subscribedPowerLowSummer;
    }

    public void setSubscribedPowerLowSummer(int subscribedPowerLowSummer) {
        this.subscribedPowerLowSummer = subscribedPowerLowSummer;
    }

    public int getSubscribedPowerMobile() {
        return subscribedPowerMobile;
    }

    private void setSubscribedPowerMobile(int subscribedPowerMobile) {
        this.subscribedPowerMobile = subscribedPowerMobile;
    }

    public int getSubscribedPowerNormalHalfSeason() {
        return subscribedPowerNormalHalfSeason;
    }

    private void setSubscribedPowerNormalHalfSeason(int subscribedPowerNormalHalfSeason) {
        this.subscribedPowerNormalHalfSeason = subscribedPowerNormalHalfSeason;
    }

    public int getSubscribedPowerLowHalfSeason() {
        return subscribedPowerLowHalfSeason;
    }

    private void setSubscribedPowerLowHalfSeason(int subscribedPowerLowHalfSeason) {
        this.subscribedPowerLowHalfSeason = subscribedPowerLowHalfSeason;
    }

    public int getSubscribedPowerLowLowSeason() {
        return subscribedPowerLowLowSeason;
    }

    private void setSubscribedPowerLowLowSeason(int subscribedPowerLowLowSeason) {
        this.subscribedPowerLowLowSeason = subscribedPowerLowLowSeason;
    }

    public int getRapport() {
        return rapport;
    }

    private void setRapport(int rapport) {
        this.rapport = rapport;
    }

    public int getExceededEnergy() {
        return exceededEnergy;
    }

    private void setExceededEnergy(int exceededEnergy) {
        this.exceededEnergy = exceededEnergy;
    }

    private void setTimestampCalendar(Calendar timestampCalendar) {
        this.timestampCalendar = timestampCalendar;
    }

}