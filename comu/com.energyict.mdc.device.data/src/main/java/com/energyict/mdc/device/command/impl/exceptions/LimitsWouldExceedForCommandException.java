/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.command.impl.exceptions;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.streams.FancyJoiner;

import com.energyict.mdc.device.command.impl.TranslationKeys;

import java.util.ArrayList;
import java.util.List;

public class LimitsWouldExceedForCommandException extends LimitsExceededForCommandException {

    public LimitsWouldExceedForCommandException(Thesaurus thesaurus, List<ExceededCommandRule> exceededCommandRules) {
        super(thesaurus, exceededCommandRules);
    }

    protected String getFancyLimits(ExceededCommandRule exceededCommandRule, String and) {
        List<String> brokenLimits = new ArrayList<>();
        BulkExceededCommandRule bulkExceededCommandRule = (BulkExceededCommandRule)exceededCommandRule;
        String message;
        if (bulkExceededCommandRule.isDayLimitExceeded()) {
            message = thesaurus.getFormat(TranslationKeys.DAY_LIMIT_NO_CASE).format()
                    + thesaurus.getFormat(TranslationKeys.COMMANDS_ALLOWED).format(bulkExceededCommandRule.getDayAllowedCommands());
            brokenLimits.add(message);
        }
        if (bulkExceededCommandRule.isWeekLimitExceeded()) {
            message = thesaurus.getFormat(TranslationKeys.WEEK_LIMIT_NO_CASE).format()
                    + thesaurus.getFormat(TranslationKeys.COMMANDS_ALLOWED).format(bulkExceededCommandRule.getWeekAllowedCommands());
            brokenLimits.add(message);
        }
        if (bulkExceededCommandRule.isMonthLimitExceeded()) {
            message = thesaurus.getFormat(TranslationKeys.MONTH_LIMIT_NO_CASE).format()
                    + thesaurus.getFormat(TranslationKeys.COMMANDS_ALLOWED).format(bulkExceededCommandRule.getMonthAllowedCommands());
            brokenLimits.add(message);
        }

        return brokenLimits.stream()
                .collect(FancyJoiner.joining(", ", and));
    }
}