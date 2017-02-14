/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import com.elster.jupiter.nls.Thesaurus;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Generic resource that lists all possible values for a certain field. Used in frontend to fill combo-boxes
 * <p>
 * Why the wrapped return value? JavaScript people didn't want to see a naked JSON list, had to be
 * wrapped with meaningful field name.
 */

@Path("/field")
public abstract class FieldResource {

    private final Thesaurus thesaurus;

    public FieldResource(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    /**
     * This method will return a JSON list of all available field descriptions in this resource
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Object getAllFields() {
        final List<Object> allFields = new ArrayList<>();
        for (Method method : this.getClass().getMethods()) {
            if (method.isAnnotationPresent(Path.class)) {
                Path annotation = method.getAnnotation(Path.class);
                final String path = annotation.value();
                if (path.length() > 1) {
                    allFields.add(new Object() {
                        public String field = path.substring(1);
                    });
                }
            }
        }

        return new Object() {
            public List<Object> fields = allFields;
        };

    }

    /**
     * Creates a list with all allowed values for a field, including localized display value
     * For JavaScript, values have to be wrapped, for example
     * <p>
     * Don't serialize as
     * {
     * [
     * "FIVE",
     * "SIX",
     * "SEVEN",
     * "EIGHT"
     * ]
     * }
     * <p>
     * But as
     * {
     * "nrOfDataBits": [
     * {
     * "nrOfDataBits": "FIVE",
     * "localizedValue": "vijf"
     * },
     * {
     * "nrOfDataBits": "SIX"
     * "localizedValue": ...
     * },
     * {
     * "nrOfDataBits": "SEVEN"
     * "localizedValue": ...
     * },
     * {
     * "nrOfDataBits": "EIGHT"
     * "localizedValue": ...
     * }
     * ]
     * }
     *
     * @param fieldName the top level list name, collection name for the values, eg: values
     * @param valueName value level field name, eg: value
     * @param values The actual values to enumerate
     * @param <T> The type of values being listed
     * @return ExtJS JSON format for listed values
     */
    protected <T> Map<String, Object> asJsonArrayObjectWithTranslation(String fieldName, String valueName, Collection<T> values) {
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        map.put(fieldName, list);
        for (final T value : values) {
            Map<String, Object> subMap = new HashMap<>();
            subMap.put(valueName, value);
            subMap.put("localizedValue", thesaurus.getString(value.toString(), value.toString()));
            list.add(subMap);
        }
        sortList(list);
        return map;
    }

    public List<Map<String, Object>> sortList(List<Map<String, Object>> list) {
        Collections.sort(list, new Comparator<Map<String, Object>>() {
            public int compare(Map<String, Object> m1, Map<String, Object> m2) {
                return m1.values().toString().compareTo(m2.values().toString());
            }
        });
        return list;
    }

    /**
     * For JavaScript, values have to be wrapped, for example
     * <p>
     * Don't serialize as
     * {
     * [
     * "FIVE",
     * "SIX",
     * "SEVEN",
     * "EIGHT"
     * ]
     * }
     * <p>
     * But as
     * {
     * "nrOfDataBits": [
     * {
     * "nrOfDataBits": "FIVE",
     * "localizedValue": "vijf"
     * },
     * {
     * "nrOfDataBits": "SIX"
     * "localizedValue": ...
     * },
     * {
     * "nrOfDataBits": "SEVEN"
     * "localizedValue": ...
     * },
     * {
     * "nrOfDataBits": "EIGHT"
     * "localizedValue": ...
     * }
     * ]
     * }
     *
     * @param fieldName the top level list name, collection name for the values, eg: values
     * @param valueName value level field name, eg: value
     * @param values The actual values to enumerate
     * @param <T> The type of values being listed
     * @return ExtJS JSON format for listed values
     */
    protected <T> Map<String, Object> asJsonArrayObject(String fieldName, String valueName, Collection<T> values) {
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        map.put(fieldName, list);
        for (final T value : values) {
            Map<String, Object> subMap = new HashMap<>();
            subMap.put(valueName, value);
            list.add(subMap);
        }
        return map;
    }

    /**
     * Creates a list with all allowed values for a field, including localized display value
     * Localization keys can be explicitly provided in the method
     * For JavaScript, values have to be wrapped, for example
     * <p>
     * Don't serialize as
     * {
     * [
     * "FIVE",
     * "SIX",
     * "SEVEN",
     * "EIGHT"
     * ]
     * }
     * <p>
     * But as
     * {
     * "nrOfDataBits": [
     * {
     * "nrOfDataBits": "FIVE",
     * "localizedValue": "vijf"
     * },
     * {
     * "nrOfDataBits": "SIX"
     * "localizedValue": ...
     * },
     * {
     * "nrOfDataBits": "SEVEN"
     * "localizedValue": ...
     * },
     * {
     * "nrOfDataBits": "EIGHT"
     * "localizedValue": ...
     * }
     * ]
     * }
     *
     * @param fieldName the top level list name, collection name for the values, eg: values
     * @param valueName value level field name, eg: value
     * @param values The actual values to enumerate
     * @param translationKeys The list of keys to use for translations. The order of the keys must be the same as the order of the values, that means, n-th value in values will be translated using n-th translation key
     * @param <T> The type of values being listed
     * @return ExtJS JSON format for listed values
     */
    protected <T> Map<String, Object> asJsonArrayObjectWithTranslation(String fieldName, String valueName, List<T> values, List<String> translationKeys) {
        if (values.size() != translationKeys.size()) {
            throw new IllegalStateException(String.format("Not enough translation keys for field resource '%s'", fieldName));
        }
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        map.put(fieldName, list);
        Iterator<String> translationKeyIterator = translationKeys.iterator();
        for (final T value : values) {
            Map<String, Object> subMap = new HashMap<>();
            subMap.put(valueName, value);
            String translationKey = translationKeyIterator.next();
            subMap.put("localizedValue", thesaurus.getString(translationKey, translationKey));
            list.add(subMap);
        }
        return map;
    }

}