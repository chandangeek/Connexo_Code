package com.elster.jupiter.issue.share.entity;

import com.elster.jupiter.issue.share.cep.CreationRuleTemplate;
import com.elster.jupiter.util.time.UtcInstant;

import java.util.List;

public interface CreationRule extends Entity {

    String getName();
    void setName(String name);

    String getComment();
    void setComment(String comment);

    String getContent();
    byte[] getData();
    void setContent(String content);

    IssueReason getReason();
    void setReason(IssueReason reason);

    long getDueInValue();
    void setDueInValue(long dueInValue);

    DueInType getDueInType();
    void setDueInType(DueInType dueInType);

    String getTemplateUuid();
    CreationRuleTemplate getTemplate();
    void setTemplateUuid(String templateName);

    UtcInstant getObsoleteTime();
    void setObsoleteTime(UtcInstant obsoleteTime);

    List<CreationRuleParameter> getParameters();
    List<CreationRuleAction> getActions();
    void addParameter(String key, String value);
    CreationRuleAction addAction(IssueActionType type, CreationRuleActionPhase phase);
    void updateContent();
    void validate();
}
