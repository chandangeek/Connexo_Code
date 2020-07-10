/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.inbound.soap.servicecall;

import com.elster.jupiter.cim.webservices.inbound.soap.impl.DataLinkageConfigChecklist;
import com.elster.jupiter.cim.webservices.inbound.soap.impl.MessageSeeds;
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

import ch.iec.tc57._2011.usagepointconfigmessage.UsagePointConfigRequestMessageType;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class ServiceCallCommands {

    public enum ServiceCallTypes {

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