/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.impl.TranslationKeys;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import java.util.Map;

enum HsmProperties implements Properties {
    DECRYPTED_KEY("key") {
        @Override
        public PropertySpec asPropertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return propertySpecService
                    .stringSpec()
                    .named(getPropertyName(), TranslationKeys.KEY).fromThesaurus(thesaurus)
                    .finish();
        }

        @Override
        public void copyFromMap(Map<String, Object> properties, PropertySetter propertySetter) {
            if (properties.containsKey(getPropertyName())){
                propertySetter.setHexBinaryKey((String) properties.get(getPropertyName()));
            }
        }

        @Override
        public void copyToMap(Map<String, Object> properties, PropertySetter propertySetter) {
            properties.put(getPropertyName(), propertySetter.getHexBinaryKey());
        }
    },
    LABEL("label") {
        @Override
        public PropertySpec asPropertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return propertySpecService.stringSpec()
                    .named(getPropertyName(), TranslationKeys.LABEL).fromThesaurus(thesaurus)
                    .finish();
        }

        @Override
        public void copyFromMap(Map<String, Object> properties, PropertySetter propertySetter) {
            if (properties.containsKey(getPropertyName())){
                String label = (String) properties.get(getPropertyName());
                ((HsmPropertySetter)propertySetter).setLabel(label);
            }
        }

        @Override
        public void copyToMap(Map<String, Object> properties, PropertySetter propertySetter) {
            properties.put(getPropertyName(), ((HsmPropertySetter)propertySetter).getLabel());
        }
    };

    private final String propertyName;

    HsmProperties(String propertyName) {
        this.propertyName = propertyName;
    }

    String getPropertyName() {
        return propertyName;
    }
}