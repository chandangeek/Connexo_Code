package com.elster.jupiter.issue.impl.actions;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.AbstractIssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.IssueActionResult.DefaultActionResult;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.StringFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class CloseIssueAction extends AbstractIssueAction {
    
    private static final String NAME = "CloseIssueAction";
    private static final String CLOSESTATUS = NAME + ".status";
    private static final String COMMENT = NAME + ".comment";

    private final IssueService issueService;

    @Inject
    protected CloseIssueAction(DataModel dataModel,Thesaurus thesaurus, PropertySpecService propertySpecService, IssueService issueService) {
        super(dataModel, thesaurus, propertySpecService);
        this.issueService = issueService;
    }

    @Override
    public IssueActionResult execute(Issue issue) {
      DefaultActionResult result = new DefaultActionResult();

      IssueStatus closeStatus = getStatusFromParameters(properties);
      if (closeStatus == null) {
          result.fail(MessageSeeds.ACTION_WRONG_STATUS.getTranslated(getThesaurus()));
          return result;
      }
      if (isApplicable(issue)){
          ((OpenIssue)issue).close(closeStatus);
          result.success(MessageSeeds.ACTION_ISSUE_WAS_CLOSED.getTranslated(getThesaurus()));
      } else {
          result.fail(MessageSeeds.ACTION_ISSUE_ALREADY_CLOSED.getTranslated(getThesaurus()));
      }
      return result;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        Builder<PropertySpec> builder = ImmutableList.builder();
        builder.add(getPropertySpecService().newPropertySpecBuilder(new StringFactory())
                                            .name(CLOSESTATUS)
                                            .addValues(MessageSeeds.ISSUE_STATUS_RESOLVED.getKey(), 
                                                       MessageSeeds.ISSUE_STATUS_WONT_FIX.getKey())
                                             .markRequired()
                                             .finish());
        builder.add(getPropertySpecService().stringPropertySpec(COMMENT, false, null));
        return builder.build();
    }

    @Override
    public String getNameDefaultFormat() {
        return MessageSeeds.ACTION_CLOSE_ISSUE.getDefaultFormat();
    }

    @Override
    public String getPropertyDefaultFormat(String property) {
        switch (property) {
        case CLOSESTATUS:
            return MessageSeeds.PARAMETER_CLOSE_STATUS.getDefaultFormat();
        case COMMENT:
            return MessageSeeds.PARAMETER_COMMENT.getDefaultFormat();
        default:
            break;
        }
        return null;
    }

    @Override
    public boolean isApplicable(Issue issue) {
        return super.isApplicable(issue) && IssueStatus.OPEN.equals(issue.getStatus().getKey());
    }

    private IssueStatus getStatusFromParameters(Map<String, Object> properties){
        String statusKey = getPropertySpec(CLOSESTATUS).getValueFactory().toStringValue(properties.get(CLOSESTATUS));
        return issueService.findStatus(statusKey).orElse(null);
    }
}
