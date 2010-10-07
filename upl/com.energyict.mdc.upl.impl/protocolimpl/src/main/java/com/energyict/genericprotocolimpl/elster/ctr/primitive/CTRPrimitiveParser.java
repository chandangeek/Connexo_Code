package com.energyict.genericprotocolimpl.elster.ctr.primitive;

import com.energyict.cbo.Unit;
import com.energyict.genericprotocolimpl.elster.ctr.object.*;
import com.energyict.protocol.ProtocolUtils;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 21-sep-2010
 * Time: 11:10:27
 */
public class CTRPrimitiveParser {

    public CTRPrimitiveParser() {}

    public byte[] getBytesFromInt(int value, int length) {
        byte[] bytes = new byte[length];
        for (int i = 0; i < bytes.length; i++) {
            int ptr = (bytes.length - (i + 1));
            bytes[ptr] = (i < 4) ? (byte) ((value >> (i * 8))) : 0x00;
        }
        return bytes;
    }

    //Parses BIN byte arrays into BigDecimals
    //also parses single byte fields (e.g. hours, minutes,...)
    public CTRAbstractValue[] parseUnsignedBINValue(AbstractCTRObject object, CTRObjectID id, byte[] rawData, int offset, int[] valueLength) {

        int i = 0;
        CTRAbstractValue[] result = new CTRAbstractValue[valueLength.length];

        //Parse all given values. Each has its length.
        for(int valueLength1: valueLength) {
            byte[] value = ProtocolUtils.getSubArray(rawData, offset, offset + valueLength1 - 1);
            Unit unit = object.parseUnit(id, i);
            result[i] = new CTRBINValue(unit, object.parseOverflowValue(id, i,unit), convertByteArrayToBigDecimal(value),"BIN");
            i++;
            offset += valueLength1;
        }
        return result;  //Array of all value objects, each with its unit & domain.
    }

    
    //Parses Signed BIN byte arrays, can also parse Unsigned BIN byte arrays.
    public CTRAbstractValue[] parseSignedBINValue(AbstractCTRObject object, CTRObjectID id, byte[] rawData, int offset, int[] valueLength) {

        int i = 0;
        CTRAbstractValue[] result = new CTRAbstractValue[valueLength.length];
        int x = id.getX();
        int y = id.getY();
        int z = id.getZ();
        boolean signed;

        //Parse all given values. Each has its length.
        for(int valueLength1: valueLength) {
            byte[] value = ProtocolUtils.getSubArray(rawData, offset, offset + valueLength1 - 1);
            Unit unit = object.parseUnit(id, i);

            signed = false;
            if (x == 8 && y == 0 && z == 0 && i ==7) {signed = true;}
            if (x == 8 && y == 1 && z == 2) {signed = true;}
            if (x == 0x0C && y == 0 && z == 5) {signed = true;}
            if (x == 0x0E && y == 0x0C) {signed = true;}

            if (signed) {
                result[i] = new CTRBINValue(unit, object.parseOverflowValue(id, i,unit), convertSignedByteArrayToBigDecimal(value), "SignedBIN");
            } else {
                result[i] = new CTRBINValue(unit, object.parseOverflowValue(id, i, unit), convertByteArrayToBigDecimal(value), "BIN");
            }

            i++;
            offset += valueLength1;
        }
        return result;  //Array of all value objects, each with its unit & domain.
    }


    //Parses String values, can also parse Unsigned BIN byte arrays.
    public CTRAbstractValue[] parseStringValue(AbstractCTRObject object, CTRObjectID id, byte[] rawData, int offset, int[] valueLength) {

        int i = 0;
        CTRAbstractValue[] result = new CTRAbstractValue[valueLength.length];
        int x = id.getX();
        int y = id.getY();
        int z = id.getZ();
        boolean stringValue;

        //Parse all given values. Each has its length.
        for(int valueLength1: valueLength) {
            byte[] value = ProtocolUtils.getSubArray(rawData, offset, offset + valueLength1 - 1);
            Unit unit = object.parseUnit(id, i);

            stringValue = false;
            if (x == 9 && y == 0 && z < 9) {stringValue = true;}
            if (x == 9 && y == 2 && z < 3) {stringValue = true;}
            if (x == 9 && y == 2 && z < 3) {stringValue = true;}
            if (x == 9 && y == 3) {stringValue = true;}
            if (x == 0x0D && y == 7) {stringValue = true;}

            if (stringValue) {
                result[i] = new CTRStringValue(unit, object.parseOverflowValue(id, i,unit), convertByteArrayToString(value), "String");
            } else {
                result[i] = new CTRBINValue(unit, object.parseOverflowValue(id, i, unit), convertByteArrayToBigDecimal(value), "BIN");
            }

            i++;
            offset += valueLength1;
        }
        return result;  //Array of all value objects, each with its unit & domain.
    }


    //Parses BCD values, can also parse other types.
    public CTRAbstractValue[] parseBCDValue(AbstractCTRObject object, CTRObjectID id, byte[] rawData, int offset, int[] valueLength) {

        CTRAbstractValue[] result = new CTRAbstractValue[valueLength.length];
        int i = 0;
        int x = id.getX();
        int y = id.getY();
        int z = id.getZ();
        boolean signedValue;
        boolean stringValue;
        boolean bcdValue;

        //Parse all given values. Each has its length.
        for(int valueLength1: valueLength) {
            byte[] value = ProtocolUtils.getSubArray(rawData, offset, offset + valueLength1 - 1);
            Unit unit = object.parseUnit(id, i);

            signedValue = false;
            stringValue = false;
            bcdValue = false;

            if (x == 0x0C && y == 0 && z == 0) {bcdValue = true;}
            if (x == 0x0C && y == 0 && z == 4) {stringValue = true;}
            if (x == 0x0C && y == 0 && z == 5) {signedValue = true;}
            if (x == 0x0C && y == 0 && z == 6) {stringValue = true;}
            if (x == 0x0C && y == 0 && z == 7) {stringValue = true;}
            if (x == 0x0C && y == 2 && z == 0 && i == 0) {stringValue = true;}
            if (x == 0x0C && y == 2 && z == 0 && i == 2) {stringValue = true;}
            if (x == 0x0C && y == 2 && z == 1) {stringValue = true;}
            if (x == 0x0C && y == 1) {stringValue = true;}
            if (x == 0x0E && y == 0x0C) {signedValue = true;}
            if (x == 0x0E && y == 0x0E) {stringValue = true;}

            if (signedValue) {
                result[i] = new CTRBINValue(unit, object.parseOverflowValue(id, i,unit), convertSignedByteArrayToBigDecimal(value), "SignedBIN");
            } else if (stringValue) {
                result[i] = new CTRStringValue(unit, object.parseOverflowValue(id, i, unit), convertByteArrayToString(value), "String");
            } else if (bcdValue) {
                result[i] = new CTRBCDValue(unit, object.parseOverflowValue(id, i, unit), convertByteArrayToBCD(value), "BCD");
            } else {
                result[i] = new CTRBINValue(unit, object.parseOverflowValue(id, i,unit), convertByteArrayToBigDecimal(value), "BIN" );
            }

            i++;
            offset += valueLength1;
        }
        return result;  //Array of all value objects, each with its unit & domain.
    }


    private BigDecimal convertSignedByteArrayToBigDecimal(byte[] value) {
        BigInteger convertedValue = new BigInteger(value);
        return new BigDecimal(convertedValue);
    }

    private String convertByteArrayToBCD(byte[] value) {
        String convertedValue = "";
        int len = value.length;
        boolean firstHalf = true;

        for (int i = 0; i <= 2 * len - 1; i++) {
            byte Byte = value[i / 2];
            if (firstHalf) {
                Byte = (byte) ((byte) (Byte & 0xFF) >> 4 & 0xFF);
            }
            Byte = (byte) (Byte & 0x0F);
            convertedValue += ((int) Byte);
            firstHalf = !firstHalf;
        }
        return convertedValue;
    }

    private BigDecimal convertByteArrayToBigDecimal(byte[] value) {
        byte[] temp = new byte[]{0x00};    //To bypass the sign bit :P
        value = concat(temp,value);
        BigInteger convertedValue = new BigInteger(value);
        return new BigDecimal(convertedValue);
    }

    private String convertByteArrayToString(byte[] value) {
        return new String(value);
    }

    public CTRObjectID parseId(byte[] data, int offset) {
        byte byte1 = data[offset];
        byte byte2 = data[offset + 1];
        int x,y,z;
        x = (byte1 & 0xFF) & 0xFF;
        y = ((byte2 & 0xF0) >> 4) & 0x0F;
        z = byte2 & 0x0F;
        return new CTRObjectID(x,y,z);
    }

    public int parseQlf(byte[] rawData, int offset) {
        return ((int)rawData[offset]) & 0xFF;
    }

    public int parseAccess(byte[] rawData, int offset) {
        return ((int)rawData[offset]) & 0xFF;
    }

    public int[] parseDefault(CTRObjectID id) {
        int x = id.getX();
        int y = id.getY();
        int z = id.getZ();
        int[] def = null;

        //Doesn't contain the manufacturer specific default values yet.
        if (x == 1 && y == 0x0C && z == 3) {def = new int[]{0};}
        if (x == 1 && y == 0x0C && z == 2) {def = new int[]{0};}
        if (x == 4 && y == 2 && z == 6) {def = new int[]{101325};}    //combined with the Kmolt multiplier: 1.01325
        if (x == 4 && y == 9 && z > 0) {def = new int[]{101325};}
        if (x == 4 && y == 9 && z == 0) {def = new int[]{101325, 101325, 101325, 101325, 101325};}
        if (x == 4 && y == 0x0A) {def = new int[]{0};}
        if (x == 7 && y == 0x0B && z == 0) {def = new int[]{28815,28815,28815,28815,28815,28815};}
        if (x == 7 && y == 0x0B && z > 0) {def = new int[]{28815};}
        if (x == 8 && y == 0 && z == 0) {def = new int[]{5,1,1,6,0,0,0,1,0};}
        if (x == 8 && y == 0 && z == 1) {def = new int[]{5,1,1,0,0};}
        if (x == 8 && y == 1 && z == 3) {def = new int[]{6};}
        if (x == 8 && y == 1 && z == 4) {def = new int[]{0,0,0,0,0,0,0};}
        if (x == 8 && y == 2 && z == 0) {def = new int[]{1,0,0,0,0};}
        if (x == 9 && y == 4 && z == 0) {def = new int[]{0};}
        if (x == 0x0A && y == 3 && z == 6) {def = new int[]{7252};}
        if (x == 0x0A && y == 1 && z == 6) {def = new int[]{0};}
        if (x == 0x0A && y == 3 && z == 7) {def = new int[]{0};}
        if (x == 0x0A && y == 4 && z == 6) {def = new int[]{122541};}
        if (x == 0x0A && y == 4 && z == 7) {def = new int[]{0};}
        if (x == 0x0A && y == 5 && z == 6) {def = new int[]{59175};}
        if (x == 0x0C && y == 0 && z == 1) {def = new int[]{1};}
        if (x == 0x0C && y == 0 && z == 2) {def = new int[]{0x20};}
        if (x == 0x0C && y == 0 && z == 3) {def = new int[]{0};}
        if (x == 0x0C && y == 0 && z == 4) {def = new int[]{0};}
        if (x == 0x0C && y == 0 && z == 5) {def = new int[]{0xFFFFFF,0xFFFFFF,0xFFFF};}
        if (x == 0x0C && y == 0 && z == 6) {def = new int[]{0};}
        if (x == 0x0C && y == 0 && z == 7) {def = new int[]{0};}
        if (x == 0x0E && y == 9) {def = new int[]{300};}
        if (x == 0x13 && y == 7) {def = new int[]{1};}
        if (x == 0x0E && y == 0x0A && x == 0) {def = new int[]{0};}
        if (x == 0x0C && y == 1) {def = new int[]{0};}

        return def;
    }

    private byte[] concat(byte[] valueBytesPrevious, byte[] valueBytes) {
        byte[] result = new byte[valueBytesPrevious.length + valueBytes.length];
        System.arraycopy(valueBytesPrevious, 0, result, 0, valueBytesPrevious.length);
        System.arraycopy(valueBytes, 0, result, valueBytesPrevious.length, valueBytes.length);
        return result;
    }

}