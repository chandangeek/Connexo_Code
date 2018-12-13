/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mail.impl;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.util.exception.MessageSeed;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * Created by bvn on 9/22/14.
 */
class SimpleNlsMessageFormat implements NlsMessageFormat {

        private final MessageSeed messageSeed;

        SimpleNlsMessageFormat(MessageSeed messageSeed) {
            this.messageSeed = messageSeed;
        }

        @Override
        public String format(Object... args) {
            return MessageFormat.format(messageSeed.getDefaultFormat(), args);
        }

        @Override
        public String format(Locale locale, Object... args) {
            return MessageFormat.format(messageSeed.getDefaultFormat(), args);
        }
    }