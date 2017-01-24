package com.energyict.protocolimplv2.elster.garnet.common.field;

import java.util.BitSet;

/**
 * @author sva
 * @since 23/05/2014 - 10:19
 */
public abstract class AbstractBitMaskField<T extends BitMaskField> implements BitMaskField<T> {

    protected int convertBitSetToInt(BitSet bitSet) {
        int result = 0 ;
        for(int i = 0 ; i < bitSet.length() ; i++){
            if(bitSet.get(i)){
                result |= (1 << i);
            }
        }
        return result;
    }
}
