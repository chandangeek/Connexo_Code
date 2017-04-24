/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.garnet.common.field;

import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

import java.util.BitSet;

/**
 * @author sva
 * @since 23/05/2014 - 10:12
 */
public interface BitMaskField<T extends BitMaskField> {

    /**
     * Getter for the BitMask
     */
    BitSet getBitMask();

    /**
     * Create a proper BitMaskField out of the given BitSet
     *
     * @param bitSet the BitSet, who contains multiple BitMaskFields
     * @param posInMask the position in the mask (~ the xth BitMaskField to parse)
     * @return the parsed BitMaskField
     * @throws ParsingException
     */
    T parse(BitSet bitSet, int posInMask) throws ParsingException;

    /**
     * Getter for the length of this BitMaskField, expressed as nr of bits
     */
    int getLength();

}