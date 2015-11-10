package com.elster.jupiter.search.rest;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyOperator;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.search.rest.SearchablePropertyValueConverter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class SearchablePropertyValueConverterTest {

    @Mock
    SearchableProperty propertySpec;

    private SearchablePropertyValueConverter testInstance(){
        return new SearchablePropertyValueConverter();
    }

    @Test
    public void testApplyForNullNode(){
        assertThat(testInstance().apply(null)).isNull();
    }

    @Test(expected = LocalizedFieldValidationException.class)
    public void testApplyForNodeWithoutOperatorAndCriteria(){
        LongNode someNode = LongNode.valueOf(42L);
        testInstance().apply(someNode);
    }

    @Test(expected = LocalizedFieldValidationException.class)
    public void testApplyForNodeWithoutOperator(){
        ObjectNode node = new ObjectNode(null);
        node.set(SearchablePropertyValueConverter.OPERATOR_FIELD, TextNode.valueOf(SearchablePropertyOperator.GREATER_THAN.code()));
        testInstance().apply(node);
    }

    @Test(expected = LocalizedFieldValidationException.class)
    public void testApplyForNodeWithoutCriteria(){
        ObjectNode node = new ObjectNode(null);
        node.set(SearchablePropertyValueConverter.CRITERIA_FIELD, TextNode.valueOf("*"));
        testInstance().apply(node);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testApplyForNodeWithUnknownOperator(){
        ObjectNode node = new ObjectNode(null);
        node.set(SearchablePropertyValueConverter.OPERATOR_FIELD, TextNode.valueOf("++"));
        testInstance().apply(node);
    }

    @Test
    public void testApplyForSingleStringCriteria(){
        ObjectNode node = new ObjectNode(null);
        node.set(SearchablePropertyValueConverter.OPERATOR_FIELD, TextNode.valueOf(SearchablePropertyOperator.GREATER_THAN.code()));
        String userInput = "Some name *";
        node.set(SearchablePropertyValueConverter.CRITERIA_FIELD, TextNode.valueOf(userInput));

        SearchablePropertyValue.ValueBean propertyValue = testInstance().apply(node);
        assertThat(propertyValue.operator).isEqualTo(SearchablePropertyOperator.GREATER_THAN);
        assertThat(propertyValue.values).hasSize(1);
        assertThat(propertyValue.values).containsExactly(userInput);
    }

    @Test
    public void testApplyForSingleLongCriteria(){
        ObjectNode node = new ObjectNode(null);
        node.set(SearchablePropertyValueConverter.OPERATOR_FIELD, TextNode.valueOf(SearchablePropertyOperator.GREATER_THAN.code()));
        node.set(SearchablePropertyValueConverter.CRITERIA_FIELD, LongNode.valueOf(42L));

        SearchablePropertyValue.ValueBean propertyValue = testInstance().apply(node);
        assertThat(propertyValue.operator).isEqualTo(SearchablePropertyOperator.GREATER_THAN);
        assertThat(propertyValue.values).hasSize(1);
        assertThat(propertyValue.values).containsExactly("42");
    }

    @Test
    public void testApplyForSingleComplexCriteria(){
        ObjectNode complexCriteria = new ObjectNode(null);
        complexCriteria.set("answer", TextNode.valueOf("42!"));
        ObjectNode node = new ObjectNode(null);
        node.set(SearchablePropertyValueConverter.OPERATOR_FIELD, TextNode.valueOf(SearchablePropertyOperator.GREATER_THAN.code()));
        node.set(SearchablePropertyValueConverter.CRITERIA_FIELD, complexCriteria);

        SearchablePropertyValue.ValueBean propertyValue = testInstance().apply(node);
        assertThat(propertyValue.operator).isEqualTo(SearchablePropertyOperator.GREATER_THAN);
        assertThat(propertyValue.values).hasSize(1);
        assertThat((String) propertyValue.values.get(0)).contains("answer", "42!");
    }

    @Test
    public void testApplyForListStringCriteria(){
        ObjectNode node = new ObjectNode(null);
        node.set(SearchablePropertyValueConverter.OPERATOR_FIELD, TextNode.valueOf(SearchablePropertyOperator.GREATER_THAN.code()));
        String userInput1 = "Some name";
        String userInput2 = "Another name";
        ArrayNode criteriaNode = new ArrayNode(null);
        criteriaNode.addAll(Arrays.asList(TextNode.valueOf(userInput1), TextNode.valueOf(userInput2)));
        node.set(SearchablePropertyValueConverter.CRITERIA_FIELD, criteriaNode);

        SearchablePropertyValue.ValueBean propertyValue = testInstance().apply(node);
        assertThat(propertyValue.operator).isEqualTo(SearchablePropertyOperator.GREATER_THAN);
        assertThat(propertyValue.values).hasSize(2);
        assertThat(propertyValue.values).containsExactly(userInput1, userInput2);
    }

    @Test
    public void testApplyForListLongCriteria(){
        ObjectNode node = new ObjectNode(null);
        node.set(SearchablePropertyValueConverter.OPERATOR_FIELD, TextNode.valueOf(SearchablePropertyOperator.GREATER_THAN.code()));
        ArrayNode criteriaNode = new ArrayNode(null);
        criteriaNode.addAll(Arrays.asList(LongNode.valueOf(42L), IntNode.valueOf(17)));
        node.set(SearchablePropertyValueConverter.CRITERIA_FIELD, criteriaNode);

        SearchablePropertyValue.ValueBean propertyValue = testInstance().apply(node);
        assertThat(propertyValue.operator).isEqualTo(SearchablePropertyOperator.GREATER_THAN);
        assertThat(propertyValue.values).hasSize(2);
        assertThat(propertyValue.values).containsExactly("42", "17");
    }

    @Test
    public void testApplyForListComplexCriteria(){
        ObjectNode complexInput1 = new ObjectNode(null);
        complexInput1.set("answer", TextNode.valueOf("42!"));
        ObjectNode complexInput2 = new ObjectNode(null);
        complexInput1.set("bla-bla", IntNode.valueOf(17));
        ArrayNode criteriaNode = new ArrayNode(null);
        criteriaNode.addAll(Arrays.asList(complexInput1, complexInput2));
        ObjectNode node = new ObjectNode(null);
        node.set(SearchablePropertyValueConverter.OPERATOR_FIELD, TextNode.valueOf(SearchablePropertyOperator.GREATER_THAN.code()));
        node.set(SearchablePropertyValueConverter.CRITERIA_FIELD, criteriaNode);

        SearchablePropertyValue.ValueBean propertyValue = testInstance().apply(node);
        assertThat(propertyValue.operator).isEqualTo(SearchablePropertyOperator.GREATER_THAN);
        assertThat(propertyValue.values).hasSize(2);
        assertThat((String) propertyValue.values.get(0)).contains("answer", "42!");
        assertThat((String) propertyValue.values.get(0)).contains("bla-bla", "17");
    }
}
