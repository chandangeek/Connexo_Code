package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.security.Privileges;
import com.google.common.base.Optional;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class RegisterResource {

    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;
    private final Provider<RegisterDataResource> registerDataResourceProvider;
    private final ValidationEvaluator evaluator;
    private final ValidationInfoHelper validationInfoHelper;
    private final Clock clock;

    @Inject
    public RegisterResource(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory, Provider<RegisterDataResource> registerDataResourceProvider, ValidationInfoHelper validationInfoHelper, ValidationService validationService, Clock clock) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.registerDataResourceProvider = registerDataResourceProvider;
        this.clock = clock;
        this.evaluator = validationService.getEvaluator();
        this.validationInfoHelper = validationInfoHelper;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_DEVICE)
    public PagedInfoList getRegisters(@PathParam("mRID") String mRID, @BeanParam QueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        List<Register> registers = ListPager.of(device.getRegisters(), new Comparator<Register>() {
            @Override
            public int compare(Register o1, Register o2) {
                return o1.getRegisterSpec().getRegisterType().getName().compareToIgnoreCase(o2.getRegisterSpec().getRegisterType().getName());
            }
        }).from(queryParameters).find();

        List<RegisterInfo> registerInfos = RegisterInfoFactory.asInfoList(registers, validationInfoHelper, evaluator);
        return PagedInfoList.asJson("data", registerInfos, queryParameters);
    }

    @GET
    @Path("/{registerId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_DEVICE)
    public RegisterInfo getRegister(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId) {
        Register<?> register = doGetRegister(mRID, registerId);
        return RegisterInfoFactory.asInfo(register, validationInfoHelper.getRegisterValidationInfo(register), evaluator);
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
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(com.elster.jupiter.validation.security.Privileges.VIEW_VALIDATION_CONFIGURATION)
    public Response getValidationFeatureStatus(@PathParam("mRID") String mrid, @PathParam("registerId") long registerId) {
        Register<?> register = doGetRegister(mrid, registerId);
        ValidationStatusInfo validationStatusInfo = determineStatus(register);
        return Response.status(Response.Status.OK).entity(validationStatusInfo).build();
    }

    private ValidationStatusInfo determineStatus(Register<?> register) {
        return new ValidationStatusInfo(isValidationActive(register), lastChecked(register), hasData(register));
    }

    private boolean isValidationActive(Register<?> register) {
        return register.getDevice().forValidation().isValidationActive(register, clock.now());
    }

    private boolean hasData(Register<?> channel) {
        return channel.getDevice().forValidation().hasData(channel);
    }

    private Date lastChecked(Register<?> register) {
        Optional<Date> optional =  register.getDevice().forValidation().getLastChecked(register);
        return (optional.isPresent()) ? optional.get() : null;
    }


}
