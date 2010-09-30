package com.energyict.genericprotocolimpl.elster.ctr.primitive;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;
import com.energyict.genericprotocolimpl.elster.ctr.common.CTRParsingException;
import com.energyict.genericprotocolimpl.elster.ctr.common.Field;
import com.energyict.genericprotocolimpl.elster.ctr.object.*;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 21-sep-2010
 * Time: 11:10:27
 */
public class CTRPrimitiveConverter extends AbstractField {

    public CTRPrimitiveConverter() {}

    public byte[] convertId(CTRObjectID id) {
        int x = id.getX();
        int y = id.getY();
        int z = id.getZ();

        byte Byte1 = (byte) ((byte) x & 0xFF);
        byte Byte2 = (byte) ((((byte) y & 0xFF) << 4) & 0xF0);
        byte Byte3 = (byte) ((byte) z & 0xFF);

        return new byte[]{Byte1, (byte) (Byte2+Byte3)};
    }

    public byte[] convertQlf(int qlf) {
        return new byte[]{(byte) qlf};
    }

    public byte[] convertStringValue(String value) {
        return value.getBytes();
    }

    public byte[] convertBINValue(BigDecimal value, int valueLength) {
        byte[] result = new byte[valueLength];
        for (int i = (valueLength - 1); i >= 0; i--) {
            BigDecimal divider = new BigDecimal(Math.pow(256, i));
            result[valueLength - 1 - i] =  (byte) ((value.divide(divider)).intValue() & 0xFF);
        }
        return result;
    }

    public byte[] convertBCDValue(BigDecimal value) {
        String hex = value.toString();
        byte[] bts = new byte[hex.length() / 2];

        for (int i = 0; i < bts.length; i++) {
            bts[i] = (byte) Integer.parseInt(hex.substring(2*i, 2*i+2), 16);
        }
        return bts;
    }

    public byte[] convertUnsignedBINValue(BigDecimal value, int valueLength) {
        byte[] result = new byte[valueLength];
        for (int i = (valueLength - 1); i >= 0; i--) {
            BigDecimal divider = new BigDecimal(Math.pow(256, i));
            result[valueLength - 1 - i] =  (byte) ((value.divide(divider)).intValue() & 0xFF);

            //Only the first byte is parsed as a negative value
            if (i == (valueLength - 1)) {
                value = value.multiply(new BigDecimal(-1));
            }
        }
        return result;
    }

    public byte[] convertAccess(int access) {
        return new byte[]{(byte) access};
    }

    public byte[] convertDefaults(int[] defaults, int[] valueLength) {

        if (defaults == null) {
            byte[] result = new byte[sum(valueLength)];
            for (int i = 0; i < result.length; i++) {
                result[i] = 0x00;
            }
            return result;
        }

        int k = 0;
        byte[] bytes;
        byte[] result = null;
        
        for (int def : defaults) {
            bytes = getBytesFromInt(def, valueLength[k]);
            if (k == 0) {
                result = bytes;
            } else {
                result = concat(result,bytes);
            }
            k++;
        }
        return result;
    }

    public int sum(int[] valueLength) {
         int sum = 0;
         for (int i : valueLength) {
             sum += i;
         }
         return sum;
    }



    public byte[] getBytes() {
        return null;
    }

    public Field parse(byte[] rawData, int offset) throws CTRParsingException {
        return null;
    }

    private byte[] concat(byte[] valueBytesPrevious, byte[] valueBytes) {
        byte[] result = new byte[valueBytesPrevious.length + valueBytes.length];
        System.arraycopy(valueBytesPrevious, 0, result, 0, valueBytesPrevious.length);
        System.arraycopy(valueBytes, 0, result, valueBytesPrevious.length, valueBytes.length);
        return result;
    }

}