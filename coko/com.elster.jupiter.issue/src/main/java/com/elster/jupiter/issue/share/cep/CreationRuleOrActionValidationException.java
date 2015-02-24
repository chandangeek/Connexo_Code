package com.elster.jupiter.issue.share.cep;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class CreationRuleOrActionValidationException extends LocalizedException {
    private final Map<String, String> errors = new LinkedHashMap<>();

    public CreationRuleOrActionValidationException(Thesaurus thesaurus, MessageSeeds messageSeed) {
        super(thesaurus, messageSeed);
    }

    public void addError(String fieldId, String messageSeed, Object... args) {
        String resultMessage = getThesaurus().getStringBeyondComponent(messageSeed, messageSeed);
        if (args != null) {
            resultMessage = String.format(resultMessage, args);
        }
        errors.put(fieldId, resultMessage);
    }

    public void addErrors(List<ParameterViolation> errorList) {
        for (ParameterViolation error : errorList) {
            addError(error.getParameterKey(), error.getMessageSeed(), error.getArgs());
        }
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
