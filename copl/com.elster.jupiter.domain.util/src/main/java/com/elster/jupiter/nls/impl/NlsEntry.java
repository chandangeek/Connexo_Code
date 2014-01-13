package com.elster.jupiter.nls.impl;

import java.util.List;
import java.util.Locale;

import com.elster.jupiter.orm.associations.Reference;

public class NlsEntry {
	// persistent fields
	private String languageTag;
	
	// associations
	private Reference<NlsKey> nlsKey;
	// composite association
	private List<NlsEntry> entries;
	
	void add(Locale locale , String translation) {
		//this.entries(NlsKey.from(dataModel,Lo))
	}

}
