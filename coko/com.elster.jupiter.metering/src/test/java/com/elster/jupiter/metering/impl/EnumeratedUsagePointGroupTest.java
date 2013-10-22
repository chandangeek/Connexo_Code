package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.EnumeratedUsagePointGroup;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class EnumeratedUsagePointGroupTest {

    private EnumeratedUsagePointGroupImpl usagePointGroup;

    @Before
    public void setUp() {
        usagePointGroup = new EnumeratedUsagePointGroupImpl();
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testImplementsInterface() {
        assertThat(usagePointGroup).isInstanceOf(EnumeratedUsagePointGroup.class);
    }

    @Test
    public void testGetName() {
        String name = "name";
        usagePointGroup.setName(name);

        assertThat(usagePointGroup.getName()).isEqualTo(name);
    }

    @Test
    public void testGetMRID() {
        String mrid = "MRID";
        usagePointGroup.setMRID(mrid);

        assertThat(usagePointGroup.getMRID()).isEqualTo(mrid);
    }

    @Test
    public void testGetDescription() {
        String description = "description";
        usagePointGroup.setDescription(description);

        assertThat(usagePointGroup.getDescription()).isEqualTo(description);
    }

    @Test
    public void testGetAliasName() {
        String alias = "alias";
        usagePointGroup.setAliasName(alias);

        assertThat(usagePointGroup.getAliasName()).isEqualTo(alias);
    }

    @Test
    public void testGetType() {
        String type = "type";
        usagePointGroup.setType(type);

        assertThat(usagePointGroup.getType()).isEqualTo(type);
    }

}
