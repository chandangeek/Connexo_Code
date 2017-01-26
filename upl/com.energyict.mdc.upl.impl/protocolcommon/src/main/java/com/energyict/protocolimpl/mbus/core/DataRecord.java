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

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    DataRecord(byte[] data, int offset, TimeZone timeZone, final Logger logger) throws IOException {

        if (DEBUG>=1) {
            System.out.println("KV_DEBUG> DataRecord, offset=" + offset);
        }

        setDataRecordHeader(new DataRecordHeader(data, offset, timeZone, logger));
        offset += getDataRecordHeader().size();

        if (dataRecordHeader.getValueInformationBlock()==null) {
            return;
        }

        if (dataRecordHeader.getValueInformationBlock().getValueInformationfieldCoding() != null) {
            if (dataRecordHeader.getValueInformationBlock().getValueInformationfieldCoding().isTypeFormat()) {
                final int dataFieldCodingTypeId = dataRecordHeader.getDataInformationBlock().getDataInformationfield().getDataFieldCoding().getType();
                final DataFieldCoding.DataFieldCodingType dataFieldCodingType = DataFieldCoding.DataFieldCodingType.fromId(dataFieldCodingTypeId);
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "Parsing the value of data type [" + dataFieldCodingType + "] with VIF type [format]");
                }
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
                        final int nrBytesToRead = dataRecordHeader.getDataInformationBlock().getDataInformationfield().getDataFieldCoding().getLengthInBytes();
                        if (!hasSufficientData(data, offset,nrBytesToRead )) {
                            if (logger.isLoggable(Level.SEVERE)) {
                                logger.log(Level.SEVERE, "Trying to parse the data at offset [" + offset +
                                        "] , but the buffer has no bytes left. The DIF type [" + dataFieldCodingType + "] with VIF type [" + dataRecordHeader.getValueInformationBlock().getValueInformationfieldCoding().getType() + "] indicates we need to read [" + nrBytesToRead + "] bytes," +
                                        "and there are only [" + (data.length - offset) + "] bytes left in the buffer.");
                            }
                            throw new IOException("Trying to parse the data at offset [" + offset +
                                    "] , but the buffer has no bytes left. The DIF indicates we need to read [" + nrBytesToRead + "] bytes," +
                                    "and there are only [" + (data.length - offset) + "] bytes left in the buffer.");
                        }
                        offset+=dataRecordHeader.getDataInformationBlock().getDataInformationfield().getDataFieldCoding().getLengthInBytes();
                        BigDecimal bd = BigDecimal.valueOf(val);
                        bd = bd.multiply(dataRecordHeader.getValueInformationBlock().getValueInformationfieldCoding().getMultiplier());
                        quantity = new Quantity(bd,dataRecordHeader.getValueInformationBlock().getValueInformationfieldCoding().getUnit());
                    } break; // TYPE_B

                    case ValueInformationfieldCoding.TYPE_E: {
                        throw new ProtocolException("DataRecord, invalid data coding TYPE_E is obsolete");
                    } // TYPE_E

                    case ValueInformationfieldCoding.TYPE_F: {
                        DataRecordTypeF_CP32 cp32 = new DataRecordTypeF_CP32(timeZone);
                        cp32.parse(ProtocolUtils.getSubArray2(data,offset,4));
                        offset+=4;
                        setDate(cp32.getCalendar().getTime());
                        if (DEBUG>=1) {
                            System.out.println("KV_DEBUG> date=" + getDate());
                        }
                    } break; // TYPE_F

                    case ValueInformationfieldCoding.TYPE_G: {
                        DataRecordTypeG_CP16 cp16 = new DataRecordTypeG_CP16(timeZone);
                        cp16.parse(ProtocolUtils.getSubArray2(data,offset,2));
                        offset+=4;
                        setDate(cp16.getCalendar().getTime());
                        if (DEBUG>=1) {
                            System.out.println("KV_DEBUG> date=" + getDate());
                        }
                    } break; // TYPE_G

                    case ValueInformationfieldCoding.TYPE_H: {
                        final int nrBytesToRead = 4;
                        if (!hasSufficientData(data, offset,nrBytesToRead )) {
                            if (logger.isLoggable(Level.SEVERE)) {
                                logger.log(Level.SEVERE, "Trying to parse the data at offset [" + offset +
                                        "] , but the buffer has no bytes left. The DIF type H indicates we need to read [" + nrBytesToRead + "] bytes," +
                                        "and there are only [" + (data.length - offset) + "] bytes left in the buffer.");
                            }
                            throw new IOException("Trying to parse the data at offset [" + offset +
                                    "] , but the buffer has no bytes left. The DIF indicates we need to read [" + nrBytesToRead + "] bytes," +
                                    "and there are only [" + (data.length - offset) + "] bytes left in the buffer.");
                        }
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
                        if (DEBUG>=1) {
                            System.out.println("KV_DEBUG> date=" + getDate());
                        }
                    } break; // TYPE_I

                    case ValueInformationfieldCoding.TYPE_J: {
                        DataRecordTypeJ typeJ = new DataRecordTypeJ(timeZone);
                        typeJ.parse(ProtocolUtils.getSubArray2(data,offset,3));
                        offset+=3;
                        setDate(typeJ.getCalendar().getTime());
                        if (DEBUG>=1) {
                            System.out.println("KV_DEBUG> date=" + getDate());
                        }
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
                final int dataFieldCodingType = dataRecordHeader.getDataInformationBlock().getDataInformationfield().getDataFieldCoding().getType();
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "Parsing the value of data type [" + DataFieldCoding.DataFieldCodingType.fromId(dataFieldCodingType) + "]");
                }
                switch(dataRecordHeader.getDataInformationBlock().getDataInformationfield().getDataFieldCoding().getType()) {
                    case DataFieldCoding.TYPE_BCD: {
                        long val = ParseUtils.getBCD2LongLE(data,offset,dataRecordHeader.getDataInformationBlock().getDataInformationfield().getDataFieldCoding().getLengthInBytes());
                        offset+=dataRecordHeader.getDataInformationBlock().getDataInformationfield().getDataFieldCoding().getLengthInBytes();
                        BigDecimal bd = BigDecimal.valueOf(val);
                        bd = bd.multiply(dataRecordHeader.getValueInformationBlock().getValueInformationfieldCoding().getMultiplier());
                        quantity = new Quantity(bd,dataRecordHeader.getValueInformationBlock().getValueInformationfieldCoding().getUnit());
                    } break; // DataFieldCoding.TYPE_BCD

                    case DataFieldCoding.TYPE_BINARY: {
                        final int nrBytesToRead = dataRecordHeader.getDataInformationBlock().getDataInformationfield().getDataFieldCoding().getLengthInBytes();
                        if (!hasSufficientData(data, offset,nrBytesToRead )) {
                            if (logger.isLoggable(Level.SEVERE)) {
                                logger.log(Level.SEVERE, "Trying to parse the Binary data at offset [" + offset +
                                        "] , but the buffer has no bytes left. The DIF indicates we need to read [" + nrBytesToRead + "] bytes," +
                                        "and there are only [" + (data.length - offset) + "] bytes left in the buffer.");
                            }
                            throw new IOException("Trying to parse the Binary data at offset [" + offset +
                                    "] , but the buffer has no bytes left. The DIF indicates we need to read [" + nrBytesToRead + "] bytes," +
                                    "and there are only [" + (data.length - offset) + "] bytes left in the buffer.");
                        }
                        long val = ProtocolUtils.getLongLE(data,offset,dataRecordHeader.getDataInformationBlock().getDataInformationfield().getDataFieldCoding().getLengthInBytes());
                        final BigDecimal multiplier = dataRecordHeader.getValueInformationBlock().getValueInformationfieldCoding().getMultiplier();
                        final Unit originalUnit = dataRecordHeader.getValueInformationBlock().getValueInformationfieldCoding().getUnit();
                        if (logger.isLoggable(Level.FINE)) {
                            logger.log(Level.FINE, "Converting Binary data [" + val + "] at offset [" + offset +
                                    "] with multiplier [" + multiplier + "] [" + originalUnit + "]");
                        }
                        if (logger.isLoggable(Level.FINE)) {
                            logger.log(Level.FINE, "Converted Binary data [" + val + "] into value [" + val + "] at offset [" + offset +
                                    "] with multiplier [" + multiplier + "] [" + originalUnit + "]");
                        }
                        offset+=dataRecordHeader.getDataInformationBlock().getDataInformationfield().getDataFieldCoding().getLengthInBytes();
                        BigDecimal bd = BigDecimal.valueOf(val);
                        bd = bd.multiply(dataRecordHeader.getValueInformationBlock().getValueInformationfieldCoding().getMultiplier());
                        quantity = new Quantity(bd,dataRecordHeader.getValueInformationBlock().getValueInformationfieldCoding().getUnit());
                    } break; // DataFieldCoding.TYPE_BINARY

                    case DataFieldCoding.TYPE_REAL: {
                        final int nrBytesToRead = 4;
                        if (!hasSufficientData(data, offset,nrBytesToRead )) {
                            if (logger.isLoggable(Level.SEVERE)) {
                                logger.log(Level.SEVERE, "Trying to parse the Binary data at offset [" + offset +
                                        "] , but the buffer has no bytes left. The DIF type real indicates we need to read [" + nrBytesToRead + "] bytes," +
                                        "and there are only [" + (data.length - offset) + "] bytes left in the buffer.");
                            }
                            throw new IOException("Trying to parse the Binary data at offset [" + offset +
                                    "] , but the buffer has no bytes left. The DIF indicates we need to read [" + nrBytesToRead + "] bytes," +
                                    "and there are only [" + (data.length - offset) + "] bytes left in the buffer.");
                        }
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
            if ("cust. ID".equals(vif)) {
                int length = data[offset++];
                setText(new String(ProtocolTools.getReverseByteArray(ProtocolTools.getSubArray(data, offset, offset + length))));
                offset += length;
            }
        } else {

        }

        if (DEBUG>=1) {
            System.out.println("KV_DEBUG> DataRecord, quantity=" + quantity);
        }
    }

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
        else {
            throw new ProtocolException("DataRecord, invalid LVAR value (" + length + ") (length byte of variable length data type)");
        }

        return offset;
    }

    public String toString() {
        return "DataRecord:\n" +
                "   dataRecordHeader=" + getDataRecordHeader() + "\n" +
                "   quantity=" + getQuantity() + "\n" +
                "   text=" + getText() + "\n" +
                "   date=" + getDate() + "\n";
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

    /**
     * Validates whether the buffer to read from has enough bytes left to read "nrOfBytesToRead", starting at position "offset"
     * @param buffer			The buffer to read from
     * @param offset			The starting position
     * @param nrOfBytesToRead	The number of bytes to read from the buffer
     * @return					true if the buffer to read from has enough bytes left to read "nrOfBytesToRead", starting at position "offset"; False otherwise
     */
    private boolean hasSufficientData(final byte[] buffer, final int offset, final int nrOfBytesToRead) {
        return (buffer.length - offset) >= nrOfBytesToRead;
    }

}