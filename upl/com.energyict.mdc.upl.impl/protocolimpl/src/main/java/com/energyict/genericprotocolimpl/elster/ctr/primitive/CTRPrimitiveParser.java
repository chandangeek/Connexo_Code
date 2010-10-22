package com.energyict.genericprotocolimpl.elster.ctr.primitive;

import com.energyict.cbo.Unit;
import com.energyict.genericprotocolimpl.elster.ctr.object.AbstractCTRObject;
import com.energyict.genericprotocolimpl.elster.ctr.object.CTRObjectID;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.*;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 21-sep-2010
 * Time: 11:10:27
 */
public class CTRPrimitiveParser {

    public CTRPrimitiveParser() {
    }

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
        for (int valueLength1 : valueLength) {
            byte[] value = ProtocolUtils.getSubArray(rawData, offset, offset + valueLength1 - 1);
            Unit unit = object.getUnit(id, i);
            result[i] = new CTRBINValue(unit, object.getOverflowValue(id, i, unit), convertByteArrayToBigDecimal(value), CTRAbstractValue.BIN , valueLength1);
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
        for (int valueLength1 : valueLength) {
            byte[] value = ProtocolUtils.getSubArray(rawData, offset, offset + valueLength1 - 1);
            Unit unit = object.getUnit(id, i);

            signed = false;
            if (x == 8 && y == 0 && z == 0 && i == 7) {
                signed = true;
            }
            if (x == 8 && y == 1 && z == 2) {
                signed = true;
            }
            if (x == 0x0C && y == 0 && z == 5) {
                signed = true;
            }
            if (x == 0x0E && y == 0x0C) {
                signed = true;
            }

            if (signed) {
                result[i] = new CTRBINValue(unit, object.getOverflowValue(id, i, unit), convertSignedByteArrayToBigDecimal(value), CTRAbstractValue.SIGNEDBIN, valueLength1);
            } else {
                result[i] = new CTRBINValue(unit, object.getOverflowValue(id, i, unit), convertByteArrayToBigDecimal(value), CTRAbstractValue.BIN, valueLength1);
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
        for (int valueLength1 : valueLength) {
            byte[] value = ProtocolUtils.getSubArray(rawData, offset, offset + valueLength1 - 1);
            Unit unit = object.getUnit(id, i);

            stringValue = false;
            if (x == 9 && y == 0 && z < 9) {
                stringValue = true;
            }
            if (x == 9 && y == 2 && z < 3) {
                stringValue = true;
            }
            if (x == 9 && y == 2 && z < 3) {
                stringValue = true;
            }
            if (x == 9 && y == 3) {
                stringValue = true;
            }
            if (x == 0x0D && y == 7) {
                stringValue = true;
            }
            if (x == 0x0D && y < 7) {
                stringValue = true;
            }

            if (stringValue) {
                result[i] = new CTRStringValue(unit, object.getOverflowValue(id, i, unit), convertByteArrayToString(value), CTRAbstractValue.STRING, valueLength1);
            } else {
                result[i] = new CTRBINValue(unit, object.getOverflowValue(id, i, unit), convertByteArrayToBigDecimal(value), CTRAbstractValue.BIN, valueLength1);
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
        for (int valueLength1 : valueLength) {
            byte[] value = ProtocolUtils.getSubArray(rawData, offset, offset + valueLength1 - 1);
            Unit unit = object.getUnit(id, i);

            signedValue = false;
            stringValue = false;
            bcdValue = false;

            if (x == 0x0C && y == 0 && z == 0) {
                bcdValue = true;
            }
            if (x == 0x0C && y == 0 && z == 4) {
                stringValue = true;
            }
            if (x == 0x0C && y == 0 && z == 5) {
                signedValue = true;
            }
            if (x == 0x0C && y == 0 && z == 6) {
                stringValue = true;
            }
            if (x == 0x0C && y == 0 && z == 7) {
                stringValue = true;
            }
            if (x == 0x0C && y == 2 && z == 0 && i == 0) {
                stringValue = true;
            }
            if (x == 0x0C && y == 2 && z == 0 && i == 2) {
                stringValue = true;
            }
            if (x == 0x0C && y == 2 && z == 1) {
                stringValue = true;
            }
            if (x == 0x0C && y == 1) {
                stringValue = true;
            }
            if (x == 0x0E && y == 0x0C) {
                signedValue = true;
            }
            if (x == 0x0E && y == 0x0E) {
                stringValue = true;
            }

            if (signedValue) {
                result[i] = new CTRBINValue(unit, object.getOverflowValue(id, i, unit), convertSignedByteArrayToBigDecimal(value), CTRAbstractValue.SIGNEDBIN, valueLength1);
            } else if (stringValue) {
                result[i] = new CTRStringValue(unit, object.getOverflowValue(id, i, unit), convertByteArrayToString(value), CTRAbstractValue.STRING, valueLength1);
            } else if (bcdValue) {
                result[i] = new CTRBCDValue(unit, object.getOverflowValue(id, i, unit), convertByteArrayToBCD(value), CTRAbstractValue.BCD, valueLength1);
            } else {
                result[i] = new CTRBINValue(unit, object.getOverflowValue(id, i, unit), convertByteArrayToBigDecimal(value), CTRAbstractValue.BIN, valueLength1);
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
        value = ProtocolTools.concatByteArrays(temp, value);
        BigInteger convertedValue = new BigInteger(value);
        return new BigDecimal(convertedValue);
    }

    private String convertByteArrayToString(byte[] value) {
        return new String(value);
    }

    public CTRObjectID parseId(byte[] data, int offset) {
        byte byte1 = data[offset];
        byte byte2 = data[offset + 1];
        int x, y, z;
        x = (byte1 & 0xFF) & 0xFF;
        y = ((byte2 & 0xF0) >> 4) & 0x0F;
        z = byte2 & 0x0F;
        return new CTRObjectID(x, y, z);
    }

    public int parseQlf(byte[] rawData, int offset) {
        return ((int) rawData[offset]) & 0xFF;
    }

    public int parseAccess(byte[] rawData, int offset) {
        return ((int) rawData[offset]) & 0xFF;
    }

    public Default[] parseDefault(CTRObjectID id, CTRAbstractValue[] values) {
        int x = id.getX();
        int y = id.getY();
        int z = id.getZ();
        Default[] def = null;

        //Doesn't contain the manufacturer specific default values yet.
        if (x == 1 && y == 0x0C && z == 3) {
            def = new Default[]{new Default(0, values[0].getUnit())};
        }
        if (x == 1 && y == 0x0C && z == 2) {
            def = new Default[]{new Default(0, values[0].getUnit())};
        }
        if (x == 4 && y == 2 && z == 6) {
            def = new Default[]{new Default(101325, values[0].getUnit())};         //combined with the Kmolt multiplier (in the unit): 1.01325
        }
        if (x == 4 && y == 9 && z > 0) {
            def = new Default[]{new Default(101325, values[0].getUnit())};
        }
        if (x == 4 && y == 9 && z == 0) {
            def = new Default[]{new Default(101325, values[0].getUnit()), new Default(101325, values[1].getUnit()), new Default(101325, values[2].getUnit()), new Default(101325, values[3].getUnit()), new Default(101325, values[4].getUnit())};
        }
        if (x == 4 && y == 0x0A) {
            def = new Default[]{new Default(0, values[0].getUnit())};
        }
        if (x == 7 && y == 0x0B && z == 0) {
            def = new Default[]{new Default(22815, values[0].getUnit()), new Default(22815, values[1].getUnit()), new Default(22815, values[2].getUnit()), new Default(22815, values[3].getUnit()), new Default(22815, values[4].getUnit()), new Default(22815, values[5].getUnit())};
        }
        if (x == 7 && y == 0x0B && z > 0) {
            def = new Default[]{new Default(22815, values[0].getUnit())};
        }
        if (x == 8 && y == 0 && z == 0) {
            def = new Default[]{new Default(5, values[0].getUnit()), new Default(1, values[1].getUnit()), new Default(1, values[2].getUnit()), new Default(6, values[3].getUnit()), new Default(0, values[4].getUnit()), new Default(0, values[5].getUnit()), new Default(0, values[6].getUnit()), new Default(1, values[7].getUnit()), new Default(0, values[8].getUnit())};
        }
        if (x == 8 && y == 0 && z == 1) {
            def = new Default[]{new Default(5, values[0].getUnit()), new Default(1, values[1].getUnit()), new Default(1, values[2].getUnit()), new Default(0, values[3].getUnit()), new Default(0, values[4].getUnit()),};
        }
        if (x == 8 && y == 1 && z == 3) {
            def = new Default[]{new Default(6, values[0].getUnit())};
        }
        if (x == 8 && y == 1 && z == 4) {
            def = new Default[]{new Default(0, values[0].getUnit()), new Default(0, values[1].getUnit()), new Default(0, values[2].getUnit()), new Default(0, values[3].getUnit()), new Default(0, values[4].getUnit()), new Default(0, values[5].getUnit()), new Default(0, values[6].getUnit())};
        }
        if (x == 8 && y == 2 && z == 0) {
            def = new Default[]{new Default(1, values[0].getUnit()), new Default(0, values[1].getUnit()), new Default(0, values[2].getUnit()), new Default(0, values[3].getUnit()), new Default(0, values[4].getUnit()),};
        }
        if (x == 9 && y == 4 && z == 0) {
            def = new Default[]{new Default(0, values[0].getUnit())};
        }
        if (x == 0x0A && y == 3 && z == 6) {
            def = new Default[]{new Default(7252, values[0].getUnit())};
        }
        if (x == 0x0A && y == 1 && z == 6) {
            def = new Default[]{new Default(0, values[0].getUnit())};
        }
        if (x == 0x0A && y == 3 && z == 7) {
            def = new Default[]{new Default(0, values[0].getUnit())};
        }
        if (x == 0x0A && y == 4 && z == 6) {
            def = new Default[]{new Default(122541, values[0].getUnit())};
        }
        if (x == 0x0A && y == 4 && z == 7) {
            def = new Default[]{new Default(0, values[0].getUnit())};
        }
        if (x == 0x0A && y == 5 && z == 6) {
            def = new Default[]{new Default(59175, values[0].getUnit())};
        }
        if (x == 0x0C && y == 0 && z == 1) {
            def = new Default[]{new Default(1, values[0].getUnit())};
        }
        if (x == 0x0C && y == 0 && z == 2) {
            def = new Default[]{new Default(0x20, values[0].getUnit())};
        }
        if (x == 0x0C && y == 0 && z == 3) {
            def = new Default[]{new Default(0, values[0].getUnit())};
        }
        if (x == 0x0C && y == 0 && z == 4) {
            def = new Default[]{new Default(0, values[0].getUnit())};
        }
        if (x == 0x0C && y == 0 && z == 5) {
            def = new Default[]{new Default(0xFFFFFF, values[0].getUnit()), new Default(0xFFFFFF, values[1].getUnit()), new Default(0xFFFF, values[2].getUnit())};
        }
        if (x == 0x0C && y == 0 && z == 6) {
            def = new Default[]{new Default(0, values[0].getUnit())};
        }
        if (x == 0x0C && y == 0 && z == 7) {
            def = new Default[]{new Default(0, values[0].getUnit())};
        }
        if (x == 0x0E && y == 9) {
            def = new Default[]{new Default(300, values[0].getUnit())};
        }
        if (x == 0x13 && y == 7) {
            def = new Default[]{new Default(1, values[0].getUnit())};
        }
        if (x == 0x0E && y == 0x0A && x == 0) {
            def = new Default[]{new Default(0, values[0].getUnit())};
        }
        if (x == 0x0C && y == 1) {
            def = new Default[]{new Default(0, values[0].getUnit())};
        }

        return def;
    }
}