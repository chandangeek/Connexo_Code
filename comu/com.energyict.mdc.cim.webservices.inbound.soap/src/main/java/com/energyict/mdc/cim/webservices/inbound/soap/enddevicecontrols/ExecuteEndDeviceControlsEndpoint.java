/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.enddevicecontrols;

import com.elster.jupiter.domain.util.VerboseConstraintViolationException;
import com.elster.jupiter.metering.CimAttributeNames;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.util.Checks;

import com.energyict.mdc.cim.webservices.inbound.soap.impl.CIMWebservicesException;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.ReplyTypeFactory;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.ScheduleStrategy;
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
    private static final String CHANGE_END_DEVICE_CONTROLS_ITEM = "ChangeEndDeviceControls";
    private static final String CANCEL_END_DEVICE_CONTROLS_ITEM = "CancelEndDeviceControls";
    private static final String PAYLOAD_ITEM = "Payload";
    private static final String HEADER_ITEM = "Header";
    private static final String CORRELATION_ITEM = "CorrelationID";
    private static final String REPLY_ADDRESS_ITEM = "ReplyAddress";
    private static final String END_DEVICE_CONTROLS_ITEM = "EndDeviceControls";
    private static final String MAXIMUM_EXECUTION_TIME = "MaximumExecutionTime";

    private final ReplyTypeFactory replyTypeFactory;
    private final EndDeviceControlsFaultMessageFactory faultMessageFactory;
    private final ServiceCallCommands serviceCallCommands;
    private final EndPointConfigurationService endPointConfigurationService;
    private final Thesaurus thesaurus;

    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory
            = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private final ch.iec.tc57._2011.enddevicecontrolsmessage.ObjectFactory endDeviceControlsMessageObjectFactory
            = new ch.iec.tc57._2011.enddevicecontrolsmessage.ObjectFactory();

    @Inject
    ExecuteEndDeviceControlsEndpoint(ReplyTypeFactory replyTypeFactory, EndDeviceControlsFaultMessageFactory faultMessageFactory,
                                     ServiceCallCommands serviceCallCommands, EndPointConfigurationService endPointConfigurationService,
                                     Thesaurus thesaurus) {
        this.replyTypeFactory = replyTypeFactory;
        this.faultMessageFactory = faultMessageFactory;
        this.serviceCallCommands = serviceCallCommands;
        this.endPointConfigurationService = endPointConfigurationService;
        this.thesaurus = thesaurus;
    }

    @Override
    public EndDeviceControlsResponseMessageType createEndDeviceControls(EndDeviceControlsRequestMessageType requestMessage)
            throws FaultMessage {
        return runInTransactionWithOccurrence(() -> {
            try {
                List<EndDeviceControl> listOfControls = extractEndDeviceControls(requestMessage, CREATE_END_DEVICE_CONTROLS_ITEM);

                saveWebserviceOccurrenceAttributes(listOfControls);

                String fullHeaderItem = CREATE_END_DEVICE_CONTROLS_ITEM + "." + HEADER_ITEM;
                HeaderType header = Optional.ofNullable(requestMessage.getHeader())
                        .orElseThrow(CIMWebservicesException.missingElement(thesaurus, fullHeaderItem));

                boolean async = Optional.ofNullable(header.isAsyncReplyFlag())
                        .orElse(Boolean.FALSE);

                if (async) {
                    String correlationId = header.getCorrelationID();
                    checkIfMissingOrIsEmpty(correlationId, fullHeaderItem + "." + CORRELATION_ITEM);

                    String replyAddress = header.getReplyAddress();
                    checkIfMissingOrIsEmpty(replyAddress, fullHeaderItem + "." + REPLY_ADDRESS_ITEM);

                    validateEndpointsForCreateRequest(replyAddress);
                    Optional<Long> maxExecTime = getMaxExecTime(requestMessage);

                    EndDeviceControlsRequestMessage edcRequestMessage = parseControls(listOfControls, true, true);

                    List<ErrorType> errorTypes = edcRequestMessage.getErrorTypes();
                    if (errorTypes.isEmpty()) {
                        edcRequestMessage.setCorrelationId(correlationId);
                        edcRequestMessage.setReplyAddress(replyAddress);
                        maxExecTime.ifPresent(edcRequestMessage::setMaxExecTime);

                        if (serviceCallCommands.createMasterEndDeviceControlsServiceCall(edcRequestMessage, errorTypes)) {
                            return createPartialOrSuccessfulResponseMessage(correlationId, HeaderType.Verb.REPLY, errorTypes);
                        } else {
                            return createFailedResponseMessage(correlationId, HeaderType.Verb.REPLY, errorTypes);
                        }
                    } else {
                        throw faultMessageFactory.edcFaultMessageSupplier(MessageSeeds.UNABLE_TO_CREATE_END_DEVICE_CONTROLS, errorTypes).get();
                    }
                } else {
                    throw CIMWebservicesException.unsupportedSyncMode(thesaurus);
                }
            } catch (VerboseConstraintViolationException e) {
                throw faultMessageFactory.edcFaultMessage(MessageSeeds.UNABLE_TO_CREATE_END_DEVICE_CONTROLS, e.getLocalizedMessage());
            } catch (LocalizedException e) {
                throw faultMessageFactory.edcFaultMessage(MessageSeeds.UNABLE_TO_CREATE_END_DEVICE_CONTROLS, e.getLocalizedMessage(), e.getErrorCode());
            }
        });
    }

    @Override
    public EndDeviceControlsResponseMessageType cancelEndDeviceControls(EndDeviceControlsRequestMessageType requestMessage)
            throws FaultMessage {
        return runInTransactionWithOccurrence(() -> cancelOrChangeEndDeviceControls(requestMessage, true));
    }

    @Override
    public EndDeviceControlsResponseMessageType changeEndDeviceControls(EndDeviceControlsRequestMessageType requestMessage)
            throws FaultMessage {
        return runInTransactionWithOccurrence(() -> cancelOrChangeEndDeviceControls(requestMessage, false));
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

    private EndDeviceControlsResponseMessageType cancelOrChangeEndDeviceControls(EndDeviceControlsRequestMessageType requestMessage, boolean isCancel) throws FaultMessage {
        MessageSeeds basicSeed = isCancel ? MessageSeeds.UNABLE_TO_CANCEL_END_DEVICE_CONTROLS : MessageSeeds.UNABLE_TO_CHANGE_END_DEVICE_CONTROLS;
        HeaderType.Verb verb = isCancel ? HeaderType.Verb.CANCELED : HeaderType.Verb.CHANGED;
        String operationName = isCancel ? CANCEL_END_DEVICE_CONTROLS_ITEM : CHANGE_END_DEVICE_CONTROLS_ITEM;

        try {
            List<EndDeviceControl> listOfControls = extractEndDeviceControls(requestMessage, operationName);

            saveWebserviceOccurrenceAttributes(listOfControls);

            String fullHeaderItem = operationName + "." + HEADER_ITEM;
            HeaderType header = Optional.ofNullable(requestMessage.getHeader())
                    .orElseThrow(CIMWebservicesException.missingElement(thesaurus, fullHeaderItem));

            String correlationId = header.getCorrelationID();
            checkIfMissingOrIsEmpty(correlationId, fullHeaderItem + "." + CORRELATION_ITEM);

            EndDeviceControlsRequestMessage edcRequestMessage = parseControls(listOfControls, !isCancel, false);
            List<ErrorType> errorTypes = edcRequestMessage.getErrorTypes();
            if (errorTypes.isEmpty()) {
                edcRequestMessage.setCorrelationId(correlationId);

                if (serviceCallCommands.cancelOrChangeMasterEndDeviceControlsServiceCall(edcRequestMessage, errorTypes, isCancel)) {
                    return createPartialOrSuccessfulResponseMessage(correlationId, verb, errorTypes);
                } else {
                    return createFailedResponseMessage(correlationId, verb, errorTypes);
                }
            } else {
                throw faultMessageFactory.edcFaultMessageSupplier(basicSeed, errorTypes).get();
            }
        } catch (VerboseConstraintViolationException e) {
            throw faultMessageFactory.edcFaultMessage(basicSeed, e.getLocalizedMessage());
        } catch (LocalizedException e) {
            throw faultMessageFactory.edcFaultMessage(basicSeed, e.getLocalizedMessage(), e.getErrorCode());
        }
    }

    private List<EndDeviceControl> extractEndDeviceControls(EndDeviceControlsRequestMessageType requestMessage, String operationName) {
        String fullPayloadItem = operationName + "." + PAYLOAD_ITEM;
        EndDeviceControlsPayloadType payload = Optional.ofNullable(requestMessage.getPayload())
                .orElseThrow(CIMWebservicesException.missingElement(thesaurus, fullPayloadItem));

        EndDeviceControls endDeviceControls = Optional.ofNullable(payload.getEndDeviceControls())
                .orElseThrow(CIMWebservicesException.missingElement(thesaurus, fullPayloadItem + "." + END_DEVICE_CONTROLS_ITEM));

        List<EndDeviceControl> listOfControls = endDeviceControls.getEndDeviceControl();

        if (listOfControls.isEmpty()) {
            throw CIMWebservicesException.emptyList(thesaurus, fullPayloadItem + "." + END_DEVICE_CONTROLS_ITEM);
        } else {
            return listOfControls;
        }
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

    private void validateEndpointsForCreateRequest(String url) {
        endPointConfigurationService
                .getEndPointConfigurationsForWebService(EndDeviceEventsServiceProvider.NAME)
                .stream()
                .filter(EndPointConfiguration::isActive)
                .filter(endPointConfiguration -> !endPointConfiguration.isInbound())
                .filter(endPointConfiguration -> endPointConfiguration.getUrl().equals(url))
                .findFirst()
                .orElseThrow(CIMWebservicesException.missingEndpoint(thesaurus, EndDeviceEventsServiceProvider.NAME, url));
    }

    private EndDeviceControlsRequestMessage parseControls(List<EndDeviceControl> listOfControls, boolean needsReleaseDate,
                                                          boolean needsScheduleStrategy) {
        EndDeviceControlsRequestMessage edcRequestMessage = new EndDeviceControlsRequestMessage();
        Instant releaseDate = null;
        ScheduleStrategy scheduleStrategy = ScheduleStrategy.RUN_NOW;

        for (int i = 0; i < listOfControls.size(); ++i) {
            List<ErrorType> errorTypes = new ArrayList<>();
            EndDeviceControlMessage endDeviceControlMessage = new EndDeviceControlMessage();
            EndDeviceControl endDeviceControl = listOfControls.get(i);
            String ref = getEndDeviceControlType(endDeviceControl);
            if (ref == null) {
                errorTypes.add(replyTypeFactory.errorType(MessageSeeds.COMMAND_CODE_MISSING, null, i));
            }

            if (needsReleaseDate) {
                releaseDate = getReleaseDateDate(endDeviceControl);
                if (releaseDate == null) {
                    errorTypes.add(replyTypeFactory.errorType(MessageSeeds.RELEASE_DATE_MISSING, null, i));
                }
            }

            if (needsScheduleStrategy) {
                String strScheduleStrategy = getScheduleStrategy(endDeviceControl);
                if (strScheduleStrategy != null) {
                    ScheduleStrategy scheduleStrategyEnum = ScheduleStrategy.getByName(strScheduleStrategy);
                    if (scheduleStrategyEnum == ScheduleStrategy.RUN_NOW || scheduleStrategyEnum == ScheduleStrategy.RUN_WITH_PRIORITY) {
                        scheduleStrategy = scheduleStrategyEnum;
                    } else {
                        errorTypes.add(replyTypeFactory.errorType(MessageSeeds.EDC_SCHEDULE_STRATEGY_NOT_SUPPORTED, null, strScheduleStrategy, i));
                    }
                }
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
                        endDeviceMessage.setReleaseDate(releaseDate);
                        endDeviceControlMessage.addEndDeviceMessage(endDeviceMessage);
                    }
                }
                if (errorTypes.isEmpty()) {
                    endDeviceControlMessage.setCommandCode(ref);
                    endDeviceControlMessage.setAttributes(endDeviceControl.getEndDeviceControlAttribute());
                    if (needsScheduleStrategy) {
                        endDeviceControlMessage.setScheduleStrategy(scheduleStrategy);
                    }

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

    private String getScheduleStrategy(EndDeviceControl endDeviceControl) {
        return Optional.ofNullable(endDeviceControl)
                .map(EndDeviceControl::getScheduleStrategy)
                .filter(ref -> !Checks.is(ref).emptyOrOnlyWhiteSpace())
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

    private Optional<Long> getMaxExecTime(EndDeviceControlsRequestMessageType requestMessage) {
        return Optional.ofNullable(requestMessage.getRequest())
                .map(request -> request.getOption().stream().filter(option -> option.getName().equals(MAXIMUM_EXECUTION_TIME)))
                .flatMap(Stream::findFirst)
                .filter(option -> !Checks.is(option.getValue()).emptyOrOnlyWhiteSpace())
                .map(option -> {
                    try {
                        return Long.parseLong(option.getValue());
                    } catch (NumberFormatException ignored) {
                        throw CIMWebservicesException.unsupportedValue(thesaurus, MAXIMUM_EXECUTION_TIME);
                    }
                });
    }

    private void checkIfMissingOrIsEmpty(String element, String elementName) {
        if (element == null) {
            throw CIMWebservicesException.missingElement(thesaurus, elementName);
        }
        if (Checks.is(element).emptyOrOnlyWhiteSpace()) {
            throw CIMWebservicesException.emptyElement(thesaurus, elementName);
        }
    }

    private EndDeviceControlsResponseMessageType createPartialOrSuccessfulResponseMessage(String correlationId, HeaderType.Verb verb, List<ErrorType> errorTypes) {
        EndDeviceControlsResponseMessageType responseMessage = endDeviceControlsMessageObjectFactory.createEndDeviceControlsResponseMessageType();
        responseMessage.setHeader(createHeader(correlationId, verb));

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

    private EndDeviceControlsResponseMessageType createFailedResponseMessage(String correlationId, HeaderType.Verb verb, List<ErrorType> errorTypes) {
        EndDeviceControlsResponseMessageType responseMessage = endDeviceControlsMessageObjectFactory.createEndDeviceControlsResponseMessageType();
        responseMessage.setHeader(createHeader(correlationId, verb));

        ReplyType replyType = replyTypeFactory.failureReplyType(ReplyType.Result.FAILED, errorTypes.stream()
                .toArray(ErrorType[]::new));

        responseMessage.setReply(replyType);
        return responseMessage;
    }

    private HeaderType createHeader(String correlationId, HeaderType.Verb verb) {
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setNoun(NOUN);
        header.setVerb(verb);
        header.setCorrelationID(correlationId);

        return header;
    }
}
