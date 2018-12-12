/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.prepayment.impl;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.prepayment.impl.servicecall.ServiceCallCommands;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

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
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by bvn on 9/16/15.
 */
@Path("usagepoints/{mrid}")
public class UsagePointResource {

    private final MeteringService meteringService;
    private final ExceptionFactory exceptionFactory;
    private final TransactionService transactionService;
    private final ServiceCallCommands serviceCallCommands;
    private final HeadEndController headEndController;

    @Inject
    public UsagePointResource(MeteringService meteringService, ExceptionFactory exceptionFactory, TransactionService transactionService, ServiceCallCommands serviceCallCommands, HeadEndController headEndController) {
        this.meteringService = meteringService;
        this.exceptionFactory = exceptionFactory;
        this.transactionService = transactionService;
        this.serviceCallCommands = serviceCallCommands;
        this.headEndController = headEndController;
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/contactor")
    public Response updateContactor(@PathParam("mrid") String mRID, ContactorInfo contactorInfo, @Context UriInfo uriInfo) {
        UsagePoint usagePoint = null;
        ServiceCall serviceCall = null;
        try (TransactionContext context = transactionService.getContext()) {
            try {
                usagePoint = findUsagePoint(mRID);
                List<EndDevice> endDevices = findEndDeviceThroughUsagePoint(usagePoint);
                serviceCall = serviceCallCommands.createContactorOperationServiceCall(Optional.of(usagePoint), contactorInfo);
                serviceCallCommands.requestTransition(serviceCall, DefaultState.PENDING);
                serviceCallCommands.requestTransition(serviceCall, DefaultState.ONGOING);    // Immediately transit to 'ONGOING' state
                validateContactorInfo(serviceCall, contactorInfo);
                for (EndDevice endDevice : endDevices) {
                    serviceCall.log(LogLevel.INFO, "Handling operations for end device with MRID " + endDevice.getMRID());
                    headEndController.performContactorOperations(endDevice, serviceCall, contactorInfo);
                }

                serviceCallCommands.requestTransition(serviceCall, DefaultState.WAITING);
                context.commit();
                return Response.accepted().build();
            } catch (RuntimeException e) {
                //TODO: should we take specific measures to prevent DDoS attacks? (cause now we create a new ServiceCall object for each incoming request)
                if (serviceCall == null) {
                    serviceCall = serviceCallCommands.createContactorOperationServiceCall(Optional.ofNullable(usagePoint), contactorInfo);
                }
                serviceCallCommands.rejectServiceCall(serviceCall, e.getMessage() != null ? e.getMessage() : e.toString());
                context.commit();
                return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
            }
        }
    }

    /**
     * Validate the specified ContactorInfo contains valid data
     */
    private void validateContactorInfo(ServiceCall serviceCall, ContactorInfo contactorInfo) {
        serviceCall.log(LogLevel.INFO, "Received parameters: " + contactorInfo.toString());
        if (contactorInfo.status == null && contactorInfo.loadLimit == null) {
            throw exceptionFactory.newException(MessageSeeds.INCOMPLETE_CONTACTOR_INFO);
        }
        if (contactorInfo.activationDate == null) {
            contactorInfo.activationDate = Instant.now();
        }

        if (contactorInfo.loadLimit != null) {
            if (contactorInfo.loadLimit.limit == null || (!contactorInfo.loadLimit.limit.equals(BigDecimal.ZERO) && contactorInfo.loadLimit.unit == null)) {
                throw exceptionFactory.newException(MessageSeeds.INCOMPLETE_LOADLIMIT);
            } else if (!contactorInfo.loadLimit.limit.equals(BigDecimal.ZERO) && !contactorInfo.loadLimit.getUnit().isPresent()) {
                throw exceptionFactory.newException(MessageSeeds.UNKNOWN_UNIT_CODE);
            }
            if (contactorInfo.loadTolerance != null && contactorInfo.loadTolerance < 0) {
                serviceCall.log(LogLevel.WARNING, "The specified load tolerance contains a negative value; this value will be ignored (the load tolerance will remain untouched).");
                contactorInfo.loadTolerance = null; // If tolerance is negative, then ignore it
            }
        } else if (contactorInfo.loadTolerance != null) {
            throw exceptionFactory.newException(MessageSeeds.TOLERANCE_WITHOUT_LOAD_LIMIT);
        }
    }

    private UsagePoint findUsagePoint(String mRID) {
        return meteringService.findUsagePointByMRID(mRID).orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_USAGE_POINT));
    }

    private List<EndDevice> findEndDeviceThroughUsagePoint(UsagePoint usagePoint) {
        List<MeterActivation> meterActivations = usagePoint.getCurrentMeterActivations();
        if (!meterActivations.isEmpty()) {
            List<EndDevice> endDevices = new ArrayList<>();
            meterActivations.stream().forEach(meterActivation -> endDevices.add(meterActivation.getMeter().orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_METER_IN_ACTIVATION))));
            return endDevices;
        } else {
            throw exceptionFactory.newException(MessageSeeds.NO_CURRENT_METER_ACTIVATION);
        }
    }
}