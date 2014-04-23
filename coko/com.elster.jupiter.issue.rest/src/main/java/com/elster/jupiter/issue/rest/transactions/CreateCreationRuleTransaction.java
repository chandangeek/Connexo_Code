package com.elster.jupiter.issue.rest.transactions;

import com.elster.jupiter.issue.rest.response.cep.CreationRuleInfo;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;

public class CreateCreationRuleTransaction extends EditCreationRuleTransaction {

    public CreateCreationRuleTransaction(IssueService issueService, IssueCreationService issueCreationService, CreationRuleInfo request) {
        super(issueService,issueCreationService, request);
    }

    @Override
    protected CreationRule getCreaionRule() {
        return getIssueCreationService().createRule();
    }

    @Override
    protected void saveChanges(CreationRule rule){
        if (rule != null) {
            rule.save();
        }
    }
}
