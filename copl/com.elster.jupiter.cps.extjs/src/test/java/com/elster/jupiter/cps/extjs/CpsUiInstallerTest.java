/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps.extjs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Properties;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class CpsUiInstallerTest {
    @Test
    public void testPropertyValuesDoNotExceed256Chars() throws Exception {
        Properties properties = new Properties();
        properties.load(CpsUiInstaller.class.getClassLoader().getResourceAsStream("i18n.properties"));
        List<String> list = properties.stringPropertyNames()
                .stream()
                .filter(p -> properties.getProperty(p).length() > 256)
                .collect(toList());
        assertThat(list).describedAs("Some property values are too long: "+list).isEmpty();
    }

    @Test
    public void testPropertyKeysDoNotExceed256Chars() throws Exception {
        Properties properties = new Properties();
        properties.load(CpsUiInstaller.class.getClassLoader().getResourceAsStream("i18n.properties"));
        List<String> list = properties.stringPropertyNames()
                .stream()
                .filter(p -> p.length() > 256)
                .collect(toList());
        assertThat(list).describedAs("Some property keys are too long: "+list).isEmpty();
    }
}
