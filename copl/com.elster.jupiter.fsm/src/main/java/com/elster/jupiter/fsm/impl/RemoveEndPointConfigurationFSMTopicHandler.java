/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.fsm.EndPointConfigurationReference;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EventType;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.elster.jupiter.fsm.impl.RemoveEndPointConfigurationFSMTopicHandler", service = TopicHandler.class, immediate = true)
public class RemoveEndPointConfigurationFSMTopicHandler implements TopicHandler {
    private Thesaurus thesaurus;
    private OrmService ormService;

    public RemoveEndPointConfigurationFSMTopicHandler() {
        // for OSGi
    }

    @Inject
    public RemoveEndPointConfigurationFSMTopicHandler(OrmService ormService, NlsService nlsService) {
        this.setThesaurus(nlsService);
        this.setOrmService(ormService) ;
    }

    @Override
    public void handle(LocalEvent localEvent) {
        EndPointConfiguration endPointConfiguration = (EndPointConfiguration) localEvent.getSource();
        try(QueryStream<EndPointConfigurationReference> endPointStream = ormService.getDataModel(FiniteStateMachineService.COMPONENT_NAME)
                .orElseThrow(() -> new IllegalStateException(DataModel.class.getSimpleName() + " of " + FiniteStateMachineService.COMPONENT_NAME + " isn't found"))
                .stream(EndPointConfigurationReference.class)){
            boolean isUsedByLifeCycle = endPointStream
                .join(EndPointConfiguration.class)
                    .filter(where("endPointConfiguration.id").isEqualTo(endPointConfiguration.getId()))
                    .findAny()
                    .isPresent();
            if (isUsedByLifeCycle) {
                throw new VetoEndPointConfigurationDeleteException(this.thesaurus, endPointConfiguration);
            }
        }

    }

    @Reference
    public final void setThesaurus(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(FiniteStateMachineService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
    }

    @Override
    public String getTopicMatcher() {
        return EventType.ENDPOINT_CONFIGURATION_VALIDATE_DELETE.topic();
    }
}