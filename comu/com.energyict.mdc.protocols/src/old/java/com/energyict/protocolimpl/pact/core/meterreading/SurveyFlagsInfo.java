/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * surveyFlagsInfo.java
 *
 * Created on 11 maart 2004, 10:07
 */

package com.energyict.protocolimpl.pact.core.meterreading;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 * @author  Koen
 */
public class SurveyFlagsInfo extends MeterReadingsBlockImpl {

	private int flags;
	private int fac;
	private int surfac;
	private int surtyp;
    //int blocks;
	private int meterFactorExp;


    /** Creates a new instance of surveyFlagsInfo */
    public SurveyFlagsInfo(byte[] data) {
        super(data);
    }

    public String print() {
        return "FLAGS=0x"+Integer.toHexString(getFlags())+", FAC="+getFac()+" (meterFactorExt="+getMeterFactorExp()+"), SURFAC="+getSurfac()+", SURTYP="+getSurtyp()+", BLOCKS=obsolete";
    }

    protected void parse() throws IOException {
        setFlags(ProtocolUtils.byte2int(getData()[1])+ProtocolUtils.byte2int(getData()[2])*256);
        setFac(ProtocolUtils.byte2int(getData()[3]));
        setMeterFactorExp(getFac()-48);
        setSurfac(ProtocolUtils.byte2int(getData()[4]));
        setSurtyp(ProtocolUtils.byte2int(getData()[5]));
        byte[] strdat = new byte[2];
        strdat[0] = getData()[6];
        strdat[1] = getData()[7];
        //String str = new String(strdat);
        //setBlocks(Integer.parseInt(str));
    }

    /** Getter for property flags.
     * @return Value of property flags.
     *
     */
    public int getFlags() {
        return flags;
    }

    /** Setter for property flags.
     * @param flags New value of property flags.
     *
     */
    public void setFlags(int flags) {
        this.flags = flags;
    }

    /** Getter for property fac.
     * @return Value of property fac.
     *
     */
    public int getFac() {
        return fac;
    }

    /** Setter for property fac.
     * @param fac New value of property fac.
     *
     */
    public void setFac(int fac) {
        this.fac = fac;
    }

    /** Getter for property surfac.
     * @return Value of property surfac.
     *
     */
    public int getSurfac() {
        return surfac;
    }

    /** Setter for property surfac.
     * @param surfac New value of property surfac.
     *
     */
    public void setSurfac(int surfac) {
        this.surfac = surfac;
    }

    /** Getter for property surtyp.
     * @return Value of property surtyp.
     *
     */
    public int getSurtyp() {
        return surtyp;
    }

    /** Setter for property surtyp.
     * @param surtyp New value of property surtyp.
     *
     */
    public void setSurtyp(int surtyp) {
        this.surtyp = surtyp;
    }


    public boolean isLinkSurvey() {
        return ((getSurtyp()>=18) && (getSurtyp()<=19));
    }
    public boolean isAsciiSurvey() {
        return ((getSurtyp()>=0) && (getSurtyp()<=1));
    }
    public boolean isBinarySurvey() {
        return (((getSurtyp()>=2) && (getSurtyp()<=7)) ||
                ((getSurtyp()>=9) && (getSurtyp()<=15)) ||
                ((getSurtyp()>=20) && (getSurtyp()<=21)));
    }
    public boolean isAbsoluteSurvey() {
        return ((getSurtyp()>=22) && (getSurtyp()<=37));
    }
    public boolean isDiscreteSurvey() {
        return ((getSurtyp()>=38) && (getSurtyp()<=40));
    }




    /** Getter for property meterFactorExp.
     * @return Value of property meterFactorExp.
     *
     */
    public int getMeterFactorExp() {
        return meterFactorExp;
    }

    public BigDecimal getMeterFactor() {
       return new BigDecimal(Math.pow(10, getMeterFactorExp()));
    }
    /** Setter for property meterFactorExp.
     * @param meterFactorExp New value of property meterFactorExp.
     *
     */
    public void setMeterFactorExp(int meterFactorExp) {
        this.meterFactorExp = meterFactorExp;
    }

}
