/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates your logging message interfaces to support internationalization (i18n).
 * The format of your {@link Configuration} annotations on your methods
 * will then be interpreted as a translation key for the NlsService and Thesaurus classes.
 * @see com.elster.jupiter.nls.NlsService#getThesaurus(String, com.elster.jupiter.nls.Layer)
 * @see com.elster.jupiter.nls.Thesaurus#getString(String, String)
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-09 (08:37)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface I18N {
}