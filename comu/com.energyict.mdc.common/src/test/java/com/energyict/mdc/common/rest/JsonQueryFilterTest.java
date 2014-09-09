package com.energyict.mdc.common.rest;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Created by bvn on 9/9/14.
 */
public class JsonQueryFilterTest {

    public static final XmlAdapter<String, String> STRING_ADAPTER = new XmlAdapter<String, String>() {
        @Override
        public String unmarshal(String v) throws Exception {
            return v;
        }

        @Override
        public String marshal(String v) throws Exception {
            return v;
        }
    };

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

    public static final XmlAdapter<Object, BigDecimal> BIG_DECIMAL_LIST_ADAPTER = new XmlAdapter<Object, BigDecimal>() {
        @Override
        public BigDecimal unmarshal(Object v) throws Exception {
            return BigDecimal.valueOf(Long.valueOf((String) v));
        }

        @Override
        public String marshal(BigDecimal v) throws Exception {
            return v.toString();
        }
    };

    public static final XmlAdapter<String, Integer> INTEGER_ADAPTER = new XmlAdapter<String, Integer>() {
        @Override
        public Integer unmarshal(String v) throws Exception {
            return Integer.parseInt(v);
        }

        @Override
        public String marshal(Integer v) throws Exception {
            return v.toString();
        }
    };

    @Test
    public void testJsonQueryFilterSingleValue() throws Exception {
        Map<String, Object> hashMap1 = new HashMap<>();
        hashMap1.put("property", "name");
        hashMap1.put("value", "test");

        JSONArray jsonArray = new JSONArray(Arrays.asList(new JSONObject(hashMap1)));
        JsonQueryFilter jsonQueryFilter = new JsonQueryFilter(jsonArray);
        Map<String, ?> filterProperties = jsonQueryFilter.getFilterProperties();
        assertThat(filterProperties).hasSize(1).containsKey("name");
        assertThat(jsonQueryFilter.getProperty("name", STRING_ADAPTER)).isEqualTo("test");
    }

    @Test
    public void testJsonQueryFilterTwoObjects() throws Exception {
        Map<String, Object> hashMap1 = new HashMap<>();
        hashMap1.put("property", "name");
        hashMap1.put("value", "test");
        Map<String, Object> hashMap2 = new HashMap<>();
        hashMap2.put("property", "other");
        hashMap2.put("value", BigDecimal.TEN);

        JSONArray jsonArray = new JSONArray(Arrays.asList(new JSONObject(hashMap1), new JSONObject(hashMap2)));
        JsonQueryFilter jsonQueryFilter = new JsonQueryFilter(jsonArray);
        Map<String, ?> filterProperties = jsonQueryFilter.getFilterProperties();
        assertThat(filterProperties).hasSize(2).containsKey("name").containsKey("other");
        assertThat(jsonQueryFilter.getProperty("name")).isEqualTo("test");
        assertThat(jsonQueryFilter.getProperty("other")).isEqualTo(BigDecimal.TEN);
    }

    @Test
    public void testJsonQueryFilterWithListValueOfIntegers() throws Exception {
        Map<String, Object> hashMap1 = new HashMap<>();
        hashMap1.put("property", "ids");
        hashMap1.put("value", new JSONArray(Arrays.asList(1,2,3)));

        JSONArray jsonArray = new JSONArray(Arrays.asList(new JSONObject(hashMap1)));
        JsonQueryFilter jsonQueryFilter = new JsonQueryFilter(jsonArray);
        Map<String, ?> filterProperties = jsonQueryFilter.getFilterProperties();
        assertThat(filterProperties).hasSize(1).containsKey("ids");
        assertThat(jsonQueryFilter.getPropertyList("ids", INTEGER_ADAPTER)).hasSize(3).containsExactly(1, 2, 3);
    }

    @Test
    public void testJsonQueryFilterWithListValueOfBigDecimals() throws Exception {
        Map<String, Object> hashMap1 = new HashMap<>();
        hashMap1.put("property", "ids");
        hashMap1.put("value", new JSONArray(Arrays.asList(BigDecimal.ONE, BigDecimal.TEN)));

        JSONArray jsonArray = new JSONArray(Arrays.asList(new JSONObject(hashMap1)));
        JsonQueryFilter jsonQueryFilter = new JsonQueryFilter(jsonArray);
        Map<String, ?> filterProperties = jsonQueryFilter.getFilterProperties();
        assertThat(filterProperties).hasSize(1).containsKey("ids");
        assertThat(jsonQueryFilter.getPropertyList("ids", BIG_DECIMAL_ADAPTER)).hasSize(2).containsExactly(BigDecimal.ONE, BigDecimal.TEN);
    }
}
