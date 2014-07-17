package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.rest.PhenomenonAdapter;
import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UnitAdapterTest {

    private final PhenomenonAdapter phenomenonAdapter = new PhenomenonAdapter();

    @Test
    public void testNormalMarshal() throws Exception {
        assertThat(phenomenonAdapter.marshal(Unit.get("kWh"))).isEqualTo("kWh");
    }

    @Ignore
    @Test
    public void testNormalUnmarshal() throws Exception {
        assertThat(phenomenonAdapter.unmarshal("kWh")).isEqualTo(Unit.get("kWh"));
    }

    @Test
    public void testMarshalUndefined() throws Exception {
        assertThat(phenomenonAdapter.marshal(Unit.getUndefined())).isEqualTo("");

    }

    @Ignore
    @Test
    public void testUnmarshalEmptyString() throws Exception {
        assertThat(phenomenonAdapter.unmarshal("")).isEqualTo(Unit.getUndefined());

    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnmarshalIllegalUnit() throws Exception {
        phenomenonAdapter.unmarshal("XYZ");
    }
}
