package com.energyict.mdc.device.alarms.rest.resource;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.request.CreateCommentRequest;
import com.elster.jupiter.issue.rest.resource.StandardParametersBean;
import com.elster.jupiter.issue.rest.response.IssueCommentInfo;
import com.elster.jupiter.issue.share.entity.IssueComment;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.energyict.mdc.device.alarms.DeviceAlarmFilter;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.alarms.rest.response.DeviceAlarmInfo;
import com.energyict.mdc.device.alarms.rest.response.DeviceAlarmInfoFactory;
import com.energyict.mdc.device.alarms.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;


@Path("/alarms")
public class DeviceAlarmResource{

    private final DeviceAlarmService deviceAlarmService;
    private final DeviceAlarmInfoFactory deviceAlarmInfoFactory;
    private final IssueService issueService;


    @Inject
    public DeviceAlarmResource(DeviceAlarmService deviceAlarmService, DeviceAlarmInfoFactory deviceAlarmInfoFactory, IssueService issueService){
        this.deviceAlarmService = deviceAlarmService;
        this.deviceAlarmInfoFactory = deviceAlarmInfoFactory;
        this.issueService = issueService;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ALARM, Privileges.Constants.ASSIGN_ALARM, Privileges.Constants.CLOSE_ALARM, Privileges.Constants.COMMENT_ALARM, Privileges.Constants.ACTION_ALARM})
    public PagedInfoList getAllDeviceAlarms(@BeanParam StandardParametersBean params, @BeanParam JsonQueryParameters queryParams, @BeanParam JsonQueryFilter filter){
//        validateMandatory(params, START, LIMIT);
        Finder<? extends DeviceAlarm> finder = deviceAlarmService.findAlarms(new DeviceAlarmFilter()); //FixMe implement filter;
//        addSorting(finder, params);
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
        Optional<? extends DeviceAlarm> deviceAlarm = deviceAlarmService.findAlarm(id);
        return deviceAlarm.map(i -> Response.ok().entity(deviceAlarmInfoFactory.asInfo(i)).build())
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    @GET
    @Transactional
    @Path("/{id}/comments")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ALARM, Privileges.Constants.ASSIGN_ALARM, Privileges.Constants.CLOSE_ALARM, Privileges.Constants.COMMENT_ALARM, Privileges.Constants.ACTION_ALARM})
    public PagedInfoList getComments(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters) {
        DeviceAlarm deviceAlarm = deviceAlarmService.findAlarm(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return PagedInfoList.fromCompleteList("comments", getAlarmComments(deviceAlarm), queryParameters);
    }

    @POST
    @Transactional
    @Path("/{id}/comments")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.COMMENT_ALARM)
    public Response postComment(@PathParam("id") long id, CreateCommentRequest request, @Context SecurityContext securityContext) {
        DeviceAlarm deviceAlarm = deviceAlarmService.findAlarm(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        User author = (User) securityContext.getUserPrincipal();
        IssueComment comment = deviceAlarm.addComment(request.getComment(), author)
                .orElseThrow(() -> new WebApplicationException(Response.Status.BAD_REQUEST));
        return Response.ok(new IssueCommentInfo(comment)).status(Response.Status.CREATED).build();
    }

    public List<IssueCommentInfo> getAlarmComments(DeviceAlarm deviceAlarm) {
        Condition condition = where("issueId").isEqualTo(deviceAlarm.getId());
        Query<IssueComment> query = issueService.query(IssueComment.class, User.class);
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
            finder.sorted(order.getName(), order.ascending());
        }
        return finder;
    }
}
