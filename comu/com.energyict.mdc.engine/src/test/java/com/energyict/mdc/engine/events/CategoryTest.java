/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.events;

import org.fest.assertions.Assertions;

import org.junit.Test;

/**
 * Tests the {@link com.energyict.mdc.engine.events.Category} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-07 (16:43)
 */
public class CategoryTest {

    @Test
    public void testValueOfIgnoreCaseForConnection () {
        Assertions.assertThat(Category.valueOfIgnoreCase("connection")).isEqualTo(Category.CONNECTION);
        Assertions.assertThat(Category.valueOfIgnoreCase("CONNECTION")).isEqualTo(Category.CONNECTION);
        Assertions.assertThat(Category.valueOfIgnoreCase("Connection")).isEqualTo(Category.CONNECTION);
        Assertions.assertThat(Category.valueOfIgnoreCase("ConnectioN")).isEqualTo(Category.CONNECTION);
    }

    @Test
    public void testValueOfIgnoreCaseForComtask () {
        Assertions.assertThat(Category.valueOfIgnoreCase("comtask")).isEqualTo(Category.COMTASK);
        Assertions.assertThat(Category.valueOfIgnoreCase("COMTASK")).isEqualTo(Category.COMTASK);
        Assertions.assertThat(Category.valueOfIgnoreCase("Comtask")).isEqualTo(Category.COMTASK);
        Assertions.assertThat(Category.valueOfIgnoreCase("ComTask")).isEqualTo(Category.COMTASK);
    }

    @Test
    public void testValueOfIgnoreCaseForCollectedData () {
        Assertions.assertThat(Category.valueOfIgnoreCase("collected_data_processing")).isEqualTo(Category.COLLECTED_DATA_PROCESSING);
        Assertions.assertThat(Category.valueOfIgnoreCase("collected_Data_processing")).isEqualTo(Category.COLLECTED_DATA_PROCESSING);
        Assertions.assertThat(Category.valueOfIgnoreCase("collected_Data_Processing")).isEqualTo(Category.COLLECTED_DATA_PROCESSING);
        Assertions.assertThat(Category.valueOfIgnoreCase("collected_data_Processing")).isEqualTo(Category.COLLECTED_DATA_PROCESSING);
        Assertions.assertThat(Category.valueOfIgnoreCase("COLLECTED_DATA_processing")).isEqualTo(Category.COLLECTED_DATA_PROCESSING);
        Assertions.assertThat(Category.valueOfIgnoreCase("Collected_data_processing")).isEqualTo(Category.COLLECTED_DATA_PROCESSING);
        Assertions.assertThat(Category.valueOfIgnoreCase("Collected_Data_processing")).isEqualTo(Category.COLLECTED_DATA_PROCESSING);
        Assertions.assertThat(Category.valueOfIgnoreCase("Collected_Data_Processing")).isEqualTo(Category.COLLECTED_DATA_PROCESSING);
        Assertions.assertThat(Category.valueOfIgnoreCase("Collected_data_Processing")).isEqualTo(Category.COLLECTED_DATA_PROCESSING);
    }

    @Test
    public void testValueOfIgnoreCaseForLogging () {
        Assertions.assertThat(Category.valueOfIgnoreCase("logging")).isEqualTo(Category.LOGGING);
        Assertions.assertThat(Category.valueOfIgnoreCase("LOGGING")).isEqualTo(Category.LOGGING);
        Assertions.assertThat(Category.valueOfIgnoreCase("Logging")).isEqualTo(Category.LOGGING);
        Assertions.assertThat(Category.valueOfIgnoreCase("LogginG")).isEqualTo(Category.LOGGING);
    }

}