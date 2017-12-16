package com.energyict.mdc.metering.impl;

import com.energyict.obis.ObisCode;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class ObisCodeToReadingTypeFilterFactoryTest {

    private static final int NUMBER_OF_READING_TYPE_ARGUMENTS = 18;

    @Test
    public void testCountFields(){
        ObisCode code = new ObisCode();
        String regex = ObisCodeToReadingTypeFilterFactory.createMRIDFilterFrom(code);
        String[] parts = regex.split("\\.");
        assertThat(parts.length).isEqualTo(NUMBER_OF_READING_TYPE_ARGUMENTS);
    }
}
