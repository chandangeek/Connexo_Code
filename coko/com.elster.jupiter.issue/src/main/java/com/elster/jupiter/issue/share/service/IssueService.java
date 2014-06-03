package com.elster.jupiter.issue.share.service;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.entity.*;
import com.google.common.base.Optional;

import java.util.List;

public interface IssueService {
    String COMPONENT_NAME = "ISU";

    public Optional<Issue> findIssue(long id);
    public Optional<Issue> findIssue(long id, boolean searchInHistory);
    public Optional<IssueStatus> findStatus(long id);
    public Optional<IssueReason> findReason(long id);
    public Optional<IssueComment> findComment(long id);
    public Optional<IssueType> findIssueType(String uuid);
    public Optional<AssigneeRole> findAssigneeRole(long id);
    public Optional<AssigneeTeam> findAssigneeTeam(long id);
    public IssueAssignee findIssueAssignee(String type, long id);
    public boolean checkIssueAssigneeType(String type);

    public IssueStatus createStatus(String name, boolean isFinal);
    public IssueReason createReason(String name, IssueType type);
    public IssueType createIssueType(String typeUuid, String typeName);
    public AssigneeRole createAssigneeRole();
    public AssigneeTeam createAssigneeTeam();

    public <T extends Entity> Query<T> query(Class<T> clazz, Class<?>... eagers);
    List<GroupByReasonEntity> getIssueGroupList(GroupQueryBuilder builder);
    public int countOpenDataCollectionIssues(String mRID);
}
