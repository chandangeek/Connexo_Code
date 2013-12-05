/*
 * IntervalFormat.java
 *
 * Created on 8 november 2005, 13:58
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 * @author Koen
 */
public class IntervalFormat {

    private Number value;

    static public final int UINT8 = 1;
    static public final int UINT16 = 2;
    static public final int UINT32 = 4;
    static public final int INT8 = 8;
    static public final int INT16 = 16;
    static public final int INT32 = 32;
    static public final int NI_FORMAT1 = 64;
    static public final int NI_FORMAT2 = 128;


    /** Creates a new instance of IntervalFormat */
    public IntervalFormat(byte[] data, int offset, TableFactory tableFactory, int set) throws IOException {
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        int format=0;
        if (set==0)
           format = tableFactory.getC12ProtocolLink().getStandardTableFactory().getLoadProfileControlTable().getIntervalFormatCode1();
        if (set==1)
           format = tableFactory.getC12ProtocolLink().getStandardTableFactory().getLoadProfileControlTable().getIntervalFormatCode2();
        if (set==2)
           format = tableFactory.getC12ProtocolLink().getStandardTableFactory().getLoadProfileControlTable().getIntervalFormatCode3();
        if (set==3)
           format = tableFactory.getC12ProtocolLink().getStandardTableFactory().getLoadProfileControlTable().getIntervalFormatCode4();

        switch(format) {
            case 1: // UINT8

                value = BigDecimal.valueOf((long)C12ParseUtils.getInt(data,offset));
                break;
            case 2: // UINT16
                value = BigDecimal.valueOf((long)C12ParseUtils.getInt(data,offset,2,dataOrder));
                break;
            case 4: // UINT32
                value = BigDecimal.valueOf(C12ParseUtils.getLong(data,offset,4,dataOrder));
                break;
            case 8: // INT8
                value = BigDecimal.valueOf((long)C12ParseUtils.getExtendedLong(data,offset));
                break;
            case 16: // INT16
                value = BigDecimal.valueOf((long)C12ParseUtils.getExtendedLong(data,offset,2,dataOrder));
                break;
            case 32: // INT32
                value = BigDecimal.valueOf(C12ParseUtils.getExtendedLong(data,offset,4,dataOrder));
                break;
            case 64: // NI_FORMAT1
                value = C12ParseUtils.getNumberFromNonInteger(data,offset, cfgt.getNonIntFormat1(),dataOrder);
                break;
            case 128: // NI_FORMAT2
                value = C12ParseUtils.getNumberFromNonInteger(data,offset, cfgt.getNonIntFormat2(),dataOrder);
                break;

        }

    }



    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("IntervalFormat: value="+value+"\n");
        return strBuff.toString();

    }

    static public int getSize(TableFactory tableFactory, int set) throws IOException {
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        int format=0;
        if (set==0)
           format = tableFactory.getC12ProtocolLink().getStandardTableFactory().getLoadProfileControlTable().getIntervalFormatCode1();
        if (set==1)
           format = tableFactory.getC12ProtocolLink().getStandardTableFactory().getLoadProfileControlTable().getIntervalFormatCode2();
        if (set==2)
           format = tableFactory.getC12ProtocolLink().getStandardTableFactory().getLoadProfileControlTable().getIntervalFormatCode3();
        if (set==3)
           format = tableFactory.getC12ProtocolLink().getStandardTableFactory().getLoadProfileControlTable().getIntervalFormatCode4();

        switch(format) {
            case 1: // UINT8
                return 1;
            case 2: // UINT16
                return 2;
            case 4: // UINT32
                return 4;
            case 8: // INT8
                return 1;
            case 16: // INT16
                return 2;
            case 32: // INT32
                return 4;
            case 64: // NI_FORMAT1
                return C12ParseUtils.getNonIntegerSize(cfgt.getNonIntFormat1());
            case 128: // NI_FORMAT2
                return C12ParseUtils.getNonIntegerSize(cfgt.getNonIntFormat2());

            default:
                throw new IOException("IntervalFormat, getSize(), invalid format "+format);
        }


    }

    public Number getValue() {
        return value;
    }

    public void setValue(Number value) {
        this.value = value;
    }

}
