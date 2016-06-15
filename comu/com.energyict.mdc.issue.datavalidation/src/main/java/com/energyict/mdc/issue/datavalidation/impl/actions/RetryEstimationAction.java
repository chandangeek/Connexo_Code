package com.energyict.mdc.issue.datavalidation.impl.actions;


import com.elster.jupiter.issue.share.AbstractIssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.IssueActionResult.DefaultActionResult;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issue.datavalidation.IssueDataValidation;
import com.energyict.mdc.issue.datavalidation.impl.TranslationKeys;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class RetryEstimationAction extends AbstractIssueAction {

    private static final String NAME = "RetryEstimationAction";
    //public static final String CLOSE_STATUS = NAME + ".status";
    //public static final String COMMENT = NAME + ".comment";

    private IssueService issueService;

    @Inject
    public RetryEstimationAction(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService, IssueService issueService) {
        super(dataModel, thesaurus, propertySpecService);
        this.issueService = issueService;
    }

    @Override
    public IssueActionResult execute(Issue issue) {
        DefaultActionResult result = new DefaultActionResult();
        if (isApplicable(issue)){
            issue.setStatus(issueService.findStatus(IssueStatus.IN_PROGRESS).get());
            issue.update();

            result.success(getThesaurus().getFormat(TranslationKeys.ACTION_RETRY_ESTIMATION_SUCCESS).format());
        }
        return result;
    }

    @Override
    public boolean isApplicable(Issue issue) {
        if (issue != null && !issue.getStatus().isHistorical() && issue instanceof IssueDataValidation){
            IssueDataValidation dvIssue = (IssueDataValidation) issue;
            if (!dvIssue.getStatus().isHistorical()) {

            }
        }
        return false;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(TranslationKeys.ACTION_RETRY_ESTIMATION).format();
    }
}
