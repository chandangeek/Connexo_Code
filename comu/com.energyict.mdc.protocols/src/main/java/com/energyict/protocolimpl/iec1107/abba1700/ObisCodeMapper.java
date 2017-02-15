/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ObisCodeMapper.java
 *
 * Created on 11 juni 2004, 13:55
 */

package com.energyict.protocolimpl.iec1107.abba1700;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.protocolimpl.iec1107.abba1700.counters.ProgrammingCounter;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.energyict.protocolimpl.iec1107.abba1700.ABBA1700RegisterFactory.BatterySupportStatusKey;
import static com.energyict.protocolimpl.iec1107.abba1700.ABBA1700RegisterFactory.CurrentTransformerRatioPrimary;
import static com.energyict.protocolimpl.iec1107.abba1700.ABBA1700RegisterFactory.CurrentTransformerRatioSecondary;
import static com.energyict.protocolimpl.iec1107.abba1700.ABBA1700RegisterFactory.HistoricalValuesKey;
import static com.energyict.protocolimpl.iec1107.abba1700.ABBA1700RegisterFactory.ProgrammingCounterKey;
import static com.energyict.protocolimpl.iec1107.abba1700.ABBA1700RegisterFactory.SerialNumberKey;
import static com.energyict.protocolimpl.iec1107.abba1700.ABBA1700RegisterFactory.VoltageTransformerRatioPrimary;
import static com.energyict.protocolimpl.iec1107.abba1700.ABBA1700RegisterFactory.VoltageTransformerRatioSecondary;

/**
 * @author Koen
 */
public class ObisCodeMapper {

    ABBA1700RegisterFactory abba1700RegisterFactory;

    /**
     * Creates a new instance of ObisCodeMapping
     */
    public ObisCodeMapper(ABBA1700RegisterFactory abba1700RegisterFactory) {
        this.abba1700RegisterFactory = abba1700RegisterFactory;
    }


    public static RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(null);
        return (RegisterInfo) ocm.doGetRegister(obisCode, false);
    }

//    public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
//        return (RegisterInfo)doGetRegister(obisCode,false);
//    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        return (RegisterValue) doGetRegister(obisCode, true);
    }


    private boolean isWithinRange(int id, int c) {
        if ((c == id) || (c == (id + 20)) || (c == (id + 40))) {
            return true;
        } else {
            return false;
        }
    }

    private Object doGetRegister(ObisCode obisCode, boolean read) throws IOException {
        RegisterValue registerValue = null;
        String registerName = null;
        int billingPoint = -1;
        StringBuffer obisTranslation = new StringBuffer();
        Unit unit = null;

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
        if (obisCode.toString().indexOf("1.1.0.1.0.255") != -1) { // billing counter
            if (read) {
                HistoricalValueSetInfo hvsi = ((HistoricalValues) abba1700RegisterFactory.getRegister(HistoricalValuesKey, 0)).getHistoricalValueSetInfo();
                registerValue = new RegisterValue(obisCode, new Quantity(new BigDecimal(hvsi.getBillingCount()), Unit.get("")));
                return registerValue;
            } else {
                return new RegisterInfo("billing counter");
            }
        } // billing counter
        else if (obisCode.toString().indexOf("1.1.0.1.2.") != -1) { // billing point timestamp
            if ((billingPoint >= 0) && (billingPoint < 99)) {
                if (read) {
                    HistoricalValueSetInfo hvsi = ((HistoricalValues) abba1700RegisterFactory.getRegister(HistoricalValuesKey, billingPoint)).getHistoricalValueSetInfo();
                    //registerValue = new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(hvsi.getBillingEndDateTime().getTime()/1000),Unit.get(255)),hvsi.getBillingResetDateTime(),hvsi.getBillingStartDateTime(),hvsi.getBillingEndDateTime());
                    registerValue = new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(hvsi.getBillingTriggerSource()), Unit.get(255)), hvsi.getBillingResetDateTime(), hvsi.getBillingStartDateTime(), hvsi.getBillingEndDateTime());
                    return registerValue;
                } else {
                    return new RegisterInfo("billing point " + billingPoint + " timestamp");
                }
            } else {
                throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
            }
        } // // billing point timestamp
        else if (obisCode.toString().indexOf("0.0.96.50.0.255") != -1) { // current system status
            if (read) {
                SystemStatus ss = (SystemStatus) abba1700RegisterFactory.getRegister("SystemStatus");
                registerValue = new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(ss.getValue()), Unit.get(255)));
                return registerValue;
            } else {
                return new RegisterInfo("Current System Status (32 bit word)");
            }
        } else if (obisCode.toString().indexOf("0.0.96.51.0.255") != -1) { // historical system status
            if (read) {
                SystemStatus ss = (SystemStatus) abba1700RegisterFactory.getRegister("HistoricalSystemStatus");
                registerValue = new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(ss.getValue()), Unit.get(255)));
                return registerValue;
            } else {
                return new RegisterInfo("Historical System Status (32 bit word)");
            }
        } else if ((obisCode.toString().indexOf("1.1.0.4.2.255") != -1) || (obisCode.toString().indexOf("1.0.0.4.2.255") != -1)) { // CT numerator
            if (read) {
                BigDecimal ctPrimary = (BigDecimal) abba1700RegisterFactory.getRegister(CurrentTransformerRatioPrimary);
                BigDecimal ctSecondary = (BigDecimal) abba1700RegisterFactory.getRegister(CurrentTransformerRatioSecondary);
                BigDecimal ctRatio = ctPrimary.divide(ctSecondary);
                registerValue = new RegisterValue(obisCode, new Quantity(ctRatio, Unit.get(255)), null, null, null, new Date(), -1, "Primary : " + ctPrimary + " - Secondary: " + ctSecondary);
                return registerValue;
            } else {
                return new RegisterInfo("CT numerator");
            }
        } else if ((obisCode.toString().indexOf("1.1.0.4.3.255") != -1) || (obisCode.toString().indexOf("1.0.0.4.3.255") != -1)) { // VT numerator
            if (read) {
                BigDecimal vtPrimary = (BigDecimal) abba1700RegisterFactory.getRegister(VoltageTransformerRatioPrimary);
                BigDecimal vtSecondary = (BigDecimal) abba1700RegisterFactory.getRegister(VoltageTransformerRatioSecondary);
                BigDecimal vtRatio = vtPrimary.divide(vtSecondary);
                registerValue = new RegisterValue(obisCode, new Quantity(vtRatio, Unit.get(255)), null, null, null, new Date(), -1, "Primary : " + vtPrimary + " - Secondary: " + vtSecondary);
                return registerValue;
            } else {
                return new RegisterInfo("VT numerator");
            }

            //TODO these are not correct, the primary and secondary are not stored in these obiscodes
//        } else if ((obisCode.toString().indexOf("1.1.0.4.5.255") != -1) || (obisCode.toString().indexOf("1.0.0.4.5.255") != -1)) { // CT denominator
//            if (read) {
//                BigDecimal bd = (BigDecimal) abba1700RegisterFactory.getRegister("CTSecundary");
//                registerValue = new RegisterValue(obisCode, new Quantity(bd, Unit.get(255)));
//                return registerValue;
//            } else {
//                return new RegisterInfo("CT denominator");
//            }
//        } else if ((obisCode.toString().indexOf("1.1.0.4.6.255") != -1) || (obisCode.toString().indexOf("1.0.0.4.6.255") != -1)) { // VT denominator
//            if (read) {
//                BigDecimal bd = (BigDecimal) abba1700RegisterFactory.getRegister("VTSecundary");
//                registerValue = new RegisterValue(obisCode, new Quantity(bd, Unit.get(255)));
//                return registerValue;
//            } else {
//                return new RegisterInfo("VT denominator");
//            }
        } else if (obisCode.toString().indexOf("1.0.0.0.1.255") != -1) { // SchemeID
            if (read) {
                String schemeId = (String) abba1700RegisterFactory.getRegister("SchemeID");
                registerValue = new RegisterValue(obisCode, schemeId);
                return registerValue;
            } else {
                return new RegisterInfo("SchemeID");
            }
        } else if (obisCode.toString().indexOf("0.0.96.1.0.255") != -1) { // Meter SerialNumber
            if (read) {
                String schemeId = (String) abba1700RegisterFactory.getRegister(SerialNumberKey);
                registerValue = new RegisterValue(obisCode, schemeId);
                return registerValue;
            } else {
                return new RegisterInfo(SerialNumberKey);
            }
        } else if (obisCode.toString().indexOf("0.0.96.1.5.255") != -1) {
            if (read) {
                registerValue = new RegisterValue(obisCode, abba1700RegisterFactory.getMeterType().getFirmwareVersion());
                return registerValue;
            } else {
                return new RegisterInfo("FirmwareVersion");
            }
        } else if (obisCode.toString().indexOf("0.0.96.1.4.255") != -1) {   //ProgrammingCounter
            if (read) {
                ProgrammingCounter pc = (ProgrammingCounter) abba1700RegisterFactory.getRegister(ProgrammingCounterKey);
                registerValue = new RegisterValue(obisCode, new Quantity(new BigDecimal(pc.getCounter()),Unit.getUndefined()), pc.getMostRecentEventTime());
                return registerValue;
            } else {
                return new RegisterInfo(ProgrammingCounterKey);
            }
        } else if (obisCode.toString().indexOf("0.0.96.6.0.255") != -1) {   // Battery Status
            if(read){
                BatterySupportStatus bss = (BatterySupportStatus) abba1700RegisterFactory.getRegister(BatterySupportStatusKey);
                registerValue = new RegisterValue(obisCode, new Quantity(new BigDecimal(bss.getRemainingBatterySupportTime()), Unit.get(BaseUnit.DAY)));
                return registerValue;
            } else {
                return new RegisterInfo(BatterySupportStatusKey);
            }
        }

        // *********************************************************************************
        // Electricity related ObisRegisters
        // verify a & b
        if ((obisCode.getA() != 1) || (obisCode.getB() < 0) || (obisCode.getB() > 3)) {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        }
        //else setTriggerChannel(obisCode.getB()-1);

        // Check if instantaneous value
        if ((obisCode.getD() == 7) && (billingPoint == -1)) {
            int valId = 0;
            int phase = ABBA1700InstantaneousValues.PHASE_TOTAL;
            if ((obisCode.getC() >= 21) && (obisCode.getC() < 41) || (obisCode.getE() == 70)) {
                phase = ABBA1700InstantaneousValues.PHASE_A;
            } else if ((obisCode.getC() >= 41) && (obisCode.getC() < 61) || (obisCode.getE() == 71)) {
                phase = ABBA1700InstantaneousValues.PHASE_B;
            } else if ((obisCode.getC() >= 61) && (obisCode.getC() < 81) || (obisCode.getE() == 72)) {
                phase = ABBA1700InstantaneousValues.PHASE_C;
            } else if ((obisCode.getC() >= 141) && (obisCode.getC() < 161) || (obisCode.getE() == 70)) {
                phase = ABBA1700InstantaneousValues.PHASE_A;
            } else if ((obisCode.getC() >= 161) && (obisCode.getC() < 181) || (obisCode.getE() == 71)) {
                phase = ABBA1700InstantaneousValues.PHASE_B;
            } else if ((obisCode.getC() >= 181) && (obisCode.getC() < 81) || (obisCode.getE() == 72)) {
                phase = ABBA1700InstantaneousValues.PHASE_C;
            }

            if (obisCode.getE() == 0) {
                if (isWithinRange(31, obisCode.getC())) {
                    valId = ABBA1700InstantaneousValues.RMS_CURRENT;
                } else if (isWithinRange(151, obisCode.getC())) {
                    valId = ABBA1700InstantaneousValues.RMS_CURRENT_SCALE;
                } else if (isWithinRange(32, obisCode.getC())) {
                    valId = ABBA1700InstantaneousValues.RMS_VOLTAGE;
                } else if (isWithinRange(152, obisCode.getC())) {
                    valId = ABBA1700InstantaneousValues.RMS_VOLTAGE_SCALE;
                } else if ((isWithinRange(21, obisCode.getC())) || (obisCode.getC() == 1)) {
                    valId = ABBA1700InstantaneousValues.ACTIVE_POWER;
                } else if ((isWithinRange(141, obisCode.getC())) || (obisCode.getC() == 200)) {
                    valId = ABBA1700InstantaneousValues.ACTIVE_POWER_SCALED;
                } else if ((isWithinRange(23, obisCode.getC())) || (obisCode.getC() == 3)) {
                    valId = ABBA1700InstantaneousValues.REACTIVE_POWER;
                } else if ((isWithinRange(143, obisCode.getC())) || (obisCode.getC() == 201)) {
                    valId = ABBA1700InstantaneousValues.REACTIVE_POWER_SCALED;
                } else if ((isWithinRange(144, obisCode.getC())) || (obisCode.getC() == 9)) {
                    valId = ABBA1700InstantaneousValues.APPARENT_POWER;
                } else if ((isWithinRange(145, obisCode.getC())) || (obisCode.getC() == 202)) {
                    valId = ABBA1700InstantaneousValues.APPARENT_POWER_SCALED;
                } else if ((isWithinRange(33, obisCode.getC())) || (obisCode.getC() == 13)) {
                    valId = ABBA1700InstantaneousValues.POWER_FACTOR;
                } else if (isWithinRange(34, obisCode.getC())) {
                    valId = ABBA1700InstantaneousValues.FREQUENCY;
                }
            } // if (obisCode.getD() == 0)
            else {
                if (obisCode.getC() == 81) {
                    valId = ABBA1700InstantaneousValues.PHASE_ANGLE;
                }
            }

            ABBA1700InstantaneousValues aiv = new ABBA1700InstantaneousValues(abba1700RegisterFactory);
            if (read) {
                return new RegisterValue(obisCode, aiv.getInstantaneousValue(phase, valId));
            } else {
                if (valId == 0) {
                    throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
                } else {
                    return new RegisterInfo(aiv.getInstantaneousValueDescription(phase, valId));
                }
            }


        } // if ((obisCode.getD() == 7) && (billingPoint == -1))


        // obis C code
        if (obisCode.getC() == 1) {
            registerName = "CummMainImport";
        } else if (obisCode.getC() == 2) {
            registerName = "CummMainExport";
        } else if (obisCode.getC() == 5) {
            registerName = "CummMainQ1";
        } else if (obisCode.getC() == 6) {
            registerName = "CummMainQ2";
        } else if (obisCode.getC() == 7) {
            registerName = "CummMainQ3";
        } else if (obisCode.getC() == 8) {
            registerName = "CummMainQ4";
        } else if (obisCode.getC() == 9) {
            registerName = "CummMainVA";
        } else if (obisCode.getC() == 128) {
            if (read) {
                CustDefRegConfig cdrc = (CustDefRegConfig) abba1700RegisterFactory.getRegister("CustDefRegConfig");
                unit = EnergyTypeCode.getUnitFromRegSource(cdrc.getRegSource(0), true);
            }
            registerName = "CummMainCustDef1";
        } else if (obisCode.getC() == 129) {
            if (read) {
                CustDefRegConfig cdrc = (CustDefRegConfig) abba1700RegisterFactory.getRegister("CustDefRegConfig");
                unit = EnergyTypeCode.getUnitFromRegSource(cdrc.getRegSource(1), true);
            }
            registerName = "CummMainCustDef2";
        } else if (obisCode.getC() == 130) {
            if (read) {
                CustDefRegConfig cdrc = (CustDefRegConfig) abba1700RegisterFactory.getRegister("CustDefRegConfig");
                unit = EnergyTypeCode.getUnitFromRegSource(cdrc.getRegSource(2), true);
            }
            registerName = "CummMainCustDef3";
        } else if (obisCode.getC() == 131) {
            registerName = "ExternalInput1";
        } else if (obisCode.getC() == 132) {
            registerName = "ExternalInput2";
        } else if (obisCode.getC() == 133) {
            registerName = "ExternalInput3";
        } else if (obisCode.getC() == 134) {
            registerName = "ExternalInput4";
        } else {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        }


        // obis D code
        if (obisCode.getD() == 2) {// cumulative maximum 1  CMD
            // search for right unit (registersource)...
            if (read) {
                for (int i = 0; i < ABBA1700RegisterFactory.MAX_CMD_REGS; i++) {
                    if ((((obisCode.getE() == 0) || (obisCode.getE() == 1)) && (i == 0)) ||
                            ((obisCode.getE() > 1) && ((i + 1) == obisCode.getE()))) {

                        CumulativeMaximumDemand cmd = (CumulativeMaximumDemand) abba1700RegisterFactory.getRegister("CumulativeMaximumDemand" + i, billingPoint);
                        // energytype code match
                        if (cmd.getRegSource() == EnergyTypeCode.getRegSourceFromObisCCode(obisCode.getC())) {
                            if (unit != null) // in case of customer defined registers unit is defined earlier
                            {
                                cmd.setQuantity(new Quantity(cmd.getQuantity().getAmount(), unit.getFlowUnit()));
                            }
                            registerValue = new RegisterValue(obisCode,
                                    cmd.getQuantity(),
                                    cmd.getHistoricalValueSetInfo().getBillingResetDateTime(),
                                    cmd.getHistoricalValueSetInfo().getBillingStartDateTime(),
                                    cmd.getHistoricalValueSetInfo().getBillingEndDateTime());
                            return registerValue;
                        }
                    }
                }
            } else {
                obisTranslation.append(EnergyTypeCode.getCompountInfoFromObisC(obisCode.getC(), false));
                obisTranslation.append(", cumulative maximum demand");
            }

        } else if (obisCode.getD() == 6) {// maximum 1 MD
            // search for right unit (registersource)...

            /*
            *   1a 1b 1c
            *   2a 2b 2c
            *   ...
            *   Search in the 1a, 2a,... values to match the energytype code. Then search for the highest value of the 3.
            *   We suppost that the energytype code is the same for all 3 MD registers which is not always so. When the
            *   configuration changes, it is possible that the energytype code changes in the registers.
            *   Generate an NoSuchRegisterException wanneer het energytype verschillend is in het te lezen register!
            *   Gebruik de Obis B code om te bepalen welk van de 3 registers je wil lezen.
            *
            */

            if (read) {
                for (int i = 0; i < ABBA1700RegisterFactory.MAX_MD_REGS; i += 3) {
                    if ((((obisCode.getE() == 0) || (obisCode.getE() == 1)) && (i == 0)) ||
                            ((obisCode.getE() > 1) && (((i / 3) + 1) == obisCode.getE()))) {

                        //MaximumDemand md = (MaximumDemand)abba1700RegisterFactory.getRegister("MaximumDemand"+(i+obisCode.getB()-1),billingPoint);
                        List mds = new ArrayList();
                        for (int j = 0; j < 3; j++) {
                            mds.add((MaximumDemand) abba1700RegisterFactory.getRegister("MaximumDemand" + (i + j), billingPoint));
                        }
                        // sort in accending datetime
                        MaximumDemand.sortOnDateTime(mds);
                        // energytype code match with the maximumdemand register with the most
                        // recent datetime stamp.
                        MaximumDemand md = (MaximumDemand) mds.get(2);
                        if (md.getRegSource() == EnergyTypeCode.getRegSourceFromObisCCode(obisCode.getC())) {

                            // Sort in accending quantity. If not all 3 energytype codes are
                            // the same, an IOException is thrown.
                            MaximumDemand.sortOnQuantity(mds);
                            int index = 3 - obisCode.getB();
                            if ((index < 0) || (index >= mds.size())) {
                                throw new NoSuchRegisterException("B field should be 1-3 but was [" + obisCode.getB() + "] for register with obiscode [" + obisCode + "]");
                            }
                            md = (MaximumDemand)mds.get(index); // B=1 => get(2), B=2 => get(1), B=3 => get(0)
                            if (unit != null) // in case of customer defined registers unit is defined earlier
                            {
                                md.setQuantity(new Quantity(md.getQuantity().getAmount(), unit.getFlowUnit()));
                            }
                            registerValue = new RegisterValue(obisCode, md.getQuantity(),
                                    md.getDateTime(),
                                    md.getHistoricalValueSetInfo().getBillingStartDateTime(),
                                    md.getHistoricalValueSetInfo().getBillingEndDateTime());
                            return registerValue;
                        } // if (md.getRegSource() == EnergyTypeCode.getRegSourceFromObisCCode(obisCode.getC()))
                    } // for (int i=0;i<ABBA1700RegisterFactory.MAX_MD_REGS;i+=3)
                } // tariff check

                throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
            } else {
                obisTranslation.append(EnergyTypeCode.getCompountInfoFromObisC(obisCode.getC(), false));
                obisTranslation.append(", maximum demand");
            }

        } else if (obisCode.getD() == 8) {// time integral 1 TOTAL & RATE
            obisTranslation.append(EnergyTypeCode.getCompountInfoFromObisC(obisCode.getC(), true));

            if (read) {
                if (obisCode.getE() > 0) {
                    TariffSources ts;
                    if (billingPoint == -1) {
                        ts = (TariffSources) abba1700RegisterFactory.getRegister("TariffSources");
                    } else {
                        HistoricalDisplayScalings hds = (HistoricalDisplayScalings) abba1700RegisterFactory.getRegister("HistoricalDisplayScalings", billingPoint);
                        HistoricalDisplayScalingSet hdss = hds.getHistoricalDisplayScalingSet();
                        ts = hdss.getTariffSources();

                    }
                    registerName = "TimeOfUse" + (obisCode.getE() - 1);
                    MainRegister mr = (MainRegister) abba1700RegisterFactory.getRegister(registerName, billingPoint);
                    if (ts.getRegSource()[obisCode.getE() - 1] == EnergyTypeCode.getRegSourceFromObisCCode(obisCode.getC())) {
                        if (EnergyTypeCode.isCustomerDefined(ts.getRegSource()[obisCode.getE() - 1])) {
                            CustDefRegConfig cdrc = (CustDefRegConfig) abba1700RegisterFactory.getRegister("CustDefRegConfig");
                            unit = EnergyTypeCode.getUnitFromRegSource(EnergyTypeCode.getRegSourceFromObisCCode(obisCode.getC()), true);
                        } else {
                            unit = EnergyTypeCode.getUnitFromObisCCode(obisCode.getC(), true);
                        }
                        mr.setQuantity(new Quantity(mr.getQuantity().getAmount(), unit));
                        registerValue = new RegisterValue(obisCode, mr.getQuantity(), mr.getHistoricalValueSetInfo().getBillingResetDateTime(), mr.getHistoricalValueSetInfo().getBillingStartDateTime(), mr.getHistoricalValueSetInfo().getBillingEndDateTime());
                        return registerValue;
                    } else {
                        throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
                    }
                } else {
                    MainRegister mr = (MainRegister) abba1700RegisterFactory.getRegister(registerName, billingPoint);
                    if (unit != null) {
                        mr.setQuantity(new Quantity(mr.getQuantity().getAmount(), unit));
                    }
                    registerValue = new RegisterValue(obisCode,
                            mr.getQuantity(),
                            mr.getHistoricalValueSetInfo().getBillingResetDateTime(),
                            mr.getHistoricalValueSetInfo().getBillingStartDateTime(),
                            mr.getHistoricalValueSetInfo().getBillingEndDateTime());
                    return registerValue;
                }
            } else {
                if (obisCode.getE() > 0) {
                    obisTranslation.append(", tariff register " + obisCode.getE());
                }
            }
        } else {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        }

        if (billingPoint == -1) {
            obisTranslation.append(", current value");
        } else {
            obisTranslation.append(", billing point " + (billingPoint + 1));
        }

        if (read) {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        } else {
            return new RegisterInfo(obisTranslation.toString());
        }

    }

}
