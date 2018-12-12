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

    @Test
    @Transactional
    public void testSearchAll() {
        List<String> matches = findByRegex("*");
        assertThat(matches).hasSize(30);
    }

    @Test
    @Transactional
    public void testSearchUnderscore() {
        List<String> matches = findByRegex("*_*");
        assertThat(matches).hasSize(3).contains("ZAFB_010001", "ZAFB_010011", "ZAFB?0100_1");
    }

    @Test
    @Transactional
    public void testSearchQuestion() {
        List<String> matches = findByRegex("ZAF?0010001");
        assertThat(matches).hasSize(2).contains("ZAFC0010001", "ZAFB0010001");
    }

    @Test
    @Transactional
    public void testSearchDoubleQuestion() {
        List<String> matches = findByRegex("ZAF?00?0001");
        assertThat(matches).hasSize(4).containsExactly("ZAFB0010001", "ZAFB0020001", "ZAFC0010001", "ZAFD0020001");
    }

    @Test
    @Transactional
    public void testSearchAstrixQuestion() {
        List<String> matches = findByRegex("ZAF?001*1");
        assertThat(matches).hasSize(4).containsExactly("ZAFB0010001", "ZAFC0010001", "ZAFB00100?1", "ZAFB00100*1");
    }

    @Test
    @Transactional
    public void testSearchDoubleAstrix() {
        List<String> matches = findByRegex("Z*D00*");
        assertThat(matches).hasSize(5);
    }

    @Test
    @Transactional
    public void testSearchTrailingAstrix() {
        List<String> matches = findByRegex("ZAF*");
        assertThat(matches).hasSize(29);
    }

    @Test
    @Transactional
    public void testSearchExclamation() {
        List<String> matches = findByRegex("*!*");
        assertThat(matches).hasSize(2).contains("ZAFB!110001", "!EXCEPTION");
    }

    @Test
    @Transactional
    public void testStartExclamation() {
        List<String> matches = findByRegex("!*");
        assertThat(matches).hasSize(1).contains("!EXCEPTION");
    }

    @Test
    @Transactional
    public void testSearchAstrixLiteralCombination() {
        List<String> matches = findByRegex("*\\**");
        assertThat(matches).hasSize(2).contains("ZAFB*010001", "ZAFB00100*1");
    }

    @Test
    @Transactional
    public void testSearchBracketsCombination() {
        List<String> matches = findByRegex("*[?]*");
        assertThat(matches).hasSize(1).contains("ZAFB[1]0001");
    }

    @Test
    @Transactional
    public void testSearchBracketsLiteral() {
        List<String> matches = findByRegex("ZAFB[1]0001");
        assertThat(matches).hasSize(1).contains("ZAFB[1]0001");
    }

    @Test
    @Transactional
    public void testSearchBracketsGroupDoesNotWork() {
        List<String> matches = findByRegex("ZAFB[12]0001");
        assertThat(matches).isEmpty();
    }

    @Test
    @Transactional
    public void testSearchSqlPercentDoesNotWork() {
        List<String> matches = findByRegex("ZAF%");
        assertThat(matches).isEmpty();
    }

    @Test
    @Transactional
    public void testSearchSqlUnderscoreDoesNotWork() {
        List<String> matches = findByRegex("ZAF_0010001");
        assertThat(matches).isEmpty();
    }

    @Test
    @Transactional
    public void testSearchAstrixLiteral() {
        List<String> matches = findByRegex("*\\*1");
        assertThat(matches).hasSize(1).contains("ZAFB00100*1");
    }

    private List<String> findByRegex(String regex) {
        Condition condition = Where.where("name").likeIgnoreCase(regex);
        return DefaultFinder.of(Device.class, condition, inMemoryPersistence.getDataModel(), DeviceConfiguration.class, DeviceType.class).stream().map(Device::getName).collect(toList());
    }


}