/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.nls.impl;

import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import java.util.Locale;

class NlsEntry {

    // associations
    private final Reference<NlsKey> nlsKey = ValueReference.absent();

	// persistent fields
	private String languageTag;

    private String translation;

    @Inject
    private NlsEntry() {
    }

    NlsEntry(NlsKey key, Locale locale) {
        this.nlsKey.set(key);
        this.languageTag = locale.toLanguageTag();
    }

    Locale getLocale() {
        return Locale.forLanguageTag(languageTag);
    }

    String getTranslation() {
        return translation;
    }

    public NlsEntry translation(String translation) {
        this.translation = translation;
        return this;
    }
}
