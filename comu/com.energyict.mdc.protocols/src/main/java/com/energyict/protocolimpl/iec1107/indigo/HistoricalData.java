/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * HistoricalData.java
 *
 * Created on 7 juli 2004, 12:39
 */

package com.energyict.protocolimpl.iec1107.indigo;

import com.energyict.mdc.common.Unit;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
/**
 *
 * @author  Koen
 */
public class HistoricalData extends AbstractLogicalAddress {

    final Unit[] units={Unit.get("kW"),Unit.get("kW"),Unit.get("kvar"),Unit.get("kVA")};

    static final int[] OBIS_C_MAPPING_MD_CMD={1,2,129,9};

    Date billingDate; // in GMT
    int ctvtUnits; //
    int ctvtFraction;
    int normalDisplayFormat;
    int engineeringDisplayFormat;
    int registerSignificantFigure;
    int ctPrimaryAnnunciator;
    int vtPrimaryAnnunciator;
    String ctPrimary;
    String ctSecondary;
    String vtPrimary;
    String vtSecondary;
    int[] demandUnits = new int[DemandRegisters.NR_OF_MAXIMUM_DEMANDS];

    /** Creates a new instance of HistoricalData */
    public HistoricalData(int id,int size, LogicalAddressFactory laf) throws IOException {
        super(id,size,laf);
    }

    private boolean isNONE(String str) {
        return (str.indexOf("NONE")!=-1);
    }

    public boolean isCTMeter() {
        return ((isNONE(vtPrimary) ||
                 isNONE(vtSecondary)) &&
              (!(isNONE(ctPrimary) ||
                 isNONE(ctSecondary))));
    }



    public boolean isCTVTMeter() {
        return ((!(isNONE(vtPrimary) ||
                 isNONE(vtSecondary))) &&
              (!(isNONE(ctPrimary) ||
                 isNONE(ctSecondary))));
    }

    public boolean isWholeCurrentMeter() {
        return !isCTVTMeter() && !isCTMeter();
    }

    private int getCTRatio() {
        return Integer.parseInt(getCtPrimary())/Integer.parseInt(getCtSecondary());
    }

    private int getVTRatio() {
        return Integer.parseInt(getVtPrimary())/Integer.parseInt(getVtSecondary());
    }

    public int getMultiplier() {
        if (isCTMeter())
           return getCTRatio();
        else if (isCTVTMeter())
           return getCTRatio()*getVTRatio();
        else if (isWholeCurrentMeter())
            return 1;

        else return 1;
    }

    public String toString() {
       StringBuffer strBuff = new StringBuffer();
       strBuff.append("HistoricalData: ");
       strBuff.append("billingDate="+getBillingDate().toString()+", ");
       strBuff.append("ctvtUnits="+getCtvtUnits()+", ");
       strBuff.append("ctvtFraction="+getCtvtFraction()+", ");
       strBuff.append("normalDisplayFormat="+getNormalDisplayFormat()+", ");
       strBuff.append("engineeringDisplayFormat="+getEngineeringDisplayFormat()+", ");
       strBuff.append("registerSignificantFigure="+getRegisterSignificantFigure()+", ");
       strBuff.append("ctPrimaryAnnunciator="+getCtPrimaryAnnunciator()+", ");
       strBuff.append("vtPrimaryAnnunciator="+getVtPrimaryAnnunciator()+", ");
       strBuff.append("ctPrimary="+getCtPrimary()+", ");
       strBuff.append("ctSecondary="+getCtSecondary()+", ");
       strBuff.append("vtPrimary="+getVtPrimary()+", ");
       strBuff.append("vtSecondary="+getVtSecondary()+", ");
       for (int i=0;i<DemandRegisters.NR_OF_MAXIMUM_DEMANDS;i++) {
           if (i!=0) strBuff.append(", ");
           strBuff.append("demand unit "+i+"="+getDemandUnit(i));
       }
       strBuff.append(", wholeCurrent="+isWholeCurrentMeter()+", ct="+isCTMeter()+", ctvt="+isCTVTMeter());
       return strBuff.toString();
    }

    public void parse(byte[] data, TimeZone timeZone) throws java.io.IOException {
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTimeInMillis(ProtocolUtils.getLong(data,0,4)*1000);
        setBillingDate(calendar.getTime());
        setCtvtUnits(ProtocolUtils.getInt(data,4,2));
        setCtvtFraction(ProtocolUtils.getInt(data,6,2));
        setNormalDisplayFormat(ProtocolUtils.getInt(data,8,1));
        setEngineeringDisplayFormat(ProtocolUtils.getInt(data,9,1));
        setRegisterSignificantFigure(ProtocolUtils.getInt(data,10,1));
        setCtPrimaryAnnunciator(ProtocolUtils.getInt(data,11,1));
        setVtPrimaryAnnunciator(ProtocolUtils.getInt(data,12,1));
        setCtPrimary(new String(ProtocolUtils.getSubArray2(data,13,5)));
        setCtSecondary(new String(ProtocolUtils.getSubArray2(data,18,1)));
        setVtPrimary(new String(ProtocolUtils.getSubArray2(data,19,5)));
        setVtSecondary(new String(ProtocolUtils.getSubArray2(data,24,5)));
        for (int i=0;i<DemandRegisters.NR_OF_MAXIMUM_DEMANDS;i++) {
            getDemandUnits()[i] = ProtocolUtils.getInt(data,29+i,1);
        }
    }

    /**
     * Getter for property billingDate.
     * @return Value of property billingDate.
     */
    public java.util.Date getBillingDate() {
        // return null if current billing period cause timestamp makes no sence...
        //if ((getId()%0x100)==0) return null;
        //else
            return billingDate;
    }

    /**
     * Setter for property billingDate.
     * @param billingDate New value of property billingDate.
     */
    public void setBillingDate(java.util.Date billingDate) {
        this.billingDate = billingDate;
    }

    /**
     * Getter for property ctvtUnits.
     * @return Value of property ctvtUnits.
     */
    public int getCtvtUnits() {
        return ctvtUnits;
    }

    /**
     * Setter for property ctvtUnits.
     * @param ctvtUnits New value of property ctvtUnits.
     */
    public void setCtvtUnits(int ctvtUnits) {
        this.ctvtUnits = ctvtUnits;
    }

    /**
     * Getter for property ctvtFraction.
     * @return Value of property ctvtFraction.
     */
    public int getCtvtFraction() {
        return ctvtFraction;
    }

    /**
     * Setter for property ctvtFraction.
     * @param ctvtFraction New value of property ctvtFraction.
     */
    public void setCtvtFraction(int ctvtFraction) {
        this.ctvtFraction = ctvtFraction;
    }

    /**
     * Getter for property normalDisplayFormat.
     * @return Value of property normalDisplayFormat.
     */
    public int getNormalDisplayFormat() {
        return normalDisplayFormat;
    }

    /**
     * Setter for property normalDisplayFormat.
     * @param normalDisplayFormat New value of property normalDisplayFormat.
     */
    public void setNormalDisplayFormat(int normalDisplayFormat) {
        this.normalDisplayFormat = normalDisplayFormat;
    }

    /**
     * Getter for property engineeringDisplayFormat.
     * @return Value of property engineeringDisplayFormat.
     */
    public int getEngineeringDisplayFormat() {
        return engineeringDisplayFormat;
    }

    /**
     * Setter for property engineeringDisplayFormat.
     * @param engineeringDisplayFormat New value of property engineeringDisplayFormat.
     */
    public void setEngineeringDisplayFormat(int engineeringDisplayFormat) {
        this.engineeringDisplayFormat = engineeringDisplayFormat;
    }

    /**
     * Getter for property registerSignificantFigure.
     * @return Value of property registerSignificantFigure.
     */
    public int getRegisterSignificantFigure() {
        return registerSignificantFigure;
    }

    /**
     * Setter for property registerSignificantFigure.
     * @param registerSignificantFigure New value of property registerSignificantFigure.
     */
    /*
     *  999999999. kxx = 5
     *  99999999.9 kxx = 4
     *  9999999.99 kxx = 3
     *  999999.999 kxx = 2
     *  99999.9999 kxx = 1
     *  9999.99999 kxx = 0
     */
    public void setRegisterSignificantFigure(int registerSignificantFigure) {
        this.registerSignificantFigure = registerSignificantFigure;
    }

    /**
     * Getter for property ctPrimaryAnnunciator.
     * @return Value of property ctPrimaryAnnunciator.
     */
    public int getCtPrimaryAnnunciator() {
        return ctPrimaryAnnunciator;
    }

    /**
     * Setter for property ctPrimaryAnnunciator.
     * @param ctPrimaryAnnunciator New value of property ctPrimaryAnnunciator.
     */
    public void setCtPrimaryAnnunciator(int ctPrimaryAnnunciator) {
        this.ctPrimaryAnnunciator = ctPrimaryAnnunciator;
    }

    /**
     * Getter for property vtPrimaryAnnunciator.
     * @return Value of property vtPrimaryAnnunciator.
     */
    public int getVtPrimaryAnnunciator() {
        return vtPrimaryAnnunciator;
    }

    /**
     * Setter for property vtPrimaryAnnunciator.
     * @param vtPrimaryAnnunciator New value of property vtPrimaryAnnunciator.
     */
    public void setVtPrimaryAnnunciator(int vtPrimaryAnnunciator) {
        this.vtPrimaryAnnunciator = vtPrimaryAnnunciator;
    }

    /**
     * Getter for property ctPrimary.
     * @return Value of property ctPrimary.
     */
    public String getCtPrimary() {
        return ctPrimary;
    }

    /**
     * Setter for property ctPrimary.
     * @param ctPrimary New value of property ctPrimary.
     */
    public void setCtPrimary(String ctPrimary) {
        this.ctPrimary = ctPrimary;
    }

    /**
     * Getter for property ctSecondary.
     * @return Value of property ctSecondary.
     */
    public String getCtSecondary() {
        return ctSecondary;
    }

    /**
     * Setter for property ctSecondary.
     * @param ctSecondary New value of property ctSecondary.
     */
    public void setCtSecondary(String ctSecondary) {
        this.ctSecondary = ctSecondary;
    }

    /**
     * Getter for property vtPrimary.
     * @return Value of property vtPrimary.
     */
    public String getVtPrimary() {
        return vtPrimary;
    }

    /**
     * Setter for property vtPrimary.
     * @param vtPrimary New value of property vtPrimary.
     */
    public void setVtPrimary(String vtPrimary) {
        this.vtPrimary = vtPrimary;
    }

    /**
     * Getter for property vtSecondary.
     * @return Value of property vtSecondary.
     */
    public String getVtSecondary() {
        return vtSecondary;
    }

    /**
     * Setter for property vtSecondary.
     * @param vtSecondary New value of property vtSecondary.
     */
    public void setVtSecondary(String vtSecondary) {
        this.vtSecondary = vtSecondary;
    }

    /**
     * Getter for property demandUnits.
     * @return Value of property demandUnits.
     */
    public int[] getDemandUnits() {
        return this.demandUnits;
    }

    /**
     * Getter for property demandUnits.
     * @return Value of property demandUnits.
     */
    public int getObisC(int i) {
        if (demandUnits[i] > 3) return 255;
        else return OBIS_C_MAPPING_MD_CMD[demandUnits[i]];
    }

    /**
     * Setter for property demandUnits.
     * @param demandUnits New value of property demandUnits.
     */
    public void setDemandUnits(int[] demandUnits) {
        this.demandUnits = demandUnits;
    }

    /**
     * Getter for property demandUnits.
     * @return Value of property demandUnits.
     */
    public Unit getDemandUnit(int mdIndex) {
        if (getDemandUnits()[mdIndex] > 3 ) // KV TO_DO following the logical addresses specifications document,
                                            // 'Demand x demand units' can NEVER have a value <0 or >3
                                            // My experience is that is DOES happen!
            return Unit.get("");
        else
            return units[getDemandUnits()[mdIndex]];
    }



    /**
     * Getter for property scaler.
     * @return Value of property scaler.
     */
    public int getScaler() {
        return (5-getRegisterSignificantFigure());
    }

}
