/*
 * CommissioningInformation.java
 *
 * Created on 22 maart 2004, 16:52
 */

package com.energyict.protocolimpl.pact.core.meterreading;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.pact.core.common.MeterType;
/**
 *
 * @author  Koen
 */
public class CommissioningInformation extends MeterReadingsBlockImpl {

	private int iPrimary;
	private int vPrimary;

	private int redCTAdj,yelCTAdj,blueCTAdj;

	private int redVTAdj,yelVTAdj,blueVTAdj;

	private int hware;
	private int io;
	private int owner;
	private int rangeAndClass;
	private int range;
	private int meterClass;
	private MeterType meterType;
	private int diAndFac;
	private int di;
	private int meterFactor;

	private int mask;

	private boolean mw=false;
	private double voltage;
	private int current;

    /** Creates a new instance of CommissioningInformation */
    public CommissioningInformation(byte[] data) {
        super(data);
    }

    protected void parse() throws java.io.IOException {
        int type = ProtocolUtils.byte2int(getData()[1]);

        if (type == 0) {
            setIPrimary(ProtocolUtils.getBCD2Int(getData(),2,2));
            setVPrimary(ProtocolUtils.getBCD2Int(getData(),4,2));
        }
        else if (type == 1) {
            setRedCTAdj(ProtocolUtils.getIntLE(getData(),2,2));
            setYelCTAdj(ProtocolUtils.getIntLE(getData(),4,2));
            setBlueCTAdj(ProtocolUtils.getIntLE(getData(),6,2));
        }
        else if (type == 2) {
            setRedVTAdj(ProtocolUtils.getIntLE(getData(),2,2));
            setYelVTAdj(ProtocolUtils.getIntLE(getData(),4,2));
            setBlueVTAdj(ProtocolUtils.getIntLE(getData(),6,2));
        }
        else if (type == 3) {
            setMeterType(MeterType.getMeterType(ProtocolUtils.byte2int(getData()[2])));
            setHware(ProtocolUtils.byte2int(getData()[3]));
            setIo(ProtocolUtils.byte2int(getData()[4]));
            setOwner(ProtocolUtils.byte2int(getData()[5]));
            setDiAndFac(ProtocolUtils.byte2int(getData()[6]));
            setDi(getDiAndFac()>>4);
            setMeterFactor(((getDiAndFac()&0x0F)-6));
            setRangeAndClass(ProtocolUtils.byte2int(getData()[7]));
            setRange(getRangeAndClass()>>4);
            setMeterClass(getRangeAndClass()&0x0F);
        }
        mask |= (0x01 << type);

        // if we got iprimary, vprimary and metertype
        if ((mask&0x09) == 0x09) {
            setCurrent(getIPrimary());
            setVoltage(getVPrimary());
            // process voltage
            if (getVPrimary() == 0) {
				setVoltage(getMeterType().getMeasuredVoltage());
			}
            int exp = (getVPrimary() / 1000) - 11;
            if (exp >= -1) {
                double multiplier = Math.pow(10,exp);
                setVoltage((int)((getVPrimary()%1000) * multiplier));
            } else {
				setVoltage(getVPrimary());
			}

            double maxPower = getMeterType().getMultiplier() * getVoltage() * getCurrent();
            if (maxPower > 1000000) {
                setMw(true);
            }
            else {
                setMw(false);
            }
        }

    } // protected void parse()

    protected String print() {
       StringBuffer strBuff = new StringBuffer();
       boolean pre = false;

       if ((mask & 0x0001) == 0x0001) {
           strBuff.append("I_PRIMARY="+getIPrimary()+", V_PRIMARY="+getVPrimary());
           pre = true;
       }
       if ((mask & 0x0002) == 0x0002) {
           if (pre) {
			strBuff.append(", ");
		}
           strBuff.append("REDCTADJ="+getRedCTAdj()+", YELCTADJ="+getYelCTAdj()+", BLUECTADJ="+getBlueCTAdj());
           pre = true;
       }
       if ((mask & 0x0004) == 0x0004) {
           if (pre) {
			strBuff.append(", ");
		}
           strBuff.append("REDVTADJ="+getRedVTAdj()+", YELVTADJ="+getYelVTAdj()+", BLUEVTADJ="+getBlueVTAdj());
           pre = true;
       }
       if ((mask & 0x0008) == 0x0008) {
           if (pre) {
			strBuff.append(", ");
		}
           strBuff.append("HWARE="+getHware()+", OWNER="+getOwner()+", IO=0x"+Integer.toHexString(getIo())+", R&C=0x"+Integer.toHexString(getRangeAndClass())+", (RANGE="+getRange()+", CLASS="+getMeterClass()+"),MT="+getMeterType().getType()+"("+getMeterType()+"), D&F=0x"+Integer.toHexString(getDiAndFac())+", (DI="+getDi()+", METERFACTOR="+getMeterFactor()+")");
           pre = true;
       }

       // if we got iprimary, vprimary and metertype
       if ((mask & 0x0009) == 0x0009) {
           if (pre) {
			strBuff.append(", ");
		}
           strBuff.append("current="+getCurrent()+", voltage="+getVoltage()+", mW commissioned="+isMw());
           pre = true;
       }


        return strBuff.toString();
    }

    /** Getter for property iPrimary.
     * @return Value of property iPrimary.
     *
     */
    public int getIPrimary() {
        return iPrimary;
    }

    /** Setter for property iPrimary.
     * @param iPrimary New value of property iPrimary.
     *
     */
    public void setIPrimary(int iPrimary) {
        this.iPrimary = iPrimary;
    }

    /** Getter for property vPrimary.
     * @return Value of property vPrimary.
     *
     */
    public int getVPrimary() {
        return vPrimary;
    }

    /** Setter for property vPrimary.
     * @param vPrimary New value of property vPrimary.
     *
     */
    public void setVPrimary(int vPrimary) {
        this.vPrimary = vPrimary;
    }

    /** Getter for property redCTAdj.
     * @return Value of property redCTAdj.
     *
     */
    public int getRedCTAdj() {
        return redCTAdj;
    }

    /** Setter for property redCTAdj.
     * @param redCTAdj New value of property redCTAdj.
     *
     */
    public void setRedCTAdj(int redCTAdj) {
        this.redCTAdj = redCTAdj;
    }

    /** Getter for property yelCTAdj.
     * @return Value of property yelCTAdj.
     *
     */
    public int getYelCTAdj() {
        return yelCTAdj;
    }

    /** Setter for property yelCTAdj.
     * @param yelCTAdj New value of property yelCTAdj.
     *
     */
    public void setYelCTAdj(int yelCTAdj) {
        this.yelCTAdj = yelCTAdj;
    }

    /** Getter for property blueCTAdj.
     * @return Value of property blueCTAdj.
     *
     */
    public int getBlueCTAdj() {
        return blueCTAdj;
    }

    /** Setter for property blueCTAdj.
     * @param blueCTAdj New value of property blueCTAdj.
     *
     */
    public void setBlueCTAdj(int blueCTAdj) {
        this.blueCTAdj = blueCTAdj;
    }

    /** Getter for property redVTAdj.
     * @return Value of property redVTAdj.
     *
     */
    public int getRedVTAdj() {
        return redVTAdj;
    }

    /** Setter for property redVTAdj.
     * @param redVTAdj New value of property redVTAdj.
     *
     */
    public void setRedVTAdj(int redVTAdj) {
        this.redVTAdj = redVTAdj;
    }

    /** Getter for property yelVTAdj.
     * @return Value of property yelVTAdj.
     *
     */
    public int getYelVTAdj() {
        return yelVTAdj;
    }

    /** Setter for property yelVTAdj.
     * @param yelVTAdj New value of property yelVTAdj.
     *
     */
    public void setYelVTAdj(int yelVTAdj) {
        this.yelVTAdj = yelVTAdj;
    }

    /** Getter for property blueVTAdj.
     * @return Value of property blueVTAdj.
     *
     */
    public int getBlueVTAdj() {
        return blueVTAdj;
    }

    /** Setter for property blueVTAdj.
     * @param blueVTAdj New value of property blueVTAdj.
     *
     */
    public void setBlueVTAdj(int blueVTAdj) {
        this.blueVTAdj = blueVTAdj;
    }

    /** Getter for property io.
     * @return Value of property io.
     *
     */
    public int getIo() {
        return io;
    }

    /** Setter for property io.
     * @param io New value of property io.
     *
     */
    public void setIo(int io) {
        this.io = io;
    }

    /** Getter for property rangeAndClass.
     * @return Value of property rangeAndClass.
     *
     */
    public int getRangeAndClass() {
        return rangeAndClass;
    }

    /** Setter for property rangeAndClass.
     * @param rangeAndClass New value of property rangeAndClass.
     *
     */
    public void setRangeAndClass(int rangeAndClass) {
        this.rangeAndClass = rangeAndClass;
    }

    /** Getter for property range.
     * @return Value of property range.
     *
     */
    public int getRange() {
        return range;
    }

    /** Setter for property range.
     * @param range New value of property range.
     *
     */
    public void setRange(int range) {
        this.range = range;
    }

    /** Getter for property meterClass.
     * @return Value of property meterClass.
     *
     */
    public int getMeterClass() {
        return meterClass;
    }

    /** Setter for property meterClass.
     * @param meterClass New value of property meterClass.
     *
     */
    public void setMeterClass(int meterClass) {
        this.meterClass = meterClass;
    }



    /** Getter for property diAndFac.
     * @return Value of property diAndFac.
     *
     */
    public int getDiAndFac() {
        return diAndFac;
    }

    /** Setter for property diAndFac.
     * @param diAndFac New value of property diAndFac.
     *
     */
    public void setDiAndFac(int diAndFac) {
        this.diAndFac = diAndFac;
    }

    /** Getter for property di.
     * @return Value of property di.
     *
     */
    public int getDi() {
        return di;
    }

    /** Setter for property di.
     * @param di New value of property di.
     *
     */
    public void setDi(int di) {
        this.di = di;
    }

    /** Getter for property meterFactor.
     * @return Value of property meterFactor.
     *
     */
    public int getMeterFactor() {
        return meterFactor;
    }

    /** Setter for property meterFactor.
     * @param meterFactor New value of property meterFactor.
     *
     */
    public void setMeterFactor(int meterFactor) {
        this.meterFactor = meterFactor;
    }

    /** Getter for property hware.
     * @return Value of property hware.
     *
     */
    public int getHware() {
        return hware;
    }

    /** Setter for property hware.
     * @param hware New value of property hware.
     *
     */
    public void setHware(int hware) {
        this.hware = hware;
    }

    /** Getter for property owner.
     * @return Value of property owner.
     *
     */
    public int getOwner() {
        return owner;
    }

    /** Setter for property owner.
     * @param owner New value of property owner.
     *
     */
    public void setOwner(int owner) {
        this.owner = owner;
    }

    /** Getter for property meterType.
     * @return Value of property meterType.
     *
     */
    public com.energyict.protocolimpl.pact.core.common.MeterType getMeterType() {
        return meterType;
    }

    /** Setter for property meterType.
     * @param meterType New value of property meterType.
     *
     */
    public void setMeterType(com.energyict.protocolimpl.pact.core.common.MeterType meterType) {
        this.meterType = meterType;
    }

    /** Getter for property mw.
     * @return Value of property mw.
     *
     */
    public boolean isMw() {
        return mw;
    }

    /** Setter for property mw.
     * @param mw New value of property mw.
     *
     */
    public void setMw(boolean mw) {
        this.mw = mw;
    }



    /** Getter for property current.
     * @return Value of property current.
     *
     */
    public int getCurrent() {
        return current;
    }

    /** Setter for property current.
     * @param current New value of property current.
     *
     */
    public void setCurrent(int current) {
        this.current = current;
    }

    /** Getter for property voltage.
     * @return Value of property voltage.
     *
     */
    public double getVoltage() {
        return voltage;
    }

    /** Setter for property voltage.
     * @param voltage New value of property voltage.
     *
     */
    public void setVoltage(double voltage) {
        this.voltage = voltage;
    }

}
