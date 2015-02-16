package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/registertypes")
public class RegisterTypeResource {

    private final MasterDataService masterDataService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final MeteringService meteringService;
    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public RegisterTypeResource(MasterDataService masterDataService, DeviceConfigurationService deviceConfigurationService, MeteringService meteringService, ResourceHelper resourceHelper, ExceptionFactory exceptionFactory) {
        super();
        this.masterDataService = masterDataService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.meteringService = meteringService;
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
    }

    /**
     * We should filter out the ChannelTypes from the RegisterTypes list as these have an interval.
     */
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_MASTER_DATA, Privileges.VIEW_MASTER_DATA})
    public PagedInfoList getRegisterTypes(@BeanParam QueryParameters queryParameters) {
        List<RegisterType> registerTypes = this.masterDataService.findAllRegisterTypes().from(queryParameters).find();
        List<RegisterTypeInfo> registerTypeInfos = new ArrayList<>();
        for (RegisterType registerType : registerTypes) {
            registerTypeInfos.add(new RegisterTypeInfo(registerType, this.deviceConfigurationService.isRegisterTypeUsedByDeviceType(registerType), false));
        }
        return PagedInfoList.asJson("registerTypes", registerTypeInfos, queryParameters);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_MASTER_DATA, Privileges.VIEW_MASTER_DATA})
    public RegisterTypeInfo getRegisterType(@PathParam("id") long id) {
        RegisterType registerType = this.resourceHelper.findRegisterTypeByIdOrThrowException(id);
        return new RegisterTypeInfo(registerType, this.deviceConfigurationService.isRegisterTypeUsedByDeviceType(registerType), false);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed(Privileges.ADMINISTRATE_MASTER_DATA)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response deleteRegisterType(@PathParam("id") long id) {
        MeasurementType measurementType = this.resourceHelper.findRegisterTypeByIdOrThrowException(id);
        measurementType.delete();
        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_MASTER_DATA)
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
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_MASTER_DATA)
    public RegisterTypeInfo updateRegisterType(@PathParam("id") long id, RegisterTypeInfo registerTypeInfo) {
        RegisterType registerType = this.resourceHelper.findRegisterTypeByIdOrThrowException(id);
        registerTypeInfo.writeTo(registerType, findReadingType(registerTypeInfo));
        registerType.save();
        return new RegisterTypeInfo(registerType, this.deviceConfigurationService.isRegisterTypeUsedByDeviceType(registerType), false);
    }

    private ReadingType findReadingType(RegisterTypeInfo registerTypeInfo) {
        return meteringService.getReadingType(registerTypeInfo.readingType.mRID).orElse(null);
    }
}