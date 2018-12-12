/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.export.WebServiceDestination;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EventType;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Listens to 'validate_delete' events of {@link EndPointConfiguration}
 * and will veto the deletion if it is still referenced by a {@link WebServiceDestination}
 */
@Component(name = EndPointConfigurationDeletionValidatorEventHandler.NAME, service = TopicHandler.class, immediate = true)
public class EndPointConfigurationDeletionValidatorEventHandler implements TopicHandler {
    static final String NAME = "com.elster.jupiter.export.impl.EndPointConfigurationDeletionValidatorEventHandler";
    private volatile IDataExportService exportService;

    // OSGi
    public EndPointConfigurationDeletionValidatorEventHandler() {
        super();
    }

    // For testing purposes only
    public EndPointConfigurationDeletionValidatorEventHandler(IDataExportService exportService) {
        this();
        setDataExportService(exportService);
    }

    @Override
    public void handle(LocalEvent localEvent) {
        EndPointConfiguration source = (EndPointConfiguration) localEvent.getSource();
        if (exportService.isUsedAsADestination(source)) {
            throw new VetoDeleteEndPointConfigurationException(exportService.getThesaurus(), source);
        }
    }

    @Override
    public String getTopicMatcher() {
        return EventType.ENDPOINT_CONFIGURATION_VALIDATE_DELETE.topic();
    }

    @Reference
    public void setDataExportService(IDataExportService exportService) {
        this.exportService = exportService;
    }
}
