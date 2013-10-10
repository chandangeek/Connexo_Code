package com.elster.jupiter.cbo;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.fest.assertions.api.Assertions.assertThat;

public class MetricMultiplierTest {

    @Test
    public void testWith() {
        assertThat(MetricMultiplier.with(6)).isEqualTo(MetricMultiplier.MEGA);
    }

    @Test
    public void testIdsUnique() {
        Set<Integer> usedIds = new HashSet<>();
        for (MetricMultiplier metricMultiplier : MetricMultiplier.values()) {
            int id = metricMultiplier.getId();
            assertThat(usedIds.add(id)).describedAs("Id " + id + " is used more than once.").isTrue();
        }
    }

    @Test
    public void testIdsMapping() {
        for (MetricMultiplier metricMultiplier : MetricMultiplier.values()) {
            assertThat(MetricMultiplier.get(metricMultiplier.getId())).isEqualTo(metricMultiplier);
        }
    }

    @Test
    public void testIdsPositive() {
        for (MetricMultiplier metricMultiplier : MetricMultiplier.values()) {
            assertThat(metricMultiplier.getId()).isGreaterThanOrEqualTo(0);
        }
    }

    @Test
    public void testMultiplierMapping() {
        for (MetricMultiplier metricMultiplier : MetricMultiplier.values()) {
            assertThat(MetricMultiplier.with(metricMultiplier.getMultiplier())).isEqualTo(metricMultiplier);
        }
    }

}
