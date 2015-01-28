package com.elster.jupiter.export.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.ReadingTypeDataExportTask;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EndDeviceGroupEventData;
import com.elster.jupiter.metering.groups.EventType;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import javax.inject.Inject;
import java.util.List;


/**
 * Copyrights EnergyICT
 * Date: 27/03/2014
 * Time: 10:37
 */
@Component(name = "com.energyict.mdc.device.data.delete.queryenddevicegroup.eventhandler", service = TopicHandler.class, immediate = true)
public class EndDeviceGroupDeletionVetoEventHandler implements TopicHandler {

    private volatile DataExportService dataExportService;
    private volatile Thesaurus thesaurus;

    public EndDeviceGroupDeletionVetoEventHandler() {
    }

    @Inject
    public EndDeviceGroupDeletionVetoEventHandler(DataExportService dataExportService, Thesaurus thesaurus) {
        setDataCollectionKpiService(dataExportService);
        this.thesaurus = thesaurus;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setDataCollectionKpiService(DataExportService dataExportService) {
        this.dataExportService = dataExportService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus("DES", Layer.SERVICE);
    }

    @Override
    public void handle(LocalEvent localEvent) {
        EndDeviceGroupEventData eventSource = (EndDeviceGroupEventData) localEvent.getSource();
        EndDeviceGroup endDeviceGroup = eventSource.getEndDeviceGroup();
        List<? extends ReadingTypeDataExportTask> tasks = dataExportService.findReadingTypeDataExportTasks();
        for (ReadingTypeDataExportTask task : tasks) {
            if (task.getEndDeviceGroup().getId() == endDeviceGroup.getId()) {
                throw new VetoDeleteDeviceGroupException(thesaurus, endDeviceGroup);
            }
        }
    }

    @Override
    public String getTopicMatcher() {
        return EventType.ENDDEVICEGROUP_VALIDATE_DELETED.topic();
    }

}
