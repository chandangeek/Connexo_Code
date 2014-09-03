package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Comparator;
import java.util.List;

public class RegisterResource {

    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;
    private final Provider<RegisterDataResource> registerDataResourceProvider;

    @Inject
    public RegisterResource(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory, Provider<RegisterDataResource> registerDataResourceProvider) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.registerDataResourceProvider = registerDataResourceProvider;
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
        List<RegisterInfo> registerInfos = RegisterInfoFactory.asInfoList(registers);
        return PagedInfoList.asJson("data", registerInfos, queryParameters);
    }

    @GET
    @Path("/{registerId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_DEVICE)
    public RegisterInfo getRegister(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        Register register = resourceHelper.findRegisterOrThrowException(device, registerId);
        return RegisterInfoFactory.asInfo(register);
    }

    @Path("/{registerId}/data")
    public RegisterDataResource getRegisterDataResource() {
        return registerDataResourceProvider.get();
    }
}
