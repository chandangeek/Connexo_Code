/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.dataquality.DataQualityKpiService;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EventType;
import com.elster.jupiter.metering.groups.GroupEventData;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.energyict.mdc.device.data.delete.queryenddevicegroup.eventhandler", service = TopicHandler.class, immediate = true)
public class EndDeviceGroupDeletionVetoEventHandler implements TopicHandler {

    private volatile DataCollectionKpiService dataCollectionKpiService;
    private volatile DataQualityKpiService dataQualityKpiService;
    private volatile Thesaurus thesaurus;

    public EndDeviceGroupDeletionVetoEventHandler() {
    }

    @Inject
    public EndDeviceGroupDeletionVetoEventHandler(DataCollectionKpiService dataCollectionKpiService, Thesaurus thesaurus, DataQualityKpiService dataQualityKpiService) {
        this();
        setDataCollectionKpiService(dataCollectionKpiService);
        setDataQualityKpiService(dataQualityKpiService);
        this.thesaurus = thesaurus;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setDataCollectionKpiService(DataCollectionKpiService dataCollectionKpiService) {
        this.dataCollectionKpiService = dataCollectionKpiService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setDataQualityKpiService(DataQualityKpiService dataQualityKpiService) {
        this.dataQualityKpiService = dataQualityKpiService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceDataServices.COMPONENT_NAME, Layer.SERVICE);
    }

    @Override
    public void handle(LocalEvent localEvent) {
        GroupEventData eventSource = (GroupEventData) localEvent.getSource();
        EndDeviceGroup endDeviceGroup = (EndDeviceGroup) eventSource.getGroup();
        boolean usedByDataCollectionKpi = dataCollectionKpiService.findDataCollectionKpi(endDeviceGroup).isPresent();
        boolean usedByDataQualityKpi = dataQualityKpiService.deviceDataQualityKpiFinder().forGroup(endDeviceGroup).find().size() > 0;
        if (usedByDataCollectionKpi && usedByDataQualityKpi) {
            throw new VetoDeleteDeviceGroupException(thesaurus, MessageSeeds.KPIS_DEVICEGROUP_DELETION, endDeviceGroup);
        }
        if (usedByDataCollectionKpi) {
            throw new VetoDeleteDeviceGroupException(thesaurus, MessageSeeds.VETO_DEVICEGROUP_DELETION, endDeviceGroup);
        }
        if (usedByDataQualityKpi) {
            throw new VetoDeleteDeviceGroupException(thesaurus, MessageSeeds.DATA_QUALITY_KPI_DEVICEGROUP_DELETION, endDeviceGroup);
        }
    }

    @Override
    public String getTopicMatcher() {
        return EventType.ENDDEVICEGROUP_VALIDATE_DELETED.topic();
    }
}
