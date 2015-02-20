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
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.Validator;
import com.elster.jupiter.validation.ValidatorNotFoundException;
import com.elster.jupiter.validation.security.Privileges;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import static com.elster.jupiter.util.conditions.Where.where;

@Path("/validation")
public class ValidationResource {

    private final RestQueryService queryService;
    private final ValidationService validationService;
    private final TransactionService transactionService;

    @Inject
    public ValidationResource(RestQueryService queryService, ValidationService validationService, TransactionService transactionService) {
        this.queryService = queryService;
        this.validationService = validationService;
        this.transactionService = transactionService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.VIEW_VALIDATION_CONFIGURATION})
    public ValidationRuleSetInfos getValidationRuleSets(@Context UriInfo uriInfo) {
        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<ValidationRuleSet> list = queryRuleSets(params);

        ValidationRuleSetInfos infos = new ValidationRuleSetInfos(params.clipToLimit(list));
        infos.total = params.determineTotal(list.size());

        return infos;
    }

    private List<ValidationRuleSet> queryRuleSets(QueryParameters queryParameters) {
        Query<ValidationRuleSet> query = validationService.getRuleSetQuery();
        query.setRestriction(where("obsoleteTime").isNull());
        RestQuery<ValidationRuleSet> restQuery = queryService.wrap(query);
        return restQuery.select(queryParameters, Order.ascending("upper(name)"));
    }


    @GET
    @Path("/{ruleSetId}/rules")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.VIEW_VALIDATION_CONFIGURATION})
    public ValidationRuleInfos getValidationRules(@PathParam("ruleSetId") long ruleSetId, @Context UriInfo uriInfo) {
        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());
        Optional<? extends ValidationRuleSet> optional = validationService.getValidationRuleSet(ruleSetId);
        if (optional.isPresent()) {
            ValidationRuleInfos infos = new ValidationRuleInfos();
            ValidationRuleSet set = optional.get();
            List<? extends ValidationRule> rules;
            if (params.size() == 0) {
                rules = set.getRules();
            } else {
                rules = set.getRules(params.getStart(), params.getLimit());
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
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION)
    public Response createValidationRuleSet(final ValidationRuleSetInfo info) {
        return Response.status(Response.Status.CREATED).entity(new ValidationRuleSetInfo(transactionService.execute(new Transaction<ValidationRuleSet>() {
            @Override
            public ValidationRuleSet perform() {
                return validationService.createValidationRuleSet(info.name, info.description);
            }
        }))).build();
    }

    @PUT
    @Path("/{ruleSetId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION)
    public ValidationRuleSetInfo updateValidationRuleSet(@PathParam("ruleSetId") long ruleSetId, final ValidationRuleSetInfo info, @Context SecurityContext securityContext) {
        transactionService.execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                validationService.getValidationRuleSet(ruleSetId).ifPresent(set -> {
                    set.setName(info.name);
                    set.setDescription(info.description);
                    set.save();
                });
            }
        });
        return getValidationRuleSet(ruleSetId, securityContext);
    }

    class RuleSetUsageInfo {
        public boolean isInUse;
    }

    @GET
    @Path("/{ruleSetId}/usage")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.VIEW_VALIDATION_CONFIGURATION})
    public Response getValidationRuleSetUsage(@PathParam("ruleSetId") final long ruleSetId, @Context final SecurityContext securityContext) {
        ValidationRuleSet validationRuleSet = fetchValidationRuleSet(ruleSetId, securityContext);
        RuleSetUsageInfo info = new RuleSetUsageInfo();
        info.isInUse=validationService.isValidationRuleSetInUse(validationRuleSet);
        return Response.status(Response.Status.OK).entity(info).build();
    }

    @DELETE
    @Path("/{ruleSetId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION)
    public Response deleteValidationRuleSet(@PathParam("ruleSetId") final long ruleSetId, @Context final SecurityContext securityContext) {
        transactionService.execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                validationService.getValidationRuleSet(ruleSetId).
                        orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND)).
                        delete();
            }
        });
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @POST
    @Path("/{ruleSetId}/rules")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION)
    public Response addRule(@PathParam("ruleSetId") final long ruleSetId, final ValidationRuleInfo info, @Context SecurityContext securityContext) {
        ValidationRuleInfo result =
        transactionService.execute(() -> {
            ValidationRuleSet set = validationService.getValidationRuleSet(ruleSetId).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
            ValidationRule rule = set.addRule(ValidationAction.FAIL, info.implementation, info.name);
            for (ReadingTypeInfo readingTypeInfo : info.readingTypes) {
                rule.addReadingType(readingTypeInfo.mRID);
            }
            PropertyUtils propertyUtils = new PropertyUtils();
            try {
                for (PropertySpec propertySpec : rule.getPropertySpecs()) {
                    Object value = propertyUtils.findPropertyValue(propertySpec, info.properties);
                    rule.addProperty(propertySpec.getName(), value);
                }
            } catch (ValidatorNotFoundException ex) {
            } finally {
                set.save();
            }
            return new ValidationRuleInfo(rule);
        });
        return Response.status(Response.Status.CREATED).entity(result).build();
    }

    @PUT
    @Path("/{ruleSetId}/rules/{ruleId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION)
    public ValidationRuleInfos editRule(@PathParam("ruleSetId") final long ruleSetId, @PathParam("ruleId") final long ruleId, final ValidationRuleInfo info, @Context SecurityContext securityContext) {
        ValidationRuleInfos result = new ValidationRuleInfos();
        result.add(transactionService.execute(new Transaction<ValidationRule>() {
            @Override
            public ValidationRule perform() {
                ValidationRuleSet ruleSet = validationService.getValidationRuleSet(ruleSetId).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));

                ValidationRule rule = getValidationRuleFromSetOrThrowException(ruleSet, ruleId);

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
                rule = ruleSet.updateRule(ruleId, info.name, info.active, mRIDs, propertyMap);
                ruleSet.save();
                return rule;
            }
        }));
        return result;
    }

    private ValidationRule getValidationRuleFromSetOrThrowException(ValidationRuleSet ruleSet, long ruleId) {
        return ruleSet.getRules().stream()
                            .filter(input -> input.getId() == ruleId)
                            .findAny()
                            .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    @GET
    @Path("/{ruleSetId}/rules/{ruleId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION)
    public Response getRule(@PathParam("ruleSetId") final long ruleSetId, @PathParam("ruleId") final long ruleId, final ValidationRuleInfo info, @Context SecurityContext securityContext) {
        ValidationRule rule = transactionService.execute((Transaction<ValidationRule>) () -> {
            ValidationRuleSet ruleSet = validationService.getValidationRuleSet(ruleSetId).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));

            return getValidationRuleFromSetOrThrowException(ruleSet, ruleId);
        });
        return Response.ok(new ValidationRuleInfo(rule)).build();
    }

    @DELETE
    @Path("/{ruleSetId}/rules/{ruleId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION)
    public Response removeRule(@PathParam("ruleSetId") final long ruleSetId, @PathParam("ruleId") final long ruleId) {
        transactionService.execute(new Transaction<ValidationRule>() {
            @Override
            public ValidationRule perform() {
                Optional<? extends ValidationRuleSet> ruleSetRef = validationService.getValidationRuleSet(ruleSetId);
                if (!ruleSetRef.isPresent() || ruleSetRef.get().getObsoleteDate() != null) {
                    throw new WebApplicationException(Response.Status.NOT_FOUND);
                }
                Optional<ValidationRule> ruleRef = validationService.getValidationRule(ruleId);
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
    @Path("/{ruleSetId}/")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.VIEW_VALIDATION_CONFIGURATION})
    public ValidationRuleSetInfo getValidationRuleSet(@PathParam("ruleSetId") long ruleSetId, @Context SecurityContext securityContext) {
        ValidationRuleSet validationRuleSet = fetchValidationRuleSet(ruleSetId, securityContext);
        return new ValidationRuleSetInfo(validationRuleSet);
    }

    private ValidationRuleSet fetchValidationRuleSet(long id, SecurityContext securityContext) {
        return validationService.getValidationRuleSet(id)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    @GET
    @Path("/actions")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.VIEW_VALIDATION_CONFIGURATION})
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
    @Path("/{ruleSetId}/rule/{ruleId}/readingtypes")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.VIEW_VALIDATION_CONFIGURATION})
    public ReadingTypeInfos getReadingTypesForRule(@PathParam("ruleSetId") long ruleSetId, @PathParam("ruleId") long ruleId, @Context SecurityContext securityContext) {
        ReadingTypeInfos infos = new ReadingTypeInfos();
        ValidationRuleSet validationRuleSet = fetchValidationRuleSet(ruleSetId, securityContext);
        ValidationRule validationRule = getValidationRuleFromSetOrThrowException(validationRuleSet, ruleId);
        Set<ReadingType> readingTypes = validationRule.getReadingTypes();
        for (ReadingType readingType : readingTypes) {
            infos.add(readingType);
        }
        infos.total = readingTypes.size();
        return infos;
    }

    @GET
    @Path("/validators")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.VIEW_VALIDATION_CONFIGURATION})
    public ValidatorInfos getAvailableValidators(@Context UriInfo uriInfo) {
        ValidatorInfos infos = new ValidatorInfos();
        List<Validator> toAdd = validationService.getAvailableValidators();
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
