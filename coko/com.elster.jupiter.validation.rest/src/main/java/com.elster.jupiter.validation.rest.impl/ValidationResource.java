package com.elster.jupiter.validation.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfos;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.google.common.base.Optional;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Set;


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
     @Path("/rules/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     public ValidationRuleInfos getValidationRules(@PathParam("id") String id) {
        Optional<ValidationRuleSet> optional = Bus.getValidationService().getValidationRuleSet(Long.parseLong(id));
        if (optional.isPresent()) {
            ValidationRuleInfos infos = new ValidationRuleInfos();
            ValidationRuleSet set = optional.get();
            for (ValidationRule rule : set.getRules()) {
                infos.add(rule);
            }
            infos.total = set.getRules().size();
            return infos;
        } else {
            return new ValidationRuleInfos();
        }
    }

    @GET
     @Path("/propertyspecsforrule/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     public ValidationRulePropertySpecInfos getAvailableProperties(@PathParam("id") String id) {
        ValidationRulePropertySpecInfos infos = new ValidationRulePropertySpecInfos();
        Optional<ValidationRule> optional =
                Bus.getValidationService().getValidationRule(Long.parseLong(id));
        if (optional.isPresent()) {
            ValidationRule rule = optional.get();
            List<String> requiredKeys = rule.getValidator().getRequiredKeys();
            List<String> optionalKeys = rule.getValidator().getOptionalKeys();
            for (String key : requiredKeys) {
                infos.add(key, false, rule.getImplementation());
            }
            for (String key : optionalKeys) {
                infos.add(key, true, rule.getImplementation());
            }
            return infos;
        }
        else {
            return infos;
        }
    }

    @GET
    @Path("/propertyspecs")
    @Produces(MediaType.APPLICATION_JSON)
    //TODO return all available properties instead of this mock implementation
    public ValidationRulePropertySpecInfos getAllAvailableProperties(@Context UriInfo uriInfo) {
        ValidationRulePropertySpecInfos infos = new ValidationRulePropertySpecInfos();
        infos.add("minimum", true, "com.elster.jupiter.validators.MinMaxValidator");
        infos.add("maximum", true, "com.elster.jupiter.validators.MinMaxValidator");
        infos.add("high", true, "com.elster.jupiter.validators.RatedPowerValidator");
        infos.add("low", true, "com.elster.jupiter.validators.RatedPowerValidator");
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
    @Path("/readingtypes/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ReadingTypeInfos getReadingTypesForRule(@PathParam("id") String id) {
        ReadingTypeInfos infos = new ReadingTypeInfos();
        Optional<ValidationRule> optional =
                Bus.getValidationService().getValidationRule(Long.parseLong(id));
        if (optional.isPresent()) {
            ValidationRule rule = optional.get();
            Set<ReadingType> readingTypes = rule.getReadingTypes();
            for (ReadingType readingType : readingTypes) {
                infos.add(readingType);
            }
            infos.total = readingTypes.size();
            return infos;
        } else {
            return infos;
        }
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

    @GET
    @Path("/properties")
    @Produces(MediaType.APPLICATION_JSON)
    public ValidatorInfos getAvailableProperties(@Context UriInfo uriInfo) {
        ValidatorInfos infos = new ValidatorInfos();
        List<String> toAdd = Bus.getValidationService().getAvailableValidators();
        for (String implementation : toAdd) {
            infos.add(implementation);
        }
        infos.total = toAdd.size();
        return infos;
    }
	




}
