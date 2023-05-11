/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.masterdatalinkageconfig;

import com.elster.jupiter.domain.util.VerboseConstraintViolationException;
import com.elster.jupiter.metering.CimAttributeNames;
import com.elster.jupiter.metering.CimUsagePointAttributeNames;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.streams.ExceptionThrowingSupplier;

import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.ServiceCallCommands;

import ch.iec.tc57._2011.executemasterdatalinkageconfig.FaultMessage;
import ch.iec.tc57._2011.executemasterdatalinkageconfig.MasterDataLinkageConfigPort;
import ch.iec.tc57._2011.masterdatalinkageconfigmessage.MasterDataLinkageConfigRequestMessageType;
import ch.iec.tc57._2011.masterdatalinkageconfigmessage.MasterDataLinkageConfigResponseMessageType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javax.inject.Inject;
import javax.inject.Provider;

import java.util.Optional;

public class ExecuteMasterDataLinkageConfigEndpoint extends AbstractInboundEndPoint implements MasterDataLinkageConfigPort, ApplicationSpecific {
    static final String NOUN = "MasterDataLinkageConfig";
    private static final String UNSUPPORTED_OPERATION_MESSAGE = "Specified action is not supported. Only Create and Close actions are allowed";

    private final MasterDataLinkageFaultMessageFactory faultMessageFactory;
    private final Provider<MasterDataLinkageHandler> masterDataLinkageHandlerProvider;
    private final Provider<MasterDataLinkageMessageValidator> masterDataLinkageMessageValidatorProvider;
    private final EndPointConfigurationService endPointConfigurationService;
    private final WebServicesService webServicesService;
    private final ServiceCallCommands serviceCallCommands;

    @Inject
    public ExecuteMasterDataLinkageConfigEndpoint(MasterDataLinkageFaultMessageFactory faultMessageFactory,
            Provider<MasterDataLinkageHandler> masterDataLinkageHandlerProvider,
            Provider<MasterDataLinkageMessageValidator> masterDataLinkageMessageValidatorProvider,
            EndPointConfigurationService endPointConfigurationService, WebServicesService webServicesService,
            ServiceCallCommands serviceCallCommands) {
        this.faultMessageFactory = faultMessageFactory;
        this.masterDataLinkageHandlerProvider = masterDataLinkageHandlerProvider;
        this.masterDataLinkageMessageValidatorProvider = masterDataLinkageMessageValidatorProvider;
        this.endPointConfigurationService = endPointConfigurationService;
        this.webServicesService = webServicesService;
        this.serviceCallCommands = serviceCallCommands;
    }

    @Override
    public MasterDataLinkageConfigResponseMessageType createMasterDataLinkageConfig(
            MasterDataLinkageConfigRequestMessageType message) throws FaultMessage {
        return process(message, MasterDataLinkageAction.CREATE, () -> {
            MasterDataLinkageConfigResponseMessageType response = masterDataLinkageHandlerProvider.get()
                    .forMessage(message).createLinkage();
            return response;
        });
    }

    @Override
    public MasterDataLinkageConfigResponseMessageType closeMasterDataLinkageConfig(
            MasterDataLinkageConfigRequestMessageType message) throws FaultMessage {
        return process(message, MasterDataLinkageAction.CLOSE, () -> {
            MasterDataLinkageConfigResponseMessageType response = masterDataLinkageHandlerProvider.get()
                    .forMessage(message).closeLinkage();
            return response;
        });
    }

    private MasterDataLinkageConfigResponseMessageType process(MasterDataLinkageConfigRequestMessageType message,
            MasterDataLinkageAction action,
            ExceptionThrowingSupplier<MasterDataLinkageConfigResponseMessageType, FaultMessage> synchronousProcessor)
            throws FaultMessage {
        masterDataLinkageMessageValidatorProvider.get().validate(message, action);
        return runInTransactionWithOccurrence(() -> {
            try {
                SetMultimap<String, String> values = HashMultimap.create();
                message.getPayload().getMasterDataLinkageConfig().getMeter().forEach(meter -> {
                    if (!meter.getNames().isEmpty()) {
                        values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), meter.getNames().get(0).getName());
                    }
                    values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), meter.getMRID());
                });
                message.getPayload().getMasterDataLinkageConfig().getEndDevice().forEach(endDevice -> {
                    if (!endDevice.getNames().isEmpty()) {
                        values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), endDevice.getNames().get(0).getName());
                    }
                    values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), endDevice.getMRID());
                });
                message.getPayload().getMasterDataLinkageConfig().getUsagePoint().forEach(usagePoint -> {
                    if (!usagePoint.getNames().isEmpty()) {
                        values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_NAME.getAttributeName(), usagePoint.getNames().get(0).getName());
                    }
                    values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_MR_ID.getAttributeName(), usagePoint.getMRID());
                });

                saveRelatedAttributes(values);

                if (Boolean.TRUE.equals(message.getHeader().isAsyncReplyFlag())) {
                    return processAsynchronously(message, action);
                }
                return synchronousProcessor.get();
            } catch (VerboseConstraintViolationException e) {
                throw faultMessageFactory.createMasterDataLinkageFaultMessage(action, e.getLocalizedMessage());
            } catch (LocalizedException e) {
                throw faultMessageFactory.createMasterDataLinkageFaultMessage(action, e.getLocalizedMessage(),
                        e.getErrorCode());
            }
        });
    }

    private MasterDataLinkageConfigResponseMessageType processAsynchronously(
            MasterDataLinkageConfigRequestMessageType message, MasterDataLinkageAction action) throws FaultMessage {
        Optional<EndPointConfiguration> outboundEndPointConfiguration;
        String replyAddress = getReplyAddress(message);
        if (Checks.is(replyAddress).emptyOrOnlyWhiteSpace()) {
            outboundEndPointConfiguration = Optional.empty();
        } else {
            outboundEndPointConfiguration = Optional.of(getOutboundEndPointConfiguration(action, replyAddress));
        }
        createServiceCallAndTransition(message, outboundEndPointConfiguration, action);
        return masterDataLinkageHandlerProvider.get().forMessage(message)
                .createQuickResponseMessage(HeaderType.Verb.REPLY, message.getHeader().getCorrelationID());
    }

    @Override
    public MasterDataLinkageConfigResponseMessageType changeMasterDataLinkageConfig(
            MasterDataLinkageConfigRequestMessageType message) throws FaultMessage {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public MasterDataLinkageConfigResponseMessageType cancelMasterDataLinkageConfig(
            MasterDataLinkageConfigRequestMessageType message) throws FaultMessage {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public MasterDataLinkageConfigResponseMessageType deleteMasterDataLinkageConfig(
            MasterDataLinkageConfigRequestMessageType message) throws FaultMessage {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    private String getReplyAddress(MasterDataLinkageConfigRequestMessageType requestMessage) throws FaultMessage {
        return requestMessage.getHeader().getReplyAddress();
    }

    private EndPointConfiguration getOutboundEndPointConfiguration(MasterDataLinkageAction action, String url)
            throws FaultMessage {
        EndPointConfiguration endPointConfig = endPointConfigurationService.getOutboundEndpointConfigurationByUrl(url)
                .filter(EndPointConfiguration::isActive)
                .orElseThrow(faultMessageFactory.createMasterDataLinkageFaultMessageSupplier(action,
                        MessageSeeds.NO_END_POINT_WITH_URL, url));
        if (!webServicesService.isPublished(endPointConfig)) {
            webServicesService.publishEndPoint(endPointConfig);
        }
        if (!webServicesService.isPublished(endPointConfig)) {
            throw faultMessageFactory.createMasterDataLinkageFaultMessage(action,
                    MessageSeeds.NO_PUBLISHED_END_POINT_WITH_URL, url);
        }
        return endPointConfig;
    }

    private ServiceCall createServiceCallAndTransition(MasterDataLinkageConfigRequestMessageType config,
            Optional<EndPointConfiguration> endPointConfiguration, MasterDataLinkageAction action) throws FaultMessage {
        ServiceCall serviceCall = serviceCallCommands.createMasterDataLinkageConfigMasterServiceCall(config,
                endPointConfiguration, action, faultMessageFactory);
        serviceCallCommands.requestTransition(serviceCall, DefaultState.PENDING);
        return serviceCall;
    }

    @Override
    public String getApplication(){
        return WebServiceApplicationName.MULTISENSE_INSIGHT.getName();
    }
}
