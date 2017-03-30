/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class MeterStatusTable extends AbstractTable {

    private Date timestamp;
    private int tarif;

    private int modeta;
    private int sommod;
    private int errFat;
    private int errSes;

    private int subscribedPowerPeak; // kW
    private int subscribedPowerNormalWinter; // kW
    private int subscribedPowerLowWinter; // kW
    private int subscribedPowerNormalSummer; // kW
    private int subscribedPowerLowSummer; // kW
    private int subscribedPowerMobile; // kW
    private int subscribedPowerNormalHalfSeason; // kW
    private int subscribedPowerLowHalfSeason; // kW
    private int subscribedPowerLowLowSeason; // kW



    /** Creates a new instance of MonthInfoTable */
    public MeterStatusTable(DataFactory dataFactory) {
        super(dataFactory);
    }

    protected int getCode() {
        return 3;
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("MeterStatusTable:\n");
        strBuff.append("   errFat="+getErrFat()+"\n");
        strBuff.append("   errSes="+getErrSes()+"\n");
        strBuff.append("   modeta="+getModeta()+"\n");
        strBuff.append("   sommod="+getSommod()+"\n");
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

    protected void parse(byte[] data) throws IOException {
        int offset=0;
        Calendar cal = ProtocolUtils.getCleanCalendar(getDataFactory().getTrimaran().getTimeZone());
        cal.set(Calendar.DAY_OF_MONTH,ProtocolUtils.BCD2hex(data[offset++]));
        cal.set(Calendar.MONTH,ProtocolUtils.BCD2hex(data[offset++])-1);
		int year = ProtocolUtils.BCD2hex(data[offset++]) % 10;	// we only need the unit (eenheid in dutch)
		cal.set(Calendar.YEAR, getDecenniumYearTable()[year]);
        cal.set(Calendar.HOUR_OF_DAY,ProtocolUtils.BCD2hex(data[offset++]));
        cal.set(Calendar.MINUTE,ProtocolUtils.BCD2hex(data[offset++]));
        setTimestamp(cal.getTime());
        setTarif(data[offset++]);

        setModeta(ProtocolUtils.getIntLE(data,offset++,1));
        setSommod(ProtocolUtils.getIntLE(data,offset,2)); offset+=2;
        setErrFat(ProtocolUtils.getIntLE(data,offset++,1));
        setErrSes(ProtocolUtils.getIntLE(data,offset++,1));

        setSubscribedPowerPeak(ProtocolUtils.getIntLE(data,offset,2)); offset+=2;
        setSubscribedPowerNormalWinter(ProtocolUtils.getIntLE(data,offset,2)); offset+=2;
        setSubscribedPowerLowWinter(ProtocolUtils.getIntLE(data,offset,2)); offset+=2;
        setSubscribedPowerNormalSummer(ProtocolUtils.getIntLE(data,offset,2)); offset+=2;
        setSubscribedPowerLowSummer(ProtocolUtils.getIntLE(data,offset,2)); offset+=2;
        setSubscribedPowerMobile(ProtocolUtils.getIntLE(data,offset,2)); offset+=2;
        setSubscribedPowerNormalHalfSeason(ProtocolUtils.getIntLE(data,offset,2)); offset+=2;
        setSubscribedPowerLowHalfSeason(ProtocolUtils.getIntLE(data,offset,2)); offset+=2;
        setSubscribedPowerLowLowSeason(ProtocolUtils.getIntLE(data,offset,2)); offset+=2;

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

    public int getModeta() {
        return modeta;
    }

    public void setModeta(int modeta) {
        this.modeta = modeta;
    }

    public int getSommod() {
        return sommod;
    }

    public void setSommod(int sommod) {
        this.sommod = sommod;
    }

    public int getErrFat() {
        return errFat;
    }

    public void setErrFat(int errFat) {
        this.errFat = errFat;
    }

    public int getErrSes() {
        return errSes;
    }

    public void setErrSes(int errSes) {
        this.errSes = errSes;
    }

    public int getSubscribedPowerPeak() {
        return subscribedPowerPeak;
    }

    public void setSubscribedPowerPeak(int subscribedPowerPeak) {
        this.subscribedPowerPeak = subscribedPowerPeak;
    }

    public int getSubscribedPowerNormalWinter() {
        return subscribedPowerNormalWinter;
    }

    public void setSubscribedPowerNormalWinter(int subscribedPowerNormalWinter) {
        this.subscribedPowerNormalWinter = subscribedPowerNormalWinter;
    }

    public int getSubscribedPowerLowWinter() {
        return subscribedPowerLowWinter;
    }

    public void setSubscribedPowerLowWinter(int subscribedPowerLowWinter) {
        this.subscribedPowerLowWinter = subscribedPowerLowWinter;
    }

    public int getSubscribedPowerNormalSummer() {
        return subscribedPowerNormalSummer;
    }

    public void setSubscribedPowerNormalSummer(int subscribedPowerNormalSummer) {
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

    public void setSubscribedPowerMobile(int subscribedPowerMobile) {
        this.subscribedPowerMobile = subscribedPowerMobile;
    }

    public int getSubscribedPowerNormalHalfSeason() {
        return subscribedPowerNormalHalfSeason;
    }

    public void setSubscribedPowerNormalHalfSeason(int subscribedPowerNormalHalfSeason) {
        this.subscribedPowerNormalHalfSeason = subscribedPowerNormalHalfSeason;
    }

    public int getSubscribedPowerLowHalfSeason() {
        return subscribedPowerLowHalfSeason;
    }

    public void setSubscribedPowerLowHalfSeason(int subscribedPowerLowHalfSeason) {
        this.subscribedPowerLowHalfSeason = subscribedPowerLowHalfSeason;
    }

    public int getSubscribedPowerLowLowSeason() {
        return subscribedPowerLowLowSeason;
    }

    public void setSubscribedPowerLowLowSeason(int subscribedPowerLowLowSeason) {
        this.subscribedPowerLowLowSeason = subscribedPowerLowLowSeason;
    }



}
