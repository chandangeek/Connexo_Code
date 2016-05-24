package com.elster.jupiter.prepayment.impl;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.prepayment.impl.fullduplex.FullDuplexController;
import com.elster.jupiter.prepayment.impl.servicecall.ServiceCallCommands;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.time.Clock;
import java.util.Optional;

/**
 * Created by bvn on 9/16/15.
 */
@Path("usagepoints/{mrid}")
public class UsagePointResource {

    private static final String UNDEFINED = "undefined";

    private final MeteringService meteringService;
    private final DeviceService deviceService;
    private final Clock clock;
    private final ExceptionFactory exceptionFactory;
    private final TransactionService transactionService;
    private final ServiceCallCommands serviceCallCommands;
    private final FullDuplexController fullDuplexController;

    @Inject
    public UsagePointResource(MeteringService meteringService, DeviceService deviceService, Clock clock, ExceptionFactory exceptionFactory, TransactionService transactionService, ServiceCallCommands serviceCallCommands, FullDuplexController fullDuplexController) {
        this.meteringService = meteringService;
        this.deviceService = deviceService;
        this.clock = clock;
        this.exceptionFactory = exceptionFactory;
        this.transactionService = transactionService;
        this.serviceCallCommands = serviceCallCommands;
        this.fullDuplexController = fullDuplexController;
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/contactor")
    public Response updateContactor(@PathParam("mrid") String mRID, ContactorInfo contactorInfo, @Context UriInfo uriInfo) {
        UsagePoint usagePoint = null;
        EndDevice endDevice = null;
        ServiceCall serviceCall = null;
        try (TransactionContext context = transactionService.getContext()) {    //TODO: review transaction mechanism / when to rollback stuff
            try {
                usagePoint = findUsagePoint(mRID);
                endDevice = findEndDeviceThroughUsagePoint(usagePoint);
                serviceCall = serviceCallCommands.createContactorOperationServiceCall(Optional.of(usagePoint), Optional.of(endDevice), contactorInfo);

                serviceCallCommands.requestTransition(serviceCall, DefaultState.PENDING);
                serviceCallCommands.requestTransition(serviceCall, DefaultState.ONGOING);    // Immediately transit to 'ONGOING' state
                validateContactorInfo(serviceCall, contactorInfo);
                fullDuplexController.performContactorOperations(endDevice, serviceCall, contactorInfo);
                serviceCallCommands.requestTransition(serviceCall, DefaultState.WAITING);

                //TODO: which info should the URI contain?
                URI uri = uriInfo.getBaseUriBuilder().path(UsagePointResource.class).path(UsagePointResource.class, "getDeviceMessage").build(mRID, serviceCall.getId());
                context.commit();
                return Response.ok().location(uri).build();
            } catch (ExceptionFactory.RestException | ConstraintViolationException e) { //TODO: catch also other types of exceptions?
                if (serviceCall == null) {                  //TODO: should we take specific measures to prevent DDoS attacks? (cause now we create a new ServiceCall object for each incoming request)
                    serviceCall = serviceCallCommands.createContactorOperationServiceCall(Optional.ofNullable(usagePoint), Optional.ofNullable(endDevice), contactorInfo);
                }
                serviceCallCommands.rejectServiceCall(serviceCall, e.getMessage());
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

        if (contactorInfo.loadLimit != null && (contactorInfo.loadLimit.limit == null || contactorInfo.loadLimit.unit == null)) {
            throw exceptionFactory.newException(MessageSeeds.INCOMPLETE_LOADLIMIT);
        } else if (contactorInfo.loadLimit != null && !contactorInfo.loadLimit.getUnit().isPresent()) {
            throw exceptionFactory.newException(MessageSeeds.UNKNOWN_UNIT_CODE);
        }

        if (contactorInfo.loadLimit == null && contactorInfo.loadTolerance != null) {
            throw exceptionFactory.newException(MessageSeeds.TOLERANCE_WITHOUT_LOAD_LIMIT);
        } else if (contactorInfo.loadTolerance != null && contactorInfo.loadTolerance < 0) {
            serviceCall.log(LogLevel.WARNING, "The specified load tolerance contains a negative value; this value will be ignored (the load tolerance will remain untouched).");
            contactorInfo.loadTolerance = null; // If tolerance is negative, then ignore it
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/messages/{messageId}")
    @Transactional
    public Response getDeviceMessage(@PathParam("mrid") String mRID, @PathParam("messageId") long id) {
        UsagePoint usagePoint = findUsagePoint(mRID);
        EndDevice endDevice = findEndDeviceThroughUsagePoint(usagePoint);
        Device device = findDeviceThroughEndDevice(endDevice);
        DeviceMessage<Device> deviceMessage = device.getMessages()
                .stream()
                .filter(msg -> msg.getId() == id)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DEVICE_MESSAGE));
        DeviceMessageInfo info = new DeviceMessageInfo();
        info.status = deviceMessage.getStatus();
        info.sentDate = deviceMessage.getSentDate().orElse(null);
        return Response.ok(info).build();
    }

    private UsagePoint findUsagePoint(String mRID) {
        return meteringService.findUsagePoint(mRID).orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_USAGE_POINT));
    }

    private EndDevice findEndDeviceThroughUsagePoint(UsagePoint usagePoint) {
        MeterActivation meterActivation = usagePoint.getCurrentMeterActivation().orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_CURRENT_METER_ACTIVATION));
        return meterActivation.getMeter().orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_METER_IN_ACTIVATION));
    }

    private Device findDeviceThroughEndDevice(EndDevice endDevice) { //TODO: only temporary, should be done by FullDuplexINterface
        return deviceService.findByUniqueMrid(endDevice.getMRID()).orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_DEVICE_FOR_METER, endDevice.getMRID()));
    }
}