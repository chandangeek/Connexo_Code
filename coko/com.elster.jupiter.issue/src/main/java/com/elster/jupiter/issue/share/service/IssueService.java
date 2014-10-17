package com.elster.jupiter.issue.share.service;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.util.exception.MessageSeed;
import java.util.Optional;

import java.util.List;

public interface IssueService {
    String COMPONENT_NAME = "ISU";

    public Optional<Issue> findIssue(long id);
    public Optional<OpenIssue> findOpenIssue(long id);
    public Optional<HistoricalIssue> findHistoricalIssue(long id);
    public Optional<IssueStatus> findStatus(String key);
    public Optional<IssueReason> findReason(String key);
    public Optional<IssueComment> findComment(long id);
    public Optional<IssueType> findIssueType(String key);
    public Optional<AssigneeRole> findAssigneeRole(long id);
    public Optional<AssigneeTeam> findAssigneeTeam(long id);
    public IssueAssignee findIssueAssignee(String type, long id);
    public boolean checkIssueAssigneeType(String type);

    /**
     * Creates new status
     * @param key unique string id
     * @param isHistorical if it is {@code true} then this status can be used for closing an open issue
     * @param seed MessageSeed which contains translation for this status. It will be saved automatically as a part of the {@value IssueService#COMPONENT_NAME} component
     * @return instance of issue status (it is already saved into database)
     */
    public IssueStatus createStatus(String key, boolean isHistorical, MessageSeed seed);

    /**
     * Creates new reason
     * @param key unique string id
     * @param type for which issue types this reason can be applied (data collection, data validation and so on)
     * @param seed MessageSeed which contains translation for this reason. It will be saved automatically as a part of the {@value IssueService#COMPONENT_NAME} component
     * @return instance of issue reason (it is already saved into database)
     */
    public IssueReason createReason(String key, IssueType type, MessageSeed seed);

    /**
     * Creates new issue type (For example: data collection or data validation)
     * @param key unique string id
     * @param seed MessageSeed which contains translation for this issue type. It will be saved automatically as a part of the {@value IssueService#COMPONENT_NAME} component
     * @return instance of issue type (it is already saved into database)
     */
    public IssueType createIssueType(String key, MessageSeed seed);
    public AssigneeRole createAssigneeRole();
    public AssigneeTeam createAssigneeTeam();

    public <T extends Entity> Query<T> query(Class<T> clazz, Class<?>... eagers);
    List<IssueGroup> getIssueGroupList(IssueGroupFilter builder);
    public int countOpenDataCollectionIssues(String mRID);
}
