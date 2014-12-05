package com.elster.jupiter.rest.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Created by bvn on 9/9/14.
 */
public class JsonQueryFilterTest {

    public static final XmlAdapter<String, BigDecimal> BIG_DECIMAL_ADAPTER = new XmlAdapter<String, BigDecimal>() {
        @Override
        public BigDecimal unmarshal(String v) throws Exception {
            return BigDecimal.valueOf(Long.valueOf(v));
        }

        @Override
        public String marshal(BigDecimal v) throws Exception {
            return v.toString();
        }
    };

    @Test
    public void testJsonQueryFilterSingleValue() throws Exception {
        Map<Object, Object> hashMap1 = new HashMap<>();
        hashMap1.put("property", "name");
        hashMap1.put("value", "test");
        String string = new ObjectMapper().writer().writeValueAsString(new Object[]{hashMap1});
        JsonQueryFilter jsonQueryFilter = new JsonQueryFilter(string);
        assertThat(jsonQueryFilter.hasFilters()).isTrue();
        assertThat(jsonQueryFilter.getString("name")).isEqualTo("test");
    }

    @Test
    public void testJsonQueryFilterTwoObjects() throws Exception {
        Map<String, Object> hashMap1 = new HashMap<>();
        hashMap1.put("property", "name");
        hashMap1.put("value", "test");
        Map<String, Object> hashMap2 = new HashMap<>();
        hashMap2.put("property", "other");
        hashMap2.put("value", 10l);

        String string = new ObjectMapper().writer().writeValueAsString(new Object[]{hashMap1, hashMap2});
        JsonQueryFilter jsonQueryFilter = new JsonQueryFilter(string);

        assertThat(jsonQueryFilter.hasFilters()).isTrue();
        assertThat(jsonQueryFilter.hasProperty("name"));
        assertThat(jsonQueryFilter.hasProperty("other"));
        assertThat(jsonQueryFilter.getString("name")).isEqualTo("test");
        assertThat(jsonQueryFilter.getLong("other")).isEqualTo(10l);
    }

    @Test
    public void testJsonQueryFilterWithListValueOfIntegers() throws Exception {
        Map<String, Object> hashMap1 = new HashMap<>();
        hashMap1.put("property", "ids");
        hashMap1.put("value", Arrays.asList(1, 2, 3));

        String string = new ObjectMapper().writer().writeValueAsString(new Object[]{hashMap1});
        JsonQueryFilter jsonQueryFilter = new JsonQueryFilter(string);

        assertThat(jsonQueryFilter.hasFilters()).isTrue();
        assertThat(jsonQueryFilter.hasProperty("ids"));
        assertThat(jsonQueryFilter.getIntegerList("ids")).hasSize(3).containsExactly(1, 2, 3);
    }

   /* @Test
    public void testJsonQueryFilterWithListValueOfBigDecimals() throws Exception {
        Map<String, Object> hashMap1 = new HashMap<>();
        hashMap1.put("property", "ids");
        hashMap1.put("value", Arrays.asList(BigDecimal.ONE, BigDecimal.TEN));

        String string = new ObjectMapper().writer().writeValueAsString(new Object[]{hashMap1});
        JsonQueryFilter jsonQueryFilter = new JsonQueryFilter(string);

        assertThat(jsonQueryFilter.hasFilters()).isTrue();
        assertThat(jsonQueryFilter.hasProperty("ids"));
        assertThat(jsonQueryFilter.getPropertyList("ids", BIG_DECIMAL_ADAPTER)).hasSize(2).containsExactly(BigDecimal.ONE, BigDecimal.TEN);
    }*/
}
