package com.energyict.protocolimpl.utils;

import java.util.Arrays;

public final class ArrayTools {

    public static <T> T[] concatenateArrays(T[]... arrays){
        int length = 0;
        for (T[] array : arrays){
            length+= array.length;
        }
        T[] result = Arrays.copyOf(arrays[0], length);
        int count = 0;
        for (T[] array : arrays){
            System.arraycopy(array,0,result,count,array.length);
            count+= array.length;
        }
        return result;
    }
}
