package com.elster.jupiter.validation.rest;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.metering.rest.ReadingTypeInfos;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.validation.*;
import com.elster.jupiter.validation.impl.IValidationRuleSetVersion;
import com.elster.jupiter.validation.security.Privileges;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
    @RolesAllowed({Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.VIEW_VALIDATION_CONFIGURATION,
            Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE, Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})
    public Response getValidationRuleSets(@Context UriInfo uriInfo) {
        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<ValidationRuleSet> list = queryRuleSets(params);

        ValidationRuleSetInfos infos = new ValidationRuleSetInfos(params.clipToLimit(list));
        infos.total = params.determineTotal(list.size());

        return Response.ok(infos).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION)
    public Response createValidationRuleSet(final ValidationRuleSetInfo info) {
        return Response.status(Response.Status.CREATED)
                .entity(new ValidationRuleSetInfo(transactionService.execute(
                        () -> validationService.createValidationRuleSet(info.name, info.description)))).build();
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

    private List<ValidationRuleSet> queryRuleSets(QueryParameters queryParameters) {
        Query<ValidationRuleSet> query = validationService.getRuleSetQuery();
        query.setRestriction(where("obsoleteTime").isNull());
        RestQuery<ValidationRuleSet> restQuery = queryService.wrap(query);
        return restQuery.select(queryParameters, Order.ascending("upper(name)"));
    }
    ////// VERSIONS
    @GET
    @Path("/{ruleSetId}/versions")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.VIEW_VALIDATION_CONFIGURATION,
            Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE, Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})
    public Response getRuleSetVersions(@PathParam("ruleSetId") long ruleSetId,@Context UriInfo uriInfo, @Context SecurityContext securityContext) {

        ValidationRuleSet ruleSet = validationService.getValidationRuleSet(ruleSetId).orElseThrow(
                () -> new WebApplicationException(Response.Status.NOT_FOUND));


        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());

        List<? extends ValidationRuleSetVersion> versions;
        if (queryParameters.size() == 0) {
            versions = ruleSet.getRuleSetVersions();
        } else {
            versions = ruleSet.getRuleSetVersions(queryParameters.getStart(), queryParameters.getLimit());
        }
        ValidationRuleSetVersionInfos infos = new ValidationRuleSetVersionInfos();
        infos.addAll(versions);
        return Response.ok(infos).build();
    }

    @POST
    @Path("/{ruleSetId}/versions")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION)
    public Response createRuleSetVersion(@PathParam("ruleSetId") long ruleSetId,ValidationRuleSetVersionInfo info) {

        ValidationRuleSetVersionInfo result =
                transactionService.execute(() -> {
                    ValidationRuleSet ruleSet = validationService.getValidationRuleSet(ruleSetId).orElseThrow(
                            () -> new WebApplicationException(Response.Status.NOT_FOUND));

                    ValidationRuleSetVersion version = ruleSet.addRuleSetVersion(info.description, info.startDate);
                    ruleSet.save();
                    return new ValidationRuleSetVersionInfo(version);
                });
        return Response.status(Response.Status.CREATED).entity(result).build();
    }

    @DELETE
    @Path("/{ruleSetId}/versions/{ruleSetVersionId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION)
    public Response deleteRuleSetVersion(@PathParam("ruleSetId") long ruleSetId,@PathParam("ruleSetVersionId") final long ruleSetVersionId, @Context final SecurityContext securityContext) {

        transactionService.execute(() -> {
                Optional<? extends ValidationRuleSet> ruleSetRef = validationService.getValidationRuleSet(ruleSetId);
                if (!ruleSetRef.isPresent() || ruleSetRef.get().getObsoleteDate() != null) {
                    throw new WebApplicationException(Response.Status.NOT_FOUND);
                }
                ValidationRuleSetVersion ruleSetVersion = getValidationRuleVersionFromSetOrThrowException(ruleSetRef.get(), ruleSetVersionId);
                ruleSetRef.get().deleteRuleSetVersion(ruleSetVersion);
                return null;
        });
        return Response.status(Response.Status.NO_CONTENT).build();

    }

    @POST
    @Path("/{ruleSetId}/versions/{ruleSetVersionId}/clone")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION)
    public Response cloneRuleSetVersion(@PathParam("ruleSetId") long ruleSetId,
                                        @PathParam("ruleSetVersionId") final long ruleSetVersionId,
                                        ValidationRuleSetVersionInfo info) {

        ValidationRuleSetVersionInfo result =
                transactionService.execute(() -> {
                    ValidationRuleSet ruleSet = validationService.getValidationRuleSet(ruleSetId).orElseThrow(
                            ()-> new WebApplicationException(Response.Status.NOT_FOUND));

                    ValidationRuleSetVersion version = ruleSet.cloneRuleSetVersion(ruleSetVersionId, info.description, info.startDate);
                    ruleSet.save();
                    return new ValidationRuleSetVersionInfo(version);
                });
        return Response.status(Response.Status.CREATED).entity(result).build();
    }

    @PUT
    @Path("/{ruleSetId}/versions/{ruleSetVersionId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION)
    public Response updateRuleSetVersion(@PathParam("ruleSetId")long ruleSetId,
                                         @PathParam("ruleSetVersionId") long ruleSetVersionId, ValidationRuleSetVersionInfo info) {

        ValidationRuleSetVersionInfo result = transactionService.execute(() -> {
            Optional<? extends ValidationRuleSet> ruleSetRef = validationService.getValidationRuleSet(ruleSetId);
            if (!ruleSetRef.isPresent() || ruleSetRef.get().getObsoleteDate() != null) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
            getValidationRuleVersionFromSetOrThrowException(ruleSetRef.get(), ruleSetVersionId);
            ValidationRuleSetVersion ruleSetVersion = ruleSetRef.get().updateRuleSetVersion(info.id, info.description, info.startDate);
            ruleSetRef.get().save();
            return new ValidationRuleSetVersionInfo(ruleSetVersion);
        });
        return Response.ok(result).build();
    }

    ////// RULES
    @GET
    @Path("/{ruleSetId}/versions/{ruleSetVersionId}/rules")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.VIEW_VALIDATION_CONFIGURATION,
            Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE, Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})
    public Response getValidationRules(@PathParam("ruleSetId") long ruleSetId
            , @PathParam("ruleSetVersionId") final long ruleSetVersionId, @Context UriInfo uriInfo) {


        ValidationRuleSet ruleSet = validationService.getValidationRuleSet(ruleSetId).orElseThrow(
                ()-> new WebApplicationException(Response.Status.NOT_FOUND));
        ValidationRuleSetVersion ruleSetVersion = getValidationRuleVersionFromSetOrThrowException(ruleSet, ruleSetVersionId);

        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());

        List<? extends ValidationRule> rules;
        if (queryParameters.size() == 0) {
            rules = ruleSetVersion.getRules();
        } else {
            rules = ruleSetVersion.getRules(queryParameters.getStart(), queryParameters.getLimit());
        }
        ValidationRuleInfos infos = new ValidationRuleInfos();
        infos.addAll(rules);
        return Response.ok(infos).build();
    }


    @POST
    @Path("/{ruleSetId}/versions/{ruleSetVersionId}/rules")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION)
    public Response addRule(@PathParam("ruleSetId") final long ruleSetId,
                            @PathParam("ruleSetVersionId") final long ruleSetVersionId,
                            final ValidationRuleInfo info, @Context SecurityContext securityContext) {
        ValidationRuleInfo result =
            transactionService.execute(() -> {
                ValidationRuleSet ruleSet = validationService.getValidationRuleSet(ruleSetId).orElseThrow(
                        () -> new WebApplicationException(Response.Status.NOT_FOUND));
                ValidationRuleSetVersion ruleSetVersion = getValidationRuleVersionFromSetOrThrowException(ruleSet, ruleSetVersionId);

                ValidationRule rule = ruleSetVersion.addRule(info.action, info.implementation, info.name);
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
                    ruleSetVersion.save();
                }
                return new ValidationRuleInfo(rule);
            });
        return Response.status(Response.Status.CREATED).entity(result).build();
    }

    @PUT
    @Path("/{ruleSetId}/versions/{ruleSetVersionId}/rules/{ruleId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION)
    public Response editRule(@PathParam("ruleSetId") final long ruleSetId,
                                        @PathParam("ruleSetVersionId") final long ruleSetVersionId,
                                        @PathParam("ruleId") final long ruleId,
                                        final ValidationRuleInfo info, @Context SecurityContext securityContext) {
        ValidationRuleInfos result = new ValidationRuleInfos();
        result.add(transactionService.execute(() -> {
            ValidationRuleSet ruleSet = validationService.getValidationRuleSet(ruleSetId).orElseThrow(
                    () -> new WebApplicationException(Response.Status.NOT_FOUND));

            ValidationRuleSetVersion ruleSetVersion = getValidationRuleVersionFromSetOrThrowException(ruleSet, ruleSetVersionId);
            ValidationRule rule = getValidationRuleFromVersionOrThrowException(ruleSetVersion, ruleId);

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
            rule = ruleSetVersion.updateRule(ruleId, info.name, info.active, info.action, mRIDs, propertyMap);
            ruleSetVersion.save();
            return rule;
        }));
        return Response.ok(result).build();
    }

    private ValidationRuleSetVersion getValidationRuleVersionFromSetOrThrowException(ValidationRuleSet ruleSet, long ruleSetVersionId) {
        return ruleSet.getRuleSetVersions().stream()
                .filter(input -> input.getId() == ruleSetVersionId)
                .findAny()
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }


    private ValidationRule getValidationRuleFromVersionOrThrowException(ValidationRuleSetVersion ruleSetVersion, long ruleId) {
        return ruleSetVersion.getRules().stream()
                            .filter(input -> input.getId() == ruleId)
                            .findAny()
                            .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    @GET
    @Path("/{ruleSetId}/versions/{ruleSetVersionId}/rules/{ruleId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION)
    public Response getRule(@PathParam("ruleSetId") final long ruleSetId,
                            @PathParam("ruleSetVersionId") final long ruleSetVersionId,
                            @PathParam("ruleId") final long ruleId) {
        ValidationRule rule = transactionService.execute((Transaction<ValidationRule>) () -> {
            Optional<? extends ValidationRuleSet> ruleSetRef = validationService.getValidationRuleSet(ruleSetId);
            if (!ruleSetRef.isPresent() || ruleSetRef.get().getObsoleteDate() != null) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }

            ValidationRuleSetVersion ruleSetVersion = getValidationRuleVersionFromSetOrThrowException(ruleSetRef.get(), ruleSetVersionId);
            return getValidationRuleFromVersionOrThrowException(ruleSetVersion, ruleId);
        });
        return Response.ok(new ValidationRuleInfo(rule)).build();
    }

    @DELETE
    @Path("/{ruleSetId}/versions/{ruleSetVersionId}/rules/{ruleId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION)
    public Response removeRule(@PathParam("ruleSetId") final long ruleSetId,
                               @PathParam("ruleSetVersionId") final long ruleSetVersionId,
                               @PathParam("ruleId") final long ruleId) {
        transactionService.execute(() -> {
            Optional<? extends ValidationRuleSet> ruleSetRef = validationService.getValidationRuleSet(ruleSetId);
            if (!ruleSetRef.isPresent() || ruleSetRef.get().getObsoleteDate() != null) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }

            ValidationRuleSetVersion ruleSetVersion = getValidationRuleVersionFromSetOrThrowException(ruleSetRef.get(), ruleSetVersionId);
            ValidationRule validationRule = getValidationRuleFromVersionOrThrowException(ruleSetVersion, ruleId);

            ruleSetVersion.deleteRule(validationRule);
            return null;
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
    public ReadingTypeInfos getReadingTypesForRule(@PathParam("ruleSetId") final long ruleSetId,
                                                   @PathParam("ruleSetVersionId") final long ruleSetVersionId,
                                                   @PathParam("ruleId") long ruleId, @Context SecurityContext securityContext) {
        ReadingTypeInfos infos = new ReadingTypeInfos();

        Optional<? extends ValidationRuleSet> ruleSetRef = validationService.getValidationRuleSet(ruleSetId);
        if (!ruleSetRef.isPresent() || ruleSetRef.get().getObsoleteDate() != null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        ValidationRuleSetVersion ruleSetVersion = getValidationRuleVersionFromSetOrThrowException(ruleSetRef.get(), ruleSetVersionId);
        ValidationRule validationRule = getValidationRuleFromVersionOrThrowException(ruleSetVersion, ruleId);
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
