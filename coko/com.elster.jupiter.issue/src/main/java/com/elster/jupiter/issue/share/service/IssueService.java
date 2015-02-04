package com.elster.jupiter.issue.share.service;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.cep.CreationRuleTemplate;
import com.elster.jupiter.issue.share.cep.IssueActionFactory;
import com.elster.jupiter.issue.share.entity.Entity;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.entity.IssueComment;
import com.elster.jupiter.issue.share.entity.IssueGroup;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IssueService {
    
    String COMPONENT_NAME = "ISU";

    Optional<? extends Issue> findIssue(long id);

    Optional<OpenIssue> findOpenIssue(long id);

    Optional<HistoricalIssue> findHistoricalIssue(long id);

    Optional<IssueStatus> findStatus(String key);

    Optional<IssueReason> findReason(String key);

    Optional<IssueComment> findComment(long id);

    Optional<IssueType> findIssueType(String key);

    IssueAssignee findIssueAssignee(String type, long id);

    boolean checkIssueAssigneeType(String type);

    /**
     * Creates new status
     * @param key unique string id
     * @param isHistorical if it is {@code true} then this status can be used for closing an open issue
     * @param seed MessageSeed which contains translation for this status. It will be saved automatically as a part of the {@value IssueService#COMPONENT_NAME} component
     * @return instance of issue status (it is already saved into database)
     */
    IssueStatus createStatus(String key, boolean isHistorical, MessageSeed seed);

    /**
     * Creates new reason
     * @param key unique string id
     * @param type for which issue types this reason can be applied (data collection, data validation and so on)
     * @param seed MessageSeed which contains translation for this reason. It will be saved automatically as a part of the {@value IssueService#COMPONENT_NAME} component
     * @return instance of issue reason (it is already saved into database)
     */
    IssueReason createReason(String key, IssueType type, MessageSeed seed);

    /**
     * Creates new issue type (For example: data collection or data validation)
     * @param key unique string id
     * @param seed MessageSeed which contains translation for this issue type. It will be saved automatically as a part of the {@value IssueService#COMPONENT_NAME} component
     * @return instance of issue type (it is already saved into database)
     */
    IssueType createIssueType(String key, MessageSeed seed);

    <T extends Entity> Query<T> query(Class<T> clazz, Class<?>... eagers);

    List<IssueGroup> getIssueGroupList(IssueGroupFilter builder);

    int countOpenDataCollectionIssues(String mRID);

    List<IssueProvider> getIssueProviders();
    
    IssueActionService getIssueActionService();
    
    IssueAssignmentService getIssueAssignmentService();
    
    IssueCreationService getIssueCreationService();
    
    Map<String, CreationRuleTemplate> getCreationRuleTemplates();
    
    Map<String, IssueActionFactory> getIssueActionFactories();
    
}
