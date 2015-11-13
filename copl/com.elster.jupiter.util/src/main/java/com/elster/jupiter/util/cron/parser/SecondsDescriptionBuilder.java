package com.elster.jupiter.util.cron.parser;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.impl.TranslationKeys;

import java.text.MessageFormat;

/**
 * @author grhodes
 * @since 10 Dec 2012 14:10:43
 */
public class SecondsDescriptionBuilder extends AbstractDescriptionBuilder {

    public SecondsDescriptionBuilder(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @Override
    protected String getSingleItemDescription(String expression) {
        return expression;
    }

    @Override
    protected String getIntervalDescriptionFormat(String expression) {
        return MessageFormat.format(thesaurus.getFormat(TranslationKeys.every_x_seconds).format(), expression);
    }

    @Override
    protected String getBetweenDescriptionFormat(String expression) {
        return thesaurus.getFormat(TranslationKeys.seconds_through_past_the_minute).format();
    }

    @Override
    protected String getDescriptionFormat(String expression) {
        return thesaurus.getFormat(TranslationKeys.at_x_seconds_past_the_minute).format();
    }
}
