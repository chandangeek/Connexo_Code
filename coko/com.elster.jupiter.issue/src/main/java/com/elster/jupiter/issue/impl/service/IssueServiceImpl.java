package com.elster.jupiter.issue.impl.service;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.issue.impl.database.DatabaseConst;
import com.elster.jupiter.issue.impl.database.groups.GroupIssuesOperation;
import com.elster.jupiter.issue.impl.database.groups.IssueGroupColumns;
import com.elster.jupiter.issue.impl.event.EventConst;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.issue.share.service.IssueMainService;
import com.elster.jupiter.issue.share.service.IssueMappingService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.base.Optional;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

@Component(name = "com.elster.jupiter.issue", service = IssueService.class)
public class IssueServiceImpl implements IssueService {
    private static Logger LOG = Logger.getLogger(IssueServiceImpl.class.getName());

    private volatile DataModel dataModel;
    private volatile QueryService queryService;
    private volatile IssueMainService issueMainService;
    private volatile MeteringService meteringService;
    private volatile UserService userService;

    public IssueServiceImpl(){}

    @Inject
    public IssueServiceImpl(MeteringService meteringService,
                            UserService userService,
                            QueryService queryService,
                            IssueMainService issueMainService,
                            IssueMappingService issueMappingService) {
        setQueryService(queryService);
        setMeteringService(meteringService);
        setUserService(userService);
        setIssueInternalService(issueMappingService);
        setIssueMainService(issueMainService);
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
    @Reference
    public void setIssueMainService(IssueMainService issueMainService) {
        this.issueMainService = issueMainService;
    }
    @Reference
    public void setIssueInternalService(IssueMappingService issueMappingService) {
        dataModel = IssueMappingServiceImpl.class.cast(issueMappingService).getDataModel();
    }


    public Query<Issue> getIssueListQuery() {
        QueryExecutor<Issue> queryExecutor = dataModel.query(Issue.class, IssueReason.class,
                IssueStatus.class, AssigneeRole.class, AssigneeTeam.class);
        Query<Issue> query = queryService.wrap(queryExecutor);
        query.setEager();
        return query;
    }

    @Override
    public Map<String, Long> getIssueGroupList (String groupColumn, boolean isAsc, long from, long to) {
        IssueGroupColumns groupBy = IssueGroupColumns.fromString(groupColumn);
        if (groupBy == null){
            return Collections.<String, Long>emptyMap();
        }
        return GroupIssuesOperation.init(groupBy, this.dataModel)
                .setTo(to).setFrom(from).setOrderDirection(isAsc).execute();
    }

    @Override
    public OperationResult<String, String[]> closeIssue(long issueId, long version, IssueStatus newStatus, String comment){
        OperationResult<String, String[]> result = new OperationResult<>();
        Optional<Issue> issueRef = issueMainService.get(Issue.class, issueId);
        if (!issueRef.isPresent()){
            String title = "issue with id = " + String.valueOf(issueId);
            return result.setFail(new String[]{MessageSeeds.ISSUE_NOT_PRESENT.getDefaultFormat(), title});
        }
        Issue issueForClose = issueRef.get();
        if (version != issueForClose.getVersion()){
            return result.setFail(new String[]{MessageSeeds.ISSUE_ALREADY_CHANGED.getDefaultFormat(), issueForClose.getTitle()});
        }
        HistoricalIssue historicalIssue = new HistoricalIssue();
        historicalIssue.copy(issueForClose);
        historicalIssue.setStatus(newStatus);
        issueMainService.save(historicalIssue);
        issueMainService.delete(issueForClose);
        return result;
    }

    private IssueAssignee buildIssueAssignee(IssueAssigneeType assigneeType, long assigneeId){
        boolean correctAssignee = false;
        IssueAssignee assignee = new IssueAssignee();
        if (assigneeType != null){
            switch (assigneeType){
                case USER:
                    User user = userService.getUser(assigneeId).orNull();
                    if (user != null){
                        correctAssignee = true;
                        assignee.setType(IssueAssigneeType.USER);
                        assignee.setUser(user);
                    }
                    break;
                case ROLE:
                    Optional<AssigneeRole> role = issueMainService.get(AssigneeRole.class, assigneeId);
                    if (role.isPresent()){
                        correctAssignee = true;
                        assignee.setType(IssueAssigneeType.ROLE);
                        assignee.setRole(role.get());
                    }
                    break;

                case TEAM:
                    Optional<AssigneeTeam> team = issueMainService.get(AssigneeTeam.class, assigneeId);
                    if (team.isPresent()){
                        correctAssignee = true;
                        assignee.setType(IssueAssigneeType.TEAM);
                        assignee.setTeam(team.get());
                    }
                    break;
            }
        }
        if (!correctAssignee){
            return null;
        }
        return assignee;
    }

    @Override
    public OperationResult<String, String[]> assignIssue(long issueId, long version, IssueAssigneeType type, long assignId, String comment) {
        OperationResult<String, String[]> result = new OperationResult<>();
        Optional<Issue> issueRef = issueMainService.get(Issue.class, issueId);
        if (!issueRef.isPresent()){
            String title = "issue with id = " + String.valueOf(issueId);
            return result.setFail(new String[]{MessageSeeds.ISSUE_NOT_PRESENT.getDefaultFormat(), title});
        }
        Issue issueForAssign = issueRef.get();
        if (version != issueForAssign.getVersion()){
            return result.setFail(new String[]{MessageSeeds.ISSUE_ALREADY_CHANGED.getDefaultFormat(), issueForAssign.getTitle()});
        }
        IssueAssignee newAssignee = buildIssueAssignee(type, assignId);
        if (newAssignee == null){
            return result.setFail(new String[]{MessageSeeds.ISSUE_ASSIGNEE_BAD.getDefaultFormat(), issueForAssign.getTitle()});
        }
        Issue issueImpl = Issue.class.cast(issueForAssign);
        issueImpl.setAssignee(newAssignee);
        dataModel.mapper(Issue.class).update(issueImpl);
        return result;
    }

    @Override
    public Optional<Issue> createIssue(Map<?, ?> map) {
        String statusName = "open"; // TODO Hardcoded
        String reasonTopic = String.class.cast(map.get(EventConst.EVENT_TOPICS));

        IssueStatus status = new IssueStatus();
        status.setName(statusName);
        Optional<IssueStatus> statusRef = issueMainService.searchFirst(status);
        if(!statusRef.isPresent()) {
            LOG.severe("Issue creation failed due to unexpected status value value: " +  statusName);
            return Optional.<Issue>absent();
        } else {
            status = statusRef.get();
        }

        IssueReason reason = new IssueReason();
        reason.setTopic(reasonTopic);
        Optional<IssueReason> reasonRef = issueMainService.searchFirst(reason);
        if(!reasonRef.isPresent()) {
            LOG.severe("Issue creation failed due to unexpected reason topic value: " +  reasonTopic);
            return Optional.<Issue>absent();
        } else {
            reason = reasonRef.get();
        }

        Issue issue = new Issue();
        issue.setReason(reason);
        issue.setStatus(status);
        //TODO specify due date setting rules
        issue.setDueDate(new UtcInstant(1393316133000L));
        issue.setAssignee(buildIssueAssignee(IssueAssigneeType.USER, 1L));

        String amrId = String.class.cast(map.get(EventConst.DEVICE_IDENTIFIER));
        Optional<AmrSystem> amrSystemRef = meteringService.findAmrSystem(DatabaseConst.MDC_AMR_SYSTEM_ID);
        if (amrSystemRef.isPresent()) {
            Optional<Meter> meterRef =  amrSystemRef.get().findMeter(amrId);
            if(meterRef.isPresent()) {
                issue.setDevice(meterRef.get());
            }
        }
        return issueMainService.save(issue);
    }
}