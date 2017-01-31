/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.rest.UnitAdapter;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UnitAdapterTest {

    private final UnitAdapter unitAdapter = new UnitAdapter();

    @Test
    public void testNormalMarshal() throws Exception {
        assertThat(unitAdapter.marshal(Unit.get("kWh"))).isEqualTo("kWh");
    }

    @Test
    public void testNormalUnmarshal() throws Exception {
        assertThat(unitAdapter.unmarshal("kWh")).isEqualTo(Unit.get("kWh"));
    }

    @Test
    public void testMarshalUndefined() throws Exception {
        assertThat(unitAdapter.marshal(Unit.getUndefined())).isEqualTo("");

    }

    @Test
    public void testUnmarshalEmptyString() throws Exception {
        assertThat(unitAdapter.unmarshal("")).isEqualTo(Unit.getUndefined());

    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnmarshalIllegalUnit() throws Exception {
        unitAdapter.unmarshal("XYZ");
    }
}
