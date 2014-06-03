package com.elster.jupiter.issue.share.cep;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CreationRuleValidationException extends LocalizedException {
    private final Map<String, String> errors = new LinkedHashMap<>();
    private final NlsService nlsService;

    public CreationRuleValidationException(NlsService nlsService) {
        super(nlsService.getThesaurus(IssueService.COMPONENT_NAME, Layer.DOMAIN), MessageSeeds.ISSUE_CREATION_RULE_VALIDATION_FAILED);
        this.nlsService = nlsService;
    }

    public void addError(String fieldId, String messageSeed, String componentName, Object... args) {
        String resultMessage = messageSeed;
        if (componentName != null) {
            Thesaurus thesaurus = nlsService.getThesaurus(componentName, Layer.DOMAIN);
            resultMessage = thesaurus.getString(messageSeed, messageSeed);
        }
        if (args != null) {
            resultMessage = String.format(messageSeed, args);
        }
        errors.put(fieldId, resultMessage);
    }

    public void addErrors(List<ParameterViolation> errorList) {
        for (ParameterViolation error : errorList) {
            addError(error.getParameterKey(), error.getMessageSeed(), error.getComponentName(), error.getArgs());
        }
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
