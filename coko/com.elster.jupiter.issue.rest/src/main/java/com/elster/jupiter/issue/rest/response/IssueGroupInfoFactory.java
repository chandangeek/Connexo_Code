package com.elster.jupiter.issue.rest.response;


import com.elster.jupiter.issue.rest.MessageSeeds;
import com.elster.jupiter.issue.rest.resource.IssueRestModuleConst;
import com.elster.jupiter.issue.share.entity.AssigneeType;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.entity.IssueGroup;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryFilter;

import javax.inject.Inject;
import java.util.Optional;

import static com.elster.jupiter.issue.rest.TranslationKeys.ISSUE_ASSIGNEE_UNASSIGNED;

public class IssueGroupInfoFactory {
    private final IssueService issueService;
    private final Thesaurus thesaurus;


    @Inject
    public IssueGroupInfoFactory(IssueService issueService, Thesaurus thesaurus) {
        this.issueService = issueService;
        this.thesaurus = thesaurus;
    }

    public IssueGroupInfo asInfo(IssueGroup issueGroup, JsonQueryFilter filter) {
        IssueGroupInfo issueGroupInfo = new IssueGroupInfo(issueGroup);
        if (filter.hasProperty(IssueRestModuleConst.FIELD)) {
            switch (filter.getString(IssueRestModuleConst.FIELD)) {
                case IssueRestModuleConst.REASON:
                    Optional<IssueReason> issueReasonRef = issueService.findReason(issueGroup.getGroupKey().toString());
                    if (issueReasonRef.isPresent()) {
                        issueGroupInfo.id = issueReasonRef.get().getKey();
                        issueGroupInfo.description = issueReasonRef.get().getName();
                    }
                    break;
                case IssueRestModuleConst.STATUS:
                    Optional<IssueStatus> issueStatusRef = issueService.findStatus(issueGroup.getGroupKey().toString());
                    if (issueStatusRef.isPresent()) {
                        issueGroupInfo.id = issueStatusRef.get().getKey();
                        issueGroupInfo.description = issueStatusRef.get().getName();
                    }
                    break;
                case IssueRestModuleConst.ISSUE_TYPE:
                    Optional<IssueType> issueTypeRef = issueService.findIssueType(issueGroup.getGroupKey().toString());
                    if (issueTypeRef.isPresent()) {
                        issueGroupInfo.id = issueTypeRef.get().getKey();
                        issueGroupInfo.description = issueTypeRef.get().getName();
                    }
                    break;
                case IssueRestModuleConst.ASSIGNEE:
                    IssueAssigneeInfoAdapter issueAssigneeInfoAdapter = new IssueAssigneeInfoAdapter();
                    Optional<IssueAssignee> issueAssigneeRef = issueService.findIssueAssignee(AssigneeType.USER, Long.parseLong(issueGroup.getGroupKey().toString()));

                    try {
                        if (issueAssigneeRef.isPresent()) {
                            issueGroupInfo.id = issueAssigneeInfoAdapter.marshal(new IssueAssigneeInfo(issueAssigneeRef.get()));
                        } else if (Long.parseLong(issueGroup.getGroupKey().toString()) == IssueRestModuleConst.ISSUE_UNASSIGNED_ID) {
                            issueGroupInfo.id = issueAssigneeInfoAdapter.marshal(new IssueAssigneeInfo
                                    (IssueRestModuleConst.ISSUE_UNEXISTING_TYPE, IssueRestModuleConst.ISSUE_UNASSIGNED_ID, thesaurus.getFormat(ISSUE_ASSIGNEE_UNASSIGNED).format()));
                        }
                    } catch (Exception ex) {
                        throw new IllegalArgumentException("Grouping field assignee: can't be marshalled");
                    }
                    break;
                default:
                    throw new LocalizedFieldValidationException(MessageSeeds.INVALID_VALUE, "filter");
            }
        }
        return issueGroupInfo;
    }

}
