/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.abnt.common.structure.field;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.AbstractField;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 23/05/2014 - 15:58
 */
public class LoadProfileWords extends AbstractField<LoadProfileWords> {

    private final int length;

    private byte[] bytes;
    private List<Integer> loadProfileWords;

    public LoadProfileWords(int length) {
        this.loadProfileWords = new ArrayList<>();
        this.length = length;
        this.bytes = new byte[length];
    }

    @Override
    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public LoadProfileWords parse(byte[] rawData, int offset) throws ParsingException {
        this.bytes = ProtocolTools.getSubArray(rawData, offset, offset + getLength());

        int ptr = offset;
        for (int i = 0; i < getLength(); i += 3) {
            int i1 = ((rawData[ptr + i + 1] & 0x00f0) << 4) | ((rawData[ptr + i] & 0x00FF)) & 0x0FFF;
            int i2 = (((rawData[ptr + i + 1] & 0x000f) << 8) | (rawData[ptr + i + 2] & 0x00FF)) & 0x0FFF;
            loadProfileWords.add(i1);
            loadProfileWords.add(i2);
        }
        return this;
    }

    @Override
    public int getLength() {
        return length;
    }

    public List<Integer> getLoadProfileWords() {
        return loadProfileWords;
    }
}