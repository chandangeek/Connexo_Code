package com.elster.jupiter.rest.util.impl;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.rest.util.ConstraintViolationExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedExceptionMapper;
import com.google.common.collect.ImmutableSet;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Application;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonQueryFilterTest extends FelixRestApplicationJerseyTest {

    public static final String FILTER_PARAM = "filter";

    public static final class JsonFilterProperty {

        JsonFilterProperty(String property, Object value) {
            this.property = property;
            this.value = value;
        }

        public String property;
        public Object value;
    }

    @Override
    protected Application getApplication() {
        return new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return ImmutableSet.of(
                        JsonQueryFilterResource.class,
                        ConstraintViolationExceptionMapper.class,
                        LocalizedExceptionMapper.class);
            }
        };
    }

    private String getFilterFromValue(Object value) {
        return URLEncoder.encode(JsonModel.model(Collections.singletonList(new JsonFilterProperty(JsonQueryFilterResource.DEFAUL_FILTER_NAME, value))).toJson());
    }

    @Test
    public void testSingleStringNonEmptyInput() {
        String response = target("/filters/single/string").queryParam(FILTER_PARAM, getFilterFromValue("non-empty")).request().get(String.class);
        assertThat(response).isEqualTo("non-empty");
    }
    @Test
    public void testSingleStringEmptyInput() {
        String response = target("/filters/single/string").queryParam(FILTER_PARAM, getFilterFromValue("")).request().get(String.class);
        assertThat(response).isEqualTo("");
    }
    @Test
    public void testSingleStringNullInput() {
        String response = target("/filters/single/string").queryParam(FILTER_PARAM, getFilterFromValue(null)).request().get(String.class);
        assertThat(response).isEqualTo("");
    }
    @Test
    public void testSingleStringLongInput() {
        String response = target("/filters/single/string").queryParam(FILTER_PARAM, getFilterFromValue(4L)).request().get(String.class);
        assertThat(response).isEqualTo("");
    }

    @Test
    public void testListStringEmptyListInput() {
        List<String> response = target("/filters/list/string").queryParam(FILTER_PARAM, getFilterFromValue(Collections.emptyList())).request().get(List.class);
        assertThat(response.size()).isEqualTo(0);
    }
    @Test
    public void testListStringEmptyArrayInput() {
        List<String> response = target("/filters/list/string").queryParam(FILTER_PARAM, getFilterFromValue(new String[]{})).request().get(List.class);
        assertThat(response.size()).isEqualTo(0);
    }
    @Test
    public void testListStringOneElementInArrayInput() {
        List<String> response = target("/filters/list/string").queryParam(FILTER_PARAM, getFilterFromValue(new String[]{"one"})).request().get(List.class);
        assertThat(response.size()).isEqualTo(1);
        assertThat(response.get(0)).isEqualTo("one");
    }
    @Test
    public void testListStringTwoElementsInArrayInput() {
        List<String> response = target("/filters/list/string").queryParam(FILTER_PARAM, getFilterFromValue(Arrays.asList("one", "two"))).request().get(List.class);
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.get(1)).isEqualTo("two");
    }
    @Test
    public void testListStringNullInput() {
        List<String> response = target("/filters/list/string").queryParam(FILTER_PARAM, getFilterFromValue(null)).request().get(List.class);
        assertThat(response.size()).isEqualTo(0);
    }
    @Test
    public void testListStringLongListAsInput() {
        List<String> response = target("/filters/list/string").queryParam(FILTER_PARAM, getFilterFromValue(Collections.singletonList(4L))).request().get(List.class);
        assertThat(response.size()).isEqualTo(1);
        assertThat(response.get(0)).isEqualTo(null);
    }

    @Test
    public void testSingleIntegerIntegerAsInput() {
        Integer response = target("/filters/single/integer").queryParam(FILTER_PARAM, getFilterFromValue(4)).request().get(Integer.class);
        assertThat(response).isEqualTo(4);
    }

    @Test
    public void testSingleIntegerLongAsInput() {
        Integer response = target("/filters/single/integer").queryParam(FILTER_PARAM, getFilterFromValue(4L)).request().get(Integer.class);
        assertThat(response).isEqualTo(4);
    }

    @Test
    public void testSingleIntegerNullAsInput() {
        Integer response = target("/filters/single/integer").queryParam(FILTER_PARAM, getFilterFromValue(null)).request().get(Integer.class);
        assertThat(response).isEqualTo(null);
    }

    @Test
    public void testSingleIntegerNoExceptionWhenOverflow() {
        // No exception when overflow
        Integer response = target("/filters/single/integer").queryParam(FILTER_PARAM, getFilterFromValue(Long.MAX_VALUE)).request().get(Integer.class);
    }

    @Test
    public void testSingleInteger() {
        Integer response = target("/filters/single/integer").queryParam(FILTER_PARAM, getFilterFromValue("string")).request().get(Integer.class);
        assertThat(response).isEqualTo(null);
    }

    @Test
    public void testListIntegerEmptyListAsInput() {
        List<Integer> response = target("/filters/list/integer").queryParam(FILTER_PARAM, getFilterFromValue(Collections.emptyList())).request().get(List.class);
        assertThat(response.size()).isEqualTo(0);
    }
    @Test
    public void testListIntegerEmptyArrayAsInput() {
        List<Integer> response = target("/filters/list/integer").queryParam(FILTER_PARAM, getFilterFromValue(new Integer[]{})).request().get(List.class);
        assertThat(response.size()).isEqualTo(0);
    }
    @Test
    public void testListIntegerOneElementInArrayInput() {
        List<Integer> response = target("/filters/list/integer").queryParam(FILTER_PARAM, getFilterFromValue(new Integer[]{4})).request().get(List.class);
        assertThat(response.size()).isEqualTo(1);
        assertThat(response.get(0)).isEqualTo(4);
    }
    @Test
    public void testListIntegerTwoElementsInArrayInput() {
        List<Integer> response = target("/filters/list/integer").queryParam(FILTER_PARAM, getFilterFromValue(Arrays.asList(4, 20))).request().get(List.class);
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.get(1)).isEqualTo(20);
    }
    @Test
    public void testListIntegerNullAsInput() {
        List<Integer> response = target("/filters/list/integer").queryParam(FILTER_PARAM, getFilterFromValue(null)).request().get(List.class);
        assertThat(response.size()).isEqualTo(0);
    }
    @Test
    public void testListIntegerNoExceptionWhenPverflow() {
        // No exception when overflow
        List<Integer>response = target("/filters/list/integer").queryParam(FILTER_PARAM, getFilterFromValue(new Long[]{Long.MAX_VALUE})).request().get(List.class);
    }
    @Test
    public void testListIntegerStringInArrayAsInput() {
        List<Integer> response = target("/filters/list/integer").queryParam(FILTER_PARAM, getFilterFromValue(Collections.singletonList("20"))).request().get(List.class);
        assertThat(response.size()).isEqualTo(1);
        assertThat(response.get(0)).isEqualTo(null);
    }

    @Test
    public void testSingleLongIntegerAsInput() {
        Long response = target("/filters/single/long").queryParam(FILTER_PARAM, getFilterFromValue(4)).request().get(Long.class);
        assertThat(response).isEqualTo(4L);
    }

    @Test
    public void testSingleLongLongAsInput() {
        Long response = target("/filters/single/long").queryParam(FILTER_PARAM, getFilterFromValue(4L)).request().get(Long.class);
        assertThat(response).isEqualTo(4L);
    }

    @Test
    public void testSingleLongNullAsInput() {
        Long response = target("/filters/single/long").queryParam(FILTER_PARAM, getFilterFromValue(null)).request().get(Long.class);
        assertThat(response).isEqualTo(null);
    }

    @Test
    public void testSingleLongNoExceptionWhenOverflow() {
        // No exception when overflow
        Long response = target("/filters/single/long").queryParam(FILTER_PARAM, getFilterFromValue(Double.MAX_VALUE)).request().get(Long.class);
    }

    @Test
    public void testSingleLongStringAsInput() {
        Long response = target("/filters/single/long").queryParam(FILTER_PARAM, getFilterFromValue("string")).request().get(Long.class);
        assertThat(response).isEqualTo(null);
    }

    @Test
    public void testListLongEmptyListAsInput() {
        List<Number> response = target("/filters/list/long").queryParam(FILTER_PARAM, getFilterFromValue(Collections.emptyList())).request().get(List.class);
        assertThat(response.size()).isEqualTo(0);
    }

    @Test
    public void testListLongEmptyArrayAsInput() {
        List<Number> response = target("/filters/list/long").queryParam(FILTER_PARAM, getFilterFromValue(new Long[]{})).request().get(List.class);
        assertThat(response.size()).isEqualTo(0);
    }

    @Test
    public void testListLongIntegerArrayAsInput() {
        List<Number> response = target("/filters/list/long").queryParam(FILTER_PARAM, getFilterFromValue(new Integer[]{4})).request().get(List.class);
        assertThat(response.size()).isEqualTo(1);
        assertThat(response.get(0)).isEqualTo(4);
    }

    @Test
    public void testListLongTwoElementsInArrayInput() {
        List<Number> response = target("/filters/list/long").queryParam(FILTER_PARAM, getFilterFromValue(Arrays.asList(4L, 20L))).request().get(List.class);
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.get(1)).isEqualTo(20);
    }

    @Test
    public void testListLongNullAsInput() {
        List<Number> response = target("/filters/list/long").queryParam(FILTER_PARAM, getFilterFromValue(null)).request().get(List.class);
        assertThat(response.size()).isEqualTo(0);
    }

    @Test
    public void testListLongNoExceptionWhenOverflow() {
        // No exception when overflow
        List<Number> response = target("/filters/list/long").queryParam(FILTER_PARAM, getFilterFromValue(new Double[]{Double.MAX_VALUE})).request().get(List.class);
    }

    @Test
    public void testListLongStringInArrayInput() {
        List<Number> response = target("/filters/list/long").queryParam(FILTER_PARAM, getFilterFromValue(Collections.singletonList("20"))).request().get(List.class);
        assertThat(response.size()).isEqualTo(1);
        assertThat(response.get(0)).isEqualTo(null);
    }

    @Test
    public void testSingleInstantLongMaxAsInput() {
        Instant response = target("/filters/single/instant").queryParam(FILTER_PARAM, getFilterFromValue(Long.MAX_VALUE)).request().get(Instant.class);
        assertThat(response).isEqualTo(Instant.ofEpochMilli(Long.MAX_VALUE));
    }

    @Test
    public void testSingleInstantLongAsInput() {
        Instant response = target("/filters/single/instant").queryParam(FILTER_PARAM, getFilterFromValue(10L)).request().get(Instant.class);
        assertThat(response).isEqualTo(Instant.ofEpochMilli(10L));
    }

    @Test
    public void testSingleInstantNullAsInput() {
        Instant response = target("/filters/single/instant").queryParam(FILTER_PARAM, getFilterFromValue(null)).request().get(Instant.class);
        assertThat(response).isEqualTo(null);
    }

    @Test
    public void testSingleInstantStringAsInput() {
        Instant response = target("/filters/single/instant").queryParam(FILTER_PARAM, getFilterFromValue("string")).request().get(Instant.class);
        assertThat(response).isEqualTo(null);
    }

    @Test
    public void testListInstantEmptyListAsInput() {
        List<Instant> response = target("/filters/list/instant").queryParam(FILTER_PARAM, getFilterFromValue(Collections.emptyList())).request().get(List.class);
        assertThat(response.size()).isEqualTo(0);
    }

    @Test
    public void testListInstantEmptyArrayAsInput() {
        List<Instant> response = target("/filters/list/instant").queryParam(FILTER_PARAM, getFilterFromValue(new Long[]{})).request().get(List.class);
        assertThat(response.size()).isEqualTo(0);
    }

    @Test
    public void testListInstantOneElementInArrayInput() {
        List<Instant> response = target("/filters/list/instant").queryParam(FILTER_PARAM, getFilterFromValue(new Long[]{10L})).request().get(List.class);
        assertThat(response.size()).isEqualTo(1);
    }

    @Test
    public void testListInstantTwoElementsInArrayInput() {
        List<Instant> response = target("/filters/list/instant").queryParam(FILTER_PARAM, getFilterFromValue(Arrays.asList(4L, 20L))).request().get(List.class);
        assertThat(response.size()).isEqualTo(2);
    }

    @Test
    public void testListInstantNullAsInput() {
        List<Instant> response = target("/filters/list/instant").queryParam(FILTER_PARAM, getFilterFromValue(null)).request().get(List.class);
        assertThat(response.size()).isEqualTo(0);
    }

    @Test
    public void testListInstantStringInArrayInput() {
        List<Instant> response =  target("/filters/list/instant").queryParam(FILTER_PARAM, getFilterFromValue(Collections.singletonList("20"))).request().get(List.class);
        assertThat(response.size()).isEqualTo(1);
        assertThat(response.get(0)).isEqualTo(null);
    }

    @Test
    public void testSingleBooleanCorrectStringValueAsInput() {
        Boolean response = target("/filters/single/boolean").queryParam(FILTER_PARAM, getFilterFromValue("true")).request().get(Boolean.class);
        assertThat(response).isTrue();
    }

    @Test
    public void testSingleBooleanTrueAsInput() {
        Boolean response = target("/filters/single/boolean").queryParam(FILTER_PARAM, getFilterFromValue(true)).request().get(Boolean.class);
        assertThat(response).isTrue();
    }

    @Test
    public void testSingleBooleanFalseObjectAsInput() {
        Boolean response = target("/filters/single/boolean").queryParam(FILTER_PARAM, getFilterFromValue(Boolean.FALSE)).request().get(Boolean.class);
        assertThat(response).isFalse();
    }

    @Test
    public void testSingleBooleanNullAsInput() {
        Boolean response = target("/filters/single/boolean").queryParam(FILTER_PARAM, getFilterFromValue(null)).request().get(Boolean.class);
        assertThat(response).isEqualTo(false);
    }

    @Test
    public void testSingleBooleanLongAsInput() {
        Boolean response = target("/filters/single/boolean").queryParam(FILTER_PARAM, getFilterFromValue(4L)).request().get(Boolean.class);
        assertThat(response).isEqualTo(true);
    }

    @Test
    public void testSingleBooleanStringValueAsInput() {
        Boolean response = target("/filters/single/boolean").queryParam(FILTER_PARAM, getFilterFromValue("some-string")).request().get(Boolean.class);
        assertThat(response).isFalse();
    }
    @Test
    public void testListBooleanEmptyListAsInput() {
        List<Boolean> response = target("/filters/list/boolean").queryParam(FILTER_PARAM, getFilterFromValue(Collections.emptyList())).request().get(List.class);
        assertThat(response.size()).isEqualTo(0);
    }
    @Test
    public void testListBooleanEmptyArrayAsBoolean() {
        List<Boolean> response = target("/filters/list/boolean").queryParam(FILTER_PARAM, getFilterFromValue(new boolean[]{})).request().get(List.class);
        assertThat(response.size()).isEqualTo(0);
    }
    @Test
    public void testListBooleanOneElementInArrayInput() {
        List<Boolean>response = target("/filters/list/boolean").queryParam(FILTER_PARAM, getFilterFromValue(new boolean[]{true})).request().get(List.class);
        assertThat(response.size()).isEqualTo(1);
        assertThat(response.get(0)).isTrue();
    }
    @Test
    public void testListBooleanCorrectStringInArrayInput() {
        List<Boolean> response = target("/filters/list/boolean").queryParam(FILTER_PARAM, getFilterFromValue(new String[]{"true"})).request().get(List.class);
        assertThat(response.size()).isEqualTo(1);
        assertThat(response.get(0)).isTrue();
    }
    @Test
    public void testListBooleanTwoElementsInArrayInput() {
        List<Boolean> response = target("/filters/list/boolean").queryParam(FILTER_PARAM, getFilterFromValue(Arrays.asList(false, Boolean.TRUE))).request().get(List.class);
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.get(1)).isTrue();
    }
    @Test
    public void testListBooleanNullAsInput() {
        List<Boolean> response = target("/filters/list/boolean").queryParam(FILTER_PARAM, getFilterFromValue(null)).request().get(List.class);
        assertThat(response.size()).isEqualTo(0);
    }
    @Test
    public void testListBooleanLongValueInArrayInput() {
        List<Boolean> response =  target("/filters/list/boolean").queryParam(FILTER_PARAM, getFilterFromValue(Collections.singletonList(20L))).request().get(List.class);
        assertThat(response.size()).isEqualTo(1);
        assertThat(response.get(0)).isTrue();
    }

    @Test
    public void testNoExceptionForUnexisting() {
        assertThat(target("/filters/single/integer").request().get(Integer.class)).isEqualTo(null);
        assertThat(target("/filters/list/integer").request().get(List.class).size()).isZero();
    }

    @Test
    public void testNoExceptionForIncorrectFilterValue() {
        assertThat(target("/filters/single/integer").queryParam(FILTER_PARAM, 20L).request().get(Integer.class)).isEqualTo(null);
    }

    @Test
    public void testComplexObject() {
        String response = target("/filters/single/complex").queryParam(FILTER_PARAM, getFilterFromValue(new JsonFilterProperty("complex", "true"))).request().get(String.class);
        assertThat(response).isEqualTo("{\"property\":\"complex\",\"value\":\"true\"}");
    }

    @Test
    public void testStringForAdapaterObject() {
        Integer response = target("/filters/single/adapter").queryParam(FILTER_PARAM, getFilterFromValue("125_68")).request().get(Integer.class);
        assertThat(response).isEqualTo(125068);
    }

    @Test
    public void testListStringForAdapaterObject() {
        List<Integer> response = target("/filters/list/adapter").queryParam(FILTER_PARAM, getFilterFromValue(Collections.singletonList("125_68"))).request().get(List.class);
        assertThat(response).hasSize(1);
        assertThat(response.get(0)).isEqualTo(125068);
    }
}
