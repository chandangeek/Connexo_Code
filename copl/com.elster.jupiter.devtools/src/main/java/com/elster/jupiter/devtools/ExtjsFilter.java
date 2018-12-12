/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.devtools;

import com.google.common.base.Joiner;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * This helper class allows constructing a filter as it would be built by ExtJS
 * The String output from these methods can be added as QueryParam to a JerseyTest WebTarget
 * Example:
 *   target("/schedules/1/comTasks").queryParam("filter", ExtjsFilter.filter().property("available", "true").create()).request().get();
 */
public class ExtjsFilter {
    private static final String PROPERTY_STRING_VALUE_FORMAT = "{\"property\":\"%s\",\"value\":\"%s\"}";
    private static final String PROPERTY_NUMBER_VALUE_FORMAT = "{\"property\":\"%s\",\"value\":%d}";
    private static final String PROPERTY_LIST_VALUE_FORMAT = "{\"property\":\"%s\",\"value\":%s}";

    static public String filter(String property, String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(String.format("[" + PROPERTY_STRING_VALUE_FORMAT + "]", property, value), "UTF-8");
    }

    static public String filter(String property, Long value) throws UnsupportedEncodingException {
        return URLEncoder.encode(String.format("[" + PROPERTY_NUMBER_VALUE_FORMAT + "]", property, value), "UTF-8");
    }

    static public String filter(String property, List<?> list) throws UnsupportedEncodingException {
        if (list.get(0) instanceof Number) {
            return URLEncoder.encode(String.format("[" + PROPERTY_LIST_VALUE_FORMAT + "]", property, "[" + Joiner.on(",").join(list) + "]"), "UTF-8");
        }
        return URLEncoder.encode(String.format("[" + PROPERTY_LIST_VALUE_FORMAT + "]", property, "[\"" + Joiner.on("\",\"").join(list) + "\"]"), "UTF-8");
    }

    static public FilterBuilder filter() {
        return new FilterBuilderImpl();
    }


    public interface FilterBuilder {
        public FilterBuilder property(String property, String value);
        public FilterBuilder property(String property, Long value);
        public FilterBuilder property(String property, List<?> list);

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
            filter.append(String.format("%s" + PROPERTY_STRING_VALUE_FORMAT, separator, property, value));
            separator=",";
            return this;
        }

        @Override
        public FilterBuilder property(String property, Long value) {
            filter.append(String.format("%s" + PROPERTY_NUMBER_VALUE_FORMAT, separator, property, value));
            separator=",";
            return this;
        }

        @Override
        public FilterBuilder property(String property, List<?> list) {
            if (list.get(0) instanceof Number) {
                filter.append(String.format("%s" + PROPERTY_LIST_VALUE_FORMAT, separator, property, "[" + Joiner.on(",").join(list) + "]"));
            } else {
                filter.append(String.format("%s" + PROPERTY_LIST_VALUE_FORMAT, separator, property, "[\"" + Joiner.on("\",\"").join(list) + "\"]"));
            }

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
