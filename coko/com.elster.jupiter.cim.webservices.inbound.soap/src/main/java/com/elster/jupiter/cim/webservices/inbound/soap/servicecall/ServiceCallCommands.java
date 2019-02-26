package com.elster.jupiter.cim.webservices.inbound.soap.servicecall;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.TransactionRequired;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallBuilder;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.util.json.JsonService;

import ch.iec.tc57._2011.masterdatalinkageconfig.ConfigurationEvent;
import ch.iec.tc57._2011.masterdatalinkageconfig.Meter;
import ch.iec.tc57._2011.masterdatalinkageconfig.UsagePoint;
import ch.iec.tc57._2011.masterdatalinkageconfigmessage.MasterDataLinkageConfigRequestMessageType;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;

import com.elster.jupiter.cim.webservices.inbound.soap.OperationEnum;
import com.elster.jupiter.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigMasterServiceCallHandler;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigServiceCallHandler;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigDomainExtension;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigMasterCustomPropertySet;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigMasterDomainExtension;

public class ServiceCallCommands {

	public enum ServiceCallTypes {

		MASTER_DATA_LINKAGE_CONFIG(MasterDataLinkageConfigMasterServiceCallHandler.SERVICE_CALL_HANDLER_NAME,
				MasterDataLinkageConfigMasterServiceCallHandler.VERSION,
				MasterDataLinkageConfigMasterCustomPropertySet.class.getName()),
		DATA_LINKAGE_CONFIG(MasterDataLinkageConfigServiceCallHandler.SERVICE_CALL_HANDLER_NAME,
				MasterDataLinkageConfigServiceCallHandler.VERSION, "");

		private final String typeName;
		private final String typeVersion;
		private final String customPropertySetClass;

		ServiceCallTypes(String typeName, String typeVersion, String customPropertySetClass) {
			this.typeName = typeName;
			this.typeVersion = typeVersion;
			this.customPropertySetClass = customPropertySetClass;
		}

		public String getTypeName() {
			return typeName;
		}

		public String getTypeVersion() {
			return typeVersion;
		}

		public String getCustomPropertySetClass() {
			return customPropertySetClass;
		}
	}

	private final ServiceCallService serviceCallService;
	private final JsonService jsonService;
	private final Thesaurus thesaurus;

	@Inject
	public ServiceCallCommands(ServiceCallService serviceCallService, JsonService jsonService, Thesaurus thesaurus) {
		this.serviceCallService = serviceCallService;
		this.jsonService = jsonService;
		this.thesaurus = thesaurus;
	}

	@TransactionRequired
	public void requestTransition(ServiceCall serviceCall, DefaultState newState) {
		serviceCall.requestTransition(newState);
	}

	@TransactionRequired
	public ServiceCall createMasterDataLinkageConfigMasterServiceCall(
			MasterDataLinkageConfigRequestMessageType meterConfig, EndPointConfiguration endPointConfiguration,
			OperationEnum operation) {
		ServiceCallType serviceCallType = getServiceCallType(ServiceCallTypes.MASTER_DATA_LINKAGE_CONFIG);
		MasterDataLinkageConfigMasterDomainExtension domainExtension = new MasterDataLinkageConfigMasterDomainExtension();
		domainExtension.setActualNumberOfSuccessfulCalls(new BigDecimal(0));
		domainExtension.setActualNumberOfFailedCalls(new BigDecimal(0));
		domainExtension.setExpectedNumberOfCalls(
				BigDecimal.valueOf(meterConfig.getPayload().getMasterDataLinkageConfig().getUsagePoint().size()));
		domainExtension.setCallbackURL(endPointConfiguration.getUrl());
		ServiceCallBuilder serviceCallBuilder = serviceCallType.newServiceCall().origin("Core")
				.extendedWith(domainExtension);
		ServiceCall parentServiceCall = serviceCallBuilder.create();
		final List<UsagePoint> usagePoints = meterConfig.getPayload().getMasterDataLinkageConfig().getUsagePoint();
		final List<Meter> meters = meterConfig.getPayload().getMasterDataLinkageConfig().getMeter();
		final ConfigurationEvent configurationEvent = meterConfig.getPayload().getMasterDataLinkageConfig()
				.getConfigurationEvent();
		for (int i = 0; i < usagePoints.size(); i++) {
			createMasterDataLinkageChildServiceCall(parentServiceCall, operation, usagePoints.get(i), meters.get(i),
					configurationEvent);
		}
		return parentServiceCall;
	}

	private ServiceCall createMasterDataLinkageChildServiceCall(ServiceCall parentServiceCall, OperationEnum operation,
			UsagePoint usagePoint, Meter meter, ConfigurationEvent configurationEvent) {
		ServiceCallType serviceCallType = getServiceCallType(ServiceCallTypes.DATA_LINKAGE_CONFIG);
		MasterDataLinkageConfigDomainExtension domainExtension = new MasterDataLinkageConfigDomainExtension();
		domainExtension.setParentServiceCallId(BigDecimal.valueOf(parentServiceCall.getId()));
		domainExtension.setMeter(jsonService.serialize(meter));
		domainExtension.setUsagePoint(jsonService.serialize(usagePoint));
		domainExtension.setConfigurationEvent(jsonService.serialize(configurationEvent));
		domainExtension.setOperation(operation.getOperation());
		ServiceCallBuilder serviceCallBuilder = parentServiceCall.newChildCall(serviceCallType)
				.extendedWith(domainExtension);
		return serviceCallBuilder.create();
	}

	private ServiceCallType getServiceCallType(ServiceCallTypes serviceCallType) {
		return serviceCallService.findServiceCallType(serviceCallType.getTypeName(), serviceCallType.getTypeVersion())
				.orElseThrow(() -> new IllegalStateException(
						thesaurus.getFormat(MessageSeeds.COULD_NOT_FIND_SERVICE_CALL_TYPE)
								.format(serviceCallType.getTypeName(), serviceCallType.getTypeVersion())));
	}
}
