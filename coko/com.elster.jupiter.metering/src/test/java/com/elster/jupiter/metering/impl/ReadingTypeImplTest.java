package com.elster.jupiter.metering.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class ReadingTypeImplTest extends EqualsContractTest {

    private static final String MRID = "1.2.7.4.0.8.1.8.16.9.11";
    private static final String MRID2 = "1.2.3.4.0.8.1.9.16.9.11";
    private static final String ALIAS = "alias";
    private ReadingTypeImpl readingType = new ReadingTypeImpl(MRID, ALIAS);


    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceA() {
        return readingType;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new ReadingTypeImpl(MRID, ALIAS);
    }

    @Override
    protected Object getInstanceNotEqualToA() {
        return new ReadingTypeImpl(MRID2, ALIAS);
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testGetMRID() {
        assertThat(readingType.getMRID()).isEqualTo(MRID);
    }

    @Test
    public void testGetAliasName() {
        assertThat(readingType.getAliasName()).isEqualTo(ALIAS);
    }

    @Test
    public void testGetName() {
        assertThat(readingType.getName()).isEqualTo("10-minute Average IntervalData Net Demand Interharmonic8 Phase-N (Gsr)");
    }

    @Test
    public void testIsCumulativeReadingType() {
        assertThat(readingType.isCumulativeReadingType(new ReadingTypeImpl(MRID2, "2"))).isTrue();
    }

}
