/*
 * ObisCodeMapper.java
 *
 * Created on 19 mei 2005, 17:19
 */

package com.energyict.protocolimpl.emon.ez7.core;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.ObisUtils;
import com.energyict.protocolimpl.emon.ez7.core.command.GenericValue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author  Koen
 */
public class ObisCodeMapper {



    EZ7CommandFactory ez7CommandFactory;

    /** Creates a new instance of ObisCodeMapper */
    public ObisCodeMapper(EZ7CommandFactory ez7CommandFactory) {
        this.ez7CommandFactory=ez7CommandFactory;
    }

    static public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(null);
        return (RegisterInfo)ocm.doGetRegister(obisCode,false);
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        return (RegisterValue)doGetRegister(obisCode, true);
    }

    private Object doGetRegister(ObisCode obisCode, boolean read) throws IOException {

        RegisterValue registerValue=null;
        String registerName=null;
        int billingPoint=-1;
        Unit unit = null;

        // *********************************************************************************
        // Manufacturer specific
        if (ObisUtils.isManufacturerSpecific(obisCode)) {
            if (read) {
                GenericValue genv = ez7CommandFactory.getGenericValue(obisCode.getB());
                if (genv==null)
                    throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported! No command for B field "+obisCode.getB());
                else {
                    int value = genv.getValue(obisCode.getF(), obisCode.getE());
                    if (value != -1) {
                        registerValue = new RegisterValue(obisCode,
                                                          new Quantity(BigDecimal.valueOf((long)value).multiply(ez7CommandFactory.ez7.getAdjustRegisterMultiplier()),Unit.get("")),
                                                          null, // eventtime
                                                          null, // fromtime
                                                          null, // totime
                                                          new Date(), // readtime
                                                          0, // registerid
                                                          "0x"+Integer.toHexString(value)); // text
                    }
                    else throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
                }
            }
            else return new RegisterInfo("manufacturer specific ObisCode");
        } // if (ObisUtils.isManufacturerSpecific(obisCode))
        // obis F code
        else if (obisCode.getF() != 255)
            throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported! No billing points supported!");
        // *********************************************************************************
        // Electricity related ObisRegisters
        else if ((obisCode.getA() == 1) && (obisCode.getB() >= 1) && (obisCode.getB() <= 8)) {
            if ((obisCode.getC() == 1) && (obisCode.getE() >= 1) && (obisCode.getE() <= 8)) { // active import
                // *********************************************************************************
                // kWh cumulative energy
                if (obisCode.getD() == 8) {
                    if (read) {
                       Quantity q = ez7CommandFactory.getAllEnergy().getQuantity(obisCode.getB()-1, obisCode.getE()-1);
                       Quantity quantity = new Quantity(q.getAmount().multiply(ez7CommandFactory.ez7.getAdjustRegisterMultiplier()),q.getUnit());
                       if (quantity != null) {
                            registerValue = new RegisterValue(obisCode,quantity);
                       }
                       else throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
                    }
                    else return new RegisterInfo(obisCode.getDescription());
                }
                // kW maximum demand
                else if (obisCode.getD() == 6) {
                    if (read) {
                       Quantity q = ez7CommandFactory.getAllMaximumDemand().getQuantity(obisCode.getB()-1, obisCode.getE()-1);
                       Quantity quantity = new Quantity(q.getAmount().multiply(ez7CommandFactory.ez7.getAdjustRegisterMultiplier()),q.getUnit());
                       Date date = ez7CommandFactory.getAllMaximumDemand().getDate(obisCode.getB()-1, obisCode.getE()-1);
                       if (date != null) {
                            registerValue = new RegisterValue(obisCode,quantity,date);
                       }
                       else throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
                    }
                    else return new RegisterInfo(obisCode.getDescription());
                }
                // kW sliding demand
                else if (obisCode.getD() == 5) {
                    if (read) {
                       Quantity q = ez7CommandFactory.getSlidingKWDemands().getQuantity(obisCode.getB()-1, obisCode.getE()-1);
                       Quantity quantity = new Quantity(q.getAmount().multiply(ez7CommandFactory.ez7.getAdjustRegisterMultiplier()),q.getUnit());
                       if (quantity != null) {
                            registerValue = new RegisterValue(obisCode,quantity);
                       }
                       else throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
                    }
                    else return new RegisterInfo(obisCode.getDescription()+"(last-"+(obisCode.getE()-1)+" 5 minute sliding demand interval)");
                }
            }
        }
        else if ((obisCode.getA() == 1) && (obisCode.getB() == 0)) {
            // instantaneous values
            if ((obisCode.getD() == 7) && (obisCode.getE() == 0)) {
                if (read) {
                    Quantity quantity=null;
                    // Current
                    if ((obisCode.getC() == 11) || (obisCode.getC() == 31) || (obisCode.getC() == 51) || (obisCode.getC() == 71)) {
                       quantity = ez7CommandFactory.getPowerQuality().getAmperage((obisCode.getC()-11)/20);
                    }
                    // Voltage
                    else if ((obisCode.getC() == 12) || (obisCode.getC() == 32) || (obisCode.getC() == 52) || (obisCode.getC() == 72)) {
                       quantity = ez7CommandFactory.getPowerQuality().getVoltage((obisCode.getC()-12)/20);
                    }
                    // Power factor
                    else if ((obisCode.getC() == 13) || (obisCode.getC() == 33) || (obisCode.getC() == 53) || (obisCode.getC() == 73)) {
                       quantity = ez7CommandFactory.getPowerQuality().getPowerFactor((obisCode.getC()-13)/20);
                    }
                    // kW load
                    else if ((obisCode.getC() == 1) || (obisCode.getC() == 21) || (obisCode.getC() == 41) || (obisCode.getC() == 61)) {
                       quantity = ez7CommandFactory.getPowerQuality().getKwLoad((obisCode.getC()-1)/20);
                    }
                    // Frequency
                    else if ((obisCode.getC() == 14) || (obisCode.getC() == 34) || (obisCode.getC() == 54) || (obisCode.getC() == 74)) {
                       quantity = ez7CommandFactory.getPowerQuality().getFrequency((obisCode.getC()-14)/20);
                    }
                    else if (obisCode.getC() == 81) {
                        if (obisCode.getE() == 40)
                            quantity = ez7CommandFactory.getPowerQuality().getPhaseAngle(0);
                        if (obisCode.getE() == 51)
                            quantity = ez7CommandFactory.getPowerQuality().getPhaseAngle(1);
                        if (obisCode.getE() == 62)
                            quantity = ez7CommandFactory.getPowerQuality().getPhaseAngle(2);
                    }

                    if (quantity != null)
                        registerValue = new RegisterValue(obisCode,quantity);
                    else
                        throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
                }
                else return new RegisterInfo(obisCode.getDescription());
            }
        }

        if ((read) && (registerValue != null))
            return registerValue;
        else
            throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
    }
}
