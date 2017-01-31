/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.command.impl.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.streams.FancyJoiner;
import com.energyict.mdc.device.command.impl.MessageSeeds;
import com.energyict.mdc.device.command.impl.TranslationKeys;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LimitsExceededForCommandException extends LocalizedException {
    private final Thesaurus thesaurus;
    String message;

    public LimitsExceededForCommandException(Thesaurus thesaurus, List<ExceededCommandRule> exceededCommandRules) {
        //Dummy call to super so it the exception would get propagated correctly to the FE
        super(thesaurus, MessageSeeds.LIMITS_EXCEEDED);
        this.thesaurus = thesaurus;
        if (exceededCommandRules.size() > 0) {
            createTranslatedMessage(exceededCommandRules);
        }
    }

    private void createTranslatedMessage(List<ExceededCommandRule> exceededCommandRules) {
        String and = ' ' + thesaurus.getFormat(TranslationKeys.AND).format() + ' ';

        ExceededCommandRule exceededCommandRule = exceededCommandRules.get(0);
        this.message = thesaurus.getFormat(MessageSeeds.LIMITS_EXCEEDED).format(getFancyLimits(exceededCommandRule, and), exceededCommandRule.getName());
        if (exceededCommandRules.size() > 1) {
            exceededCommandRules.remove(0);
            List<String> otherMessages = exceededCommandRules.stream()
                    .map(rule -> thesaurus.getFormat(TranslationKeys.THE_X_OF_Y).format(getFancyLimits(rule, and), rule.getName()))
                    .collect(Collectors.toList());
            otherMessages.add(0, message);

            message = otherMessages.stream()
                    .collect(FancyJoiner.joining(", ", and));
        }
    }

    private String getFancyLimits(ExceededCommandRule exceededCommandRule, String and) {
        List<String> brokenLimits = new ArrayList<>();
        if (exceededCommandRule.isDayLimitExceeded()) {
            brokenLimits.add(thesaurus.getFormat(TranslationKeys.DAY_LIMIT_NO_CASE).format());
        }
        if (exceededCommandRule.isWeekLimitExceeded()) {
            brokenLimits.add(thesaurus.getFormat(TranslationKeys.WEEK_LIMIT_NO_CASE).format());
        }
        if (exceededCommandRule.isMonthLimitExceeded()) {
            brokenLimits.add(thesaurus.getFormat(TranslationKeys.MONTH_LIMIT_NO_CASE).format());
        }

        return brokenLimits.stream()
                .collect(FancyJoiner.joining(", ", and));
    }

    @Override
    public String getLocalizedMessage() {
        return message;
    }
}
