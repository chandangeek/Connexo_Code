package com.elster.jupiter.issue.share.cep;

import java.util.List;

public interface CreationRuleTemplate {
    String getUUID();
    String getName();
    String getDescription();
    String getContent();
    String getIssueType();
    List<CreationRuleTemplateParameter> getParameters();
}
