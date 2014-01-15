package com.elster.jupiter.nls;

public interface NlsService {

    String COMPONENTNAME = "NLS";

	Thesaurus getThesaurus(String componentName, Layer layer);

}
