package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import javax.inject.Inject;
import org.osgi.service.component.annotations.Component;

/**
 * Copyrights EnergyICT
 * Date: 27/03/2014
 * Time: 10:37
 */
@Component(name = "com.energyict.mdc.device.data.delete.queryenddevicegroup.eventhandler", service = TopicHandler.class, immediate = true)
public class DeviceGroupDeletionEventHandler implements TopicHandler {

    private final DataCollectionKpiService dataCollectionKpiService;
    private final Thesaurus thesaurus;

    @Inject
    public DeviceGroupDeletionEventHandler(DataCollectionKpiService dataCollectionKpiService, Thesaurus thesaurus) {
        this.dataCollectionKpiService = dataCollectionKpiService;
        this.thesaurus = thesaurus;
    }

    @Override
    public void handle(LocalEvent localEvent) {
        EndDeviceGroup endDeviceGroup = (EndDeviceGroup) localEvent.getSource();
        if (endDeviceGroup instanceof QueryEndDeviceGroup) {
            if (dataCollectionKpiService.findDataCollectionKpi((QueryEndDeviceGroup) endDeviceGroup).isPresent()) {
                throw new VetoDeleteDeviceGroupException(thesaurus, endDeviceGroup);
            }
        }
    }

    @Override
    public String getTopicMatcher() {
        return EventType.ENDDEVICEGROUP_VALIDATE_DELETED.topic();
    }


}