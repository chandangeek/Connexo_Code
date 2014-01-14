package com.elster.jupiter.nls;

public interface NlsService {

    String COMPONENTNAME = "NLS";

	Thesaurus getThesaurus(String componentName, Layer layer);
	
//	// optional api TODO no optional API :)
//	// equivalent to getThesaurus(componentName.get(key,defaultMessage);
//	//
//	String getString(String componentName, String key, String defaultMessage);
	
	// create time api
	Thesaurus newThesaurus(String componentName, Layer layer);
}
