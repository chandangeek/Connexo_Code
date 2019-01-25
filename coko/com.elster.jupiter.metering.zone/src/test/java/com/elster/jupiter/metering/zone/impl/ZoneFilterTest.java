package com.elster.jupiter.metering.zone.impl;

import com.elster.jupiter.metering.zone.ZoneFilter;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ZoneFilterTest {

    @Test
    public void testZoneFilterWithZoneTypes() {
        ZoneFilter zoneFilter = new ZoneFilterImpl();
        List<Long> zoneTypeIds = Arrays.asList(1L, 2L, 3L);
        zoneFilter.setZoneTypes(zoneTypeIds);
        assertThat(zoneFilter.toCondition().toString()).isEqualTo("(zoneType.id IN [1, 2, 3])");
    }

    @Test
    public void testZoneFilterWithNullZoneTypes() {
        ZoneFilter zoneFilter = new ZoneFilterImpl();
        assertThat(zoneFilter.toCondition().toString()).isEqualTo("TRUE");
    }
}
