package com.elster.jupiter.issue.impl.service;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.issue.impl.database.groups.GroupIssuesOperation;
import com.elster.jupiter.issue.impl.records.IssueReasonImpl;
import com.elster.jupiter.issue.impl.records.IssueStatusImpl;
import com.elster.jupiter.issue.impl.records.IssueTypeImpl;
import com.elster.jupiter.issue.impl.records.assignee.AssigneeRoleImpl;
import com.elster.jupiter.issue.impl.records.assignee.AssigneeTeamImpl;
import com.elster.jupiter.issue.impl.records.assignee.types.AssigneeTypes;
import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.issue.share.service.GroupQueryBuilder;
import com.elster.jupiter.issue.share.service.IssueMappingService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.google.common.base.Optional;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;
import java.util.logging.Logger;

import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.elster.jupiter.issue", service = IssueService.class)
public class IssueServiceImpl implements IssueService {
    private static final Logger LOG = Logger.getLogger(IssueServiceImpl.class.getName());

    private volatile DataModel dataModel;
    private volatile QueryService queryService;
    private volatile UserService userService;

    public IssueServiceImpl() {
    }

    @Inject
    public IssueServiceImpl(QueryService queryService,
                            UserService userService,
                            IssueMappingService issueMappingService) {
        setQueryService(queryService);
        setIssueMappingService(issueMappingService);
        setUserService(userService);
    }

    @Reference
    public final void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Reference
    public final void setIssueMappingService(IssueMappingService issueMappingService) {
        dataModel = IssueMappingServiceImpl.class.cast(issueMappingService).getDataModel();
    }

    @Reference
    public final void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Optional<Issue> findIssue(long id) {
        return findIssue(id, false);
    }

    @Override
    public Optional<Issue> findIssue(long id, boolean searchInHistory) {
        Optional<Issue> issue = find(Issue.class, id);
        if (!issue.isPresent() && searchInHistory) {
            Issue historicalIssue = Issue.class.cast(find(HistoricalIssue.class, id).orNull());
            issue = historicalIssue != null ? Optional.<Issue>of(historicalIssue) : Optional.<Issue>absent();
        }
        return issue;
    }

    @Override
    public Optional<IssueStatus> findStatus(long id) {
        return find(IssueStatus.class, id);
    }

    @Override
    public Optional<IssueReason> findReason(long id) {
        return find(IssueReason.class, id);
    }

    @Override
    public Optional<IssueComment> findComment(long id) {
        return find(IssueComment.class, id);
    }

    @Override
    public Optional<IssueType> findIssueType(String uuid) {
        return find(IssueType.class, uuid);
    }

    @Override
    public IssueAssignee findIssueAssignee(String type, long id) {
        AssigneeTypes assigneeType = AssigneeTypes.fromString(type);
        if (assigneeType != null) {
            return assigneeType.getAssignee(this, userService, id);
        }
        return null;
    }

    @Override
    public boolean checkIssueAssigneeType(String type) {
        return AssigneeTypes.fromString(type) != null;
    }

    @Override
    public Optional<AssigneeRole> findAssigneeRole(long id) {
        return find(AssigneeRole.class, id);
    }

    @Override
    public Optional<AssigneeTeam> findAssigneeTeam(long id) {
        return find(AssigneeTeam.class, id);
    }

    @Override
    public IssueStatus createStatus(String name, boolean isFinal) {
        IssueStatus status = dataModel.getInstance(IssueStatusImpl.class);
        status.setName(name);
        status.setFinal(isFinal);
        status.save();
        return status;
    }

    @Override
    public IssueReason createReason(String name, IssueType type) {
        IssueReason reason = dataModel.getInstance(IssueReasonImpl.class);
        reason.setName(name);
        reason.setIssueType(type);
        reason.save();
        return reason;
    }

    @Override
    public IssueType createIssueType(String typeUuid, String typeName) {
        IssueType issueType = dataModel.getInstance(IssueTypeImpl.class);
        issueType.setUUID(typeUuid);
        issueType.setName(typeName);
        issueType.save();
        return issueType;
    }

    @Override
    public AssigneeRole createAssigneeRole() {
        return dataModel.getInstance(AssigneeRoleImpl.class);
    }

    @Override
    public AssigneeTeam createAssigneeTeam() {
        return dataModel.getInstance(AssigneeTeamImpl.class);
    }

    private <T extends Entity> Optional<T> find(Class<T> clazz, Object... key) {
        return queryService.wrap(dataModel.query(clazz)).get(key);
    }

    @Override
    public <T extends Entity> Query<T> query(Class<T> clazz, Class<?>... eagers) {
        QueryExecutor<T> queryExecutor = dataModel.query(clazz, eagers);
        Query<T> query = queryService.wrap(queryExecutor);
        query.setEager();
        return query;
    }

    @Override
    public List<GroupByReasonEntity> getIssueGroupList(GroupQueryBuilder builder) {
        return GroupIssuesOperation.init(builder, this.dataModel).execute();
    }

    @Override
    public int countOpenDataCollectionIssues(String mRID) {
        Optional<IssueType> issueType = findIssueType("datacollection");
        if (issueType.isPresent()) {
            Condition condition = Where.where("reason.issueType").isEqualTo(issueType.get())
                    .and(where("device.mRID").isEqualTo(mRID));
            List<Issue> issues = dataModel.query(Issue.class, IssueReason.class, EndDevice.class)
                    .select(condition);
            return issues.size();
        } else {
            return 0;
        }
    }

}