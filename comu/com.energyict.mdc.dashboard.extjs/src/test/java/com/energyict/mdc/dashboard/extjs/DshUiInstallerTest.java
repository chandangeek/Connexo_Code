package com.energyict.mdc.dashboard.extjs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Properties;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by bvn on 8/25/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class DshUiInstallerTest {
    @Test
    public void testPropertyKeysDoNotExceed256Chars() throws Exception {
        Properties properties = new Properties();
        properties.load(DshUiInstallerTest.class.getClassLoader().getResourceAsStream("i18n.properties"));
        List<String> list = properties.stringPropertyNames()
                .stream()
                .filter(p -> properties.getProperty(p).length() > 256)
                .collect(toList());
        assertThat(list).describedAs("Some properties are too long: "+list).isEmpty();
    }
}
