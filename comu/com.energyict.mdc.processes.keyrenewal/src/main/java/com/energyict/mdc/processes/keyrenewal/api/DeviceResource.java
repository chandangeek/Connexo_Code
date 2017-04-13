/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.processes.keyrenewal.api;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.processes.keyrenewal.api.servicecall.ServiceCallCommands;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.time.Instant;
import java.util.Optional;

@Path("/devices")
public class DeviceResource {

    private final DeviceService deviceService;
    private final ExceptionFactory exceptionFactory;
    private final TransactionService transactionService;
    private final ServiceCallCommands serviceCallCommands;
    private final HeadEndController headEndController;
    private final MeteringService meteringService;


    @Inject
    public DeviceResource(DeviceService deviceService, ExceptionFactory exceptionFactory, TransactionService transactionService,
                          ServiceCallCommands serviceCallCommands, HeadEndController headEndController, MeteringService meteringService) {
        this.deviceService = deviceService;
        this.exceptionFactory = exceptionFactory;
        this.transactionService = transactionService;
        this.serviceCallCommands = serviceCallCommands;
        this.headEndController = headEndController;
        this.meteringService = meteringService;
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{mRID}/renewkey")
    public Response renewKey(@PathParam("mRID") String mRID, DeviceCommandInfo deviceCommandInfo, @Context UriInfo uriInfo) {
        ServiceCall serviceCall = null;
        Device device = null;
        try (TransactionContext context = transactionService.getContext()) {
            try {
                device = deviceService.findDeviceByMrid(mRID)
                        .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));
                EndDevice endDevice = meteringService.findEndDeviceByMRID(mRID)
                        .orElseThrow((exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE)));

                serviceCall = serviceCallCommands.createRenewKeyServiceCall(Optional.of(device), deviceCommandInfo);
                serviceCallCommands.requestTransition(serviceCall, DefaultState.PENDING);
                serviceCallCommands.requestTransition(serviceCall, DefaultState.ONGOING);    // Immediately transit to 'ONGOING' state
                validateDeviceCommandInfo(serviceCall, deviceCommandInfo);
                //for (EndDevice endDevice : endDevices) {
                serviceCall.log(LogLevel.INFO, "Handling operations for end device with MRID " + endDevice.getMRID());
                headEndController.performOperations(endDevice, serviceCall, deviceCommandInfo, device);
                //}
                serviceCallCommands.requestTransition(serviceCall, DefaultState.WAITING);
                context.commit();
                return Response.accepted().build();
            } catch (RuntimeException e) {
                if (serviceCall == null) {
                    serviceCall = serviceCallCommands.createRenewKeyServiceCall(Optional.ofNullable(device), deviceCommandInfo);
                }
                serviceCallCommands.rejectServiceCall(serviceCall, e.getMessage() != null ? e.getMessage() : e.toString());
                context.commit();
                return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
            }
        }
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{mRID}/testCommunication")
    public Response testCommunication(@PathParam("mRID") String mRID, DeviceCommandInfo deviceCommandInfo, @Context UriInfo uriInfo) {
        ServiceCall serviceCall = null;
        Device device = null;
        try (TransactionContext context = transactionService.getContext()) {
            try {
                device = deviceService.findDeviceByMrid(mRID)
                        .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));
                EndDevice endDevice = meteringService.findEndDeviceByMRID(mRID)
                        .orElseThrow((exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE)));

                serviceCall = serviceCallCommands.createRenewKeyServiceCall(Optional.of(device), deviceCommandInfo);
                serviceCallCommands.requestTransition(serviceCall, DefaultState.PENDING);
                serviceCallCommands.requestTransition(serviceCall, DefaultState.ONGOING);    // Immediately transit to 'ONGOING' state
                validateDeviceCommandInfo(serviceCall, deviceCommandInfo);
                //for (EndDevice endDevice : endDevices) {
                serviceCall.log(LogLevel.INFO, "Handling operations for end device with MRID " + endDevice.getMRID());
                headEndController.performTestCommunication(endDevice, serviceCall, deviceCommandInfo, device);
                //}
                serviceCallCommands.requestTransition(serviceCall, DefaultState.WAITING);
                context.commit();
                return Response.accepted().build();
            } catch (RuntimeException e) {
                if (serviceCall == null) {
                    serviceCall = serviceCallCommands.createRenewKeyServiceCall(Optional.ofNullable(device), deviceCommandInfo);
                }
                serviceCallCommands.rejectServiceCall(serviceCall, e.getMessage() != null ? e.getMessage() : e.toString());
                context.commit();
                return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
            }
        }
    }

    private HeadEndInterface getHeadEndInterface(EndDevice endDevice) {
        return endDevice.getHeadEndInterface().orElseThrow(exceptionFactory.newExceptionSupplier(com.energyict.mdc.processes.keyrenewal.api.MessageSeeds.NO_HEAD_END_INTERFACE, endDevice.getMRID()));
    }


    /**
     * Validate the specified DeviceCommandInfo contains valid data
     */
    private void validateDeviceCommandInfo(ServiceCall serviceCall, DeviceCommandInfo deviceCommandInfo) {
        serviceCall.log(LogLevel.INFO, "Received parameters: " + deviceCommandInfo.toString());
        if (deviceCommandInfo.activationDate == null) {
            deviceCommandInfo.activationDate = Instant.now();
        }
        if (deviceCommandInfo.callbackError == null) {
            throw exceptionFactory.newException(MessageSeeds.CALL_BACK_ERROR_URI_NOT_SPECIFIED);
        }
        if (deviceCommandInfo.callbackSuccess == null) {
            throw exceptionFactory.newException(MessageSeeds.CALL_BACK_SUCCESS_URI_NOT_SPECIFIED);
        }
    }

}
