/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.masterdatalinkageconfig;

import com.elster.jupiter.cim.webservices.inbound.soap.impl.EndPointHelper;
import com.elster.jupiter.domain.util.VerboseConstraintViolationException;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceAplication;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import ch.iec.tc57._2011.executemasterdatalinkageconfig.FaultMessage;
import ch.iec.tc57._2011.executemasterdatalinkageconfig.MasterDataLinkageConfigPort;
import ch.iec.tc57._2011.masterdatalinkageconfigmessage.MasterDataLinkageConfigRequestMessageType;
import ch.iec.tc57._2011.masterdatalinkageconfigmessage.MasterDataLinkageConfigResponseMessageType;

import javax.inject.Inject;
import javax.inject.Provider;

public class ExecuteMasterDataLinkageConfigEndpoint implements MasterDataLinkageConfigPort, WebServiceAplication {
    static final String NOUN = "MasterDataLinkageConfig";
    private static final String UNSUPPORTED_OPERATION_MESSAGE = "Specified action is not supported. Only Create and Close actions are allowed";

    private final TransactionService transactionService;
    private final EndPointHelper endPointHelper;
    private final MasterDataLinkageFaultMessageFactory faultMessageFactory;
    private final Provider<MasterDataLinkageHandler> masterDataLinkageHandlerProvider;
    private final Provider<MasterDataLinkageMessageValidator> masterDataLinkageMessageValidatorProvider;

    @Inject
    public ExecuteMasterDataLinkageConfigEndpoint(MasterDataLinkageFaultMessageFactory faultMessageFactory,
                                                  TransactionService transactionService,
                                                  EndPointHelper endPointHelper,
                                                  Provider<MasterDataLinkageHandler> masterDataLinkageHandlerProvider,
                                                  Provider<MasterDataLinkageMessageValidator> masterDataLinkageMessageValidatorProvider) {
        this.faultMessageFactory = faultMessageFactory;
        this.transactionService = transactionService;
        this.endPointHelper = endPointHelper;
        this.masterDataLinkageHandlerProvider = masterDataLinkageHandlerProvider;
        this.masterDataLinkageMessageValidatorProvider = masterDataLinkageMessageValidatorProvider;
    }

    @Override
    public MasterDataLinkageConfigResponseMessageType createMasterDataLinkageConfig(MasterDataLinkageConfigRequestMessageType message) throws FaultMessage {
        masterDataLinkageMessageValidatorProvider.get().validate(message, MasterDataLinkageAction.CREATE);
        endPointHelper.setSecurityContext();
        try (TransactionContext context = transactionService.getContext()) {
            MasterDataLinkageConfigResponseMessageType response = masterDataLinkageHandlerProvider.get().forMessage(message).createLinkage();
            context.commit();
            return response;
        } catch (VerboseConstraintViolationException e) {
            throw faultMessageFactory.createMasterDataLinkageFaultMessage(MasterDataLinkageAction.CREATE, e.getLocalizedMessage());
        } catch (LocalizedException e) {
            throw faultMessageFactory.createMasterDataLinkageFaultMessage(MasterDataLinkageAction.CREATE, e.getLocalizedMessage(), e.getErrorCode());
        }
    }

    @Override
    public MasterDataLinkageConfigResponseMessageType closeMasterDataLinkageConfig(MasterDataLinkageConfigRequestMessageType message) throws FaultMessage {
        masterDataLinkageMessageValidatorProvider.get().validate(message, MasterDataLinkageAction.CLOSE);
        endPointHelper.setSecurityContext();
        try (TransactionContext context = transactionService.getContext()) {
            MasterDataLinkageConfigResponseMessageType response = masterDataLinkageHandlerProvider.get().forMessage(message).closeLinkage();
            context.commit();
            return response;
        } catch (VerboseConstraintViolationException e) {
            throw faultMessageFactory.createMasterDataLinkageFaultMessage(MasterDataLinkageAction.CLOSE, e.getLocalizedMessage());
        } catch (LocalizedException e) {
            throw faultMessageFactory.createMasterDataLinkageFaultMessage(MasterDataLinkageAction.CLOSE, e.getLocalizedMessage(), e.getErrorCode());
        }
    }

    @Override
    public MasterDataLinkageConfigResponseMessageType changeMasterDataLinkageConfig(MasterDataLinkageConfigRequestMessageType message) throws FaultMessage {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public MasterDataLinkageConfigResponseMessageType cancelMasterDataLinkageConfig(MasterDataLinkageConfigRequestMessageType message) throws FaultMessage {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public MasterDataLinkageConfigResponseMessageType deleteMasterDataLinkageConfig(MasterDataLinkageConfigRequestMessageType message) throws FaultMessage {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public String getApplication(){
        return WebServiceApplicationName.MULTISENSE.getName();
    }
}
