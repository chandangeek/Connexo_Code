package com.energyict.protocolimplv2.abnt.common.structure.field;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.AbstractField;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 23/05/2014 - 15:58
 */
public class LoadProfileWords extends AbstractField<LoadProfileWords> {

    public static final int LENGTH = 249;

    private List<Integer> loadProfileWords;

    public LoadProfileWords() {
        this.loadProfileWords = new ArrayList<>();
    }

    @Override
    public byte[] getBytes() {
        return new byte[LENGTH];    //TODO: add real implementation
    }

    @Override
    public LoadProfileWords parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        for (int i = 0; i < 166; i += 2) {
            loadProfileWords.add((rawData[ptr + i] << 4) | ((rawData[ptr + i + 1] & 0x00f0) >> 4));
            loadProfileWords.add(((rawData[ptr + i +1] & 0x000f) << 8) | (rawData[ptr + i + 2]));
        }
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public List<Integer> getLoadProfileWords() {
        return loadProfileWords;
    }
}