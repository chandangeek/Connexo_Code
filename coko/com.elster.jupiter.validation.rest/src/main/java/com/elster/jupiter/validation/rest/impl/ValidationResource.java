package com.elster.jupiter.validation.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.metering.rest.ReadingTypeInfos;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.units.Unit;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.Validator;
import com.google.common.base.Optional;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.math.BigDecimal;
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

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public ValidationRuleSetInfos createValidationRuleSet(final ValidationRuleSetInfo info) {
        ValidationRuleSetInfos result = new ValidationRuleSetInfos();
        result.add(
            Bus.getTransactionService().execute(new Transaction<ValidationRuleSet>() {
                @Override
                public ValidationRuleSet perform() {
                    return Bus.getValidationService().createValidationRuleSet(info.name, info.description);
                }
            }));
        return result;
    }

    @PUT
     @Path("/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     public ValidationRuleSetInfos updateValidationRuleSet(@PathParam("id") long id, final ValidationRuleSetInfo info, @Context SecurityContext securityContext) {
        info.id = id;
        Bus.getTransactionService().execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                Optional<ValidationRuleSet> optional =
                        Bus.getValidationService().getValidationRuleSet(info.id);
                if (optional.isPresent()) {
                    ValidationRuleSet set = optional.get();
                    set.setName(info.name);
                    set.setDescription(info.description);
                    set.save();
                }
            }
        });
        return getValidationRuleSet(info.id, securityContext);
    }

    @POST
    @Path("/rules/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ValidationRuleInfos addRule(@PathParam("id") final long id, final ValidationRuleInfo info, @Context SecurityContext securityContext) {
        ValidationRuleInfos result = new ValidationRuleInfos();
        result.add(
                Bus.getTransactionService().execute(new Transaction<ValidationRule>() {
                    @Override
                    public ValidationRule perform() {
                        Optional<ValidationRuleSet> optional =
                                Bus.getValidationService().getValidationRuleSet(id);
                        ValidationRule rule = null;
                        if (optional.isPresent()) {
                            ValidationRuleSet set = optional.get();
                            rule = set.addRule(ValidationAction.FAIL, info.implementation, info.name);
                            for (ReadingTypeInfo readingTypeInfo: info.readingTypes) {
                                rule.addReadingType(Bus.getMeteringService().getReadingType(readingTypeInfo.mRID).get());
                            }
                            for (ValidationRulePropertyInfo propertyInfo: info.properties) {
                                rule.addProperty(propertyInfo.name, Unit.WATT_HOUR.amount(propertyInfo.value));
                            }
                            set.save();
                        }
                        return rule;
                    }
                }));
        return result;
    }

    @GET
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON)
    public ValidationRuleSetInfos getValidationRuleSet(@PathParam("id") long id, @Context SecurityContext securityContext) {
        ValidationRuleSet validationRuleSet = fetchValidationRuleSet(id, securityContext);
        ValidationRuleSetInfos result = new ValidationRuleSetInfos(validationRuleSet);
        return result;
    }

    private ValidationRuleSet fetchValidationRuleSet(long id, SecurityContext securityContext) {
        Optional<ValidationRuleSet> found =
                Bus.getValidationService().getValidationRuleSet(id);
        if (!found.isPresent()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        ValidationRuleSet validationRuleSet = found.get();
        return validationRuleSet;
    }

    /*@GET
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
    }     */

    @GET
    @Path("/propertyspecs")
    @Produces(MediaType.APPLICATION_JSON)
    public ValidationRulePropertySpecInfos getAllAvailableProperties(@Context UriInfo uriInfo) {
        List<Validator> validators = Bus.getValidationService().getAvailableValidators();
        ValidationRulePropertySpecInfos infos = new ValidationRulePropertySpecInfos();
        for (Validator validator : validators) {
            for (String property : validator.getRequiredKeys()) {
                infos.add(property, false, validator.getClass().getName());
            }
            for (String property : validator.getOptionalKeys()) {
                infos.add(property, true, validator.getClass().toString());
            }
        }
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
                List<Validator> toAdd = Bus.getValidationService().getAvailableValidators();
                for (Validator validator : toAdd) {
                    infos.add(validator.getClass().getName(), validator.getDisplayName());
                }
                infos.total = toAdd.size();
                return infos;
            }


        }
