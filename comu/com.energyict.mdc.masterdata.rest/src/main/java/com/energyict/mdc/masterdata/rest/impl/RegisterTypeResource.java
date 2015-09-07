package com.energyict.mdc.masterdata.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.masterdata.rest.RegisterTypeInfo;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Path("/registertypes")
public class RegisterTypeResource {

    private final MasterDataService masterDataService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final MeteringService meteringService;

    @Inject
    public RegisterTypeResource(MasterDataService masterDataService, DeviceConfigurationService deviceConfigurationService, MeteringService meteringService) {
        super();
        this.masterDataService = masterDataService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.meteringService = meteringService;
    }

    /**
     * We should filter out the ChannelTypes from the RegisterTypes list as these have an interval.
     */
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_MASTER_DATA, Privileges.Constants.VIEW_MASTER_DATA})
    public PagedInfoList getRegisterTypes(@BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        Stream<RegisterType> registerTypeStream = null;
        if (filter.hasProperty("ids")) {
            // case for remote filtering for buffered store
            List<Long> registerTypesAlreadyInUse = filter.getLongList("ids");
            registerTypeStream = this.masterDataService.findAllRegisterTypes().stream()
                .filter(regType -> !registerTypesAlreadyInUse.contains(regType.getId()))
                .skip(queryParameters.getStart().get())
                .limit(queryParameters.getLimit().get() + 1);
        } else {
            registerTypeStream = this.masterDataService.findAllRegisterTypes().from(queryParameters).stream();
        }
        List<RegisterTypeInfo> registerTypeInfos = registerTypeStream
            .map(registerType -> new RegisterTypeInfo(registerType, this.deviceConfigurationService.isRegisterTypeUsedByDeviceType(registerType), false))
            .collect(toList());
        return PagedInfoList.fromPagedList("registerTypes", registerTypeInfos, queryParameters);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_MASTER_DATA, Privileges.Constants.VIEW_MASTER_DATA})
    public RegisterTypeInfo getRegisterType(@PathParam("id") long id) {
        RegisterType registerType = this.findRegisterTypeByIdOrThrowException(id);
        return new RegisterTypeInfo(registerType, this.deviceConfigurationService.isRegisterTypeUsedByDeviceType(registerType), false);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_MASTER_DATA)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response deleteRegisterType(@PathParam("id") long id) {
        MeasurementType measurementType = this.findRegisterTypeByIdOrThrowException(id);
        measurementType.delete();
        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_MASTER_DATA)
    public RegisterTypeInfo createRegisterType(RegisterTypeInfo registerTypeInfo) {
        ReadingType readingType = findReadingType(registerTypeInfo);
        MeasurementType measurementType = this.masterDataService.newRegisterType(readingType, registerTypeInfo.obisCode);
        registerTypeInfo.writeTo(measurementType, findReadingType(registerTypeInfo));
        measurementType.save();
        return new RegisterTypeInfo(measurementType, false, false); // It's a new one so cannot be used yet in a DeviceType right
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_MASTER_DATA)
    public RegisterTypeInfo updateRegisterType(@PathParam("id") long id, RegisterTypeInfo registerTypeInfo) {
        RegisterType registerType = this.findRegisterTypeByIdOrThrowException(id);
        registerTypeInfo.writeTo(registerType, findReadingType(registerTypeInfo));
        registerType.save();
        return new RegisterTypeInfo(registerType, this.deviceConfigurationService.isRegisterTypeUsedByDeviceType(registerType), false);
    }

    private ReadingType findReadingType(RegisterTypeInfo registerTypeInfo) {
        return registerTypeInfo.readingType != null ? meteringService.getReadingType(registerTypeInfo.readingType.mRID).orElse(null) : null;
    }

    private RegisterType findRegisterTypeByIdOrThrowException(long id) {
        return masterDataService
            .findRegisterType(id)
            .orElseThrow(() -> new WebApplicationException("No register type with id " + id, Response.Status.NOT_FOUND));
    }
}