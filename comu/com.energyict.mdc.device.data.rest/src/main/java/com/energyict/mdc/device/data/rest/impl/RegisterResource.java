package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.security.Privileges;

import com.elster.jupiter.nls.LocalizedFieldValidationException;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RegisterResource {

    private final ResourceHelper resourceHelper;
    private final Provider<RegisterDataResource> registerDataResourceProvider;
    private final ValidationInfoHelper validationInfoHelper;
    private final DeviceDataInfoFactory deviceDataInfoFactory;
    private final Clock clock;

    @Inject
    public RegisterResource(ResourceHelper resourceHelper, Provider<RegisterDataResource> registerDataResourceProvider, ValidationInfoHelper validationInfoHelper, Clock clock, DeviceDataInfoFactory deviceDataInfoFactory) {
        this.resourceHelper = resourceHelper;
        this.registerDataResourceProvider = registerDataResourceProvider;
        this.clock = clock;
        this.validationInfoHelper = validationInfoHelper;
        this.deviceDataInfoFactory = deviceDataInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    public PagedInfoList getRegisters(@PathParam("mRID") String mRID, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        List<Register> registers = ListPager.of(device.getRegisters(),
                (r1, r2) -> r1.getRegisterSpec().getRegisterType().getReadingType().getAliasName().compareToIgnoreCase(r2.getRegisterSpec().getRegisterType().getReadingType().getAliasName()))
                .from(queryParameters).find();

        List<RegisterInfo> registerInfos = registers.stream().map(register -> deviceDataInfoFactory.createRegisterInfo(register, validationInfoHelper.getRegisterValidationInfo(register))).collect(Collectors.toList());
        return PagedInfoList.fromPagedList("data", registerInfos, queryParameters);
    }

    @GET
    @Path("/{registerId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    public RegisterInfo getRegister(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId) {
        Register<?> register = doGetRegister(mRID, registerId);
        return deviceDataInfoFactory.createRegisterInfo(register, validationInfoHelper.getRegisterValidationInfo(register));
    }

    @PUT
    @Path("/{registerId}/validate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({com.elster.jupiter.validation.security.Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION, com.elster.jupiter.validation.security.Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    public Response validateNow(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, TriggerValidationInfo validationInfo) {
        Register<?> register = doGetRegister(mRID, registerId);
        if (validationInfo.lastChecked == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.NULL_DATE, "lastChecked");
        }
        Instant newDate = Instant.ofEpochMilli(validationInfo.lastChecked);
        Optional<Instant> lastChecked = register.getDevice().forValidation().getLastChecked(register);
        if (lastChecked.isPresent() && newDate.isAfter(lastChecked.get())) {
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_DATE, "lastChecked", lastChecked);
        }
        validateRegister(register, newDate);
        return Response.status(Response.Status.OK).build();
    }

    private void validateRegister(Register<?> register, Instant start) {
    	if (start != null) {
    		register.getDevice().forValidation().setLastChecked(register, start);
    	}
        register.getDevice().forValidation().validateRegister(register);
    }

    private Register<?> doGetRegister(String mRID, long registerId) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        return resourceHelper.findRegisterOrThrowException(device, registerId);
    }

    @Path("/{registerId}/data")
    public RegisterDataResource getRegisterDataResource() {
        return registerDataResourceProvider.get();
    }

    @Path("{registerId}/validationstatus")
    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({com.elster.jupiter.validation.security.Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION,com.elster.jupiter.validation.security.Privileges.VIEW_VALIDATION_CONFIGURATION,com.elster.jupiter.validation.security.Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    public Response getValidationFeatureStatus(@PathParam("mRID") String mrid, @PathParam("registerId") long registerId) {
        Register<?> register = doGetRegister(mrid, registerId);
        ValidationStatusInfo validationStatusInfo = determineStatus(register);
        return Response.status(Response.Status.OK).entity(validationStatusInfo).build();
    }

    private ValidationStatusInfo determineStatus(Register<?> register) {
        return new ValidationStatusInfo(isValidationActive(register), register.getDevice().forValidation().getLastChecked(register), hasData(register));
    }

    private boolean isValidationActive(Register<?> register) {
        return register.getDevice().forValidation().isValidationActive(register, clock.instant());
    }

    private boolean hasData(Register<?> register) {
        return register.hasData();
    }

}