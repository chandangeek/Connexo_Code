/*
 * CTVT.java
 *
 * Created on 7 juli 2004, 12:25
 */

package com.energyict.protocolimpl.iec1107.indigo;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.TimeZone;

/**
 *
 * @author  Koen
 */
public class CTVT extends AbstractLogicalAddress {
    
    private int ctvtUnits;
    private int ctvtFraction;
    private int registerSignificantFigure;
    private int ctPrimaryAnnunciator;
    private int vtPrimaryAnnunciator;
    private String ctPrimary;
    private String ctSecondary;
    private String vtPrimary;
    private String vtSecondary;
    
    /** Creates a new instance of CTVT */
    public CTVT(int id,int size, LogicalAddressFactory laf) throws IOException {
        super(id,size,laf);
    }
    
    
    public String toString() {
       StringBuffer strBuff = new StringBuffer();
       strBuff.append("CTVT: ");
       strBuff.append("ctvtUnits="+getCtvtUnits()+", ");
       strBuff.append("ctvtFraction="+getCtvtFraction()+", ");
       strBuff.append("registerSignificantFigure="+getRegisterSignificantFigure()+", ");
       strBuff.append("ctPrimaryAnnunciator="+getCtPrimaryAnnunciator()+", ");
       strBuff.append("vtPrimaryAnnunciator="+getVtPrimaryAnnunciator()+", ");
       strBuff.append("ctPrimary="+getCtPrimary()+", ");
       strBuff.append("ctSecondary="+getCtSecondary()+", ");
       strBuff.append("vtPrimary="+getVtPrimary()+", ");
       strBuff.append("vtSecondary="+getVtSecondary()+", ");
       strBuff.append(", wholeCurrent="+isWholeCurrentMeter()+", ct="+isCTMeter()+", ctvt="+isCTVTMeter());
       return strBuff.toString();
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
    
    public void parse(byte[] data, TimeZone timeZone) throws java.io.IOException {
        setCtvtUnits(ProtocolUtils.getInt(data,0,2));
        setCtvtFraction(ProtocolUtils.getInt(data,2,2));
        setRegisterSignificantFigure(ProtocolUtils.getInt(data,4,1));
        setCtPrimaryAnnunciator(ProtocolUtils.getInt(data,5,1));
        setVtPrimaryAnnunciator(ProtocolUtils.getInt(data,6,1));
        setCtPrimary(new String(ProtocolUtils.getSubArray2(data,7,5)));
        setCtSecondary(new String(ProtocolUtils.getSubArray2(data,12,1)));
        setVtPrimary(new String(ProtocolUtils.getSubArray2(data,13,5)));
        setVtSecondary(new String(ProtocolUtils.getSubArray2(data,18,5)));
    }

    public int getCtvtUnits() {
        return ctvtUnits;
    }

    public void setCtvtUnits(int ctvtUnits) {
        this.ctvtUnits = ctvtUnits;
    }

    public int getCtvtFraction() {
        return ctvtFraction;
    }

    public void setCtvtFraction(int ctvtFraction) {
        this.ctvtFraction = ctvtFraction;
    }

    public int getRegisterSignificantFigure() {
        return registerSignificantFigure;
    }

    public void setRegisterSignificantFigure(int registerSignificantFigure) {
        this.registerSignificantFigure = registerSignificantFigure;
    }

    public int getCtPrimaryAnnunciator() {
        return ctPrimaryAnnunciator;
    }

    public void setCtPrimaryAnnunciator(int ctPrimaryAnnunciator) {
        this.ctPrimaryAnnunciator = ctPrimaryAnnunciator;
    }

    public int getVtPrimaryAnnunciator() {
        return vtPrimaryAnnunciator;
    }

    public void setVtPrimaryAnnunciator(int vtPrimaryAnnunciator) {
        this.vtPrimaryAnnunciator = vtPrimaryAnnunciator;
    }

    public String getCtPrimary() {
        return ctPrimary;
    }

    public void setCtPrimary(String ctPrimary) {
        this.ctPrimary = ctPrimary;
    }

    public String getCtSecondary() {
        return ctSecondary;
    }

    public void setCtSecondary(String ctSecondary) {
        this.ctSecondary = ctSecondary;
    }

    public String getVtPrimary() {
        return vtPrimary;
    }

    public void setVtPrimary(String vtPrimary) {
        this.vtPrimary = vtPrimary;
    }

    public String getVtSecondary() {
        return vtSecondary;
    }

    public void setVtSecondary(String vtSecondary) {
        this.vtSecondary = vtSecondary;
    }
    
}
