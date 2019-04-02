/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;

import java.time.Instant;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class WhereLikeClauseTest extends PersistenceIntegrationTest {

    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    private Device createSimpleDeviceWithName(String name) {
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, name, "" + name.hashCode(), Instant.now());
        device.save();
        return device;
    }

    @Before
    public void setUp() throws Exception {
        createSimpleDeviceWithName("ZAFB0010001");
        createSimpleDeviceWithName("ZAFB0010002");
        createSimpleDeviceWithName("ZAFB0010003");
        createSimpleDeviceWithName("ZAFB0010004");
        createSimpleDeviceWithName("ZAFB0010005");
        createSimpleDeviceWithName("ZAFB0020001");
        createSimpleDeviceWithName("ZAFB0020002");
        createSimpleDeviceWithName("ZAFB0020003");
        createSimpleDeviceWithName("ZAFB0020004");
        createSimpleDeviceWithName("ZAFB0020005");
        createSimpleDeviceWithName("ZAFC0010001");
        createSimpleDeviceWithName("ZAFC0010002");
        createSimpleDeviceWithName("ZAFC0010003");
        createSimpleDeviceWithName("ZAFC0010004");
        createSimpleDeviceWithName("ZAFC0010005");
        createSimpleDeviceWithName("ZAFD0020001");
        createSimpleDeviceWithName("ZAFD0020002");
        createSimpleDeviceWithName("ZAFD0020003");
        createSimpleDeviceWithName("ZAFD0020004");
        createSimpleDeviceWithName("ZAFD0020005");
        createSimpleDeviceWithName("ZAFB_010001");
        createSimpleDeviceWithName("ZAFB?0100_1");
        createSimpleDeviceWithName("ZAFB00100?1");
        createSimpleDeviceWithName("ZAFB_010011");
        createSimpleDeviceWithName("ZAFB%010001");
        createSimpleDeviceWithName("ZAFB*010001");
        createSimpleDeviceWithName("ZAFB00100*1");
        createSimpleDeviceWithName("ZAFB[1]0001");
        createSimpleDeviceWithName("ZAFB!110001");
        createSimpleDeviceWithName("!EXCEPTION");
    }

    /**
     * Moved all the tests in one class to avoid creating the devices for each test.
     * Thus, execution time was reduced to ~4% of the initial duration.
     */
    @Test
    @Transactional
    public void testSearchAll() {
        List<String> matches = findByRegex("*");
        assertThat(matches).hasSize(30);

        matches = findByRegex("*_*");
        assertThat(matches).hasSize(3).contains("ZAFB_010001", "ZAFB_010011", "ZAFB?0100_1");

        matches = findByRegex("ZAF?0010001");
        assertThat(matches).hasSize(2).contains("ZAFC0010001", "ZAFB0010001");

        matches = findByRegex("ZAF?00?0001");
        assertThat(matches).hasSize(4).containsExactly("ZAFB0010001", "ZAFB0020001", "ZAFC0010001", "ZAFD0020001");

        matches = findByRegex("ZAF?001*1");
        assertThat(matches).hasSize(4).containsExactly("ZAFB0010001", "ZAFC0010001", "ZAFB00100?1", "ZAFB00100*1");

        matches = findByRegex("Z*D00*");
        assertThat(matches).hasSize(5);

        matches = findByRegex("ZAF*");
        assertThat(matches).hasSize(29);

        matches = findByRegex("*!*");
        assertThat(matches).hasSize(2).contains("ZAFB!110001", "!EXCEPTION");

        matches = findByRegex("!*");
        assertThat(matches).hasSize(1).contains("!EXCEPTION");

        matches = findByRegex("*\\**");
        assertThat(matches).hasSize(2).contains("ZAFB*010001", "ZAFB00100*1");

        matches = findByRegex("*[?]*");
        assertThat(matches).hasSize(1).contains("ZAFB[1]0001");

        matches = findByRegex("ZAFB[1]0001");
        assertThat(matches).hasSize(1).contains("ZAFB[1]0001");

        matches = findByRegex("ZAFB[12]0001");
        assertThat(matches).isEmpty();

        matches = findByRegex("ZAF%");
        assertThat(matches).isEmpty();

        matches = findByRegex("ZAF_0010001");
        assertThat(matches).isEmpty();

        matches = findByRegex("*\\*1");
        assertThat(matches).hasSize(1).contains("ZAFB00100*1");
    }

    private List<String> findByRegex(String regex) {
        Condition condition = Where.where("name").likeIgnoreCase(regex);
        return DefaultFinder.of(Device.class, condition, inMemoryPersistence.getDataModel(), DeviceConfiguration.class, DeviceType.class).stream().map(Device::getName).collect(toList());
    }
}