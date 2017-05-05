/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ReadingTypeComparator;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ReadingTypeComparatorTest {

    @Mock
    private DataModel dataModel;

    Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;

    @Test
    public void compareEquivalentReadingTypes() {
        ReadingType secondaryDeltaAPlusWh = mockReadingType("0.0.0.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0");

        // Business method
        ReadingTypeComparator readingTypeComparator = ReadingTypeComparator.instance();

        // Asserts
        assertThat(readingTypeComparator.compare(secondaryDeltaAPlusWh, secondaryDeltaAPlusWh)).isEqualTo(0);
    }

    @Test
    public void compareDifferentReadingTypes() {
        ReadingType secondaryDeltaAPlusWh = mockReadingType("0.0.0.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
        ReadingType secondaryDeltaAPlusKWh = mockReadingType("0.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0");

        // Business method
        ReadingTypeComparator readingTypeComparator = ReadingTypeComparator.instance();

        // Asserts
        assertThat(readingTypeComparator.compare(secondaryDeltaAPlusWh, secondaryDeltaAPlusKWh)).isLessThan(0);
        assertThat(readingTypeComparator.compare(secondaryDeltaAPlusKWh, secondaryDeltaAPlusWh)).isGreaterThan(0);
    }

    @Test
    public void compareReadingTypesIgnoringMultiplier() {
        ReadingType secondaryDeltaAPlusWh = mockReadingType("0.0.0.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
        ReadingType secondaryDeltaAPlusKWh = mockReadingType("0.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0");

        // Business method
        ReadingTypeComparator readingTypeComparator = ReadingTypeComparator.ignoring(ReadingTypeComparator.Attribute.Multiplier);

        // Asserts
        assertThat(readingTypeComparator.compare(secondaryDeltaAPlusWh, secondaryDeltaAPlusKWh)).isEqualTo(0);
    }

    @Test
    public void compareReadingTypesIgnoringMacroPeriodAndMeasuringPeriod() {
        ReadingType secondaryDeltaAPlusWh = mockReadingType("0.0.0.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
        ReadingType min15SecondaryDeltaAPlusWh = mockReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
        ReadingType dailySecondaryDeltaAPlusWh = mockReadingType("11.0.0.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
        ReadingType min15DailySecondaryDeltaAPlusWh = mockReadingType("11.0.0.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0");

        // Business method
        ReadingTypeComparator readingTypeComparator = ReadingTypeComparator.ignoring(
                ReadingTypeComparator.Attribute.MacroPeriod,
                ReadingTypeComparator.Attribute.MeasuringPeriod
        );

        // Asserts
        assertThat(readingTypeComparator.compare(secondaryDeltaAPlusWh, min15SecondaryDeltaAPlusWh)).isEqualTo(0);
        assertThat(readingTypeComparator.compare(secondaryDeltaAPlusWh, dailySecondaryDeltaAPlusWh)).isEqualTo(0);
        assertThat(readingTypeComparator.compare(secondaryDeltaAPlusWh, min15DailySecondaryDeltaAPlusWh)).isEqualTo(0);
    }

    @Test
    public void compareRegisterAndChannelReadingTypesIgnoringMultiplier() {
        ReadingType secondaryDeltaAPlusKWh = mockReadingType("0.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
        ReadingType min15SecondaryDeltaAPlusWh = mockReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
        ReadingType dailySecondaryDeltaAPlusMWh = mockReadingType("11.0.0.4.1.1.12.0.0.0.0.0.0.0.0.6.72.0");

        // Business method
        ReadingTypeComparator readingTypeComparator = ReadingTypeComparator.ignoring(
                ReadingTypeComparator.Attribute.MacroPeriod,
                ReadingTypeComparator.Attribute.MeasuringPeriod,
                ReadingTypeComparator.Attribute.Multiplier
        );

        // Asserts
        assertThat(readingTypeComparator.compare(secondaryDeltaAPlusKWh, min15SecondaryDeltaAPlusWh)).isEqualTo(0);
        assertThat(readingTypeComparator.compare(secondaryDeltaAPlusKWh, min15SecondaryDeltaAPlusWh)).isEqualTo(0);
        assertThat(readingTypeComparator.compare(secondaryDeltaAPlusKWh, dailySecondaryDeltaAPlusMWh)).isEqualTo(0);
    }

    private ReadingType mockReadingType(String mrid) {
        return new ReadingTypeImpl(dataModel, thesaurus).init(mrid, mrid);
    }
}
