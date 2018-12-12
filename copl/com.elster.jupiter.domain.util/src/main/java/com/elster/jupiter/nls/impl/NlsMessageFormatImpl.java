/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.nls.impl;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.google.common.collect.ImmutableMap;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

final class NlsMessageFormatImpl implements NlsMessageFormat {

    private static Map<Level, String> levelIndicators = ImmutableMap.of(Level.INFO, "I", Level.CONFIG, "C", Level.WARNING, "W", Level.SEVERE, "S");

    private final int number;
    private final Level level;
    private final NlsString message;
    private final IThesaurus thesaurus;

    NlsMessageFormatImpl(IThesaurus thesaurus, int number, NlsString message, Level level) {
        this.thesaurus = thesaurus;
        this.number = number;
        this.message = message;
        this.level = level;
    }

    @Override public String format(Object... args) {
        return message.getComponent() + new DecimalFormat("0000").format(number) + levelIndicators.get(level) + ' ' + new MessageFormat(message.getString(), thesaurus.getLocale()).format(args, new StringBuffer(), null);
    }

    @Override
    public String format(Locale locale, Object... args) {
        return message.getComponent() + new DecimalFormat("0000").format(number) + levelIndicators.get(level) + ' ' + new MessageFormat(message.getString(locale), locale).format(args, new StringBuffer(), null);
    }
}
