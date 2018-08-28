/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportDestination;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.WebServiceDestination;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;

import org.glassfish.hk2.api.ServiceLocator;

import javax.inject.Inject;
import java.util.Optional;

public class WebServiceDestinationInfoFactory implements DestinationInfoFactory {
    private final EndPointConfigurationService endPointConfigurationService;

    @Inject
    public WebServiceDestinationInfoFactory(EndPointConfigurationService endPointConfigurationService) {
        this.endPointConfigurationService = endPointConfigurationService;
    }

    @Override
    public void create(ServiceLocator serviceLocator, ExportTask task, DestinationInfo info) {
        task.addWebServiceDestination(getEndPointConfiguration(info.createEndPoint), getEndPointConfiguration(info.changeEndPoint));
    }

    @Override
    public DestinationInfo toInfo(ServiceLocator serviceLocator, DataExportDestination destination) {
        if (!(destination instanceof WebServiceDestination)) {
            throw new IllegalArgumentException();
        }
        WebServiceDestination webServiceDestination = (WebServiceDestination) destination;
        DestinationInfo destinationInfo = new DestinationInfo();
        destinationInfo.type = DestinationType.WEBSERVICE;
        destinationInfo.id = destination.getId();
        destinationInfo.createEndPoint = new IdWithNameInfo(webServiceDestination.getCreateWebServiceEndpoint());
        webServiceDestination.getChangeWebServiceEndpoint().map(IdWithNameInfo::new).ifPresent(changeEndPoint -> destinationInfo.changeEndPoint = changeEndPoint);
        return destinationInfo;
    }

    @Override
    public Class<? extends DataExportDestination> getDestinationClass(ServiceLocator serviceLocator) {
        return WebServiceDestination.class;
    }

    @Override
    public void update(ServiceLocator serviceLocator, DataExportDestination destination, DestinationInfo info) {
        if (!(destination instanceof WebServiceDestination)) {
            throw new IllegalArgumentException();
        }
        WebServiceDestination webServiceDestination = (WebServiceDestination) destination;
        webServiceDestination.setCreateWebServiceEndpoint(getEndPointConfiguration(info.createEndPoint));
        webServiceDestination.setChangeWebServiceEndpoint(getEndPointConfiguration(info.changeEndPoint));
        webServiceDestination.save();
    }

    private EndPointConfiguration getEndPointConfiguration(IdWithNameInfo idWithName) {
        return Optional.ofNullable(idWithName)
                .map(info -> ((Number) info.id).longValue())
                .flatMap(endPointConfigurationService::getEndPointConfiguration)
                .orElse(null);
    }
}
