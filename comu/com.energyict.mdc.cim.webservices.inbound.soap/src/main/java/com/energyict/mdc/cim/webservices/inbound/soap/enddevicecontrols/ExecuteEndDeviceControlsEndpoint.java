/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.enddevicecontrols;

import com.elster.jupiter.domain.util.VerboseConstraintViolationException;
import com.elster.jupiter.metering.CimAttributeNames;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.util.Checks;

import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.ReplyTypeFactory;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.ServiceCallCommands;
import com.energyict.mdc.cim.webservices.outbound.soap.EndDeviceEventsServiceProvider;

import ch.iec.tc57._2011.enddevicecontrols.DateTimeInterval;
import ch.iec.tc57._2011.enddevicecontrols.EndDevice;
import ch.iec.tc57._2011.enddevicecontrols.EndDeviceControl;
import ch.iec.tc57._2011.enddevicecontrols.EndDeviceControls;
import ch.iec.tc57._2011.enddevicecontrols.EndDeviceTiming;
import ch.iec.tc57._2011.enddevicecontrols.Name;
import ch.iec.tc57._2011.enddevicecontrolsmessage.EndDeviceControlsPayloadType;
import ch.iec.tc57._2011.enddevicecontrolsmessage.EndDeviceControlsRequestMessageType;
import ch.iec.tc57._2011.enddevicecontrolsmessage.EndDeviceControlsResponseMessageType;
import ch.iec.tc57._2011.executeenddevicecontrols.EndDeviceControlsPort;
import ch.iec.tc57._2011.executeenddevicecontrols.FaultMessage;
import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ExecuteEndDeviceControlsEndpoint extends AbstractInboundEndPoint implements EndDeviceControlsPort, ApplicationSpecific {
    private static final String NOUN = "EndDeviceControls";
    private static final String CREATE_END_DEVICE_CONTROLS_ITEM = "CreateEndDeviceControls";
    private static final String PAYLOAD_ITEM = CREATE_END_DEVICE_CONTROLS_ITEM + "." + "Payload";
    private static final String HEADER_ITEM = CREATE_END_DEVICE_CONTROLS_ITEM + "." + "Header";
    private static final String CORRELATION_ITEM = HEADER_ITEM + ".CorrelationID";
    private static final String REPLY_ADDRESS_ITEM = HEADER_ITEM + ".ReplyAddress";
    private static final String END_DEVICE_CONTROLS_ITEM = PAYLOAD_ITEM + ".EndDeviceControls";

    private final ReplyTypeFactory replyTypeFactory;
    private final EndDeviceControlsFaultMessageFactory faultMessageFactory;
    private final ServiceCallCommands serviceCallCommands;
    private final EndPointConfigurationService endPointConfigurationService;

    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory
            = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private final ch.iec.tc57._2011.enddevicecontrolsmessage.ObjectFactory endDeviceControlsMessageObjectFactory
            = new ch.iec.tc57._2011.enddevicecontrolsmessage.ObjectFactory();

    @Inject
    ExecuteEndDeviceControlsEndpoint(ReplyTypeFactory replyTypeFactory, EndDeviceControlsFaultMessageFactory faultMessageFactory,
                                     ServiceCallCommands serviceCallCommands, EndPointConfigurationService endPointConfigurationService) {
        this.replyTypeFactory = replyTypeFactory;
        this.faultMessageFactory = faultMessageFactory;
        this.serviceCallCommands = serviceCallCommands;
        this.endPointConfigurationService = endPointConfigurationService;
    }

    @Override
    public EndDeviceControlsResponseMessageType createEndDeviceControls(EndDeviceControlsRequestMessageType requestMessage)
            throws FaultMessage {
        return runInTransactionWithOccurrence(() -> {
            try {
                EndDeviceControlsPayloadType payload = Optional.ofNullable(requestMessage.getPayload())
                        .orElseThrow(faultMessageFactory.createEDCFaultMessageSupplier(MessageSeeds.MISSING_ELEMENT, PAYLOAD_ITEM));

                EndDeviceControls endDeviceControls = Optional.ofNullable(payload.getEndDeviceControls())
                        .orElseThrow(faultMessageFactory.createEDCFaultMessageSupplier(MessageSeeds.MISSING_ELEMENT, END_DEVICE_CONTROLS_ITEM));

                List<EndDeviceControl> listOfControls = endDeviceControls.getEndDeviceControl();

                if (listOfControls.isEmpty()) {
                    throw faultMessageFactory.createEDCFaultMessageSupplier(MessageSeeds.EMPTY_LIST, END_DEVICE_CONTROLS_ITEM).get();
                } else {
                    saveWebserviceOccurrenceAttributes(listOfControls);

                    HeaderType header = Optional.ofNullable(requestMessage.getHeader())
                            .orElseThrow(faultMessageFactory.createEDCFaultMessageSupplier(MessageSeeds.MISSING_ELEMENT, HEADER_ITEM));

                    boolean async = Optional.ofNullable(header.isAsyncReplyFlag())
                            .orElse(Boolean.FALSE);

                    if (async) {
                        String correlationId = header.getCorrelationID();
                        checkIfMissingOrIsEmpty(correlationId, CORRELATION_ITEM);

                        String replyAddress = header.getReplyAddress();
                        checkIfMissingOrIsEmpty(replyAddress, REPLY_ADDRESS_ITEM);

                        validateEndpointsForCreateRequest(replyAddress);

                        EndDeviceControlsRequestMessage edcRequestMessage = parseControls(listOfControls);

                        List<ErrorType> errorTypes = edcRequestMessage.getErrorTypes();
                        if (errorTypes.isEmpty()) {
                            edcRequestMessage.setCorrelationId(correlationId);
                            edcRequestMessage.setReplyAddress(replyAddress);

                            if (serviceCallCommands.createMasterEndDeviceControlsServiceCall(edcRequestMessage, errorTypes)) {
                                return createPartialOrSuccessfulResponseMessage(correlationId, errorTypes);
                            } else {
                                return createFailedResponseMessage(correlationId, errorTypes);
                            }
                        } else {
                            throw faultMessageFactory.createEDCFaultMessageSupplier(errorTypes).get();
                        }
                    } else {
                        throw faultMessageFactory.createEDCFaultMessageSupplier(MessageSeeds.SYNC_MODE_NOT_SUPPORTED_GENERAL).get();
                    }
                }
            } catch (VerboseConstraintViolationException e) {
                throw faultMessageFactory.createEDCFaultMessage(e.getLocalizedMessage());
            } catch (LocalizedException e) {
                throw faultMessageFactory.createEDCFaultMessage(e.getLocalizedMessage(), e.getErrorCode());
            }
        });
    }

    @Override
    public EndDeviceControlsResponseMessageType changeEndDeviceControls(EndDeviceControlsRequestMessageType changeEndDeviceControlsRequestMessage)
            throws FaultMessage {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public EndDeviceControlsResponseMessageType cancelEndDeviceControls(EndDeviceControlsRequestMessageType cancelEndDeviceControlsRequestMessage)
            throws FaultMessage {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public EndDeviceControlsResponseMessageType closeEndDeviceControls(EndDeviceControlsRequestMessageType closeEndDeviceControlsRequestMessage)
            throws FaultMessage {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public EndDeviceControlsResponseMessageType deleteEndDeviceControls(EndDeviceControlsRequestMessageType deleteEndDeviceControlsRequestMessage)
            throws FaultMessage {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }

    private void saveWebserviceOccurrenceAttributes(List<EndDeviceControl> endDeviceControls) {
        SetMultimap<String, String> values = HashMultimap.create();

        endDeviceControls.forEach(endDeviceControl -> {
            endDeviceControl.getEndDevices().forEach(device -> {
                if (!device.getNames().isEmpty()) {
                    Optional<String> name = device.getNames().stream().filter(item -> !Checks.is(item.getName()).emptyOrOnlyWhiteSpace())
                            .map(Name::getName).findFirst();
                    name.ifPresent(s -> values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), s));
                }
                if (device.getMRID() != null) {
                    values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), device.getMRID());
                }
            });
        });

        saveRelatedAttributes(values);
    }

    private void validateEndpointsForCreateRequest(String url) throws FaultMessage {
        endPointConfigurationService
                .getEndPointConfigurationsForWebService(EndDeviceEventsServiceProvider.NAME)
                .stream()
                .filter(EndPointConfiguration::isActive)
                .filter(endPointConfiguration -> !endPointConfiguration.isInbound())
                .filter(endPointConfiguration -> endPointConfiguration.getUrl().equals(url))
                .findFirst()
                .orElseThrow(faultMessageFactory.createEDCFaultMessageSupplier(MessageSeeds.MISSING_ENDPOINT_SEND_END_DEVICE_EVENTS, url));

    }

    private EndDeviceControlsRequestMessage parseControls(List<EndDeviceControl> listOfControls) {
        EndDeviceControlsRequestMessage edcRequestMessage = new EndDeviceControlsRequestMessage();

        for (int i = 0; i < listOfControls.size(); ++i) {
            List<ErrorType> errorTypes = new ArrayList<>();
            EndDeviceControlMessage endDeviceControlMessage = new EndDeviceControlMessage();
            EndDeviceControl endDeviceControl = listOfControls.get(i);
            String ref = getEndDeviceControlType(endDeviceControl);
            if (ref == null) {
                errorTypes.add(replyTypeFactory.errorType(MessageSeeds.COMMAND_CODE_MISSING, null, i));
            }

            Instant releaseDate = getReleaseDateDate(endDeviceControl);
            if (releaseDate == null) {
                errorTypes.add(replyTypeFactory.errorType(MessageSeeds.RELEASE_DATE_MISSING, null, i));
            }

            List<EndDevice> endDevices = endDeviceControl.getEndDevices();
            if (endDevices.isEmpty()) {
                errorTypes.add(replyTypeFactory.errorType(MessageSeeds.END_DEVICES_MISSING, null, i));
                edcRequestMessage.addErrorTypes(errorTypes);
            } else {
                for (int j = 0; j < endDevices.size(); ++j) {
                    EndDevice endDevice = endDevices.get(j);
                    EndDeviceMessage endDeviceMessage = new EndDeviceMessage();
                    String mrid = getDeviceMrid(endDevice);
                    String name = getDeviceName(endDevice);
                    if (mrid == null && name == null) {
                        errorTypes.add(replyTypeFactory.errorType(MessageSeeds.MISSING_MRID_OR_NAME_FOR_END_DEVICE_CONTROL, null, i, j));
                    } else {
                        endDeviceMessage.setDeviceMrid(mrid);
                        endDeviceMessage.setDeviceName(name);
                        endDeviceControlMessage.addEndDeviceMessage(endDeviceMessage);
                    }
                }
                if (errorTypes.isEmpty()) {
                    endDeviceControlMessage.setCommandCode(ref);
                    endDeviceControlMessage.setReleaseDate(releaseDate);
                    endDeviceControlMessage.setAttributes(endDeviceControl.getEndDeviceControlAttribute());

                    edcRequestMessage.addEndDeviceControlMessage(endDeviceControlMessage);
                } else {
                    edcRequestMessage.addErrorTypes(errorTypes);
                }
            }
        }

        return edcRequestMessage;
    }

    private String getEndDeviceControlType(EndDeviceControl endDeviceControl) {
        return Optional.ofNullable(endDeviceControl)
                .map(EndDeviceControl::getEndDeviceControlType)
                .map(EndDeviceControl.EndDeviceControlType::getRef)
                .filter(ref -> !Checks.is(ref).emptyOrOnlyWhiteSpace())
                .orElse(null);
    }

    private Instant getReleaseDateDate(EndDeviceControl endDeviceControl) {
        return Optional.ofNullable(endDeviceControl)
                .map(EndDeviceControl::getPrimaryDeviceTiming)
                .map(EndDeviceTiming::getInterval)
                .map(DateTimeInterval::getStart)
                .orElse(null);
    }

    private String getDeviceMrid(EndDevice endDevice) {
        return Optional.ofNullable(endDevice)
                .map(EndDevice::getMRID)
                .filter(mrid -> !Checks.is(mrid).emptyOrOnlyWhiteSpace())
                .orElse(null);
    }

    private String getDeviceName(EndDevice endDevice) {
        return Optional.ofNullable(endDevice)
                .map(EndDevice::getNames)
                .map(names -> names.stream().filter(name -> !Checks.is(name.getName()).emptyOrOnlyWhiteSpace())
                        .map(Name::getName))
                .flatMap(Stream::findFirst)
                .orElse(null);
    }

    private void checkIfMissingOrIsEmpty(String element, String elementName) throws FaultMessage {
        if (element == null) {
            throw faultMessageFactory.createEDCFaultMessageSupplier(
                    MessageSeeds.MISSING_ELEMENT, elementName).get();
        }
        if (Checks.is(element).emptyOrOnlyWhiteSpace()) {
            throw faultMessageFactory.createEDCFaultMessageSupplier(
                    MessageSeeds.EMPTY_ELEMENT, elementName).get();
        }
    }

    private EndDeviceControlsResponseMessageType createPartialOrSuccessfulResponseMessage(String correlationId, List<ErrorType> errorTypes) {
        EndDeviceControlsResponseMessageType responseMessage = endDeviceControlsMessageObjectFactory.createEndDeviceControlsResponseMessageType();
        responseMessage.setHeader(createHeader(correlationId));

        ReplyType replyType;
        if (errorTypes.isEmpty()) {
            replyType = replyTypeFactory.okReplyType();
        } else {
            replyType = replyTypeFactory.failureReplyType(ReplyType.Result.PARTIAL, errorTypes.stream()
                    .toArray(ErrorType[]::new));
        }

        responseMessage.setReply(replyType);
        return responseMessage;
    }

    private EndDeviceControlsResponseMessageType createFailedResponseMessage(String correlationId, List<ErrorType> errorTypes) {
        EndDeviceControlsResponseMessageType responseMessage = endDeviceControlsMessageObjectFactory.createEndDeviceControlsResponseMessageType();
        responseMessage.setHeader(createHeader(correlationId));

        ReplyType replyType = replyTypeFactory.failureReplyType(ReplyType.Result.FAILED, errorTypes.stream()
                .toArray(ErrorType[]::new));

        responseMessage.setReply(replyType);
        return responseMessage;
    }

    private HeaderType createHeader(String correlationId) {
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setNoun(NOUN);
        header.setVerb(HeaderType.Verb.REPLY);
        header.setCorrelationID(correlationId);

        return header;
    }
}
