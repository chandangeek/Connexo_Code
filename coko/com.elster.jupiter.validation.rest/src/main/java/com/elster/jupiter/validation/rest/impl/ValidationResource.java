/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionBuilder;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleBuilder;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.Validator;
import com.elster.jupiter.validation.ValidatorNotFoundException;
import com.elster.jupiter.validation.rest.ValidationActionInfos;
import com.elster.jupiter.validation.rest.ValidationRuleInfo;
import com.elster.jupiter.validation.rest.ValidationRuleInfoFactory;
import com.elster.jupiter.validation.rest.ValidationRuleInfos;
import com.elster.jupiter.validation.rest.ValidationRuleSetInfo;
import com.elster.jupiter.validation.rest.ValidationRuleSetInfos;
import com.elster.jupiter.validation.rest.ValidationRuleSetVersionInfo;
import com.elster.jupiter.validation.rest.ValidationRuleSetVersionInfos;
import com.elster.jupiter.validation.rest.ValidatorInfo;
import com.elster.jupiter.validation.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
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
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

@Path("/validation")
public class ValidationResource {

    private static final String APPLICATION_HEADER_PARAM = "X-CONNEXO-APPLICATION-NAME";
    private final RestQueryService queryService;
    private final ValidationService validationService;
    private final TransactionService transactionService;
    private final ValidationRuleInfoFactory validationRuleInfoFactory;
    private final PropertyValueInfoService propertyValueInfoService;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final Thesaurus thesaurus;

    @Inject
    public ValidationResource(RestQueryService queryService,
                              ValidationService validationService,
                              TransactionService transactionService,
                              ValidationRuleInfoFactory validationRuleInfoFactory,
                              PropertyValueInfoService propertyValueInfoService,
                              ConcurrentModificationExceptionFactory conflictFactory,
                              Thesaurus thesaurus) {
        this.queryService = queryService;
        this.validationService = validationService;
        this.transactionService = transactionService;
        this.validationRuleInfoFactory = validationRuleInfoFactory;
        this.propertyValueInfoService = propertyValueInfoService;
        this.conflictFactory = conflictFactory;
        this.thesaurus = thesaurus;
    }

    private QualityCodeSystem getQualityCodeSystemFromApplicationName(@HeaderParam(APPLICATION_HEADER_PARAM) String applicationName) {
        // TODO kore shouldn't know anything about applications, to be fixed
        return "MDC".equals(applicationName) ? QualityCodeSystem.MDC : QualityCodeSystem.MDM;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.Constants.VIEW_VALIDATION_CONFIGURATION,
            Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE, Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})
    public Response getValidationRuleSets(@HeaderParam(APPLICATION_HEADER_PARAM) String applicationName, @Context UriInfo uriInfo) {
        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<ValidationRuleSet> list = queryRuleSets(params, getQualityCodeSystemFromApplicationName(applicationName));

        ValidationRuleSetInfos infos = new ValidationRuleSetInfos(params.clipToLimit(list));
        infos.total = params.determineTotal(list.size());

        return Response.ok(infos).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION)
    public Response createValidationRuleSet(final ValidationRuleSetInfo info, @HeaderParam(APPLICATION_HEADER_PARAM) String applicationName) {
        QualityCodeSystem qualityCodeSystem = getQualityCodeSystemFromApplicationName(applicationName);

        return Response.status(Response.Status.CREATED)
                .entity(new ValidationRuleSetInfo(transactionService.execute(
                        () -> validationService.createValidationRuleSet(info.name, qualityCodeSystem, info.description)))).build();
    }

    @PUT
    @Path("/{ruleSetId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION)
    public ValidationRuleSetInfo updateValidationRuleSet(@PathParam("ruleSetId") long ruleSetId, ValidationRuleSetInfo info) {
        info.id = ruleSetId;
        transactionService.execute(() -> {
            ValidationRuleSet set = findAndLockValidationRuleSet(info);
            set.setName(info.name);
            set.setDescription(info.description);
            set.save();
            return null;
        });
        return getValidationRuleSet(ruleSetId);
    }

    class RuleSetUsageInfo {
        public boolean isInUse;
    }

    @GET
    @Path("/{ruleSetId}/usage")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.Constants.VIEW_VALIDATION_CONFIGURATION})
    public Response getValidationRuleSetUsage(@PathParam("ruleSetId") final long ruleSetId) {
        ValidationRuleSet validationRuleSet = fetchValidationRuleSet(ruleSetId);
        RuleSetUsageInfo info = new RuleSetUsageInfo();
        info.isInUse = validationService.isValidationRuleSetInUse(validationRuleSet);
        return Response.status(Response.Status.OK).entity(info).build();
    }

    @DELETE
    @Path("/{ruleSetId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION)
    public Response deleteValidationRuleSet(@PathParam("ruleSetId") long ruleSetId, ValidationRuleSetInfo info) {
        info.id = ruleSetId;
        ValidationRuleSet validationRuleSet = findAndLockValidationRuleSet(info);
        if (validationService.isValidationRuleSetInUse(validationRuleSet) && validationRuleSet.getQualityCodeSystem().equals(QualityCodeSystem.MDM)) {
            throw new ValidationRuleSetInUseLocalizedException(thesaurus, validationRuleSet);
        } else {
            transactionService.execute(() -> {
                validationRuleSet.delete();
                return null;
            });
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    private List<ValidationRuleSet> queryRuleSets(QueryParameters queryParameters, QualityCodeSystem qualityCodeSystem) {
        Query<ValidationRuleSet> query = validationService.getRuleSetQuery();
        query.setRestriction(where("qualityCodeSystem").isEqualTo(qualityCodeSystem));
        RestQuery<ValidationRuleSet> restQuery = queryService.wrap(query);
        return restQuery.select(queryParameters, Order.ascending("upper(name)"));
    }

    ////// VERSIONS
    @GET
    @Path("/{ruleSetId}/versions")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.Constants.VIEW_VALIDATION_CONFIGURATION,
            Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE, Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})
    public Response getRuleSetVersions(@PathParam("ruleSetId") long ruleSetId, @Context UriInfo uriInfo, @Context SecurityContext securityContext) {

        ValidationRuleSet ruleSet = validationService.getValidationRuleSet(ruleSetId).orElseThrow(
                () -> new WebApplicationException(Response.Status.NOT_FOUND));

        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());

        List<? extends ValidationRuleSetVersion> versions;
        if (queryParameters.size() == 0) {
            versions = ruleSet.getRuleSetVersions();
        } else {
            versions = ruleSet.getRuleSetVersions(queryParameters.getStartInt(), queryParameters.getLimit());
        }
        ValidationRuleSetVersionInfos infos = new ValidationRuleSetVersionInfos();
        infos.addAll(versions);
        return Response.ok(infos).build();
    }

    @POST
    @Path("/{ruleSetId}/versions")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION)
    public Response createRuleSetVersion(@PathParam("ruleSetId") long ruleSetId, ValidationRuleSetVersionInfo info) {

        ValidationRuleSetVersionInfo result =
                transactionService.execute(() -> {
                    ValidationRuleSet ruleSet = validationService.getValidationRuleSet(ruleSetId).orElseThrow(
                            () -> new WebApplicationException(Response.Status.NOT_FOUND));

                    ValidationRuleSetVersion version = ruleSet.addRuleSetVersion(info.description, makeInstant(info.startDate));
                    ruleSet.save();
                    return new ValidationRuleSetVersionInfo(version);
                });
        return Response.status(Response.Status.CREATED).entity(result).build();
    }

    @DELETE
    @Path("/{ruleSetId}/versions/{ruleSetVersionId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION)
    public Response deleteRuleSetVersion(@PathParam("ruleSetId") long ruleSetId, @PathParam("ruleSetVersionId") long ruleSetVersionId, ValidationRuleSetVersionInfo info) {
        info.id = ruleSetVersionId;
        transactionService.execute(() -> {
            ValidationRuleSetVersion ruleSetVersion = findAndLockValidationRuleSetVersion(info);
            ruleSetVersion.getRuleSet().deleteRuleSetVersion(ruleSetVersion);
            return null;
        });
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @POST
    @Path("/{ruleSetId}/versions/{ruleSetVersionId}/clone")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION)
    public Response cloneRuleSetVersion(@PathParam("ruleSetId") long ruleSetId,
                                        @PathParam("ruleSetVersionId") final long ruleSetVersionId,
                                        ValidationRuleSetVersionInfo info) {

        ValidationRuleSetVersionInfo result =
                transactionService.execute(() -> {
                    ValidationRuleSet ruleSet = validationService.getValidationRuleSet(ruleSetId).orElseThrow(
                            () -> new WebApplicationException(Response.Status.NOT_FOUND));

                    ValidationRuleSetVersion version = ruleSet.cloneRuleSetVersion(ruleSetVersionId, info.description, makeInstant(info.startDate));
                    ruleSet.save();
                    return new ValidationRuleSetVersionInfo(version);
                });
        return Response.status(Response.Status.CREATED).entity(result).build();
    }

    @PUT
    @Path("/{ruleSetId}/versions/{ruleSetVersionId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION)
    public Response updateRuleSetVersion(@PathParam("ruleSetId") long ruleSetId,
                                         @PathParam("ruleSetVersionId") long ruleSetVersionId,
                                         ValidationRuleSetVersionInfo info) {
        info.id = ruleSetVersionId;
        ValidationRuleSetVersionInfo result = transactionService.execute(() -> {
            ValidationRuleSet validationRuleSet = findAndLockValidationRuleSetVersion(info).getRuleSet();
            ValidationRuleSetVersion ruleSetVersion = validationRuleSet.updateRuleSetVersion(ruleSetVersionId, info.description, makeInstant(info.startDate));
            validationRuleSet.save();
            return new ValidationRuleSetVersionInfo(ruleSetVersion);
        });
        return Response.ok(result).build();
    }

    @GET
    @Path("/{ruleSetId}/versions/{ruleSetVersionId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.Constants.VIEW_VALIDATION_CONFIGURATION,
            Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE, Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})
    public Response getValidationRule(@PathParam("ruleSetId") long ruleSetId,
                                      @PathParam("ruleSetVersionId") final long ruleSetVersionId) {

        ValidationRuleSet ruleSet = validationService.getValidationRuleSet(ruleSetId).orElseThrow(
                () -> new WebApplicationException(Response.Status.NOT_FOUND));
        ValidationRuleSetVersion ruleSetVersion = getValidationRuleVersionFromSetOrThrowException(ruleSet, ruleSetVersionId);

        ValidationRuleSetVersionInfo info = new ValidationRuleSetVersionInfo(ruleSetVersion);
        return Response.ok(info).build();
    }

    ////// RULES
    @GET
    @Path("/{ruleSetId}/versions/{ruleSetVersionId}/rules")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.Constants.VIEW_VALIDATION_CONFIGURATION,
            Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE, Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})
    public Response getValidationRules(@PathParam("ruleSetId") long ruleSetId
            , @PathParam("ruleSetVersionId") final long ruleSetVersionId, @Context UriInfo uriInfo) {


        ValidationRuleSet ruleSet = validationService.getValidationRuleSet(ruleSetId).orElseThrow(
                () -> new WebApplicationException(Response.Status.NOT_FOUND));
        ValidationRuleSetVersion ruleSetVersion = getValidationRuleVersionFromSetOrThrowException(ruleSet, ruleSetVersionId);

        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());

        List<? extends ValidationRule> rules;
        if (queryParameters.size() == 0 || queryParameters.getStartInt() == 0) {
            rules = ruleSetVersion.getRules();
        } else {
            rules = ruleSetVersion.getRules(queryParameters.getStartInt(), queryParameters.getLimit());
        }
        ValidationRuleInfos infos = validationRuleInfoFactory.createValidationRuleInfos(rules.stream().map(validationRuleInfoFactory::createValidationRuleInfo).collect(Collectors.toList()));
        return Response.ok(infos).build();
    }

    @POST
    @Path("/{ruleSetId}/versions/{ruleSetVersionId}/rules")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION)
    public Response addRule(@PathParam("ruleSetId") final long ruleSetId,
                            @PathParam("ruleSetVersionId") final long ruleSetVersionId,
                            final ValidationRuleInfo info, @Context SecurityContext securityContext) {
        ValidationRuleInfo result =
                transactionService.execute(() -> {
                    ValidationRule validationRule = null;
                    ValidationRuleSet ruleSet = validationService.getValidationRuleSet(ruleSetId).orElseThrow(
                            () -> new WebApplicationException(Response.Status.NOT_FOUND));
                    ValidationRuleSetVersion ruleSetVersion = getValidationRuleVersionFromSetOrThrowException(ruleSet, ruleSetVersionId);

                    ValidationRuleBuilder ruleBuilder = ruleSetVersion.addRule(info.action, info.implementation, info.name)
                            .withReadingType(info.readingTypes.stream()
                                    .map(rtInfo -> rtInfo.mRID)
                                    .toArray(String[]::new));
                    try {
                        validationService.getValidator(info.implementation)
                                .getPropertySpecs()
                                .stream()
                                .map(spec -> Pair.of(spec.getName(), propertyValueInfoService.findPropertyValue(spec, info.properties)))
                                .filter(pair -> pair.getLast() != null)
                                .forEach(pair -> ruleBuilder.havingProperty(pair.getFirst()).withValue(pair.getLast()));
                    } catch (ValidatorNotFoundException ex) {
                        // we'll rely on validation of the save
                    } finally {
                        validationRule = ruleBuilder.create();
                    }
                    return validationRuleInfoFactory.createValidationRuleInfo(validationRule);
                });
        return Response.status(Response.Status.CREATED).entity(result).build();
    }

    @PUT
    @Path("/{ruleSetId}/versions/{ruleSetVersionId}/rules/{ruleId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION)
    public Response editRule(@PathParam("ruleSetId") long ruleSetId,
                             @PathParam("ruleSetVersionId") long ruleSetVersionId,
                             @PathParam("ruleId") long ruleId,
                             ValidationRuleInfo info) {
        info.id = ruleId;
        ValidationRule validationRule = transactionService.execute(() -> {
            ValidationRule rule = findAndLockValidationRule(info);
            ValidationRuleSetVersion ruleSetVersion = rule.getRuleSetVersion();

            List<String> mRIDs = info.readingTypes.stream().map(readingTypeInfo -> readingTypeInfo.mRID).collect(Collectors.toList());
            Map<String, Object> propertyMap = new HashMap<>();
            for (PropertySpec propertySpec : rule.getPropertySpecs()) {
                Object value = propertyValueInfoService.findPropertyValue(propertySpec, info.properties);
                if (value != null) {
                    propertyMap.put(propertySpec.getName(), value);
                }
            }
            rule = ruleSetVersion.updateRule(ruleId, info.name, info.active, info.action, mRIDs, propertyMap);
            ruleSetVersion.save();
            return rule;
        });
        ValidationRuleInfos result = validationRuleInfoFactory.createValidationRuleInfos(Collections.singletonList(validationRuleInfoFactory.createValidationRuleInfo(validationRule)));
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
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION)
    public Response getRule(@PathParam("ruleSetId") final long ruleSetId,
                            @PathParam("ruleSetVersionId") final long ruleSetVersionId,
                            @PathParam("ruleId") final long ruleId) {
        ValidationRule rule = transactionService.execute(() -> {
            Optional<? extends ValidationRuleSet> ruleSetRef = validationService.getValidationRuleSet(ruleSetId);
            if (!ruleSetRef.isPresent() || ruleSetRef.get().getObsoleteDate() != null) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }

            ValidationRuleSetVersion ruleSetVersion = getValidationRuleVersionFromSetOrThrowException(ruleSetRef.get(), ruleSetVersionId);
            return getValidationRuleFromVersionOrThrowException(ruleSetVersion, ruleId);
        });
        return Response.ok(validationRuleInfoFactory.createValidationRuleInfo(rule)).build();
    }

    @DELETE
    @Path("/{ruleSetId}/versions/{ruleSetVersionId}/rules/{ruleId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION)
    public Response removeRule(@PathParam("ruleSetId") final long ruleSetId,
                               @PathParam("ruleSetVersionId") final long ruleSetVersionId,
                               @PathParam("ruleId") final long ruleId,
                               ValidationRuleInfo info) {
        transactionService.execute(() -> {
            ValidationRule rule = findAndLockValidationRule(info);
            rule.getRuleSetVersion().deleteRule(rule);
            return null;
        });
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @GET
    @Path("/{ruleSetId}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.Constants.VIEW_VALIDATION_CONFIGURATION})
    public ValidationRuleSetInfo getValidationRuleSet(@PathParam("ruleSetId") long ruleSetId) {
        ValidationRuleSet validationRuleSet = fetchValidationRuleSet(ruleSetId);
        return new ValidationRuleSetInfo(validationRuleSet);
    }

    private ValidationRuleSet fetchValidationRuleSet(long id) {
        return validationService.getValidationRuleSet(id)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    @GET
    @Path("/actions")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.Constants.VIEW_VALIDATION_CONFIGURATION})
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
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.Constants.VIEW_VALIDATION_CONFIGURATION})
    public PagedInfoList getAvailableValidators(@HeaderParam(APPLICATION_HEADER_PARAM) String applicationName, @BeanParam JsonQueryParameters parameters) {
        QualityCodeSystem qualityCodeSystem = getQualityCodeSystemFromApplicationName(applicationName);

        List<ValidatorInfo> data = validationService.getAvailableValidators(qualityCodeSystem).stream()
                .sorted(Compare.BY_DISPLAY_NAME)
                .map(validator -> new ValidatorInfo(
                        validator.getClass().getName(),
                        validator.getDisplayName(),
                        propertyValueInfoService.getPropertyInfos(validator.getPropertySpecs())))
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("validators", data, parameters);
    }

    private enum Compare implements Comparator<Validator> {
        BY_DISPLAY_NAME;

        @Override
        public int compare(Validator o1, Validator o2) {
            return o1.getDisplayName().compareTo(o2.getDisplayName());
        }
    }

    private Instant makeInstant(Long startDate) {
        return startDate == null ? null : Instant.ofEpochMilli(startDate);
    }

    private Long getCurrentRuleSetVersion(long id) {
        return validationService.getValidationRuleSet(id)
                .filter(candidate -> candidate.getObsoleteDate() == null)
                .map(ValidationRuleSet::getVersion)
                .orElse(null);
    }

    private Long getCurrentRuleSetVersionVersion(long id) {
        return validationService.findValidationRuleSetVersion(id).map(ValidationRuleSetVersion::getVersion).orElse(null);
    }

    private Long getCurrentRuleVersion(long id) {
        return validationService.findValidationRule(id).map(ValidationRule::getVersion).orElse(null);
    }

    private Optional<? extends ValidationRuleSet> getLockedValidationRuleSet(long id, long version) {
        return validationService.findAndLockValidationRuleSetByIdAndVersion(id, version)
                .filter(candidate -> candidate.getObsoleteDate() == null);
    }

    private Optional<? extends ValidationRuleSetVersion> getLockedValidationRuleSetVersion(long id, long version) {
        return validationService.findAndLockValidationRuleSetVersionByIdAndVersion(id, version)
                .filter(candidate -> candidate.getObsoleteDate() == null);
    }

    private ValidationRuleSet findAndLockValidationRuleSet(ValidationRuleSetInfo info) {
        return getLockedValidationRuleSet(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> getCurrentRuleSetVersion(info.id))
                        .supplier());
    }

    private ValidationRuleSetVersion findAndLockValidationRuleSetVersion(ValidationRuleSetVersionInfo info) {
        Optional<? extends ValidationRuleSet> validationRuleSet = getLockedValidationRuleSet(info.parent.id, info.parent.version);
        ConcurrentModificationExceptionBuilder modificationExceptionBuilder = conflictFactory.contextDependentConflictOn(info.description);
        if (validationRuleSet.isPresent()) {
            return getLockedValidationRuleSetVersion(info.id, info.version)
                    .orElseThrow(modificationExceptionBuilder
                            .withActualParent(() -> getCurrentRuleSetVersion(info.parent.id), info.parent.id)
                            .withActualVersion(() -> getCurrentRuleSetVersionVersion(info.id))
                            .supplier());
        }
        throw modificationExceptionBuilder
                .withActualParent(() -> getCurrentRuleSetVersion(info.parent.id), info.parent.id)
                .withActualVersion(() -> getCurrentRuleSetVersionVersion(info.id))
                .build();
    }

    private ValidationRule findAndLockValidationRule(ValidationRuleInfo info) {
        Optional<? extends ValidationRuleSetVersion> ruleSetVersion = getLockedValidationRuleSetVersion(info.parent.id, info.parent.version);
        ConcurrentModificationExceptionBuilder modificationExceptionBuilder = conflictFactory.contextDependentConflictOn(info.name);
        if (ruleSetVersion.isPresent()) {
            return validationService.findAndLockValidationRuleByIdAndVersion(info.id, info.version)
                    .filter(candidate -> candidate.getObsoleteDate() == null)
                    .orElseThrow(modificationExceptionBuilder
                            .withActualParent(() -> getCurrentRuleSetVersionVersion(info.parent.id), info.parent.id)
                            .withActualVersion(() -> getCurrentRuleVersion(info.id))
                            .supplier());
        }
        throw modificationExceptionBuilder
                .withActualParent(() -> getCurrentRuleSetVersionVersion(info.parent.id), info.parent.id)
                .withActualVersion(() -> getCurrentRuleVersion(info.id))
                .build();
    }
}
