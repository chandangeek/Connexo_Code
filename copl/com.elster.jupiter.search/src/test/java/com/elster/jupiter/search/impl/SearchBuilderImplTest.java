/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.search.impl;

import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.impl.PropertySpecPossibleValuesImpl;
import com.elster.jupiter.search.SearchBuilder;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link SearchBuilderImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-01 (14:17)
 */
@RunWith(MockitoJUnitRunner.class)
public class SearchBuilderImplTest {

    private static final String REF_PROPERTY_NAME = "ref";
    private static final String ID_PROPERTY_NAME = "id";
    private static final String NAME_PROPERTY_NAME = "name";
    private static final long ID1_VALUE = 97L;
    private static final long ID2_VALUE = 101L;
    private static final long ID3_VALUE = 103L;

    @Mock
    private SearchDomain searchDomain;
    @Mock
    private SearchMonitor searchMonitor;
    @Mock
    private SearchableProperty idSearchProperty;
    @Mock
    private SearchableProperty nameSearchProperty;
    @Mock
    private SearchableProperty referenceSearchProperty;
    @Captor
    private ArgumentCaptor<List<SearchablePropertyCondition>> conditionsArgumentCaptor;

    @Before
    public void initializeMocks() {
        PropertySpec idPropertySpec = mock(PropertySpec.class);
        when(idPropertySpec.getName()).thenReturn(ID_PROPERTY_NAME);
        when(idPropertySpec.isRequired()).thenReturn(true);
        when(idPropertySpec.isReference()).thenReturn(false);
        when(idPropertySpec.getValueFactory()).thenReturn(new BigDecimalFactory());
        when(idPropertySpec.getPossibleValues()).thenReturn(new PropertySpecPossibleValuesImpl());
        when(this.idSearchProperty.getConstraints()).thenReturn(Collections.<SearchableProperty>emptyList());
        when(this.idSearchProperty.getGroup()).thenReturn(Optional.<SearchablePropertyGroup>empty());
        when(this.idSearchProperty.getSpecification()).thenReturn(idPropertySpec);
        when(this.idSearchProperty.getName()).thenReturn(ID_PROPERTY_NAME);
        when(this.idSearchProperty.hasName(ID_PROPERTY_NAME)).thenReturn(true);
        PropertySpec namePropertySpec = mock(PropertySpec.class);
        when(namePropertySpec.getName()).thenReturn(NAME_PROPERTY_NAME);
        when(namePropertySpec.isRequired()).thenReturn(true);
        when(namePropertySpec.isReference()).thenReturn(false);
        when(namePropertySpec.getValueFactory()).thenReturn(new StringFactory());
        when(namePropertySpec.getPossibleValues()).thenReturn(new PropertySpecPossibleValuesImpl());
        when(this.nameSearchProperty.getConstraints()).thenReturn(Collections.<SearchableProperty>emptyList());
        when(this.nameSearchProperty.getGroup()).thenReturn(Optional.<SearchablePropertyGroup>empty());
        when(this.nameSearchProperty.getSpecification()).thenReturn(namePropertySpec);
        when(this.nameSearchProperty.getName()).thenReturn(NAME_PROPERTY_NAME);
        when(this.nameSearchProperty.hasName(NAME_PROPERTY_NAME)).thenReturn(true);
        when(this.searchDomain.getId()).thenReturn(Example.class.getName());
        doReturn(Example.class).when(this.searchDomain).getDomainClass();
        PropertySpec refPropertySpec = mock(PropertySpec.class);
        when(refPropertySpec.getName()).thenReturn(REF_PROPERTY_NAME);
        when(refPropertySpec.isRequired()).thenReturn(true);
        when(refPropertySpec.isReference()).thenReturn(true);
        when(refPropertySpec.getValueFactory()).thenReturn(new ExampleValueFactory());
        when(refPropertySpec.getPossibleValues()).thenReturn(new PropertySpecPossibleValuesImpl());
        when(this.referenceSearchProperty.getConstraints()).thenReturn(Collections.emptyList());
        when(this.referenceSearchProperty.getGroup()).thenReturn(Optional.<SearchablePropertyGroup>empty());
        when(this.referenceSearchProperty.getSpecification()).thenReturn(refPropertySpec);
        when(this.referenceSearchProperty.getName()).thenReturn(REF_PROPERTY_NAME);
        when(this.referenceSearchProperty.hasName(REF_PROPERTY_NAME)).thenReturn(true);
        when(this.searchDomain.getProperties()).thenReturn(Arrays.asList(this.idSearchProperty, this.nameSearchProperty, this.referenceSearchProperty));
    }

    @Test
    public void testConstructor() {
        // Business method
        SearchBuilderImpl<Example> builder = this.getTestInstance();

        // Asserts
        assertThat(builder.getDomain()).isEqualTo(this.searchDomain);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whereForNonExistingProperty() {
        SearchBuilderImpl<Example> builder = this.getTestInstance();

        // Business method
        builder.where("DoesNotExists");

        // Asserts: see expected exception rule
    }

    @Test
    public void whereDoesNotReturnNull() {
        SearchBuilderImpl<Example> builder = this.getTestInstance();

        // Business method
        SearchBuilder.CriterionBuilder<Example> criterionBuilder = builder.where(ID_PROPERTY_NAME);

        // Asserts
        assertThat(criterionBuilder).isNotNull();
    }

    @Test(expected = IllegalStateException.class)
    public void whereWithoutOperatorProducesAnIllegalStateException() {
        SearchBuilderImpl<Example> builder = this.getTestInstance();
        builder.where(ID_PROPERTY_NAME);

        // Business method
        builder.toFinder();

        // Asserts: see expected exception rule
    }

    @Test
    public void whereIdEqualTo() throws InvalidValueException {
        SearchBuilderImpl<Example> builder = this.getTestInstance();
        builder.where(ID_PROPERTY_NAME).isEqualTo(ID1_VALUE);

        // Business method
        builder.toFinder();

        // Asserts
        verify(this.searchDomain).finderFor(this.conditionsArgumentCaptor.capture());
        List<SearchablePropertyCondition> capturedSearchablePropertyConditions = this.conditionsArgumentCaptor.getValue();
        assertThat(capturedSearchablePropertyConditions).hasSize(1);
        SearchablePropertyCondition searchablePropertyCondition = capturedSearchablePropertyConditions.get(0);
        assertThat(searchablePropertyCondition.getProperty()).isEqualTo(this.idSearchProperty);
        assertThat(searchablePropertyCondition.getCondition()).isInstanceOf(Comparison.class);
        Comparison comparison = (Comparison) searchablePropertyCondition.getCondition();
        assertThat(comparison.getFieldName()).isEqualTo(ID_PROPERTY_NAME);
        assertThat(comparison.getOperator()).isEqualTo(Operator.EQUAL);
        assertThat(comparison.getValues()).containsOnly(ID1_VALUE);
    }

    @Test
    public void whereIdNotEqualTo() throws InvalidValueException {
        SearchBuilderImpl<Example> builder = this.getTestInstance();
        builder.where(ID_PROPERTY_NAME).isNotEqualTo(ID1_VALUE);

        // Business method
        builder.toFinder();

        // Asserts
        verify(this.searchDomain).finderFor(this.conditionsArgumentCaptor.capture());
        List<SearchablePropertyCondition> capturedSearchablePropertyConditions = this.conditionsArgumentCaptor.getValue();
        assertThat(capturedSearchablePropertyConditions).hasSize(1);
        SearchablePropertyCondition searchablePropertyCondition = capturedSearchablePropertyConditions.get(0);
        assertThat(searchablePropertyCondition.getProperty()).isEqualTo(this.idSearchProperty);
        assertThat(searchablePropertyCondition.getCondition()).isInstanceOf(Comparison.class);
        Comparison comparison = (Comparison) searchablePropertyCondition.getCondition();
        assertThat(comparison.getFieldName()).isEqualTo(ID_PROPERTY_NAME);
        assertThat(comparison.getOperator()).isEqualTo(Operator.NOTEQUAL);
        assertThat(comparison.getValues()).containsOnly(ID1_VALUE);
    }

    @Test
    public void whereIdLessThen() throws InvalidValueException {
        SearchBuilderImpl<Example> builder = this.getTestInstance();
        builder.where(ID_PROPERTY_NAME).isLessThan(ID1_VALUE);

        // Business method
        builder.toFinder();

        // Asserts
        verify(this.searchDomain).finderFor(this.conditionsArgumentCaptor.capture());
        List<SearchablePropertyCondition> capturedSearchablePropertyConditions = this.conditionsArgumentCaptor.getValue();
        assertThat(capturedSearchablePropertyConditions).hasSize(1);
        SearchablePropertyCondition searchablePropertyCondition = capturedSearchablePropertyConditions.get(0);
        assertThat(searchablePropertyCondition.getProperty()).isEqualTo(this.idSearchProperty);
        assertThat(searchablePropertyCondition.getCondition()).isInstanceOf(Comparison.class);
        Comparison comparison = (Comparison) searchablePropertyCondition.getCondition();
        assertThat(comparison.getFieldName()).isEqualTo(ID_PROPERTY_NAME);
        assertThat(comparison.getOperator()).isEqualTo(Operator.LESSTHAN);
        assertThat(comparison.getValues()).containsOnly(ID1_VALUE);
    }

    @Test
    public void whereIdLessThenOrEqual() throws InvalidValueException {
        SearchBuilderImpl<Example> builder = this.getTestInstance();
        builder.where(ID_PROPERTY_NAME).isLessThanOrEqualTo(ID1_VALUE);

        // Business method
        builder.toFinder();

        // Asserts
        verify(this.searchDomain).finderFor(this.conditionsArgumentCaptor.capture());
        List<SearchablePropertyCondition> capturedSearchablePropertyConditions = this.conditionsArgumentCaptor.getValue();
        assertThat(capturedSearchablePropertyConditions).hasSize(1);
        SearchablePropertyCondition searchablePropertyCondition = capturedSearchablePropertyConditions.get(0);
        assertThat(searchablePropertyCondition.getProperty()).isEqualTo(this.idSearchProperty);
        assertThat(searchablePropertyCondition.getCondition()).isInstanceOf(Comparison.class);
        Comparison comparison = (Comparison) searchablePropertyCondition.getCondition();
        assertThat(comparison.getFieldName()).isEqualTo(ID_PROPERTY_NAME);
        assertThat(comparison.getOperator()).isEqualTo(Operator.LESSTHANOREQUAL);
        assertThat(comparison.getValues()).containsOnly(ID1_VALUE);
    }

    @Test
    public void whereIdGreaterThen() throws InvalidValueException {
        SearchBuilderImpl<Example> builder = this.getTestInstance();
        builder.where(ID_PROPERTY_NAME).isGreaterThan(ID1_VALUE);

        // Business method
        builder.toFinder();

        // Asserts
        verify(this.searchDomain).finderFor(this.conditionsArgumentCaptor.capture());
        List<SearchablePropertyCondition> capturedSearchablePropertyConditions = this.conditionsArgumentCaptor.getValue();
        assertThat(capturedSearchablePropertyConditions).hasSize(1);
        SearchablePropertyCondition searchablePropertyCondition = capturedSearchablePropertyConditions.get(0);
        assertThat(searchablePropertyCondition.getProperty()).isEqualTo(this.idSearchProperty);
        assertThat(searchablePropertyCondition.getCondition()).isInstanceOf(Comparison.class);
        Comparison comparison = (Comparison) searchablePropertyCondition.getCondition();
        assertThat(comparison.getFieldName()).isEqualTo(ID_PROPERTY_NAME);
        assertThat(comparison.getOperator()).isEqualTo(Operator.GREATERTHAN);
        assertThat(comparison.getValues()).containsOnly(ID1_VALUE);
    }

    @Test
    public void whereIdGreaterThenOrEqual() throws InvalidValueException {
        SearchBuilderImpl<Example> builder = this.getTestInstance();
        builder.where(ID_PROPERTY_NAME).isGreaterThanOrEqualTo(ID1_VALUE);

        // Business method
        builder.toFinder();

        // Asserts
        verify(this.searchDomain).finderFor(this.conditionsArgumentCaptor.capture());
        List<SearchablePropertyCondition> capturedSearchablePropertyConditions = this.conditionsArgumentCaptor.getValue();
        assertThat(capturedSearchablePropertyConditions).hasSize(1);
        SearchablePropertyCondition searchablePropertyCondition = capturedSearchablePropertyConditions.get(0);
        assertThat(searchablePropertyCondition.getProperty()).isEqualTo(this.idSearchProperty);
        assertThat(searchablePropertyCondition.getCondition()).isInstanceOf(Comparison.class);
        Comparison comparison = (Comparison) searchablePropertyCondition.getCondition();
        assertThat(comparison.getFieldName()).isEqualTo(ID_PROPERTY_NAME);
        assertThat(comparison.getOperator()).isEqualTo(Operator.GREATERTHANOREQUAL);
        assertThat(comparison.getValues()).containsOnly(ID1_VALUE);
    }

    @Test
    public void whereIdIn() throws InvalidValueException {
        SearchBuilderImpl<Example> builder = this.getTestInstance();
        builder.where(ID_PROPERTY_NAME).in(ID1_VALUE, ID2_VALUE, ID3_VALUE);

        // Business method
        builder.toFinder();

        // Asserts
        verify(this.searchDomain).finderFor(this.conditionsArgumentCaptor.capture());
        List<SearchablePropertyCondition> capturedSearchablePropertyConditions = this.conditionsArgumentCaptor.getValue();
        assertThat(capturedSearchablePropertyConditions).hasSize(1);
        SearchablePropertyCondition searchablePropertyCondition = capturedSearchablePropertyConditions.get(0);
        assertThat(searchablePropertyCondition.getProperty()).isEqualTo(this.idSearchProperty);
        assertThat(searchablePropertyCondition.getCondition()).isInstanceOf(Contains.class);
        Contains contains = (Contains) searchablePropertyCondition.getCondition();
        assertThat(contains.getFieldName()).isEqualTo(ID_PROPERTY_NAME);
        assertThat(contains.getOperator()).isEqualTo(ListOperator.IN);
        assertThat(contains.getCollection()).containsOnly(ID1_VALUE, ID2_VALUE, ID3_VALUE);
    }

    @Test
    public void whereIdInList() throws InvalidValueException {
        SearchBuilderImpl<Example> builder = this.getTestInstance();
        builder.where(ID_PROPERTY_NAME).in(Arrays.asList(ID1_VALUE, ID2_VALUE, ID3_VALUE));

        // Business method
        builder.toFinder();

        // Asserts
        verify(this.searchDomain).finderFor(this.conditionsArgumentCaptor.capture());
        List<SearchablePropertyCondition> capturedSearchablePropertyConditions = this.conditionsArgumentCaptor.getValue();
        assertThat(capturedSearchablePropertyConditions).hasSize(1);
        SearchablePropertyCondition searchablePropertyCondition = capturedSearchablePropertyConditions.get(0);
        assertThat(searchablePropertyCondition.getProperty()).isEqualTo(this.idSearchProperty);
        assertThat(searchablePropertyCondition.getCondition()).isInstanceOf(Contains.class);
        Contains contains = (Contains) searchablePropertyCondition.getCondition();
        assertThat(contains.getFieldName()).isEqualTo(ID_PROPERTY_NAME);
        assertThat(contains.getOperator()).isEqualTo(ListOperator.IN);
        assertThat(contains.getCollection()).containsOnly(ID1_VALUE, ID2_VALUE, ID3_VALUE);
    }

    @Test
    public void whereNameEqualTo() throws InvalidValueException {
        SearchBuilderImpl<Example> builder = this.getTestInstance();
        builder.where(NAME_PROPERTY_NAME).isEqualTo("Hello");

        // Business method
        builder.toFinder();

        // Asserts
        verify(this.searchDomain).finderFor(this.conditionsArgumentCaptor.capture());
        List<SearchablePropertyCondition> capturedSearchablePropertyConditions = this.conditionsArgumentCaptor.getValue();
        assertThat(capturedSearchablePropertyConditions).hasSize(1);
        SearchablePropertyCondition searchablePropertyCondition = capturedSearchablePropertyConditions.get(0);
        assertThat(searchablePropertyCondition.getProperty()).isEqualTo(this.nameSearchProperty);
        assertThat(searchablePropertyCondition.getCondition()).isInstanceOf(Comparison.class);
        Comparison comparison = (Comparison) searchablePropertyCondition.getCondition();
        assertThat(comparison.getFieldName()).isEqualTo(NAME_PROPERTY_NAME);
        assertThat(comparison.getOperator()).isEqualTo(Operator.EQUALIGNORECASE);
        assertThat(comparison.getValues()).containsOnly("Hello");
    }

    @Test
    public void whereNameEqualToIgnoreCase() throws InvalidValueException {
        SearchBuilderImpl<Example> builder = this.getTestInstance();
        builder.where(NAME_PROPERTY_NAME).isEqualToIgnoreCase("Hello");

        // Business method
        builder.toFinder();

        // Asserts
        verify(this.searchDomain).finderFor(this.conditionsArgumentCaptor.capture());
        List<SearchablePropertyCondition> capturedSearchablePropertyConditions = this.conditionsArgumentCaptor.getValue();
        assertThat(capturedSearchablePropertyConditions).hasSize(1);
        SearchablePropertyCondition searchablePropertyCondition = capturedSearchablePropertyConditions.get(0);
        assertThat(searchablePropertyCondition.getProperty()).isEqualTo(this.nameSearchProperty);
        assertThat(searchablePropertyCondition.getCondition()).isInstanceOf(Comparison.class);
        Comparison comparison = (Comparison) searchablePropertyCondition.getCondition();
        assertThat(comparison.getFieldName()).isEqualTo(NAME_PROPERTY_NAME);
        assertThat(comparison.getOperator()).isEqualTo(Operator.EQUALIGNORECASE);
    }

    @Test
    public void whereNameLike() throws InvalidValueException {
        SearchBuilderImpl<Example> builder = this.getTestInstance();
        builder.where(NAME_PROPERTY_NAME).like("Hello*");

        // Business method
        builder.toFinder();

        // Asserts
        verify(this.searchDomain).finderFor(this.conditionsArgumentCaptor.capture());
        List<SearchablePropertyCondition> capturedSearchablePropertyConditions = this.conditionsArgumentCaptor.getValue();
        assertThat(capturedSearchablePropertyConditions).hasSize(1);
        SearchablePropertyCondition searchablePropertyCondition = capturedSearchablePropertyConditions.get(0);
        assertThat(searchablePropertyCondition.getProperty()).isEqualTo(this.nameSearchProperty);
        assertThat(searchablePropertyCondition.getCondition()).isInstanceOf(Comparison.class);
        Comparison comparison = (Comparison) searchablePropertyCondition.getCondition();
        assertThat(comparison.getFieldName()).isEqualTo(NAME_PROPERTY_NAME);
        assertThat(comparison.getOperator()).isEqualTo(Operator.LIKE);
        assertThat(comparison.getValues()).containsOnly("Hello%");
    }

    @Test
    public void whereNameLikeIgnoreCase() throws InvalidValueException {
        SearchBuilderImpl<Example> builder = this.getTestInstance();
        builder.where(NAME_PROPERTY_NAME).likeIgnoreCase("Hello*");

        // Business method
        builder.toFinder();

        // Asserts
        verify(this.searchDomain).finderFor(this.conditionsArgumentCaptor.capture());
        List<SearchablePropertyCondition> capturedSearchablePropertyConditions = this.conditionsArgumentCaptor.getValue();
        assertThat(capturedSearchablePropertyConditions).hasSize(1);
        SearchablePropertyCondition searchablePropertyCondition = capturedSearchablePropertyConditions.get(0);
        assertThat(searchablePropertyCondition.getProperty()).isEqualTo(this.nameSearchProperty);
        assertThat(searchablePropertyCondition.getCondition()).isInstanceOf(Comparison.class);
        Comparison comparison = (Comparison) searchablePropertyCondition.getCondition();
        assertThat(comparison.getFieldName()).isEqualTo(NAME_PROPERTY_NAME);
        assertThat(comparison.getOperator()).isEqualTo(Operator.LIKEIGNORECASE);
    }

    @Test
    public void whereNameNotNullCase() throws InvalidValueException {
        SearchBuilderImpl<Example> builder = this.getTestInstance();
        builder.where(NAME_PROPERTY_NAME).isDefined();

        // Business method
        builder.toFinder();

        // Asserts
        verify(this.searchDomain).finderFor(this.conditionsArgumentCaptor.capture());
        List<SearchablePropertyCondition> capturedSearchablePropertyConditions = this.conditionsArgumentCaptor.getValue();
        assertThat(capturedSearchablePropertyConditions).hasSize(1);
        SearchablePropertyCondition searchablePropertyCondition = capturedSearchablePropertyConditions.get(0);
        assertThat(searchablePropertyCondition.getProperty()).isEqualTo(this.nameSearchProperty);
        assertThat(searchablePropertyCondition.getCondition()).isInstanceOf(Comparison.class);
        Comparison comparison = (Comparison) searchablePropertyCondition.getCondition();
        assertThat(comparison.getFieldName()).isEqualTo(NAME_PROPERTY_NAME);
        assertThat(comparison.getOperator()).isEqualTo(Operator.ISNOTNULL);
    }

    @Test
    public void whereNameNullCase() throws InvalidValueException {
        SearchBuilderImpl<Example> builder = this.getTestInstance();
        builder.where(NAME_PROPERTY_NAME).isNotDefined();

        // Business method
        builder.toFinder();

        // Asserts
        verify(this.searchDomain).finderFor(this.conditionsArgumentCaptor.capture());
        List<SearchablePropertyCondition> capturedSearchablePropertyConditions = this.conditionsArgumentCaptor.getValue();
        assertThat(capturedSearchablePropertyConditions).hasSize(1);
        SearchablePropertyCondition searchablePropertyCondition = capturedSearchablePropertyConditions.get(0);
        assertThat(searchablePropertyCondition.getProperty()).isEqualTo(this.nameSearchProperty);
        assertThat(searchablePropertyCondition.getCondition()).isInstanceOf(Comparison.class);
        Comparison comparison = (Comparison) searchablePropertyCondition.getCondition();
        assertThat(comparison.getFieldName()).isEqualTo(NAME_PROPERTY_NAME);
        assertThat(comparison.getOperator()).isEqualTo(Operator.ISNULL);
    }

    @Test
    public void whereIdEqualToAndNameLike() throws InvalidValueException {
        SearchBuilderImpl<Example> builder = this.getTestInstance();
        builder
            .where(ID_PROPERTY_NAME).isEqualTo(ID2_VALUE)
            .and(NAME_PROPERTY_NAME).like("Hello*");

        // Business method
        builder.toFinder();

        // Asserts
        verify(this.searchDomain).finderFor(this.conditionsArgumentCaptor.capture());
        List<SearchablePropertyCondition> capturedSearchablePropertyConditions = this.conditionsArgumentCaptor.getValue();
        assertThat(capturedSearchablePropertyConditions).hasSize(2);
    }

    @Test
    public void whereIdEqualToAndNameLikeUsingSearchableProperty() throws InvalidValueException {
        SearchBuilderImpl<Example> builder = this.getTestInstance();
        builder
            .where(this.idSearchProperty).isEqualTo(ID2_VALUE)
            .and(this.nameSearchProperty).like("Hello*");

        // Business method
        builder.toFinder();

        // Asserts
        verify(this.searchDomain).finderFor(this.conditionsArgumentCaptor.capture());
        List<SearchablePropertyCondition> capturedSearchablePropertyConditions = this.conditionsArgumentCaptor.getValue();
        assertThat(capturedSearchablePropertyConditions).hasSize(2);
    }

    @Test(expected = InvalidValueException.class)
    public void whereIdEqualToString() throws InvalidValueException {
        PropertySpec idPropertySpec = this.idSearchProperty.getSpecification();
        doThrow(new InvalidValueException("whatever", "don't care", "id")).when(idPropertySpec).validateValue(anyString());
        doThrow(new InvalidValueException("whatever", "don't care", "id")).when(idPropertySpec).validateValueIgnoreRequired(anyString());
        SearchBuilderImpl<Example> builder = this.getTestInstance();
        String valueOfWrongType = "wrong type";
        builder.where(ID_PROPERTY_NAME).isEqualTo(valueOfWrongType);

        // Business method
        builder.toFinder();

        // Asserts
        verify(this.searchDomain).finderFor(this.conditionsArgumentCaptor.capture());
        List<SearchablePropertyCondition> capturedSearchablePropertyConditions = this.conditionsArgumentCaptor.getValue();
        assertThat(capturedSearchablePropertyConditions).hasSize(1);
        SearchablePropertyCondition searchablePropertyCondition = capturedSearchablePropertyConditions.get(0);
        assertThat(searchablePropertyCondition.getProperty()).isEqualTo(this.idSearchProperty);
        assertThat(searchablePropertyCondition.getCondition()).isInstanceOf(Comparison.class);
        Comparison comparison = (Comparison) searchablePropertyCondition.getCondition();
        assertThat(comparison.getFieldName()).isEqualTo(ID_PROPERTY_NAME);
        assertThat(comparison.getOperator()).isEqualTo(Operator.EQUAL);
        assertThat(comparison.getValues()).containsOnly(ID1_VALUE);
    }

    @Test(expected = InvalidValueException.class)
    public void whereIdEqualToIgnoreCase() throws InvalidValueException {
        PropertySpec idPropertySpec = this.idSearchProperty.getSpecification();
        doThrow(new InvalidValueException("whatever", "don't care", "id")).when(idPropertySpec).validateValue(anyString());
        doThrow(new InvalidValueException("whatever", "don't care", "id")).when(idPropertySpec).validateValueIgnoreRequired(anyString());
        SearchBuilderImpl<Example> builder = this.getTestInstance();
        String valueOfWrongType = "wrong type";
        builder.where(ID_PROPERTY_NAME).isEqualToIgnoreCase(valueOfWrongType);

        // Business method
        builder.toFinder();

        // Asserts
        verify(this.searchDomain).finderFor(this.conditionsArgumentCaptor.capture());
        List<SearchablePropertyCondition> capturedSearchablePropertyConditions = this.conditionsArgumentCaptor.getValue();
        assertThat(capturedSearchablePropertyConditions).hasSize(1);
        SearchablePropertyCondition searchablePropertyCondition = capturedSearchablePropertyConditions.get(0);
        assertThat(searchablePropertyCondition.getProperty()).isEqualTo(this.idSearchProperty);
        assertThat(searchablePropertyCondition.getCondition()).isInstanceOf(Comparison.class);
        Comparison comparison = (Comparison) searchablePropertyCondition.getCondition();
        assertThat(comparison.getFieldName()).isEqualTo(ID_PROPERTY_NAME);
        assertThat(comparison.getOperator()).isEqualTo(Operator.EQUAL);
        assertThat(comparison.getValues()).containsOnly(ID1_VALUE);
    }

    @Test(expected = InvalidValueException.class)
    public void whereIdInListOfStrings() throws InvalidValueException {
        PropertySpec idPropertySpec = this.idSearchProperty.getSpecification();
        doThrow(new InvalidValueException("whatever", "don't care", "id")).when(idPropertySpec).validateValue(anyString());
        doThrow(new InvalidValueException("whatever", "don't care", "id")).when(idPropertySpec).validateValueIgnoreRequired(anyString());
        SearchBuilderImpl<Example> builder = this.getTestInstance();
        String s1 = "Hello";
        String s2 = "world";
        builder.where(ID_PROPERTY_NAME).in(s1, s2);

        // Business method
        builder.toFinder();

        // Asserts
        verify(this.searchDomain).finderFor(this.conditionsArgumentCaptor.capture());
        List<SearchablePropertyCondition> capturedSearchablePropertyConditions = this.conditionsArgumentCaptor.getValue();
        assertThat(capturedSearchablePropertyConditions).hasSize(1);
        SearchablePropertyCondition searchablePropertyCondition = capturedSearchablePropertyConditions.get(0);
        assertThat(searchablePropertyCondition.getProperty()).isEqualTo(this.idSearchProperty);
        assertThat(searchablePropertyCondition.getCondition()).isInstanceOf(Comparison.class);
        Comparison comparison = (Comparison) searchablePropertyCondition.getCondition();
        assertThat(comparison.getFieldName()).isEqualTo(ID_PROPERTY_NAME);
        assertThat(comparison.getOperator()).isEqualTo(Operator.EQUAL);
        assertThat(comparison.getValues()).containsOnly(ID1_VALUE);
    }

    @Test
    public void whereRefEqualToActualObject() throws InvalidValueException {
        SearchBuilderImpl<Example> builder = this.getTestInstance();
        Example example = new Example(ID1_VALUE, NAME_PROPERTY_NAME);
        builder.where(REF_PROPERTY_NAME).isEqualTo(example);

        // Business method
        builder.toFinder();

        // Asserts
        verify(this.searchDomain).finderFor(this.conditionsArgumentCaptor.capture());
        List<SearchablePropertyCondition> capturedSearchablePropertyConditions = this.conditionsArgumentCaptor.getValue();
        assertThat(capturedSearchablePropertyConditions).hasSize(1);
        SearchablePropertyCondition searchablePropertyCondition = capturedSearchablePropertyConditions.get(0);
        assertThat(searchablePropertyCondition.getProperty()).isEqualTo(this.referenceSearchProperty);
        assertThat(searchablePropertyCondition.getCondition()).isInstanceOf(Comparison.class);
        Comparison comparison = (Comparison) searchablePropertyCondition.getCondition();
        assertThat(comparison.getFieldName()).isEqualTo(REF_PROPERTY_NAME);
        assertThat(comparison.getOperator()).isEqualTo(Operator.EQUAL);
        assertThat(comparison.getValues()).containsOnly(example);
    }

    @Test
    public void whereRefEqualToActualObjectId() throws InvalidValueException {
        SearchBuilderImpl<Example> builder = this.getTestInstance();
        Example example = new Example(ID1_VALUE, NAME_PROPERTY_NAME);
        builder.where(REF_PROPERTY_NAME).isEqualTo(example.toString());

        // Business method
        builder.toFinder();

        // Asserts
        verify(this.searchDomain).finderFor(this.conditionsArgumentCaptor.capture());
        List<SearchablePropertyCondition> capturedSearchablePropertyConditions = this.conditionsArgumentCaptor.getValue();
        assertThat(capturedSearchablePropertyConditions).hasSize(1);
        SearchablePropertyCondition searchablePropertyCondition = capturedSearchablePropertyConditions.get(0);
        assertThat(searchablePropertyCondition.getProperty()).isEqualTo(this.referenceSearchProperty);
        assertThat(searchablePropertyCondition.getCondition()).isInstanceOf(Comparison.class);
        Comparison comparison = (Comparison) searchablePropertyCondition.getCondition();
        assertThat(comparison.getFieldName()).isEqualTo(REF_PROPERTY_NAME);
        assertThat(comparison.getOperator()).isEqualTo(Operator.EQUAL);
        assertThat(comparison.getValues()).containsOnly(example);
    }

    private SearchBuilderImpl<Example> getTestInstance() {
        return new SearchBuilderImpl<>(this.searchDomain, this.searchMonitor);
    }

    private static class Example {
        private final long id;
        private final String name;

        private Example(long id, String name) {
            super();
            this.id = id;
            this.name = name;
        }

        static Example from(String persistentValue) {
            String[] idAndName = persistentValue.split(";");
            if (idAndName.length < 2) {
                throw new IllegalArgumentException("Expecting string of format <id>;<name>");
            }
            return new Example(Long.parseLong(idAndName[0]), idAndName[1]);
        }

        @Override
        public String toString() {
            return this.id + ";" + this.name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Example example = (Example) o;

            return id == example.id;

        }

        @Override
        public int hashCode() {
            return (int) (id ^ (id >>> 32));
        }
    }

    private static class ExampleValueFactory implements ValueFactory<Example> {
        @Override
        public Example fromStringValue(String s) {
            return Example.from(s);
        }

        @Override
        public String toStringValue(Example example) {
            return example.toString();
        }

        @Override
        public Class<Example> getValueType() {
            return Example.class;
        }

        @Override
        public Example valueFromDatabase(Object o) {
            return (Example) o;
        }

        @Override
        public Object valueToDatabase(Example example) {
            return this.toStringValue(example);
        }

        @Override
        public void bind(PreparedStatement preparedStatement, int i, Example example) throws SQLException {
            preparedStatement.setString(i, this.toStringValue(example));
        }

        @Override
        public void bind(SqlBuilder sqlBuilder, Example example) {
            sqlBuilder.addObject(this.valueToDatabase(example));
        }

    }

}