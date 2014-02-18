package com.elster.jupiter.nls;

import javax.validation.ConstraintViolation;

public interface NlsService {

    String COMPONENTNAME = "NLS";

	Thesaurus getThesaurus(String componentName, Layer layer);

	String interpolate(ConstraintViolation<?> violation);

}
