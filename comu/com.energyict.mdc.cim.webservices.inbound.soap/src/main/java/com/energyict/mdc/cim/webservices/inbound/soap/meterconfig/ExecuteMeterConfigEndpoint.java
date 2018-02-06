/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.meterconfig;

import com.elster.jupiter.domain.util.VerboseConstraintViolationException;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.EndPointHelper;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.ReplyTypeFactory;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.ServiceCallCommands;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.exceptions.InvalidLastCheckedException;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolationException;

import ch.iec.tc57._2011.executemeterconfig.FaultMessage;
import ch.iec.tc57._2011.executemeterconfig.MeterConfigPort;
import ch.iec.tc57._2011.meterconfig.Meter;
import ch.iec.tc57._2011.meterconfig.MeterConfig;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigPayloadType;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigRequestMessageType;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigResponseMessageType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ReplyType;

import javax.inject.Inject;

public class ExecuteMeterConfigEndpoint implements MeterConfigPort {

    private static final String NOUN = "MeterConfig";
    private static final String METER_ITEM = NOUN + ".Meter";

    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private final ch.iec.tc57._2011.meterconfigmessage.ObjectFactory meterConfigMessageObjectFactory = new ch.iec.tc57._2011.meterconfigmessage.ObjectFactory();

    private final TransactionService transactionService;
    private final MeterConfigFaultMessageFactory faultMessageFactory;
    private final MeterConfigFactory meterConfigFactory;
    private final ReplyTypeFactory replyTypeFactory;
    private final EndPointHelper endPointHelper;
    private final DeviceBuilder deviceBuilder;

    private final ServiceCallCommands serviceCallCommands;
    private final EndPointConfigurationService endPointConfigurationService;

    @Inject
    public ExecuteMeterConfigEndpoint(TransactionService transactionService, MeterConfigFactory meterConfigFactory,
                                      MeterConfigFaultMessageFactory faultMessageFactory, ReplyTypeFactory replyTypeFactory,
                                      EndPointHelper endPointHelper, DeviceBuilder deviceBuilder,
                                      ServiceCallCommands serviceCallCommands,  EndPointConfigurationService endPointConfigurationService) {
        this.transactionService = transactionService;
        this.meterConfigFactory = meterConfigFactory;
        this.faultMessageFactory = faultMessageFactory;
        this.replyTypeFactory = replyTypeFactory;
        this.endPointHelper = endPointHelper;
        this.deviceBuilder = deviceBuilder;
        this.serviceCallCommands = serviceCallCommands;
        this.endPointConfigurationService = endPointConfigurationService;
    }

    @Override
    public MeterConfigResponseMessageType createMeterConfig(MeterConfigRequestMessageType requestMessage) throws FaultMessage {
        endPointHelper.setSecurityContext();
        try (TransactionContext context = transactionService.getContext()) {
            MeterConfig meterConfig = requestMessage.getPayload().getMeterConfig();
            if (requestMessage.getHeader().isAsyncReplyFlag()) {
                // check outbound end point
                String replyAddress = requestMessage.getHeader().getReplyAddress();
                if (Checks.is(replyAddress).emptyOrOnlyWhiteSpace()) {
                    throw faultMessageFactory.meterConfigFaultMessage(MessageSeeds.NO_REPLY_ADDRESS, ReplyType.Result.FAILED.value()); // todo
                }
                EndPointConfiguration outboundEndPointConfiguration = endPointConfigurationService.findEndPointConfigurations().stream()
                        .filter(EndPointConfiguration::isActive)
                        .filter(endPointConfiguration -> !endPointConfiguration.isInbound())
                        .filter(endPointConfiguration -> endPointConfiguration.getUrl().equals(replyAddress))
                        .findFirst()
                        .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(MessageSeeds.NO_END_POINT_WITH_URL, replyAddress));

                createMeterConfigServiceCallAndTransition(meterConfig, outboundEndPointConfiguration);
                context.commit();
                return createQuickResponseMessage(HeaderType.Verb.REPLY);
            } else if (meterConfig.getMeter().size() > 1) {
                throw faultMessageFactory.meterConfigFaultMessage(MessageSeeds.SYNC_MODE_NOT_SUPPORTED, ReplyType.Result.FAILED.value()); // todo
            } else {
                // call synchronously
                Meter meter = meterConfig.getMeter().stream().findFirst() // only process first meter
                        .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(MessageSeeds.EMPTY_LIST, METER_ITEM));
                Device createdDevice = deviceBuilder.prepareCreateFrom(meter, meterConfig.getSimpleEndDeviceFunction()).build();
                context.commit();
                return createResponseMessage(createdDevice, HeaderType.Verb.CREATED);
            }
        } catch (VerboseConstraintViolationException e) {
            throw faultMessageFactory.meterConfigFaultMessage(MessageSeeds.UNABLE_TO_CREATE_DEVICE, e.getLocalizedMessage());
        } catch (LocalizedException e) {
            throw faultMessageFactory.meterConfigFaultMessage(MessageSeeds.UNABLE_TO_CREATE_DEVICE, e.getLocalizedMessage(), e.getErrorCode());
        }
    }

    @Override
    public MeterConfigResponseMessageType changeMeterConfig(MeterConfigRequestMessageType requestMessage) throws FaultMessage {
        endPointHelper.setSecurityContext();
        try (TransactionContext context = transactionService.getContext()) {
            MeterConfig meterConfig = requestMessage.getPayload().getMeterConfig();
            Meter meter = meterConfig.getMeter().stream().findFirst() // only process first meter
                    .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(MessageSeeds.EMPTY_LIST, METER_ITEM));
            Device changedDevice = deviceBuilder.prepareChangeFrom(meter).build();
            context.commit();
            return createResponseMessage(changedDevice, HeaderType.Verb.CHANGED);
        } catch (VerboseConstraintViolationException | SecurityException | InvalidLastCheckedException | DeviceLifeCycleActionViolationException e) {
            throw faultMessageFactory.meterConfigFaultMessage(MessageSeeds.UNABLE_TO_CHANGE_DEVICE, e.getLocalizedMessage());
        } catch (LocalizedException e) {
            throw faultMessageFactory.meterConfigFaultMessage(MessageSeeds.UNABLE_TO_CHANGE_DEVICE, e.getLocalizedMessage(), e.getErrorCode());
        }
    }

    private ServiceCall createMeterConfigServiceCallAndTransition(MeterConfig meterConfig, EndPointConfiguration endPointConfiguration) {
        ServiceCall serviceCall = serviceCallCommands.createMeterConfigMasterServiceCall(meterConfig, endPointConfiguration);
        serviceCallCommands.requestTransition(serviceCall, DefaultState.PENDING);
        return serviceCall;
    }

    private MeterConfigResponseMessageType createResponseMessage(Device device, HeaderType.Verb verb) {
        MeterConfigResponseMessageType responseMessage = meterConfigMessageObjectFactory.createMeterConfigResponseMessageType();

        // set header
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setNoun(NOUN);
        header.setVerb(verb);
        responseMessage.setHeader(header);

        // set reply
        responseMessage.setReply(replyTypeFactory.okReplyType());

        // set payload
        MeterConfigPayloadType meterConfigPayload = meterConfigMessageObjectFactory.createMeterConfigPayloadType();
        meterConfigPayload.setMeterConfig(meterConfigFactory.asMeterConfig(device));
        responseMessage.setPayload(meterConfigPayload);

        return responseMessage;
    }

    private MeterConfigResponseMessageType createQuickResponseMessage(HeaderType.Verb verb) {
        MeterConfigResponseMessageType responseMessage = meterConfigMessageObjectFactory.createMeterConfigResponseMessageType();

        // set header
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setNoun(NOUN);
        header.setVerb(verb);
        responseMessage.setHeader(header);

        // set reply
        responseMessage.setReply(replyTypeFactory.okReplyType());

        return responseMessage;
    }

    @Override
    public MeterConfigResponseMessageType cancelMeterConfig(MeterConfigRequestMessageType cancelMeterConfigRequestMessage) throws FaultMessage {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public MeterConfigResponseMessageType closeMeterConfig(MeterConfigRequestMessageType closeMeterConfigRequestMessage) throws FaultMessage {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public MeterConfigResponseMessageType deleteMeterConfig(MeterConfigRequestMessageType deleteMeterConfigRequestMessage) throws FaultMessage {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}