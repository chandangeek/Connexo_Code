/*
 * ConcentratorProfile.java
 *
 * Created on 6 december 2007, 10:35
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.genericprotocolimpl.actarisplcc3g;

import com.energyict.cbo.*;
import com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects.*;
import com.energyict.genericprotocolimpl.common.*;
import com.energyict.obis.*;
import com.energyict.protocol.*;
import java.math.*;
import java.util.*;
import java.io.*;
import com.energyict.dlms.cosem.DataAccessResultException;
/**
 *
 * @author kvds
 */
public class ConcentratorRegister {
    
    Concentrator concentrator;
    PLCCMeterDailyEnergyValueProfile pLCCMeterDailyEnergyValueProfile=null;
     
    /** Creates a new instance of ConcentratorProfile */
    public ConcentratorRegister(Concentrator concentrator) {
        this.concentrator=concentrator;
    }
    
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        boolean fakePowerQualityConfig = false;
        
        if (concentrator.getCurrentSelectedDevice() != null) {
            fakePowerQualityConfig = Integer.parseInt(concentrator.getCurrentSelectedDevice().getProperties().getProperty("PowerQualityConfig","0"))==1;
        }
            
        try {
            if (obisCode.equals(ObisCode.fromString("0.0.96.5.0.255")))
                return new RegisterValue(obisCode,new Quantity(concentrator.getPLCCObjectFactory().getPLCCMeterStatus().getStatus(),Unit.get("")));
            else if (obisCode.equals(ObisCode.fromString("0.0.97.97.0.255")))
                return new RegisterValue(obisCode,new Quantity(concentrator.getPLCCObjectFactory().getPLCCMeterErrorCodeRegister().getErrorCode(),Unit.get("")));
            else if (obisCode.equals(ObisCode.fromString("0.0.128.30.22.255")))
                return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(concentrator.getPLCCObjectFactory().getPLCCMeterContactorState().readState()),Unit.get(""))); 
            else if (obisCode.equals(ObisCode.fromString("0.0.96.2.0.255")))
                return new RegisterValue(obisCode, concentrator.getPLCCObjectFactory().getPLCCMeterIdentification().getIdentification());
            else if (obisCode.equals(ObisCode.fromString("0.0.96.3.2.255")))
                return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(concentrator.getPLCCObjectFactory().getPLCCMeterTICConfiguration().readMode()),Unit.get("")));
            else if (isActiveImportEnergyRegister(obisCode))
                return getActiveImportEnergyRegister(obisCode);
            else if (isBillingActiveImportEnergyRegister(obisCode))
                return getBillingActiveImportEnergyRegister(obisCode);
            else if (isBillingStatusRegister(obisCode))
                return getBillingStatusRegister(obisCode);
            else if (obisCode.equals(ObisCode.fromString("1.1.0.4.2.255"))) {
                PLCCMeterCurrentRatio o = concentrator.getPLCCObjectFactory().getPLCCMeterCurrentRatio();
                return new RegisterValue(obisCode,new Quantity(o.getEnergyMultiplier(),Unit.get("")));
            }
            else if (obisCode.equals(ObisCode.fromString("1.0.12.31.0.255"))) { //1
                if (fakePowerQualityConfig)
                    return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(207),Unit.get(BaseUnit.VOLT)));
                else {
                    PLCCMeterThresholdForSag o = concentrator.getPLCCObjectFactory().getPLCCMeterThresholdForSag();
                    return new RegisterValue(obisCode,new Quantity(o.getValue(),o.getScalerUnit().getUnit()));
                }
            }
            else if (obisCode.equals(ObisCode.fromString("1.0.12.35.0.255"))) { //2
                if (fakePowerQualityConfig)
                    return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(257),Unit.get(BaseUnit.VOLT)));
                else {    
                    PLCCMeterThresholdForSwell o = concentrator.getPLCCObjectFactory().getPLCCMeterThresholdForSwell();
                    return new RegisterValue(obisCode,new Quantity(o.getValue(),o.getScalerUnit().getUnit()));
                }
            }
            else if (obisCode.equals(ObisCode.fromString("1.0.12.31.129.255"))) { //3
                if (fakePowerQualityConfig)
                    return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(1),Unit.get(BaseUnit.SECOND)));
                else {
                    PLCCMeterTimeIntegralForSagMeasurement o = concentrator.getPLCCObjectFactory().getPLCCMeterTimeIntegralForSagMeasurement();
                    return new RegisterValue(obisCode,new Quantity(o.getValue(),o.getScalerUnit().getUnit()));
                }
            }
            else if (obisCode.equals(ObisCode.fromString("0.0.96.7.20.255"))) { //4
                if (fakePowerQualityConfig)
                    return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(180),Unit.get(BaseUnit.SECOND)));
                else {
                    PLCCMeterTimeThresholdForLongPowerFailure o = concentrator.getPLCCObjectFactory().getPLCCMeterTimeThresholdForLongPowerFailure();
                    return new RegisterValue(obisCode,new Quantity(o.getValue(),o.getScalerUnit().getUnit()));
                }
            }
            else if (obisCode.equals(ObisCode.fromString("1.0.0.8.2.255"))) { //5
                if (fakePowerQualityConfig)
                    return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(1),Unit.get(BaseUnit.SECOND)));
                else {
                    PLCCMeterTimeIntegralForInstantaneousDemand o = concentrator.getPLCCObjectFactory().getPLCCMeterTimeIntegralForInstantaneousDemand();
                    return new RegisterValue(obisCode,new Quantity(o.getValue(),o.getScalerUnit().getUnit()));
                }
            }
            else if (obisCode.equals(ObisCode.fromString("1.0.12.35.129.255"))) {//6
                if (fakePowerQualityConfig)
                    return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(1),Unit.get(BaseUnit.SECOND)));
                else {
                    PLCCMeterTimeIntegralForSwellMeasurement o = concentrator.getPLCCObjectFactory().getPLCCMeterTimeIntegralForSwellMeasurement();
                    return new RegisterValue(obisCode,new Quantity(o.getValue(),o.getScalerUnit().getUnit()));
                }
            }
            else if (obisCode.equals(ObisCode.fromString("0.0.96.7.0.255"))) {
                PLCCMeterNumberOfShortPowerFailures o = concentrator.getPLCCObjectFactory().getPLCCMeterNumberOfShortPowerFailures();
                return new RegisterValue(obisCode,new Quantity(o.getValue(),Unit.get("")));
            }
            else if (obisCode.equals(ObisCode.fromString("0.0.96.7.5.255"))) {
                PLCCMeterNumberOfLongPowerFailures o = concentrator.getPLCCObjectFactory().getPLCCMeterNumberOfLongPowerFailures();
                return new RegisterValue(obisCode,new Quantity(o.getValue(),Unit.get("")));
            }
            else if (obisCode.equals(ObisCode.fromString("1.0.12.32.0.255"))) {
                PLCCMeterNumberOfSag o = concentrator.getPLCCObjectFactory().getPLCCMeterNumberOfSag();
                return new RegisterValue(obisCode,new Quantity(o.getValue(),Unit.get("")));
            }
            else if (obisCode.equals(ObisCode.fromString("1.0.12.36.0.255"))) {
                PLCCMeterNumberOfSwell o = concentrator.getPLCCObjectFactory().getPLCCMeterNumberOfSwell();
                return new RegisterValue(obisCode,new Quantity(o.getValue(),Unit.get("")));
            }
            else if (obisCode.equals(ObisCode.fromString("1.0.12.38.0.255"))) {
                PLCCMeterMaximumVoltage o = concentrator.getPLCCObjectFactory().getPLCCMeterMaximumVoltage();
                return new RegisterValue(obisCode,new Quantity(o.getValue(),o.getScalerUnit().getUnit()));
            }
            else if (obisCode.equals(ObisCode.fromString("1.0.12.34.0.255"))) {
                PLCCMeterMinimumVoltage o = concentrator.getPLCCObjectFactory().getPLCCMeterMinimumVoltage();
                return new RegisterValue(obisCode,new Quantity(o.getValue(),o.getScalerUnit().getUnit()));
            }
            else if (obisCode.equals(ObisCode.fromString("1.0.1.7.0.255"))) {
                PLCCMeterInstantaneousDemand o = concentrator.getPLCCObjectFactory().getPLCCMeterInstantaneousDemand();
                return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(o.getValue()),o.getScalerUnit().getUnit()));
            }
            // ftpServerId object
            else if (obisCode.equals(ObisCode.fromString("0.0.25.5.0.255"))) {
                com.energyict.edf.messages.objects.FtpServerId o = concentrator.getPLCCObjectFactory().getPLCCFTPServerId().readFtpServerId();
                return new RegisterValue(obisCode,o.xmlEncode());
            }
            // meterclockobject
            else if (obisCode.equals(ObisCode.fromString("0.0.1.0.0.255"))) {
                com.energyict.edf.messages.objects.MeterClock o = concentrator.getPLCCObjectFactory().getPLCCMeterCurrentDateTime().readMeterClock();
                return new RegisterValue(obisCode,o.xmlEncode());
            }
            // meterclockobject
            else if (obisCode.equals(ObisCode.fromString("0.1.1.0.0.255"))) {
                com.energyict.edf.messages.objects.MeterClock o = concentrator.getPLCCObjectFactory().getPLCCCurrentDateTime().readMeterClock();
                return new RegisterValue(obisCode,o.xmlEncode());
            }
            // activity calendar object
            else if (obisCode.equals(ObisCode.fromString("0.0.13.0.0.255"))) {
                com.energyict.edf.messages.objects.ActivityCalendar o = concentrator.getPLCCObjectFactory().getPLCCMeterActivityCalendar().readActivityCalendar();
                return new RegisterValue(obisCode,o.xmlEncode());
            }
            // moving peak object
            else if (obisCode.equals(ObisCode.fromString("0.0.10.0.125.255"))) {
                com.energyict.edf.messages.objects.MovingPeak o = concentrator.getPLCCObjectFactory().getPLCCMeterMovingPeak().readMovingPeak();
                return new RegisterValue(obisCode,o.xmlEncode());
            }
            // demand management object
            else if (obisCode.equals(ObisCode.fromString("0.0.16.0.1.255"))) {
                com.energyict.edf.messages.objects.DemandManagement o = concentrator.getPLCCObjectFactory().getPLCCMeterDemandManagement().readDemandManagement();
                return new RegisterValue(obisCode,o.xmlEncode());
            }
            // meter identification object
            else if (obisCode.equals(ObisCode.fromString("0.0.96.2.0.255"))) {
                com.energyict.edf.messages.objects.MeterIdentification o = concentrator.getPLCCObjectFactory().getPLCCMeterIdentification().toMeterIdentification();
                return new RegisterValue(obisCode,o.xmlEncode());
            }
        }
        catch(DataAccessResultException e) {
            throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported! (error received is "+e.toString()+")");
            
//            if (!o.isCPLError()) || (retries++>2)) {
//            // KV_TO_DO
//            // action f(data access result)
//                throw new IOException(e+", "+o.toString());
//            }
        }
        
        throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
    }
    
    private boolean isActiveImportEnergyRegister(ObisCode obisCode) {
        if ((obisCode.getA() == 1) && (obisCode.getB() == 0) &&  (obisCode.getC() == 1) && (obisCode.getD() == 8) && (obisCode.getF() == 255) &&
            (obisCode.getE() >=0) && (obisCode.getE()<=6)) 
            return true;
        else
            return false;
    }
    
    private RegisterValue getActiveImportEnergyRegister(ObisCode obisCode) throws IOException {
        if ((obisCode.getA() == 1) && (obisCode.getB() == 0) &&  (obisCode.getC() == 1) && (obisCode.getD() == 8) && (obisCode.getF() == 255) &&
            (obisCode.getE() >=0) && (obisCode.getE()<=6)) {
            PLCCMeterEnergyRegister o = concentrator.getPLCCObjectFactory().getPLCCMeterEnergyRegister(obisCode.getE());
            return new RegisterValue(obisCode,new Quantity(o.getValue(),o.getScalerUnit().getUnit()));
        }
        throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
    }
    
    private boolean isBillingActiveImportEnergyRegister(ObisCode obisCode) {
        if ((obisCode.getA() == 1) && (obisCode.getB() == 0) &&  (obisCode.getC() == 1) && (obisCode.getD() == 8) && (obisCode.getF() != 255) &&
            (obisCode.getE() >=0) && (obisCode.getE()<=6)) 
            return true;
        else
            return false;
    }
    
    private RegisterValue getBillingActiveImportEnergyRegister(ObisCode obisCode) throws IOException {
        if ((obisCode.getA() == 1) && (obisCode.getB() == 0) &&  (obisCode.getC() == 1) && (obisCode.getD() == 8) && (obisCode.getF() != 255) &&
            (obisCode.getE() >=1) && (obisCode.getE()<=6)) {
            
            int readDays;
            if (obisCode.getF() == 0) {
                readDays = 1;
            }
            else if (obisCode.getF() < 0) {
                readDays = Math.abs(obisCode.getF());
            }
            else {
                readDays = 255 - obisCode.getF();
            }
            
            Calendar cal = ProtocolUtils.getCalendar(concentrator.getTimeZone());
            cal.add(Calendar.DATE, -1*readDays);
            
            if (pLCCMeterDailyEnergyValueProfile == null) {
                pLCCMeterDailyEnergyValueProfile = concentrator.getPLCCObjectFactory().getPLCCMeterDailyEnergyValueProfile(cal.getTime());
            }
            else {
                if (pLCCMeterDailyEnergyValueProfile.getDailyBillingEntries().size() < readDays)
                    pLCCMeterDailyEnergyValueProfile = concentrator.getPLCCObjectFactory().getPLCCMeterDailyEnergyValueProfile(cal.getTime());
            }
            
            if (pLCCMeterDailyEnergyValueProfile.getDailyBillingEntries().size()>0) {
                if (pLCCMeterDailyEnergyValueProfile.getDailyBillingEntries().size()<=(readDays-1))
                    throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
                else {
                    DailyBillingEntry dbe = (DailyBillingEntry)pLCCMeterDailyEnergyValueProfile.getDailyBillingEntries().get(readDays-1); 
                    if (dbe.getValues().length <= (obisCode.getE()-1))
                       throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
                    else 
                       return new RegisterValue(obisCode,new Quantity(dbe.getValues()[obisCode.getE()-1],Unit.get("Wh")),null,dbe.getCalendar().getTime());
                }
            }
        }
        throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
    }
    private boolean isBillingStatusRegister(ObisCode obisCode) {
        if ((obisCode.getA() == 0) && (obisCode.getB() == 0) &&  (obisCode.getC() == 96) && (obisCode.getD() == 5) && (obisCode.getF() != 255) &&
            (obisCode.getE() ==0)) 
            return true;
        else
            return false;
    }
    
    private RegisterValue getBillingStatusRegister(ObisCode obisCode) throws IOException {
        if ((obisCode.getA() == 0) && (obisCode.getB() == 0) &&  (obisCode.getC() == 96) && (obisCode.getD() == 5) && (obisCode.getF() != 255) &&
            (obisCode.getE() ==0)) {
            
            int readDays;
            if (obisCode.getF() == 0) {
                readDays = 1;
            }
            else if (obisCode.getF() < 0) {
                readDays = Math.abs(obisCode.getF())+1;
            }
            else {
                readDays = 255 - obisCode.getF();
            }
            
            Calendar cal = ProtocolUtils.getCalendar(concentrator.getTimeZone());
            cal.add(Calendar.DATE, -1*readDays);
            
            if (pLCCMeterDailyEnergyValueProfile == null) {
                pLCCMeterDailyEnergyValueProfile = concentrator.getPLCCObjectFactory().getPLCCMeterDailyEnergyValueProfile(cal.getTime());
            }
            else {
                if (pLCCMeterDailyEnergyValueProfile.getDailyBillingEntries().size() < readDays)
                    pLCCMeterDailyEnergyValueProfile = concentrator.getPLCCObjectFactory().getPLCCMeterDailyEnergyValueProfile(cal.getTime());
            }
            
            DailyBillingEntry dbe = (DailyBillingEntry)pLCCMeterDailyEnergyValueProfile.getDailyBillingEntries().get(readDays-1);             
            
            return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(dbe.getStatus()),Unit.get("")),null,dbe.getCalendar().getTime());
        }
        throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
    }
    
}
