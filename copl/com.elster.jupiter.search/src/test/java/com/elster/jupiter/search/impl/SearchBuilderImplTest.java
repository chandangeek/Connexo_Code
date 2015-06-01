package com.elster.jupiter.search.impl;

import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValuesImpl;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.search.SearchBuilder;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Operator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
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

    private static final String ID_PROPERTY_NAME = "id";
    private static final String NAME_PROPERTY_NAME = "name";
    private static final long ID1_VALUE = 97L;
    private static final long ID2_VALUE = 101L;
    private static final long ID3_VALUE = 103L;

    @Mock
    private SearchDomain searchDomain;
    @Mock
    private SearchableProperty idSearchProperty;
    @Mock
    private SearchableProperty nameSearchProperty;
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
        when(this.nameSearchProperty.hasName(NAME_PROPERTY_NAME)).thenReturn(true);
        when(this.searchDomain.getId()).thenReturn(Example.class.getName());
        when(this.searchDomain.supports(Example.class)).thenReturn(true);
        when(this.searchDomain.supports(Example.class)).thenReturn(true);
        when(this.searchDomain.getProperties()).thenReturn(Arrays.asList(this.idSearchProperty, this.nameSearchProperty));
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
    public void whereIdEqualTo() {
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
    public void whereIdIn() {
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
    public void whereIdInList() {
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
    public void whereNameEqualTo() {
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
        assertThat(comparison.getOperator()).isEqualTo(Operator.EQUAL);
        assertThat(comparison.getValues()).containsOnly("Hello");
    }

    @Test
    public void whereNameEqualToIgnoreCase() {
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
    public void whereNameLike() {
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
    public void whereNameLikeIgnoreCase() {
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
    public void whereIdEqualToAndNameLike() {
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
    public void whereIdEqualToAndNameLikeUsingSearchableProperty() {
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

    private SearchBuilderImpl<Example> getTestInstance() {
        return new SearchBuilderImpl<>(this.searchDomain);
    }

    private class Example {
        private String id;
        private String name;
    }

}