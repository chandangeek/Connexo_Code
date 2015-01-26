package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EndDeviceGroupEventData;
import com.elster.jupiter.metering.groups.EventType;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import javax.inject.Inject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Copyrights EnergyICT
 * Date: 27/03/2014
 * Time: 10:37
 */
@Component(name = "com.energyict.mdc.device.data.delete.queryenddevicegroup.eventhandler", service = TopicHandler.class, immediate = true)
public class EndDeviceGroupDeletionVetoEventHandler implements TopicHandler {

    private volatile DataCollectionKpiService dataCollectionKpiService;
    private volatile Thesaurus thesaurus;

    public EndDeviceGroupDeletionVetoEventHandler() {
    }

    @Inject
    public EndDeviceGroupDeletionVetoEventHandler(DataCollectionKpiService dataCollectionKpiService, Thesaurus thesaurus) {
        setDataCollectionKpiService(dataCollectionKpiService);
        this.thesaurus = thesaurus;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setDataCollectionKpiService(DataCollectionKpiService dataCollectionKpiService) {
        this.dataCollectionKpiService = dataCollectionKpiService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceDataServices.COMPONENT_NAME, Layer.SERVICE);
    }

    @Override
    public void handle(LocalEvent localEvent) {
        EndDeviceGroupEventData eventSource = (EndDeviceGroupEventData) localEvent.getSource();
        EndDeviceGroup endDeviceGroup = eventSource.getEndDeviceGroup();
        if (dataCollectionKpiService.findDataCollectionKpi(endDeviceGroup).isPresent()) {
            throw new VetoDeleteDeviceGroupException(thesaurus, endDeviceGroup);
        }
    }

    @Override
    public String getTopicMatcher() {
        return EventType.ENDDEVICEGROUP_VALIDATE_DELETED.topic();
    }

}