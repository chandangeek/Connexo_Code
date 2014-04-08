package com.elster.jupiter.devtools;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * This helper class allows constructing a filter as it would be built by ExtJS
 * The String output from these methods can be added as QueryParam to a JerseyTest WebTarget
 */
public class ExtjsFilter {
    private static final String PROPERTY_VALUE_FORMAT = "{\"property\":\"%s\",\"value\":\"%s\"}";

    static public String filter(String property, String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(String.format("[" + PROPERTY_VALUE_FORMAT + "]", property, value), "UTF-8");
    }

    static public FilterBuilder filter() {
        return new FilterBuilderImpl();
    }


    public interface FilterBuilder {
        public FilterBuilder property(String property, String value);
        public String create() throws UnsupportedEncodingException;
    }


    private static class FilterBuilderImpl implements FilterBuilder {

        private final StringBuilder filter = new StringBuilder();
        private String separator = "";

        private FilterBuilderImpl() {
            filter.append('[');
        }

        @Override
        public FilterBuilder property(String property, String value) {
            filter.append(String.format("%s" + PROPERTY_VALUE_FORMAT, separator, property, value));
            separator=",";
            return this;
        }

        @Override
        public String create() throws UnsupportedEncodingException {
            filter.append(']');
            String encodedFilter = URLEncoder.encode(filter.toString(), "UTF-8");
            filter.delete(0,filter.length());
            return encodedFilter;
        }
    }
}
