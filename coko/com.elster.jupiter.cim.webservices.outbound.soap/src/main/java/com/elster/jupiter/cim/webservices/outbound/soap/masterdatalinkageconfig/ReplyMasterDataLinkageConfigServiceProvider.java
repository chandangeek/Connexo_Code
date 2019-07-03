/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.outbound.soap.masterdatalinkageconfig;

import com.elster.jupiter.cim.webservices.outbound.soap.FailedLinkageOperation;
import com.elster.jupiter.cim.webservices.outbound.soap.LinkageOperation;
import com.elster.jupiter.cim.webservices.outbound.soap.ReplyMasterDataLinkageConfigWebService;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;

import ch.iec.tc57._2011.masterdatalinkageconfig.MasterDataLinkageConfig;
import ch.iec.tc57._2011.masterdatalinkageconfig.Meter;
import ch.iec.tc57._2011.masterdatalinkageconfig.UsagePoint;
import ch.iec.tc57._2011.masterdatalinkageconfigmessage.MasterDataLinkageConfigEventMessageType;
import ch.iec.tc57._2011.masterdatalinkageconfigmessage.MasterDataLinkageConfigPayloadType;
import ch.iec.tc57._2011.replymasterdatalinkageconfig.FaultMessage;
import ch.iec.tc57._2011.replymasterdatalinkageconfig.MasterDataLinkageConfigPort;
import ch.iec.tc57._2011.replymasterdatalinkageconfig.ReplyMasterDataLinkageConfig;
import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.Name;
import ch.iec.tc57._2011.schema.message.ObjectType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import org.apache.cxf.jaxws.JaxWsClientProxy;
import org.apache.cxf.message.Message;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.ws.Service;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component(name = "com.elster.jupiter.cim.webservices.outbound.soap.replymasterdatalinkageconfig.provider", service = {
		ReplyMasterDataLinkageConfigWebService.class,
		OutboundSoapEndPointProvider.class }, immediate = true, property = {
				"name=" + ReplyMasterDataLinkageConfigWebService.NAME })
public class ReplyMasterDataLinkageConfigServiceProvider
		implements ReplyMasterDataLinkageConfigWebService, OutboundSoapEndPointProvider, ApplicationSpecific {

	private static final String NOUN = "MasterDateLinkageConfig";
	private static final String URL = "url";
	private static final String RESOURCE_WSDL = "/masterdatalinkageconfig/ReplyMasterDataLinkageConfig.wsdl";

	private volatile WebServicesService webServicesService;

	private final Map<String, MasterDataLinkageConfigPort> masterDataLinkageConfigPorts = new ConcurrentHashMap<>();

	private ch.iec.tc57._2011.schema.message.ObjectFactory headerTypeFactory = new ch.iec.tc57._2011.schema.message.ObjectFactory();
	private ch.iec.tc57._2011.masterdatalinkageconfigmessage.ObjectFactory payloadFactory = new ch.iec.tc57._2011.masterdatalinkageconfigmessage.ObjectFactory();

	@Reference
	public void setWebServicesService(WebServicesService webServicesService) {
		this.webServicesService = webServicesService;
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	public void addMasterDataLinkageConfigPort(MasterDataLinkageConfigPort masterDataLinkageConfigPort,
			Map<String, Object> properties) {
		masterDataLinkageConfigPorts.put(properties.get(URL).toString(), masterDataLinkageConfigPort);
	}

	public void removeMasterDataLinkageConfigPort(MasterDataLinkageConfigPort masterDataLinkageConfigPort) {
		masterDataLinkageConfigPorts.values().removeIf(
				objMasterDataLinkageConfigPort -> objMasterDataLinkageConfigPort == masterDataLinkageConfigPort);
	}

	public Map<String, MasterDataLinkageConfigPort> getMasterDataLinkageConfigPorts() {
		return Collections.unmodifiableMap(masterDataLinkageConfigPorts);
	}

	@Override
	public void call(EndPointConfiguration endPointConfiguration, String operation,
			List<LinkageOperation> successfulLinkages, List<FailedLinkageOperation> failedLinkages,
			BigDecimal expectedNumberOfCalls) {
		publish(endPointConfiguration);
		try {
			Optional.ofNullable(getMasterDataLinkageConfigPorts().get(endPointConfiguration.getUrl()))
					.filter(masterDataLinkageConfigPort -> isValidMasterDataLinkageConfigPortService(
							masterDataLinkageConfigPort))
					.ifPresent(service -> {
						try {
							switch (operation) {
							case "CREATE":
								service.createdMasterDataLinkageConfig(createResponseMessage(
										createMasterDataLinkageConfig(successfulLinkages), failedLinkages,
										expectedNumberOfCalls.intValue(), HeaderType.Verb.CREATED));
								break;
							case "CLOSE":
								service.closedMasterDataLinkageConfig(createResponseMessage(
										createMasterDataLinkageConfig(successfulLinkages), failedLinkages,
										expectedNumberOfCalls.intValue(), HeaderType.Verb.CLOSED));
								break;
							}
						} catch (FaultMessage faultMessage) {
							endPointConfiguration.log(faultMessage.getMessage(), faultMessage);
						}
					});
		} catch (RuntimeException ex) {
			endPointConfiguration.log(LogLevel.SEVERE, ex.getMessage());
		}
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

	private void publish(EndPointConfiguration endPointConfiguration) {
		if (endPointConfiguration.isActive() && !webServicesService.isPublished(endPointConfiguration)) {
			webServicesService.publishEndPoint(endPointConfiguration);
		}
	}

	boolean isValidMasterDataLinkageConfigPortService(MasterDataLinkageConfigPort masterDataLinkageConfigPort) {
		return ((JaxWsClientProxy) Proxy.getInvocationHandler(masterDataLinkageConfigPort)).getRequestContext()
				.containsKey(Message.ENDPOINT_ADDRESS);
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
		});
		return config;
	}

	private MasterDataLinkageConfigEventMessageType createResponseMessage(
			MasterDataLinkageConfig masterDataLinkageConfig, HeaderType.Verb verb) {
		MasterDataLinkageConfigEventMessageType response = new MasterDataLinkageConfigEventMessageType();
		HeaderType header = headerTypeFactory.createHeaderType();
		header.setNoun(NOUN);
		header.setVerb(verb);
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
			int expectedNumberOfCalls, HeaderType.Verb verb) {
		MasterDataLinkageConfigEventMessageType response = createResponseMessage(masterDataLinkageConfig, verb);
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
