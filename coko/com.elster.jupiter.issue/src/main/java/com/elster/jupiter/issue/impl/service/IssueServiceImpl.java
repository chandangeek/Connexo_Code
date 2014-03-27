package com.elster.jupiter.issue.impl.service;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.issue.impl.database.DatabaseConst;
import com.elster.jupiter.issue.impl.database.groups.GroupIssuesOperation;
import com.elster.jupiter.issue.impl.event.EventConst;
import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.issue.share.service.GroupQueryBuilder;
import com.elster.jupiter.issue.share.service.IssueAssignmentService;
import com.elster.jupiter.issue.share.service.IssueMappingService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.base.Optional;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.elster.jupiter.issue", service = IssueService.class)
public class IssueServiceImpl implements IssueService {
    private static Logger LOG = Logger.getLogger(IssueServiceImpl.class.getName());

    private volatile DataModel dataModel;
    private volatile QueryService queryService;
    private volatile MeteringService meteringService;
    private volatile UserService userService;

    private volatile IssueAssignmentService issueAssignmentService;

    public IssueServiceImpl() {
    }

    @Inject
    public IssueServiceImpl(MeteringService meteringService,
                            UserService userService,
                            QueryService queryService,
                            IssueMappingService issueMappingService) {
        setQueryService(queryService);
        setMeteringService(meteringService);
        setUserService(userService);
        setIssueInternalService(issueMappingService);
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
    public void setIssueInternalService(IssueMappingService issueMappingService) {
        dataModel = IssueMappingServiceImpl.class.cast(issueMappingService).getDataModel();
    }

    @Reference
    public void setIssueAssignmentService(IssueAssignmentService issueAssignmentService) {
        this.issueAssignmentService = issueAssignmentService;
    }

    @Override
    public Optional<Issue> findIssue(long id) {
        return findIssue(id, false);
    }

    @Override
    public Optional<Issue> findIssue(long id, boolean searchInHistory) {
        Optional<Issue> issue = find(Issue.class, id);
        if (!issue.isPresent() && searchInHistory){
            issue = Optional.of(Issue.class.cast(find(HistoricalIssue.class, id).orNull()));
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
    public Optional<AssigneeRole> findAssigneeRole(long id) {
        return find(AssigneeRole.class, id);
    }

    @Override
    public Optional<AssigneeTeam> findAssigneeTeam(long id) {
        return find(AssigneeTeam.class, id);
    }

    @Override
    public Issue createIssue() {
        return dataModel.getInstance(Issue.class);
    }

    @Override
    public IssueStatus createStatus(String name, boolean isFinal) {
        IssueStatus status = dataModel.getInstance(IssueStatus.class);
        status.setName(name);
        status.setFinal(isFinal);
        status.save();
        return status;
    }

    @Override
    public IssueReason createReason(String name, String topic) {
        IssueReason reason = dataModel.getInstance(IssueReason.class);
        reason.setName(name);
        reason.setTopic(topic);
        reason.save();
        return reason;
    }

    @Override
    public AssigneeRole createAssigneeRole() {
        return dataModel.getInstance(AssigneeRole.class);
    }

    @Override
    public AssigneeTeam createAssigneeTeam() {
        return dataModel.getInstance(AssigneeTeam.class);
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
    public List<GroupByReasonEntity> getIssueGroupList (GroupQueryBuilder builder) {
        return GroupIssuesOperation.init(builder, this.dataModel).execute();
    }

    @Override
    public Optional<Issue> createIssue(Map<?, ?> map) {
        String statusName = "open"; // TODO Hardcoded
        String reasonTopic = String.class.cast(map.get(EventConst.EVENT_TOPICS));

        IssueStatus status = null;
        Query<IssueStatus> statusQuery = query(IssueStatus.class);
        List<IssueStatus> statusList = statusQuery.select(where("name").isEqualToIgnoreCase(statusName));
        if (statusList.isEmpty()){
            LOG.severe("Issue creation failed due to unexpected status value value: " + statusName);
            return Optional.absent();
        } else {
            status = statusList.get(0);
        }

        IssueReason reason = null;
        Query<IssueReason> reasonQuery = query(IssueReason.class);
        List<IssueReason> reasonList = reasonQuery.select(where("topic").isEqualToIgnoreCase(reasonTopic));
        if (reasonList.isEmpty()){
            LOG.severe("Issue creation failed due to unexpected reason topic value: " + reasonTopic);
            return Optional.absent();
        } else {
            reason = reasonList.get(0);
        }

        Issue issue = createIssue();
        issue.setReason(reason);
        issue.setStatus(status);
        //TODO specify due date setting rules
        issue.setDueDate(new UtcInstant(1393316133000L));

        String amrId = String.class.cast(map.get(EventConst.DEVICE_IDENTIFIER));
        Optional<AmrSystem> amrSystemRef = meteringService.findAmrSystem(DatabaseConst.MDC_AMR_SYSTEM_ID);
        if (amrSystemRef.isPresent()) {
            Optional<Meter> meterRef = amrSystemRef.get().findMeter(amrId);
            if (meterRef.isPresent()) {
                issue.setDevice(meterRef.get());
            }
        }

        issue.save();
        issue.autoAssign();
        return Optional.of(issue);
    }
}