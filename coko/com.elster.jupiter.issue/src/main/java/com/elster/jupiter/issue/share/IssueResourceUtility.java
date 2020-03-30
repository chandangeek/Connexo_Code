package com.elster.jupiter.issue.share;

import aQute.bnd.annotation.ConsumerType;
import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.issue.share.entity.Issue;

import java.util.List;

@ProviderType
public interface IssueResourceUtility {
    List<IssueGroupInfo> getIssueGroupList(List<? extends Issue> issues, String value);
}
