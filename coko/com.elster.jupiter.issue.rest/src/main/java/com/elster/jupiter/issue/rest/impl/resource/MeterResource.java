package com.elster.jupiter.issue.rest.impl.resource;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.resource.StandardParametersBean;
import com.elster.jupiter.issue.rest.response.device.MeterShortInfo;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static com.elster.jupiter.issue.rest.request.RequestHelper.ID;
import static com.elster.jupiter.issue.rest.request.RequestHelper.LIKE;
import static com.elster.jupiter.issue.rest.request.RequestHelper.LIMIT;
import static com.elster.jupiter.issue.rest.request.RequestHelper.START;
import static com.elster.jupiter.issue.rest.response.ResponseHelper.entity;
import static com.elster.jupiter.util.conditions.Where.where;

@Path("/meters")
public class MeterResource extends BaseResource {
    /**
     * <b>API link</b>: none<br />
     * <b>Pagination</b>: true<br />
     * <b>Mandatory parameters</b>:
     *      '{@value com.elster.jupiter.issue.rest.request.RequestHelper#START}',
     *      '{@value com.elster.jupiter.issue.rest.request.RequestHelper#LIMIT}',
     * <b>Optional parameters</b>: '{@value com.elster.jupiter.issue.rest.request.RequestHelper#LIKE}'<br />
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE,Privileges.Constants.ASSIGN_ISSUE,Privileges.Constants.CLOSE_ISSUE,Privileges.Constants.COMMENT_ISSUE,Privileges.Constants.ACTION_ISSUE})
    public Response getMeters(@BeanParam StandardParametersBean params) {
        validateMandatory(params, START, LIMIT);
        // We shouldn't return anything if the 'like' parameter is absent or it is an empty string.
        // Update: COPL-631, we should.
        Query<Meter> meterQuery = getMeteringService().getMeterQuery();
        String searchText = params.getFirst(LIKE);
        String dbSearchText = (searchText != null && !searchText.isEmpty()) ? ("*" + searchText + "*") : "*";
        Condition condition = where("mRID").likeIgnoreCase(dbSearchText);
        List<Meter> listMeters = meterQuery.select(condition, params.getFrom(), params.getTo(), Order.ascending("mRID"));
        return entity(listMeters, MeterShortInfo.class, params.getStart(), params.getLimit()).build();
    }

    /**
     * <b>API link</b>: none<br />
     * <b>Pagination</b>: false<br />
     * <b>Mandatory parameters</b>: '{@value com.elster.jupiter.issue.rest.request.RequestHelper#ID}'<br />
     * <b>Optional parameters</b>: none<br />
     */
    @GET
    @Path("/{" + ID + "}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE,Privileges.Constants.ASSIGN_ISSUE,Privileges.Constants.CLOSE_ISSUE,Privileges.Constants.COMMENT_ISSUE,Privileges.Constants.ACTION_ISSUE})
    public Response getMeter(@PathParam(ID) String mrid){
        Query<Meter> meterQuery = getMeteringService().getMeterQuery();
        List<Meter> meters = meterQuery.select(where("mRID").isEqualTo(mrid));
        if(meters == null || meters.isEmpty()){
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return entity(new MeterShortInfo(meters.get(0))).build();
    }
}
