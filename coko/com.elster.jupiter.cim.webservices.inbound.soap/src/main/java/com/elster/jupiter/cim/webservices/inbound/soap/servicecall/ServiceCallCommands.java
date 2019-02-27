package com.elster.jupiter.cim.webservices.inbound.soap.servicecall;

import com.elster.jupiter.cim.webservices.inbound.soap.DataLinkageConfigChecklist;
import com.elster.jupiter.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.elster.jupiter.cim.webservices.inbound.soap.masterdatalinkageconfig.MasterDataLinkageAction;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigCustomPropertySet;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigDomainExtension;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigMasterCustomPropertySet;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigMasterDomainExtension;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigMasterServiceCallHandler;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigServiceCallHandler;
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

import javax.inject.Inject;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ServiceCallCommands {

    public enum ServiceCallTypes {

        MASTER_DATA_LINKAGE_CONFIG(
                MasterDataLinkageConfigMasterServiceCallHandler.SERVICE_CALL_HANDLER_NAME,
                MasterDataLinkageConfigMasterServiceCallHandler.VERSION,
                MasterDataLinkageConfigMasterCustomPropertySet.class.getName()),
        DATA_LINKAGE_CONFIG(
                MasterDataLinkageConfigServiceCallHandler.SERVICE_CALL_HANDLER_NAME,
                MasterDataLinkageConfigServiceCallHandler.VERSION,
                MasterDataLinkageConfigCustomPropertySet.class.getName());

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
    public ServiceCall createMasterDataLinkageConfigMasterServiceCall(MasterDataLinkageConfigRequestMessageType config,
            Optional<EndPointConfiguration> endPointConfiguration, MasterDataLinkageAction action) {
        ServiceCallType serviceCallType = getServiceCallType(ServiceCallTypes.MASTER_DATA_LINKAGE_CONFIG);
        MasterDataLinkageConfigMasterDomainExtension domainExtension = new MasterDataLinkageConfigMasterDomainExtension();
        domainExtension.setActualNumberOfSuccessfulCalls(BigDecimal.ZERO);
        domainExtension.setActualNumberOfFailedCalls(BigDecimal.ZERO);
        domainExtension.setExpectedNumberOfCalls(
                BigDecimal.valueOf(config.getPayload().getMasterDataLinkageConfig().getUsagePoint().size()));
        if (endPointConfiguration.isPresent()) {
            domainExtension.setCallbackURL(endPointConfiguration.get().getUrl());
        }
        ServiceCallBuilder serviceCallBuilder = serviceCallType.newServiceCall()
                .origin(DataLinkageConfigChecklist.APPLICATION_NAME).extendedWith(domainExtension);
        ServiceCall parentServiceCall = serviceCallBuilder.create();
        final List<UsagePoint> usagePoints = config.getPayload().getMasterDataLinkageConfig().getUsagePoint();
        final List<Meter> meters = config.getPayload().getMasterDataLinkageConfig().getMeter();
        final ConfigurationEvent configurationEvent = config.getPayload().getMasterDataLinkageConfig()
                .getConfigurationEvent();
        for (int i = 0; i < usagePoints.size(); i++) {
            createMasterDataLinkageChildServiceCall(parentServiceCall, action, usagePoints.get(i), meters.get(i),
                    configurationEvent);
        }
        return parentServiceCall;
    }

    private ServiceCall createMasterDataLinkageChildServiceCall(ServiceCall parentServiceCall,
            MasterDataLinkageAction action, UsagePoint usagePoint, Meter meter, ConfigurationEvent configurationEvent) {
        ServiceCallType serviceCallType = getServiceCallType(ServiceCallTypes.DATA_LINKAGE_CONFIG);
        MasterDataLinkageConfigDomainExtension domainExtension = new MasterDataLinkageConfigDomainExtension();
        domainExtension.setParentServiceCallId(BigDecimal.valueOf(parentServiceCall.getId()));
        domainExtension.setMeter(jsonService.serialize(new MeterInfo(meter)));
        domainExtension.setUsagePoint(jsonService.serialize(new UsagePointInfo(usagePoint)));
        domainExtension.setConfigurationEvent(jsonService.serialize(new ConfigEventInfo(configurationEvent)));
        domainExtension.setOperation(action.name());
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
