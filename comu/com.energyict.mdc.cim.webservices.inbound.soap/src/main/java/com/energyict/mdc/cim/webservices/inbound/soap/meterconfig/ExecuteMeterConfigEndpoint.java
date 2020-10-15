/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.meterconfig;

import com.elster.jupiter.domain.util.VerboseConstraintViolationException;
import com.elster.jupiter.metering.CimAttributeNames;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointProp;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.cim.webservices.inbound.soap.MeterInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.ReplyTypeFactory;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.SecurityHelper;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.SecurityKeyInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.TranslationKeys;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.customattributeset.CasHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.customattributeset.CasInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.ServiceCallCommands;
import com.energyict.mdc.cim.webservices.outbound.soap.MeterConfigFactory;
import com.energyict.mdc.cim.webservices.outbound.soap.OperationEnum;
import com.energyict.mdc.cim.webservices.outbound.soap.PingResult;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.InvalidLastCheckedException;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolationException;

import ch.iec.tc57._2011.executemeterconfig.FaultMessage;
import ch.iec.tc57._2011.executemeterconfig.MeterConfigPort;
import ch.iec.tc57._2011.meterconfig.Meter;
import ch.iec.tc57._2011.meterconfig.MeterConfig;
import ch.iec.tc57._2011.meterconfig.Name;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigPayloadType;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigRequestMessageType;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigResponseMessageType;
import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.HeaderType.Verb;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import ch.iec.tc57._2011.schema.message.ReplyType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.SetMultimap;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ExecuteMeterConfigEndpoint extends AbstractInboundEndPoint implements MeterConfigPort, ApplicationSpecific, EndPointProp {
    private static final String NOUN = "MeterConfig";
    private static final String METER_ITEM = NOUN + ".Meter";
    private static final String METER_STATUS_SOURCE_ELEMENT = "MeterStatusSource";
    public static final String CIM_MERER_CONFIG = "CIM MeterConfig";

    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private final ch.iec.tc57._2011.meterconfigmessage.ObjectFactory meterConfigMessageObjectFactory = new ch.iec.tc57._2011.meterconfigmessage.ObjectFactory();

    private final MeterConfigFaultMessageFactory faultMessageFactory;
    private final MeterConfigFactory meterConfigFactory;
    private final MeterConfigParser meterConfigParser;
    private final MeterConfigPingUtils meterConfigPingUtils;
    private final ReplyTypeFactory replyTypeFactory;
    private final DeviceBuilder deviceBuilder;
    private final DeviceFinder deviceFinder;
    private final DeviceDeleter deviceDeleter;
    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    private final ServiceCallCommands serviceCallCommands;
    private final EndPointConfigurationService endPointConfigurationService;
    private final WebServicesService webServicesService;
    private final InboundCIMWebServiceExtensionFactory webServiceExtensionFactory;
    private final CasHandler casHandler;
    private final SecurityHelper securityHelper;

    @Inject
    public ExecuteMeterConfigEndpoint(MeterConfigFactory meterConfigFactory,
                                      MeterConfigFaultMessageFactory faultMessageFactory, ReplyTypeFactory replyTypeFactory,
                                      DeviceBuilder deviceBuilder, ServiceCallCommands serviceCallCommands,
                                      EndPointConfigurationService endPointConfigurationService, MeterConfigParser meterConfigParser,
                                      WebServicesService webServicesService, InboundCIMWebServiceExtensionFactory webServiceExtensionFactory,
                                      CasHandler casHandler, SecurityHelper securityHelper, DeviceFinder deviceFinder, DeviceDeleter deviceDeleter,
                                      MeterConfigPingUtils meterConfigPingUtils,PropertySpecService propertySpecService,
                                      Thesaurus thesaurus) {
        this.meterConfigFactory = meterConfigFactory;
        this.meterConfigParser = meterConfigParser;
        this.faultMessageFactory = faultMessageFactory;
        this.replyTypeFactory = replyTypeFactory;
        this.deviceBuilder = deviceBuilder;
        this.serviceCallCommands = serviceCallCommands;
        this.endPointConfigurationService = endPointConfigurationService;
        this.webServicesService = webServicesService;
        this.webServiceExtensionFactory = webServiceExtensionFactory;
        this.casHandler = casHandler;
        this.securityHelper = securityHelper;
        this.deviceFinder = deviceFinder;
        this.deviceDeleter = deviceDeleter;
        this.meterConfigPingUtils = meterConfigPingUtils;
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    @Override
    public MeterConfigResponseMessageType createMeterConfig(MeterConfigRequestMessageType requestMessage)
            throws FaultMessage {
        return runInTransactionWithOccurrence(() -> {
            String meterName = null;
            try {
                MeterConfig meterConfig = requestMessage.getPayload().getMeterConfig();
                SetMultimap<String, String> values = HashMultimap.create();
                meterConfig.getMeter().stream().forEach(meter -> {
                    if (!meter.getNames().isEmpty()) {
                        values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), meter.getNames().get(0).getName());
                    }
                    if (meter.getMRID() != null) {
                        values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), meter.getMRID());
                    }
                    if (meter.getSerialNumber() != null) {
                        values.put(CimAttributeNames.CIM_DEVICE_SERIAL_NUMBER.getAttributeName(), meter.getSerialNumber());
                    }
                });
                saveRelatedAttributes(values);

                if (Boolean.TRUE.equals(requestMessage.getHeader().isAsyncReplyFlag())) {
                    // call asynchronously
                    EndPointConfiguration outboundEndPointConfiguration = getOutboundEndPointConfiguration(requestMessage.getHeader().getReplyAddress());
                    createMeterConfigServiceCallAndTransition(meterConfig, outboundEndPointConfiguration,
                            OperationEnum.CREATE, requestMessage.getHeader().getCorrelationID());

                    return createQuickResponseMessage(HeaderType.Verb.REPLY, requestMessage.getHeader().getCorrelationID());
                } else if (meterConfig.getMeter().size() > 1) {
                    throw faultMessageFactory.meterConfigFaultMessage(meterName, MessageSeeds.UNABLE_TO_CREATE_DEVICE,
                            MessageSeeds.SYNC_MODE_NOT_SUPPORTED);
                } else {
                    // call synchronously
                    Meter meter = meterConfig.getMeter().stream().findFirst().orElseThrow(faultMessageFactory
                            .meterConfigFaultMessageSupplier(meterName, MessageSeeds.EMPTY_LIST, METER_ITEM));
                    meterName = meter.getNames().stream().findFirst().map(Name::getName).orElse(null);

                    MeterInfo meterInfo = meterConfigParser.asMeterInfo(meter, meterConfig.getSimpleEndDeviceFunction(),
                            OperationEnum.CREATE);

                    boolean respondSuccessForExisting = returnDeviceIfExists(true);
                    MeterInfo parsedMeterInfo = meterConfigParser.asMeterInfo(meter, meterConfig.getSimpleEndDeviceFunction(), OperationEnum.CREATE);
                    List<Device> existingDevices = deviceBuilder.getExistingDevices(parsedMeterInfo.getDeviceName(), parsedMeterInfo.getSerialNumber());
                    if (existingDevices.size() == 1) {
                        // only one device exists
                        if (respondSuccessForExisting) {
                            Device existingDevice = existingDevices.get(0);
                            if (existingDevice.getName().equals(parsedMeterInfo.getDeviceName())
                                    && existingDevice.getSerialNumber().equals(parsedMeterInfo.getSerialNumber())
                                    && existingDevice.getDeviceType().getName().equals(parsedMeterInfo.getDeviceType())) {
                                // respond as it was created
                                return createResponseMessage(existingDevice, HeaderType.Verb.CREATED,requestMessage.getHeader().getCorrelationID());
                            }
                        }
                    }

                    // standard functionality
                    Device createdDevice = deviceBuilder.prepareCreateFrom(parsedMeterInfo).build();
                    return processDevice(createdDevice, meterInfo, HeaderType.Verb.CREATED, requestMessage.getHeader().getCorrelationID());
                }
            } catch (VerboseConstraintViolationException e) {
                throw faultMessageFactory.meterConfigFaultMessage(meterName, MessageSeeds.UNABLE_TO_CREATE_DEVICE,
                        e.getLocalizedMessage());
            } catch (LocalizedException e) {
                throw faultMessageFactory.meterConfigFaultMessage(meterName, MessageSeeds.UNABLE_TO_CREATE_DEVICE,
                        e.getLocalizedMessage(), e.getErrorCode());
            }
        });
    }

    private MeterConfigResponseMessageType processDevice(Device device, MeterInfo meterInfo, Verb verb, String correlationId) throws FaultMessage {
        List<FaultMessage> faults = new ArrayList<>();
        faults.addAll(processCustomAttributeSets(device, meterInfo));
        faults.addAll(processSecurityAttributes(device, meterInfo));
        if (faults.isEmpty()) {
            postProcessDevice(device, meterInfo);
            return createResponseMessage(device, verb, correlationId);
        }
        throw faultMessageFactory.meterConfigFaultMessage(faults);
    }

    private List<FaultMessage> processSecurityAttributes(Device device, MeterInfo meterInfo) {
        List<SecurityKeyInfo> securityInfoList = meterInfo.getSecurityInfo().getSecurityKeys();
        if (securityInfoList != null && !securityInfoList.isEmpty()) {
            return securityHelper.addSecurityKeys(device, securityInfoList);
        }
        return Collections.emptyList();
    }

    @Override
    public MeterConfigResponseMessageType changeMeterConfig(MeterConfigRequestMessageType requestMessage)
            throws FaultMessage {
        return runInTransactionWithOccurrence(() -> {
            String meterName = null;
            try {
                MeterConfig meterConfig = requestMessage.getPayload().getMeterConfig();

                SetMultimap<String, String> values = HashMultimap.create();
                meterConfig.getMeter().stream().forEach(meter -> {
                    if (!meter.getNames().isEmpty()) {
                        values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), meter.getNames().get(0).getName());
                    }
                    if (meter.getMRID() != null) {
                        values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), meter.getMRID());
                    }
                    if (meter.getSerialNumber() != null) {
                        values.put(CimAttributeNames.CIM_DEVICE_SERIAL_NUMBER.getAttributeName(), meter.getSerialNumber());
                    }
                });
                saveRelatedAttributes(values);

                if (Boolean.TRUE.equals(requestMessage.getHeader().isAsyncReplyFlag())) {
                    // call asynchronously
                    EndPointConfiguration outboundEndPointConfiguration = getOutboundEndPointConfiguration(requestMessage.getHeader().getReplyAddress());
                    createMeterConfigServiceCallAndTransition(meterConfig, outboundEndPointConfiguration,
                            OperationEnum.UPDATE, requestMessage.getHeader().getCorrelationID());
                    return createQuickResponseMessage(HeaderType.Verb.REPLY, requestMessage.getHeader().getCorrelationID());
                } else if (meterConfig.getMeter().size() > 1) {
                    throw faultMessageFactory.meterConfigFaultMessage(meterName, MessageSeeds.UNABLE_TO_CHANGE_DEVICE,
                            MessageSeeds.SYNC_MODE_NOT_SUPPORTED);
                } else {
                    // call synchronously
                    Meter meter = meterConfig.getMeter().stream().findFirst().orElseThrow(faultMessageFactory
                            .meterConfigFaultMessageSupplier(meterName, MessageSeeds.EMPTY_LIST, METER_ITEM));
                    meterName = meter.getNames().stream().findFirst().map(Name::getName).orElse(null);
                    MeterInfo meterInfo = meterConfigParser.asMeterInfo(meter, meterConfig.getSimpleEndDeviceFunction(),
                            OperationEnum.UPDATE);

                    Device changedDevice = deviceBuilder.prepareChangeFrom(meterInfo).build();
                    return processDevice(changedDevice, meterInfo, HeaderType.Verb.CHANGED, requestMessage.getHeader().getCorrelationID());
                }
            } catch (VerboseConstraintViolationException | SecurityException | InvalidLastCheckedException
                    | DeviceLifeCycleActionViolationException e) {
                throw faultMessageFactory.meterConfigFaultMessage(meterName, MessageSeeds.UNABLE_TO_CHANGE_DEVICE,
                        e.getLocalizedMessage());
            } catch (LocalizedException e) {
                throw faultMessageFactory.meterConfigFaultMessage(meterName, MessageSeeds.UNABLE_TO_CHANGE_DEVICE,
                        e.getLocalizedMessage(), e.getErrorCode());
            }
        });
    }


    private EndPointConfiguration getOutboundEndPointConfiguration(String url) throws FaultMessage {
        EndPointConfiguration endPointConfig = null;
        if (!Checks.is(url).emptyOrOnlyWhiteSpace()) {
            endPointConfig = endPointConfigurationService.findEndPointConfigurations().stream()
                    .filter(EndPointConfiguration::isActive)
                    .filter(endPointConfiguration -> !endPointConfiguration.isInbound())
                    .filter(endPointConfiguration -> endPointConfiguration.getUrl().equals(url)).findFirst()
                    .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(null,
                            MessageSeeds.NO_END_POINT_WITH_URL, url));
            if (!webServicesService.isPublished(endPointConfig)) {
                webServicesService.publishEndPoint(endPointConfig);
            }
            if (!webServicesService.isPublished(endPointConfig)) {
                throw faultMessageFactory
                        .meterConfigFaultMessageSupplier(null, MessageSeeds.NO_PUBLISHED_END_POINT_WITH_URL, url).get();
            }
        }
        return endPointConfig;
    }

    private ServiceCall createMeterConfigServiceCallAndTransition(MeterConfig meterConfig,
                                                                  EndPointConfiguration endPointConfiguration, OperationEnum operation, String correlationId) throws FaultMessage {
        ServiceCall serviceCall = serviceCallCommands.createMeterConfigMasterServiceCall(meterConfig,
                endPointConfiguration, operation, correlationId);
        serviceCallCommands.requestTransition(serviceCall, DefaultState.PENDING);
        return serviceCall;
    }

    private MeterConfigResponseMessageType createResponseMessage(Device device, HeaderType.Verb verb, String correlationId) {

        MeterConfigResponseMessageType responseMessage = createResponseMessageCustomPayload(verb, correlationId, replyTypeFactory.okReplyType());

        // set payload
        MeterConfigPayloadType meterConfigPayload = meterConfigMessageObjectFactory.createMeterConfigPayloadType();
        if (device != null) {
            meterConfigPayload.setMeterConfig(meterConfigFactory.asMeterConfig(device));
        } else {
            meterConfigPayload.setMeterConfig(new MeterConfig());
        }
        responseMessage.setPayload(meterConfigPayload);

        return responseMessage;
    }

    private MeterConfigResponseMessageType createGetResponseMessage(Device device, PingResult pingResult, boolean meterStatusRequired,
                                                                    ErrorMessage errorMessage, String correlationId) {
        MeterConfigResponseMessageType responseMessage = createResponseMessageCustomPayload(HeaderType.Verb.REPLY, correlationId, replyTypeFactory.okReplyType());

        if (errorMessage != null) {
            responseMessage.getReply().getError().add(replyTypeFactory.errorType(device, errorMessage.getMessage(), errorMessage.getCode(), ErrorType.Level.WARNING));
        }

        // set payload
        MeterConfigPayloadType meterConfigPayload = meterConfigMessageObjectFactory.createMeterConfigPayloadType();
        meterConfigPayload.setMeterConfig(meterConfigFactory.asGetMeterConfig(device, pingResult, meterStatusRequired));

        responseMessage.setPayload(meterConfigPayload);

        return responseMessage;
    }

    private MeterConfigResponseMessageType createResponseMessageCustomPayload(HeaderType.Verb verb, String correlationId, ReplyType replyType) {
        MeterConfigResponseMessageType responseMessage = meterConfigMessageObjectFactory
                .createMeterConfigResponseMessageType();

        responseMessage.setHeader(createHeader(verb, correlationId));
        responseMessage.setReply(replyType);
        return responseMessage;
    }

    private HeaderType createHeader(HeaderType.Verb verb, String correlationId) {
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setNoun(NOUN);
        header.setVerb(verb);
        header.setCorrelationID(correlationId);
        return header;
    }

    private MeterConfigResponseMessageType createQuickResponseMessage(HeaderType.Verb verb, String correlationId) {
        return createResponseMessageCustomPayload(verb, correlationId, replyTypeFactory.okReplyType());
    }

    private void postProcessDevice(Device device, MeterInfo meterInfo) {
        if (webServiceExtensionFactory != null) {
            if (webServiceExtensionFactory.getWebServiceExtension().isPresent()) {
                webServiceExtensionFactory.getWebServiceExtension().get().extendMeterInfo(device, meterInfo);
            }
        }
    }

    private List<FaultMessage> processCustomAttributeSets(Device device, MeterInfo meterInfo) {
        List<CasInfo> customAttributeSets = meterInfo.getCustomAttributeSets();
        if (!customAttributeSets.isEmpty()) {
            return casHandler.addCustomPropertySetsData(device, customAttributeSets);
        }
        return Collections.emptyList();
    }

    @Override
    public MeterConfigResponseMessageType cancelMeterConfig(
            MeterConfigRequestMessageType cancelMeterConfigRequestMessage) throws FaultMessage {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public MeterConfigResponseMessageType closeMeterConfig(MeterConfigRequestMessageType closeMeterConfigRequestMessage)
            throws FaultMessage {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public MeterConfigResponseMessageType deleteMeterConfig(
            MeterConfigRequestMessageType deleteMeterConfigRequestMessageType) throws FaultMessage {
        return runInTransactionWithOccurrence(() -> {
            try {
                MeterConfig meterConfig = deleteMeterConfigRequestMessageType.getPayload().getMeterConfig();
                SetMultimap<String, String> values = HashMultimap.create();

                meterConfig.getMeter().stream().forEach(meter -> {
                    if (!meter.getNames().isEmpty()) {
                        values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), meter.getNames().get(0).getName());
                    }
                    if (meter.getMRID() != null) {
                        values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), meter.getMRID());
                    }
                });
                saveRelatedAttributes(values);

                //get mrid or name of device
                if (Boolean.TRUE.equals(deleteMeterConfigRequestMessageType.getHeader().isAsyncReplyFlag())) {
                    // call asynchronously
                    List<FaultMessage> faultMessages = new ArrayList<>();
                    meterConfig.getMeter().stream().map(meterConfigParser::asMeterInfo).forEach(meterInfo -> {
                        try {
                            deviceFinder.findDevice(meterInfo.getmRID(), meterInfo.getDeviceName());
                        } catch (FaultMessage e) {
                            faultMessages.add(e);
                        }
                    });
                    if (meterConfig.getMeter().size() == faultMessages.size()) {
                        throw faultMessageFactory.meterConfigFaultMessage(MessageSeeds.NO_DEVICE, faultMessages, ReplyType.Result.FAILED);
                    } else {
                        EndPointConfiguration outboundEndPointConfiguration = getOutboundEndPointConfiguration(deleteMeterConfigRequestMessageType.getHeader().getReplyAddress());
                        createMeterConfigServiceCallAndTransition(meterConfig, outboundEndPointConfiguration, OperationEnum.DELETE, deleteMeterConfigRequestMessageType.getHeader().getCorrelationID());
                        if (faultMessages.isEmpty()) {
                            return createQuickResponseMessage(Verb.REPLY, deleteMeterConfigRequestMessageType.getHeader().getCorrelationID());
                        } else {
                            return createResponseMessageCustomPayload(Verb.REPLY, deleteMeterConfigRequestMessageType.getHeader().getCorrelationID(),
                                    faultMessageFactory.meterConfigFaultMessage(MessageSeeds.NO_DEVICE, faultMessages, ReplyType.Result.PARTIAL).getFaultInfo().getReply());
                        }
                    }
                } else {
                    // call synchronously
                    Meter meter = meterConfig.getMeter().stream().findFirst()
                            .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(null, MessageSeeds.EMPTY_LIST, METER_ITEM));
                    MeterInfo meterInfo = meterConfigParser.asMeterInfo(meter);
                    Device device = deviceFinder.findDevice(meterInfo.getmRID(), meterInfo.getDeviceName());
                    deviceDeleter.delete(device);
                    return createResponseMessage(null, Verb.DELETED, deleteMeterConfigRequestMessageType.getHeader().getCorrelationID());
                }
            } catch (LocalizedException e) {
                throw faultMessageFactory.meterConfigFaultMessage(null, MessageSeeds.UNABLE_TO_DELETE_DEVICE, e.getLocalizedMessage());
            }
        });
    }

    @Override
    public MeterConfigResponseMessageType getMeterConfig(MeterConfigRequestMessageType meterConfigRequestMessageType) throws FaultMessage {
        return runInTransactionWithOccurrence(() -> {
            try {
                MeterConfig meterConfig = meterConfigRequestMessageType.getPayload().getMeterConfig();
                SetMultimap<String, String> values = HashMultimap.create();

                meterConfig.getMeter().stream().forEach(meter -> {
                    if (!meter.getNames().isEmpty()) {
                        values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), meter.getNames().get(0).getName());
                    }
                    if (meter.getMRID() != null) {
                        values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), meter.getMRID());
                    }
                    if (meter.getSerialNumber() != null) {
                        values.put(CimAttributeNames.CIM_DEVICE_SERIAL_NUMBER.getAttributeName(), meter.getSerialNumber());
                    }
                });
                saveRelatedAttributes(values);

                //get mrid or name of device
                if (Boolean.TRUE.equals(meterConfigRequestMessageType.getHeader().isAsyncReplyFlag())) {
                    // call asynchronously
                    List<FaultMessage> faultMessages = new ArrayList<>();
                    meterConfig.getMeter().stream().map(meterConfigParser::asMeterInfo).forEach(meterInfo -> {
                        try {
                            deviceFinder.findDevice(meterInfo.getmRID(), meterInfo.getDeviceName());
                        } catch (FaultMessage e) {
                            faultMessages.add(e);
                        }
                    });
                    if (meterConfig.getMeter().size() == faultMessages.size()) {
                        throw faultMessageFactory.meterConfigFaultMessage(MessageSeeds.NO_DEVICE, faultMessages, ReplyType.Result.FAILED);
                    } else {
                        checkMeterStatusSourceAsync(meterConfig.getMeterStatusSource());
                        isPingRequested(meterConfig.getPing(), null);
                        EndPointConfiguration outboundEndPointConfiguration = getOutboundEndPointConfiguration(meterConfigRequestMessageType.getHeader().getReplyAddress());
                        createMeterConfigServiceCallAndTransition(meterConfig, outboundEndPointConfiguration, OperationEnum.GET, meterConfigRequestMessageType.getHeader().getCorrelationID());
                        if (faultMessages.isEmpty()) {
                            return createQuickResponseMessage(HeaderType.Verb.REPLY, meterConfigRequestMessageType.getHeader().getCorrelationID());
                        } else {
                            return createResponseMessageCustomPayload(Verb.REPLY, meterConfigRequestMessageType.getHeader().getCorrelationID(),
                                    faultMessageFactory.meterConfigFaultMessage(MessageSeeds.NO_DEVICE, faultMessages, ReplyType.Result.PARTIAL).getFaultInfo().getReply());
                        }
                    }
                } else {
                    // call synchronously
                    Meter meter = meterConfig.getMeter().stream().findFirst()
                            .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(null, MessageSeeds.EMPTY_LIST, METER_ITEM));
                    MeterInfo meterInfo = meterConfigParser.asMeterInfo(meter);
                    Device device = deviceFinder.findDevice(meterInfo.getmRID(), meterInfo.getDeviceName());
                    checkMeterStatusSourceSync(meterConfig.getMeterStatusSource(), device.getName());
                    boolean meterStatusRequired = MeterStatusSource.SYSTEM.getSource().equalsIgnoreCase(meterConfig.getMeterStatusSource());
                    PingResult pingResult = PingResult.NOT_NEEDED;
                    Optional<ErrorMessage> errorMessageOptional = Optional.empty();
                    if (isPingRequested(meterConfig.getPing(), device.getName())) {
                        errorMessageOptional = meterConfigPingUtils.ping(device);
                        if (errorMessageOptional.isPresent()) {
                            pingResult = PingResult.FAILED;
                        } else {
                            pingResult = PingResult.SUCCESSFUL;
                        }
                    }
                    return createGetResponseMessage(device, pingResult, meterStatusRequired, errorMessageOptional.orElse(null),
                            meterConfigRequestMessageType.getHeader().getCorrelationID());
                }
            } catch (VerboseConstraintViolationException e) {
                throw faultMessageFactory.meterConfigFaultMessage(null, MessageSeeds.UNABLE_TO_GET_METER_CONFIG_EVENTS, e.getLocalizedMessage());
            }
        });
    }

    private void checkMeterStatusSourceSync(String meterStatusSource, String meterName) throws FaultMessage {
        if (!Strings.isNullOrEmpty(meterStatusSource) && !MeterStatusSource.SYSTEM.getSource().equalsIgnoreCase(meterStatusSource)) {
            throw faultMessageFactory.meterConfigFaultMessageSupplier(meterName, MessageSeeds.METER_STATUS_NOT_SUPPORTED, METER_STATUS_SOURCE_ELEMENT, meterStatusSource, "'" + MeterStatusSource.SYSTEM
                    .getSource() + "'").get();
        }
    }

    private void checkMeterStatusSourceAsync(String meterStatusSource) throws FaultMessage {
        if (!Strings.isNullOrEmpty(meterStatusSource)
                && !MeterStatusSource.SYSTEM.getSource().equalsIgnoreCase(meterStatusSource)
                && !MeterStatusSource.METER.getSource().equalsIgnoreCase(meterStatusSource)) {
            throw faultMessageFactory.meterConfigFaultMessageSupplier(null, MessageSeeds.METER_STATUS_NOT_SUPPORTED, METER_STATUS_SOURCE_ELEMENT, meterStatusSource,
                    new StringBuilder().append('\'')
                            .append(MeterStatusSource.SYSTEM.getSource())
                            .append("\', \'")
                            .append(MeterStatusSource.METER.getSource())
                            .append('\'').toString()).get();
        }
    }

    private boolean isPingRequested(String pingField, String meterName) throws FaultMessage {
        if (!Checks.is(pingField).emptyOrOnlyWhiteSpace()) {
            if (pingField.toLowerCase().equals("yes")) {
                return true;
            } else if (pingField.toLowerCase().equals("no")) {
                return false;
            } else {
                throw faultMessageFactory.meterConfigFaultMessageSupplier(meterName, MessageSeeds.UNSUPPORTED_PING_VALUE,
                        pingField).get();
            }
        } else {
            return false;
        }
    }

    private boolean returnDeviceIfExists(boolean defaultValue){
        Optional<EndPointConfiguration> outboundEndPointConfiguration = getEndPointConfigurations();
        if (outboundEndPointConfiguration.isPresent()) {
            Map<String, Object> properties = outboundEndPointConfiguration.get().getPropertiesWithValue();
            Object value = properties.getOrDefault(TranslationKeys.RETURN_DEVICE_IF_EXISTS.getKey(), defaultValue);
            return (boolean) value;
        }

        return defaultValue;
    }

    private Optional<EndPointConfiguration> getEndPointConfigurations() {
        return endPointConfigurationService.findEndPointConfigurations()
                .stream()
                .filter(EndPointConfiguration::isActive)
                .filter(endPointConfiguration -> endPointConfiguration.isInbound())
                .filter(ws -> ws.getWebServiceName().equalsIgnoreCase(CIM_MERER_CONFIG))
                //.filter(endPointConfiguration -> endPointConfiguration.getUrl().equals(url))
                .findFirst();

    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();

        builder.add(propertySpecService
                .specForValuesOf(new BooleanFactory())
                .named(TranslationKeys.RETURN_DEVICE_IF_EXISTS)
                .describedAs(TranslationKeys.RETURN_DEVICE_IF_EXISTS_DESCRIPTION)
                .fromThesaurus(thesaurus)
                .setDefaultValue(true)
                .markEditable()
                .finish());

        return builder.build();
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }
}
