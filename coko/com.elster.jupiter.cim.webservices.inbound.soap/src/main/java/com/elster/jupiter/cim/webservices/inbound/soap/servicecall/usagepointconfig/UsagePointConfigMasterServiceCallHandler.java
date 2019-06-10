/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.servicecall.usagepointconfig;

import com.elster.jupiter.cim.webservices.inbound.soap.impl.ObjectHolder;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.parent.AbstractMasterServiceCallHandler;
import com.elster.jupiter.cim.webservices.inbound.soap.usagepointconfig.Action;
import com.elster.jupiter.cim.webservices.inbound.soap.usagepointconfig.UsagePointBuilder;
import com.elster.jupiter.cim.webservices.outbound.soap.FailedUsagePointOperation;
import com.elster.jupiter.cim.webservices.outbound.soap.ReplyUsagePointConfigWebService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.util.json.JsonService;

import ch.iec.tc57._2011.usagepointconfig.Name;

import javax.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ServiceCallHandler} interface which handles the different steps for CIM WS UsagePointConfig
 */
public class UsagePointConfigMasterServiceCallHandler extends
        AbstractMasterServiceCallHandler<UsagePointConfigMasterDomainExtension, ReplyUsagePointConfigWebService> {
    public static final String SERVICE_CALL_HANDLER_NAME = "UsagePointConfigMasterServiceCallHandler";
    public static final String VERSION = "v1.0";
    public static final String APPLICATION = null;

    private final JsonService jsonService;
    private final MeteringService meteringService;

    @Inject
    public UsagePointConfigMasterServiceCallHandler(EndPointConfigurationService endPointConfigurationService,
            ObjectHolder<ReplyUsagePointConfigWebService> replyUsagePointConfigWebServiceHolder,
            JsonService jsonService, MeteringService meteringService, Thesaurus thesaurus,
            WebServicesService webServicesService) {
        super(UsagePointConfigMasterDomainExtension.class, replyUsagePointConfigWebServiceHolder,
                endPointConfigurationService, thesaurus, webServicesService);
        this.jsonService = jsonService;
        this.meteringService = meteringService;
    }

    @Override
    protected void sendReply(ReplyUsagePointConfigWebService replyWebService,
            EndPointConfiguration endPointConfiguration, ServiceCall serviceCall,
            UsagePointConfigMasterDomainExtension extension) {
        ServiceCall child = serviceCall.findChildren().stream().findFirst().get();
        UsagePointConfigDomainExtension extensionForChild = child.getExtension(UsagePointConfigDomainExtension.class)
                .get();
        replyWebService.call(endPointConfiguration, extensionForChild.getOperation(),
                getSuccessfulUsagePoints(serviceCall), getFailedUsagePoints(serviceCall),
                extension.getExpectedNumberOfCalls());
    }

    private List<FailedUsagePointOperation> getFailedUsagePoints(ServiceCall serviceCall) {
        return serviceCall.findChildren().stream().filter(child -> child.getState().equals(DefaultState.FAILED))
                .map(child -> {
                    UsagePointConfigDomainExtension extension = child
                            .getExtension(UsagePointConfigDomainExtension.class).get();
                    FailedUsagePointOperation failedUsagePointOperation = new FailedUsagePointOperation();
                    failedUsagePointOperation.setErrorCode(extension.getErrorCode());
                    failedUsagePointOperation.setErrorMessage(extension.getErrorMessage());
                    ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePoint = jsonService.deserialize(
                            extension.getUsagePoint(), ch.iec.tc57._2011.usagepointconfig.UsagePoint.class);
                    failedUsagePointOperation.setUsagePointMrid(usagePoint.getMRID());
                    failedUsagePointOperation.setUsagePointName(retrieveName(usagePoint));
                    return failedUsagePointOperation;
                }).collect(Collectors.toList());
    }

    private String retrieveName(ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePoint) {
        List<Name> names = usagePoint.getNames();
        switch (names.size()) {
        case 0:
            return null;
        case 1:
            return names.get(0).getName();
        default:
            return names.stream()
                    .filter(name -> UsagePointBuilder.USAGE_POINT_NAME.equals(name.getNameType().getName())).findFirst()
                    .map(Name::getName).orElse(null);
        }
    }

    private List<UsagePoint> getSuccessfulUsagePoints(ServiceCall serviceCall) {
        return serviceCall.findChildren().stream().filter(child -> child.getState().equals(DefaultState.SUCCESSFUL))
                .map(child -> {
                    UsagePointConfigDomainExtension extension = child
                            .getExtension(UsagePointConfigDomainExtension.class).get();
                    return findUsagePoint(extension);
                }).collect(Collectors.toList());
    }

    private UsagePoint findUsagePoint(UsagePointConfigDomainExtension extension) {
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo = jsonService
                .deserialize(extension.getUsagePoint(), ch.iec.tc57._2011.usagepointconfig.UsagePoint.class);
        Action action = Action.valueOf(extension.getOperation());
        // MRID is ignored for CREATE so it can have invalid value which references another existing usage point
        if (action != Action.CREATE && usagePointInfo.getMRID() != null) {
            return meteringService.findUsagePointByMRID(usagePointInfo.getMRID()).get(); // NOSONAR operation was successful so usagePoint should be found
        }
        return meteringService.findUsagePointByName(retrieveName(usagePointInfo)).get(); // NOSONAR operation was successful so usagePoint should be found
    }

}
