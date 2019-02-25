/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.masterdatalinkageconfig;

import com.elster.jupiter.cim.webservices.inbound.soap.OperationEnum;
import com.elster.jupiter.cim.webservices.inbound.soap.impl.EndPointHelper;
import com.elster.jupiter.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.elster.jupiter.domain.util.VerboseConstraintViolationException;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.Checks;

import ch.iec.tc57._2011.executemasterdatalinkageconfig.FaultMessage;
import ch.iec.tc57._2011.executemasterdatalinkageconfig.MasterDataLinkageConfigPort;
import ch.iec.tc57._2011.masterdatalinkageconfigmessage.MasterDataLinkageConfigRequestMessageType;
import ch.iec.tc57._2011.masterdatalinkageconfigmessage.MasterDataLinkageConfigResponseMessageType;
import ch.iec.tc57._2011.schema.message.HeaderType;

import javax.inject.Inject;
import javax.inject.Provider;

public class ExecuteMasterDataLinkageConfigEndpoint implements MasterDataLinkageConfigPort {
    static final String NOUN = "MasterDataLinkageConfig";
    private static final String UNSUPPORTED_OPERATION_MESSAGE = "Specified action is not supported. Only Create and Close actions are allowed";

    private final TransactionService transactionService;
    private final EndPointHelper endPointHelper;
    private final MasterDataLinkageFaultMessageFactory faultMessageFactory;
    private final Provider<MasterDataLinkageHandler> masterDataLinkageHandlerProvider;
    private final Provider<MasterDataLinkageMessageValidator> masterDataLinkageMessageValidatorProvider;
    private final EndPointConfigurationService endPointConfigurationService;
    private final WebServicesService webServicesService;

    @Inject
    public ExecuteMasterDataLinkageConfigEndpoint(MasterDataLinkageFaultMessageFactory faultMessageFactory,
            TransactionService transactionService, EndPointHelper endPointHelper,
            Provider<MasterDataLinkageHandler> masterDataLinkageHandlerProvider,
            Provider<MasterDataLinkageMessageValidator> masterDataLinkageMessageValidatorProvider,
            EndPointConfigurationService endPointConfigurationService, WebServicesService webServicesService) {
        this.faultMessageFactory = faultMessageFactory;
        this.transactionService = transactionService;
        this.endPointHelper = endPointHelper;
        this.masterDataLinkageHandlerProvider = masterDataLinkageHandlerProvider;
        this.masterDataLinkageMessageValidatorProvider = masterDataLinkageMessageValidatorProvider;
        this.endPointConfigurationService = endPointConfigurationService;
        this.webServicesService = webServicesService;
    }

    @Override
    public MasterDataLinkageConfigResponseMessageType createMasterDataLinkageConfig(
            MasterDataLinkageConfigRequestMessageType message) throws FaultMessage {
        masterDataLinkageMessageValidatorProvider.get().validate(message, MasterDataLinkageAction.CREATE);
        endPointHelper.setSecurityContext();
        try (TransactionContext context = transactionService.getContext()) {
            if (Boolean.TRUE.equals(message.getHeader().isAsyncReplyFlag())) {
                // call asynchronously
                EndPointConfiguration outboundEndPointConfiguration = getOutboundEndPointConfiguration(
                        MasterDataLinkageAction.CREATE, getReplyAddress(MasterDataLinkageAction.CREATE, message));
                createMeterConfigServiceCallAndTransition(message, outboundEndPointConfiguration, OperationEnum.LINK);
                context.commit();
                return masterDataLinkageHandlerProvider.get().forMessage(message)
                        .createQuickResponseMessage(HeaderType.Verb.REPLY);
            } else {
                // call synchronously
                MasterDataLinkageConfigResponseMessageType response = masterDataLinkageHandlerProvider.get()
                        .forMessage(message).createLinkage();
                context.commit();
                return response;
            }
        } catch (VerboseConstraintViolationException e) {
            throw faultMessageFactory.createMasterDataLinkageFaultMessage(MasterDataLinkageAction.CREATE,
                    e.getLocalizedMessage());
        } catch (LocalizedException e) {
            throw faultMessageFactory.createMasterDataLinkageFaultMessage(MasterDataLinkageAction.CREATE,
                    e.getLocalizedMessage(), e.getErrorCode());
        }
    }

    @Override
    public MasterDataLinkageConfigResponseMessageType closeMasterDataLinkageConfig(
            MasterDataLinkageConfigRequestMessageType message) throws FaultMessage {
        masterDataLinkageMessageValidatorProvider.get().validate(message, MasterDataLinkageAction.CLOSE);
        endPointHelper.setSecurityContext();
        try (TransactionContext context = transactionService.getContext()) {
            MasterDataLinkageConfigResponseMessageType response = masterDataLinkageHandlerProvider.get()
                    .forMessage(message).closeLinkage();
            context.commit();
            return response;
        } catch (VerboseConstraintViolationException e) {
            throw faultMessageFactory.createMasterDataLinkageFaultMessage(MasterDataLinkageAction.CLOSE,
                    e.getLocalizedMessage());
        } catch (LocalizedException e) {
            throw faultMessageFactory.createMasterDataLinkageFaultMessage(MasterDataLinkageAction.CLOSE,
                    e.getLocalizedMessage(), e.getErrorCode());
        }
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

    private String getReplyAddress(MasterDataLinkageAction action,
            MasterDataLinkageConfigRequestMessageType requestMessage) throws FaultMessage {
        String replyAddress = requestMessage.getHeader().getReplyAddress();
        if (Checks.is(replyAddress).emptyOrOnlyWhiteSpace()) {
            throw faultMessageFactory.createMasterDataLinkageFaultMessage(action, MessageSeeds.NO_REPLY_ADDRESS);
        }
        return replyAddress;
    }

    private EndPointConfiguration getOutboundEndPointConfiguration(MasterDataLinkageAction action, String url)
            throws FaultMessage {
        EndPointConfiguration endPointConfig = endPointConfigurationService.findEndPointConfigurations().stream()
                .filter(EndPointConfiguration::isActive)
                .filter(endPointConfiguration -> !endPointConfiguration.isInbound())
                .filter(endPointConfiguration -> endPointConfiguration.getUrl().equals(url)).findFirst()
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

    private ServiceCall createMeterConfigServiceCallAndTransition(MasterDataLinkageConfigRequestMessageType meterConfig,
            EndPointConfiguration endPointConfiguration, OperationEnum operation) throws FaultMessage {
//        ServiceCall serviceCall = serviceCallCommands.createMeterConfigMasterServiceCall(meterConfig,
//                endPointConfiguration, operation);
//        serviceCallCommands.requestTransition(serviceCall, DefaultState.PENDING);
//        return serviceCall;
        return null;
    }

}
