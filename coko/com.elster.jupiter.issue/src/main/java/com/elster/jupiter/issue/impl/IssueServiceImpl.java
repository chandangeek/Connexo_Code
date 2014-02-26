package com.elster.jupiter.issue.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.issue.*;
import com.elster.jupiter.issue.database.GroupingOperation;
import com.elster.jupiter.issue.database.TableSpecs;
import com.elster.jupiter.issue.module.Installer;
import com.elster.jupiter.issue.module.MessageSeeds;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Component(name = "com.elster.jupiter.issue", service = {IssueService.class, InstallService.class}, property = "name=" + IssueService.COMPONENT_NAME)
public class IssueServiceImpl implements IssueService, InstallService {
    private volatile DataModel dataModel;
    private volatile OrmService ormService;
    private volatile QueryService queryService;
    private volatile MeteringService meteringService;
    private volatile UserService userService;
    private static Logger LOG = Logger.getLogger(IssueServiceImpl.class.getName());

    public IssueServiceImpl(){
    }

    @Inject
    public IssueServiceImpl(OrmService ormService, QueryService queryService, MeteringService meteringService, UserService userService) {
        setOrmService(ormService);
        setQueryService(queryService);
        setMeteringService(meteringService);
        setUserService(userService);
        activate();
        if (!dataModel.isInstalled()) {
            install();
        }
    }

    @Activate
    public void activate() {
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(OrmService.class).toInstance(ormService);
                bind(QueryService.class).toInstance(queryService);
                bind(MeteringService.class).toInstance(meteringService);
                bind(UserService.class).toInstance(userService);
            }
        });
    }

    @Override
    public void install() {
        new Installer(this, this.dataModel).install(true, false);
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
        dataModel = ormService.newDataModel(IssueService.COMPONENT_NAME, "Issue Management");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    @Reference
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Optional<Issue> getIssueById(long issueId) {
        Query<Issue> query = queryService.wrap(dataModel.query(Issue.class));
        return query.get(issueId);
    }

    @Override
    public Optional<IssueReason> getIssueReasonById(long reasonId) {
        Query<IssueReason> query = queryService.wrap(dataModel.query(IssueReason.class));
        return query.get(reasonId);
    }

    public void createIssueReason(String reasonName){
        if (reasonName != null && reasonName.length() > 0){
            IssueReason newReason = IssueReasonImpl.from(this.dataModel, "", reasonName);
            dataModel.mapper(IssueReason.class).persist(newReason);
        }
    }

    @Override
    public Optional<IssueStatus> getIssueStatusById(long statusId) {
        Query<IssueStatus> query = queryService.wrap(dataModel.query(IssueStatus.class));
        return query.get(statusId);
    }

    @Override
    public IssueStatus getIssueStatusFromString(String status) {
        if (status != null && status.length() > 0){
            Query<IssueStatus> query = queryService.wrap(dataModel.query(IssueStatus.class));
            Condition condition = Where.where("name").isEqualToIgnoreCase(status);
            List<IssueStatus> allStatuses = query.select(condition);
            if (allStatuses != null && allStatuses.size() > 0){
                return allStatuses.get(0);
            }
        }
        return null;
    }

    public void createIssueStatus(String statusName){
        if (statusName != null && statusName.length() > 0){
            IssueStatus newStatus = IssueStatusImpl.from(this.dataModel, statusName);
            dataModel.mapper(IssueStatus.class).persist(newStatus);
        }
    }

    private AssigneeRole getAssigneeRole(long roleId){
        Query<AssigneeRole> query = queryService.wrap(dataModel.query(AssigneeRole.class));
        return query.get(roleId).orNull();
    }

    private AssigneeTeam getAssigneeTeam(long teamId){
        Query<AssigneeTeam> query = queryService.wrap(dataModel.query(AssigneeTeam.class));
        return query.get(teamId).orNull();
    }

    @Override
    public Query<Issue> getIssueListQuery() {
        return queryService.wrap(dataModel.query(Issue.class, IssueReason.class, IssueStatus.class));
    }

    @Override
    public Query<AssigneeRole> getAssigneeRoleListQuery() {
        return queryService.wrap(dataModel.query(AssigneeRole.class));
    }

    @Override
    public Query<AssigneeTeam> getAssigneeTeamListQuery() {
        return queryService.wrap(dataModel.query(AssigneeTeam.class));
    }

    @Override
    public Map<String, Long> getIssueGroupList (String groupColumn, boolean isAsc, long start, long limit) {
        IssueGroupColumns orderBy = IssueGroupColumns.fromString(groupColumn);
        if (orderBy == null){
            return Collections.<String, Long>emptyMap();
        }
        Map<String, Long> groups = GroupingOperation.init(this.dataModel)
                .setLimit(limit)
                .setStart(start)
                .setGroupColumn(orderBy)
                .setOrderDirection(isAsc)
                .execute();
        return groups;
    }

    @Override
    public OperationResult<String, String[]> closeIssue(long issueId, long version, IssueStatus newStatus, String comment){
        Issue issueForClose = this.getIssueById(issueId).orNull();
        OperationResult<String, String[]> result = new OperationResult<>();
        if (issueForClose == null){
            String title = "issue with id = " + String.valueOf(issueId);
            return result.setFail(new String[]{MessageSeeds.ISSUE_NOT_PRESENT.getDefaultFormat(), title});
        }
        if (version != issueForClose.getVersion()){
            return result.setFail(new String[]{MessageSeeds.ISSUE_ALREADY_CHANGED.getDefaultFormat(), issueForClose.getTitle()});
        }
        // TODO may be it will better to extract this code to separate method (update(Issue issue) for example)
        IssueImpl.class.cast(issueForClose).setStatus(newStatus);
        HistoricalIssue historicalIssue = new HistoricalIssueImpl(issueForClose);
        dataModel.mapper(HistoricalIssue.class).persist(historicalIssue);
        dataModel.mapper(Issue.class).remove(issueForClose);
        return result;
    }

    @Override
    public OperationResult<String, String[]> assignIssue(long issueId, long version, IssueAssigneeType type, long assignId, String comment) {
        Issue issueForAssign = this.getIssueById(issueId).orNull();
        OperationResult<String, String[]> result = new OperationResult<>();
        if (issueForAssign == null){
            String title = "issue with id = " + String.valueOf(issueId);
            return result.setFail(new String[]{MessageSeeds.ISSUE_NOT_PRESENT.getDefaultFormat(), title});
        }
        if (version != issueForAssign.getVersion()){
            return result.setFail(new String[]{MessageSeeds.ISSUE_ALREADY_CHANGED.getDefaultFormat(), issueForAssign.getTitle()});
        }
        boolean correctAssignee = false;
        IssueAssigneeImpl newAssignee = new IssueAssigneeImpl();
        if (type != null){
            switch (type){
                case USER:
                    User user = userService.getUser(assignId).orNull();
                    if (user != null){
                        correctAssignee = true;
                        newAssignee.setType(IssueAssigneeType.USER);
                        newAssignee.setUser(user);
                    }
                    break;
                case ROLE:
                    AssigneeRole role = this.getAssigneeRole(assignId);
                    if (role != null){
                        correctAssignee = true;
                        newAssignee.setType(IssueAssigneeType.ROLE);
                        newAssignee.setRole(role);
                    }
                    break;

                case TEAM:
                    AssigneeTeam team = this.getAssigneeTeam(assignId);
                    if (team != null){
                        correctAssignee = true;
                        newAssignee.setType(IssueAssigneeType.TEAM);
                        newAssignee.setTeam(team);
                    }
                    break;
            }
        }
        if (!correctAssignee){
            return result.setFail(new String[]{MessageSeeds.ISSUE_ASSIGNEE_BAD.getDefaultFormat(), issueForAssign.getTitle()});
        }
        IssueImpl issueImpl = IssueImpl.class.cast(issueForAssign);
        issueImpl.setAssignee(newAssignee);
        dataModel.mapper(Issue.class).update(issueImpl);
        return result;
    }
}