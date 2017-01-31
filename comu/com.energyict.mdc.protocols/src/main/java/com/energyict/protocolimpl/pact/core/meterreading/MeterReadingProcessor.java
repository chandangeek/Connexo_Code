/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * MeterReadingProcessor.java
 *
 * Created on 22 maart 2004, 15:39
 */

package com.energyict.protocolimpl.pact.core.meterreading;

import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.protocolimpl.pact.core.common.EnergyTypeCode;
import com.energyict.protocolimpl.pact.core.common.PACTProtocolException;
import com.energyict.protocolimpl.pact.core.common.PactUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author  Koen
 */
public class MeterReadingProcessor {

	private MeterReadingsInterpreter mri;
	private MeterReadingIdentifier mrid;

    /** Creates a new instance of MeterReadingProcessor */
    public MeterReadingProcessor(MeterReadingsInterpreter mri, MeterReadingIdentifier mrid) {
        this.mri=mri;
        this.mrid=mrid;
    }

//    private int getNrOfChannelDefinitions() {
//        return mri.getChannelDefinitionRegisters().size();
//    }

    private boolean isMultipleSet() {
        return (mri.getTotalRegisters() == null);
    }

    /*
     * For multiple set or single set of history data, get the energytype code into a unit!
     *
     *
     */
    private Unit getUnit(boolean energy) throws PACTProtocolException, NoSuchRegisterException {
        if (isMultipleSet()) {
            return EnergyTypeCode.getUnit(getChannelDefinitionRegister().getEType(), energy);
        }
        else {
            // TODO ?????????????????????
            // We should take the energycode from the i or p blocks if tariff series clem program ?? ...
            // However, (TA) tariff series clem programs are obsolete.
            // UK : (C5) (code 5 clems
            // all the rest (TPA) : Tariff+ CLEM programs

//-------------------------------------------------------------------------------
//  		|250308| GN Moved this into the "if"
//            TotalRegister tr = getTotalRegister();
//            Unit unit = EnergyTypeCode.getUnit(tr.getEType(),energy);
//-------------------------------------------------------------------------------
        	Unit unit = null;

            if (mrid.isTotal()) {
            	TotalRegister tr = getTotalRegister();
            	unit = EnergyTypeCode.getUnit(tr.getEType(),energy);
                return unit;
            }
            else if (mrid.isRate()) {
                if (getRateRegister().getRegIdEnergyIndex() != 0) {
					return EnergyTypeCode.getUnit(mri.getEnergyTypeList().getEType(getRateRegister().getRegIdEnergyIndex()-1),energy);
				} else {
					return unit;
				}
            }
            else if (mrid.isMaximumDemand()) {
                // demand
                if (getMaximumDemand().getRegIdEnergyIndex() != 0) {
					return EnergyTypeCode.getUnit(mri.getEnergyTypeList().getEType(getMaximumDemand().getRegIdEnergyIndex()-1),energy);
				} else {
					return unit;
				}
            }
            else if (mrid.isCumulativeMaximumDemand()) {
                // demand
                if (getCumulativeMaximumDemand().getRegIdEnergyIndex() != 0) {
					return EnergyTypeCode.getUnit(mri.getEnergyTypeList().getEType(getCumulativeMaximumDemand().getRegIdEnergyIndex()-1),energy);
				} else {
                    if (mri.isCode5SeriesCLEM()) {
                        if (getChannelNumber() == 0) {
							return EnergyTypeCode.getUnit(0x82, energy);
						} else if (getChannelNumber() == 1) {
							return EnergyTypeCode.getUnit(0xA0, energy);
						} else {
							return unit;
						}
                    } else {
						return unit;
					}
                }
            }
        }
        throw new NoSuchRegisterException("MeterReadingProcessor, getUnit(), No meter reading for "+mrid.toString());
    }

    private BigDecimal getMeterFactorMultiplier() throws PACTProtocolException,NoSuchRegisterException {
        BigDecimal bd = getMeterFactor();
        // reduce scale, otherwise 59 numbers after decimal point
        bd=bd.setScale((getMeterFactorExp()<0?Math.abs(getMeterFactorExp()):0),BigDecimal.ROUND_HALF_UP);
        return bd;
    }

    private int getMeterFactorExp() throws PACTProtocolException,NoSuchRegisterException {
        if (isMultipleSet()) {
            ChannelDefinitionRegister chdr = getChannelDefinitionRegister();
            return chdr.getMeterFactorExp();
        }
        else {
            // TODO ?????????????????????
            // We should take the energycode from the i or p blocks if tariff clem program...
            // However, (TA) tariff series clem programs are obsolete.
            return mri.getSurveyFlagsInfo().getMeterFactorExp();
        }
    }

    private BigDecimal getMeterFactor() throws PACTProtocolException,NoSuchRegisterException {
        if (isMultipleSet()) {
            ChannelDefinitionRegister chdr = getChannelDefinitionRegister();
            return chdr.getMeterFactor();
        }
        else {
            // TODO ?????????????????????
            // We should take the energycode from the i or p blocks if tariff clem program...
            // However, (TA) tariff series clem programs are obsolete.
            return mri.getSurveyFlagsInfo().getMeterFactor();
        }
    }

    private BigDecimal getMDDivisor(char mdType) throws PACTProtocolException,NoSuchRegisterException {
        if (isMultipleSet()) {
            DemandScaling ds = getDemandScaling();
            return BigDecimal.valueOf(ds.getMdDivisor());
        }
        else {
            if (mdType == 'm') {
				return BigDecimal.valueOf(10);
			} else if (mdType == 'q') {
				return BigDecimal.valueOf(mri.getAdditionalInformation().getMdDivisor());
			}
            throw new PACTProtocolException("MeterReadingProcessor, getMDDivisor, wrong MaximumDemand reading block type ("+mdType+")");
        }
    }

    private BigDecimal getCMDDivisor(char cmdType) throws PACTProtocolException,NoSuchRegisterException {
        if (isMultipleSet()) {
            DemandScaling ds = getDemandScaling();
            return BigDecimal.valueOf(ds.getCmdDivisor());
        }
        else {
            if (cmdType == 'Q') {
				return BigDecimal.valueOf(mri.getAdditionalInformation().getCmdDivisor());
			} else if (cmdType == 'd') {
                if (getMeterFactor().intValue() == 1) {
					return BigDecimal.valueOf(1);
				} else {
					return BigDecimal.valueOf(10);
				}
            }
            throw new PACTProtocolException("MeterReadingProcessor, getCMDDivisor, wrong CumulativeMaximumDemand reading block type ("+cmdType+")");
        }
    }

    //**************************************************************************************************************
    // S I N G L E   S E T   O F   H I S T O R Y   D A T A
    // Get Total, rate, MD or CMD for channel,register,bpindex,triggerchannel
    // TOTAL|RATE|MD|CMD[.0|1(calc using dividers)]:[[r|e]registerNumber],[bpIndex]
    // bpIndex 0=current value, <>0=billing point value
    //**************************************************************************************************************
    private TotalRegister getTotalRegister() throws PACTProtocolException,NoSuchRegisterException {
        if ((mri.getTotalRegisters() == null) || (getChannelNumber() >= mri.getTotalRegisters().size())) {
			throw new NoSuchRegisterException("MeterReadingProcessor, getTotalRegister(), No meter reading for "+mrid.toString());
		}
        return (TotalRegister)mri.getTotalRegisters().get(getChannelNumber());
    } // private TotalRegister getTotalRegister()

    private RateRegister getRateRegister() throws PACTProtocolException,NoSuchRegisterException {
        if ((mri.getRateRegisters() == null) || (getChannelNumber() >= mri.getRateRegisters().size())) {
			throw new NoSuchRegisterException("MeterReadingProcessor, getRateRegister(), No meter reading for "+mrid.toString());
		}
        return (RateRegister)mri.getRateRegisters().get(getChannelNumber());
    }

    private MaximumDemand getMaximumDemand() throws PACTProtocolException,NoSuchRegisterException {
        if (getChannelNumber() >= getMaximumDemands().size()) {
			throw new NoSuchRegisterException("MeterReadingProcessor, getMaximumDemand(), No meter reading for "+mrid.toString());
		}
        return (MaximumDemand)getMaximumDemands().get(getChannelNumber());
    }

    private TimeDateMD getTimeDateMD() throws PACTProtocolException,NoSuchRegisterException {
        if ((mri.getTimeDateMDs() == null) || (getChannelNumber() >= mri.getTimeDateMDs().size())) {
			throw new NoSuchRegisterException("MeterReadingProcessor, getTimeDateMD(), No meter reading for "+mrid.toString());
		}
        return (TimeDateMD)mri.getTimeDateMDs().get(getChannelNumber());
    }

    private CumulativeMaximumDemand getCumulativeMaximumDemand() throws PACTProtocolException,NoSuchRegisterException {
        if (getChannelNumber() >= getCumulativeMaximumDemands().size()) {
			throw new NoSuchRegisterException("MeterReadingProcessor, getCumulativeMaximumDemand(), No meter reading for "+mrid.toString());
		}
        return (CumulativeMaximumDemand)getCumulativeMaximumDemands().get(getChannelNumber());
    }


    private List getMaximumDemands() throws PACTProtocolException,NoSuchRegisterException {
        if (mri.getMaximumDemand_ms() != null) {
			return mri.getMaximumDemand_ms();
		} else if (mri.getMaximumDemand_qs() != null) {
			return mri.getMaximumDemand_qs();
		}
        throw new NoSuchRegisterException("MeterReadingProcessor, getMaximumDemands(), No meter reading for "+mrid.toString());
    }

    private List getCumulativeMaximumDemands() throws PACTProtocolException,NoSuchRegisterException {
        if (mri.getCumulativeMaximumDemand_Qs() != null) {
			return mri.getCumulativeMaximumDemand_Qs();
		} else if (mri.getCumulativeMaximumDemand_ds() != null) {
			return mri.getCumulativeMaximumDemand_ds();
		}
        throw new NoSuchRegisterException("MeterReadingProcessor, getCumulativeMaximumDemands(), No meter reading for "+mrid.toString());
    }


    private int getChannelNumber() throws PACTProtocolException,NoSuchRegisterException {
        if (mrid.isTotal()) {
            if (mrid.isIdETyped()) {
                for (int i=0;i<mri.getTotalRegisters().size();i++) {
                    TotalRegister tr = (TotalRegister)mri.getTotalRegisters().get(i);
                    if (tr.getEType() == mrid.getId()) {
						return i;
					}
                }
            }
            else {
                return mrid.getId();
            }
        }
        else if (mrid.isRate()) {
            for (int i=0;i<mri.getRateRegisters().size();i++) {
                RateRegister rr = (RateRegister)mri.getRateRegisters().get(i);
                if (mrid.isIdETyped()) {
                    if (rr.getRegIdEnergyIndex() != 0) {
                        int eType = mri.getEnergyTypeList().getEType(rr.getRegIdEnergyIndex()-1);
                        // KV 30082006 Also add dependency on the E-field mrid.getRegisterNumber()
                        if ((eType == mrid.getId()) && ((rr.getRegIdRegisterNumber()+1) == mrid.getRegisterNumber())) {
							return i;
						}
                    }
                }
                else {
                    if (rr.getRegIdRegisterNumber() == mrid.getId()) {
						return i;
					}
                }
            }
        }
        else if (mrid.isMaximumDemand()) {
            for (int i=0;i<getMaximumDemands().size();i++) {
                MaximumDemand md = (MaximumDemand)getMaximumDemands().get(i);
                if (mrid.isIdETyped()) {
                    if (md.getRegIdEnergyIndex() != 0) {
                        int eType = mri.getEnergyTypeList().getEType(md.getRegIdEnergyIndex()-1);
                        if ((eType == mrid.getId()) && (md.getRegIdRegisterNumber()==mrid.getRegisterNumber())) {
							return i;
						}
                    }
                    else {
                        // KV 07062004 getRegIdEnergyIndex() == 0, energy type index is defined
                        // by the tariff configuration. So, only use the register ID
                        if (md.getRegIdRegisterNumber()==mrid.getRegisterNumber()) {
							return i;
						}
                    }
                }
                else {
                    if (md.getRegIdRegisterNumber() == mrid.getId()) {
						return i;
					}
                }
            }
        }
        else if (mrid.isCumulativeMaximumDemand()) {
            for (int i=0;i<getCumulativeMaximumDemands().size();i++) {
                CumulativeMaximumDemand cmd = (CumulativeMaximumDemand)getCumulativeMaximumDemands().get(i);
                if (mrid.isIdETyped()) {
                    if (cmd.getRegIdEnergyIndex() != 0) {
                        int eType = mri.getEnergyTypeList().getEType(cmd.getRegIdEnergyIndex()-1);
                        if ((eType == mrid.getId()) && (cmd.getRegIdRegisterNumber()==mrid.getRegisterNumber())) {
							return i;
						}
                    }
                    else {
                        // KV 07062004 getRegIdEnergyIndex() == 0, energy type index is defined
                        // by the tariff configuration. So, only use the register ID
                        if (cmd.getRegIdRegisterNumber()==mrid.getRegisterNumber()) {
							return i;
						}
                    }
                }
                else {
                    if (cmd.getRegIdRegisterNumber() == mrid.getId()) {
						return i;
					}
                }
            }
        }
        throw new NoSuchRegisterException("MeterReadingProcessor, getChannelNumber(), No meter reading for "+mrid.toString());

    } // private int getChannelNumber() throws PACTProtocolException


    //**************************************************************************************************************
    // M U L T I P L E   S E T   O F   H I S T O R Y   D A T A
    // Get Total, rate, MD or CMD for channel,register,bpindex,triggerchannel
    // TOTAL|RATE|MD|CMD[.0|1(calc using dividers)]:[[c|e]channelNumber],[registerNumber],[bpIndex],[triggerChannel]
    //**************************************************************************************************************
    private ChannelDefinitionRegister getChannelDefinitionRegister() throws PACTProtocolException,NoSuchRegisterException {
        for (int i = 0;i<mri.getChannelDefinitionRegisters().size();i++) {
            ChannelDefinitionRegister chdr = (ChannelDefinitionRegister)mri.getChannelDefinitionRegisters().get(i);
            if (mrid.isIdETyped()) {
                if ((chdr.getEType() == mrid.getId()) &&
                    (chdr.getBpIndex() == mrid.getBpIndex())) {
					return chdr;
				}
            }
            else {
                if ((chdr.getChannelNumber() == mrid.getId()) &&
                    (chdr.getBpIndex() == mrid.getBpIndex())) {
					return chdr;
				}
            }
        }
        throw new NoSuchRegisterException("MeterReadingProcessor, getChannelDefinitionRegister(), No meter reading for "+mrid.toString());

    } // private ChannelDefinitionRegister getChannelDefinitionRegister()

    private BillingPointIdentifier getBillingPointIdentifier() throws PACTProtocolException,NoSuchRegisterException {
        Iterator it = mri.getBillingPointIdentifiers().iterator();
        while(it.hasNext()) {
            BillingPointIdentifier bpid = (BillingPointIdentifier)it.next();
            if (bpid.getBpIndex() == mrid.getBpIndex()) {
                return bpid;
            }
        }
        throw new NoSuchRegisterException("No meter reading for "+mrid.toString());
    }

    private RateRegisterValue getRateRegisterValue() throws PACTProtocolException,NoSuchRegisterException {
        Iterator it = mri.getRateRegisterValues().iterator();
        while(it.hasNext()) {
            RateRegisterValue rate = (RateRegisterValue)it.next();
            if ((rate.getChannelNumber() == getChannelDefinitionRegister().getChannelNumber()) &&
                (rate.getRegisterNumber() == mrid.getRegisterNumber()) &&
                (rate.getBpIndex() == mrid.getBpIndex())) {
                return rate;
            }
        }
        throw new NoSuchRegisterException("No meter reading for "+mrid.toString());
    } // getRateRegisterValue()


    private DemandScaling getDemandScaling() throws PACTProtocolException,NoSuchRegisterException {
        Iterator it = mri.getDemandScalings().iterator();
        while(it.hasNext()) {
            DemandScaling ds = (DemandScaling)it.next();
            if (ds.getChannelNumber() == getChannelDefinitionRegister().getChannelNumber()) {
                return ds;
            }
        }
        throw new NoSuchRegisterException("No meter reading for "+mrid.toString());
    } // getDemandScaling()

    private CumulativeMaximumDemandRegister getCMDRegister() throws PACTProtocolException,NoSuchRegisterException {
        if (mri.getCumulativeMaximumDemandRegisters() != null) {
            Iterator it = mri.getCumulativeMaximumDemandRegisters().iterator();
            while(it.hasNext()) {
                CumulativeMaximumDemandRegister cmdr = (CumulativeMaximumDemandRegister)it.next();
                if ((cmdr.getChannelNumber() == getChannelDefinitionRegister().getChannelNumber()) &&
                    (cmdr.getRegisterNumber() == mrid.getRegisterNumber()) &&
                    (cmdr.getBpIndex() == mrid.getBpIndex())) {
                    return cmdr;
                }
            }
        }
        throw new NoSuchRegisterException("No meter reading for "+mrid.toString());
    } // getCMDRegister()

    private MaximumDemandRegister getMDRegister() throws PACTProtocolException,NoSuchRegisterException {
        if (mri.getMaximumDemandRegisters() != null) {
            Iterator it = mri.getMaximumDemandRegisters().iterator();
            while(it.hasNext()) {
                MaximumDemandRegister mdr = (MaximumDemandRegister)it.next();
                if ((mdr.getChannelNumber() == getChannelDefinitionRegister().getChannelNumber()) &&
                    (mdr.getRegisterNumber() == mrid.getRegisterNumber()) &&
                    (mdr.getBpIndex() == mrid.getBpIndex()) &&
                    (mdr.getTriggerChannel() == mrid.getTriggerChannel())) {
                    return mdr;
                }
            }
        }
        throw new NoSuchRegisterException("No meter reading for "+mrid.toString());
    } // getMDRegister()


    //**************************************************************************************************************
    // Get a register using the meteridentification...
    //**************************************************************************************************************
    public RegisterValue getValue() throws PACTProtocolException,NoSuchRegisterException {
        BigDecimal rawValue=null;
        Date mdTimestamp=null;
        Date billingTimestamp=null;

        /****************************************************************************************************/
        /**************************************** ChannelInformation/TotalRegisters ****************************************/
        if (mrid.isTotal()) {
            if (isMultipleSet()) {
                ChannelDefinitionRegister chdr = getChannelDefinitionRegister();
                rawValue = BigDecimal.valueOf(chdr.getRegisterValue()%getModulo());
                if (mrid.getBpIndex() > 0) {
					billingTimestamp=getBillingPointIdentifier().getDateTime();
				}
            }
            else {
                TotalRegister tr = getTotalRegister();
                if (mrid.getBpIndex() == 0) {
					rawValue = BigDecimal.valueOf(tr.getRegister()%getModulo());
				} else {
                   rawValue = BigDecimal.valueOf(tr.getBillingRegister()%getModulo());
                   billingTimestamp=mri.getBillingPoint().getBillingDate();
                }
            }
            if (mrid.isModeCalculate()) {
                rawValue = rawValue.multiply(getMeterFactorMultiplier());
                return new RegisterValue(mrid.getObisCode(),new Quantity(rawValue,getUnit(true)),mdTimestamp,billingTimestamp);
            }
            else {
                Unit chUnit = getUnit(true);
                Unit unit = Unit.get(chUnit.getDlmsCode(),getMeterFactorExp()+chUnit.getScale());
                return new RegisterValue(mrid.getObisCode(),new Quantity(rawValue,unit),mdTimestamp,billingTimestamp);
            }
        }
        /****************************************************************************************************/
        /************************************* CumulativeMaximumDemand **************************************/
        else if (mrid.isCumulativeMaximumDemand()) {
            if (isMultipleSet()) {
                CumulativeMaximumDemandRegister cmdr = getCMDRegister();
                rawValue = BigDecimal.valueOf(cmdr.getRegisterValue());
                if (mrid.getBpIndex() > 0) {
                   billingTimestamp = getBillingPointIdentifier().getDateTime();
                }
            }
            else {
                CumulativeMaximumDemand cmd = (CumulativeMaximumDemand)getCumulativeMaximumDemand();
                if (mrid.getBpIndex() == 0) {
                    rawValue = BigDecimal.valueOf(cmd.getCurrentCMD());
                }
                else {
                    rawValue = BigDecimal.valueOf(cmd.getBillingCMD());
                    billingTimestamp = mri.getBillingPoint().getBillingDate();
                }

            }

            if (mrid.isModeCalculate()) {
                rawValue = rawValue.multiply(getMeterFactorMultiplier());
                rawValue = rawValue.setScale(PactUtils.FRACTIONAL_DIGITS,BigDecimal.ROUND_HALF_UP);
                if (mri.getCumulativeMaximumDemand_Qs() != null) {
					rawValue = rawValue.divide(getCMDDivisor('Q'),BigDecimal.ROUND_HALF_UP);
				} else if (mri.getCumulativeMaximumDemand_ds() != null) {
					rawValue = rawValue.divide(getCMDDivisor('d'),BigDecimal.ROUND_HALF_UP);
				}
                return new RegisterValue(mrid.getObisCode(),new Quantity(rawValue,getUnit(false)),mdTimestamp,billingTimestamp);
            }
            else {
                Unit chUnit = getUnit(false);
                Unit unit = Unit.get(chUnit.getDlmsCode(),getMeterFactorExp()+chUnit.getScale());
                return new RegisterValue(mrid.getObisCode(),new Quantity(rawValue,unit),mdTimestamp,billingTimestamp);
            }

        }
        /****************************************************************************************************/
        /****************************************** MaximumDemand *******************************************/
        else if (mrid.isMaximumDemand()) {
            char mdType=' '; // dummy to initialize
            if (isMultipleSet()) {
                MaximumDemandRegister mdr = getMDRegister();
                rawValue = BigDecimal.valueOf(mdr.getRegisterValue());
                mdTimestamp = mdr.getDateTime();
                if (mrid.getBpIndex() > 0) {
                   billingTimestamp = getBillingPointIdentifier().getDateTime();
                }
            }
            else {
                MaximumDemand md = getMaximumDemand();
                if (mrid.getBpIndex() == 0) {
                    rawValue = BigDecimal.valueOf(md.getCurrentMD());
                    mdTimestamp = getTimeDateMD().getCurrentTime();
                }
                else {
                    rawValue = BigDecimal.valueOf(md.getBillingMD());
                    mdTimestamp = getTimeDateMD().getBillingTime();
                    billingTimestamp = mri.getBillingPoint().getBillingDate();
                }

                mdType = md.getType();
            }

            if (mrid.isModeCalculate()) {
                // formula:
                // (rawValue * getMeterFactorMultiplier()) / getMDDivisor()
                rawValue = rawValue.multiply(getMeterFactorMultiplier());
                // reduce scale to fractional digits set in PactUtils
                rawValue = rawValue.setScale(PactUtils.FRACTIONAL_DIGITS,BigDecimal.ROUND_HALF_UP);
                rawValue = rawValue.divide(getMDDivisor(mdType),BigDecimal.ROUND_HALF_UP);
                return new RegisterValue(mrid.getObisCode(),new Quantity(rawValue,getUnit(false)),mdTimestamp,billingTimestamp);
            }
            else {
                Unit chUnit = getUnit(false);
                Unit unit = Unit.get(chUnit.getDlmsCode(),getMeterFactorExp()+chUnit.getScale());
                return new RegisterValue(mrid.getObisCode(),new Quantity(rawValue,unit),mdTimestamp,billingTimestamp);
            }

        }
        /****************************************************************************************************/
        /********************************************** Rate/Tariff ************************************************/
        else if (mrid.isRate()) {
            if (isMultipleSet()) {
                RateRegisterValue rate = getRateRegisterValue();
                rawValue = BigDecimal.valueOf(rate.getRegisterValue()%getModulo());
                if (mrid.getBpIndex() > 0) {
					billingTimestamp=getBillingPointIdentifier().getDateTime();
				}
            }
            else {
                RateRegister rr = (RateRegister)getRateRegister();
                if (mrid.getBpIndex() == 0) {
					rawValue = BigDecimal.valueOf(rr.getCurrentValue()%getModulo());
				} else {
                    rawValue = BigDecimal.valueOf(rr.getBillingValue()%getModulo());
                    billingTimestamp = mri.getBillingPoint().getBillingDate();
                }
            }

            if (mrid.isModeCalculate()) {
                rawValue = rawValue.multiply(getMeterFactorMultiplier());
                return new RegisterValue(mrid.getObisCode(),new Quantity(rawValue,getUnit(true)),mdTimestamp,billingTimestamp);
            }
            else {
                Unit chUnit = getUnit(true);
                Unit unit = Unit.get(chUnit.getDlmsCode(),getMeterFactorExp()+chUnit.getScale());
                return new RegisterValue(mrid.getObisCode(),new Quantity(rawValue,unit),mdTimestamp,billingTimestamp);
            }
        }
        else if (mrid.isBillingCounter()) {
            if (isMultipleSet()) {
                return new RegisterValue(mrid.getObisCode(),new Quantity(new Integer(mri.getCounters().getBillingResetCounter()), Unit.get(255)));
            }
            else {
                return new RegisterValue(mrid.getObisCode(),new Quantity(new Integer(mri.getGeneralInformation().getReadCount()), Unit.get(255)));
            }
        }
        else if (mrid.isBillingTimestamp()) {
            if (isMultipleSet()) {
                BillingPointIdentifier bpid = getBillingPointIdentifier();
                return new RegisterValue(mrid.getObisCode(),bpid.getDateTime());
            }
            else { // single set...
                return new RegisterValue(mrid.getObisCode(),mri.getBillingPoint().getBillingDate());
            }
        }


        throw new NoSuchRegisterException("No meter reading for "+mrid.toString());
    } // public BigDecimal getValue(MeterReadingIdentifier mrid)

    private int getModulo() {

        // see section 5.4 of Interpreting Meter Readings
        // we should use commissioned power and meter hardware type (from K block)
        // to calculate other modulo values
        // Following a email from Martin, it is OK to modulo all registers with 1000000
        //return 1000000;
        return mri.getProtocolLink().getModulo();
    }


}
