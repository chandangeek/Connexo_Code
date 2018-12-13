/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.nls.impl;

import com.elster.jupiter.nls.Thesaurus;

import java.util.Locale;

final class NlsString {
	private final String key;
	private final String defaultMessage;
	private final String component;
	private final Thesaurus thesaurus;

	NlsString(Thesaurus thesaurus, String component, String key, String defaultMessage) {
		this.thesaurus = thesaurus;
        this.component = component;
        this.key = key;
        this.defaultMessage = defaultMessage;
    }

    static NlsString from(Thesaurus thesaurus, String component, String key, String defaultFormat) {
        return new NlsString(thesaurus, component, key, defaultFormat);
    }

	public String getString() {
		return thesaurus.getString(key, defaultMessage);
	}

    public String getString(Locale locale) {
        return thesaurus.getString(locale, key, defaultMessage);
    }

	public String getComponent() {
		return component;
	}
}
