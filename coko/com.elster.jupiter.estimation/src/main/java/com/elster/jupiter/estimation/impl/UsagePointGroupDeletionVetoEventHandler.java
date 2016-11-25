package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.metering.groups.EventType;
import com.elster.jupiter.metering.groups.GroupEventData;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.streams.Functions;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;

@Component(name = "com.elster.jupiter.export.usagepointgroup.deletionEventHandler", service = TopicHandler.class, immediate = true)
public class UsagePointGroupDeletionVetoEventHandler implements TopicHandler {

    private volatile IEstimationService estimationService;
    private volatile Thesaurus thesaurus;
    public final static String COMPONENT_NAME = "EST";

    @SuppressWarnings("unused") // for OSGI
    public UsagePointGroupDeletionVetoEventHandler() {
    }

    @Inject
    public UsagePointGroupDeletionVetoEventHandler(EstimationService estimationService, Thesaurus thesaurus) {
        setEstimationService(estimationService);
        this.thesaurus = thesaurus;
    }

    @Reference
    public void setEstimationService(EstimationService estimationService) {
        this.estimationService = (IEstimationService) estimationService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.SERVICE);
    }

    @Override
    public void handle(LocalEvent localEvent) {
        GroupEventData eventSource = (GroupEventData) localEvent.getSource();
        UsagePointGroup usagePointGroup = (UsagePointGroup) eventSource.getGroup();
        List<? extends EstimationTask> tasks = estimationService.findEstimationTasks(QualityCodeSystem.MDM);

        tasks.stream()
                .map(EstimationTask::getUsagePointGroup)
                .flatMap(Functions.asStream())
                .filter(usagePointGroup::equals)
                .findAny()
                .ifPresent(selector -> {
                    throw new VetoDeleteUsagePointGroupException(thesaurus, usagePointGroup);
                });
    }

    @Override
    public String getTopicMatcher() {
        return EventType.USAGEPOINTGROUP_VALIDATE_DELETED.topic();
    }
}