package com.elster.jupiter.time.impl.parser;

import com.elster.jupiter.nls.Thesaurus;

/**
 * @author grhodes
 * @since 10 Dec 2012 14:24:08
 */
class DayOfMonthDescriptionBuilder extends AbstractDescriptionBuilder {

    public DayOfMonthDescriptionBuilder(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @Override
    protected String getSingleItemDescription(String expression) {
        return expression;
    }

    @Override
    protected String getIntervalDescriptionFormat(String expression) {
        return ", "+ thesaurus.getFormat(TranslationKeys.every_x).format()+" " +
                plural(expression, thesaurus.getFormat(TranslationKeys.day).format(), thesaurus.getFormat(TranslationKeys.days).format());
    }

    @Override
    protected String getBetweenDescriptionFormat(String expression) {
        return ", "+thesaurus.getFormat(TranslationKeys.between_days_of_the_month).format();
    }

    @Override
    protected String getDescriptionFormat(String expression) {
        return ", "+thesaurus.getFormat(TranslationKeys.on_day_of_the_month).format();
    }

}
