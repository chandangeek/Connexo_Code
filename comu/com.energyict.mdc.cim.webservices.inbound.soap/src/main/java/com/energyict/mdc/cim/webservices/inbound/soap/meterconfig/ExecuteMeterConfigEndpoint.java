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
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.Checks;

import com.energyict.mdc.cim.webservices.inbound.soap.MeterInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.OperationEnum;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.customattributeset.CustomPropertySetHelper;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.CustomPropertySetInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.EndPointHelper;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.ReplyTypeFactory;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.SecurityHelper;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.SecurityKeyInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.ServiceCallCommands;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.exceptions.InvalidLastCheckedException;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolationException;

import ch.iec.tc57._2011.executemeterconfig.FaultMessage;
import ch.iec.tc57._2011.executemeterconfig.MeterConfigPort;
import ch.iec.tc57._2011.meterconfig.Meter;
import ch.iec.tc57._2011.meterconfig.MeterConfig;
import ch.iec.tc57._2011.meterconfig.Name;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigPayloadType;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigRequestMessageType;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigResponseMessageType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.HeaderType.Verb;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExecuteMeterConfigEndpoint implements MeterConfigPort {

    private static final String NOUN = "MeterConfig";
    private static final String METER_ITEM = NOUN + ".Meter";

    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private final ch.iec.tc57._2011.meterconfigmessage.ObjectFactory meterConfigMessageObjectFactory = new ch.iec.tc57._2011.meterconfigmessage.ObjectFactory();

    private volatile TransactionService transactionService;
    private volatile MeterConfigFaultMessageFactory faultMessageFactory;
    private volatile MeterConfigFactory meterConfigFactory;
    private volatile MeterConfigParser meterConfigParser;
    private volatile ReplyTypeFactory replyTypeFactory;
    private volatile EndPointHelper endPointHelper;
    private volatile DeviceBuilder deviceBuilder;

    private volatile ServiceCallCommands serviceCallCommands;
    private volatile EndPointConfigurationService endPointConfigurationService;
    private volatile WebServicesService webServicesService;
    private volatile InboundCIMWebServiceExtensionFactory webServiceExtensionFactory;
    private volatile CustomPropertySetHelper customPropertySetHelper;
    private volatile SecurityHelper securityHelper;

    @Inject
    public ExecuteMeterConfigEndpoint(TransactionService transactionService, MeterConfigFactory meterConfigFactory,
            MeterConfigFaultMessageFactory faultMessageFactory, ReplyTypeFactory replyTypeFactory,
            EndPointHelper endPointHelper, DeviceBuilder deviceBuilder, ServiceCallCommands serviceCallCommands,
            EndPointConfigurationService endPointConfigurationService, MeterConfigParser meterConfigParser,
            WebServicesService webServicesService, InboundCIMWebServiceExtensionFactory webServiceExtensionFactory,
            CustomPropertySetHelper customPropertySetHelper, SecurityHelper securityHelper) {
        this.transactionService = transactionService;
        this.meterConfigFactory = meterConfigFactory;
        this.meterConfigParser = meterConfigParser;
        this.faultMessageFactory = faultMessageFactory;
        this.replyTypeFactory = replyTypeFactory;
        this.endPointHelper = endPointHelper;
        this.deviceBuilder = deviceBuilder;
        this.serviceCallCommands = serviceCallCommands;
        this.endPointConfigurationService = endPointConfigurationService;
        this.webServicesService = webServicesService;
        this.webServiceExtensionFactory = webServiceExtensionFactory;
        this.customPropertySetHelper = customPropertySetHelper;
        this.securityHelper = securityHelper;
    }

    @Override
    public MeterConfigResponseMessageType createMeterConfig(MeterConfigRequestMessageType requestMessage)
            throws FaultMessage {
        endPointHelper.setSecurityContext();
        String meterName = null;
        try (TransactionContext context = transactionService.getContext()) {
            MeterConfig meterConfig = requestMessage.getPayload().getMeterConfig();
            if (Boolean.TRUE.equals(requestMessage.getHeader().isAsyncReplyFlag())) {
                // call asynchronously
                EndPointConfiguration outboundEndPointConfiguration = getOutboundEndPointConfiguration(
                        getReplyAddress(requestMessage));
                createMeterConfigServiceCallAndTransition(meterConfig, outboundEndPointConfiguration,
                        OperationEnum.CREATE);
                context.commit();
                return createQuickResponseMessage(HeaderType.Verb.REPLY);
            } else if (meterConfig.getMeter().size() > 1) {
                throw faultMessageFactory.meterConfigFaultMessage(meterName, MessageSeeds.UNABLE_TO_CREATE_DEVICE,
                        MessageSeeds.SYNC_MODE_NOT_SUPPORTED);
            } else {
                // call synchronously
                Meter meter = meterConfig.getMeter().stream().findFirst().orElseThrow(faultMessageFactory
                        .meterConfigFaultMessageSupplier(meterName, MessageSeeds.EMPTY_LIST, METER_ITEM));
                MeterInfo meterInfo = meterConfigParser.asMeterInfo(meter, meterConfig.getSimpleEndDeviceFunction(),
                        OperationEnum.CREATE);
                meterName = meter.getNames().stream().findFirst().map(Name::getName).orElse(null);

                Device createdDevice = deviceBuilder.prepareCreateFrom(meterInfo).build();
                return processDevice(createdDevice, meterInfo, HeaderType.Verb.CREATED, context);
            }
        } catch (VerboseConstraintViolationException e) {
            throw faultMessageFactory.meterConfigFaultMessage(meterName, MessageSeeds.UNABLE_TO_CREATE_DEVICE,
                    e.getLocalizedMessage());
        } catch (LocalizedException e) {
            throw faultMessageFactory.meterConfigFaultMessage(meterName, MessageSeeds.UNABLE_TO_CREATE_DEVICE,
                    e.getLocalizedMessage(), e.getErrorCode());
        }
    }

    private MeterConfigResponseMessageType processDevice(Device device, MeterInfo meterInfo, Verb verb,
            TransactionContext context) throws FaultMessage {
		List<FaultMessage> faults = new ArrayList<>();
		faults.addAll(processCustomAttributeSets(device, meterInfo));
		faults.addAll(processSecurityAttributes(device, meterInfo));
        if (faults.isEmpty()) {
            postProcessDevice(device, meterInfo);
            context.commit();
            return createResponseMessage(device, verb);
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
        endPointHelper.setSecurityContext();
        String meterName = null;
        try (TransactionContext context = transactionService.getContext()) {
            MeterConfig meterConfig = requestMessage.getPayload().getMeterConfig();
            if (Boolean.TRUE.equals(requestMessage.getHeader().isAsyncReplyFlag())) {
                // call asynchronously
                EndPointConfiguration outboundEndPointConfiguration = getOutboundEndPointConfiguration(
                        getReplyAddress(requestMessage));
                createMeterConfigServiceCallAndTransition(meterConfig, outboundEndPointConfiguration,
                        OperationEnum.UPDATE);
                context.commit();
                return createQuickResponseMessage(HeaderType.Verb.REPLY);
            } else if (meterConfig.getMeter().size() > 1) {
                throw faultMessageFactory.meterConfigFaultMessage(meterName, MessageSeeds.UNABLE_TO_CHANGE_DEVICE,
                        MessageSeeds.SYNC_MODE_NOT_SUPPORTED);
            } else {
                // call synchronously
                Meter meter = meterConfig.getMeter().stream().findFirst().orElseThrow(faultMessageFactory
                        .meterConfigFaultMessageSupplier(meterName, MessageSeeds.EMPTY_LIST, METER_ITEM));
                MeterInfo meterInfo = meterConfigParser.asMeterInfo(meter, meterConfig.getSimpleEndDeviceFunction(),
                        OperationEnum.UPDATE);
                meterName = meter.getNames().stream().findFirst().map(Name::getName).orElse(null);
                Device changedDevice = deviceBuilder.prepareChangeFrom(meterInfo).build();
                return processDevice(changedDevice, meterInfo, HeaderType.Verb.CHANGED, context);
            }
        } catch (VerboseConstraintViolationException | SecurityException | InvalidLastCheckedException
                | DeviceLifeCycleActionViolationException e) {
            throw faultMessageFactory.meterConfigFaultMessage(meterName, MessageSeeds.UNABLE_TO_CHANGE_DEVICE,
                    e.getLocalizedMessage());
        } catch (LocalizedException e) {
            throw faultMessageFactory.meterConfigFaultMessage(meterName, MessageSeeds.UNABLE_TO_CHANGE_DEVICE,
                    e.getLocalizedMessage(), e.getErrorCode());
        }
    }

    private String getReplyAddress(MeterConfigRequestMessageType requestMessage) throws FaultMessage {
        String replyAddress = requestMessage.getHeader().getReplyAddress();
        if (Checks.is(replyAddress).emptyOrOnlyWhiteSpace()) {
            throw faultMessageFactory.meterConfigFaultMessage(null, MessageSeeds.UNABLE_TO_CREATE_DEVICE,
                    MessageSeeds.NO_REPLY_ADDRESS);
        }
        return replyAddress;
    }

    private EndPointConfiguration getOutboundEndPointConfiguration(String url) throws FaultMessage {
        EndPointConfiguration endPointConfig = endPointConfigurationService.findEndPointConfigurations().stream()
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
        return endPointConfig;
    }

    private ServiceCall createMeterConfigServiceCallAndTransition(MeterConfig meterConfig,
            EndPointConfiguration endPointConfiguration, OperationEnum operation) throws FaultMessage {
        ServiceCall serviceCall = serviceCallCommands.createMeterConfigMasterServiceCall(meterConfig,
                endPointConfiguration, operation);
        serviceCallCommands.requestTransition(serviceCall, DefaultState.PENDING);
        return serviceCall;
    }

    private MeterConfigResponseMessageType createResponseMessage(Device device, HeaderType.Verb verb) {
        MeterConfigResponseMessageType responseMessage = meterConfigMessageObjectFactory
                .createMeterConfigResponseMessageType();

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
        MeterConfigResponseMessageType responseMessage = meterConfigMessageObjectFactory
                .createMeterConfigResponseMessageType();

        // set header
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setNoun(NOUN);
        header.setVerb(verb);
        responseMessage.setHeader(header);

        // set reply
        responseMessage.setReply(replyTypeFactory.okReplyType());

        return responseMessage;
    }

    private void postProcessDevice(Device device, MeterInfo meterInfo) {
        if (webServiceExtensionFactory != null) {
            if (webServiceExtensionFactory.getWebServiceExtension().isPresent()) {
                webServiceExtensionFactory.getWebServiceExtension().get().extendMeterInfo(device, meterInfo);
            }
        }
    }

    private List<FaultMessage> processCustomAttributeSets(Device device, MeterInfo meterInfo) {
        List<CustomPropertySetInfo> customAttributeSets = meterInfo.getCustomAttributeSets();
        if (!customAttributeSets.isEmpty()) {
            return customPropertySetHelper.addCustomPropertySetsData(device, customAttributeSets);
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
            MeterConfigRequestMessageType deleteMeterConfigRequestMessage) throws FaultMessage {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
