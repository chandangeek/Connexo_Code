package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EventType;
import com.elster.jupiter.metering.groups.GroupEventData;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.validation.kpi.DataValidationKpiService;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

/**
 * Copyrights EnergyICT
 * Date: 27/03/2014
 * Time: 10:37
 */
@Component(name = "com.energyict.mdc.device.data.delete.queryenddevicegroup.eventhandler", service = TopicHandler.class, immediate = true)
public class EndDeviceGroupDeletionVetoEventHandler implements TopicHandler {

    private volatile DataCollectionKpiService dataCollectionKpiService;
    private volatile DataValidationKpiService dataValidationKpiService;
    private volatile Thesaurus thesaurus;

    public EndDeviceGroupDeletionVetoEventHandler() {
    }

    @Inject
    public EndDeviceGroupDeletionVetoEventHandler(DataCollectionKpiService dataCollectionKpiService, Thesaurus thesaurus, DataValidationKpiService dataValidationKpiService) {
        setDataCollectionKpiService(dataCollectionKpiService);
        setDataValidationKpiService(dataValidationKpiService);
        this.thesaurus = thesaurus;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setDataCollectionKpiService(DataCollectionKpiService dataCollectionKpiService) {
        this.dataCollectionKpiService = dataCollectionKpiService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setDataValidationKpiService(DataValidationKpiService dataValidationKpiService) {
        this.dataValidationKpiService = dataValidationKpiService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceDataServices.COMPONENT_NAME, Layer.SERVICE);
    }

    @Override
    public void handle(LocalEvent localEvent) {
        GroupEventData eventSource = (GroupEventData) localEvent.getSource();
        EndDeviceGroup endDeviceGroup = (EndDeviceGroup) eventSource.getGroup();
        if(dataCollectionKpiService.findDataCollectionKpi(endDeviceGroup).isPresent() && dataValidationKpiService.findDataValidationKpi(endDeviceGroup).isPresent()){
            throw new VetoDeleteDeviceGroupException(thesaurus, MessageSeeds.KPIS_DEVICEGROUP_DELETION, endDeviceGroup);
        }
        if (dataCollectionKpiService.findDataCollectionKpi(endDeviceGroup).isPresent()) {
            throw new VetoDeleteDeviceGroupException(thesaurus, MessageSeeds.VETO_DEVICEGROUP_DELETION, endDeviceGroup);
        }
        if(dataValidationKpiService.findDataValidationKpi(endDeviceGroup).isPresent()){
            throw new VetoDeleteDeviceGroupException(thesaurus, MessageSeeds.VAL_KPI_DEVICEGROUP_DELETION, endDeviceGroup);
        }
    }

    @Override
    public String getTopicMatcher() {
        return EventType.ENDDEVICEGROUP_VALIDATE_DELETED.topic();
    }

}
