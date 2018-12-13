/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.domain.util;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormValidationException extends RuntimeException {

    private Map<String, List<String>> exceptions = new HashMap<>();

    public FormValidationException addException(String fieldName, String message) {
        List<String> messages = exceptions.get(fieldName);
        if (messages == null) {
            messages = new ArrayList<>();
            exceptions.put(fieldName, messages);
        }
        messages.add(message);
        return this;
    }

    public FormValidationException addException(String fieldName, LocalizedException exception) {
        return addException(fieldName, exception.getLocalizedMessage());
    }

    public FormValidationException addException(String fieldName, MessageSeed exception, Thesaurus thesaurus) {
        return addException(fieldName, thesaurus.getFormat(exception).format());
    }

    public FormValidationException addException(String fieldName, TranslationKey exception, Thesaurus thesaurus) {
        return addException(fieldName, thesaurus.getFormat(exception).format());
    }

    public Map<String, List<String>> getExceptions() {
        return exceptions;
    }
}