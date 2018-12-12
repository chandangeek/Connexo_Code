/*
 * MeterReadingBlocks.java
 *
 * Created on 11 maart 2004, 11:07
 */

package com.energyict.protocolimpl.pact.core.meterreading;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.pact.core.common.EnergyTypeCode;
import com.energyict.protocolimpl.pact.core.common.ProtocolLink;
import com.energyict.protocolimpl.pact.core.survey.LoadSurveyInterpreter;
import com.energyict.protocolimpl.pact.core.survey.absolute.AbsoluteSurvey;
import com.energyict.protocolimpl.pact.core.survey.ascii.AsciiSurvey;
import com.energyict.protocolimpl.pact.core.survey.binary.BinarySurvey;
import com.energyict.protocolimpl.pact.core.survey.discrete.DiscreteSurvey;
import com.energyict.protocolimpl.pact.core.survey.link.LinkSurvey;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
/**
 *
 * @author  Koen
 */
public class MeterReadingsInterpreter {

    private static final int DEBUG=0;

    // General information
    private SurveyInfo surveyInfo=null;
    private SurveyFlagsInfo surveyFlagsInfo=null;
    private GeneralInformation generalInformation=null;

    // One set of historical data
    private List totalRegisters=null; // of type TotalRegister
    private List cumulativeMaximumDemand_Qs=null; // of type CumulativeMaximumDemand
    private List cumulativeMaximumDemand_ds=null; // of type CumulativeMaximumDemand
    private List maximumDemand_ms=null; // of type MaximumDemand
    private List maximumDemand_qs=null; // of type MaximumDemand
    private List rateRegisters=null; // of type RateRegister
    //List billingPoints=null; // of type BillingPoint


    private List timeDateMDs=null; // of type TimeDateMD
    //TimeDateMD timeDateMD=null;
    private AdditionalInformation additionalInformation=null;
    private CommissioningInformation commissioningInformation=null;
    private EnergyTypeList energyTypeList=null;
    private Seeds seeds=null;
    private Counters counters=null;
    private BillingPoint billingPoint=null;

    // Many sets of historical data
    private List billingPointIdentifiers = null; // of type BillingPointIdentifier
    private List channelDefinitionRegisters = null; // of type ChannelDefinitionRegister
    private List rateRegisterValues = null; // of type RateRegisterValue
    private List maximumDemandRegisters = null; // of type MaximumDemandRegister
    private List cumulativeMaximumDemandRegisters = null; // of type CumulativeMaximumDemandRegister
    private List demandScalings = null; // of type DemandScaling
    private List tariffNameFlags = null; // of type TariffNameFlag

    private boolean registersRead=false;

    private TariffNameFlag tariffNameFlag=null;

    private String serialId=null;
    private String clemProgramName=null;
    private String currentTariffName=null;

    private byte[] data;
    private byte[] loadSurveyData;
    private int dataPtr=0;
    private ProtocolLink protocolLink;


    /** Creates a new instance of MeterReadingBlocks */
    public MeterReadingsInterpreter(byte[] data,ProtocolLink protocolLink) {
       this(data, 0, protocolLink);
    }

    /** Creates a new instance of MeterReadingBlocks */
    public MeterReadingsInterpreter(byte[] data, int dataPtr,ProtocolLink protocolLink) {
    	if(data != null){
    		this.data=data.clone();
    	}
        this.dataPtr=dataPtr;
        this.protocolLink=protocolLink;
    }

    public void parse() {
        parseFirstBlocks();
        parseNextBlocks();
    }

    /*
     *  KV/KH 29062004
     *  Tricky! Because we do not have an indication of when the DST switchover happens,
     *  we must guess the DST corrected timezone from the rawoffset!
     *  Another alternative is to use a DST corrected timezone in EIServer and get
     *  the standardtimezone. This should be better but we obtained for the first, tricky method!
     */
    private TimeZone getMeterTypeDependentTimeZone() {
        if (protocolLink.getRegisterTimeZone() == null) {
            if (isCode5SeriesCLEM()) {
                if (protocolLink.getTimeZone().getRawOffset() == 0) {
					return TimeZone.getTimeZone("WET");
				}
                if (protocolLink.getTimeZone().getRawOffset() == 3600000) {
					return TimeZone.getTimeZone("ECT");
				}
                if (protocolLink.getTimeZone().getRawOffset() == 7200000) {
					return TimeZone.getTimeZone("EET");
				}
            }
            return protocolLink.getTimeZone();
        }
        else {
            return protocolLink.getRegisterTimeZone();
        }
    }

    private boolean isMultipleSet() {
        return (getTotalRegisters() == null);
    }

    private List getMaximumDemands() {
        if (getMaximumDemand_ms() != null) {
			return getMaximumDemand_ms();
		} else if (getMaximumDemand_qs() != null) {
			return getMaximumDemand_qs();
		} else {
			return null;
		}
    }

    private List getCumulativeMaximumDemands() {
        if (getCumulativeMaximumDemand_Qs() != null) {
			return getCumulativeMaximumDemand_Qs();
		} else if (getCumulativeMaximumDemand_ds() != null) {
			return getCumulativeMaximumDemand_ds();
		} else {
			return null;
		}
    }

    public String getObisCodeDescriptions() {
        StringBuffer strBuff = new StringBuffer();
        if (isMultipleSet()) { // multiple set of history data
            strBuff.append(getMultipleSetObisCodeDescriptions());
        }
        else {  // single set of History data
            strBuff.append(getSingleSetObisCodeDescriptions());
        } // if single set of history data

        strBuff.append("General purpose registers:\n");
        strBuff.append(MeterReadingIdentifier.GENERAL_PURPOSE_CODES);


        return strBuff.toString();
    } // public String getRegisterInfo()

    private int getMultipleSetChannelDefinitionEType(int channelNumber) {
        Iterator it = getChannelDefinitionRegisters().iterator();
        while(it.hasNext()) {
            ChannelDefinitionRegister chdr = (ChannelDefinitionRegister)it.next();
            if (chdr.getChannelNumber() == channelNumber) {
                return chdr.getEType();
            }
        }
        return -1;
    }


    private String getMultipleSetObisCodeDescriptions() {
        if (!registersRead) {
			return "No registers available for reading!\n";
		}
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("Total registers (Multiple Set):\n");
        Iterator it = getChannelDefinitionRegisters().iterator();
        while(it.hasNext()) {
            ChannelDefinitionRegister chdr = (ChannelDefinitionRegister)it.next();
            int obisCCode = EnergyTypeCode.getObisCCode(chdr.getEType());
            if (chdr.getBpIndex() == 0) {
				strBuff.append("1.1."+Integer.toString(obisCCode)+".8.0.255 (current value) "+EnergyTypeCode.getCompountInfoFromObisC(obisCCode,true)+"\n");
			} else {
				strBuff.append("1.1."+Integer.toString(obisCCode)+".8.0."+(chdr.getBpIndex()-1)+" (billing value "+chdr.getBpIndex()+") "+EnergyTypeCode.getCompountInfoFromObisC(obisCCode,true)+"\n");
			}
        }
        strBuff.append("Rate registers:\n");
        if (getRateRegisterValues() != null) {
            it = getRateRegisterValues().iterator();
            while(it.hasNext()) {
                RateRegisterValue rrv = (RateRegisterValue)it.next();
                int obisCCode = EnergyTypeCode.getObisCCode(getMultipleSetChannelDefinitionEType(rrv.getChannelNumber()));
                if (rrv.getBpIndex() == 0) {
                    strBuff.append("1.1."+Integer.toString(obisCCode)+".8."+(rrv.getRegisterNumber()+1)+".255 (current value) "+EnergyTypeCode.getCompountInfoFromObisC(obisCCode,true)+"\n");
                }
                else {
                    strBuff.append("1.1."+Integer.toString(obisCCode)+".8."+(rrv.getRegisterNumber()+1)+"."+(rrv.getBpIndex()-1)+" (billing value "+rrv.getBpIndex()+") "+EnergyTypeCode.getCompountInfoFromObisC(obisCCode,true)+"\n");
                }
            }
        }
        strBuff.append("Maximum demand registers:\n");
        if (getMaximumDemandRegisters() != null) {
            it = getMaximumDemandRegisters().iterator();
            while(it.hasNext()) {
                MaximumDemandRegister mdr = (MaximumDemandRegister)it.next();
                int obisCCode = EnergyTypeCode.getObisCCode(getMultipleSetChannelDefinitionEType(mdr.getChannelNumber()));
                if (mdr.getBpIndex() == 0) {
                    strBuff.append("1."+(mdr.getTriggerChannel()+1)+"."+Integer.toString(obisCCode)+".6."+(mdr.getRegisterNumber())+".255 (current value) "+EnergyTypeCode.getCompountInfoFromObisC(obisCCode,false)+"\n");
                }
                else {
                    strBuff.append("1."+(mdr.getTriggerChannel()+1)+"."+Integer.toString(obisCCode)+".6."+(mdr.getRegisterNumber())+"."+(mdr.getBpIndex()-1)+" (billing value "+mdr.getBpIndex()+") "+EnergyTypeCode.getCompountInfoFromObisC(obisCCode,false)+"\n");
                }
            }
        }
        strBuff.append("Cumulative maximum demand registers:\n");
        if (getCumulativeMaximumDemandRegisters() != null) {
            it = getCumulativeMaximumDemandRegisters().iterator();
            while(it.hasNext()) {
                CumulativeMaximumDemandRegister cmdr = (CumulativeMaximumDemandRegister)it.next();
                int obisCCode = EnergyTypeCode.getObisCCode(getMultipleSetChannelDefinitionEType(cmdr.getChannelNumber()));
                if (cmdr.getBpIndex() == 0) {
                    strBuff.append("1.1."+Integer.toString(obisCCode)+".2."+(cmdr.getRegisterNumber())+".255 (current value) "+EnergyTypeCode.getCompountInfoFromObisC(obisCCode,false)+"\n");
                }
                else {
                    strBuff.append("1.1."+Integer.toString(obisCCode)+".2."+(cmdr.getRegisterNumber())+"."+(cmdr.getBpIndex()-1)+" (billing value "+cmdr.getBpIndex()+") "+EnergyTypeCode.getCompountInfoFromObisC(obisCCode,false)+"\n");
                }
            }
        }

        return strBuff.toString();
    }

    private String getSingleSetObisCodeDescriptions() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("Total registers (Single Set):\n");
        Iterator it = getTotalRegisters().iterator();
        while(it.hasNext()) {
            TotalRegister tr = (TotalRegister)it.next();
            int obisCCode = EnergyTypeCode.getObisCCode(tr.getEType());
            strBuff.append("1.1."+Integer.toString(obisCCode)+".8.0.255 (current value) "+EnergyTypeCode.getCompountInfoFromObisC(obisCCode,true)+"\n");
            strBuff.append("1.1."+Integer.toString(obisCCode)+".8.0.0 (billing value) "+EnergyTypeCode.getCompountInfoFromObisC(obisCCode,true)+"\n");
        }
        strBuff.append("Rate registers:\n");
        if (getRateRegisters() != null) {
            it = getRateRegisters().iterator();
            while(it.hasNext()) {
                RateRegister rr = (RateRegister)it.next();
                if (rr.getRegIdEnergyIndex() != 0) {
                    int obisCCode = EnergyTypeCode.getObisCCode(getEnergyTypeList().getEType(rr.getRegIdEnergyIndex()-1));
                    strBuff.append("1.1."+Integer.toString(obisCCode)+".8."+(rr.getRegIdRegisterNumber()+1)+".255 (current value) "+EnergyTypeCode.getCompountInfoFromObisC(obisCCode,true)+"\n");
                    strBuff.append("1.1."+Integer.toString(obisCCode)+".8."+(rr.getRegIdRegisterNumber()+1)+".0 (billing value) "+EnergyTypeCode.getCompountInfoFromObisC(obisCCode,true)+"\n");
                }
                else {
                    strBuff.append("1.1.x.8."+(rr.getRegIdRegisterNumber()+1)+".255 (current value, x must be set according to the tariff configuration!)\n");
                    strBuff.append("1.1.x.8."+(rr.getRegIdRegisterNumber()+1)+".0 (billing value, x must be set according to the tariff configuration!)\n");
                }
            }
        } // if (getRateRegisters() != null)
        strBuff.append("Maximum demand registers:\n");
        if (getMaximumDemands() != null) {
            it = getMaximumDemands().iterator();
            while(it.hasNext()) {
                MaximumDemand md = (MaximumDemand)it.next();
                if (md.getRegIdEnergyIndex() != 0) {
                    int obisCCode = EnergyTypeCode.getObisCCode(getEnergyTypeList().getEType(md.getRegIdEnergyIndex()-1));
                    strBuff.append("1.1."+Integer.toString(obisCCode)+".6."+(md.getRegIdRegisterNumber())+".255 (current value) "+EnergyTypeCode.getCompountInfoFromObisC(obisCCode,false)+"\n");
                    strBuff.append("1.1."+Integer.toString(obisCCode)+".6."+(md.getRegIdRegisterNumber())+".0 (billing value) "+EnergyTypeCode.getCompountInfoFromObisC(obisCCode,false)+"\n");
                }
                else {
                    strBuff.append("1.1.x.6."+(md.getRegIdRegisterNumber())+".255 (current value, x must be set according to the tariff configuration!)\n");
                    strBuff.append("1.1.x.6."+(md.getRegIdRegisterNumber())+".0 (billing value,x must be set according to the tariff configuration!)\n");
                }
            }
        } // if (getMaximumDemands() != null)
        strBuff.append("Cumulative maximum demand registers:\n");
        if (getCumulativeMaximumDemands() != null) {
            it = getCumulativeMaximumDemands().iterator();
            while(it.hasNext()) {
                CumulativeMaximumDemand cmd = (CumulativeMaximumDemand)it.next();
                if (cmd.getRegIdEnergyIndex() != 0) {
                    int obisCCode = EnergyTypeCode.getObisCCode(getEnergyTypeList().getEType(cmd.getRegIdEnergyIndex()-1));
                    strBuff.append("1.1."+Integer.toString(obisCCode)+".2."+(cmd.getRegIdRegisterNumber())+".255 (current value) "+EnergyTypeCode.getCompountInfoFromObisC(obisCCode,false)+"\n");
                    strBuff.append("1.1."+Integer.toString(obisCCode)+".2."+(cmd.getRegIdRegisterNumber())+".0 (billing value) "+EnergyTypeCode.getCompountInfoFromObisC(obisCCode,false)+"\n");
                }
                else {
                    strBuff.append("1.1.x.2."+(cmd.getRegIdRegisterNumber())+".255 (current value, x must be set according to the tariff configuration!)\n");
                    strBuff.append("1.1.x.2."+(cmd.getRegIdRegisterNumber())+".0 (billing value,x must be set according to the tariff configuration!)\n");
                }
            }
        } // if (getCumulativeMaximumDemands() != null)
        return strBuff.toString();
    } // private String getSingleSetObisCodeDescriptions()

    public String toString() {
        return "serialId (first block)= "+getSerialId()+"\n"+
               "clemProgramName (second block)= "+getClemProgramName()+"\n"+
               "seeds (S)= "+getSeeds()+"\n"+
               "currentTariffName (t)= "+getCurrentTariffName()+"\n"+
               "SurveyInfo (s)= "+getSurveyInfo().toString()+"\n"+
               "SurveyFlagsInfo (F)= "+getSurveyFlagsInfo().toString()+"\n"+
               "GeneralInformation (i)= "+(getGeneralInformation()!=null ? getGeneralInformation().toString():"no GeneralInformation")+"\n"+
               "totalRegisters (T)= "+(totalRegisters != null ? printList(totalRegisters):"no totalRegister objects")+"\n"+
               "rateRegisters (c)= "+(rateRegisters != null ? printList(rateRegisters):"no RateRegister objects")+"\n"+
               "maximumDemands (m)= "+(maximumDemand_ms != null ? printList(maximumDemand_ms):"no MaximumDemand objects")+"\n"+
               "maximumDemands (q)= "+(maximumDemand_qs != null ? printList(maximumDemand_qs):"no MaximumDemand objects")+"\n"+
               "timeDateMDs (h)= "+(getTimeDateMDs() != null ? printList(getTimeDateMDs()):"no TimeDateMD objects")+"\n"+
               "cumulativeMaximumDemands (d)= "+(cumulativeMaximumDemand_ds != null ? printList(cumulativeMaximumDemand_ds):"no CumulativeMaximumDemand objects")+"\n"+
               "cumulativeMaximumDemands (Q)= "+(cumulativeMaximumDemand_Qs != null ? printList(cumulativeMaximumDemand_Qs):"no CumulativeMaximumDemand objects")+"\n"+

               "billingPoint (p)= "+(getBillingPoint() != null ? getBillingPoint().toString():"no BillingPoint")+"\n"+
               "AdditionalInformation (I)= "+(getAdditionalInformation()!=null ? getAdditionalInformation().toString():"no AdditionalInformation")+"\n"+
               "CommissioningInformation (K)= "+(getCommissioningInformation()!=null ? getCommissioningInformation().toString():"no CommissioningInformation")+"\n"+
               "EnergyTypeList (J)= "+(getEnergyTypeList()!=null ? getEnergyTypeList().toString():"no EnergyTypeList")+"\n"+
               "Counters (H)= "+(getCounters()!=null ? getCounters().toString():"no Counters")+"\n"+


               "billingPointIdentifiers (0x80)= "+(billingPointIdentifiers != null ? printList(billingPointIdentifiers):"no BillingPointIdentifier objects")+"\n"+
               "channelDefinitionRegisters (0x81)= "+(channelDefinitionRegisters != null ? printList(channelDefinitionRegisters):"no ChannelDefinitionRegister objects")+"\n"+
               "rateRegisterValues (0x82)= "+(rateRegisterValues != null ? printList(rateRegisterValues):"no RateRegisterValue objects")+"\n"+
               "maximumDemandRegisters (0x83)= "+(maximumDemandRegisters != null ? printList(maximumDemandRegisters):"no MaximumDemandRegister objects")+"\n"+
               "cumulativeMaximumDemandRegisters (0x84)= "+(cumulativeMaximumDemandRegisters != null ? printList(cumulativeMaximumDemandRegisters):"no CumulativeMaximumDemandRegister objects")+"\n"+
               "demandScalings (0x85)= "+(demandScalings != null ? printList(demandScalings):"no DemandScaling objects")+"\n"+
               "tariffNameFlags (0x86&0x87)= "+(tariffNameFlags != null ? printList(tariffNameFlags):"no TariffNameFlag objects")+"\n"+
               getObisCodeDescriptions();
    }

    private String printList(List lst) {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("\n");
        Iterator it = lst.iterator();
        while(it.hasNext()) {
            MeterReadingsBlock mrb = (MeterReadingsBlock)it.next();
            strBuff.append(mrb.toString()+"\n");
        }
        return strBuff.toString();
    }

    public boolean isTariffSeriesCLEM() {
        return (getClemProgramName().indexOf("TA")!=-1);
    }
    public boolean isCode5SeriesCLEM() {
        return (getClemProgramName().indexOf("C5")!=-1);
    }
    public boolean isTariffPlusSeriesCLEM() {
        return (getClemProgramName().indexOf("TPA")!=-1);
    }
    public boolean isProfileSeriesCLEM() {
        return ((getClemProgramName().indexOf("RR")!=-1) || (getClemProgramName().indexOf("PR")!=-1));
    }

    private void parseFirstBlocks() {
        // get serialId and CLEM program name
        setSerialId(new String(ProtocolUtils.getSubArray(data,dataPtr,dataPtr+7)));
        dataPtr+=8; // inc one block
        setClemProgramName(new String(ProtocolUtils.getSubArray2(data,dataPtr+1,7)));

        if (isTariffSeriesCLEM()) {
            System.out.println("KV_DEBUG> Tariff Series CLEM program!!!! Obsolete? Tariff series CLEM programs are not fully supported!!");
        }

        dataPtr+=8; // inc one block
    }

    private void parseNextBlocks() {
        // parse further meterreading blocks
        boolean endMarker=false;
        do {
            switch (data[dataPtr]) {

                //************************* General information **************************
                case 'F': {
                    if (DEBUG>=1) {
						System.out.println("KV_DEBUG> Flags and survey information");
					}
                    setSurveyFlagsInfo(new SurveyFlagsInfo(ProtocolUtils.getSubArray(data,dataPtr,dataPtr+7)));
                } break; // F, Flags and survey information

                case 'H': {
                    if (DEBUG>=1) {
						System.out.println("KV_DEBUG> General purpose counters");
					}
                    if (counters == null) {
						setCounters(new Counters(ProtocolUtils.getSubArray(data,dataPtr,dataPtr+7),protocolLink.getTimeZone()));
					} else {
                        getCounters().setData(ProtocolUtils.getSubArray(data,dataPtr,dataPtr+7));
                    }
                } break; // H, General purpose counters

                case 'S': {
                    if (DEBUG>=1) {
						System.out.println("KV_DEBUG> Seeds");
					}
                    setSeeds(new Seeds(ProtocolUtils.getSubArray(data,dataPtr,dataPtr+7)));
                } break; // S, Seeds

                case 'i': {
                    if (DEBUG>=1) {
						System.out.println("KV_DEBUG> General information");
					}
                    setGeneralInformation(new GeneralInformation(ProtocolUtils.getSubArray(data,dataPtr,dataPtr+7)));
                } break; // i, General information

                case 's': {
                    if (DEBUG>=1) {
						System.out.println("KV_DEBUG> Survey information");
					}
                    setSurveyInfo(new SurveyInfo(ProtocolUtils.getSubArray(data,dataPtr,dataPtr+7)));
                } break; // s, Survey information

                case 't': {
                    if (DEBUG>=1) {
						System.out.println("KV_DEBUG> Current tariff name");
					}
                    setCurrentTariffName(new String(ProtocolUtils.getSubArray(data,dataPtr+1,dataPtr+7)));
                } break; // t, Current tariff name

                case '#': {
                    if (DEBUG>=1) {
						System.out.println("KV_DEBUG> Authenticator (end block)");
					}
                    endMarker=true;
                    byte[] authent = ProtocolUtils.getSubArray(data,dataPtr,dataPtr+7);
                    try {
                       int authenticator = ProtocolUtils.getIntLE(authent,1,4);
                    }
                    catch(IOException e) {
                       e.printStackTrace(); // should not happen
                    }

                    // authentication 1..4
                    //int newDataPtr = dataPtr+8;
                    loadSurveyData = ProtocolUtils.getSubArray(data,dataPtr+8); //newDataPtr); // all profile data follows directly after the meterreading blocks
                } break; // #, Authenticator

                //************************* One set of history data **************************
                case 'I': {
                    if (DEBUG>=1) {
						System.out.println("KV_DEBUG> Additional flags information");
					}
                    if (additionalInformation == null) {
						setAdditionalInformation(new AdditionalInformation(ProtocolUtils.getSubArray(data,dataPtr,dataPtr+7)));
					} else {
                        getAdditionalInformation().setData(ProtocolUtils.getSubArray(data,dataPtr,dataPtr+7));
                    }
                } break; // I, Additional flags information

                case 'J': {
                    if (DEBUG>=1) {
						System.out.println("KV_DEBUG> Energy type list");
					}
                    setEnergyTypeList(new EnergyTypeList(ProtocolUtils.getSubArray(data,dataPtr,dataPtr+7)));

                } break; // J, Energy type list

                case 'K': {
                    if (DEBUG>=1) {
						System.out.println("KV_DEBUG> Comissioning information");
					}
                    if (commissioningInformation == null) {
						setCommissioningInformation(new CommissioningInformation(ProtocolUtils.getSubArray(data,dataPtr,dataPtr+7)));
					} else {
                        getCommissioningInformation().setData(ProtocolUtils.getSubArray(data,dataPtr,dataPtr+7));
                    }
                } break; // K, Comissioning information

                case 'Q': {
                    if (DEBUG>=1) {
						System.out.println("KV_DEBUG> Cumulative maximum demand register");
					}
                    if (cumulativeMaximumDemand_Qs == null) {
						cumulativeMaximumDemand_Qs = new ArrayList();
					}
                    cumulativeMaximumDemand_Qs.add(new CumulativeMaximumDemand(ProtocolUtils.getSubArray(data,dataPtr,dataPtr+7)));
                    registersRead=true;

                } break; // Q, Cumulative maximum demand register

                case 'T': {
                    if (DEBUG>=1) {
						System.out.println("KV_DEBUG> Total (unit) register");
					}
                    //setTotalRegister(new TotalRegister(ProtocolUtils.getSubArray(data,dataPtr,dataPtr+7)));
                    if (totalRegisters == null) {
						totalRegisters = new ArrayList();
					}
                    totalRegisters.add(new TotalRegister(ProtocolUtils.getSubArray(data,dataPtr,dataPtr+7)));
                    registersRead=true;

                } break; // T, Total (unit) register

                case 'c': {
                    if (DEBUG>=1) {
						System.out.println("KV_DEBUG> Rate register");
					}
                    if (rateRegisters == null) {
						rateRegisters = new ArrayList();
					}
                    rateRegisters.add(new RateRegister(ProtocolUtils.getSubArray(data,dataPtr,dataPtr+7)));
                    registersRead=true;

                } break; // c, Rate register

                case 'd': {
                    if (DEBUG>=1) {
						System.out.println("KV_DEBUG> Cumulative MD register");
					}
                    if (cumulativeMaximumDemand_ds == null) {
						cumulativeMaximumDemand_ds = new ArrayList();
					}
                    cumulativeMaximumDemand_ds.add(new CumulativeMaximumDemand(ProtocolUtils.getSubArray(data,dataPtr,dataPtr+7)));
                    registersRead=true;
                } break; // d, Cumulative MD register

                case 'h': {
                    if (DEBUG>=1) {
						System.out.println("KV_DEBUG> Time and date of MD");
					}
                    if (timeDateMDs == null) {
						timeDateMDs = new ArrayList();
					}
                    timeDateMDs.add(new TimeDateMD(ProtocolUtils.getSubArray(data,dataPtr,dataPtr+7),getMeterTypeDependentTimeZone()));
                } break; // h, Time and date of MD

                case 'm': {
                    if (DEBUG>=1) {
						System.out.println("KV_DEBUG> Maximum demand register (m)");
					}
                    if (maximumDemand_ms == null) {
						maximumDemand_ms = new ArrayList();
					}
                    maximumDemand_ms.add(new MaximumDemand(ProtocolUtils.getSubArray(data,dataPtr,dataPtr+7),'m'));
                    registersRead=true;

                } break; // m, Maximum demand register

                case 'o': {
                    if (DEBUG>=1) {
						System.out.println("KV_DEBUG> Old tariff name");
					}
                } break; // o, Old tariff name

                case 'p': {
                    if (DEBUG>=1) {
						System.out.println("KV_DEBUG> Billing point and billing kWh register");
					}
                    setBillingPoint(new BillingPoint(ProtocolUtils.getSubArray(data,dataPtr,dataPtr+7),protocolLink.getTimeZone()));
                } break; // p, Billing point and billing kWh register

                case 'q': {
                    if (DEBUG>=1) {
						System.out.println("KV_DEBUG> Maximum demand register (q)");
					}
                    if (maximumDemand_qs == null) {
						maximumDemand_qs = new ArrayList();
					}
                    maximumDemand_qs.add(new MaximumDemand(ProtocolUtils.getSubArray(data,dataPtr,dataPtr+7),'q'));
                    registersRead=true;
                } break; // q, Maximum demand register (q)

                //************************* Many sets of history data **************************
                case (byte)0x80: {
                    if (DEBUG>=1) {
						System.out.println("KV_DEBUG> Billing point identifier");
					}
                    if (billingPointIdentifiers == null) {
						billingPointIdentifiers = new ArrayList();
					}
                    billingPointIdentifiers.add(new BillingPointIdentifier(ProtocolUtils.getSubArray(data,dataPtr,dataPtr+7),protocolLink.getTimeZone()));
                } break; // 0x80, Billing point identifier

                case (byte)0x81: {
                    if (DEBUG>=1) {
						System.out.println("KV_DEBUG> Channel definition and register value");
					}
                    if (channelDefinitionRegisters == null) {
						channelDefinitionRegisters = new ArrayList();
					}
                    channelDefinitionRegisters.add(new ChannelDefinitionRegister(ProtocolUtils.getSubArray(data,dataPtr,dataPtr+7)));
                    registersRead=true;
                } break; // 0x81, Channel definition and register value

                case (byte)0x82: {
                    if (DEBUG>=1) {
						System.out.println("KV_DEBUG> Rate register");
					}
                    if (rateRegisterValues == null) {
						rateRegisterValues = new ArrayList();
					}
                    rateRegisterValues.add(new RateRegisterValue(ProtocolUtils.getSubArray(data,dataPtr,dataPtr+7)));
                    registersRead=true;
                } break; // 0x82, Rate register

                case (byte)0x83: {
                    if (DEBUG>=1) {
						System.out.println("KV_DEBUG> Maximum demand register");
					}
                    if (maximumDemandRegisters == null) {
						maximumDemandRegisters = new ArrayList();
					}
                    maximumDemandRegisters.add(new MaximumDemandRegister(ProtocolUtils.getSubArray(data,dataPtr,dataPtr+7),getMeterTypeDependentTimeZone()));
                    registersRead=true;
                } break; // 0x83, Maximum demand register

                case (byte)0x84: {
                    if (DEBUG>=1) {
						System.out.println("KV_DEBUG> Cumulative MD register");
					}
                    if (cumulativeMaximumDemandRegisters == null) {
						cumulativeMaximumDemandRegisters = new ArrayList();
					}
                    cumulativeMaximumDemandRegisters.add(new CumulativeMaximumDemandRegister(ProtocolUtils.getSubArray(data,dataPtr,dataPtr+7)));
                    registersRead=true;
                } break; // 0x84, Cumulative MD register

                case (byte)0x85: {
                    if (DEBUG>=1) {
						System.out.println("KV_DEBUG> Demand scaling");
					}
                    if (demandScalings == null) {
						demandScalings = new ArrayList();
					}
                    demandScalings.add(new DemandScaling(ProtocolUtils.getSubArray(data,dataPtr,dataPtr+7)));
                } break; // 0x85, Demand scaling

                case (byte)0x86: {
                    if (DEBUG>=1) {
						System.out.println("KV_DEBUG> Tariff name");
					}
                    if (tariffNameFlags == null) {
						tariffNameFlags = new ArrayList();
					}
                    if (tariffNameFlag == null) {
						tariffNameFlag = new TariffNameFlag();
					}
                    tariffNameFlag.parse86(ProtocolUtils.getSubArray(data,dataPtr,dataPtr+7));
                    if (tariffNameFlag.is86Set() && tariffNameFlag.is87Set()) {
                        tariffNameFlags.add(tariffNameFlag);
                        tariffNameFlag=null;
                    }
                } break; // 0x86, Tariff name

                case (byte)0x87: {
                    if (DEBUG>=1) {
						System.out.println("KV_DEBUG> Tariff name and flags");
					}
                    if (tariffNameFlags == null) {
						tariffNameFlags = new ArrayList();
					}
                    if (tariffNameFlag == null) {
						tariffNameFlag = new TariffNameFlag();
					}
                    tariffNameFlag.parse87(ProtocolUtils.getSubArray(data,dataPtr,dataPtr+7));
                    if (tariffNameFlag.is86Set() && tariffNameFlag.is87Set()) {
                        tariffNameFlags.add(tariffNameFlag);
                        tariffNameFlag=null;
                    }
                } break; // 0x87, Tariff name and flags

                case (byte)0x88: {
                    if (DEBUG>=1) {
						System.out.println("KV_DEBUG> Download tariff transaction seed");
					}
                } break; // 0x88, Download tariff transaction seed

                case (byte)0x89: {
                    if (DEBUG>=1) {
						System.out.println("KV_DEBUG> Multi-level maximum demands");
					}
                } break; // 0x89, Multi-level maximum demands

                case (byte)0x8A: {
                    if (DEBUG>=1) {
						System.out.println("KV_DEBUG> Time on and off power");
					}
                } break; // 0x8A, Time on and off power

                case (byte)0x8C: {
                    if (DEBUG>=1) {
						System.out.println("KV_DEBUG> Register snapshot block");
					}
                } break; // 0x8C, Register snapshot block

                case (byte)0x8D: {
                    if (DEBUG>=1) {
						System.out.println("KV_DEBUG> Miscellaneous channel data");
					}
                } break; // 0x8D, Miscellaneous channel data

                case (byte)0x8E: {
                    if (DEBUG>=1) {
						System.out.println("KV_DEBUG> Miscellaneous non-channel data");
					}
                } break; // 0x8E, Miscellaneous non-channel data

                default: {
                    if (DEBUG>=1) {
						System.out.println("KV_DEBUG> !!! unknown block identifier 0x"+Integer.toHexString(ProtocolUtils.byte2int(data[dataPtr])));
					}
                } break; // default
            } // switch (data[dataPtr])

            dataPtr+=8; // inc one block

        } while((dataPtr < data.length) && (!endMarker));
    } // private void parseNextBlocks()

    public RegisterValue getValue(ObisCode obisCode) throws IOException {
        if (!registersRead) {
			throw new NoSuchRegisterException("MeterReadingsInterpreter, getValue, No registers to read!");
		}
        MeterReadingProcessor mrp = new MeterReadingProcessor(this, new MeterReadingIdentifier(obisCode));
        return mrp.getValue();
    }

    public Quantity getValue(MeterReadingIdentifier mrid) throws IOException {
        if (!registersRead) {
			throw new IOException("MeterReadingsInterpreter, getValue, No registers to read!");
		}
        MeterReadingProcessor mrp = new MeterReadingProcessor(this, mrid);
        return mrp.getValue().getQuantity();
    }

    public Quantity getValue(int channelNumber) throws IOException {
        if (!registersRead) {
			throw new IOException("MeterReadingsInterpreter, getValue, No registers to read!");
		}
        MeterReadingProcessor mrp = new MeterReadingProcessor(this, new MeterReadingIdentifier(channelNumber));
        return mrp.getValue().getQuantity();
    }

    public Quantity getValueEType(int eType) throws IOException {
        if (!registersRead) {
			throw new IOException("MeterReadingsInterpreter, getValue, No registers to read!");
		}
        if (EnergyTypeCode.isStatusFlagsChannel(eType)) {
			return new Quantity("0",Unit.get(255));
		}
        MeterReadingProcessor mrp = new MeterReadingProcessor(this, new MeterReadingIdentifier(eType,true));
        return mrp.getValue().getQuantity();
    }

    public int getNrOfReadingChannels() {
        if (getTotalRegisters()!= null) {
			return getTotalRegisters().size();
		} else {
			return getChannelDefinitionRegisters().size();
		}
    }

    /** Getter for property surveyInfo.
     * @return Value of property surveyInfo.
     *
     */
    public SurveyInfo getSurveyInfo() {
        return surveyInfo;
    }

    /** Setter for property surveyInfo.
     * @param surveyInfo New value of property surveyInfo.
     *
     */
    public void setSurveyInfo(SurveyInfo surveyInfo) {
        this.surveyInfo = surveyInfo;
    }

    /** Getter for property surveyFlagsInfo.
     * @return Value of property surveyFlagsInfo.
     *
     */
    public SurveyFlagsInfo getSurveyFlagsInfo() {
        return surveyFlagsInfo;
    }

    /** Setter for property surveyFlagsInfo.
     * @param surveyFlagsInfo New value of property surveyFlagsInfo.
     *
     */
    public void setSurveyFlagsInfo(SurveyFlagsInfo surveyFlagsInfo) {
        this.surveyFlagsInfo = surveyFlagsInfo;
    }

    /** Getter for property serialId.
     * @return Value of property serialId.
     *
     */
    public java.lang.String getSerialId() {
        return serialId;
    }

    /** Setter for property serialId.
     * @param serialId New value of property serialId.
     *
     */
    public void setSerialId(java.lang.String serialId) {
        this.serialId = serialId;
    }

    /** Getter for property clemProgramName.
     * @return Value of property clemProgramName.
     *
     */
    public java.lang.String getClemProgramName() {
        return clemProgramName;
    }

    /** Setter for property clemProgramName.
     * @param clemProgramName New value of property clemProgramName.
     *
     */
    public void setClemProgramName(java.lang.String clemProgramName) {
        this.clemProgramName = clemProgramName;
    }

    /** Getter for property currentTariffName.
     * @return Value of property currentTariffName.
     *
     */
    public java.lang.String getCurrentTariffName() {
        return currentTariffName;
    }

    /** Setter for property currentTariffName.
     * @param currentTariffName New value of property currentTariffName.
     *
     */
    public void setCurrentTariffName(java.lang.String currentTariffName) {
        this.currentTariffName = currentTariffName;
    }

    /** Getter for property loadSurveyData.
     * @return Value of property loadSurveyData.
     *
     */
    public byte[] getLoadSurveyData() {
        return this.loadSurveyData;
    }

    /** Setter for property loadSurveyData.
     * @param loadSurveyData New value of property loadSurveyData.
     *
     */
    public void setLoadSurveyData(byte[] loadSurveyData) {
        this.loadSurveyData = loadSurveyData;
    }

    public LoadSurveyInterpreter getLoadSurveyInterpreter() {
        if (getSurveyFlagsInfo().isAbsoluteSurvey()) {
			return new AbsoluteSurvey(this,protocolLink.getTimeZone());
		}
        if (getSurveyFlagsInfo().isDiscreteSurvey()) {
			return new DiscreteSurvey(this,protocolLink.getTimeZone());
		}
        if (getSurveyFlagsInfo().isBinarySurvey()) {
			return new BinarySurvey(this,protocolLink.getTimeZone());
		}
        if (getSurveyFlagsInfo().isAsciiSurvey()) {
			return new AsciiSurvey(this,protocolLink.getTimeZone());
		}
        if (getSurveyFlagsInfo().isLinkSurvey()) {
			return new LinkSurvey(this,protocolLink.getTimeZone());
		}
        return null;
    }

    /** Getter for property generalInformation.
     * @return Value of property generalInformation.
     *
     */
    public com.energyict.protocolimpl.pact.core.meterreading.GeneralInformation getGeneralInformation() {
        return generalInformation;
    }

    /** Setter for property generalInformation.
     * @param generalInformation New value of property generalInformation.
     *
     */
    public void setGeneralInformation(com.energyict.protocolimpl.pact.core.meterreading.GeneralInformation generalInformation) {
        this.generalInformation = generalInformation;
    }

    /** Getter for property timeDateMDs.
     * @return Value of property timeDateMDs.
     *
     */
    public List getTimeDateMDs() {
        return timeDateMDs;
    }

    /** Getter for property billingPointIdentifiers.
     * @return Value of property billingPointIdentifiers.
     *
     */
    public java.util.List getBillingPointIdentifiers() {
        return billingPointIdentifiers;
    }

    /** Setter for property billingPointIdentifiers.
     * @param billingPointIdentifiers New value of property billingPointIdentifiers.
     *
     */
    public void setBillingPointIdentifiers(java.util.List billingPointIdentifiers) {
        this.billingPointIdentifiers = billingPointIdentifiers;
    }

    /** Getter for property channelDefinitionRegisters.
     * @return Value of property channelDefinitionRegisters.
     *
     */
    public java.util.List getChannelDefinitionRegisters() {
        return channelDefinitionRegisters;
    }

    /** Setter for property channelDefinitionRegisters.
     * @param channelDefinitionRegisters New value of property channelDefinitionRegisters.
     *
     */
    public void setChannelDefinitionRegisters(java.util.List channelDefinitionRegisters) {
        this.channelDefinitionRegisters = channelDefinitionRegisters;
    }

    /** Getter for property rateRegisterValues.
     * @return Value of property rateRegisterValues.
     *
     */
    public java.util.List getRateRegisterValues() {
        return rateRegisterValues;
    }

    /** Setter for property rateRegisterValues.
     * @param rateRegisterValues New value of property rateRegisterValues.
     *
     */
    public void setRateRegisterValues(java.util.List rateRegisterValues) {
        this.rateRegisterValues = rateRegisterValues;
    }

    /** Getter for property maximumDemandRegisters.
     * @return Value of property maximumDemandRegisters.
     *
     */
    public java.util.List getMaximumDemandRegisters() {
        return maximumDemandRegisters;
    }

    /** Setter for property maximumDemandRegisters.
     * @param maximumDemandRegisters New value of property maximumDemandRegisters.
     *
     */
    public void setMaximumDemandRegisters(java.util.List maximumDemandRegisters) {
        this.maximumDemandRegisters = maximumDemandRegisters;
    }

    /** Getter for property cumulativeMaximumDemandRegisters.
     * @return Value of property cumulativeMaximumDemandRegisters.
     *
     */
    public java.util.List getCumulativeMaximumDemandRegisters() {
        return cumulativeMaximumDemandRegisters;
    }

    /** Setter for property cumulativeMaximumDemandRegisters.
     * @param cumulativeMaximumDemandRegisters New value of property cumulativeMaximumDemandRegisters.
     *
     */
    public void setCumulativeMaximumDemandRegisters(java.util.List cumulativeMaximumDemandRegisters) {
        this.cumulativeMaximumDemandRegisters = cumulativeMaximumDemandRegisters;
    }

    /** Getter for property demandScalings.
     * @return Value of property demandScalings.
     *
     */
    public java.util.List getDemandScalings() {
        return demandScalings;
    }

    /** Setter for property demandScalings.
     * @param demandScalings New value of property demandScalings.
     *
     */
    public void setDemandScalings(java.util.List demandScalings) {
        this.demandScalings = demandScalings;
    }

    /** Getter for property tariffNameFlags.
     * @return Value of property tariffNameFlags.
     *
     */
    public java.util.List getTariffNameFlags() {
        return tariffNameFlags;
    }

    /** Setter for property tariffNameFlags.
     * @param tariffNameFlags New value of property tariffNameFlags.
     *
     */
    public void setTariffNameFlags(java.util.List tariffNameFlags) {
        this.tariffNameFlags = tariffNameFlags;
    }


    /** Getter for property additionalInformation.
     * @return Value of property additionalInformation.
     *
     */
    public com.energyict.protocolimpl.pact.core.meterreading.AdditionalInformation getAdditionalInformation() {
        return additionalInformation;
    }

    /** Setter for property additionalInformation.
     * @param additionalInformation New value of property additionalInformation.
     *
     */
    public void setAdditionalInformation(com.energyict.protocolimpl.pact.core.meterreading.AdditionalInformation additionalInformation) {
        this.additionalInformation = additionalInformation;
    }

    /** Getter for property commissioningInformation.
     * @return Value of property commissioningInformation.
     *
     */
    public com.energyict.protocolimpl.pact.core.meterreading.CommissioningInformation getCommissioningInformation() {
        return commissioningInformation;
    }

    /** Setter for property commissioningInformation.
     * @param commissioningInformation New value of property commissioningInformation.
     *
     */
    public void setCommissioningInformation(com.energyict.protocolimpl.pact.core.meterreading.CommissioningInformation commissioningInformation) {
        this.commissioningInformation = commissioningInformation;
    }

    /** Getter for property energyTypeList.
     * @return Value of property energyTypeList.
     *
     */
    public com.energyict.protocolimpl.pact.core.meterreading.EnergyTypeList getEnergyTypeList() {
        return energyTypeList;
    }

    /** Setter for property energyTypeList.
     * @param energyTypeList New value of property energyTypeList.
     *
     */
    public void setEnergyTypeList(com.energyict.protocolimpl.pact.core.meterreading.EnergyTypeList energyTypeList) {
        this.energyTypeList = energyTypeList;
    }

    /** Getter for property counters.
     * @return Value of property counters.
     *
     */
    public com.energyict.protocolimpl.pact.core.meterreading.Counters getCounters() {
        return counters;
    }

    /** Setter for property counters.
     * @param counters New value of property counters.
     *
     */
    public void setCounters(com.energyict.protocolimpl.pact.core.meterreading.Counters counters) {
        this.counters = counters;
    }

    /** Getter for property totalRegisters.
     * @return Value of property totalRegisters.
     *
     */
    public java.util.List getTotalRegisters() {
        return totalRegisters;
    }

    /** Setter for property totalRegisters.
     * @param totalRegisters New value of property totalRegisters.
     *
     */
    public void setTotalRegisters(java.util.List totalRegisters) {
        this.totalRegisters = totalRegisters;
    }

    /** Getter for property cumulativeMaximumDemand_Qs.
     * @return Value of property cumulativeMaximumDemand_Qs.
     *
     */
    public java.util.List getCumulativeMaximumDemand_Qs() {
        return cumulativeMaximumDemand_Qs;
    }

    /** Setter for property cumulativeMaximumDemand_Qs.
     * @param cumulativeMaximumDemand_Qs New value of property cumulativeMaximumDemand_Qs.
     *
     */
    public void setCumulativeMaximumDemand_Qs(java.util.List cumulativeMaximumDemand_Qs) {
        this.cumulativeMaximumDemand_Qs = cumulativeMaximumDemand_Qs;
    }

    /** Getter for property cumulativeMaximumDemand_ds.
     * @return Value of property cumulativeMaximumDemand_ds.
     *
     */
    public java.util.List getCumulativeMaximumDemand_ds() {
        return cumulativeMaximumDemand_ds;
    }

    /** Setter for property cumulativeMaximumDemand_ds.
     * @param cumulativeMaximumDemand_ds New value of property cumulativeMaximumDemand_ds.
     *
     */
    public void setCumulativeMaximumDemand_ds(java.util.List cumulativeMaximumDemand_ds) {
        this.cumulativeMaximumDemand_ds = cumulativeMaximumDemand_ds;
    }

    /** Getter for property maximumDemand_ms.
     * @return Value of property maximumDemand_ms.
     *
     */
    public java.util.List getMaximumDemand_ms() {
        return maximumDemand_ms;
    }

    /** Setter for property maximumDemand_ms.
     * @param maximumDemand_ms New value of property maximumDemand_ms.
     *
     */
    public void setMaximumDemand_ms(java.util.List maximumDemand_ms) {
        this.maximumDemand_ms = maximumDemand_ms;
    }

    /** Getter for property maximumDemand_qs.
     * @return Value of property maximumDemand_qs.
     *
     */
    public java.util.List getMaximumDemand_qs() {
        return maximumDemand_qs;
    }

    /** Setter for property maximumDemand_qs.
     * @param maximumDemand_qs New value of property maximumDemand_qs.
     *
     */
    public void setMaximumDemand_qs(java.util.List maximumDemand_qs) {
        this.maximumDemand_qs = maximumDemand_qs;
    }

    /** Getter for property rateRegisters.
     * @return Value of property rateRegisters.
     *
     */
    public java.util.List getRateRegisters() {
        return rateRegisters;
    }



    /** Getter for property timeZone.
     * @return Value of property timeZone.
     *
     */
    public java.util.TimeZone getTimeZone() {
        return protocolLink.getTimeZone();
    }

    /** Getter for property seeds.
     * @return Value of property seeds.
     *
     */
    public com.energyict.protocolimpl.pact.core.meterreading.Seeds getSeeds() {
        return seeds;
    }

    /** Setter for property seeds.
     * @param seeds New value of property seeds.
     *
     */
    public void setSeeds(com.energyict.protocolimpl.pact.core.meterreading.Seeds seeds) {
        this.seeds = seeds;
    }

    /**
     * Getter for property billingPoint.
     * @return Value of property billingPoint.
     */
    public com.energyict.protocolimpl.pact.core.meterreading.BillingPoint getBillingPoint() {
        return billingPoint;
    }

    /**
     * Setter for property billingPoint.
     * @param billingPoint New value of property billingPoint.
     */
    public void setBillingPoint(com.energyict.protocolimpl.pact.core.meterreading.BillingPoint billingPoint) {
        this.billingPoint = billingPoint;
    }

    /**
     * Getter for property protocolLink.
     * @return Value of property protocolLink.
     */
    public com.energyict.protocolimpl.pact.core.common.ProtocolLink getProtocolLink() {
        return protocolLink;
    }

}
