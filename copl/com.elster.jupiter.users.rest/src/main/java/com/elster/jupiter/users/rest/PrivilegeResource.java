package com.elster.jupiter.users.rest;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.users.Privilege;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@Path("/privileges")
public class PrivilegeResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PrivilegeInfos getPrivileges(@Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<Privilege> list = getPrivilegeRestQuery().select(queryParameters);
        PrivilegeInfos infos = new PrivilegeInfos(list);
        infos.total = queryParameters.determineTotal(list.size());
        return infos;
    }

    private RestQuery<Privilege> getPrivilegeRestQuery() {
        Query<Privilege> query = Bus.getUserService().getPrivilegeQuery();
        return Bus.getRestQueryService().wrap(query);
    }


}
