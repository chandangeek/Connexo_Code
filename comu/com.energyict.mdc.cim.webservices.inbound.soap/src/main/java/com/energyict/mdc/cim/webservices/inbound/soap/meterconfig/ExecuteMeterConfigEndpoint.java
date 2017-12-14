/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.meterconfig;

import com.energyict.mdc.cim.webservices.inbound.soap.impl.EndPointHelper;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.ReplyTypeFactory;
import com.elster.jupiter.domain.util.VerboseConstraintViolationException;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
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

    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private final ch.iec.tc57._2011.meterconfigmessage.ObjectFactory meterConfigMessageObjectFactory = new ch.iec.tc57._2011.meterconfigmessage.ObjectFactory();

    private final TransactionService transactionService;
    private final MeterConfigFaultMessageFactory faultMessageFactory;
    private final MeterConfigFactory meterConfigFactory;
    private final ReplyTypeFactory replyTypeFactory;
    private final EndPointHelper endPointHelper;
    private final DeviceBuilder deviceBuilder;

    @Inject
    public ExecuteMeterConfigEndpoint(TransactionService transactionService, MeterConfigFactory meterConfigFactory,
                                      MeterConfigFaultMessageFactory faultMessageFactory, ReplyTypeFactory replyTypeFactory,
                                      EndPointHelper endPointHelper, DeviceBuilder deviceBuilder) {
        this.transactionService = transactionService;
        this.meterConfigFactory = meterConfigFactory;
        this.faultMessageFactory = faultMessageFactory;
        this.replyTypeFactory = replyTypeFactory;
        this.endPointHelper = endPointHelper;
        this.deviceBuilder = deviceBuilder;
    }

    @Override
    public MeterConfigResponseMessageType createMeterConfig(MeterConfigRequestMessageType requestMessage) throws FaultMessage {
        endPointHelper.setSecurityContext();
        try (TransactionContext context = transactionService.getContext()) {
            MeterConfig meterConfig = requestMessage.getPayload().getMeterConfig();
            Meter meter = meterConfig.getMeter().stream().findFirst() // only process first meter
                    .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(MessageSeeds.EMPTY_LIST, "MeterConfig.Meter"));
            Device createdDevice = deviceBuilder.prepareCreateFrom(meter, meterConfig).build();
            context.commit();
            return createResponseMessage(createdDevice, meterConfig, HeaderType.Verb.CREATED);
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
                    .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(MessageSeeds.EMPTY_LIST, "MeterConfig.Meter"));
            Device updatedDevice = deviceBuilder.prepareUpdateFrom(meter).build();
            context.commit();
            return createResponseMessage(updatedDevice, meterConfig, HeaderType.Verb.CHANGED);
        } catch (VerboseConstraintViolationException | SecurityException | InvalidLastCheckedException | DeviceLifeCycleActionViolationException e) {
            throw faultMessageFactory.meterConfigFaultMessage(MessageSeeds.UNABLE_TO_CHANGE_DEVICE, e.getLocalizedMessage());
        } catch (LocalizedException e) {
            throw faultMessageFactory.meterConfigFaultMessage(MessageSeeds.UNABLE_TO_CHANGE_DEVICE, e.getLocalizedMessage(), e.getErrorCode());
        }
    }

    private MeterConfigResponseMessageType createResponseMessage(Device device, MeterConfig originalMeterConfig, HeaderType.Verb verb) {
        MeterConfigResponseMessageType responseMessage = meterConfigMessageObjectFactory.createMeterConfigResponseMessageType();

        // set header
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setNoun(NOUN);
        header.setVerb(verb);
        responseMessage.setHeader(header);

        // set reply
        ReplyType reply = originalMeterConfig.getMeter().size() > 1 ?
                replyTypeFactory.partialFailureReplyType(MessageSeeds.UNSUPPORTED_BULK_OPERATION, "MeterConfig.Meter") : replyTypeFactory.okReplyType();
        responseMessage.setReply(reply);

        // set payload
        MeterConfigPayloadType meterConfigPayload = meterConfigMessageObjectFactory.createMeterConfigPayloadType();
        MeterConfig meterConfig = meterConfigFactory.asMeterConfig(device);
        meterConfigPayload.setMeterConfig(meterConfig);
        responseMessage.setPayload(meterConfigPayload);

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