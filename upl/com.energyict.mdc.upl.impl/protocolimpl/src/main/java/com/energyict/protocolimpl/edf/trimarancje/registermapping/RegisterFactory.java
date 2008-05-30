/*
 * RegisterFactory.java
 *
 * Created on 27 juni 2006, 11:23
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarancje.registermapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.energyict.cbo.Quantity;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.edf.trimarancje.Trimaran;
import com.energyict.protocolimpl.edf.trimarancje.core.CurrentPeriodTable;
import com.energyict.protocolimpl.edf.trimarancje.core.MonthInfoTable;
import com.energyict.protocolimpl.edf.trimarancje.core.PreviousPeriodTable;

/**
 *
 * @author Koen
 */
public class RegisterFactory {
    
    Trimaran trimaran;
    List registers=null;
    int[] activeEnergyMatrix={6, 1, 4, 5, 11, 12}; //these form the same ObisCodes as for the CVE and ICE meter 
    
    /** Creates a new instance of RegisterFactory */
    public RegisterFactory(Trimaran trimaran) {
        this.trimaran=trimaran;
        
    }
    
    public Register findRegister(ObisCode obc) throws IOException {
        ObisCode obisCode = new ObisCode(obc.getA(),obc.getB(),obc.getC(),obc.getD(),obc.getE(),Math.abs(obc.getF()));
        Iterator it = getRegisters().iterator();
        while(it.hasNext()) {
            Register register = (Register)it.next();
            if (register.getRegisterValue().getObisCode().equals(obisCode)) {
                return register;
            }
        }
        throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
    }
    
    public List getRegisters() throws IOException {
        if (registers == null) {
            buidRegisterList();
        }
        return registers;
    }
    
    public void buidRegisterList() throws IOException {
        registers = new ArrayList();
        
        buildIndexes(trimaran.getDataFactory().getCurrentPeriodTable(),
        		trimaran.getDataFactory().getPreviousPeriodTable());
        
//        buildIndexes(255,trimaran.getDataFactory().getCurrentMonthInfoTable());
//        buildIndexes(0,trimaran.getDataFactory().getPreviousMonthInfoTable());
    }
    
    private void buildIndexes(CurrentPeriodTable currentPeriodTable, PreviousPeriodTable previousPeriodTable) {
        Quantity quantity=null;
        RegisterValue registerValue=null;
        Date toDate=null;
        Date fromDate=null;		
        
        for (int eField = 0; eField < 6; eField++) {
        	quantity = currentPeriodTable.getActiveQuantity(eField);
        	registerValue = new RegisterValue(ObisCode.fromString("1.1.1.8."+ activeEnergyMatrix[eField] +".255"), quantity);
        	registers.add(new Register(registerValue));
        	quantity = previousPeriodTable.getActiveQuantityP1(eField);
        	registerValue = new RegisterValue(ObisCode.fromString("1.1.1.8."+ activeEnergyMatrix[eField] +".0"), quantity, 
        			null, currentPeriodTable.getTimeStamp());
        	registers.add(new Register(registerValue));
        	quantity = previousPeriodTable.getActiveQuantityP2(eField);
        	registerValue = new RegisterValue(ObisCode.fromString("1.1.1.8."+ activeEnergyMatrix[eField] +".1"), quantity,
        			null, previousPeriodTable.getTimeStamp());
        	registers.add(new Register(registerValue));
        }
        int offset = 17;
        for (int zone = 0; zone <= 3; zone++){
        	// Puissance souscrite - Exceeding power
        	quantity = currentPeriodTable.getExceedingPowerQuantity(zone);
        	registerValue = new RegisterValue(ObisCode.fromString("1.1.1.129." + (offset+zone) + ".255"), quantity);
        	registers.add(new Register(registerValue));
        	quantity = previousPeriodTable.getExceedingPowerQuantityP1(zone);
        	registerValue = new RegisterValue(ObisCode.fromString("1.1.1.129." + (offset+zone) + ".0"), quantity,
        			null, currentPeriodTable.getTimeStamp());
        	registers.add(new Register(registerValue));
        	quantity = previousPeriodTable.getExceedingPowerQuantityP2(zone);
        	registerValue = new RegisterValue(ObisCode.fromString("1.1.1.129." + (offset+zone) + ".1"), quantity,
        			null, previousPeriodTable.getTimeStamp());
        	registers.add(new Register(registerValue));
        	
        	// Puissance apparente maximale - Maximum Demand
        	quantity = currentPeriodTable.getExceedingPowerQuantity(zone);
        	registerValue = new RegisterValue(ObisCode.fromString("1.1.1.6." + (offset+zone) + ".255"), quantity);
        	registers.add(new Register(registerValue));
        	quantity = previousPeriodTable.getExceedingPowerQuantityP1(zone);
        	registerValue = new RegisterValue(ObisCode.fromString("1.1.1.6." + (offset+zone) + ".0"), quantity,
        			null, currentPeriodTable.getTimeStamp());
        	registers.add(new Register(registerValue));
        	quantity = previousPeriodTable.getExceedingPowerQuantityP2(zone);
        	registerValue = new RegisterValue(ObisCode.fromString("1.1.1.6." + (offset+zone) + ".1"), quantity,
        			null, previousPeriodTable.getTimeStamp());
        	registers.add(new Register(registerValue));
        	
        	// Temps de fonctionnement pendant la zone ??
        	quantity = currentPeriodTable.getTarifDurationQuantity(zone);
        	registerValue = new RegisterValue(ObisCode.fromString("0.1.96.8." + (offset+zone) + ".255"), quantity);
        	registers.add(new Register(registerValue));
        	quantity = previousPeriodTable.getTarifDurationQuantity(zone);
        	registerValue = new RegisterValue(ObisCode.fromString("0.1.96.8." + (offset+zone) + ".0"), quantity,
        			null, currentPeriodTable.getTimeStamp());
        	registers.add(new Register(registerValue));
        	
        	// Durée totale de dépassement de la puissance souscrite dans la zone tarifaire ??
        	quantity = currentPeriodTable.getDurationExceedingPowerQuantity(zone);
        	registerValue = new RegisterValue(ObisCode.fromString("1.1.1.37." + (offset+zone) + ".255"), quantity);
        	registers.add(new Register(registerValue));
        	quantity = previousPeriodTable.getDurationExceedingPowerQuantityP1(zone);
        	registerValue = new RegisterValue(ObisCode.fromString("1.1.1.37." + (offset+zone) + ".0"), quantity,
        			null, currentPeriodTable.getTimeStamp());
        	registers.add(new Register(registerValue));
          	quantity = previousPeriodTable.getDurationExceedingPowerQuantityP2(zone);
        	registerValue = new RegisterValue(ObisCode.fromString("1.1.1.37." + (offset+zone) + ".1"), quantity,
        			null, previousPeriodTable.getTimeStamp());
        	registers.add(new Register(registerValue));
        	
        	// Coefficients de dépassement dans la zone tariaire ???
        	quantity = currentPeriodTable.getCoefficientQuantity(zone);
        	registerValue = new RegisterValue(ObisCode.fromString("1.1.1.130." + (offset+zone) + ".255"), quantity);
        	registers.add(new Register(registerValue));
        	quantity = previousPeriodTable.getCoefficientQuantityP1(zone);
        	registerValue = new RegisterValue(ObisCode.fromString("1.1.1.130." + (offset+zone) + ".0"), quantity,
        			null, currentPeriodTable.getTimeStamp());
        	registers.add(new Register(registerValue));
          	quantity = previousPeriodTable.getCoefficientQuantityP2(zone);
        	registerValue = new RegisterValue(ObisCode.fromString("1.1.1.130." + (offset+zone) + ".1"), quantity,
        			null, previousPeriodTable.getTimeStamp());
        	registers.add(new Register(registerValue));
        }
        
	}

	private void buildIndexes(int fField,MonthInfoTable monthInfo) {
        Quantity quantity=null;
        RegisterValue registerValue=null;
        Date toDate=null;
        Date fromDate=null;
                
        if (fField == 0) {
            Calendar cal = ProtocolUtils.getCalendar(trimaran.getTimeZone());
            int month = monthInfo.getMonth();
            if ((++month) > 12)
                month=1;
            month--;
            cal.set(Calendar.MONTH,month);
            cal.set(Calendar.DAY_OF_MONTH,1);
            cal.set(Calendar.HOUR_OF_DAY,2);
            cal.set(Calendar.MINUTE,0);
            cal.set(Calendar.SECOND,0);
            toDate = new Date(cal.getTime().getTime());
            cal.add(Calendar.MONTH,-1);
            fromDate = new Date(cal.getTime().getTime());
        }
        
        if (fField == 255) {
            Calendar cal = ProtocolUtils.getCalendar(trimaran.getTimeZone());
            int month = monthInfo.getMonth()-1;
            cal.set(Calendar.MONTH,month);
            cal.set(Calendar.DAY_OF_MONTH,1);
            cal.set(Calendar.HOUR_OF_DAY,2);
            cal.set(Calendar.MINUTE,0);
            cal.set(Calendar.SECOND,0);
            fromDate = new Date(cal.getTime().getTime());
        }
        
        
        for (int eField=1;eField<=3;eField++) {
            quantity = monthInfo.getActiveQuantity(eField-1);
            registerValue = new RegisterValue(ObisCode.fromString("1.1.1.8."+eField+"."+fField),quantity,null,toDate);
            registers.add(new Register(registerValue));
            
            quantity = monthInfo.getReactiveQuantity(eField-1);
            registerValue = new RegisterValue(ObisCode.fromString("1.1.3.8."+eField+"."+fField),quantity,null,toDate);
            registers.add(new Register(registerValue));

            quantity = monthInfo.getSquareExceedQuantity(eField-1);
            registerValue = new RegisterValue(ObisCode.fromString("1.1.1.38."+eField+"."+fField),quantity,null,fromDate,toDate);
            registers.add(new Register(registerValue));
            
            quantity = monthInfo.getMaxDemandQuantity(eField-1);
            registerValue = new RegisterValue(ObisCode.fromString("1.1.1.6."+eField+"."+fField),quantity,null,fromDate,toDate);
            registers.add(new Register(registerValue));
            
            quantity = monthInfo.getNrOf10inuteIntervalsQuantity(eField-1);
            registerValue = new RegisterValue(ObisCode.fromString("0.1.96.8."+eField+"."+fField),quantity,null,fromDate,toDate);
            registers.add(new Register(registerValue));
            
            quantity = monthInfo.getNrOfExceedsQuantity(eField-1);
            registerValue = new RegisterValue(ObisCode.fromString("1.1.1.128."+eField+"."+fField),quantity,null,fromDate,toDate);
            registers.add(new Register(registerValue));
        } // for (int eField=1;eField<=3;eField++)
        
        quantity = monthInfo.getExceededEnergyQuantity();
        registerValue = new RegisterValue(ObisCode.fromString("1.1.1.10.1."+fField),quantity,null,fromDate,toDate);
        registers.add(new Register(registerValue));
        
        quantity = monthInfo.getSubscribedPowerPeakQuantity();
        registerValue = new RegisterValue(ObisCode.fromString("1.1.1.129.1."+fField),quantity,null,fromDate,toDate);
        registers.add(new Register(registerValue));
        quantity = monthInfo.getSubscribedPowerNormalWinterQuantity();
        registerValue = new RegisterValue(ObisCode.fromString("1.1.1.130.1."+fField),quantity,null,fromDate,toDate);
        registers.add(new Register(registerValue));
        quantity = monthInfo.getSubscribedPowerLowWinterQuantity();
        registerValue = new RegisterValue(ObisCode.fromString("1.1.1.131.1."+fField),quantity,null,fromDate,toDate);
        registers.add(new Register(registerValue));
        quantity = monthInfo.getSubscribedPowerNormalSummerQuantity();
        registerValue = new RegisterValue(ObisCode.fromString("1.1.1.132.1."+fField),quantity,null,fromDate,toDate);
        registers.add(new Register(registerValue));
        quantity = monthInfo.getSubscribedPowerLowSummerQuantity();
        registerValue = new RegisterValue(ObisCode.fromString("1.1.1.133.1."+fField),quantity,null,fromDate,toDate);
        registers.add(new Register(registerValue));
        quantity = monthInfo.getSubscribedPowerMobileQuantity();
        registerValue = new RegisterValue(ObisCode.fromString("1.1.1.134.1."+fField),quantity,null,fromDate,toDate);
        registers.add(new Register(registerValue));
        quantity = monthInfo.getSubscribedPowerNormalHalfSeasonQuantity();
        registerValue = new RegisterValue(ObisCode.fromString("1.1.1.135.1."+fField),quantity,null,fromDate,toDate);
        registers.add(new Register(registerValue));
        quantity = monthInfo.getSubscribedPowerLowHalfSeasonQuantity();
        registerValue = new RegisterValue(ObisCode.fromString("1.1.1.136.1."+fField),quantity,null,fromDate,toDate);
        registers.add(new Register(registerValue));
        quantity = monthInfo.getSubscribedPowerLowLowSeasonQuantity();
        registerValue = new RegisterValue(ObisCode.fromString("1.1.1.137.1."+fField),quantity,null,fromDate,toDate);
        registers.add(new Register(registerValue));
        quantity = monthInfo.getRapportQuantity();
        registerValue = new RegisterValue(ObisCode.fromString("1.1.1.138.1."+fField),quantity,null,fromDate,toDate);
        registers.add(new Register(registerValue));
        
        
        
    } // private void buildIndexes(int fField,MonthInfoTable monthInfo)
    
    
    
}
