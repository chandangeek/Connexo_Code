/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.impl.TranslationKeys;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import java.util.Map;
enum HsmProperties  {
    DECRYPTED_KEY("key") {
        public PropertySpec asPropertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return propertySpecService
                    .stringSpec()
                    .named(getPropertyName(), TranslationKeys.KEY).fromThesaurus(thesaurus)
                    .finish();
        }
    },
    LABEL("label") {
        public PropertySpec asPropertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return propertySpecService.stringSpec()
                    .named(getPropertyName(), TranslationKeys.LABEL).fromThesaurus(thesaurus)
                    .finish();
        }
    },
    SM_KEY("smart meter key") {
        public PropertySpec asPropertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return propertySpecService.stringSpec()
                    .named(getPropertyName(), TranslationKeys.SM_KEY).fromThesaurus(thesaurus)
                    .finish();
        }
    };

    private final String propertyName;

    HsmProperties(String propertyName) {
        this.propertyName = propertyName;
    }

    String getPropertyName() {
        return propertyName;
    }

    abstract PropertySpec asPropertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus);
}