/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.abnt.common.structure.field;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.AbstractField;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 23/05/2014 - 15:58
 */
public class TariffEntries extends AbstractField<TariffEntries> {

    public static final int LENGTH = 8;

    private List<String> tariffEntries;

    public TariffEntries() {
        tariffEntries = new ArrayList<>(LENGTH/2);
    }

    @Override
    public byte[] getBytes() {
        String allTariffEntries = new String();
        for (String tariffEntry : tariffEntries) {
            allTariffEntries += tariffEntry;
        }

        return getBCDFromHexString(allTariffEntries, LENGTH);
    }

    @Override
    public TariffEntries parse(byte[] rawData, int offset) throws ParsingException {
        for (int i = 0; i < LENGTH; i += 2) {
            tariffEntries.add(getHexStringFromBCD(rawData, offset + i, 2));
        }
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public List<String> getTariffEntries() {
        return tariffEntries;
    }
}