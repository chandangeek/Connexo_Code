package com.energyict.protocolimpl.iec1107.ppm.register;

import java.util.Date;
import java.util.TimeZone;

import com.energyict.protocolimpl.iec1107.ppm.MetaRegister;
import com.energyict.protocolimpl.iec1107.ppm.RegisterFactory;

/** @author fbo */

public class HistoricalData {
    
    int billingCount;
    
    Date date;
    TimeZone timeZone;
    
    MainRegister importKWh;
    MainRegister exportKWh;
    MainRegister importKvarh;
    MainRegister exportKvarh;
    MainRegister totalKVAh;
    
    MainRegister timeOfUse1;
    MainRegister timeOfUse2;
    MainRegister timeOfUse3;
    MainRegister timeOfUse4;
    MainRegister timeOfUse5;
    MainRegister timeOfUse6;
    MainRegister timeOfUse7;
    MainRegister timeOfUse8;
    
    MaximumDemand maxDemand1;
    MaximumDemand maxDemand2;
    MaximumDemand maxDemand3;
    MaximumDemand maxDemand4;
    
    MainRegister cumulativeMaxDemand1;
    MainRegister cumulativeMaxDemand2;
    MainRegister cumulativeMaxDemand3;
    MainRegister cumulativeMaxDemand4;
    
    public MainRegister getExportKWh() {
        return exportKWh;
    }
    
    public void setExportKWh(MainRegister exportKWh) {
        this.exportKWh = exportKWh;
    }
    
    public MainRegister getImportKvarh() {
        return importKvarh;
    }
    
    public void setImportKvarh(MainRegister importKvarh) {
        this.importKvarh = importKvarh;
    }
    
    public MainRegister getCumulativeMaxDemand1() {
        return cumulativeMaxDemand1;
    }
    
    public int getBillingCount() {
        return billingCount;
    }
    
    public void setBillingCount(int billingCount) {
        this.billingCount = billingCount;
    }
    
    public MaximumDemand getMaxDemand1() {
        return maxDemand1;
    }
    
    public void setMaxDemand1(MaximumDemand maxDemand1) {
        this.maxDemand1 = maxDemand1;
    }
    
    public MaximumDemand getMaxDemand2() {
        return maxDemand2;
    }
    
    public void setMaxDemand2(MaximumDemand maxDemand2) {
        this.maxDemand2 = maxDemand2;
    }
    
    public MaximumDemand getMaxDemand3() {
        return maxDemand3;
    }
    
    public void setMaxDemand3(MaximumDemand maxDemand3) {
        this.maxDemand3 = maxDemand3;
    }
    
    public MaximumDemand getMaxDemand4() {
        return maxDemand4;
    }
    
    public void setMaxDemand4(MaximumDemand maxDemand4) {
        this.maxDemand4 = maxDemand4;
    }
    
    public void setCumulativeMaxDemand1(MainRegister cumulativeMaxDemand1) {
        this.cumulativeMaxDemand1 = cumulativeMaxDemand1;
    }
    
    public MainRegister getCumulativeMaxDemand2() {
        return cumulativeMaxDemand2;
    }
    
    public void setCumulativeMaxDemand2(MainRegister cumulativeMaxDemand2) {
        this.cumulativeMaxDemand2 = cumulativeMaxDemand2;
    }
    
    public MainRegister getCumulativeMaxDemand3() {
        return cumulativeMaxDemand3;
    }
    
    public void setCumulativeMaxDemand3(MainRegister cumulativeMaxDemand3) {
        this.cumulativeMaxDemand3 = cumulativeMaxDemand3;
    }
    
    public MainRegister getCumulativeMaxDemand4() {
        return cumulativeMaxDemand4;
    }
    
    public void setCumulativeMaxDemand4(MainRegister cumulativeMaxDemand4) {
        this.cumulativeMaxDemand4 = cumulativeMaxDemand4;
    }
    
    public Date getDate() {
        return date;
    }
    
    public void setDate(Date date) {
        this.date = date;
    }
    
    public MainRegister getTimeOfUse1() {
        return timeOfUse1;
    }
    
    public void setTimeOfUse1(MainRegister timeOfUse1) {
        this.timeOfUse1 = timeOfUse1;
    }
    
    public MainRegister getTimeOfUse2() {
        return timeOfUse2;
    }
    
    public void setTimeOfUse2(MainRegister timeOfUse2) {
        this.timeOfUse2 = timeOfUse2;
    }
    
    public MainRegister getTimeOfUse3() {
        return timeOfUse3;
    }
    
    public void setTimeOfUse3(MainRegister timeOfUse3) {
        this.timeOfUse3 = timeOfUse3;
    }
    
    public MainRegister getTimeOfUse4() {
        return timeOfUse4;
    }
    
    public void setTimeOfUse4(MainRegister timeOfUse4) {
        this.timeOfUse4 = timeOfUse4;
    }
    
    public MainRegister getTimeOfUse5() {
        return timeOfUse5;
    }
    
    public void setTimeOfUse5(MainRegister timeOfUse5) {
        this.timeOfUse5 = timeOfUse5;
    }
    
    public MainRegister getTimeOfUse6() {
        return timeOfUse6;
    }
    
    public void setTimeOfUse6(MainRegister timeOfUse6) {
        this.timeOfUse6 = timeOfUse6;
    }
    
    public MainRegister getTimeOfUse7() {
        return timeOfUse7;
    }
    
    public void setTimeOfUse7(MainRegister timeOfUse7) {
        this.timeOfUse7 = timeOfUse7;
    }
    
    public MainRegister getTimeOfUse8() {
        return timeOfUse8;
    }
    
    public void setTimeOfUse8(MainRegister timeOfUse8) {
        this.timeOfUse8 = timeOfUse8;
    }
    
    public MainRegister getExportKvarh() {
        return exportKvarh;
    }
    
    public void setExportKvarh(MainRegister exportKvarh) {
        this.exportKvarh = exportKvarh;
    }
    
    public MainRegister getImportKWh() {
        return importKWh;
    }
    
    public void setImportKWh(MainRegister importKWh) {
        this.importKWh = importKWh;
    }
    
    public TimeZone getTimeZone() {
        return timeZone;
    }
    
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }
    
    public MainRegister getTotalKVAh() {
        return totalKVAh;
    }
    
    public void setTotalKVAh(MainRegister totalKVAh) {
        this.totalKVAh = totalKVAh;
    }
    
    public Object get( MetaRegister register ){
        return get( register.getRegisterFactoryKey() );
    }
    
    public Object get( String key ){
        
        if( key.equals( RegisterFactory.R_TOTAL_IMPORT_WH ) )
            return importKWh;
        if( key.equals( RegisterFactory.R_TOTAL_EXPORT_WH ) )
            return exportKWh;
        if( key.equals( RegisterFactory.R_TOTAL_IMPORT_VARH ) )
            return importKvarh;
        if( key.equals( RegisterFactory.R_TOTAL_EXPORT_VARH ) )
            return exportKvarh;
        if( key.equals( RegisterFactory.R_TOTAL_VAH ) )
            return totalKVAh;
        
        if( key.equals( RegisterFactory.R_TIME_OF_USE_1 ) )
            return timeOfUse1;
        if( key.equals( RegisterFactory.R_TIME_OF_USE_2 ) )
            return timeOfUse2;
        if( key.equals( RegisterFactory.R_TIME_OF_USE_3 ) )
            return timeOfUse3;
        if( key.equals( RegisterFactory.R_TIME_OF_USE_4 ) )
            return timeOfUse4;
        if( key.equals( RegisterFactory.R_TIME_OF_USE_5 ) )
            return timeOfUse5;
        if( key.equals( RegisterFactory.R_TIME_OF_USE_6 ) )
            return timeOfUse6;
        if( key.equals( RegisterFactory.R_TIME_OF_USE_7 ) )
            return timeOfUse7;
        if( key.equals( RegisterFactory.R_TIME_OF_USE_8 ) )
            return timeOfUse8;
        
        if( key.equals( RegisterFactory.R_MAXIMUM_DEMAND_1 ) )
            return maxDemand1;
        if( key.equals( RegisterFactory.R_MAXIMUM_DEMAND_2 ) )
            return maxDemand2;
        if( key.equals( RegisterFactory.R_MAXIMUM_DEMAND_3 ) )
            return maxDemand3;
        if( key.equals( RegisterFactory.R_MAXIMUM_DEMAND_4 ) )
            return maxDemand4;
        
        if( key.equals( RegisterFactory.R_CUMULATIVE_MAXIMUM_DEMAND1 ) )
            return cumulativeMaxDemand1;
        if( key.equals( RegisterFactory.R_CUMULATIVE_MAXIMUM_DEMAND2 ) )
            return cumulativeMaxDemand2;
        if( key.equals( RegisterFactory.R_CUMULATIVE_MAXIMUM_DEMAND3 ) )
            return cumulativeMaxDemand3;
        if( key.equals( RegisterFactory.R_CUMULATIVE_MAXIMUM_DEMAND4 ) )
            return cumulativeMaxDemand4;
        
        return null;
    }
    
    public String toString() {
        
        StringBuffer sb = new StringBuffer();
        
        sb.append("Historical Data: \n");
        sb.append("Billing count = " + billingCount + " date = " + date + "\n");
        
        sb.append("Total registers\n");
        sb.append(importKWh + "\n");
        sb.append(exportKWh + "\n");
        sb.append(importKvarh + "\n");
        sb.append(totalKVAh + "\n");
        
        sb.append("TOU registers\n");
        sb.append(timeOfUse1 + "\n");
        sb.append(timeOfUse2 + "\n");
        sb.append(timeOfUse3 + "\n");
        sb.append(timeOfUse4 + "\n");
        sb.append(timeOfUse5 + "\n");
        sb.append(timeOfUse6 + "\n");
        sb.append(timeOfUse7 + "\n");
        sb.append(timeOfUse8 + "\n\n");
        
        sb.append("Max demand\n");
        sb.append(maxDemand1 + "\n");
        sb.append(maxDemand2 + "\n");
        sb.append(maxDemand3 + "\n");
        sb.append(maxDemand4 + "\n\n");
        
        sb.append("Cum Max demand\n");
        sb.append(cumulativeMaxDemand1 + "\n");
        sb.append(cumulativeMaxDemand2 + "\n");
        sb.append(cumulativeMaxDemand3 + "\n");
        sb.append(cumulativeMaxDemand4 + "\n");
        
        sb.append("___\n\n\n");
        
        return sb.toString();
    }
    
}