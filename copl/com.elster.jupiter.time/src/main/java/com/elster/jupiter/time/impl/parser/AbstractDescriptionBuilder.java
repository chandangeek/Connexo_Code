/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time.impl.parser;

import com.elster.jupiter.nls.Thesaurus;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * @author grhodes
 * @since 10 Dec 2012 14:00:49
 */
abstract class AbstractDescriptionBuilder {

    protected Thesaurus thesaurus;
    protected Locale locale;

    public AbstractDescriptionBuilder(Thesaurus thesaurus, Locale locale) {
        this.thesaurus = thesaurus;
        this.locale = locale;
    }

    protected final char[] SpecialCharsMinusStar = new char[] { '/', '-', ',' };

    public String getSegmentDescription(String expression, String allDescription) {
        String description = "";
        if (Utils.isEmpty(expression) || "0".equals(expression)) {
            description = "";
        } else if ("*".equals(expression)) {
            description = allDescription;
        } else if (!Utils.containsAny(expression, SpecialCharsMinusStar)) {
            description = MessageFormat.format(getDescriptionFormat(expression), getSingleItemDescription(expression));
        } else if (expression.contains("/")) {
            String[] segments = expression.split("/");
            description = MessageFormat.format(getIntervalDescriptionFormat(segments[1]), getSingleItemDescription(segments[1]));
            // interval contains 'between' piece (e.g. 2-59/3)
            if (segments[0].contains("-")) {
                String betweenSegmentOfInterval = segments[0];
                String[] betweenSegments = betweenSegmentOfInterval.split("-");
                description += ", " + MessageFormat.format(getBetweenDescriptionFormat(betweenSegmentOfInterval), getSingleItemDescription(betweenSegments[0]), getSingleItemDescription(betweenSegments[1]));
            }
        } else if (expression.contains("-")) {
            String[] segments = expression.split("-");
            description = MessageFormat.format(getBetweenDescriptionFormat(expression), getSingleItemDescription(segments[0]), getSingleItemDescription(segments[1]));
        } else if (expression.contains(",")) {
            String[] segments = expression.split(",");
            StringBuilder descriptionContent = new StringBuilder();
            for (int i = 0; i < segments.length; i++) {
                if ((i > 0) && (segments.length > 2)) {
                    descriptionContent.append(",");
                    if (i < (segments.length - 1)) {
                        descriptionContent.append(" ");
                    }
                }
                if ((i > 0) && (segments.length > 1) && ((i == (segments.length - 1)) || (segments.length == 2))) {
                    descriptionContent.append(" ");
                    descriptionContent.append(thesaurus.getFormat(TranslationKeys.every_minute_between).format());
                    descriptionContent.append(" ");
                }
                descriptionContent.append(getSingleItemDescription(segments[i]));
            }
            description = MessageFormat.format(getDescriptionFormat(expression), descriptionContent.toString());
        }
        return description;
    }

    /**
     * @param expression
     * @return
     */
    protected abstract String getBetweenDescriptionFormat(String expression);

    /**
     * @param expression
     * @return
     */
    protected abstract String getIntervalDescriptionFormat(String expression);

    /**
     * @param expression
     * @return
     */
    protected abstract String getSingleItemDescription(String expression);

    /**
     * @param expression
     * @return
     */
    protected abstract String getDescriptionFormat(String expression);

    /**
     * @param num
     * @param singular
     * @param plural
     * @return
     * @deprecated Use plural(String, String, String) instead
     */
    @Deprecated
    protected String plural(int num, String singular, String plural) {
        return plural(String.valueOf(num), singular, plural);
    }

    /**
     * @param expression
     * @param singular
     * @param plural
     * @return
     * @since https://github.com/RedHogs/cron-parser/issues/2
     */
    protected String plural(String expression, String singular, String plural) {
        if (Utils.isNumber(expression) && (Integer.parseInt(expression) > 1)) {
            return plural;
        } else if (Utils.contains(expression, ",")) {
            return plural;
        }
        return singular;
    }

}
