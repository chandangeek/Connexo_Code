/*
 * DataRecord.java
 *
 * Created on 3 oktober 2007, 12:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.mbus.core;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.cbo.Quantity;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author kvds
 */
public class DataRecord {

    final int DEBUG=0;

    private DataRecordHeader dataRecordHeader;
    private Quantity quantity;
    private String text="";
    private Date date;

    /** Creates a new instance of DataRecord */
    public DataRecord(byte[] data, int offset, TimeZone timeZone) throws IOException {

        if (DEBUG>=1) System.out.println("KV_DEBUG> DataRecord, offset="+offset);

        setDataRecordHeader(new DataRecordHeader(data, offset, timeZone));
        offset += getDataRecordHeader().size();

        if (dataRecordHeader.getValueInformationBlock()==null)
            return;

        if (dataRecordHeader.getValueInformationBlock().getValueInformationfieldCoding() != null) {
            if (dataRecordHeader.getValueInformationBlock().getValueInformationfieldCoding().isTypeFormat()) {
                switch(dataRecordHeader.getValueInformationBlock().getValueInformationfieldCoding().getType()) {
                    case ValueInformationfieldCoding.TYPE_A: {
                        long val = ParseUtils.getBCD2LongLE(data,offset,dataRecordHeader.getDataInformationBlock().getDataInformationfield().getDataFieldCoding().getLengthInBytes());
                        offset+=dataRecordHeader.getDataInformationBlock().getDataInformationfield().getDataFieldCoding().getLengthInBytes();
                        BigDecimal bd = BigDecimal.valueOf(val);
                        bd = bd.multiply(dataRecordHeader.getValueInformationBlock().getValueInformationfieldCoding().getMultiplier());
                        quantity = new Quantity(bd,dataRecordHeader.getValueInformationBlock().getValueInformationfieldCoding().getUnit());
                    } break; // TYPE_A

                    case ValueInformationfieldCoding.TYPE_D:
                    case ValueInformationfieldCoding.TYPE_C:
                    case ValueInformationfieldCoding.TYPE_B: {
                        long val = ProtocolUtils.getLongLE(data,offset,dataRecordHeader.getDataInformationBlock().getDataInformationfield().getDataFieldCoding().getLengthInBytes());
                        offset+=dataRecordHeader.getDataInformationBlock().getDataInformationfield().getDataFieldCoding().getLengthInBytes();
                        BigDecimal bd = BigDecimal.valueOf(val);
                        bd = bd.multiply(dataRecordHeader.getValueInformationBlock().getValueInformationfieldCoding().getMultiplier());
                        quantity = new Quantity(bd,dataRecordHeader.getValueInformationBlock().getValueInformationfieldCoding().getUnit());
                    } break; // TYPE_B

                    case ValueInformationfieldCoding.TYPE_E: {
                        throw new IOException("DataRecord, invalid data coding TYPE_E is obsolete");
                    } // TYPE_E

                    case ValueInformationfieldCoding.TYPE_F: {
                        DataRecordTypeF_CP32 cp32 = new DataRecordTypeF_CP32(timeZone);
                        cp32.parse(ProtocolUtils.getSubArray2(data,offset,4));
                        offset+=4;
                        setDate(cp32.getCalendar().getTime());
                        if (DEBUG>=1) System.out.println("KV_DEBUG> date="+getDate());
                    } break; // TYPE_F

                    case ValueInformationfieldCoding.TYPE_G: {
                        DataRecordTypeG_CP16 cp16 = new DataRecordTypeG_CP16(timeZone);
                        cp16.parse(ProtocolUtils.getSubArray2(data,offset,2));
                        offset+=4;
                        setDate(cp16.getCalendar().getTime());
                        if (DEBUG>=1) System.out.println("KV_DEBUG> date="+getDate());
                    } break; // TYPE_G

                    case ValueInformationfieldCoding.TYPE_H: {
                        float val = Float.intBitsToFloat((int)ProtocolUtils.getLongLE(data,offset,4));
                        offset+=4;
                        BigDecimal bd = new BigDecimal(""+val);
                        bd = bd.multiply(dataRecordHeader.getValueInformationBlock().getValueInformationfieldCoding().getMultiplier());
                        quantity = new Quantity(bd,dataRecordHeader.getValueInformationBlock().getValueInformationfieldCoding().getUnit());
                    } break; // TYPE_H

                    case ValueInformationfieldCoding.TYPE_I: {
                        DataRecordTypeI typeI = new DataRecordTypeI(timeZone);
                        typeI.parse(ProtocolUtils.getSubArray2(data,offset,5));
                        offset+=5;
                        setDate(typeI.getCalendar().getTime());
                        if (DEBUG>=1) System.out.println("KV_DEBUG> date="+getDate());
                    } break; // TYPE_I

                    case ValueInformationfieldCoding.TYPE_J: {
                        DataRecordTypeJ typeJ = new DataRecordTypeJ(timeZone);
                        typeJ.parse(ProtocolUtils.getSubArray2(data,offset,3));
                        offset+=3;
                        setDate(typeJ.getCalendar().getTime());
                        if (DEBUG>=1) System.out.println("KV_DEBUG> date="+getDate());
                    } break; // TYPE_J

                    case ValueInformationfieldCoding.TYPE_K: {
                        DataRecordTypeK typeK = new DataRecordTypeK(timeZone);
                        typeK.parse(ProtocolUtils.getSubArray2(data,offset,3));
                        offset+=3;
                        Date dateDSTBegin = typeK.getCalendarDSTBegin().getTime();
                        Date dateDSTEnd = typeK.getCalendarDSTEnd().getTime();
                    } break; // TYPE_K

                    case ValueInformationfieldCoding.TYPE_L: {
                        offset = decodeVariableLength(data,offset);
                        // KV_TO_DO when registervalue is introduced, this is a text value
                    } break; // TYPE_L
                }
            }
            else if ((dataRecordHeader.getValueInformationBlock().getValueInformationfieldCoding().isTypeUnit()) ||
                     (dataRecordHeader.getValueInformationBlock().getValueInformationfieldCoding().isTypeDuration())) {
                switch(dataRecordHeader.getDataInformationBlock().getDataInformationfield().getDataFieldCoding().getType()) {
                    case DataFieldCoding.TYPE_BCD: {
                        long val = ParseUtils.getBCD2LongLE(data,offset,dataRecordHeader.getDataInformationBlock().getDataInformationfield().getDataFieldCoding().getLengthInBytes());
                        offset+=dataRecordHeader.getDataInformationBlock().getDataInformationfield().getDataFieldCoding().getLengthInBytes();
                        BigDecimal bd = BigDecimal.valueOf(val);
                        bd = bd.multiply(dataRecordHeader.getValueInformationBlock().getValueInformationfieldCoding().getMultiplier());
                        quantity = new Quantity(bd,dataRecordHeader.getValueInformationBlock().getValueInformationfieldCoding().getUnit());
                    } break; // DataFieldCoding.TYPE_BCD

                    case DataFieldCoding.TYPE_BINARY: {
                        long val = ProtocolUtils.getLongLE(data,offset,dataRecordHeader.getDataInformationBlock().getDataInformationfield().getDataFieldCoding().getLengthInBytes());
                        offset+=dataRecordHeader.getDataInformationBlock().getDataInformationfield().getDataFieldCoding().getLengthInBytes();
                        BigDecimal bd = BigDecimal.valueOf(val);
                        bd = bd.multiply(dataRecordHeader.getValueInformationBlock().getValueInformationfieldCoding().getMultiplier());
                        quantity = new Quantity(bd,dataRecordHeader.getValueInformationBlock().getValueInformationfieldCoding().getUnit());
                    } break; // DataFieldCoding.TYPE_BINARY

                    case DataFieldCoding.TYPE_REAL: {
                        float val = Float.intBitsToFloat((int)ProtocolUtils.getLongLE(data,offset,4));
                        offset+=4;
                        BigDecimal bd = new BigDecimal(""+val);
                        bd = bd.multiply(dataRecordHeader.getValueInformationBlock().getValueInformationfieldCoding().getMultiplier());
                        quantity = new Quantity(bd,dataRecordHeader.getValueInformationBlock().getValueInformationfieldCoding().getUnit());
                    } break; // DataFieldCoding.TYPE_REAL

                    case DataFieldCoding.TYPE_VARIABLELENGTH: {
                        offset = decodeVariableLength(data,offset);

                        // KV_TO_DO when registervalue is introduced, this is a text value

                    } break; // DataFieldCoding.TYPE_VARIABLELENGTH

                    case DataFieldCoding.TYPE_SPECIALFUNCTIONS: {
                        quantity=null;
                    } break; // DataFieldCoding.TYPE_SPECIALFUNCTIONS

                } // switch(dataRecordHeader.getDataInformationBlock().getDataInformationfield().getDataFieldCoding().getType())
            } // type unit or type duration
        } else if (dataRecordHeader.getValueInformationBlock().getPlainTextVIF() != null) {
            String vif = dataRecordHeader.getValueInformationBlock().getPlainTextVIF();
            if (vif.equals("cust. ID")) {
                int length = data[offset++];
                setText(new String(ProtocolTools.getReverseByteArray(ProtocolTools.getSubArray(data, offset, offset + length))));
                offset += length;
            }
        } else {

        }

        if (DEBUG>=1) System.out.println("KV_DEBUG> DataRecord, quantity="+quantity);



    } // public DataRecord(byte[] data, int offset, TimeZone timeZone) throws IOException

    private int decodeVariableLength(byte[] data, int offset) throws IOException {
        int length = ProtocolUtils.getInt(data,offset++,1);
        if ((length>=0x00) && (length<=0xBF)) {
            setText(new String(ParseUtils.getSubArrayLE(data, offset, length)));
            offset+=length;
            quantity=null;
        }
        else if ((length>=0xC0) && (length<=0xC9)) {
            length -= 0xC0;
            long val = ParseUtils.getBCD2LongLE(data,offset,length);
            offset+=length;
            BigDecimal bd = BigDecimal.valueOf(val);
            bd = bd.multiply(dataRecordHeader.getValueInformationBlock().getValueInformationfieldCoding().getMultiplier());
            quantity = new Quantity(bd,dataRecordHeader.getValueInformationBlock().getValueInformationfieldCoding().getUnit());
        }
        else if ((length>=0xD0) && (length<=0xD9)) {
            length -= 0xD0;
            long val = ParseUtils.getBCD2LongLE(data,offset,length);
            offset+=length;
            BigDecimal bd = BigDecimal.valueOf(val);
            bd = bd.multiply(dataRecordHeader.getValueInformationBlock().getValueInformationfieldCoding().getMultiplier()).multiply(new BigDecimal(-1));
            quantity = new Quantity(bd,dataRecordHeader.getValueInformationBlock().getValueInformationfieldCoding().getUnit());
        }
        else if ((length>=0xE0) && (length<=0xEF)) {
            length -= 0xE0;
            BigInteger val = ParseUtils.getBigIntegerLE(data,offset,length);
            offset+=length;
            BigDecimal bd = new BigDecimal(val);
            bd = bd.multiply(dataRecordHeader.getValueInformationBlock().getValueInformationfieldCoding().getMultiplier());
            quantity = new Quantity(bd,dataRecordHeader.getValueInformationBlock().getValueInformationfieldCoding().getUnit());
        }
        else if (length==0xF8) {
            float val = Float.intBitsToFloat((int)ProtocolUtils.getLongLE(data,offset,4));
            offset+=4;
            BigDecimal bd = new BigDecimal(""+val);
            bd = bd.multiply(dataRecordHeader.getValueInformationBlock().getValueInformationfieldCoding().getMultiplier());
            quantity = new Quantity(bd,dataRecordHeader.getValueInformationBlock().getValueInformationfieldCoding().getUnit());
        }
        else throw new IOException("DataRecord, invalid LVAR value ("+length+") (length byte of variable length data type)");

        return offset;
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DataRecord:\n");
        strBuff.append("   dataRecordHeader="+getDataRecordHeader()+"\n");
        strBuff.append("   quantity="+getQuantity()+"\n");
        strBuff.append("   text="+getText()+"\n");
        strBuff.append("   date="+getDate()+"\n");

        return strBuff.toString();
    }

    public int size() {
        return getDataRecordHeader().size()+dataRecordHeader.getDataInformationBlock().getDataInformationfield().getDataFieldCoding().getLengthInBytes()+getText().length();
    }

    public DataRecordHeader getDataRecordHeader() {
        return dataRecordHeader;
    }

    public void setDataRecordHeader(DataRecordHeader dataRecordHeader) {
        this.dataRecordHeader = dataRecordHeader;
    }

    public Quantity getQuantity() {
        return quantity;
    }

    public void setQuantity(Quantity quantity) {
        this.quantity = quantity;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
