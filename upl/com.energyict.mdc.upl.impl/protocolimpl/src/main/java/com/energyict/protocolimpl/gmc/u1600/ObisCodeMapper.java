/*
 * ObisCodeMapper.java
 *
 * Created on 15 juli 2004, 8:49
 */

package com.energyict.protocolimpl.gmc.u1600;

import com.energyict.mdc.upl.NoSuchRegisterException;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;

/**
 * @author Koen
 */
public class ObisCodeMapper {

    private final LogicalAddressFactory laf;

    public ObisCodeMapper(LogicalAddressFactory laf) {
        this.laf = laf;
    }

    public static RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(null);
        return (RegisterInfo) ocm.doGetRegister(obisCode, false);
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        return (RegisterValue) doGetRegister(obisCode, true);
    }

    private Object doGetRegister(ObisCode obisCode, boolean read) throws IOException {
        RegisterValue registerValue = null;
        int billingPoint;
        StringBuilder builder = new StringBuilder();

        // obis F code
        if ((obisCode.getF() >= 0) && (obisCode.getF() <= 99)) {
            billingPoint = obisCode.getF();
        } else if ((obisCode.getF() <= 0) && (obisCode.getF() >= -99)) {
            billingPoint = obisCode.getF() * -1;
        } else if (obisCode.getF() == 255) {
            billingPoint = -1;
        } else {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        }

        // *********************************************************************************
        // General purpose ObisRegisters & abstract general service
    /*    if (obisCode.toString().indexOf("1.1.0.1.0.255") != -1) { // billing counter
            if (read) {
                registerValue = new RegisterValue(obisCode,
                                                  new Quantity(new BigDecimal(laf.getGeneralMeterData().getNrOfMDResets()),Unit.get("")));
                return registerValue;
            }
            else return new RegisterInfo("billing counter");
        } // billing counter
        else if (obisCode.toString().indexOf("1.1.0.1.2.") != -1) { // billing point timestamp
            if ((billingPoint >= 0) && (billingPoint < 99)) {
                if (read) {
                   HistoricalData hd = laf.getHistoricalData(billingPoint+1);
                   registerValue = new RegisterValue(obisCode,
                                                     hd.getBillingDate());
                   return registerValue;
                }
                else return new RegisterInfo("billing point "+billingPoint+" timestamp");
            }
            else throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
        } // // billing point timestamp
      */

        // *********************************************************************************
        // Electricity related ObisRegisters
        // verify a & b
        if ((obisCode.getA() != 1) || ((obisCode.getB() < 1) && (obisCode.getB() > 64))) {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        }

        // obis C code / KV 22072005 code changed
        if (!(obisCode.getC() == 82)) {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        }

        // T O T A L & R A T E (OBIC D field 'Time integral 1' DLMS UA 1000-1 ed.5 page 87/101)
        if (obisCode.getD() == ObisCode.CODE_D_TIME_INTEGRAL) {// time integral 1 TOTAL & RATE
            builder.append(EnergyTypeCode.getCompountInfoFromObisC(obisCode.getC(), true));

            if (read) {
                com.energyict.protocolimpl.gmc.u1600.TotalRegisters tr;


                if (billingPoint == -1) {
                    if ((obisCode.getB() >= 1) && (obisCode.getB() <= 64)) {
                        tr = laf.getTotalRegisters(obisCode.getB());
                        registerValue = new RegisterValue(obisCode,
                                tr.getValueforObisB(obisCode.getB()));
                    }
                    return registerValue;
                }
            } else {
                if (obisCode.getE() > 0) {
                    builder.append(", tariff register ").append(obisCode.getE());
                }
            }
        } else {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        }

        if (billingPoint == -1) {
            builder.append(", current value");
        } else {
            builder.append(", billing point ").append(billingPoint + 1);
        }

        if (read) {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        } else {
            return new RegisterInfo(builder.toString());
        }

    }

}