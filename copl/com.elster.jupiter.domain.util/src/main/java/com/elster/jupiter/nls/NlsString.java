package com.elster.jupiter.nls;

import javax.inject.Inject;

import com.elster.jupiter.orm.DataModel;

public final class NlsString {
	private String key;
	private String defaultMessage;
	private final Thesaurus thesaurus;
	
	@Inject
	NlsString(Thesaurus thesaurus) {
		this.thesaurus = thesaurus;
	}
	
	NlsString init(String key, String defaultMessage) {
		this.key = key;
		this.defaultMessage = defaultMessage;
		return this;
	}
	
	/*
	 * rather ugly , but consistent
	 */
	public static NlsString from(DataModel dataModel, String key, String defaultMessage) {
		return dataModel.getInstance(NlsString.class).init(key,defaultMessage);
	}
	
	public String getString() {
		return thesaurus.getString(key, defaultMessage);
	}
	
	public String getComponent() {
		return thesaurus.getComponent();
	}
}
