package com.energyict.mdc.common.rest;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.util.exception.MessageSeed;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Generic resource that lists all possible values for a certain field. Used in frontend to fill combo-boxes
 *
 * Why the wrapped return value? JavaScript people didn't want to see a naked JSON list, had to be
 * wrapped with meaningful field name.
 */

@Path("/field")
public class FieldResource {

    private final Thesaurus thesaurus;

    public FieldResource() {
        thesaurus = new NullObjectThesaurus();
    }

    public FieldResource(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }



    /**
     * This method will return a JSON list of all available field descriptions in this resource
     */
    @GET
    public Object getAllFields() {
        final List<Object> allFields = new ArrayList<>();
        for (Method method : this.getClass().getMethods()) {
            if (method.isAnnotationPresent(Path.class)) {
                Path annotation = method.getAnnotation(Path.class);
                final String path = annotation.value();
                if (path.length()>1) {
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
     * For JavaScript, values have to be wrapped, for example
     *
     * Don't serialize as
     * {
     *   [
     *      "FIVE",
     *      "SIX",
     *      "SEVEN",
     *      "EIGHT"
     *   ]
     * }
     *
     * But as
     * {
     *   "nrOfDataBits": [
     *       {
     *           "nrOfDataBits": "FIVE",
     *           "localizedValue": "vijf"
     *       },
     *       {
     *           "nrOfDataBits": "SIX"
     *       },
     *       {
     *           "nrOfDataBits": "SEVEN"
     *       },
     *       {
     *           "nrOfDataBits": "EIGHT"
     *       }
     *   ]
     * }
     * @param fieldName the top level list name, collection name for the values, eg: values
     * @param valueName value level field name, eg: value
     * @param values The actual values to enumerate
     * @param <T> The type of values being listed
     * @return ExtJS JSON format for listed values
     */
    protected <T> HashMap<String, Object> asJsonArrayObject(String fieldName, String valueName, Collection<T> values) {
        HashMap<String, Object> map = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        map.put(fieldName, list);
        for (final T value: values) {
            HashMap<String, Object> subMap = new HashMap<>();
            subMap.put(valueName, value);
            subMap.put("localizedValue", translate(value.toString()));
            list.add(subMap);
        }
        return map;
    }

    protected String translate(String value) {
        return thesaurus.getString(value, value);
    }


    private class NullObjectThesaurus implements Thesaurus {

        @Override
        public String getString(String key, String defaultMessage) {
            return key;
        }

        @Override
        public String getString(Locale locale, String key, String defaultMessage) {
            return key;
        }

        @Override
        public String getComponent() {
            return null;
        }

        @Override
        public NlsMessageFormat getFormat(MessageSeed seed) {
            return null;
        }

        @Override
        public void addTranslations(Iterable<? extends Translation> translations) {

        }

        @Override
        public Map<String, String> getTranslations() {
            return null;
        }

        @Override
        public String interpolate(String messageTemplate, Context context) {
            return messageTemplate;
        }

        @Override
        public String interpolate(String messageTemplate, Context context, Locale locale) {
            return messageTemplate;
        }
    }

}
