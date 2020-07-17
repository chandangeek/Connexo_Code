/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.processes.keyrenewal.api.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.processes.keyrenewal.api.impl.csr.CertificateRequestForCSRHandlerFactory;
import com.energyict.mdc.processes.keyrenewal.api.impl.csr.CertificateRequestForCSRMessage;
import com.energyict.mdc.processes.keyrenewal.api.impl.servicecall.ServiceCallCommands;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
    private final SecurityManagementService securityManagementService;
    private final ServiceCallService serviceCallService;
    private final MessageService messageService;
    private final JsonService jsonService;

    @Inject
    public DeviceResource(DeviceService deviceService, ExceptionFactory exceptionFactory, TransactionService transactionService,
                          ServiceCallCommands serviceCallCommands, HeadEndController headEndController, MeteringService meteringService, SecurityManagementService securityManagementService, ServiceCallService serviceCallService, MessageService messageService, JsonService jsonService) {
        this.deviceService = deviceService;
        this.exceptionFactory = exceptionFactory;
        this.transactionService = transactionService;
        this.serviceCallCommands = serviceCallCommands;
        this.headEndController = headEndController;
        this.meteringService = meteringService;
        this.securityManagementService = securityManagementService;
        this.serviceCallService = serviceCallService;
        this.messageService = messageService;
        this.jsonService = jsonService;
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
                device = findDeviceByMridOrThrowException(mRID);
                EndDevice endDevice = findEndDeviceByMridOrThrowException(mRID);
                serviceCall = createRenewKeyServiceCallAndTransition(deviceCommandInfo, device);
                validateDeviceCommandInfo(serviceCall, deviceCommandInfo);

                serviceCall.log(LogLevel.INFO, "Handling operations for end device with MRID " + endDevice.getMRID());
                headEndController.performOperations(endDevice, serviceCall, deviceCommandInfo, device);
                if (!serviceCall.getState().equals(DefaultState.SUCCESSFUL)) {
                    serviceCallCommands.requestTransition(serviceCall, DefaultState.WAITING);
                }

                context.commit();
                return Response.accepted().build();
            } catch (RuntimeException e) {
                return handleException(deviceCommandInfo, serviceCall, device, context, e);
            }
        }
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{mRID}/signCSR")
    public Response signCSR(@PathParam("mRID") String mRID, DeviceCommandInfo deviceCommandInfo, @Context UriInfo uriInfo) {
        ServiceCall serviceCall = null;
        Device device = null;
        try (TransactionContext context = transactionService.getContext()) {
            try {
                device = findDeviceByMridOrThrowException(mRID);
                EndDevice endDevice = findEndDeviceByMridOrThrowException(mRID);
                serviceCall = createRenewKeyServiceCallAndTransition(deviceCommandInfo, device);

                serviceCall.log(LogLevel.INFO, "Handling operations for end device with MRID " + endDevice.getMRID());

                serviceCall = serviceCallService.getServiceCall(serviceCall.getId())
                        .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.COULD_NOT_FIND_SERVICE_CALL));
                DestinationSpec destinationSpec = messageService.getDestinationSpec(CertificateRequestForCSRHandlerFactory.CERTIFICATE_REQUEST_FOR_CSR_DESTINATION)
                        .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.COULD_NOT_FIND_DESTINATION_SPEC, CertificateRequestForCSRHandlerFactory.CERTIFICATE_REQUEST_FOR_CSR_DESTINATION));
                CertificateRequestForCSRMessage message = new CertificateRequestForCSRMessage();;
                message.serviceCall = serviceCall.getId();
                message.device = device.getId();
                message.securityAccessor = deviceCommandInfo.keyAccessorType;
                destinationSpec.message(jsonService.serialize(message)).send();
                context.commit();
                return Response.accepted().build();
            } catch (RuntimeException e) {
                return handleException(deviceCommandInfo, serviceCall, device, context, e);
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
                device = findDeviceByMridOrThrowException(mRID);
                EndDevice endDevice = findEndDeviceByMridOrThrowException(mRID);
                serviceCall = createRenewKeyServiceCallAndTransition(deviceCommandInfo, device);
                validateDeviceCommandInfo(serviceCall, deviceCommandInfo);

                serviceCall.log(LogLevel.INFO, "Performing test comminication for end device with MRID " + endDevice.getMRID());
                headEndController.performTestCommunication(endDevice, serviceCall, deviceCommandInfo, device);
                serviceCallCommands.requestTransition(serviceCall, DefaultState.WAITING);

                context.commit();
                return Response.accepted().build();
            } catch (RuntimeException e) {
                return handleException(deviceCommandInfo, serviceCall, device, context, e);
            }
        }
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{mRID}/testCommunicationForSecuritySet")
    public Response testCommunicationForSecuritySet(@PathParam("mRID") String mRID, DeviceCommandInfo deviceCommandInfo, @Context UriInfo uriInfo) {
        ServiceCall serviceCall = null;
        Device device = null;
        try (TransactionContext context = transactionService.getContext()) {
            try {
                device = findDeviceByMridOrThrowException(mRID);
                EndDevice endDevice = findEndDeviceByMridOrThrowException(mRID);
                serviceCall = createRenewKeyServiceCallAndTransition(deviceCommandInfo, device);
                validateDeviceCommandInfo(serviceCall, deviceCommandInfo);

                serviceCall.log(LogLevel.INFO, "Performing test comminication for end device with MRID " + endDevice.getMRID());
                headEndController.performTestCommunicationForSecuritySet(endDevice, serviceCall, deviceCommandInfo, device);
                serviceCallCommands.requestTransition(serviceCall, DefaultState.WAITING);

                context.commit();
                return Response.accepted().build();
            } catch (RuntimeException e) {
                return handleException(deviceCommandInfo, serviceCall, device, context, e);
            }
        }
    }

    private ServiceCall createRenewKeyServiceCallAndTransition(DeviceCommandInfo deviceCommandInfo, Device device) {
        ServiceCall serviceCall;
        serviceCall = serviceCallCommands.createRenewKeyServiceCall(Optional.of(device), deviceCommandInfo);
        serviceCallCommands.requestTransition(serviceCall, DefaultState.PENDING);
        serviceCallCommands.requestTransition(serviceCall, DefaultState.ONGOING);
        return serviceCall;
    }

    private Device findDeviceByMridOrThrowException(String mRID) {
        return deviceService.findDeviceByMrid(mRID)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));
    }

    private EndDevice findEndDeviceByMridOrThrowException(String mRID) {
        return meteringService.findEndDeviceByMRID(mRID)
                .orElseThrow((exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE)));
    }

    private Response handleException(DeviceCommandInfo deviceCommandInfo, ServiceCall serviceCall, Device device, TransactionContext context, Exception e) {
        if (serviceCall == null) {
            serviceCall = serviceCallCommands.createRenewKeyServiceCall(Optional.ofNullable(device), deviceCommandInfo);
        }
        serviceCallCommands.rejectServiceCall(serviceCall, e.getMessage() != null ? e.getMessage() : e.toString());
        context.commit();
        return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
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
