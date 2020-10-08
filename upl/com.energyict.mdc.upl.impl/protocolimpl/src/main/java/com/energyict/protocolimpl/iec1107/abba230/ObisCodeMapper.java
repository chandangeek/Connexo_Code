/*
 * ObisCodeMapper.java
 *
 * Created on 11 juni 2004, 13:55
 */

package com.energyict.protocolimpl.iec1107.abba230;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.iec1107.abba230.eventlogs.EndOfBillingEventLog;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Koen
 */

public class ObisCodeMapper {

    private final ABBA230RegisterFactory rFactory;

    ObisCodeMapper(ABBA230RegisterFactory abba230RegisterFactory) {
        this.rFactory = abba230RegisterFactory;
    }

    static RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(null);
        return (RegisterInfo) ocm.doGetRegister(obisCode, false);
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        try {
            return (RegisterValue) doGetRegister(obisCode, true);
        } catch (IOException ioe) {
            Logger logger = rFactory.getProtocolLink().getLogger();
            String msg = "Problems retrieving " + obisCode.toString();
            logger.log(Level.INFO, msg);
            throw ioe;
        }
    }

    private Object doGetRegister(ObisCode obisCode, boolean read) throws IOException {

        RegisterValue registerValue = null;
        String registerName;
        int bp;
        StringBuilder obisTranslation = new StringBuilder();
        Unit unit = null;

        // obis F code
        if ((obisCode.getF() >= 0) && (obisCode.getF() <= 99)) {
            bp = obisCode.getF();
        } else if ((obisCode.getF() <= 0) && (obisCode.getF() >= -99)) {
            bp = obisCode.getF() * -1;
        } else if (obisCode.getF() == 255) {
            bp = -1;
        } else {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        }

        // *********************************************************************************
        // General purpose ObisRegisters & abstract general service

        /* Billing Point Timestamp */
        if (obisCode.toString().startsWith("1.1.0.1.2.")) {
            if ((bp >= 0) && (bp <= 11)) {
                if (read) {
                    HistoricalRegister hv = (HistoricalRegister) rFactory.getRegister("HistoricalRegister", bp);

                    Quantity quantity = new Quantity(new BigDecimal(0), Unit.get(255));
                    Date eventTime = hv.getBillingDate();
                    Date fromTime = null;
                    Date toTime = hv.getBillingDate();

                    registerValue = new RegisterValue(obisCode, quantity, eventTime, fromTime, toTime);

                    return registerValue;
                } else {
                    return new RegisterInfo("billing point " + bp + " timestamp");
                }
            } else if ((bp >= 12) && (bp <= 25)) {
                if (read) {
                    HistoricalRegister hv = (HistoricalRegister) rFactory.getRegister("DailyHistoricalRegister", bp);

                    Quantity quantity = new Quantity(new BigDecimal(0), Unit.get(255));
                    Date eventTime = hv.getBillingDate();
                    Date fromTime = null;
                    Date toTime = hv.getBillingDate();

                    registerValue = new RegisterValue(obisCode, quantity, eventTime, fromTime, toTime);

                    return registerValue;
                } else {
                    return new RegisterInfo("billing point " + bp + " timestamp");
                }
            } else {
                String msg = "ObisCode " + obisCode.toString() + " is not supported!";
                throw new NoSuchRegisterException(msg);
            }
        }
        /* Current System Status */
        else if (obisCode.toString().startsWith("0.0.96.50.0.255")) {
            if (read) {
                registerValue = new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(rFactory.getSystemStatus().getValue()), Unit.get(255)));
                return registerValue;
            } else {
                return new RegisterInfo("Current System Status (32 bit word)");
            }
        } else if (obisCode.toString().startsWith("0.0.96.53.")) {
            if (read) {
                if ((obisCode.getE() >= 0) && (obisCode.getE() <= 15)) {
                    if (obisCode.getF() == 255) {
                        Integer systemStatus = rFactory.getSystemStatus().getSystemStatus(obisCode.getE());
                        if (systemStatus != null) {
                            registerValue = new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(systemStatus), Unit.get(255)));
                        } else {
                            registerValue = new RegisterValue(obisCode, "System status not available");
                        }
                    } else {
                        if ((obisCode.getF() >= 0) && (obisCode.getF() <= 7)) {
                            Integer systemStatus = rFactory.getSystemStatus().getSystemStatus(obisCode.getE());
                            if (systemStatus != null) {
                                systemStatus = (systemStatus & (1 << obisCode.getF())) == (1 << obisCode.getF()) ? 1 : 0;
                                registerValue = new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(systemStatus), Unit.get(255)));
                            } else {
                                registerValue = new RegisterValue(obisCode, "System status not available");
                            }
                        }
                    }
                }
                return registerValue;
            } else {
                if (obisCode.getF() == 255) {
                    return new RegisterInfo("Current SystemStatus" + obisCode.getE() + " (1 byte)");
                } else {
                    if ((obisCode.getF() >= 0) && (obisCode.getF() <= 7)) {
                        return new RegisterInfo("Current SystemStatus" + obisCode.getE() + " bit " + obisCode.getF());
                    }
                }
            }
        } else if (obisCode.toString().startsWith("0.0.96.54.")) {
            if (read) {
                if ((obisCode.getE() >= 0) && (obisCode.getE() <= 3)) {
                    if (obisCode.getF() == 255) {
                        Integer systemError = rFactory.getSystemStatus().getSystemError(obisCode.getE());
                        if (systemError != null) {
                            registerValue = new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(systemError), Unit.get(255)));
                        } else {
                            registerValue = new RegisterValue(obisCode, "System error not available");
                        }
                    } else {
                        if ((obisCode.getF() >= 0) && (obisCode.getF() <= 7)) {
                            Integer systemError = rFactory.getSystemStatus().getSystemError(obisCode.getE());
                            if (systemError != null) {
                                systemError = (systemError & (1 << obisCode.getF())) == (1 << obisCode.getF()) ? 1 : 0;
                                registerValue = new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(systemError), Unit.get(255)));
                            } else {
                                registerValue = new RegisterValue(obisCode, "System error not available");
                            }
                        }
                    }
                }
                return registerValue;
            } else {
                if (obisCode.getF() == 255) {
                    return new RegisterInfo("Current SystemError" + obisCode.getE() + " (1 byte)");
                } else {
                    if ((obisCode.getF() >= 0) && (obisCode.getF() <= 7)) {
                        return new RegisterInfo("Current SystemError" + obisCode.getE() + " bit " + obisCode.getF());
                    }
                }
            }
        } else if ((obisCode.toString().contains("1.1.0.4.2.255")) || (obisCode.toString().contains("1.0.0.4.2.255"))) {
            if (read) {
                BigDecimal bd = (BigDecimal) rFactory.getRegister("CTPrimary");
                registerValue = new RegisterValue(obisCode, new Quantity(bd, Unit.get(255)));
                return registerValue;
            } else {
                return new RegisterInfo("CT numerator");
            }
        } else if ((obisCode.toString().contains("1.1.0.4.5.255")) || (obisCode.toString().contains("1.0.0.4.5.255"))) { // CT denominator
            if (read) {
                BigDecimal bd = (BigDecimal) rFactory.getRegister("CTSecundary");
                registerValue = new RegisterValue(obisCode, new Quantity(bd, Unit.get(255)));
                return registerValue;
            } else {
                return new RegisterInfo("CT denominator");
            }
        } else if (obisCode.toString().contains("1.0.0.0.1.255")) { // SchemeID
            if (read) {
                String schemeId = (String) rFactory.getRegister("SchemeID");
                registerValue = new RegisterValue(obisCode, schemeId);
                return registerValue;
            } else {
                return new RegisterInfo("SchemeID");
            }
        } else if (obisCode.toString().contains("0.0.96.1.0.255")) { // Serial number
            if (read) {
                String sn = (String) rFactory.getRegister("SerialNumber");
                registerValue = new RegisterValue(obisCode, sn);
                return registerValue;
            } else {
                return new RegisterInfo("SerialNumber");
            }
        } else if (obisCode.toString().contains("0.0.96.51.0.255")) { // APPL firmware version
            if (read) {
                String sn = new String(rFactory.getABBA230DataIdentityFactory().getDataIdentity("998", false, 12, 0)).trim();
                registerValue = new RegisterValue(obisCode, sn);
                return registerValue;
            } else {
                return new RegisterInfo("APPLFWVersion");
            }
        } else if (obisCode.toString().contains("0.0.96.52.0.255")) { // DSP firmware version
            if (read) {
                String sn = new String(rFactory.getABBA230DataIdentityFactory().getDataIdentity("998", false, 12, 1)).trim();
                registerValue = new RegisterValue(obisCode, sn);
                return registerValue;
            } else {
                return new RegisterInfo("DSPFWVersion");
            }
        } else if (obisCode.toString().contains("1.1.0.1.0.255")) { // Billing counter
            if (read) {
                Object register = rFactory.getRegister("EndOfBillingEventLog");
                if ((register != null) && (register instanceof EndOfBillingEventLog)) {
                    EndOfBillingEventLog historicalEventRegister = (EndOfBillingEventLog) register;
                    registerValue = new RegisterValue(obisCode, new Quantity(historicalEventRegister.getCount(), Unit.get("")));
                }
                return registerValue;
            } else {
                return new RegisterInfo("BillingCounter");
            }
        }


        // *********************************************************************************
        // Electricity related ObisRegisters
        // verify a & b
        if ((obisCode.getA() != 1) || (obisCode.getB() < 0) || (obisCode.getB() > 3)) {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        }

        // obis C code
        if (obisCode.getC() == 1) {
            registerName = "CummMainImport";
        } else if (obisCode.getC() == 2) {
            registerName = "CummMainExport";
        }  else if (obisCode.getC() == 5) {
            registerName = "CummMainQ1";
            unit = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR,-3);
        } else if (obisCode.getC() == 6) {
            registerName = "CummMainQ2";
            unit = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR,-3);
        } else if (obisCode.getC() == 7) {
            registerName = "CummMainQ3";
            unit = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR,-3);
        } else if (obisCode.getC() == 8) {
            registerName = "CummMainQ4";
            unit = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, -3);
        } else if (obisCode.getC() == 9) {
            registerName = "CummMainVAImport";
        } else if (obisCode.getC() == 10) {
            registerName = "CummMainVAExport";
        } else if (obisCode.getC() == 3) {
            registerName = "CummMainvarhImport";
        } else if (obisCode.getC() == 4) {
            registerName = "CummMainvarhExport";
        } else {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        }

        if (rFactory != null && obisCode.getF() >= 0 && obisCode.getF() <= 11) {
            HistoricalRegister hv = (HistoricalRegister) rFactory.getRegister("HistoricalRegister", bp);
            if (hv.getBillingDate() == null) {
                throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
            }
        }
        if (rFactory != null && obisCode.getF() >= 12 && obisCode.getF() <= 25) {
            HistoricalRegister hv = (HistoricalRegister) rFactory.getRegister("DailyHistoricalRegister", bp);
            if (hv.getBillingDate() == null) {
                throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
            }
        }

        // obis D code
        if (obisCode.getD() == 2) {// cumulative maximum 1  CMD
            // search for right unit (registersource)...
            if (read) {
                for (int i = 0; i < ABBA230RegisterFactory.MAX_CMD_REGS; i++) {
                    CumulativeMaximumDemand cmd = (CumulativeMaximumDemand) rFactory.getRegister("CumulativeMaximumDemand" + i, bp);
                    // energytype code match
                    if (cmd.getRegSource() == EnergyTypeCode.getRegSourceFromObisCCode(obisCode.getC())) {
                        if (unit != null) {
                            cmd.setQuantity(new Quantity(cmd.getQuantity().getAmount(), unit.getFlowUnit()));
                        }

                        return cmd.toRegisterValue(obisCode);
                    }
                }
            } else {
                obisTranslation.append(EnergyTypeCode.getCompountInfoFromObisC(obisCode.getC(), false));
                obisTranslation.append(", cumulative maximum demand");
            }

        } else if (obisCode.getD() == 6 && obisCode.getB() > 0) {// maximum 1 MD

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
                for (int i = 0; i < ABBA230RegisterFactory.MAX_MD_REGS; i++) {
                    MaximumDemand md = (MaximumDemand) rFactory.getRegister("MaximumDemand" + i, bp);
                    // energytype code match with the maximumdemand register with the most
                    // recent datetime stamp.
                    if (md.getRegSource() == EnergyTypeCode.getRegSourceFromObisCCode(obisCode.getC())) {
                        if (unit != null) {
                            md.setQuantity(new Quantity(md.getQuantity().getAmount(), unit.getFlowUnit()));
                        }
                        return md.toRegisterValue(obisCode);
                    }
                }
            } else {
                obisTranslation.append(EnergyTypeCode.getCompountInfoFromObisC(obisCode.getC(), false));
                obisTranslation.append(", maximum demand");
            }

        } else if (obisCode.getD() == 8) {// time integral 1 TOTAL & RATE
            obisTranslation.append(EnergyTypeCode.getCompountInfoFromObisC(obisCode.getC(), true));

            if (read) {
                // fbl: bugfix 9/10/2006 extra check on e field.
                if (obisCode.getE() > 16) {
                    String msg = "ObisCode " + obisCode.toString() + " is not supported!";
                    throw new NoSuchRegisterException(msg);
                }
                if (obisCode.getE() > 0) {

                    TariffSources ts = (TariffSources) rFactory.getRegister("TariffSources");
//                    if (bp == -1) {
//                        ts=(TariffSources)rFactory.getRegister("TariffSources");
//                    } else {
//                    	if ((bp >= 0) && (bp <= 11)) {
//                    		ts=((HistoricalRegister)rFactory.getRegister( "HistoricalRegister", bp )).getTariffSources();
//                    	}
//                    	else if ((bp >= 12) && (bp <= 25)) {
//                    		ts=((HistoricalRegister)rFactory.getRegister( "DailyHistoricalRegister", bp )).getTariffSources();
//                    	}
//                    }

                    registerName = "TimeOfUse" + (obisCode.getE() - 1);
                    MainRegister mr = (MainRegister) rFactory.getRegister(registerName, bp);
                    if (ts.getRegSource()[obisCode.getE() - 1] == EnergyTypeCode.getRegSourceFromObisCCode(obisCode.getC())) {
                        if (EnergyTypeCode.isCustomerDefined(ts.getRegSource()[obisCode.getE() - 1])) {
                            unit = EnergyTypeCode.getUnitFromRegSource(EnergyTypeCode.getRegSourceFromObisCCode(obisCode.getC()), true);
                        } else {
                            unit = EnergyTypeCode.getUnitFromObisCCode(obisCode.getC(), true);
                        }
                        mr.setQuantity(new Quantity(mr.getQuantity().getAmount(), unit));
                        return mr.toRegisterValue(obisCode);
                    } else {
                        String msg = "ObisCode " + obisCode.toString() + " is not supported!";
                        throw new NoSuchRegisterException(msg);
                    }

                } else {
                    MainRegister mr = (MainRegister) rFactory.getRegister(registerName, bp);
                    if (unit != null) {
                        mr.setQuantity(new Quantity(mr.getQuantity().getAmount(), unit));
                    }
                    return mr.toRegisterValue(obisCode);
                }
            } else {
                if (obisCode.getE() > 0) {
                    obisTranslation.append(", tariff register ").append(obisCode.getE());
                }
            }
        } else {
            String msg = "ObisCode " + obisCode.toString() + " is not supported!";
            throw new NoSuchRegisterException(msg);
        }

        if (bp == -1) {
            obisTranslation.append(", current value");
        } else {
            obisTranslation.append(", billing point ").append(bp + 1);
        }

        if (read) {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        } else {
            return new RegisterInfo(obisTranslation.toString());
        }

    }


}
