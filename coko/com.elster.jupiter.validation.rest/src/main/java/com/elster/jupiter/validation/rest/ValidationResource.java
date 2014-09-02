package com.elster.jupiter.validation.rest;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.metering.rest.ReadingTypeInfos;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.validation.*;
import com.elster.jupiter.validation.security.Privileges;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.elster.jupiter.util.conditions.Where.where;

@Path("/validation")
public class ValidationResource {

    private RestQueryService queryService;

    @Inject
    public ValidationResource(RestQueryService queryService) {
        this.queryService = queryService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_VALIDATION_RULE)
    public ValidationRuleSetInfos getValidationRuleSets(@Context UriInfo uriInfo) {
        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<ValidationRuleSet> list = queryRuleSets(params);

        ValidationRuleSetInfos infos = new ValidationRuleSetInfos(params.clipToLimit(list));
        infos.total = params.determineTotal(list.size());

        return infos;
    }

    private List<ValidationRuleSet> queryRuleSets(QueryParameters queryParameters) {
        Query<ValidationRuleSet> query = Bus.getValidationService().getRuleSetQuery();
        query.setRestriction(where("obsoleteTime").isNull());
        RestQuery<ValidationRuleSet> restQuery = queryService.wrap(query);
        return restQuery.select(queryParameters, Order.ascending("upper(name)"));
    }


    @GET
    @Path("/rules/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_VALIDATION_RULE)
    public ValidationRuleInfos getValidationRules(@PathParam("id") long id, @Context UriInfo uriInfo) {
        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());
        Optional<ValidationRuleSet> optional = Bus.getValidationService().getValidationRuleSet(id);
        if (optional.isPresent()) {
            ValidationRuleInfos infos = new ValidationRuleInfos();
            ValidationRuleSet set = optional.get();
            List<ValidationRule> rules;
            if (params.size() == 0) {
                rules = (List<ValidationRule>) set.getRules();
            } else {
                rules = (List<ValidationRule>) set.getRules(params.getStart(), params.getLimit());
            }
            for (ValidationRule rule : rules) {
                infos.add(rule);
            }
            infos.total = set.getRules().size();
            return infos;
        } else {
            return new ValidationRuleInfos();
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.CREATE_VALIDATION_RULE)
    public ValidationRuleSetInfo createValidationRuleSet(final ValidationRuleSetInfo info) {
        return new ValidationRuleSetInfo(Bus.getTransactionService().execute(new Transaction<ValidationRuleSet>() {
            @Override
            public ValidationRuleSet perform() {
                return Bus.getValidationService().createValidationRuleSet(info.name, info.description);
            }
        }));
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.UPDATE_VALIDATION_RULE)
    public ValidationRuleSetInfo updateValidationRuleSet(@PathParam("id") long id, final ValidationRuleSetInfo info, @Context SecurityContext securityContext) {
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

    @GET
    @Path("/{id}/usage")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_VALIDATION_RULE)
    public Response getValidationRuleSetUsage(@PathParam("id") final long id, @Context final SecurityContext securityContext) {
        ValidationRuleSet validationRuleSet = fetchValidationRuleSet(id, securityContext);
        return Response.status(Response.Status.OK).entity(Bus.getValidationService().isValidationRuleSetInUse(validationRuleSet)).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.DELETE_VALIDATION_RULE)
    public Response deleteValidationRuleSet(@PathParam("id") final long id, @Context final SecurityContext securityContext) {
        Bus.getTransactionService().execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                Optional<ValidationRuleSet> optional = Bus.getValidationService().getValidationRuleSet(id);
                if (!optional.isPresent()) {
                    throw new WebApplicationException(Response.Status.NOT_FOUND);
                }
                optional.get().delete();
            }
        });
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @POST
    @Path("/rules/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.CREATE_VALIDATION_RULE)
    public ValidationRuleInfos addRule(@PathParam("id") final long id, final ValidationRuleInfo info, @Context SecurityContext securityContext) {
        ValidationRuleInfos result = new ValidationRuleInfos();
        result.add(Bus.getTransactionService().execute(new Transaction<ValidationRule>() {
            public ValidationRule perform() {
                Optional<ValidationRuleSet> optional = Bus.getValidationService().getValidationRuleSet(id);
                ValidationRule rule = null;
                if (optional.isPresent()) {
                    ValidationRuleSet set = optional.get();
                    rule = set.addRule(ValidationAction.FAIL, info.implementation, info.name);
                    for (ReadingTypeInfo readingTypeInfo : info.readingTypes) {
                        rule.addReadingType(readingTypeInfo.mRID);
                    }
                    PropertyUtils propertyUtils = new PropertyUtils();
                    for (PropertySpec<?> propertySpec : rule.getPropertySpecs()) {
                        Object value = propertyUtils.findPropertyValue(propertySpec, info.properties);
                        rule.addProperty(propertySpec.getName(), value);
                    }
                    set.save();
                }
                return rule;
            };
        }));
        return result;
    }

    @PUT
    @Path("/rules/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.UPDATE_VALIDATION_RULE)
    public ValidationRuleInfos editRule(@PathParam("id") final long id, final ValidationRuleInfo info, @Context SecurityContext securityContext) {
        ValidationRuleInfos result = new ValidationRuleInfos();
        result.add(Bus.getTransactionService().execute(new Transaction<ValidationRule>() {
            @Override
            public ValidationRule perform() {
                Optional<ValidationRuleSet> ruleSetOptional = Bus.getValidationService().getValidationRuleSet(id);
                if (!ruleSetOptional.isPresent()) {
                    throw new WebApplicationException(Response.Status.NOT_FOUND);
                }

                ValidationRuleSet ruleSet = ruleSetOptional.get();

                Optional<? extends ValidationRule> ruleOptional = Iterables.tryFind(ruleSet.getRules(), new Predicate<ValidationRule>() {
                    @Override
                    public boolean apply(ValidationRule input) {
                        return input.getId() == info.id;
                    }
                });
                if (!ruleOptional.isPresent()) {
                    throw new WebApplicationException(Response.Status.NOT_FOUND);
                }

                ValidationRule rule = ruleOptional.get();

                List<String> mRIDs = new ArrayList<>();
                for (ReadingTypeInfo readingTypeInfo : info.readingTypes) {
                    mRIDs.add(readingTypeInfo.mRID);
                }
                Map<String, Object> propertyMap = new HashMap<>();
                PropertyUtils propertyUtils = new PropertyUtils();
                for (PropertySpec propertySpec : rule.getPropertySpecs()) {
                    Object value = propertyUtils.findPropertyValue(propertySpec, info.properties);
                    if (value != null) {
                        propertyMap.put(propertySpec.getName(), value);
                    }
                }
                rule = ruleSet.updateRule(info.id, info.name, info.active, mRIDs, propertyMap);
                ruleSet.save();
                return rule;
            }
        }));
        return result;
    }

    @DELETE
    @Path("/rules/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.DELETE_VALIDATION_RULE)
    public Response removeRule(@PathParam("id") final long id, @QueryParam("id") final long ruleId) {
        Bus.getTransactionService().execute(new Transaction<ValidationRule>() {
            @Override
            public ValidationRule perform() {
                Optional<ValidationRuleSet> ruleSetRef = Bus.getValidationService().getValidationRuleSet(id);
                if (!ruleSetRef.isPresent() || ruleSetRef.get().getObsoleteDate() != null) {
                    throw new WebApplicationException(Response.Status.NOT_FOUND);
                }
                Optional<ValidationRule> ruleRef = Bus.getValidationService().getValidationRule(ruleId);
                if (!ruleRef.isPresent() || ruleRef.get().getObsoleteDate() != null) {
                    throw new WebApplicationException(Response.Status.NOT_FOUND);
                }

                ruleSetRef.get().deleteRule(ruleRef.get());
                return null;
            }
        });
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @GET
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_VALIDATION_RULE)
    public ValidationRuleSetInfo getValidationRuleSet(@PathParam("id") long id, @Context SecurityContext securityContext) {
        ValidationRuleSet validationRuleSet = fetchValidationRuleSet(id, securityContext);
        return new ValidationRuleSetInfo(validationRuleSet);
    }

    private ValidationRuleSet fetchValidationRuleSet(long id, SecurityContext securityContext) {
        Optional<ValidationRuleSet> found = Bus.getValidationService().getValidationRuleSet(id);
        if (!found.isPresent()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        ValidationRuleSet validationRuleSet = found.get();
        return validationRuleSet;
    }

    @GET
    @Path("/actions")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_VALIDATION_RULE)
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
    @RolesAllowed(Privileges.VIEW_VALIDATION_RULE)
    public ReadingTypeInfos getReadingTypesForRule(@PathParam("id") long id) {
        ReadingTypeInfos infos = new ReadingTypeInfos();
        Optional<ValidationRule> optional = Bus.getValidationService().getValidationRule(id);
        if (optional.isPresent()) {
            ValidationRule rule = optional.get();
            Set<ReadingType> readingTypes = rule.getReadingTypes();
            for (ReadingType readingType : readingTypes) {
                infos.add(readingType);
            }
            infos.total = readingTypes.size();
        }
        return infos;
    }

    @GET
    @Path("/validators")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_VALIDATION_RULE)
    public ValidatorInfos getAvailableValidators(@Context UriInfo uriInfo) {
        ValidatorInfos infos = new ValidatorInfos();
        List<Validator> toAdd = Bus.getValidationService().getAvailableValidators();
        Collections.sort(toAdd, Compare.BY_DISPLAY_NAME);
        PropertyUtils propertyUtils = new PropertyUtils();
        for (Validator validator : toAdd) {
            infos.add(validator.getClass().getName(), validator.getDisplayName(), propertyUtils.convertPropertySpecsToPropertyInfos(validator.getPropertySpecs()));
        }
        infos.total = toAdd.size();
        return infos;
    }

    private enum Compare implements Comparator<Validator> {
        BY_DISPLAY_NAME;

        @Override
        public int compare(Validator o1, Validator o2) {
            return o1.getDisplayName().compareTo(o2.getDisplayName());
        }
    }
}
