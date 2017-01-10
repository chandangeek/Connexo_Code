package com.energyict.mdc.device.alarms.rest.resource;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.request.CreateCommentRequest;
import com.elster.jupiter.issue.rest.request.IssueDueDateInfo;
import com.elster.jupiter.issue.rest.request.IssueDueDateInfoAdapter;
import com.elster.jupiter.issue.rest.request.PerformActionRequest;
import com.elster.jupiter.issue.rest.resource.StandardParametersBean;
import com.elster.jupiter.issue.rest.response.ActionInfo;
import com.elster.jupiter.issue.rest.response.IssueCommentInfo;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.issue.share.entity.IssueComment;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.energyict.mdc.device.alarms.DeviceAlarmFilter;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.alarms.rest.i18n.MessageSeeds;
import com.energyict.mdc.device.alarms.rest.response.DeviceAlarmActionInfo;
import com.energyict.mdc.device.alarms.rest.response.DeviceAlarmInfo;
import com.energyict.mdc.device.alarms.rest.response.DeviceAlarmInfoFactory;
import com.energyict.mdc.device.alarms.rest.transactions.AssignToMeSingleDeviceAlarmTransaction;
import com.energyict.mdc.device.alarms.rest.transactions.UnassignSingleDeviceAlarmTransaction;
import com.energyict.mdc.device.alarms.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;


@Path("/alarms")
public class DeviceAlarmResource extends BaseAlarmResource{

    private final DeviceAlarmInfoFactory deviceAlarmInfoFactory;
    private final ConcurrentModificationExceptionFactory conflictFactory;

    @Inject
    public DeviceAlarmResource(DeviceAlarmInfoFactory deviceAlarmInfoFactory, ConcurrentModificationExceptionFactory conflictFactory) {
        this.conflictFactory = conflictFactory;
        this.deviceAlarmInfoFactory = deviceAlarmInfoFactory;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ALARM, Privileges.Constants.ASSIGN_ALARM, Privileges.Constants.CLOSE_ALARM, Privileges.Constants.COMMENT_ALARM, Privileges.Constants.ACTION_ALARM})
    public PagedInfoList getAllDeviceAlarms(@BeanParam StandardParametersBean params, @BeanParam JsonQueryParameters queryParams, @BeanParam JsonQueryFilter filter){
        validateMandatory(params, "start", "limit");
        DeviceAlarmFilter alarmFilter = buildFilterFromQueryParameters(filter);
        Finder<? extends DeviceAlarm> finder = getDeviceAlarmService().findAlarms(alarmFilter);
        addSorting(finder, params);
        if (queryParams.getStart().isPresent() && queryParams.getLimit().isPresent()) {
            finder.paged(queryParams.getStart().get(), queryParams.getLimit().get());
        }
        List<? extends DeviceAlarm> deviceAlarms = finder.find();
        List<DeviceAlarmInfo> deviceAlarmInfos = deviceAlarms.stream()
                .map(deviceAlarmInfoFactory::asInfo)
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("data", deviceAlarmInfos, queryParams);
    }

    @GET @Transactional
    @Path("/{id}")
    @RolesAllowed({Privileges.Constants.VIEW_ALARM, Privileges.Constants.ASSIGN_ALARM, Privileges.Constants.CLOSE_ALARM, Privileges.Constants.COMMENT_ALARM, Privileges.Constants.ACTION_ALARM})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response getAlarmById(@PathParam("id") long id) {
        Optional<? extends DeviceAlarm> deviceAlarm = getDeviceAlarmService().findAlarm(id);
        return deviceAlarm.map(i -> Response.ok().entity(deviceAlarmInfoFactory.asInfo(i)).build())
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    @GET
    @Transactional
    @Path("/{id}/comments")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ALARM, Privileges.Constants.ASSIGN_ALARM, Privileges.Constants.CLOSE_ALARM, Privileges.Constants.COMMENT_ALARM, Privileges.Constants.ACTION_ALARM})
    public PagedInfoList getComments(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters) {
        DeviceAlarm deviceAlarm = getDeviceAlarmService().findAlarm(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return PagedInfoList.fromCompleteList("comments", getAlarmComments(deviceAlarm), queryParameters);
    }

    @POST
    @Transactional
    @Path("/{id}/comments")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.COMMENT_ALARM)
    public Response postComment(@PathParam("id") long id, CreateCommentRequest request, @Context SecurityContext securityContext) {
        DeviceAlarm deviceAlarm = getDeviceAlarmService().findAlarm(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        User author = (User) securityContext.getUserPrincipal();
        IssueComment comment = deviceAlarm.addComment(request.getComment(), author)
                .orElseThrow(() -> new WebApplicationException(Response.Status.BAD_REQUEST));
        return Response.ok(new IssueCommentInfo(comment)).status(Response.Status.CREATED).build();
    }

    @GET
    @Transactional
    @Path("/{id}/actions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ALARM, Privileges.Constants.ASSIGN_ALARM, Privileges.Constants.CLOSE_ALARM, Privileges.Constants.COMMENT_ALARM, Privileges.Constants.ACTION_ALARM})
    public PagedInfoList getActions(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters, @Context SecurityContext securityContext) {
        DeviceAlarm deviceAlarm = getDeviceAlarmService().findAlarm(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        List<DeviceAlarmActionInfo> issueActions = getListOfAvailableDeviceAlarmActions(deviceAlarm, securityContext);
        return PagedInfoList.fromCompleteList("issueActions", issueActions, queryParameters);
    }

    @GET
    @Transactional
    @Path("/{id}/actions/{key}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ALARM, Privileges.Constants.ASSIGN_ALARM, Privileges.Constants.CLOSE_ALARM, Privileges.Constants.COMMENT_ALARM, Privileges.Constants.ACTION_ALARM})
    public Response getActionTypeById(@PathParam("id") long id, @PathParam("key") long actionId, @Context SecurityContext securityContext) {
        DeviceAlarm deviceAlarm = getDeviceAlarmService().findAlarm(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return Response.ok(getDeviceAlarmActionById(deviceAlarm, actionId, securityContext)).build();
    }

    @PUT
    @Transactional
    @Path("/{id}/actions/{key}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ACTION_ALARM)
    public Response performAction(@PathParam("id") long id, @PathParam("key") long actionId, PerformActionRequest request) {
        DeviceAlarm deviceAlarm = getDeviceAlarmService().findAndLockDeviceAlarmByIdAndVersion(id, request.issue.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(request.issue.title)
                        .withActualVersion(() -> getDeviceAlarmService().findAlarm(id)
                                .map(DeviceAlarm::getVersion)
                                .orElse(null))
                        .supplier());
        request.id = actionId;
        return Response.ok(performIssueAction(deviceAlarm, request)).build();
    }

    @PUT
    @Path("/assigntome/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ACTION_ALARM)
    public Response postAssignToMe(@PathParam("id") long id, PerformActionRequest request, @Context SecurityContext securityContext) {
        getDeviceAlarmService().findAndLockDeviceAlarmByIdAndVersion(id, request.issue.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(request.issue.title)
                        .withActualVersion(() -> getDeviceAlarmService().findAlarm(id)
                                .map(DeviceAlarm::getVersion)
                                .orElse(null))
                        .supplier());
        User performer = (User) securityContext.getUserPrincipal();
        Function<ActionInfo, DeviceAlarm> issueProvider = result -> getDeviceAlarm(id, result);
        ActionInfo info = getTransactionService().execute(new AssignToMeSingleDeviceAlarmTransaction(performer, issueProvider, getThesaurus()));
        return Response.ok().entity(info).build();
    }

    @PUT
    @Path("/unassign/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ACTION_ALARM)
    public Response postUnassign(@PathParam("id") long id, PerformActionRequest request, @Context SecurityContext securityContext) {
        getDeviceAlarmService().findAndLockDeviceAlarmByIdAndVersion(id, request.issue.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(request.issue.title)
                        .withActualVersion(() -> getDeviceAlarmService().findAlarm(id)
                                .map(DeviceAlarm::getVersion)
                                .orElse(null))
                        .supplier());
        Function<ActionInfo, DeviceAlarm> issueProvider = result -> getDeviceAlarm(id, result);
        ActionInfo info = getTransactionService().execute(new UnassignSingleDeviceAlarmTransaction(issueProvider, getThesaurus()));
        return Response.ok().entity(info).build();
    }

    private DeviceAlarm getDeviceAlarm(Long id, ActionInfo result) {
        DeviceAlarm deviceAlarm = getDeviceAlarmService().findAlarm(id).orElse(null);
        if (deviceAlarm == null) {
            result.addFail(getThesaurus().getFormat(MessageSeeds.ALARM_DOES_NOT_EXIST).format(), id, "Alarm (id = " + id + ")");
        }
        return deviceAlarm;
    }

    public List<DeviceAlarmActionInfo> getListOfAvailableDeviceAlarmActions(DeviceAlarm deviceAlarm, SecurityContext securityContext) {
        return getListOfAvailableDeviceAlarmActionsTypes(deviceAlarm, securityContext).stream().map(actionType ->
                new DeviceAlarmActionInfo(deviceAlarm, actionType, getPropertyValueInfoService())).collect(Collectors.toList());
    }

    public List<IssueActionType> getListOfAvailableDeviceAlarmActionsTypes(DeviceAlarm deviceAlarm, SecurityContext securityContext) {
        User user = ((User) securityContext.getUserPrincipal());
        Query<IssueActionType> query = getIssueService().query(IssueActionType.class, IssueType.class);
        IssueReason reason = deviceAlarm.getReason();
        IssueType type = reason.getIssueType();


        Condition c0 = where("issueType").isEqualTo(type).and(where("issueReason").isNull());
        Condition c1 = where("issueType").isEqualTo(type).and(where("issueReason").isEqualTo(reason));
        Condition condition = (c0).or(c1);



        return query.select(condition).stream()
                .filter(actionType -> actionType.createIssueAction()
                        .map(action -> action.isApplicable(deviceAlarm) && action.isApplicableForUser(user))
                        .orElse(false))
                .collect(Collectors.toList());
    }

    public DeviceAlarmActionInfo getDeviceAlarmActionById(DeviceAlarm deviceAlarm, long actionId, SecurityContext securityContext) {
        return getListOfAvailableDeviceAlarmActionsTypes(deviceAlarm, securityContext).stream().filter(action -> action.getId() == actionId)
                .map(actionType -> new DeviceAlarmActionInfo(deviceAlarm, actionType, getPropertyValueInfoService())).findFirst().get();
    }

    public IssueActionResult performIssueAction(DeviceAlarm deviceAlarm, PerformActionRequest request) {
        IssueActionType action = getIssueActionService().findActionType(request.id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        List<PropertySpec> propertySpecs = action.createIssueAction().get().setIssue(deviceAlarm).getPropertySpecs();
        Map<String, Object> properties = new HashMap<>();
        if (propertySpecs != null && !propertySpecs.isEmpty()) {
            for (PropertySpec propertySpec : propertySpecs) {
                Object value = getPropertyValueInfoService().findPropertyValue(propertySpec, request.properties);
                if (value != null) {
                    properties.put(propertySpec.getName(), value);
                }
            }
        }
        return getIssueActionService().executeAction(action, deviceAlarm, properties);
    }

    public List<IssueCommentInfo> getAlarmComments(DeviceAlarm deviceAlarm) {
        Condition condition = where("issueId").isEqualTo(deviceAlarm.getId());
        Query<IssueComment> query = getIssueService().query(IssueComment.class, User.class);
        List<IssueComment> commentsList = query.select(condition, Order.ascending("createTime"));
        return commentsList.stream().map(IssueCommentInfo::new).collect(Collectors.toList());
    }

    private void validateMandatory(StandardParametersBean params, String... mandatoryParameters) {
        if (mandatoryParameters != null) {
            Arrays.asList(mandatoryParameters).stream().map(params::getFirst).forEach(param -> {
                if(param == null){
                    throw new WebApplicationException(Response.Status.BAD_REQUEST);
                }
            });
        }
    }

    private Finder<? extends DeviceAlarm> addSorting(Finder<? extends DeviceAlarm> finder, StandardParametersBean parameters) {
        Order[] orders = parameters.getOrder("");
        for (Order order : orders) {
            finder.sorted("baseIssue." + order.getName(), order.ascending());
        }
        return finder;
    }

    private DeviceAlarmFilter buildFilterFromQueryParameters(JsonQueryFilter jsonFilter) {
        DeviceAlarmFilter filter = new DeviceAlarmFilter();
        if (jsonFilter.hasProperty(DeviceAlarmRestModuleConst.ID)) {
            filter.setAlarmId(jsonFilter.getString(DeviceAlarmRestModuleConst.ID));
        }
        jsonFilter.getStringList(DeviceAlarmRestModuleConst.CLEARED).stream().forEach(cleared -> {
            switch (cleared) {
                case "yes":
                    filter.addToClearead(true);
                    break;
                case "no":
                    filter.addToClearead(false);
                    break;
            }
        });
        jsonFilter.getStringList(DeviceAlarmRestModuleConst.STATUS).stream()
                .flatMap(s -> getIssueService().findStatus(s).map(Stream::of).orElse(Stream.empty()))
                .forEach(filter::setStatus);
        if (jsonFilter.hasProperty(DeviceAlarmRestModuleConst.REASON)) {
            getIssueService().findReason(jsonFilter.getString(DeviceAlarmRestModuleConst.REASON))
                    .ifPresent(filter::setAlarmReason);
        }
        if (jsonFilter.hasProperty(DeviceAlarmRestModuleConst.METER)) {
            getMeteringService().findEndDeviceByName(jsonFilter.getString(DeviceAlarmRestModuleConst.METER))
                    .ifPresent(filter::setDevice);
        }

        if (jsonFilter.getLongList(DeviceAlarmRestModuleConst.USER_ASSIGNEE).stream().allMatch(s -> s == null)) {
            jsonFilter.getStringList(DeviceAlarmRestModuleConst.USER_ASSIGNEE)
                    .stream()
                    .map(id -> getUserService().getUser(Long.valueOf(id)).orElse(null))
                    .filter(user -> user != null)
                    .forEach(filter::setUserAssignee);
            if (jsonFilter.getStringList(DeviceAlarmRestModuleConst.USER_ASSIGNEE)
                    .stream()
                    .anyMatch(id -> id.equals("-1"))) {
                filter.setUnassignedOnly();
            }
        } else {
            jsonFilter.getLongList(DeviceAlarmRestModuleConst.USER_ASSIGNEE)
                    .stream().map(id -> getUserService().getUser(id).orElse(null))
                    .filter(user -> user != null)
                    .forEach(filter::setUserAssignee);
            if (jsonFilter.getLongList(DeviceAlarmRestModuleConst.USER_ASSIGNEE).stream().anyMatch(id -> id == -1L)) {
                filter.setUnassignedOnly();
            }
        }

        if(jsonFilter.getLongList(DeviceAlarmRestModuleConst.WORKGROUP).stream().allMatch(s-> s == null)){
            jsonFilter.getStringList(DeviceAlarmRestModuleConst.WORKGROUP).stream().map(id -> getUserService().getWorkGroup(Long.valueOf(id)).orElse(null))
                    .filter(workGroup -> workGroup != null)
                    .forEach(filter::addWorkGroupAssignees);
            if(jsonFilter.getStringList(DeviceAlarmRestModuleConst.WORKGROUP).stream().anyMatch(id -> id.equals("-1"))){
                filter.setUnassignedWorkGroupSelected();
            }
        }else{
            jsonFilter.getLongList(DeviceAlarmRestModuleConst.WORKGROUP)
                    .stream().map(id -> getUserService().getWorkGroup(id).orElse(null))
                    .filter(workGroup -> workGroup != null)
                    .forEach(filter::addWorkGroupAssignees);
            if(jsonFilter.getLongList(DeviceAlarmRestModuleConst.WORKGROUP).stream().anyMatch(id -> id == -1L)){
                filter.setUnassignedWorkGroupSelected();
            }
        }
        if(jsonFilter.getLong(DeviceAlarmRestModuleConst.START_INTERVAL) !=null){
            filter.setStartCreateTime(jsonFilter.getLong(DeviceAlarmRestModuleConst.START_INTERVAL));
        }
        if(jsonFilter.getLong(DeviceAlarmRestModuleConst.END_INTERVAL) !=null){
            filter.setEndCreateTime(jsonFilter.getLong(DeviceAlarmRestModuleConst.END_INTERVAL));
        }
        getDueDates(jsonFilter).stream().forEach(dd -> filter.setDueDates(dd.startTime, dd.endTime));
        return filter;
    }

    private List<IssueDueDateInfo> getDueDates(JsonQueryFilter filter) {
        IssueDueDateInfoAdapter issueDueDateInfoAdapter = new IssueDueDateInfoAdapter();
        return filter.getStringList(DeviceAlarmRestModuleConst.DUE_DATE).stream().map(dd -> {
            try {
                return issueDueDateInfoAdapter.unmarshal(dd);
            } catch (Exception ex){
                throw new LocalizedFieldValidationException(MessageSeeds.INVALID_VALUE, "filter");
            }
        }).collect(Collectors.toList());
    }

}
