/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time.impl.parser;

import com.elster.jupiter.nls.Thesaurus;

import java.text.MessageFormat;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * @author grhodes
 * @since 10 Dec 2012 14:23:18
 */
class DayOfWeekDescriptionBuilder extends AbstractDescriptionBuilder {

    private final Options options;

    public DayOfWeekDescriptionBuilder(Thesaurus thesaurus, Locale locale) {
        super(thesaurus, locale);
        this.options = null;
    }

    public DayOfWeekDescriptionBuilder(Thesaurus thesaurus, Options options, Locale locale) {
        super(thesaurus, locale);
        this.options = options;
    }

    @Override
    protected String getSingleItemDescription(String expression) {
        String exp = expression;
        if (expression.contains("#")) {
            exp = expression.substring(0, expression.indexOf("#"));
        } else if (expression.contains("L")) {
            exp = exp.replace("L", "");
        }
        if (Utils.isNumeric(exp)) {
            int dayOfWeekNum = Integer.parseInt(exp);
            boolean isZeroBasedDayOfWeek = (options == null || options.isZeroBasedDayOfWeek());
            boolean isInvalidDayOfWeekForSetting = (options != null && !options.isZeroBasedDayOfWeek() && dayOfWeekNum <= 1);
            if(isInvalidDayOfWeekForSetting || (isZeroBasedDayOfWeek && dayOfWeekNum == 0)) {
                return Utils.getDayOfWeekName(7, locale);
            } else if(options != null && !options.isZeroBasedDayOfWeek()) {
                dayOfWeekNum -= 1;
            }
            return Utils.getDayOfWeekName(dayOfWeekNum, locale);
        } else {
            return DayOfWeek.from(DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH).parse(
                    Utils.capitalize(exp.toLowerCase()))).getDisplayName(TextStyle.FULL, locale);
        }
    }

    @Override
    protected String getIntervalDescriptionFormat(String expression) {
        return MessageFormat.format(", "+ thesaurus.getFormat(TranslationKeys.interval_description_format).format(), expression);
    }

    @Override
    protected String getBetweenDescriptionFormat(String expression) {
        return ", "+thesaurus.getFormat(TranslationKeys.between_weekday_description_format).format();
    }

    @Override
    protected String getDescriptionFormat(String expression) {
        String format = null;
        if (expression.contains("#")) {
            String dayOfWeekOfMonthNumber = expression.substring(expression.indexOf("#") + 1);
            String dayOfWeekOfMonthDescription = "";
            if ("1".equals(dayOfWeekOfMonthNumber)) {
                dayOfWeekOfMonthDescription = thesaurus.getFormat(TranslationKeys.first).format();
            } else if ("2".equals(dayOfWeekOfMonthNumber)) {
                dayOfWeekOfMonthDescription = thesaurus.getFormat(TranslationKeys.second).format();
            } else if ("3".equals(dayOfWeekOfMonthNumber)) {
                dayOfWeekOfMonthDescription = thesaurus.getFormat(TranslationKeys.third).format();
            } else if ("4".equals(dayOfWeekOfMonthNumber)) {
                dayOfWeekOfMonthDescription = thesaurus.getFormat(TranslationKeys.fourth).format();
            } else if ("5".equals(dayOfWeekOfMonthNumber)) {
                dayOfWeekOfMonthDescription = thesaurus.getFormat(TranslationKeys.fifth).format();
            }
            format = ", "+String.format(thesaurus.getFormat(TranslationKeys.on_the_day_of_the_month).format(), dayOfWeekOfMonthDescription);
        } else if (expression.contains("L")) {
            format = ", "+thesaurus.getFormat(TranslationKeys.on_the_last_of_the_month).format();
        } else {
            format = ", "+thesaurus.getFormat(TranslationKeys.only_on).format();
        }
        return format;
    }

}
