package com.elster.jupiter.devtools;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * This helper class allows constructing a filter as it would be built by ExtJS
 */
public class ExtjsFilter {

    static public String filter(String property, String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(String.format("[{\"property\":\"%s\",\"value\":\"%s\"}]", property, value), "UTF-8");
    }

    static public FilterBuilder complexFilter() {
        return new FilterBuilderImpl();
    }


    interface FilterBuilder {
        FilterBuilder addProperty(String property, String value);
        String create() throws UnsupportedEncodingException;
    }


    private static class FilterBuilderImpl implements FilterBuilder {
        private final StringBuilder filter = new StringBuilder();

        private FilterBuilderImpl() {
            filter.append('[');
        }

        @Override
        public FilterBuilder addProperty(String property, String value) {
            filter.append(String.format("[{\"property\":\"%s\",\"value\":\"%s\"}]", property, value));
            return this;
        }

        @Override
        public String create() throws UnsupportedEncodingException {
            filter.append(']');
            return URLEncoder.encode(filter.toString(), "UTF-8");
        }
    }
}
