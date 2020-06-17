/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.cim.webservices.outbound.soap.masterdatalinkageconfig;

import com.elster.jupiter.metering.CimAttributeNames;
import com.elster.jupiter.metering.CimUsagePointAttributeNames;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;

import com.energyict.mdc.cim.webservices.outbound.soap.FailedLinkageOperation;
import com.energyict.mdc.cim.webservices.outbound.soap.LinkageOperation;
import com.energyict.mdc.cim.webservices.outbound.soap.ReplyMasterDataLinkageConfigWebService;

import ch.iec.tc57._2011.masterdatalinkageconfig.EndDevice;
import ch.iec.tc57._2011.masterdatalinkageconfig.MasterDataLinkageConfig;
import ch.iec.tc57._2011.masterdatalinkageconfig.Meter;
import ch.iec.tc57._2011.masterdatalinkageconfig.UsagePoint;
import ch.iec.tc57._2011.masterdatalinkageconfigmessage.MasterDataLinkageConfigEventMessageType;
import ch.iec.tc57._2011.masterdatalinkageconfigmessage.MasterDataLinkageConfigPayloadType;
import ch.iec.tc57._2011.replymasterdatalinkageconfig.MasterDataLinkageConfigPort;
import ch.iec.tc57._2011.replymasterdatalinkageconfig.ReplyMasterDataLinkageConfig;
import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.Name;
import ch.iec.tc57._2011.schema.message.ObjectType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.ws.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component(name = "com.energyict.mdc.cim.webservices.outbound.soap.replymasterdatalinkageconfig.provider", service = {
		ReplyMasterDataLinkageConfigWebService.class,
		OutboundSoapEndPointProvider.class }, immediate = true, property = {
				"name=" + ReplyMasterDataLinkageConfigWebService.NAME })
public class ReplyMasterDataLinkageConfigServiceProvider
		extends AbstractOutboundEndPointProvider<MasterDataLinkageConfigPort>
		implements ReplyMasterDataLinkageConfigWebService, OutboundSoapEndPointProvider, ApplicationSpecific {

	private static final String NOUN = "MasterDateLinkageConfig";
	private static final String RESOURCE_WSDL = "/wsdl/masterdatalinkageconfig/ReplyMasterDataLinkageConfig.wsdl";

	private final Map<String, MasterDataLinkageConfigPort> masterDataLinkageConfigPorts = new ConcurrentHashMap<>();

	private ch.iec.tc57._2011.schema.message.ObjectFactory headerTypeFactory = new ch.iec.tc57._2011.schema.message.ObjectFactory();
	private ch.iec.tc57._2011.masterdatalinkageconfigmessage.ObjectFactory payloadFactory = new ch.iec.tc57._2011.masterdatalinkageconfigmessage.ObjectFactory();

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	public void addMasterDataLinkageConfigPort(MasterDataLinkageConfigPort masterDataLinkageConfigPort,
			Map<String, Object> properties) {
		super.doAddEndpoint(masterDataLinkageConfigPort, properties);
	}

	public void removeMasterDataLinkageConfigPort(MasterDataLinkageConfigPort masterDataLinkageConfigPort) {
		super.doRemoveEndpoint(masterDataLinkageConfigPort);
	}

	@Reference
	public void addWebServicesService(WebServicesService webServicesService) {
		// Just to inject WebServicesService
	}

	@Override
	public void	call(EndPointConfiguration endPointConfiguration, String operation,
						List<LinkageOperation> successfulLinkages, List<FailedLinkageOperation> failedLinkages,
						BigDecimal expectedNumberOfCalls, String correlationId) {
		String method;
		MasterDataLinkageConfigEventMessageType message;
		switch (operation) {
			case "CREATE":
				method = "createdMasterDataLinkageConfig";
				message = createResponseMessage(
						createMasterDataLinkageConfig(successfulLinkages), failedLinkages,
						expectedNumberOfCalls.intValue(), HeaderType.Verb.CREATED, correlationId);
				break;
			case "CLOSE":
				method = "closedMasterDataLinkageConfig";
				message = createResponseMessage(
						createMasterDataLinkageConfig(successfulLinkages), failedLinkages,
						expectedNumberOfCalls.intValue(), HeaderType.Verb.CLOSED, correlationId);
				break;
			default:
				throw new UnsupportedOperationException(operation + " isn't supported.");
		}
		SetMultimap<String, String> values = HashMultimap.create();
		successfulLinkages.forEach(link->{
			values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), link.getMeterMrid());
			values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), link.getMeterName());
			values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), link.getEndDeviceMrid());
			values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), link.getEndDeviceName());
			values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_MR_ID.getAttributeName(), link.getUsagePointMrid());
			values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_NAME.getAttributeName(), link.getUsagePointName());

		});
		failedLinkages.forEach(link->{
			values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), link.getMeterMrid());
			values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), link.getMeterName());
			values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), link.getEndDeviceMrid());
			values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), link.getEndDeviceName());
			values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_MR_ID.getAttributeName(), link.getUsagePointMrid());
			values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_NAME.getAttributeName(), link.getUsagePointName());
		});

		using(method)
				.toEndpoints(endPointConfiguration)
				.withRelatedAttributes(values)
				.send(message);
	}

	@Override
	public Service get() {
		return new ReplyMasterDataLinkageConfig(this.getClass().getResource(RESOURCE_WSDL));
	}

	@SuppressWarnings("rawtypes")
    @Override
    public Class getService() {
		return MasterDataLinkageConfigPort.class;
	}

	@Override
	protected String getName() {
		return ReplyMasterDataLinkageConfigWebService.NAME;
	}

	private MasterDataLinkageConfig createMasterDataLinkageConfig(List<LinkageOperation> successfulLinkageOperations) {
		MasterDataLinkageConfig config = new MasterDataLinkageConfig();
		successfulLinkageOperations.forEach(linkage -> {
			UsagePoint usagePoint = new UsagePoint();
			usagePoint.setMRID(linkage.getUsagePointMrid());
			ch.iec.tc57._2011.masterdatalinkageconfig.Name upName = new ch.iec.tc57._2011.masterdatalinkageconfig.Name();
			upName.setName(linkage.getUsagePointName());
			usagePoint.getNames().add(upName);
			config.getUsagePoint().add(usagePoint);
			Meter meter = new Meter();
			meter.setMRID(linkage.getMeterMrid());
			ch.iec.tc57._2011.masterdatalinkageconfig.Name meterName = new ch.iec.tc57._2011.masterdatalinkageconfig.Name();
			meterName.setName(linkage.getMeterName());
			meter.getNames().add(meterName);
			config.getMeter().add(meter);
			EndDevice endDevice = new EndDevice();
			endDevice.setMRID(linkage.getEndDeviceMrid());
			ch.iec.tc57._2011.masterdatalinkageconfig.Name endDeviceName = new ch.iec.tc57._2011.masterdatalinkageconfig.Name();
			endDeviceName.setName(linkage.getEndDeviceName());
			endDevice.getNames().add(endDeviceName);
			config.getEndDevice().add(endDevice);

		});
		return config;
	}

	private MasterDataLinkageConfigEventMessageType createResponseMessage(
			MasterDataLinkageConfig masterDataLinkageConfig, HeaderType.Verb verb, String correlationId) {
		MasterDataLinkageConfigEventMessageType response = new MasterDataLinkageConfigEventMessageType();
		HeaderType header = headerTypeFactory.createHeaderType();
		header.setNoun(NOUN);
		header.setVerb(verb);
		header.setCorrelationID(correlationId);
		response.setHeader(header);
		ReplyType replyType = headerTypeFactory.createReplyType();
		replyType.setResult(ReplyType.Result.OK);
		response.setReply(replyType);
		MasterDataLinkageConfigPayloadType payloadType = payloadFactory.createMasterDataLinkageConfigPayloadType();
		payloadType.setMasterDataLinkageConfig(masterDataLinkageConfig);
		response.setPayload(payloadType);
		return response;
	}

	private MasterDataLinkageConfigEventMessageType createResponseMessage(
			MasterDataLinkageConfig masterDataLinkageConfig, List<FailedLinkageOperation> failedLinkages,
			int expectedNumberOfCalls, HeaderType.Verb verb, String correlationId) {
		MasterDataLinkageConfigEventMessageType response = createResponseMessage(masterDataLinkageConfig, verb, correlationId);
		final ReplyType replyType = headerTypeFactory.createReplyType();
		if (expectedNumberOfCalls == masterDataLinkageConfig.getMeter().size()) {
			replyType.setResult(ReplyType.Result.OK);
		} else if (expectedNumberOfCalls == failedLinkages.size()) {
			replyType.setResult(ReplyType.Result.FAILED);
		} else {
			replyType.setResult(ReplyType.Result.PARTIAL);
		}
		failedLinkages.forEach(failedLinkage -> {
			ErrorType errorType = new ErrorType();
			errorType.setCode(failedLinkage.getErrorCode());
			errorType.setDetails(failedLinkage.getErrorMessage());
			ObjectType objectType = new ObjectType();
			objectType.setMRID(failedLinkage.getMeterMrid());
			objectType.setObjectType("Meter");
			Name name = new Name();
			name.setName(failedLinkage.getMeterName());
			objectType.getName().add(name);
			errorType.setObject(objectType);
			replyType.getError().add(errorType);
		});
		response.setReply(replyType);
		return response;
	}

	@Override
	public String getApplication(){
		return WebServiceApplicationName.MULTISENSE_INSIGHT.getName();
	};
}
