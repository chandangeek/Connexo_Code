package com.energyict.genericprotocolimpl.elster.ctr.primitive;


import com.energyict.genericprotocolimpl.elster.ctr.object.field.Default;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 21-sep-2010
 * Time: 11:10:27
 */
public class CTRPrimitiveConverter {

    public CTRPrimitiveConverter() {}

    public byte[] convertDefaults(Default[] defaults, int[] valueLength) {

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
        CTRPrimitiveParser parser = new CTRPrimitiveParser();

        for (Default def : defaults) {
            bytes = parser.getBytesFromInt(def.getDefaultValue(), valueLength[k]);
            if (k == 0) {
                result = bytes;
            } else {
                result = ProtocolTools.concatByteArrays(result,bytes);
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

}