package com.elster.jupiter.issue.impl.service;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.issue.impl.database.DatabaseConst;
import com.elster.jupiter.issue.impl.database.groups.GroupIssuesOperation;
import com.elster.jupiter.issue.impl.database.groups.IssueGroupColumns;
import com.elster.jupiter.issue.impl.drools.IssueForAssign;
import com.elster.jupiter.issue.impl.event.EventConst;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.issue.share.service.IssueAssignmentService;
import com.elster.jupiter.issue.share.service.IssueMainService;
import com.elster.jupiter.issue.share.service.IssueMappingService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.base.Optional;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.elster.jupiter.util.Checks.is;

@Component(name = "com.elster.jupiter.issue", service = IssueService.class)
public class IssueServiceImpl implements IssueService {
    private static Logger LOG = Logger.getLogger(IssueServiceImpl.class.getName());

    private volatile DataModel dataModel;
    private volatile QueryService queryService;
    private volatile MeteringService meteringService;
    private volatile UserService userService;

    private volatile IssueMainService issueMainService;
    private volatile IssueAssignmentService issueAssignmentService;

    public IssueServiceImpl() {
    }

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

    @Reference
    public void setIssueAssignmentService(IssueAssignmentService issueAssignmentService) {
        this.issueAssignmentService = issueAssignmentService;
    }

    @Override
    public List<GroupByReasonEntity> getIssueGroupList (String groupColumn, boolean isAsc, long from, long to, List<Long> id) {
        IssueGroupColumns groupBy = IssueGroupColumns.fromString(groupColumn);
        if (groupBy == null){
            return Collections.<GroupByReasonEntity>emptyList();
        }
        return GroupIssuesOperation.init(groupBy, this.dataModel)
                .setId(id.isEmpty() ? 0 : id.get(0)).setTo(to).setFrom(from).setOrderDirection(isAsc).execute();
    }

    @Override
    public OperationResult<String, String[]> closeIssue(long issueId, long version, IssueStatus newStatus, String comment, User author){
        OperationResult<String, String[]> result = new OperationResult<>();
        Optional<Issue> issueRef = issueMainService.get(Issue.class, issueId);
        if (!issueRef.isPresent()) {
            String title = "issue with id = " + String.valueOf(issueId);
            return result.setFail(new String[]{MessageSeeds.ISSUE_NOT_PRESENT.getDefaultFormat(), title});
        }
        Issue issueForClose = issueRef.get();
        if (version != issueForClose.getVersion()) {
            return result.setFail(new String[]{MessageSeeds.ISSUE_ALREADY_CHANGED.getDefaultFormat(), issueForClose.getTitle()});
        }
        HistoricalIssue historicalIssue = new HistoricalIssue();
        historicalIssue.copy(issueForClose);
        historicalIssue.setStatus(newStatus);
        issueMainService.save(historicalIssue);
        issueMainService.delete(issueForClose);
        if(!is(comment).emptyOrOnlyWhiteSpace()) {
            issueMainService.save(new IssueComment(issueId, comment, author));
        }
        return result;
    }

    @Override
    public IssueAssignee getAssigneeFromRule(Rule rule) {
        if (rule != null && rule.getRuleBody() != null) {
            Pattern pattern = Pattern.compile("IssueAssigneeType\\.(\\w+)\\s*\\)");
            Matcher matcher = pattern.matcher(rule.getRuleBody());
            if (matcher.find()) {
                String assigneeTypeStr = matcher.group(1);

                pattern = Pattern.compile("\\.setAssigneeId\\s*\\(\\s*(\\d+)\\s*\\);");
                matcher = pattern.matcher(rule.getRuleBody());
                if (matcher.find()) {
                    String assigneeIdStr = matcher.group(1);
                    long assigneeId = assigneeIdStr != null ? Long.parseLong(assigneeIdStr) : 0;
                    return buildIssueAssignee(IssueAssigneeType.valueOf(assigneeTypeStr), assigneeId);
                }
            }
        }
        return null;
    }

    private IssueAssignee buildIssueAssignee(IssueAssigneeType assigneeType, long assigneeId) {
        boolean correctAssignee = false;
        IssueAssignee assignee = new IssueAssignee();
        if (assigneeType != null) {
            switch (assigneeType) {
                case USER:
                    User user = userService.getUser(assigneeId).orNull();
                    if (user != null) {
                        correctAssignee = true;
                        assignee.setType(IssueAssigneeType.USER);
                        assignee.setUser(user);
                    }
                    break;
                case ROLE:
                    Optional<AssigneeRole> role = issueMainService.get(AssigneeRole.class, assigneeId);
                    if (role.isPresent()) {
                        correctAssignee = true;
                        assignee.setType(IssueAssigneeType.ROLE);
                        assignee.setRole(role.get());
                    }
                    break;

                case TEAM:
                    Optional<AssigneeTeam> team = issueMainService.get(AssigneeTeam.class, assigneeId);
                    if (team.isPresent()) {
                        correctAssignee = true;
                        assignee.setType(IssueAssigneeType.TEAM);
                        assignee.setTeam(team.get());
                    }
                    break;
            }
        }
        if (!correctAssignee) {
            return null;
        }
        return assignee;
    }

    @Override
    public OperationResult<String, String[]> assignIssue(long issueId, long version, IssueAssigneeType type, long assignId, String comment, User author) {
        OperationResult<String, String[]> result = new OperationResult<>();
        Optional<Issue> issueRef = issueMainService.get(Issue.class, issueId);
        if (!issueRef.isPresent()) {
            String title = "issue with id = " + String.valueOf(issueId);
            return result.setFail(new String[]{MessageSeeds.ISSUE_NOT_PRESENT.getDefaultFormat(), title});
        }
        Issue issueForAssign = issueRef.get();
        if (version != issueForAssign.getVersion()) {
            return result.setFail(new String[]{MessageSeeds.ISSUE_ALREADY_CHANGED.getDefaultFormat(), issueForAssign.getTitle()});
        }
        IssueAssignee newAssignee = buildIssueAssignee(type, assignId);
        if (newAssignee == null) {
            return result.setFail(new String[]{MessageSeeds.ISSUE_ASSIGNEE_BAD.getDefaultFormat(), issueForAssign.getTitle()});
        }
        Issue issueImpl = Issue.class.cast(issueForAssign);
        issueImpl.setAssignee(newAssignee);
        dataModel.mapper(Issue.class).update(issueImpl);
        if (!is(comment).emptyOrOnlyWhiteSpace()) {
            issueMainService.save(new IssueComment(issueId, comment, author));
        }
        return result;
    }

    @Override
    public void processAutoAssign(Issue issue) {
        if (issue != null) {
            IssueForAssign issueForAssign = new IssueForAssign(this);
            issueForAssign.setId(issue.getId());
            issueForAssign.setVersion(issue.getVersion());
            issueForAssign.setReason(issue.getReason().getName());

            // Try to fetch outage region and customer
            // TODO avoid copy-paste from Device Info
            EndDevice endDevice = issue.getDevice();
            if (endDevice != null && Meter.class.isInstance(endDevice)) {
                Meter meter = Meter.class.cast(endDevice);
                Optional<MeterActivation> meterActivation = meter.getCurrentMeterActivation();
                if (meterActivation.isPresent()) {
                    Optional<UsagePoint> usagePointRef = meterActivation.get().getUsagePoint();
                    if (usagePointRef.isPresent()) {
                        UsagePoint usagePoint = usagePointRef.get();
                        issueForAssign.setOutageRegion(usagePoint.getOutageRegion());
                        Optional<Party> partyRef = usagePoint.getCustomer(new Date());
                        if (partyRef.isPresent()) {
                            issueForAssign.setCustomer(partyRef.get().getName());
                        }
                    }
                }
            }

            // Send to the Drools Engine for further processing
            issueAssignmentService.assignIssue(Collections.singletonList(issueForAssign));
        }
    }

    @Override
    public Optional<Issue> createIssue(Map<?, ?> map) {
        String statusName = "open"; // TODO Hardcoded
        String reasonTopic = String.class.cast(map.get(EventConst.EVENT_TOPICS));

        IssueStatus status = new IssueStatus();
        status.setName(statusName);
        Optional<IssueStatus> statusRef = issueMainService.searchFirst(status);
        if (!statusRef.isPresent()) {
            LOG.severe("Issue creation failed due to unexpected status value value: " + statusName);
            return Optional.<Issue>absent();
        } else {
            status = statusRef.get();
        }

        IssueReason reason = new IssueReason();
        reason.setTopic(reasonTopic);
        Optional<IssueReason> reasonRef = issueMainService.searchFirst(reason);
        if (!reasonRef.isPresent()) {
            LOG.severe("Issue creation failed due to unexpected reason topic value: " + reasonTopic);
            return Optional.<Issue>absent();
        } else {
            reason = reasonRef.get();
        }

        Issue issue = new Issue();
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

        Optional<Issue> result = issueMainService.save(issue);
        this.processAutoAssign(issue);
        return result;
    }
}