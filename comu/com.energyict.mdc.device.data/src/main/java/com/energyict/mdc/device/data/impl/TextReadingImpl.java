package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.TextReading;

import com.elster.jupiter.metering.ReadingQuality;
import com.elster.jupiter.metering.ReadingRecord;

import java.util.List;

/**
 * Provides an implementation for the {@link TextReading} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-14 (15:38)
 */
public class TextReadingImpl extends ReadingImpl implements TextReading {

    protected TextReadingImpl(ReadingRecord actualReading) {
        super(actualReading);
    }

    protected TextReadingImpl(ReadingRecord actualReading, List<ReadingQuality> readingQualities) {
        super(actualReading, readingQualities);
    }

    @Override
    public String getValue() {
        // Todo (JP-4174)
        return "Waiting for completion of issue JP-4174";
    }

}