package com.elster.jupiter.util.cron.parser;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.impl.TranslationKeys;

import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;


/**
 * @author grhodes
 * @since 15 Sep 2014
 */
public class YearDescriptionBuilder extends AbstractDescriptionBuilder {

    public YearDescriptionBuilder(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @Override
    protected String getSingleItemDescription(String expression) {
        return DateTimeFormatter.ofPattern("yyyy", Locale.getDefault()).format(
                ZonedDateTime.now().withYear(Integer.parseInt(expression)));
    }

    @Override
    protected String getIntervalDescriptionFormat(String expression) {
        return MessageFormat.format(", " + thesaurus.getFormat(TranslationKeys.every_x).format() + " " +
          plural(expression, thesaurus.getFormat(TranslationKeys.year).format(), thesaurus.getFormat(TranslationKeys.years).format()), expression);
    }

    @Override
    protected String getBetweenDescriptionFormat(String expression) {
        return ", "+thesaurus.getFormat(TranslationKeys.between_description_format).format();
    }

    @Override
    protected String getDescriptionFormat(String expression) {
        return ", "+thesaurus.getFormat(TranslationKeys.only_in).format();
    }
}
