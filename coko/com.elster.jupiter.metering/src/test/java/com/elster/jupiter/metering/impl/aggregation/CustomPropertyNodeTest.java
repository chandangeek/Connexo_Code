/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;

import com.google.common.collect.Range;

import java.time.Instant;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link CustomPropertyNode} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-30 (13:49)
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomPropertyNodeTest {

    private static final long REGISTERED_CPS_ID = 97L;
    private static final int METER_ACTIVATION_SET_SEQUENCE_NUMBER = 101;
    private static final String PROPERTY_NAME = "example";

    @Mock
    private CustomPropertySetService customPropertySetService;
    @Mock
    private PropertySpec propertySpec;
    @Mock
    private CustomPropertySet customPropertySet;
    @Mock
    private RegisteredCustomPropertySet registeredCustomPropertySet;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private MeterActivationSet meterActivationSet;
    private Range<Instant> meterActivationRange = Range.all();

    @Before
    public void initializeMocks() {
        when(this.propertySpec.getName()).thenReturn(PROPERTY_NAME);
        when(this.customPropertySet.getId()).thenReturn("CustomPropertyNodeTest");
        when(this.customPropertySet.isVersioned()).thenReturn(true);
        when(this.registeredCustomPropertySet.getCustomPropertySet()).thenReturn(this.customPropertySet);
        when(this.registeredCustomPropertySet.getId()).thenReturn(REGISTERED_CPS_ID);
        when(this.meterActivationSet.sequenceNumber()).thenReturn(METER_ACTIVATION_SET_SEQUENCE_NUMBER);
        when(this.meterActivationSet.getRange()).thenReturn(this.meterActivationRange);
    }

    @Test
    public void testVisitor() {
        CustomPropertyNode testInstance = getTestInstance();
        ServerExpressionNode.Visitor visitor = mock(ServerExpressionNode.Visitor.class);

        // Business method
        testInstance.accept(visitor);

        // Asserts
        verify(visitor).visitProperty(testInstance);
    }

    @Test
    public void intermediateDimension() {
        CustomPropertyNode testInstance = getTestInstance();

        // Business method
        IntermediateDimension intermediateDimension = testInstance.getIntermediateDimension();

        // Asserts
        assertThat(intermediateDimension.isDimensionless()).isTrue();
    }

    @Test
    public void sqlName() {
        CustomPropertyNode testInstance = getTestInstance();

        // Business method
        String sqlName = testInstance.sqlName();

        // Asserts
        verify(this.propertySpec).getName();
        assertThat(sqlName.length()).isLessThanOrEqualTo(30);
        assertThat(sqlName).contains(String.valueOf(REGISTERED_CPS_ID));
        assertThat(sqlName).contains(String.valueOf(METER_ACTIVATION_SET_SEQUENCE_NUMBER));
    }

    @Test
    public void appendDefinitionToSqlBuilder() {
        CustomPropertyNode testInstance = getTestInstance();
        ClauseAwareSqlBuilder clauseAwareSqlBuilder = mock(ClauseAwareSqlBuilder.class);
        SqlBuilder sqlBuilder = new SqlBuilder();
        when(clauseAwareSqlBuilder.with(anyString(), any(Optional.class), anyVararg())).thenReturn(sqlBuilder);
        SqlFragment sqlFragment = mock(SqlFragment.class);
        when(sqlFragment.getText()).thenReturn("select * from dual");
        when(this.customPropertySetService.getRawValuesSql(this.customPropertySet, this.propertySpec, "value", this.usagePoint, this.meterActivationRange)).thenReturn(sqlFragment);

        // Business method
        testInstance.appendDefinitionTo(clauseAwareSqlBuilder);

        // Asserts
        verify(clauseAwareSqlBuilder).with(anyString(), any(Optional.class), anyVararg());
        verify(this.customPropertySetService).getRawValuesSql(this.customPropertySet, this.propertySpec, "value", this.usagePoint, this.meterActivationRange);
        assertThat(sqlBuilder.getText()).isNotEmpty();
        assertThat(sqlBuilder.getText().toLowerCase()).startsWith("select");
        assertThat(sqlBuilder.getText().toLowerCase()).contains("from (" + sqlFragment.getText() + ")");
    }

    private CustomPropertyNode getTestInstance() {
        return new CustomPropertyNode(this.customPropertySetService, this.propertySpec, this.registeredCustomPropertySet, this.usagePoint, this.meterActivationSet);
    }

}