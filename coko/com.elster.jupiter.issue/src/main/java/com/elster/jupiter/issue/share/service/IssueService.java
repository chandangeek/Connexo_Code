/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share.service;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueActionFactory;
import com.elster.jupiter.issue.share.IssueCreationValidator;
import com.elster.jupiter.issue.share.IssueFilter;
import com.elster.jupiter.issue.share.IssueGroupFilter;
import com.elster.jupiter.issue.share.IssueProvider;
import com.elster.jupiter.issue.share.entity.AssigneeType;
import com.elster.jupiter.issue.share.entity.Entity;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.entity.IssueComment;
import com.elster.jupiter.issue.share.entity.IssueGroup;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.IssueTypes;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.users.User;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@ProviderType
public interface IssueService {

    String COMPONENT_NAME = "ISU";

    Optional<? extends Issue> findIssue(long id);

    Optional<OpenIssue> findOpenIssue(long id);

    Optional<HistoricalIssue> findHistoricalIssue(long id);

    Optional<IssueStatus> findStatus(String key);

    Optional<IssueReason> findReason(String key);

    IssueReason findOrCreateReason(String key, IssueType issueType);

    Optional<IssueComment> findComment(long id);

    Optional<IssueType> findIssueType(String key);

    IssueAssignee findIssueAssignee(Long userId, Long workGroupId);

    /**
     * Creates new status
     *
     * @param key unique string id
     * @param isHistorical if it is {@code true} then this status can be used for closing an open issue
     * @param translationKey TranslationKey which contains translation for this status. It will be saved automatically as a part of the {@value IssueService#COMPONENT_NAME} component
     * @return instance of issue status (it is already saved into database)
     */
    IssueStatus createStatus(String key, boolean isHistorical, TranslationKey translationKey);

    /**
     * Creates new reason
     *
     * @param key unique string id
     * @param type for which issue types this reason can be applied (data collection, data validation and so on)
     * @param name TranslationKey which contains name for this reason. It will be saved automatically as a part of the {@value IssueService#COMPONENT_NAME} component
     * @param description TranslationKey which contains description for this reason. It will be saved automatically as a part of the {@value IssueService#COMPONENT_NAME} component
     * @return instance of issue reason (it is already saved into database)
     */
    IssueReason createReason(String key, IssueType type, TranslationKey name, TranslationKey description);

    /**
     * Creates new issue type (For example: data collection or data validation)
     *
     * @param key unique string id
     * @param translationKey TraslationKey which contains translation for this issue type. It will be saved automatically as a part of the {@value IssueService#COMPONENT_NAME} component
     * @return instance of issue type (it is already saved into database)
     */
    IssueType createIssueType(String key, TranslationKey translationKey, String prefix);

    <T extends Entity> Query<T> query(Class<T> clazz, Class<?>... eagers);

    List<IssueGroup> getIssueGroupList(IssueGroupFilter builder);

    Finder<OpenIssue> findOpenIssuesForDevices(List<String> deviceNames);

    Finder<OpenIssue> findOpenIssuesForDevice(String deviceName);

    List<IssueProvider> getIssueProviders();

    List<IssueCreationValidator> getIssueCreationValidators();

    IssueActionService getIssueActionService();

    IssueAssignmentService getIssueAssignmentService();

    IssueCreationService getIssueCreationService();

    Map<String, CreationRuleTemplate> getCreationRuleTemplates();

    Map<String, IssueActionFactory> getIssueActionFactories();

    Finder<? extends Issue> findIssues(IssueFilter filter, Class<?>... eagers);

    Finder<? extends Issue> findAlarms(IssueFilter filter, Class<?>... eagers);

    Optional<? extends Issue> findAndLockIssueByIdAndVersion(long id, long version);

    IssueFilter newIssueFilter();

    IssueGroupFilter newIssueGroupFilter();

    boolean checkIssueAssigneeType(String type);

    Optional<IssueAssignee> findIssueAssignee(AssigneeType assigneeType, long id);

    Map<IssueTypes, Long> getUserOpenIssueCount(User user);

    Map<IssueTypes, Long> getWorkGroupWithoutUserOpenIssueCount(User user);

}
