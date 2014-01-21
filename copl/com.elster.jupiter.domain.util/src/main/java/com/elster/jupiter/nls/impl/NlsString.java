package com.elster.jupiter.nls.impl;

import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;
import java.util.Locale;

final class NlsString {
	private String key;
	private String defaultMessage;
	private final Thesaurus thesaurus;
	
	@Inject
	NlsString(Thesaurus thesaurus) {
		this.thesaurus = thesaurus;
	}

    static NlsString from(Thesaurus thesaurus, String key, String defaultFormat) {
        return new NlsString(thesaurus).init(key, defaultFormat);
    }

    NlsString init(String key, String defaultMessage) {
		this.key = key;
		this.defaultMessage = defaultMessage;
		return this;
	}
	
	public String getString() {
		return thesaurus.getString(key, defaultMessage);
	}

    public String getString(Locale locale) {
        return thesaurus.getString(locale, key, defaultMessage);
    }
	
	public String getComponent() {
		return thesaurus.getComponent();
	}
}
