package com.elster.jupiter.issue.rest.impl.resource;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleInfo;
import com.elster.jupiter.issue.rest.transactions.CreateCreationRuleTransaction;
import com.elster.jupiter.issue.rest.transactions.EditCreationRuleTransaction;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.util.conditions.Condition;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.issue.rest.request.RequestHelper.ID;
import static com.elster.jupiter.issue.rest.response.ResponseHelper.entity;

@Path("/creationrules")
public class CreationRuleResource extends BaseResource {

    /**
     * <b>API link</b>: <a href="http://confluence.eict.vpdc/display/JUPU/REST+API#RESTAPI-Getcreationrules">Get creation rules</a><br />
     * <b>Pagination</b>: true<br />
     * <b>Mandatory parameters</b>: none<br />
     * <b>Optional parameters</b>: none<br />
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_CREATION_RULE,Privileges.VIEW_CREATION_RULE})
    public Response getCreationRules(@BeanParam StandardParametersBean params, @QueryParam("start") Integer start){
        Query<CreationRule> query = getIssueCreationService().getCreationRuleQuery(IssueReason.class, IssueType.class);
        List<CreationRule> rules = start != null ? query.select(Condition.TRUE, params.getFrom(), params.getTo()) : query.select(Condition.TRUE);
        return entity(rules, CreationRuleInfo.class, params.getStart(), params.getLimit()).build();
    }

    /**
     * <b>API link</b>: <a href="http://confluence.eict.vpdc/display/JUPU/REST+API#RESTAPI-Getcreationrule">Get creation rule</a><br />
     * <b>Pagination</b>: false<br />
     * <b>Mandatory parameters</b>: '{@value com.elster.jupiter.issue.rest.request.RequestHelper#ID}'<br />
     * <b>Optional parameters</b>: none<br />
     */
    @GET
    @Path("/{" + ID + "}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_CREATION_RULE,Privileges.VIEW_CREATION_RULE})
    public Response getCreationRule(@PathParam(ID) long id){
        Optional<CreationRule> rule = getIssueCreationService().findCreationRule(id);
        if (!rule.isPresent()){
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return entity(new CreationRuleInfo(rule.get())).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed(Privileges.ADMINISTRATE_CREATION_RULE)
    public Response deleteCreationRule(@PathParam("id") long id, @QueryParam("version") long version){
        if(version == 0){
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        final Optional<CreationRule> rule = getIssueCreationService().findCreationRule(id);
        if (!rule.isPresent()){
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        if (rule.get().getVersion() != version) {
            return Response.status(Response.Status.CONFLICT).build();
        }
        getTransactionService().execute(new Transaction<Object>() {
            @Override
            public Object perform() {
                rule.get().delete();
                getIssueCreationService().reReadRules();
                return null;
            }
        });

        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @POST
    @RolesAllowed(Privileges.ADMINISTRATE_CREATION_RULE)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response addCreationRule(CreationRuleInfo rule){
        getTransactionService().execute(new CreateCreationRuleTransaction(getIssueService(), getIssueCreationService(), getIssueActionService(), rule));
        return Response.status(Response.Status.CREATED).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed(Privileges.ADMINISTRATE_CREATION_RULE)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response editCreationRule(@PathParam("id") long id, CreationRuleInfo rule){
        rule.setId(id);
        getTransactionService().execute(new EditCreationRuleTransaction(getIssueService(), getIssueCreationService(), getIssueActionService(), rule));
        return Response.ok().build();
    }
}
