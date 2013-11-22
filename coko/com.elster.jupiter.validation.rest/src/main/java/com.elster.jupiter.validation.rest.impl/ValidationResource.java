package com.elster.jupiter.validation.rest.impl;

import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationRuleSet;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;


@Path("/validation")
public class ValidationResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ValidationRuleSetInfos getValidationRuleSets(@Context UriInfo uriInfo) {
        List<ValidationRuleSet> list = Bus.getValidationService().getValidationRuleSets();
        ValidationRuleSetInfos infos = new ValidationRuleSetInfos(list);
        infos.total = list.size();
        return infos;
    }

    @GET
    @Path("/actions")
    @Produces(MediaType.APPLICATION_JSON)
    public ValidationActionInfos getAvailableValidationActions(@Context UriInfo uriInfo) {
        ValidationActionInfos infos = new ValidationActionInfos();
        ValidationAction[] actions = ValidationAction.values();
        for (ValidationAction action : actions) {
            infos.add(action);
        }
        infos.total = actions.length;
        return infos;
    }

    @GET
    @Path("/validators")
    @Produces(MediaType.APPLICATION_JSON)
    public ValidatorInfos getAvailableValidators(@Context UriInfo uriInfo) {
        ValidatorInfos infos = new ValidatorInfos();
        List<String> toAdd = Bus.getValidationService().getAvailableValidators();
        for (String implementation : toAdd) {
            infos.add(implementation);
        }
        infos.total = toAdd.size();
        return infos;
    }
	




}
