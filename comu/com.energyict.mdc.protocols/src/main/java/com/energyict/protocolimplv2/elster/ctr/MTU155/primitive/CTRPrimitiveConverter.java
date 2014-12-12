package com.energyict.protocolimplv2.elster.ctr.MTU155.primitive;


import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.Default;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 21-sep-2010
 * Time: 11:10:27
 */
public class CTRPrimitiveConverter {

    public CTRPrimitiveConverter() {}

    /**
     * Converts a given array of defaults (of a certain CTR Object) into a matching byte array.
     * @param defaults: the defaults that need to be converted
     * @param valueLength: the length that the resulting byte array should be
     * @return byte array representing the defaults
     */
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