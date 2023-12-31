/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.fsm.StateTimeSlice;
import com.elster.jupiter.kore.api.impl.MessageSeeds;
import com.elster.jupiter.kore.api.security.Privileges;
import com.elster.jupiter.metering.EndDeviceStage;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointMeterActivator;
import com.elster.jupiter.metering.ami.EndDeviceCapabilities;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.api.util.v1.hypermedia.FieldSelection;
import com.elster.jupiter.rest.api.util.v1.hypermedia.JsonQueryParameters;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PagedInfoList;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.PROPFIND;
import com.elster.jupiter.rest.util.Transactional;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Path("/usagepoints/{mRID}/meteractivations")
public class MeterActivationResource {

    private final MeterActivationInfoFactory meterActivationInfoFactory;
    private final MeteringService meteringService;
    private final ExceptionFactory exceptionFactory;
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final Thesaurus thesaurus;
    private final ResourceHelper resourceHelper;

    @Inject
    public MeterActivationResource(MeterActivationInfoFactory meterActivationInfoFactory, MeteringService meteringService, MetrologyConfigurationService metrologyConfigurationService, ExceptionFactory exceptionFactory, Thesaurus thesaurus, ResourceHelper resourceHelper) {
        this.meterActivationInfoFactory = meterActivationInfoFactory;
        this.meteringService = meteringService;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.exceptionFactory = exceptionFactory;
        this.thesaurus = thesaurus;
        this.resourceHelper = resourceHelper;
    }

    /**
     * The meter activation records which meter was associated with a usage point during which time frame.
     *
     * @param mRID Unique identifier of the usage point
     * @param meterActivationId Unique identifier of the meter activation
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @return The meter activation on a usage point
     * @summary fetch a specific meter activation on a usage point by id
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{meterActivationId}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public MeterActivationInfo getMeterActivation(@PathParam("mRID") String mRID, @PathParam("meterActivationId") long meterActivationId, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        MeterActivation meterActivation = meteringService.findUsagePointByMRID(mRID)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_USAGE_POINT))
                .getMeterActivations().stream()
                .filter(ma -> ma.getId() == meterActivationId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_METER_ACTIVATION));
        return meterActivationInfoFactory.from(meterActivation, uriInfo, fieldSelection.getFields());
    }

    /**
     * The meter activation records which meter was associated with a usage point during which time frame.
     *
     * @param mRID Unique identifier of the usage point
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @param queryParameters query parameters
     * @return All meter activations on a usage point
     * @summary fetch all meter activation on a usage point
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public PagedInfoList<MeterActivationInfo> getMeterActivations(@PathParam("mRID") String mRID, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = meteringService.findUsagePointByMRID(mRID)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_USAGE_POINT));
        List<MeterActivationInfo> meterActivationInfos = usagePoint
                .getMeterActivations().stream()
                .map(ma -> meterActivationInfoFactory.from(ma, uriInfo, fieldSelection.getFields()))
                .collect(toList());
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(MeterActivationResource.class)
                .resolveTemplate("mRID", mRID);

        return PagedInfoList.from(meterActivationInfos, queryParameters, uriBuilder, uriInfo);
    }

    /**
     * The meter activation records which meter was associated with a usage point during which time frame.
     * When creating a meter activation, the start time must be provided. Meter is optional.
     *
     * @param mRID Unique identifier of the usage point
     * @param meterActivationInfo Description of the to be created meter activation
     * @param uriInfo uriInfo
     * @param validateOnly Indicates that only the validations need to be done without the actual object being created
     * @param replace Indicates that existing meter activation at new activation start time will be closed.
     * @return The created meter activation
     * @summary Create a new activation for a meter
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    @Transactional
    public MeterActivationInfo createMeterActivation(@PathParam("mRID") String mRID, @QueryParam("validateOnly") boolean validateOnly, @QueryParam("replace") boolean replace, @Context UriInfo uriInfo, MeterActivationInfo meterActivationInfo) {
        if (meterActivationInfo == null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.EMPTY_REQUEST);
        }
        UsagePoint usagePoint = meteringService.findUsagePointByMRID(mRID)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_USAGE_POINT));
        if (meterActivationInfo.meter == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_MISSING, "meter");
        }
        Meter meter = meteringService.findMeterByMRID(meterActivationInfo.meter)
                .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.NO_SUCH_METER, "meter"));
        if (meterActivationInfo.meterRole == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_MISSING, "meterRole");
        }
        MeterRole meterRole = metrologyConfigurationService.findMeterRole(meterActivationInfo.meterRole)
                .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.NO_SUCH_METER_ROLE, "meterRole", meterActivationInfo.meterRole));

        if (meterActivationInfo.interval == null || meterActivationInfo.interval.start == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_MISSING, "interval.start");
        }
        Instant start = Instant.ofEpochMilli(meterActivationInfo.interval.start).truncatedTo(ChronoUnit.MINUTES);

        if (validateOnly) {
            validateMeterActivationRequirements(usagePoint, meter, meterRole, start);
            return meterActivationInfo;
        }

        if (!meter.getState(start).filter(state -> state.getStage().filter(stage -> stage.getName().equals(EndDeviceStage.OPERATIONAL.getKey())).isPresent()).isPresent()) {
            start = getMeterActivationStartTime(start, meter, meterActivationInfo);
        }
        validateStartTime(usagePoint, start, meterRole, replace);

        UsagePointMeterActivator linker = usagePoint.linkMeters();
        if (replace && usagePoint.getMeterActivations(start).stream().anyMatch(meterActivation -> meterActivation.getMeterRole().filter(meterRole::equals).isPresent())) {
            linker.clear(start, meterRole);
        }
        linker.activate(start, meter, meterRole);
        linker.complete();

        MeterActivation activation = resourceHelper.findMeterActivation(usagePoint, meterRole, start)
                .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.NO_SUCH_METER_ACTIVATION_FOR_METER_ROLE, meterActivationInfo.meterRole));

        return meterActivationInfoFactory.from(activation, uriInfo, Collections.emptyList());
    }

    /**
     * The meter activation records which meter was associated with a usage point during which time frame.
     * When creating a meter activation, the start time must be provided. Meter is optional.
     *
     * @param mRID Unique identifier of the usage point
     * @param meterActivationInfo Description of the to be created meter activation
     * @param uriInfo uriInfo
     * @return The updated meter activation
     * @summary Update an activation for a meter
     */
    @PUT
    @Path("/{meterActivationId}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    @Transactional
    public MeterActivationInfo updateMeterActivation(@PathParam("mRID") String mRID, @Context UriInfo uriInfo, MeterActivationInfo meterActivationInfo) {
        if (meterActivationInfo == null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.EMPTY_REQUEST);
        }
        UsagePoint usagePoint = meteringService.findUsagePointByMRID(mRID)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_USAGE_POINT));
        if (meterActivationInfo.meterRole == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_MISSING, "meterRole");
        }
        MeterRole meterRole = metrologyConfigurationService.findMeterRole(meterActivationInfo.meterRole)
                .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.NO_SUCH_METER_ROLE, "meterRole", meterActivationInfo.meterRole));
        if (meterActivationInfo.interval == null || meterActivationInfo.interval.start == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_MISSING, "interval.start");
        }
        if (meterActivationInfo.interval.end == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_MISSING, "interval.end");
        }
        Instant start = Instant.ofEpochMilli(meterActivationInfo.interval.start).truncatedTo(ChronoUnit.MINUTES);
        Instant end = Instant.ofEpochMilli(meterActivationInfo.interval.end).truncatedTo(ChronoUnit.MINUTES);

        UsagePointMeterActivator linker = usagePoint.linkMeters();
        if (usagePoint.getMeterActivations(start).stream().anyMatch(meterActivation -> meterActivation.getMeterRole().filter(meterRole::equals).isPresent())) {
            linker.clear(end, meterRole);
        }
        linker.complete();

        MeterActivation activation = usagePoint.getMeterActivations(meterRole).stream().filter(ma -> ma.isEffectiveAt(start)).findFirst()
                .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.NO_SUCH_METER_ACTIVATION_FOR_METER_ROLE, meterActivationInfo.meterRole));

        return meterActivationInfoFactory.from(activation, uriInfo, Collections.emptyList());
    }

    private Instant getMeterActivationStartTime(Instant start, Meter meter, MeterActivationInfo meterActivationInfo) {
        StateTimeSlice state = meter.getStateTimeline().flatMap(stateTimeline -> stateTimeline.getSlices().stream()
                .filter(stateTimeSlice -> stateTimeSlice.getPeriod()
                        .lowerEndpoint()
                        .isAfter(Instant.ofEpochMilli(meterActivationInfo.interval.start).truncatedTo(ChronoUnit.MINUTES))
                        && stateTimeSlice.getState().getStage().filter(stage -> stage.getName().equals(EndDeviceStage.OPERATIONAL.getKey())).isPresent())
                .min(Comparator.comparing(slice -> slice.getPeriod().lowerEndpoint()))).orElse(null);

        if (state == null ){
            return start;
        }

        if (!state.getPeriod().lowerEndpoint().truncatedTo(ChronoUnit.MINUTES).equals(state.getPeriod().lowerEndpoint())) {
            return state.getPeriod().lowerEndpoint().plus(1, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MINUTES);
        } else {
            return state.getPeriod().lowerEndpoint();
        }
    }

    private void validateStartTime(UsagePoint usagePoint, Instant start, MeterRole meterRole, boolean isReplace) {
        for (MeterActivation meterActivation : usagePoint.getMeterActivations(meterRole)) {
            if (meterActivation.getStart().isAfter(start)) {
                throw new LocalizedFieldValidationException(MessageSeeds.INVALID_START_TIME, "interval.start");
            } else if (!isReplace && meterActivation.isEffectiveAt(start)) {
                throw new LocalizedFieldValidationException(MessageSeeds.INVALID_START_TIME, "interval.start");
            }
        }
    }

    private void validateMeterActivationRequirements(UsagePoint usagePoint, Meter meter, MeterRole meterRole, Instant instant) {
        Optional<EffectiveMetrologyConfigurationOnUsagePoint> metrologyConfigurationOnUsagePoint = usagePoint.getEffectiveMetrologyConfiguration(instant);
        if(metrologyConfigurationOnUsagePoint.isPresent()) {
            Set<ReadingTypeRequirement> requirements = metrologyConfigurationOnUsagePoint.get().getReadingTypeRequirements().stream()
                    .filter(readingTypeRequirement -> meterRole.equals(metrologyConfigurationOnUsagePoint.get().getMetrologyConfiguration()
                            .getMeterRoleFor(readingTypeRequirement)
                            .orElse(null)))
                    .collect(Collectors.toSet());

            List<ReadingType> meterProvidedReadingTypes = meter.getHeadEndInterface()
                    .map(headEndInterface -> headEndInterface.getCapabilities(meter))
                    .map(EndDeviceCapabilities::getConfiguredReadingTypes)
                    .orElse(Collections.emptyList());
            Set<ReadingTypeRequirement> unsatisfiedRequirements = requirements.stream()
                    .filter(requirement -> !meterProvidedReadingTypes.stream().anyMatch(requirement::matches))
                    .collect(Collectors.toSet());

            if (!unsatisfiedRequirements.isEmpty()) {
                throw new LocalizedFieldValidationException(MessageSeeds.UNSATISFIED_READING_TYPE_REQUIREMENTS,
                        "meter",
                        meter.getName(),
                        String.join(", ", metrologyConfigurationOnUsagePoint.get().getMetrologyConfiguration()
                                .getContracts()
                                .stream()
                                .filter(metrologyContract -> metrologyContract.getRequirements().stream().anyMatch(unsatisfiedRequirements::contains))
                                .map(mc -> mc.getMetrologyPurpose().getName())
                                .collect(Collectors.toList())),
                        usagePoint.getName());
            }
        }
    }

    /**
     * List the fields available on this type of entity.
     * <br>E.g.
     * <br>[
     * <br> "id",
     * <br> "name",
     * <br> "actions",
     * <br> "batch"
     * <br>]
     * <br>Fields in the list can be used as parameter on a GET request to the same resource, e.g.
     * <br> <i></i>GET ..../resource?fields=id,name,batch</i>
     * <br> The call above will return only the requested fields of the entity. In the absence of a field list, all fields
     * will be returned. If IDs are required in the URL for parent entities, then will be ignored when using the PROPFIND method.
     *
     * @return A list of field names that can be requested as parameter in the GET method on this entity type
     * @summary List the fields available on this type of entity
     */
    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public List<String> getFields() {
        return meterActivationInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }
}
