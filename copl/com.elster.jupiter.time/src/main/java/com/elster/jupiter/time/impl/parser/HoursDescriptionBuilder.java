/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time.impl.parser;

import com.elster.jupiter.nls.Thesaurus;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * @author grhodes
 * @since 10 Dec 2012 14:18:21
 */
class HoursDescriptionBuilder extends AbstractDescriptionBuilder {

    public HoursDescriptionBuilder(Thesaurus thesaurus, Locale locale) {
        super(thesaurus, locale);
    }

    @Override
    protected String getSingleItemDescription(String expression) {
        return new Utils(thesaurus).formatTime(expression, "0");
    }

    @Override
    protected String getIntervalDescriptionFormat(String expression) {
        return MessageFormat.format(thesaurus.getFormat(TranslationKeys.every_x).format()+" " +
                plural(expression, thesaurus.getFormat(TranslationKeys.hour).format(), thesaurus.getFormat(TranslationKeys.hours).format()), expression);
    }

    @Override
    protected String getBetweenDescriptionFormat(String expression) {
        return thesaurus.getFormat(TranslationKeys.between_x_and_y).format();
    }

    @Override
    protected String getDescriptionFormat(String expression) {
        return thesaurus.getFormat(TranslationKeys.at_x).format();
    }

}
