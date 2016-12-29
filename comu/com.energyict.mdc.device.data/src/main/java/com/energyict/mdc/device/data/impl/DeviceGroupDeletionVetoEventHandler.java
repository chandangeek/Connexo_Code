package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EventType;
import com.elster.jupiter.metering.groups.Group;
import com.elster.jupiter.metering.groups.GroupEventData;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.impl.search.DeviceGroupSearchableProperty;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;

@Component(name = "com.energyict.mdc.device.data.devicegroup.deletionEventHandler", service = TopicHandler.class, immediate = true)
public class DeviceGroupDeletionVetoEventHandler implements TopicHandler {

    private volatile MeteringGroupsService meteringGroupsService;
    private volatile Thesaurus thesaurus;
    public final static String COMPONENT_NAME = "DDC";

    @SuppressWarnings("unused") // for OSGI
    public DeviceGroupDeletionVetoEventHandler() {
    }

    @Inject
    public DeviceGroupDeletionVetoEventHandler(MeteringGroupsService meteringGroupsService,
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
        EndDeviceGroup endDeviceGroup = (EndDeviceGroup) eventSource.getGroup();
        List<EndDeviceGroup> deviceGroups = this.meteringGroupsService.findEndDeviceGroups();

        deviceGroups.stream()
                .filter(Group::isDynamic)
                .map(QueryEndDeviceGroup.class::cast)
                .flatMap(deviceGroup -> deviceGroup.getSearchablePropertyValues().stream())
                .filter(searchablePropertyValue ->
                        DeviceGroupSearchableProperty.PROPERTY_NAME.equals(searchablePropertyValue.getProperty().getName())
                )
                .flatMap(searchablePropertyValue -> searchablePropertyValue.getValues().stream())
                .filter(endDeviceGroup::equals)
                .findAny()
                .ifPresent(o -> {
                    throw new VetoDeleteDeviceGroupException(this.thesaurus);
                });
    }

    @Override
    public String getTopicMatcher() {
        return EventType.ENDDEVICEGROUP_VALIDATE_DELETED.topic();
    }
}
