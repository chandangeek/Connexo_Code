/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time.impl.parser;

import com.elster.jupiter.nls.Thesaurus;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author grhodes
 * https://github.com/RedHogs/cron-parser
 * @since 10 Dec 2012 11:36:38
 */
public class CronExpressionDescriptorImpl {

    private static final char[] specialCharacters = new char[] { '/', '-', ',', '*' };

    private final Thesaurus thesaurus;

    public CronExpressionDescriptorImpl(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public String getDescription(String expression, Locale locale)  {
        return getDescription(DescriptionTypeEnum.FULL, expression, new Options(), locale);
    }

    public String getDescription(String expression, Options options, Locale locale)  {
        return getDescription(DescriptionTypeEnum.FULL, expression, options, locale);
    }

    public String getDescription(DescriptionTypeEnum type, String expression, Locale locale){
        return getDescription(type, expression, new Options(),locale);
    }

    public String getDescription(DescriptionTypeEnum type, String expression, Options options, Locale locale)  {
        String[] expressionParts;
        String description = "";
        try {
            expressionParts = ExpressionParser.parse(expression, options, locale);
            switch (type) {
                case FULL:
                    description = getFullDescription(expressionParts, options, locale);
                    break;
                case TIMEOFDAY:
                    description = getTimeOfDayDescription(expressionParts, locale);
                    break;
                case HOURS:
                    description = getHoursDescription(expressionParts, locale);
                    break;
                case MINUTES:
                    description = getMinutesDescription(expressionParts, locale);
                    break;
                case SECONDS:
                    description = getSecondsDescription(expressionParts, locale);
                    break;
                case DAYOFMONTH:
                    description = getDayOfMonthDescription(expressionParts, locale);
                    break;
                case MONTH:
                    description = getMonthDescription(expressionParts, locale);
                    break;
                case DAYOFWEEK:
                    description = getDayOfWeekDescription(expressionParts, options, locale);
                    break;
                case YEAR:
                    description = getYearDescription(expressionParts, options, locale);
                    break;
                default:
                    description = getSecondsDescription(expressionParts, locale);
                    break;
            }
        } catch (ParseException e) {
            return expression;
        }
        return description;
    }

    /**
     * @param expressionParts
     * @return
     */
    private String getYearDescription(String[] expressionParts, Options options, Locale locale) {
      return new YearDescriptionBuilder(thesaurus, locale).getSegmentDescription(expressionParts[6], ", "+thesaurus.getFormat(TranslationKeys.every_year).format());
    }

    /**
     * @param expressionParts
     * @return
     */
    private String getDayOfWeekDescription(String[] expressionParts, Options options, Locale locale) {
        return new DayOfWeekDescriptionBuilder(thesaurus, options, locale).getSegmentDescription(expressionParts[5], ", " +
                thesaurus.getFormat(TranslationKeys.every_day).format());
    }

    /**
     * @param expressionParts
     * @return
     */
    private String getMonthDescription(String[] expressionParts, Locale locale) {
        return new MonthDescriptionBuilder(thesaurus, locale).getSegmentDescription(expressionParts[4], "");
    }

    /**
     * @param expressionParts
     * @return
     */
    private String getDayOfMonthDescription(String[] expressionParts, Locale locale) {
        String description = null;
        String exp = expressionParts[3].replace("?", "*");
        if ("L".equals(exp)) {
            description = ", "+ thesaurus.getFormat(TranslationKeys.on_the_last_day_of_the_month).format();
        } else if ("WL".equals(exp) || "LW".equals(exp)) {
            description = ", " + thesaurus.getFormat(TranslationKeys.on_the_last_weekday_of_the_month);
        } else {
            Pattern pattern = Pattern.compile("(\\dW)|(W\\d)");
            Matcher matcher = pattern.matcher(exp);
            if (matcher.matches()) {
                int dayNumber = Integer.parseInt(matcher.group().replace("W", ""));
                String dayString = dayNumber == 1 ? thesaurus.getFormat(TranslationKeys.first_weekday).format() : thesaurus.getFormat(TranslationKeys.weekday_nearest_day).format(dayNumber);
                description = thesaurus.getFormat(TranslationKeys.on_the_of_the_month).format(dayString);
            } else {
                description = new DayOfMonthDescriptionBuilder(thesaurus, locale).getSegmentDescription(exp, ", "+thesaurus.getFormat(TranslationKeys.every_day).format());
            }
        }
        return description;
    }

    /**
     * @param expressionParts
     * @return
     */
    private String getSecondsDescription(String[] expressionParts, Locale locale) {
        return new SecondsDescriptionBuilder(thesaurus, locale).getSegmentDescription(expressionParts[0], thesaurus.getFormat(TranslationKeys.every_second).format());
    }

    /**
     * @param expressionParts
     * @return
     */
    private String getMinutesDescription(String[] expressionParts, Locale locale) {
        return new MinutesDescriptionBuilder(thesaurus, locale).getSegmentDescription(expressionParts[1], thesaurus.getFormat(TranslationKeys.every_minute).format());
    }

    /**
     * @param expressionParts
     * @return
     */
    private String getHoursDescription(String[] expressionParts, Locale locale) {
        return new HoursDescriptionBuilder(thesaurus, locale).getSegmentDescription(expressionParts[2], thesaurus.getFormat(TranslationKeys.every_hour).format());
    }

    /**
     * @param expressionParts
     * @return
     */
    private String getTimeOfDayDescription(String[] expressionParts, Locale locale) {
        String secondsExpression = expressionParts[0];
        String minutesExpression = expressionParts[1];
        String hoursExpression = expressionParts[2];
        StringBuilder description = new StringBuilder();
        // Handle special cases first
        if (!Utils.containsAny(minutesExpression, specialCharacters) && !Utils.containsAny(hoursExpression, specialCharacters) && !Utils.containsAny(secondsExpression, specialCharacters)) {
            description.append(thesaurus.getFormat(TranslationKeys.at).format()).append(" ").append(new Utils(thesaurus).formatTime(hoursExpression, minutesExpression, secondsExpression)); // Specific time of day (e.g. 10 14)
        } else if (minutesExpression.contains("-") && !minutesExpression.contains("/") && !Utils.containsAny(hoursExpression, specialCharacters)) {
            // Minute range in single hour (e.g. 0-10 11)
            String[] minuteParts = minutesExpression.split("-");
            description.append(thesaurus.getFormat(TranslationKeys.every_minute_between).format(new Utils(thesaurus).formatTime(hoursExpression, minuteParts[0]),
                    new Utils(thesaurus).formatTime(hoursExpression, minuteParts[1])));
        } else if (hoursExpression.contains(",") && !Utils.containsAny(minutesExpression, specialCharacters)) {
            // Hours list with single minute (e.g. 30 6,14,16)
            String[] hourParts = hoursExpression.split(",");
            description.append(thesaurus.getFormat(TranslationKeys.at).format());
            for (int i = 0; i < hourParts.length; i++) {
                description.append(" ").append(new Utils(thesaurus).formatTime(hourParts[i], minutesExpression));
                if (i < hourParts.length - 2) {
                    description.append(",");
                }
                if (i == hourParts.length - 2) {
                    description.append(" ");
                    description.append(thesaurus.getFormat(TranslationKeys.and).format());
                }
            }
        } else {
            String secondsDescription = getSecondsDescription(expressionParts, locale);
            String minutesDescription = getMinutesDescription(expressionParts, locale);
            String hoursDescription = getHoursDescription(expressionParts, locale);
            description.append(secondsDescription);
            if (description.length() > 0 && Utils.isNotEmpty(minutesDescription)) {
                description.append(", ");
            }
            description.append(minutesDescription);
            if (description.length() > 0 && Utils.isNotEmpty(hoursDescription)) {
                description.append(", ");
            }
            description.append(hoursDescription);
        }
        return description.toString();
    }

    /**
     * @param options
     * @param expressionParts
     * @return
     */
    private String getFullDescription(String[] expressionParts, Options options, Locale locale) {
        String description = "";
        String timeSegment = getTimeOfDayDescription(expressionParts, locale);
        String dayOfMonthDesc = getDayOfMonthDescription(expressionParts, locale);
        String monthDesc = getMonthDescription(expressionParts, locale);
        String dayOfWeekDesc = getDayOfWeekDescription(expressionParts, options, locale);
        String yearDesc = getYearDescription(expressionParts, options, locale);
        description = MessageFormat.format("{0}{1}{2}{3}", timeSegment, ("*".equals(expressionParts[3]) ? dayOfWeekDesc : dayOfMonthDesc), monthDesc, yearDesc);
        description = transformVerbosity(description, options);
        description = transformCase(description, options);
        return description;
    }

    /**
     * @param description
     * @return
     */
    private String transformCase(String description, Options options) {
        String descTemp = description;
        switch (options.getCasingType()) {
            case Sentence:
                descTemp = Utils.upperCase("" + descTemp.charAt(0)) + descTemp.substring(1);
                break;
            case Title:
                descTemp = Utils.capitalize(descTemp);
                break;
            default:
                descTemp = descTemp.toLowerCase();
                break;
        }
        return descTemp;
    }

    /**
     * @param description
     * @param options
     * @return
     */
    private String transformVerbosity(String description, Options options) {
        String descTemp = description;
        if (!options.isVerbose()) {
            descTemp = descTemp.replace(thesaurus.getFormat(TranslationKeys.every_1_minute).format(), thesaurus.getFormat(TranslationKeys.every_minute).format());
            descTemp = descTemp.replace(thesaurus.getFormat(TranslationKeys.every_1_hour).format(), thesaurus.getFormat(TranslationKeys.every_hour).format());
            descTemp = descTemp.replace(thesaurus.getFormat(TranslationKeys.every_1_day).format(), thesaurus.getFormat(TranslationKeys.every_day).format());
            //descTemp = descTemp.replace(", "+thesaurus.getFormat(TranslationKeys.every_minute).format(), "");
           //descTemp = descTemp.replace(", "+thesaurus.getFormat(TranslationKeys.every_hour).format(), "");
            //descTemp = descTemp.replace(", "+thesaurus.getFormat(TranslationKeys.every_day).format(), "");
        }
        return descTemp;
    }

}
