/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.slp.SyntheticLoadProfile;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.elster.jupiter.util.units.Unit;

import com.google.common.collect.Range;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link SyntheticLoadProfilePropertyNode} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-02-14 (15:16)
 */
@RunWith(MockitoJUnitRunner.class)
public class SyntheticLoadProfilePropertyNodeTest {

    private static final long REGISTERED_CPS_ID = 97L;
    private static final int METER_ACTIVATION_SET_SEQUENCE_NUMBER = 101;
    private static final String SLP_PROPERTY_NAME = "slp";
    private static final String OTHER_PROPERTY_NAME = "other";
    private static final String UNKNOWN_PROPERTY_NAME = "unknown";

    @Mock
    private CustomPropertySetService customPropertySetService;
    @Mock
    private CustomPropertySet<UsagePoint, ?> customPropertySet;
    @Mock
    private PropertySpec slpPropertySpec;
    @Mock
    private RegisteredCustomPropertySet registeredCustomPropertySet;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private MeterActivationSet meterActivationSet;
    @Mock
    private SyntheticLoadProfile syntheticLoadProfile;

    private Range<Instant> meterActivationRange = Range.atLeast(Instant.EPOCH);

    @Before
    public void initializeMocks() {
        when(this.meterActivationSet.sequenceNumber()).thenReturn(METER_ACTIVATION_SET_SEQUENCE_NUMBER);
        when(this.meterActivationSet.getRange()).thenReturn(this.meterActivationRange);
        when(this.syntheticLoadProfile.getUnitOfMeasure()).thenReturn(Unit.WATT_HOUR);
        when(this.syntheticLoadProfile.getInterval()).thenReturn(Duration.ofMinutes(15));
        this.initializeCustomPropertySet();
    }

    private void initializeCustomPropertySet() {
        this.initializeSlpPropertySpec();
        PropertySpec other = mock(PropertySpec.class);
        when(other.getName()).thenReturn(OTHER_PROPERTY_NAME);
        when(other.isReference()).thenReturn(false);
        when(other.getValueFactory()).thenReturn(new StringFactory());
        when(this.customPropertySet.getPropertySpecs()).thenReturn(Arrays.asList(other, this.slpPropertySpec));
        when(this.registeredCustomPropertySet.getCustomPropertySet()).thenReturn(this.customPropertySet);
        when(this.registeredCustomPropertySet.getId()).thenReturn(REGISTERED_CPS_ID);
    }

    private void initializeSlpPropertySpec() {
        ValueFactory slpValueFactory = mock(ValueFactory.class);
        when(slpValueFactory.isReference()).thenReturn(true);
        when(slpValueFactory.getValueType()).thenReturn(SyntheticLoadProfile.class);
        when(this.slpPropertySpec.getName()).thenReturn(SLP_PROPERTY_NAME);
        when(this.slpPropertySpec.isReference()).thenReturn(true);
        when(this.slpPropertySpec.getValueFactory()).thenReturn(slpValueFactory);
    }

    private void useVersionedCustomPropertySet() {
        when(this.customPropertySet.isVersioned()).thenReturn(true);
        when(this.customPropertySet.getId()).thenReturn(SyntheticLoadProfilePropertyNodeTest.class.getSimpleName() + ".versioned");
        CustomPropertySetValues values = CustomPropertySetValues.emptyFrom(this.meterActivationSet.getRange().lowerEndpoint());
        values.setProperty(OTHER_PROPERTY_NAME, "OTHER");
        values.setProperty(SLP_PROPERTY_NAME, this.syntheticLoadProfile);
        when(this.customPropertySetService
                .getUniqueValuesFor(this.customPropertySet, this.usagePoint, this.meterActivationSet.getRange().lowerEndpoint()))
            .thenReturn(values);
    }

    private void useNonVersionedCustomPropertySet() {
        when(this.customPropertySet.isVersioned()).thenReturn(false);
        when(this.customPropertySet.getId()).thenReturn(SyntheticLoadProfilePropertyNodeTest.class.getSimpleName() + "non.versioned");
        CustomPropertySetValues values = CustomPropertySetValues.emptyFrom(this.meterActivationSet.getRange().lowerEndpoint());
        values.setProperty(OTHER_PROPERTY_NAME, "OTHER");
        values.setProperty(SLP_PROPERTY_NAME, this.syntheticLoadProfile);
        when(this.customPropertySetService
                .getUniqueValuesFor(this.customPropertySet, this.usagePoint))
                .thenReturn(values);
    }

    @Test
    public void testVisitorForVersionedCPS() {
        this.useVersionedCustomPropertySet();
        SyntheticLoadProfilePropertyNode testInstance = getTestInstance();
        ServerExpressionNode.Visitor visitor = mock(ServerExpressionNode.Visitor.class);

        // Business method
        testInstance.accept(visitor);

        // Asserts
        verify(visitor).visitSyntheticLoadProfile(testInstance);
    }

    @Test
    public void testVisitorForNonVersionedCPS() {
        this.useNonVersionedCustomPropertySet();
        SyntheticLoadProfilePropertyNode testInstance = getTestInstance();
        ServerExpressionNode.Visitor visitor = mock(ServerExpressionNode.Visitor.class);

        // Business method
        testInstance.accept(visitor);

        // Asserts
        verify(visitor).visitSyntheticLoadProfile(testInstance);
    }

    @Test
    public void intermediateDimensionForVersionedCPS() {
        this.useVersionedCustomPropertySet();
        SyntheticLoadProfilePropertyNode testInstance = getTestInstance();

        // Business method
        testInstance.getIntermediateDimension();

        // Asserts
        verify(this.syntheticLoadProfile).getUnitOfMeasure();
    }

    @Test
    public void intermediateDimensionForNonVersionedCPS() {
        this.useNonVersionedCustomPropertySet();
        SyntheticLoadProfilePropertyNode testInstance = getTestInstance();

        // Business method
        testInstance.getIntermediateDimension();

        // Asserts
        verify(this.syntheticLoadProfile).getUnitOfMeasure();
    }

    @Test
    public void intervalLengthForVersionedCPS() {
        this.useVersionedCustomPropertySet();
        SyntheticLoadProfilePropertyNode testInstance = getTestInstance();

        // Business method
        testInstance.getIntervalLength();

        // Asserts
        verify(this.syntheticLoadProfile).getInterval();
    }

    @Test
    public void intervalLengthForNonVersionedCPS() {
        this.useNonVersionedCustomPropertySet();
        SyntheticLoadProfilePropertyNode testInstance = getTestInstance();

        // Business method
        testInstance.getIntervalLength();

        // Asserts
        verify(this.syntheticLoadProfile).getInterval();
    }

    @Test
    public void sourceReadingTypeForVersionedCPS() {
        this.useVersionedCustomPropertySet();
        SyntheticLoadProfilePropertyNode testInstance = getTestInstance();

        // Business method
        VirtualReadingType readingType = testInstance.getSourceReadingType();

        // Asserts
        verify(this.syntheticLoadProfile).getInterval();
        verify(this.syntheticLoadProfile, atLeastOnce()).getUnitOfMeasure();
        assertThat(readingType.getCommodity()).isEqualTo(Commodity.ELECTRICITY_SECONDARY_METERED);
        assertThat(readingType.getAccumulation()).isEqualTo(Accumulation.NOTAPPLICABLE);
        assertThat(readingType.getIntervalLength()).isEqualTo(IntervalLength.MINUTE15);
        assertThat(readingType.getUnit()).isEqualTo(ReadingTypeUnit.WATTHOUR);
        assertThat(readingType.getUnitMultiplier()).isEqualTo(MetricMultiplier.ZERO);
        assertThat(readingType.getTimeOfUseBucket()).isEqualTo(0);
    }

    @Test
    public void sourceReadingTypeForNonVersionedCPS() {
        this.useNonVersionedCustomPropertySet();
        SyntheticLoadProfilePropertyNode testInstance = getTestInstance();

        // Business method
        VirtualReadingType readingType = testInstance.getSourceReadingType();

        // Asserts
        verify(this.syntheticLoadProfile).getInterval();
        verify(this.syntheticLoadProfile, atLeastOnce()).getUnitOfMeasure();
        assertThat(readingType.getCommodity()).isEqualTo(Commodity.ELECTRICITY_SECONDARY_METERED);
        assertThat(readingType.getAccumulation()).isEqualTo(Accumulation.NOTAPPLICABLE);
        assertThat(readingType.getIntervalLength()).isEqualTo(IntervalLength.MINUTE15);
        assertThat(readingType.getUnit()).isEqualTo(ReadingTypeUnit.WATTHOUR);
        assertThat(readingType.getUnitMultiplier()).isEqualTo(MetricMultiplier.ZERO);
        assertThat(readingType.getTimeOfUseBucket()).isEqualTo(0);
    }

    @Test
    public void sqlNameForVersionedCPS() {
        this.useVersionedCustomPropertySet();
        SyntheticLoadProfilePropertyNode testInstance = getTestInstance();
        reset(this.slpPropertySpec);
        this.initializeSlpPropertySpec();

        // Business method
        String sqlName = testInstance.sqlName();

        // Asserts
        verify(this.slpPropertySpec).getName();
        assertThat(sqlName.length()).isLessThanOrEqualTo(30);
        assertThat(sqlName).contains(String.valueOf(REGISTERED_CPS_ID));
        assertThat(sqlName).contains(String.valueOf(METER_ACTIVATION_SET_SEQUENCE_NUMBER));
    }

    @Test
    public void sqlNameForNonVersionedCPS() {
        this.useNonVersionedCustomPropertySet();
        SyntheticLoadProfilePropertyNode testInstance = getTestInstance();
        reset(this.slpPropertySpec);
        this.initializeSlpPropertySpec();

        // Business method
        String sqlName = testInstance.sqlName();

        // Asserts
        verify(this.slpPropertySpec).getName();
        assertThat(sqlName.length()).isLessThanOrEqualTo(30);
        assertThat(sqlName).contains(String.valueOf(REGISTERED_CPS_ID));
        assertThat(sqlName).contains(String.valueOf(METER_ACTIVATION_SET_SEQUENCE_NUMBER));
    }

    @Test
    public void appendDefinitionToSqlBuilderForVersionedCPS() {
        this.useVersionedCustomPropertySet();
        SyntheticLoadProfilePropertyNode testInstance = getTestInstance();
        ClauseAwareSqlBuilder clauseAwareSqlBuilder = mock(ClauseAwareSqlBuilder.class);
        SqlBuilder sqlBuilder = new SqlBuilder();
        when(clauseAwareSqlBuilder.with(anyString(), any(Optional.class), anyVararg())).thenReturn(sqlBuilder);
        SqlFragment sqlFragment = mock(SqlFragment.class);
        when(sqlFragment.getText()).thenReturn("select * from dual");
        when(this.syntheticLoadProfile.getRawValuesSql(eq(this.meterActivationRange), anyVararg())).thenReturn(sqlFragment);

        // Business method
        testInstance.appendDefinitionTo(clauseAwareSqlBuilder);

        // Asserts
        verify(clauseAwareSqlBuilder).with(anyString(), any(Optional.class), anyVararg());
        verify(this.syntheticLoadProfile).getRawValuesSql(eq(this.meterActivationRange), anyVararg());
        assertThat(sqlBuilder.getText()).isNotEmpty();
        assertThat(sqlBuilder.getText().toLowerCase()).startsWith("select");
        assertThat(sqlBuilder.getText().toLowerCase()).contains("from (" + sqlFragment.getText() + ")");
    }

    @Test
    public void appendDefinitionToSqlBuilderForNonVersionedCPS() {
        this.useNonVersionedCustomPropertySet();
        SyntheticLoadProfilePropertyNode testInstance = getTestInstance();
        ClauseAwareSqlBuilder clauseAwareSqlBuilder = mock(ClauseAwareSqlBuilder.class);
        SqlBuilder sqlBuilder = new SqlBuilder();
        when(clauseAwareSqlBuilder.with(anyString(), any(Optional.class), anyVararg())).thenReturn(sqlBuilder);
        SqlFragment sqlFragment = mock(SqlFragment.class);
        when(sqlFragment.getText()).thenReturn("select * from dual");
        when(this.syntheticLoadProfile.getRawValuesSql(eq(this.meterActivationRange), anyVararg())).thenReturn(sqlFragment);

        // Business method
        testInstance.appendDefinitionTo(clauseAwareSqlBuilder);

        // Asserts
        verify(clauseAwareSqlBuilder).with(anyString(), any(Optional.class), anyVararg());
        verify(this.syntheticLoadProfile).getRawValuesSql(eq(this.meterActivationRange), anyVararg());
        assertThat(sqlBuilder.getText()).isNotEmpty();
        assertThat(sqlBuilder.getText().toLowerCase()).startsWith("select");
        assertThat(sqlBuilder.getText().toLowerCase()).contains("from (" + sqlFragment.getText() + ")");
    }

    private SyntheticLoadProfilePropertyNode getTestInstance() {
        return new SyntheticLoadProfilePropertyNode(this.customPropertySetService, this.slpPropertySpec, this.registeredCustomPropertySet, this.usagePoint, this.meterActivationSet);
    }

}