package com.elster.jupiter.issue.share.service;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.cep.CreationRuleTemplate;
import com.elster.jupiter.issue.share.cep.IssueEvent;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.CreationRuleAction;
import com.google.common.base.Optional;

import java.util.List;

public interface IssueCreationService {
    CreationRule createRule();

    Optional<CreationRule> findCreationRule(long id);
    Query<CreationRule> getCreationRuleQuery(Class<?>... eagers);

    Optional<CreationRuleAction> findCreationRuleAction(long id);
    Query<CreationRuleAction> getCreationRuleActionQuery();

    Optional<CreationRuleTemplate> findCreationRuleTemplate(String uuid);
    List<CreationRuleTemplate> getCreationRuleTemplates();

    void dispatchCreationEvent(IssueEvent event);
    void processCreationEvent(long ruleId, IssueEvent event);
    boolean reReadRules();
}
