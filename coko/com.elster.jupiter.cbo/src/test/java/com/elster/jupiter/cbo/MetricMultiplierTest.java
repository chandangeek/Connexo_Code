/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cbo;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class MetricMultiplierTest {

    @Test
    public void testWith() {
        assertThat(MetricMultiplier.with(6)).isEqualTo(MetricMultiplier.MEGA);
    }

    @Test
    public void testMultipliersUnique() {
        Set<Integer> used = new HashSet<>();
        for (MetricMultiplier metricMultiplier : MetricMultiplier.values()) {
            int multiplier = metricMultiplier.getMultiplier();
            assertThat(used.add(multiplier)).describedAs("Multiplier " + multiplier + " is used more than once.").isTrue();
        }
    }

    @Test
    public void testMultipliersMapping() {
        for (MetricMultiplier metricMultiplier : MetricMultiplier.values()) {
            assertThat(MetricMultiplier.with(metricMultiplier.getMultiplier())).isEqualTo(metricMultiplier);
        }
    }

    @Test
    public void testMultiplierMapping() {
        for (MetricMultiplier metricMultiplier : MetricMultiplier.values()) {
            assertThat(MetricMultiplier.with(metricMultiplier.getMultiplier())).isEqualTo(metricMultiplier);
        }
    }
}
