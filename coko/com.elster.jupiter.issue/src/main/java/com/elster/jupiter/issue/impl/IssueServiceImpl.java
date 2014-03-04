package com.elster.jupiter.issue.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.*;
import com.elster.jupiter.issue.database.DatabaseConst;
import com.elster.jupiter.issue.database.GroupingOperation;
import com.elster.jupiter.issue.database.TableSpecs;
import com.elster.jupiter.issue.event.EventConst;
import com.elster.jupiter.issue.module.Installer;
import com.elster.jupiter.issue.module.MessageSeeds;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import org.joda.time.DateTime;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.elster.jupiter.util.Checks.is;

@Component(name = "com.elster.jupiter.issue", service = {IssueService.class, InstallService.class}, property = "name=" + IssueService.COMPONENT_NAME)
public class IssueServiceImpl implements IssueService, InstallService {
    private volatile DataModel dataModel;
    private volatile OrmService ormService;
    private volatile QueryService queryService;
    private volatile MeteringService meteringService;
    private volatile UserService userService;
    private MessageService messageService;
   /* private AppService appService;
    private CronExpressionParser cronExpressionParser;*/
    private static Logger LOG = Logger.getLogger(IssueServiceImpl.class.getName());
    // TODO delete eventService
    private volatile EventService eventService;

    public IssueServiceImpl(){
    }

    @Inject
    public IssueServiceImpl(OrmService ormService, QueryService queryService, MeteringService meteringService,
                            UserService userService, EventService eventService, MessageService messageService/*,
                            AppService appService, CronExpressionParser cronExpressionParser*/) {
        setOrmService(ormService);
        setQueryService(queryService);
        setMeteringService(meteringService);
        setUserService(userService);
        setMessageService(messageService);
        //TODO delete when events will be defined by MDC
        setEventService(eventService);
        // --END delete when events will be defined by MDC
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
                bind(MessageService.class).toInstance(messageService);
                /*bind(AppService.class).toInstance(appService);
                bind(CronExpressionParser.class).toInstance(cronExpressionParser);*/
                //TODO delete when events will be defined by MDC
                bind(EventService.class).toInstance(eventService);
                //---END delete when events will be defined by MDC
            }
        });
    }

    @Override
    public void install() {
        new Installer(this, this.dataModel, messageService/*, appService, cronExpressionParser*/).install(true, false);
        //TODO delete when events will be defined by MDC
        setEventTopics();

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

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    /*@Reference
    public void setAppService(AppService appService) {
        this.appService = appService;
    }

    @Reference
    public void setCronExpressionParser(CronExpressionParser cronExpressionParser) {
        this.cronExpressionParser = cronExpressionParser;
    }*/

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

    public void createIssueReason(String reasonName, String reasonTopic){
        if (is(reasonName).empty() || is(reasonTopic).empty()) {
            LOG.severe("Reason creation failed. Please provide not null values for \'name\' and \'topic\' fields.");
            return;
        }
        IssueReason newReason = IssueReasonImpl.from(this.dataModel, reasonTopic, reasonName);
        dataModel.mapper(IssueReason.class).persist(newReason);
    }

    @Override
    public IssueReason getIssueReasonFromTopic(String topic) {
        if (!is(topic).empty()) {
            Query<IssueReason> query = queryService.wrap(dataModel.query(IssueReason.class));
            Condition condition = Where.where("topic").isEqualToIgnoreCase(topic);
            List<IssueReason> issueReasons = query.select(condition);
            if (!issueReasons.isEmpty()){
                return issueReasons.get(0);
            }
        }
        return null;
    }

    @Override
    public IssueReason getIssueReasonFromName(String name) {
        if (!is(name).empty()){
            Query<IssueReason> query = queryService.wrap(dataModel.query(IssueReason.class));
            Condition condition = Where.where("name").isEqualToIgnoreCase(name);
            List<IssueReason> issueReasons = query.select(condition);
            if (!issueReasons.isEmpty()){
                return issueReasons.get(0);
            }
        }
        return null;
    }

    @Override
    public Optional<IssueStatus> getIssueStatusById(long statusId) {
        Query<IssueStatus> query = queryService.wrap(dataModel.query(IssueStatus.class));
        return query.get(statusId);
    }

    @Override
    public IssueStatus getIssueStatusFromString(String status) {
        if (!is(status).empty()){
            Query<IssueStatus> query = queryService.wrap(dataModel.query(IssueStatus.class));
            Condition condition = Where.where("name").isEqualToIgnoreCase(status);
            List<IssueStatus> allStatuses = query.select(condition);
            if (!allStatuses.isEmpty()){
                return allStatuses.get(0);
            }
        }
        return null;
    }

    public void createIssueStatus(String statusName){
        if (is(statusName).empty()) {
            LOG.severe("Status creation failed. Please provide not null value for status \'name\' field.");
            return;
        }
        IssueStatus newStatus = IssueStatusImpl.from(this.dataModel, statusName);
        dataModel.mapper(IssueStatus.class).persist(newStatus);
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
        IssueImpl issueImpl = IssueImpl.class.cast(issueForClose);
        issueImpl.setStatus(newStatus);
        HistoricalIssue historicalIssue = HistoricalIssueImpl.from(this.dataModel, issueImpl);
        dataModel.mapper(HistoricalIssue.class).persist(historicalIssue);
        dataModel.mapper(Issue.class).remove(issueForClose);
        return result;
    }

    private IssueAssigneeImpl buildIssueAssignee(IssueAssigneeType assigneeType, long assigneeId){
        boolean correctAssignee = false;
        IssueAssigneeImpl assignee = new IssueAssigneeImpl();
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
                    AssigneeRole role = this.getAssigneeRole(assigneeId);
                    if (role != null){
                        correctAssignee = true;
                        assignee.setType(IssueAssigneeType.ROLE);
                        assignee.setRole(role);
                    }
                    break;

                case TEAM:
                    AssigneeTeam team = this.getAssigneeTeam(assigneeId);
                    if (team != null){
                        correctAssignee = true;
                        assignee.setType(IssueAssigneeType.TEAM);
                        assignee.setTeam(team);
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
        Issue issueForAssign = this.getIssueById(issueId).orNull();
        OperationResult<String, String[]> result = new OperationResult<>();
        if (issueForAssign == null){
            String title = "issue with id = " + String.valueOf(issueId);
            return result.setFail(new String[]{MessageSeeds.ISSUE_NOT_PRESENT.getDefaultFormat(), title});
        }
        if (version != issueForAssign.getVersion()){
            return result.setFail(new String[]{MessageSeeds.ISSUE_ALREADY_CHANGED.getDefaultFormat(), issueForAssign.getTitle()});
        }
        IssueAssigneeImpl newAssignee = buildIssueAssignee(type, assignId);
        if (newAssignee == null){
            return result.setFail(new String[]{MessageSeeds.ISSUE_ASSIGNEE_BAD.getDefaultFormat(), issueForAssign.getTitle()});
        }
        IssueImpl issueImpl = IssueImpl.class.cast(issueForAssign);
        issueImpl.setAssignee(newAssignee);
        dataModel.mapper(Issue.class).update(issueImpl);
        return result;
    }

    @Override
    public void createIssue(Map<?, ?> map) {
        IssueImpl newIssue = new IssueImpl(dataModel);
        IssueReason reason = getIssueReasonFromTopic(String.class.cast(map.get(EventConst.EVENT_TOPICS)));
        if(reason == null) {
            LOG.severe("Issue creation failed due to unexpected reason topic value: " +  String.class.cast(map.get(EventConst.EVENT_TOPICS)));
            return;
        }
        newIssue.setReason(reason);
        newIssue.setStatus(getIssueStatusFromString("Open"));
        //TODO specify due date setting rules
        newIssue.setDueDate(new UtcInstant(1400075425000L));
        newIssue.setAssignee(buildIssueAssignee(IssueAssigneeType.USER, 1L));

        String amrId = String.class.cast(map.get(EventConst.DEVICE_IDENTIFIER));
        Optional<AmrSystem> amrSystemRef = meteringService.findAmrSystem(DatabaseConst.MDC_AMR_SYSTEM_ID);
        if (amrSystemRef.isPresent()) {
            Optional<Meter> meterRef =  amrSystemRef.get().findMeter(amrId);
            if(meterRef.isPresent()) {
                newIssue.setDevice(meterRef.get());
            }
        }
        dataModel.mapper(Issue.class).persist(newIssue);
    }

    //TODO delete when events will be defined by MDC ----------------
    @Reference
    public  void  setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    private void setEventTopics() {
        for (IssueEventType eventType : IssueEventType.values()) {
            try {
                eventType.install(eventService);
            } catch (Exception e) {
                System.out.println("Could not create event type : " + eventType.name());
            }
        }
    }

    public void getEvent() {
        eventService.postEvent(IssueEventType.DEVICE_COMMUNICATION_FAILURE.topic(), new CreateIssueEvent());
    }
    //----END delete when events will be defined by MDC-------------------
}