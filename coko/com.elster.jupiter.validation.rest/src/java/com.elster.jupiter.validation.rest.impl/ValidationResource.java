package com.elster.jupiter.validation.rest.impl;

import com.elster.jupiter.events.EventType;
import com.elster.jupiter.validation.ValidationRuleSet;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;


@Path("/validation")
public class ValidationResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ValidationRuleSetInfos getEventTypes(@Context UriInfo uriInfo) {
        List<ValidationRuleSet> list = Bus.getValidationService().getValidationRuleSets();
        ValidationRuleSetInfos infos = new ValidationRuleSetInfos(list);
        infos.total = list.size();
        return infos;
    }
	




}
