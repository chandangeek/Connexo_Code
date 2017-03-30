/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.elster.jupiter.metering.impl.config.MetrologyPurposeDeletionVetoEventHandler", service = TopicHandler.class, immediate = true)
public class MetrologyPurposeDeletionVetoEventHandler implements TopicHandler {

    private volatile Thesaurus thesaurus;
    private volatile MetrologyConfigurationServiceImpl metrologyConfigurationService;

    public MetrologyPurposeDeletionVetoEventHandler() {
    }

    @Inject
    public MetrologyPurposeDeletionVetoEventHandler(MetrologyConfigurationService metrologyConfigurationService, NlsService nlsService) {
        setMetrologyConfigurationService(metrologyConfigurationService);
        setNlsService(nlsService);
    }

    @Reference
    @SuppressWarnings("unused")
    public void setMetrologyConfigurationService(MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = (MetrologyConfigurationServiceImpl) metrologyConfigurationService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(MeteringService.COMPONENTNAME, Layer.SERVICE);
    }

    @Override
    public void handle(LocalEvent localEvent) {
        MetrologyPurpose metrologyPurpose = (MetrologyPurpose) localEvent.getSource();
        if (!this.metrologyConfigurationService.getDataModel().query(MetrologyContract.class)
                .select(where(MetrologyContractImpl.Fields.METROLOGY_PURPOSE.fieldName()).isEqualTo(metrologyPurpose))
                .isEmpty()) {
            throw new CannotDeleteMetrologyPurposeException(this.thesaurus, metrologyPurpose.getName());
        }
    }

    @Override
    public String getTopicMatcher() {
        return EventType.METROLOGY_PURPOSE_DELETED.topic();
    }
}
