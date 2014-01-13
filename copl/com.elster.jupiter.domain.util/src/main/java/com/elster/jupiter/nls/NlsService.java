package com.elster.jupiter.nls;

public interface NlsService {
	Thesaurus getThesaurus(String componentName); 
	
	// optional api
	// equivalent to getThesaurus(componentName.get(key,defaultMessage);
	//
	String getString(String componentName, String key, String defaultMessage);
	
	// create time api
	Thesaurus newThesaurus(String componentName);
}
