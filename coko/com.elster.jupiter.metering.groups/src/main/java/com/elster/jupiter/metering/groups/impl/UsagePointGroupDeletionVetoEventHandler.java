package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.metering.groups.EventType;
import com.elster.jupiter.metering.groups.Group;
import com.elster.jupiter.metering.groups.GroupEventData;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryUsagePointGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.metering.groups.impl.search.UsagePointGroupSearchableProperty;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;

@Component(name = "com.elster.jupiter.metering.groups.usagepointgroup.deletionEventHandler", service = TopicHandler.class, immediate = true)
public class UsagePointGroupDeletionVetoEventHandler implements TopicHandler {

    private volatile MeteringGroupsService meteringGroupsService;
    private volatile Thesaurus thesaurus;
    public final static String COMPONENT_NAME = "MTG";

    @SuppressWarnings("unused") // for OSGI
    public UsagePointGroupDeletionVetoEventHandler() {
    }

    @Inject
    public UsagePointGroupDeletionVetoEventHandler(MeteringGroupsService meteringGroupsService,
                                                   Thesaurus thesaurus) {
        setMeteringGroupsService(meteringGroupsService);
        this.thesaurus = thesaurus;
    }

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.SERVICE);
    }

    @Override
    public void handle(LocalEvent localEvent) {
        GroupEventData eventSource = (GroupEventData) localEvent.getSource();
        UsagePointGroup usagePointGroup = (UsagePointGroup) eventSource.getGroup();
        List<UsagePointGroup> usagePointGroups = this.meteringGroupsService.findUsagePointGroups();

        usagePointGroups.stream()
                .filter(Group::isDynamic)
                .map(QueryUsagePointGroup.class::cast)
                .flatMap(deviceGroup -> deviceGroup.getSearchablePropertyValues().stream())
                .filter(searchablePropertyValue ->
                        UsagePointGroupSearchableProperty.PROPERTY_NAME.equals(searchablePropertyValue.getProperty().getName())
                )
                .flatMap(searchablePropertyValue -> searchablePropertyValue.getValues().stream())
                .filter(usagePointGroup::equals)
                .findAny()
                .ifPresent(o -> {
                    throw new VetoDeleteUsagePointGroupException(this.thesaurus);
                });
    }

    @Override
    public String getTopicMatcher() {
        return EventType.USAGEPOINTGROUP_VALIDATE_DELETED.topic();
    }
}
