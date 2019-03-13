package com.energyict.mdc.cim.webservices.inbound.soap.getmeterconfig;

import ch.iec.tc57._2011.getmeterconfig.*;
import ch.iec.tc57._2011.getmeterconfigmessage.GetMeterConfigRequestMessageType;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigPayloadType;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigResponseMessageType;
import ch.iec.tc57._2011.meterconfigmessage.ObjectFactory;
import ch.iec.tc57._2011.schema.message.HeaderType;
import com.elster.jupiter.domain.util.VerboseConstraintViolationException;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.cim.webservices.inbound.soap.MeterInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.OperationEnum;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.EndPointHelper;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.ReplyTypeFactory;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.ServiceCallCommands;
import com.energyict.mdc.device.data.Device;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

public class GetMeterConfigEndpoint implements GetMeterConfigPort {

    private static final String NOUN = "GetMeterConfig";
    private static final String METER_ITEM = NOUN + ".Meter";

    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private final ObjectFactory meterConfigMessageObjectFactory = new ObjectFactory();

    private volatile TransactionService transactionService;
    private volatile GetMeterConfigFaultMessageFactory faultMessageFactory;
    private volatile GetMeterConfigFactory getMeterConfigFactory;
    private volatile GetMeterConfigParser meterConfigParser;
    private volatile ReplyTypeFactory replyTypeFactory;
    private volatile EndPointHelper endPointHelper;
    private volatile DeviceBuilder deviceBuilder;

    private volatile ServiceCallCommands serviceCallCommands;
    private volatile EndPointConfigurationService endPointConfigurationService;
    private volatile WebServicesService webServicesService;

    @Inject
    public GetMeterConfigEndpoint(TransactionService transactionService,GetMeterConfigFactory getMeterConfigFactory,
                                  GetMeterConfigFaultMessageFactory faultMessageFactory, ReplyTypeFactory replyTypeFactory,
                                  EndPointHelper endPointHelper, DeviceBuilder deviceBuilder,
                                  ServiceCallCommands serviceCallCommands, EndPointConfigurationService endPointConfigurationService,
                                  GetMeterConfigParser meterConfigParser, WebServicesService webServicesService) {
        this.transactionService = transactionService;
        this.getMeterConfigFactory = getMeterConfigFactory;
        this.meterConfigParser = meterConfigParser;
        this.faultMessageFactory = faultMessageFactory;
        this.replyTypeFactory = replyTypeFactory;
        this.endPointHelper = endPointHelper;
        this.deviceBuilder = deviceBuilder;
        this.serviceCallCommands = serviceCallCommands;
        this.endPointConfigurationService = endPointConfigurationService;
        this.webServicesService = webServicesService;
    }

    @Override
    public MeterConfigResponseMessageType getMeterConfig(GetMeterConfigRequestMessageType requestMessage) throws FaultMessage {
        endPointHelper.setSecurityContext();
        try (TransactionContext context = transactionService.getContext()) {
            GetMeterConfig meterConfig = requestMessage.getPayload().getGetMeterConfig();
            //get mrid or name of device
            if (Boolean.TRUE.equals(requestMessage.getHeader().isAsyncReplyFlag())) {
                // call asynchronously
                List<Meter> meters = meterConfig.getMeter();
                if (meters == null || meters.isEmpty()) {
                     throw faultMessageFactory.meterConfigFaultMessage(null, MessageSeeds.EMPTY_LIST, METER_ITEM);
                }
                EndPointConfiguration outboundEndPointConfiguration = getOutboundEndPointConfiguration(getReplyAddress(requestMessage));
                createServiceCallAndTransition(meters, outboundEndPointConfiguration);
                context.commit();
                return createQuickResponseMessage();
            } else {
                // call synchronously
                Meter meter = meterConfig.getMeter().stream().findFirst()
                        .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(null, MessageSeeds.EMPTY_LIST, METER_ITEM));
                MeterInfo meterInfo = meterConfigParser.asMeterInfo(meter);
                Optional<String> mrid = Optional.ofNullable(meterInfo.getmRID());
                Device device = deviceBuilder.findDevice(mrid, meterInfo.getDeviceName());
                return createResponseMessage(device);
            }
        } catch (VerboseConstraintViolationException e) {
            throw faultMessageFactory.meterConfigFaultMessage(null, MessageSeeds.UNABLE_TO_GET_METER_CONFIG_EVENTS, e.getLocalizedMessage());
        }

    }

    private ServiceCall createServiceCallAndTransition(List<Meter> meters, EndPointConfiguration endPointConfiguration) throws FaultMessage {
        ServiceCall serviceCall = serviceCallCommands.createGetMeterConfigMasterServiceCall(meters, endPointConfiguration,  OperationEnum.CREATE);
        serviceCallCommands.requestTransition(serviceCall, DefaultState.PENDING);
        return serviceCall;
    }

    private MeterConfigResponseMessageType createResponseMessage(Device device) {
        MeterConfigResponseMessageType responseMessage = meterConfigMessageObjectFactory.createMeterConfigResponseMessageType();

        // set header
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setNoun(NOUN);
        header.setVerb(HeaderType.Verb.GET);
        responseMessage.setHeader(header);

        // set reply
        responseMessage.setReply(replyTypeFactory.okReplyType());

        // set payload
        MeterConfigPayloadType meterConfigPayload = meterConfigMessageObjectFactory.createMeterConfigPayloadType();
        meterConfigPayload.setMeterConfig(getMeterConfigFactory.asMeterConfig(device));
        responseMessage.setPayload(meterConfigPayload);

        return responseMessage;
    }

    private MeterConfigResponseMessageType createQuickResponseMessage() {
        MeterConfigResponseMessageType responseMessage = meterConfigMessageObjectFactory.createMeterConfigResponseMessageType();

        // set header
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setVerb(HeaderType.Verb.REPLY);
        header.setNoun(NOUN);
        responseMessage.setHeader(header);

        // set reply
        responseMessage.setReply(replyTypeFactory.okReplyType());

        return responseMessage;
    }

    private EndPointConfiguration getOutboundEndPointConfiguration(String url) throws FaultMessage {
        EndPointConfiguration endPointConfig = endPointConfigurationService.findEndPointConfigurations()
                .stream()
                .filter(EndPointConfiguration::isActive)
                .filter(endPointConfiguration -> !endPointConfiguration.isInbound())
                .filter(endPointConfiguration -> endPointConfiguration.getUrl().equals(url))
                .findFirst()
                .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(null, MessageSeeds.NO_END_POINT_WITH_URL, url));
        if (!webServicesService.isPublished(endPointConfig)) {
            webServicesService.publishEndPoint(endPointConfig);
        }
        if (!webServicesService.isPublished(endPointConfig)) {
            throw faultMessageFactory.meterConfigFaultMessageSupplier(null, MessageSeeds.NO_PUBLISHED_END_POINT_WITH_URL, url).get();
        }
        return endPointConfig;
    }

    private String getReplyAddress(GetMeterConfigRequestMessageType requestMessage) throws FaultMessage {
        String replyAddress = requestMessage.getHeader().getReplyAddress();
        if (Checks.is(replyAddress).emptyOrOnlyWhiteSpace()) {
            throw faultMessageFactory.meterConfigFaultMessage(null, MessageSeeds.NO_REPLY_ADDRESS, "");
        }
        return replyAddress;
    }

}
