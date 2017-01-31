/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.extjs;

import com.elster.jupiter.orm.Table;

import java.util.List;
import java.util.Properties;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by bvn on 8/25/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class FirmwareUiInstallerTest {
    @Test
    public void testPropertyValuesDoNotExceed256Chars() throws Exception {
        Properties properties = new Properties();
        properties.load(FirmwareUiInstaller.class.getClassLoader().getResourceAsStream("i18n.properties"));
        List<String> list = properties.stringPropertyNames()
                .stream()
                .filter(p -> properties.getProperty(p).length() > Table.SHORT_DESCRIPTION_LENGTH)
                .collect(toList());
        assertThat(list).describedAs("Some property values are too long: "+list).isEmpty();
    }

    @Test
    public void testPropertyKeysDoNotExceed256Chars() throws Exception {
        Properties properties = new Properties();
        properties.load(FirmwareUiInstaller.class.getClassLoader().getResourceAsStream("i18n.properties"));
        List<String> list = properties.stringPropertyNames()
                .stream()
                .filter(p -> p.length() > Table.SHORT_DESCRIPTION_LENGTH)
                .collect(toList());
        assertThat(list).describedAs("Some property keys are too long: "+list).isEmpty();
    }
}
