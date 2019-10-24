/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.inbound.soap.servicecall;

import com.elster.jupiter.cim.webservices.inbound.soap.impl.DataLinkageConfigChecklist;
import com.elster.jupiter.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.elster.jupiter.cim.webservices.inbound.soap.masterdatalinkageconfig.MasterDataLinkageAction;
import com.elster.jupiter.cim.webservices.inbound.soap.masterdatalinkageconfig.MasterDataLinkageFaultMessageFactory;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigCustomPropertySet;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigDomainExtension;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigMasterCustomPropertySet;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigMasterDomainExtension;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigMasterServiceCallHandler;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigServiceCallHandler;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.bean.ConfigEventInfo;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.bean.MeterInfo;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.bean.UsagePointInfo;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.usagepointconfig.UsagePointConfigCustomPropertySet;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.usagepointconfig.UsagePointConfigDomainExtension;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.usagepointconfig.UsagePointConfigMasterCustomPropertySet;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.usagepointconfig.UsagePointConfigMasterDomainExtension;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.usagepointconfig.UsagePointConfigMasterServiceCallHandler;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.usagepointconfig.UsagePointConfigServiceCallHandler;
import com.elster.jupiter.cim.webservices.inbound.soap.usagepointconfig.Action;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.TransactionRequired;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallBuilder;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.util.json.JsonService;

import ch.iec.tc57._2011.executemasterdatalinkageconfig.FaultMessage;
import ch.iec.tc57._2011.masterdatalinkageconfig.ConfigurationEvent;
import ch.iec.tc57._2011.masterdatalinkageconfig.UsagePoint;
import ch.iec.tc57._2011.masterdatalinkageconfigmessage.MasterDataLinkageConfigRequestMessageType;
import ch.iec.tc57._2011.usagepointconfigmessage.UsagePointConfigRequestMessageType;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class ServiceCallCommands {

    public enum ServiceCallTypes {

        MASTER_DATA_LINKAGE_CONFIG(
                MasterDataLinkageConfigMasterServiceCallHandler.SERVICE_CALL_HANDLER_NAME,
                MasterDataLinkageConfigMasterServiceCallHandler.VERSION,
                MasterDataLinkageConfigMasterServiceCallHandler.APPLICATION,
                MasterDataLinkageConfigMasterCustomPropertySet.class.getName()),
        DATA_LINKAGE_CONFIG(
                MasterDataLinkageConfigServiceCallHandler.SERVICE_CALL_HANDLER_NAME,
                MasterDataLinkageConfigServiceCallHandler.VERSION,
                MasterDataLinkageConfigServiceCallHandler.APPLICATION,
                MasterDataLinkageConfigCustomPropertySet.class.getName()),
        MASTER_USAGE_POINT_CONFIG(
                UsagePointConfigMasterServiceCallHandler.SERVICE_CALL_HANDLER_NAME,
                UsagePointConfigMasterServiceCallHandler.VERSION,
                UsagePointConfigMasterServiceCallHandler.APPLICATION,
                UsagePointConfigMasterCustomPropertySet.class.getName()),
        USAGE_POINT_CONFIG(
                UsagePointConfigServiceCallHandler.SERVICE_CALL_HANDLER_NAME,
                UsagePointConfigServiceCallHandler.VERSION,
                UsagePointConfigServiceCallHandler.APPLICATION,
                UsagePointConfigCustomPropertySet.class.getName());

        private final String typeName;
        private final String typeVersion;
        private final String reservedByApplication;
        private final String customPropertySetClass;

        ServiceCallTypes(String typeName, String typeVersion, String reservedByApplication, String customPropertySetClass) {
            this.typeName = typeName;
            this.typeVersion = typeVersion;
            this.reservedByApplication = reservedByApplication;
            this.customPropertySetClass = customPropertySetClass;
        }

        public String getTypeName() {
            return typeName;
        }

        public String getTypeVersion() {
            return typeVersion;
        }

        public Optional<String> getApplication() {
            return Optional.ofNullable(reservedByApplication);
        }

        public String getCustomPropertySetClass() {
            return customPropertySetClass;
        }
    }

    private final ServiceCallService serviceCallService;
    private final JsonService jsonService;
    private final Thesaurus thesaurus;
    private final MessageService messageService;

    @Inject
    public ServiceCallCommands(ServiceCallService serviceCallService, JsonService jsonService, Thesaurus thesaurus,
                               MessageService messageService) {
        this.serviceCallService = serviceCallService;
        this.jsonService = jsonService;
        this.thesaurus = thesaurus;
        this.messageService = messageService;
    }

    @TransactionRequired
    public void requestTransition(ServiceCall serviceCall, DefaultState newState) {
        serviceCall.requestTransition(newState);
    }

    @TransactionRequired
    public ServiceCall createMasterDataLinkageConfigMasterServiceCall(MasterDataLinkageConfigRequestMessageType config,
            Optional<EndPointConfiguration> endPointConfiguration, MasterDataLinkageAction action,
            MasterDataLinkageFaultMessageFactory faultMessageFactory) throws FaultMessage {
        ServiceCallType serviceCallType = getServiceCallType(ServiceCallTypes.MASTER_DATA_LINKAGE_CONFIG);
        MasterDataLinkageConfigMasterDomainExtension domainExtension = new MasterDataLinkageConfigMasterDomainExtension();
        domainExtension.setActualNumberOfSuccessfulCalls(BigDecimal.ZERO);
        domainExtension.setActualNumberOfFailedCalls(BigDecimal.ZERO);
        domainExtension.setExpectedNumberOfCalls(
                BigDecimal.valueOf(config.getPayload().getMasterDataLinkageConfig().getUsagePoint().size()));
        String correlationId = config.getHeader() == null ? null : config.getHeader().getCorrelationID();
        domainExtension.setCorrelationId(correlationId);

        if (endPointConfiguration.isPresent()) {
            domainExtension.setCallbackURL(endPointConfiguration.get().getUrl());
        }
        ServiceCallBuilder serviceCallBuilder = serviceCallType.newServiceCall()
                .origin(DataLinkageConfigChecklist.APPLICATION_NAME).extendedWith(domainExtension);
        ServiceCall parentServiceCall = serviceCallBuilder.create();
        final List<UsagePoint> usagePoints = config.getPayload().getMasterDataLinkageConfig().getUsagePoint();
        final List<ch.iec.tc57._2011.masterdatalinkageconfig.Meter> meters = config.getPayload()
                .getMasterDataLinkageConfig().getMeter();
        if (usagePoints.size() != meters.size()) {
            throw faultMessageFactory.createMasterDataLinkageFaultMessage(action,
                    MessageSeeds.DIFFERENT_NUMBER_OF_METERS_AND_USAGE_POINTS, meters.size(), usagePoints.size());
        }
        final ConfigurationEvent configurationEvent = config.getPayload().getMasterDataLinkageConfig()
                .getConfigurationEvent();
        for (int i = 0; i < usagePoints.size(); i++) {
            createMasterDataLinkageChildServiceCall(parentServiceCall, action, usagePoints.get(i), meters.get(i),
                    configurationEvent);
        }
        return parentServiceCall;
    }

    private ServiceCall createMasterDataLinkageChildServiceCall(ServiceCall parentServiceCall,
            MasterDataLinkageAction action, UsagePoint usagePoint,
            ch.iec.tc57._2011.masterdatalinkageconfig.Meter meter, ConfigurationEvent configurationEvent) {
        ServiceCallType serviceCallType = getServiceCallType(ServiceCallTypes.DATA_LINKAGE_CONFIG);
        /* AS EXAMPLE !!!!!!!!!!!!!! */
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

    @TransactionRequired
    public ServiceCall createUsagePointConfigMasterServiceCall(UsagePointConfigRequestMessageType config,
            Optional<EndPointConfiguration> endPointConfiguration, Action action) {
        ServiceCallType serviceCallType = getServiceCallType(ServiceCallTypes.MASTER_USAGE_POINT_CONFIG);
        UsagePointConfigMasterDomainExtension domainExtension = new UsagePointConfigMasterDomainExtension();
        domainExtension.setActualNumberOfSuccessfulCalls(BigDecimal.ZERO);
        domainExtension.setActualNumberOfFailedCalls(BigDecimal.ZERO);
        domainExtension.setExpectedNumberOfCalls(
                BigDecimal.valueOf(config.getPayload().getUsagePointConfig().getUsagePoint().size()));
        String correlationId = config.getHeader() == null ? null : config.getHeader().getCorrelationID();
        domainExtension.setCorrelationId(correlationId);

        if (endPointConfiguration.isPresent()) {
            domainExtension.setCallbackURL(endPointConfiguration.get().getUrl());
        }
        Instant requestTimestamp = config.getHeader().getTimestamp();
        ServiceCallBuilder serviceCallBuilder = serviceCallType.newServiceCall()
                .origin(DataLinkageConfigChecklist.APPLICATION_NAME).extendedWith(domainExtension);
        ServiceCall parentServiceCall = serviceCallBuilder.create();
        final List<ch.iec.tc57._2011.usagepointconfig.UsagePoint> usagePoints = config.getPayload()
                .getUsagePointConfig().getUsagePoint();
        for (int i = 0; i < usagePoints.size(); i++) {
            createUsagePointConfigChildServiceCall(parentServiceCall, action, usagePoints.get(i), requestTimestamp);
        }
        return parentServiceCall;
    }

    private ServiceCall createUsagePointConfigChildServiceCall(ServiceCall parentServiceCall, Action action,
            ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePoint, Instant requestTimestamp) {
        ServiceCallType serviceCallType = getServiceCallType(ServiceCallTypes.USAGE_POINT_CONFIG);
        UsagePointConfigDomainExtension domainExtension = new UsagePointConfigDomainExtension();
        domainExtension.setParentServiceCallId(BigDecimal.valueOf(parentServiceCall.getId()));
        domainExtension.setUsagePoint(jsonService.serialize(usagePoint));
        domainExtension.setOperation(action.name());
        domainExtension.setRequestTimestamp(requestTimestamp);
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