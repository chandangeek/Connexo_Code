/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.tools;

import javax.management.openmbean.OpenDataException;
import java.util.Collection;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.tools.JmxStatistics} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-08 (13:37)
 */
public class JmxStatisticsTest {

    private static final String EXPECTED_NAME = "JmxStatisticsTest";

    @Test
    public void testNamedConstructor () {
        // Business method
        JmxStatistics statistics = new JmxStatistics(EXPECTED_NAME);

        // Asserts
        assertThat(statistics.getName()).isEqualTo(EXPECTED_NAME);
        assertThat(statistics.getCount()).isZero();
        assertThat(statistics.getMin()).isZero();
        assertThat(statistics.getMax()).isZero();
        assertThat(statistics.getAvg()).isZero();
    }

    @Test
    public void testConstructorWithAllValues () {
        int expectedCount = 113;
        long expectedMinimum = 23;
        long expectedMaximum = 97;
        long expectedAverage = 59;

        // Business method
        JmxStatistics statistics = new JmxStatistics(EXPECTED_NAME, expectedCount, expectedMinimum, expectedMaximum, expectedAverage);

        // Asserts
        assertThat(statistics.getName()).isEqualTo(EXPECTED_NAME);
        assertThat(statistics.getCount()).isEqualTo(expectedCount);
        assertThat(statistics.getMin()).isEqualTo(expectedMinimum);
        assertThat(statistics.getMax()).isEqualTo(expectedMaximum);
        assertThat(statistics.getAvg()).isEqualTo(expectedAverage);
    }

    @Test
    public void testUpdateAsFirstCall () {
        JmxStatistics statistics = new JmxStatistics(EXPECTED_NAME);
        int duration = 97;

        // Business method
        statistics.update(duration);

        // Asserts
        assertThat(statistics.getCount()).isEqualTo(1);
        assertThat(statistics.getMin()).isEqualTo(duration);
        assertThat(statistics.getMax()).isEqualTo(duration);
        assertThat(statistics.getAvg()).isEqualTo(duration);
    }

    @Test
    public void testUpdateAfterMultipleCalls () {
        JmxStatistics statistics = new JmxStatistics(EXPECTED_NAME);

        // Business method
        statistics.update(23);
        statistics.update(31);
        statistics.update(59);
        statistics.update(97);
        int expectedCount = 4;

        // Asserts
        assertThat(statistics.getCount()).isEqualTo(expectedCount);
        assertThat(statistics.getMin()).isEqualTo(23);
        assertThat(statistics.getMax()).isEqualTo(97);
        assertThat(statistics.getAvg()).isEqualTo(52);
    }

    @Test
    public void testGetUnknownAttribute () {
        JmxStatistics statistics = new JmxStatistics(EXPECTED_NAME);

        // Business method
        Object name = statistics.get("unknown");

        // Asserts
        assertThat(name).isNull();
    }

    @Test
    public void testGetNameByName () {
        JmxStatistics statistics = new JmxStatistics(EXPECTED_NAME);

        // Business method
        Object name = statistics.get("name");

        // Asserts
        assertThat(name).isEqualTo(EXPECTED_NAME);
    }

    @Test
    public void testGetCountByName () {
        int expectedCount = 113;
        long expectedMinimum = 23;
        long expectedMaximum = 97;
        long expectedAverage = 59;
        JmxStatistics statistics = new JmxStatistics(EXPECTED_NAME, expectedCount, expectedMinimum, expectedMaximum, expectedAverage);

        // Business method
        Object count = statistics.get("count");

        // Asserts
        assertThat(count).isEqualTo(expectedCount);
    }

    @Test
    public void testGetMinimumByName () {
        int expectedCount = 113;
        long expectedMinimum = 23;
        long expectedMaximum = 97;
        long expectedAverage = 59;
        JmxStatistics statistics = new JmxStatistics(EXPECTED_NAME, expectedCount, expectedMinimum, expectedMaximum, expectedAverage);

        // Business method
        Object minimum = statistics.get("min");

        // Asserts
        assertThat(minimum).isEqualTo(expectedMinimum);
    }

    @Test
    public void testGetMaximumByName () {
        int expectedCount = 113;
        long expectedMinimum = 23;
        long expectedMaximum = 97;
        long expectedAverage = 59;
        JmxStatistics statistics = new JmxStatistics(EXPECTED_NAME, expectedCount, expectedMinimum, expectedMaximum, expectedAverage);

        // Business method
        Object maximum = statistics.get("max");

        // Asserts
        assertThat(maximum).isEqualTo(expectedMaximum);
    }

    @Test
    public void testGetAverageByName () {
        int expectedCount = 113;
        long expectedMinimum = 23;
        long expectedMaximum = 97;
        long expectedAverage = 59;
        JmxStatistics statistics = new JmxStatistics(EXPECTED_NAME, expectedCount, expectedMinimum, expectedMaximum, expectedAverage);

        // Business method
        Object average = statistics.get("avg");

        // Asserts
        assertThat(average).isEqualTo(expectedAverage);
    }

    @Test
    public void testGetAllByName () {
        int expectedCount = 113;
        long expectedMinimum = 23;
        long expectedMaximum = 97;
        long expectedAverage = 59;
        JmxStatistics statistics = new JmxStatistics(EXPECTED_NAME, expectedCount, expectedMinimum, expectedMaximum, expectedAverage);

        // Business method
        Object[] all = statistics.getAll(new String[]{"name", "count", "min", "max", "avg"});

        // Asserts
        assertThat(all).contains(EXPECTED_NAME, expectedCount, expectedMinimum, expectedMaximum, expectedAverage);
    }

    @Test
    public void testGetValues () {
        int expectedCount = 113;
        long expectedMinimum = 23;
        long expectedMaximum = 97;
        long expectedAverage = 59;
        JmxStatistics statistics = new JmxStatistics(EXPECTED_NAME, expectedCount, expectedMinimum, expectedMaximum, expectedAverage);

        // Business method
        Collection values = statistics.values();

        // Asserts
        assertThat(values).contains(EXPECTED_NAME, expectedCount, expectedMinimum, expectedMaximum, expectedAverage);
    }

    @Test
    public void testGetNameBySupport () throws OpenDataException {
        JmxStatistics statistics = new JmxStatistics(EXPECTED_NAME);

        // Business method
        Object name = statistics.getSupport().get("name");

        // Asserts
        assertThat(name).isEqualTo(EXPECTED_NAME);
    }

    @Test
    public void testGetCountBySupport () throws OpenDataException {
        int expectedCount = 113;
        long expectedMinimum = 23;
        long expectedMaximum = 97;
        long expectedAverage = 59;
        JmxStatistics statistics = new JmxStatistics(EXPECTED_NAME, expectedCount, expectedMinimum, expectedMaximum, expectedAverage);

        // Business method
        Object count = statistics.getSupport().get("count");

        // Asserts
        assertThat(count).isEqualTo(expectedCount);
    }

    @Test
    public void testGetMinimumBySupport () throws OpenDataException {
        int expectedCount = 113;
        long expectedMinimum = 23;
        long expectedMaximum = 97;
        long expectedAverage = 59;
        JmxStatistics statistics = new JmxStatistics(EXPECTED_NAME, expectedCount, expectedMinimum, expectedMaximum, expectedAverage);

        // Business method
        Object minimum = statistics.getSupport().get("min");

        // Asserts
        assertThat(minimum).isEqualTo(expectedMinimum);
    }

    @Test
    public void testGetMaximumBySupport () throws OpenDataException {
        int expectedCount = 113;
        long expectedMinimum = 23;
        long expectedMaximum = 97;
        long expectedAverage = 59;
        JmxStatistics statistics = new JmxStatistics(EXPECTED_NAME, expectedCount, expectedMinimum, expectedMaximum, expectedAverage);

        // Business method
        Object maximum = statistics.getSupport().get("max");

        // Asserts
        assertThat(maximum).isEqualTo(expectedMaximum);
    }

    @Test
    public void testGetAverageBySupport () throws OpenDataException {
        int expectedCount = 113;
        long expectedMinimum = 23;
        long expectedMaximum = 97;
        long expectedAverage = 59;
        JmxStatistics statistics = new JmxStatistics(EXPECTED_NAME, expectedCount, expectedMinimum, expectedMaximum, expectedAverage);

        // Business method
        Object average = statistics.getSupport().get("avg");

        // Asserts
        assertThat(average).isEqualTo(expectedAverage);
    }

}