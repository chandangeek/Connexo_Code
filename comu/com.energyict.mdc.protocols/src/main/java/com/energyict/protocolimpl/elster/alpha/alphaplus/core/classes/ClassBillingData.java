/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ClassBillingData.java
 *
 * Created on 13 juli 2005, 14:54
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphaplus.core.classes;

import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.elster.alpha.core.classes.ClassParseUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author Koen
 */
abstract public class ClassBillingData extends AbstractClass {

    abstract protected ClassIdentification getClassIdentification();

    // rates 0..3, 4 rates = rate A, rate B, rate C, rate D
    // TOU blocks 0..1, 2 TOU blocks
    // non TOU meters use rate A as total!

    private static final int MAX_RATES=4;
    private static final int MAX_BLOCKS=2;

    BigDecimal[][] KWH = new BigDecimal[MAX_BLOCKS][MAX_RATES]; // total kwh (non TOU) or rate A kwh for TOU block 1
    BigDecimal[][] KW = new BigDecimal[MAX_BLOCKS][MAX_RATES]; // maximum demand (non TOU) or demand for rate A in TOU block 1
    Date[][] TD = new Date[MAX_BLOCKS][MAX_RATES]; // date & time of the max demand
    BigDecimal[][] KWCUM = new BigDecimal[MAX_BLOCKS][MAX_RATES]; // cumulative demand (non TOU) or rate A cumulative demand for TOU block 1
    BigDecimal[][] KWC = new BigDecimal[MAX_BLOCKS][MAX_RATES]; // coincident demand (non TOU) or rate A coincident demand for TOU block 1

    BigDecimal EKVARH4;
    BigDecimal EKVARH3;
    BigDecimal EKVARH2;
    BigDecimal EKVARH1;
    BigDecimal ETKWH1;
    BigDecimal ETKWH2;
    BigDecimal EAVGPF;



    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("Class "+getClassIdentification().getId()+" (billing data): \n");
        for (int block=0; block<MAX_BLOCKS;block++) {
            for (int rate=0; rate<MAX_RATES;rate++) {
                strBuff.append("KWH["+block+"]["+rate+"]="+KWH[block][rate]+
                               ", KW["+block+"]["+rate+"]="+KW[block][rate]+
                               ", TD["+block+"]["+rate+"]="+TD[block][rate]+
                               ", KWCUM["+block+"]["+rate+"]="+KWCUM[block][rate]+
                               ", KWC["+block+"]["+rate+"]="+KWC[block][rate]+"\n");
            } // for (int rate=0; rate<MAX_RATES;rate++)
        } // for (int block=0; block<MAX_BLOCKS;block++)

        strBuff.append("EKVARH4="+EKVARH4+", EKVARH3="+EKVARH3+", EKVARH2="+EKVARH2+", EKVARH1="+EKVARH1+", ETKWH2="+ETKWH2+", ETKWH1="+ETKWH1+", EAVGPF="+EAVGPF+"\n");

        return strBuff.toString();
    }

    /** Creates a new instance of Class11BillingData */
    public ClassBillingData(ClassFactory classFactory) {
        super(classFactory);
    }

    protected void parse(byte[] data) throws IOException {
        int size = 7+3+5+3+3;
        for (int block=0; block<MAX_BLOCKS;block++) {
            for (int rate=0; rate<MAX_RATES;rate++) {
                KWH[block][rate] = getValue(data,size*block*MAX_RATES+size*rate,7,true);
                KW[block][rate] = getValue(data,size*block*MAX_RATES+size*rate+7,3,false);
                TD[block][rate] = ClassParseUtils.getDate5(data,size*block*MAX_RATES+size*rate+10, classFactory.alpha.getTimeZone());
                KWCUM[block][rate] = getValue(data,size*block*MAX_RATES+size*rate+15,3,false);
                KWC[block][rate] = getValue(data,size*block*MAX_RATES+size*rate+18,3,false);
            } // for (int rate=0; rate<MAX_RATES;rate++)
        } // for (int block=0; block<MAX_BLOCKS;block++)

        EKVARH4 = getValue(data,size*(MAX_BLOCKS-1)*MAX_RATES+size*(MAX_RATES-1)+21,7,true);
        EKVARH3 = getValue(data,size*(MAX_BLOCKS-1)*MAX_RATES+size*(MAX_RATES-1)+28,7,true);
        EKVARH2 = getValue(data,size*(MAX_BLOCKS-1)*MAX_RATES+size*(MAX_RATES-1)+35,7,true);
        EKVARH1 = getValue(data,size*(MAX_BLOCKS-1)*MAX_RATES+size*(MAX_RATES-1)+42,7,true);
        ETKWH1 = getValue(data,size*(MAX_BLOCKS-1)*MAX_RATES+size*(MAX_RATES-1)+49,7,true);
        ETKWH2 = getValue(data,size*(MAX_BLOCKS-1)*MAX_RATES+size*(MAX_RATES-1)+56,7,true);
        EAVGPF  = BigDecimal.valueOf(ParseUtils.getBCD2Long(data,size*(MAX_BLOCKS-1)*MAX_RATES+size*(MAX_RATES-1)+63,2), 3);
    } // protected void parse(byte[] data) throws IOException

    private BigDecimal getValue(byte[] data, int offset, int length, boolean energy) throws IOException {
        int decimalPoint = energy ? classFactory.getClass0ComputationalConfiguration().getDPLOCE():classFactory.getClass0ComputationalConfiguration().getDPLOCD();
        return BigDecimal.valueOf(ParseUtils.getBCD2Long(data,offset,length),energy ? (6+decimalPoint):(decimalPoint));
    }

    public BigDecimal getEKVARH4() {
        return EKVARH4;
    }

    public BigDecimal getEKVARH3() {
        return EKVARH3;
    }

    public BigDecimal getEKVARH2() {
        return EKVARH2;
    }

    public BigDecimal getEKVARH1() {
        return EKVARH1;
    }

    public BigDecimal getETKWH1() {
        return ETKWH1;
    }

    public BigDecimal getETKWH2() {
        return ETKWH2;
    }

    public BigDecimal getEAVGPF() {
        return EAVGPF;
    }
    public BigDecimal getKWH(int block, int rate) {
        return KWH[block][rate];
    }
    public BigDecimal getKW(int block, int rate) {
        return KW[block][rate];
    }
    public Date getTD(int block, int rate) {
        return TD[block][rate];
    }
    public BigDecimal getKWCUM(int block, int rate) {
        return KWCUM[block][rate];
    }
    public BigDecimal getKWC(int block, int rate) {
        return KWC[block][rate];
    }

} // abstract public class ClassBillingData extends AbstractClass
