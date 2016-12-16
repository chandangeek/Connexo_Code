package com.energyict.mdc.device.alarms.rest.resource;


import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.resource.StandardParametersBean;
import com.elster.jupiter.issue.rest.response.AssigneeFilterListInfo;
import com.elster.jupiter.issue.rest.response.PagedInfoListCustomized;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.energyict.mdc.device.alarms.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

@Path("/assignees")
public class UserReource extends BaseAlarmResource{


    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ALARM,Privileges.Constants.ASSIGN_ALARM,Privileges.Constants.CLOSE_ALARM,Privileges.Constants.COMMENT_ALARM,Privileges.Constants.ACTION_ALARM})
    public PagedInfoListCustomized getAllAssignees(@BeanParam StandardParametersBean params, @BeanParam JsonQueryParameters queryParameters, @Context SecurityContext securityContext) {
        if (Boolean.parseBoolean(params.getFirst("me"))) {
            AssigneeFilterListInfo assigneeFilterListInfo = AssigneeFilterListInfo.defaults((User) securityContext.getUserPrincipal(), getThesaurus(), true);
            return PagedInfoListCustomized.fromPagedList("data", assigneeFilterListInfo.getData(), queryParameters, 0);
        }
        String searchText = params.getFirst("like");
        String dbSearchText = (searchText != null && !searchText.isEmpty()) ? ("*" + searchText + "*") : "*";
        Condition conditionUser = where("authenticationName").likeIgnoreCase(dbSearchText);
        Query<User> queryUser = getUserService().getUserQuery();

        AssigneeFilterListInfo assigneeFilterListInfo;
        if(params.getStart() == 0 && (searchText == null || searchText.isEmpty())) {
            assigneeFilterListInfo = AssigneeFilterListInfo.defaults((User) securityContext.getUserPrincipal(), getThesaurus(), false);
            List<User> listUsers = queryUser.select(conditionUser, params.getFrom(), params.getTo(), Order.ascending("authenticationName"));
            assigneeFilterListInfo.addData(listUsers);
        } else {
            List<User> listUsers = queryUser.select(conditionUser, Order.ascending("authenticationName"));
            assigneeFilterListInfo = new AssigneeFilterListInfo(listUsers);
        }
        return PagedInfoListCustomized.fromPagedList("data", assigneeFilterListInfo.getData(), queryParameters, params.getStart() == 0 ? 1 : 0);
    }

}
