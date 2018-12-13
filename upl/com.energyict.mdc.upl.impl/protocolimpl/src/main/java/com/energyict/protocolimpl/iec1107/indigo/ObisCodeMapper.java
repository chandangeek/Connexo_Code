/*
 * ObisCodeMapper.java
 *
 * Created on 15 juli 2004, 8:49
 */

package com.energyict.protocolimpl.iec1107.indigo;

import com.energyict.mdc.upl.NoSuchRegisterException;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;
import java.math.BigDecimal;

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
        StringBuilder obisTranslation = new StringBuilder();

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
        if (obisCode.toString().contains("1.1.0.1.0.255")) { // billing counter
            if (read) {
                registerValue = new RegisterValue(obisCode,
                        new Quantity(new BigDecimal(laf.getGeneralMeterData().getNrOfMDResets()), Unit.get("")));
                return registerValue;
            } else {
                return new RegisterInfo("billing counter");
            }
        } // billing counter
        else if (obisCode.toString().contains("1.1.0.1.2.")) { // billing point timestamp
            if ((billingPoint >= 0) && (billingPoint < 99)) {
                if (read) {
                    HistoricalData hd = laf.getHistoricalData(billingPoint + 1);
                    registerValue = new RegisterValue(obisCode,
                            hd.getBillingDate());
                    return registerValue;
                } else {
                    return new RegisterInfo("billing point " + billingPoint + " timestamp");
                }
            } else {
                throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
            }
        } // // billing point timestamp


        // *********************************************************************************
        // Electricity related ObisRegisters
        // verify a & b
        if ((obisCode.getA() != 1) || ((obisCode.getB() != 1) && (obisCode.getB() != 2))) {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        }

        // obis C code
        if (obisCode.getC() == 1) {
        } else if (obisCode.getC() == 2) {
        } else if (obisCode.getC() == 3) {
        } else if (obisCode.getC() == 4) {
        } else if (obisCode.getC() == 5) {
        } else if (obisCode.getC() == 6) {
        } else if (obisCode.getC() == 7) {
        } else if (obisCode.getC() == 8) {
        } else if (obisCode.getC() == 9) {
        } else if (obisCode.getC() == 129) {
        } else {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        }

        // *************************************************************************************************************
        // C U M U L A T I V E  M A X I M U M  D E M A N D (OBIC D field 'Cumulative maximum 1' DLMS UA 1000-1 ed.5 page 87/101)
        if ((obisCode.getD() == ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND) && (obisCode.getB() == 1)) {
            if (read) {
                DemandRegisters dr;
                if (billingPoint == -1) {
                    dr = laf.getDemandRegisters();
                    registerValue = new RegisterValue(obisCode,
                            dr.getDemandValueforObisCAndD(obisCode.getC(), obisCode.getD()));
                } else {
                    dr = laf.getDemandRegisters(billingPoint + 1);
                    registerValue = new RegisterValue(obisCode,
                            dr.getDemandValueforObisCAndD(obisCode.getC(), obisCode.getD()),
                            dr.getBillingTimestamp(),
                            dr.getBillingTimestamp());
                }
                return registerValue;
            } else {
                obisTranslation.append(EnergyTypeCode.getCompountInfoFromObisC(obisCode.getC(), false));
                obisTranslation.append(", cumulative maximum demand");
            }
        }
        // *************************************************************************************************************
        // R I S I N G  D E M A N D (OBIC D field 'Current average 1' DLMS UA 1000-1 ed.5 page 87/101)
        else if ((obisCode.getD() == ObisCode.CODE_D_RISING_DEMAND) && (obisCode.getB() == 1)) {
            if (read) {
                DemandRegisters dr;
                if (billingPoint == -1) {
                    dr = laf.getDemandRegisters();
                    registerValue = new RegisterValue(obisCode,
                            dr.getDemandValueforObisCAndD(obisCode.getC(), obisCode.getD()));
                } else {
                    dr = laf.getDemandRegisters(billingPoint + 1);
                    registerValue = new RegisterValue(obisCode,
                            dr.getDemandValueforObisCAndD(obisCode.getC(), obisCode.getD()),
                            dr.getBillingTimestamp(),
                            dr.getBillingTimestamp());
                }
                return registerValue;
            } else {
                obisTranslation.append(EnergyTypeCode.getCompountInfoFromObisC(obisCode.getC(), false));
                obisTranslation.append(", current average");
            }
        }
        // *************************************************************************************************************
        // M A X I M U M  D E M A N D (OBIC D field 'Maximum 1' DLMS UA 1000-1 ed.5 page 87/101)
        else if ((obisCode.getD() == ObisCode.CODE_D_MAXIMUM_DEMAND) && (obisCode.getB() == 1)) {
            if (read) {
                DemandRegisters dr;
                if (billingPoint == -1) {
                    dr = laf.getDemandRegisters();
                    registerValue = new RegisterValue(obisCode,
                            dr.getDemandValueforObisCAndD(obisCode.getC(), obisCode.getD()),
                            dr.getDemandTimestampforObisCAndD(obisCode.getC(), obisCode.getD()));
                } else {
                    dr = laf.getDemandRegisters(billingPoint + 1);
                    registerValue = new RegisterValue(obisCode,
                            dr.getDemandValueforObisCAndD(obisCode.getC(), obisCode.getD()),
                            dr.getBillingTimestamp(),
                            dr.getDemandTimestampforObisCAndD(obisCode.getC(), obisCode.getD()));
                }
                return registerValue;
            } else {
                obisTranslation.append(EnergyTypeCode.getCompountInfoFromObisC(obisCode.getC(), false));
                obisTranslation.append(", maximum demand");
            }

        }
        // *************************************************************************************************************
        // T O T A L & R A T E (OBIC D field 'Time integral 1' DLMS UA 1000-1 ed.5 page 87/101)
        else if (obisCode.getD() == ObisCode.CODE_D_TIME_INTEGRAL) {// time integral 1 TOTAL & RATE
            obisTranslation.append(EnergyTypeCode.getCompountInfoFromObisC(obisCode.getC(), true));

            if (read) {
                if ((obisCode.getE() > 0) && (obisCode.getB() == 1)) {
                    RateRegisters rr;
                    if (billingPoint == -1) {
                        rr = laf.getRateRegisters();
                        registerValue = new RegisterValue(obisCode,
                                rr.getRateValueforObisCandE(obisCode.getC(), obisCode.getE()));
                    } else {
                        rr = laf.getRateRegisters(billingPoint + 1);
                        registerValue = new RegisterValue(obisCode,
                                rr.getRateValueforObisCandE(obisCode.getC(), obisCode.getE()),
                                rr.getBillingTimestamp(),
                                rr.getBillingTimestamp());
                    }
                    return registerValue;
                } else {
                    TotalRegisters tr;
                    DefaultRegisters dr;
                    if (billingPoint == -1) {
                        if (obisCode.getB() == 1) {
                            tr = laf.getTotalRegisters();
                            registerValue = new RegisterValue(obisCode,
                                    tr.getTotalValueforObisC(obisCode.getC()));
                        } else if (obisCode.getB() == 2) { // get the default registers
                            dr = laf.getDefaultRegisters();
                            registerValue = new RegisterValue(obisCode,
                                    dr.getTotalValueforObisC(obisCode.getC()));
                        }
                    } else {
                        if (obisCode.getB() == 1) {
                            tr = laf.getTotalRegisters(billingPoint + 1);
                            registerValue = new RegisterValue(obisCode,
                                    tr.getTotalValueforObisC(obisCode.getC()),
                                    tr.getBillingTimestamp(),
                                    tr.getBillingTimestamp());
                        } else if (obisCode.getB() == 2) { // get the default registers
                            dr = laf.getDefaultRegisters(billingPoint + 1);
                            registerValue = new RegisterValue(obisCode,
                                    dr.getTotalValueforObisC(obisCode.getC()),
                                    dr.getBillingTimestamp(),
                                    dr.getBillingTimestamp());
                        }
                    }
                    return registerValue;
                }
            } else {
                if (obisCode.getE() > 0) {
                    obisTranslation.append(", tariff register ").append(obisCode.getE());
                }
            }
        } else {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        }

        if (billingPoint == -1) {
            obisTranslation.append(", current value");
        } else {
            obisTranslation.append(", billing point ").append(billingPoint + 1);
        }

        if (read) {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        } else {
            return new RegisterInfo(obisTranslation.toString());
        }

    }

}