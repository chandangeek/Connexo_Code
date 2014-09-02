package com.elster.jupiter.issue.rest.resource;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.response.device.MeterShortInfo;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.google.common.base.Optional;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static com.elster.jupiter.issue.rest.request.RequestHelper.*;
import static com.elster.jupiter.issue.rest.response.ResponseHelper.ok;
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
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_ISSUE)
    public Response getMeters(@BeanParam StandardParametersBean params) {
        validateMandatory(params, START, LIMIT);
        // We shouldn't return anything if the 'like' parameter is absent or it is an empty string.
        String searchText = params.getFirst(LIKE);
        if (searchText != null && !searchText.isEmpty()){
            String dbSearchText = "%" + searchText + "%";
            Condition condition = where("mRID").likeIgnoreCase(dbSearchText);

            Query<Meter> meterQuery = getMeteringService().getMeterQuery();
            List<Meter> listMeters = meterQuery.select(condition, params.getFrom(), params.getTo(), Order.ascending("mRID"));
            return ok(listMeters, MeterShortInfo.class, params.getStart(), params.getLimit()).build();
        }
        return ok("").build();
    }

    /**
     * <b>API link</b>: none<br />
     * <b>Pagination</b>: false<br />
     * <b>Mandatory parameters</b>: '{@value com.elster.jupiter.issue.rest.request.RequestHelper#ID}'<br />
     * <b>Optional parameters</b>: none<br />
     */
    @GET
    @Path("/{" + ID + "}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_ISSUE)
    public Response getMeter(@PathParam(ID) long id){
        Optional<Meter> meterRef = getMeteringService().findMeter(id);
        if(!meterRef.isPresent()){
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return ok(new MeterShortInfo(meterRef.get())).build();
    }
}
