/*
 * MeterReadingIdentifier.java
 *
 * Created on 22 maart 2004, 11:13
 */

package com.energyict.protocolimpl.pact.core.meterreading;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.pact.core.common.EnergyTypeCode;

import java.io.IOException;
/**
 *
 * @author  Koen
 */
public class MeterReadingIdentifier {

    public static final String TOTAL="TOTAL";
    public static final String RATE="RATE";
    public static final String MAXIMUM_DEMAND="MD";
    public static final String CUMULATIVE_MAXIMUM_DEMAND="CMD";
    public static final String BILLING_COUNTER="BPCOUNT";
    public static final String BILLING_TIMESTAMP="BPTIMESTAMP";
	private String strReg;

	private int id=0; // 0 based
	private int registerNumber=0; // 0 based
	private int bpIndex=0; // 0 based
	private int triggerChannel=0; // 0 based, 0 = current value
	private String strMRId;
	private int mode=0;
	private boolean idETyped=false;

	private StringBuffer obisTranslation=new StringBuffer();
	private ObisCode obisCode;

    /** Creates a new instance of MeterReadingIdentifier
     *
     *  @param strMRId format e.g. MD.0:0,1,1,1  (type.mode:(c|r)(e)channelNumber,registerNumber,bPIndex,triggerchannel)
     *  Default values: mode=0,id=0, registerNumber=0, bPIndex=0, triggerchannel=-1
     *  mode=0 = default
     *  mode=1 = calculate using scaler and divisor
     *
     *  (c|r)id = channelNumber (multiple set) or registernumber (single set)
     *  eid = Use energy type code to search parameter. For multiple set reading data, ChannelDefinition blocks (0x81) are
     *        used. If single set, TotalRegister blocks are used.
     *
     *
     *  e.g. REG:0  get channel 0 register value, no calculation
     *       REG.1:0 get channel 0 register value multiplied by the meterfactor
     *
     *  In case of single set historical data, only channelNumber is used as registernumber!!!
     *
     */

    public MeterReadingIdentifier(ObisCode obisCode) throws IOException {
        this.obisCode=obisCode;
        parse(obisCode);
    }

    public MeterReadingIdentifier(String strMRId) throws IOException {
        this.strMRId=strMRId;
        parse();
    }

    public MeterReadingIdentifier(int channelNumber) throws IOException {
        this(channelNumber,false);
    }

    public MeterReadingIdentifier(int id, boolean idETyped) throws IOException {
        setIdETyped(idETyped);
        if (isIdETyped()) {
			strMRId=TOTAL+".1:et"+id;
		} else {
			strMRId=TOTAL+".1:ch"+id;
		}
        parse();
    }

    public String toString() {
        return getObisCode().toString();
    }

    public String toString2() {
        return "mode="+getMode()+", id="+getId()+", register="+getRegisterNumber()+", bpIndex="+getBpIndex()+", trigger="+getTriggerChannel()+", EITyped="+isIdETyped();
    }

    public boolean isBillingTimestamp() {
        return (getStrReg().indexOf(BILLING_TIMESTAMP) != -1);
    }
    public boolean isBillingCounter() {
        return (getStrReg().indexOf(BILLING_COUNTER) != -1);
    }
    public boolean isTotal() {
        return (getStrReg().indexOf(TOTAL) != -1);
    }
    public boolean isRate() {
        return (getStrReg().indexOf(RATE) != -1);
    }
    public boolean isMaximumDemand() {
        return (getStrReg().indexOf(MAXIMUM_DEMAND) != -1);
    }
    public boolean isCumulativeMaximumDemand() {
        return (getStrReg().indexOf(CUMULATIVE_MAXIMUM_DEMAND) != -1);
    }

    public boolean isModeCalculate() {
        return (getMode() == 1);
    }

    public static final String GENERAL_PURPOSE_CODES = "1.1.0.1.0.255 (billing counter)\n1.1.0.1.2.x (billing point timestamp, x=billingpoint with 0 = VZ, 1 = VZ-1...)\n";

    private void parse(ObisCode obisCode) throws IOException {

        // *********************************************************************************
        // General purpose ObisRegisters
        if (obisCode.toString().indexOf("1.1.0.1.0.255") != -1) { // billing counter
            setStrReg(BILLING_COUNTER);
            obisTranslation.append("billing counter");
            return;
        } // billing counter
        else if (obisCode.toString().indexOf("1.1.0.1.2.") != -1) { // billing point timestamp
            setStrReg(BILLING_TIMESTAMP);
            if ((obisCode.getF()  >= 0) && (obisCode.getF() <= 99)) {
                setBpIndex(obisCode.getF()+1);
                obisTranslation.append("billing point "+(obisCode.getF())+" timestamp");
                return;
            }
            else if ((obisCode.getF()  <=0) && (obisCode.getF() >= -99)) {
                setBpIndex(obisCode.getF()*-1+1);
                obisTranslation.append("billing point "+(obisCode.getF()*-1)+" timestamp");
                return;
            } else {
				throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
			}
        } // // billing point timestamp


        // *********************************************************************************
        // Electricity related ObisRegisters

        // do calculation in engineering units!
        setMode(1);

        // verify a & b
        if ((obisCode.getA() != 1) || (obisCode.getB() == 0)) {
			throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
		} else {
			setTriggerChannel(obisCode.getB()-1);
		}


        // verify c
        if (EnergyTypeCode.getPacsEtypeCode(obisCode.getC()) == -1) {
			throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
		}
        setIdETyped(true);
        setId(EnergyTypeCode.getPacsEtypeCode(obisCode.getC()));



        // verify d
        if (obisCode.getD() == 2) {// cumulative maximum 1  CMD
           setStrReg(CUMULATIVE_MAXIMUM_DEMAND);
           obisTranslation.append(EnergyTypeCode.getCompountInfoFromObisC(obisCode.getC(),false));
           obisTranslation.append(", cumulative maximum demand");
        }
        else if (obisCode.getD() == 6) {// maximum 1             MD
           setStrReg(MAXIMUM_DEMAND);
           obisTranslation.append(EnergyTypeCode.getCompountInfoFromObisC(obisCode.getC(),false));
           obisTranslation.append(", maximum demand");
        }
        else if (obisCode.getD() == 8) {// time integral 1         TOTAL & RATE
           if (obisCode.getE() == 0) {
			setStrReg(TOTAL);
		} else {
			setStrReg(RATE);
		}
           obisTranslation.append(EnergyTypeCode.getCompountInfoFromObisC(obisCode.getC(),true));
        } else {
			throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
		}

        // verify e
        // 0 : total registers (single set) or 0x81 (multiple set)
        // 1..n rate registers (single set) or 0x82 (multiple set)

        if (obisCode.getE()==0) {
           setRegisterNumber(obisCode.getE());
           obisTranslation.append(", total");
        }
        else if (obisCode.getE()>0) {
           setRegisterNumber(obisCode.getE());
           obisTranslation.append(", rate "+obisCode.getE());
        }

        int billingPoint;
        if ((obisCode.getF()  >=0) && (obisCode.getF() <= 99)) {
			billingPoint = obisCode.getF();
		} else if ((obisCode.getF()  <=0) && (obisCode.getF() >= -99)) {
			billingPoint = obisCode.getF()*-1;
		} else if (obisCode.getF() == 255) {
			billingPoint = -1;
		} else {
			throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
		}

        if (billingPoint == -1) {
            setBpIndex(0);
            obisTranslation.append(", current value");
        }
        else {
            setBpIndex(billingPoint+1);
            obisTranslation.append(", billing point "+(billingPoint+1));
        }

    }

    public String getObisRegisterMappingDescription() {
        return obisTranslation.toString();
    }

    private void parse() throws IOException {
        try {
            int valuesIndex = strMRId.indexOf(":");
            if (valuesIndex == -1) {
				throw new IOException("Invalid meter readings identifier "+strMRId);
			}

            setStrReg(strMRId.substring(0,valuesIndex));
            if (!(isTotal() || isCumulativeMaximumDemand() || isMaximumDemand() || isRate())) {
				throw new IOException("Invalid meter readings identifier "+strMRId);
			}

            int modeIndex = getStrReg().indexOf(".");
            String strMode = modeIndex == -1 ? "0":getStrReg().substring(modeIndex+1);
            setMode(Integer.parseInt(strMode));

            String strIdentifier = strMRId.substring(valuesIndex+1);

            setIdETyped(false);

            String strId = strIdentifier;
            if (strId.toLowerCase().indexOf("re")!=-1) {
                setRegisterNumber(getParameter("re"));
                // also set id to registerNumber in case we have to do with single set historical data
                setId(getRegisterNumber());
            }
            if (strId.toLowerCase().indexOf("ch")!=-1) {
                // override id if a specific channelNumber is given in case of multiple sets of historical data
                setId(getParameter("ch"));
            }
            if (strId.toLowerCase().indexOf("et")!=-1) {
                setIdETyped(true);
                // override id cause we should search for an energy type code
                setId(getParameter("et"));
            }
            if (strId.toLowerCase().indexOf("bp")!=-1) {
                setBpIndex(getParameter("bp"));
            }
            if (strId.toLowerCase().indexOf("tc")!=-1) {
                setTriggerChannel(getParameter("tc"));
            }
        }
        catch(NumberFormatException e) {
            throw new IOException("Invalid meter readings identifier "+strMRId+", "+e.toString());
        }
    } // private parse()

    private int getParameter(String param) {
        int index = strMRId.indexOf(param);
        if (index == -1) {
			return -1;
		}
        String sub = strMRId.substring(index+2);
        index = sub.indexOf(",");
        if (index != -1) {
			return Integer.parseInt(sub.substring(0,index));
		} else {
			return Integer.parseInt(sub);
		}
    }

    /** Getter for property registerNumber.
     * @return Value of property registerNumber.
     *
     */
    public int getRegisterNumber() {
        return registerNumber;
    }

    /** Setter for property registerNumber.
     * @param registerNumber New value of property registerNumber.
     *
     */
    public void setRegisterNumber(int registerNumber) {
        this.registerNumber = registerNumber;
    }



    /** Getter for property triggerChannel.
     * @return Value of property triggerChannel.
     *
     */
    public int getTriggerChannel() {
        return triggerChannel;
    }

    /** Setter for property triggerChannel.
     * @param triggerChannel New value of property triggerChannel.
     *
     */
    public void setTriggerChannel(int triggerChannel) {
        this.triggerChannel = triggerChannel;
    }

    /** Getter for property strReg.
     * @return Value of property strReg.
     *
     */
    public java.lang.String getStrReg() {
        return strReg;
    }

    /** Setter for property strReg.
     * @param strReg New value of property strReg.
     *
     */
    public void setStrReg(java.lang.String strReg) {
        this.strReg = strReg;
    }

    /** Getter for property bpIndex.
     * @return Value of property bpIndex.
     *
     */
    public int getBpIndex() {
        return bpIndex;
    }

    /** Setter for property bpIndex.
     * @param bpIndex New value of property bpIndex.
     *
     */
    public void setBpIndex(int bpIndex) {
        this.bpIndex = bpIndex;
    }

    /** Getter for property mode.
     * @return Value of property mode.
     *
     */
    public int getMode() {
        return mode;
    }

    /** Setter for property mode.
     * @param mode New value of property mode.
     *
     */
    public void setMode(int mode) {
        this.mode = mode;
    }

    /** Getter for property id.
     * @return Value of property id.
     *
     */
    public int getId() {
        return id;
    }

    /** Setter for property id.
     * @param id New value of property id.
     *
     */
    public void setId(int id) {
        this.id = id;
    }

    /** Getter for property idETyped.
     * @return Value of property idETyped.
     *
     */
    public boolean isIdETyped() {
        return idETyped;
    }

    /** Setter for property idETyped.
     * @param idETyped New value of property idETyped.
     *
     */
    public void setIdETyped(boolean idETyped) {
        this.idETyped = idETyped;
    }

    /**
     * Getter for property obisCode.
     * @return Value of property obisCode.
     */
    public ObisCode getObisCode() {
        return obisCode;
    }

}
