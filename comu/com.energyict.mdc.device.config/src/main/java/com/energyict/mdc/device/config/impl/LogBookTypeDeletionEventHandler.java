package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.exceptions.VetoLogBookTypeDeletionBecauseStillUsedByDeviceTypesException;
import com.energyict.mdc.masterdata.LogBookType;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Thesaurus;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

/**
 * Monitors delete events of {@link LogBookType} and will veto the delete
 * if the LogBookType is still in use by at least one {@link DeviceType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-14 (09:09)
 */
@Component(name="com.energyict.mdc.masterdata.delete.logbooktype.eventhandler", service = TopicHandler.class, immediate = true)
public class LogBookTypeDeletionEventHandler implements TopicHandler {

    static final String TOPIC = "com/energyict/mdc/masterdata/logbooktype/VALIDATEDELETE";

    private volatile DeviceConfigurationService deviceConfigurationService;

    LogBookTypeDeletionEventHandler(DeviceConfigurationService deviceConfigurationService) {
        super();
        this.setDeviceConfigurationService(deviceConfigurationService);
    }

    @Override
    public String getTopicMatcher() {
        return TOPIC;
    }

    @Override
    public void handle(LocalEvent event) {
        LogBookType logBookType = (LogBookType) event.getSource();
        List<DeviceType> deviceTypesUsingLogBookType = this.deviceConfigurationService.findDeviceTypesUsingLogBookType(logBookType);
        if (!deviceTypesUsingLogBookType.isEmpty()) {
            throw new VetoLogBookTypeDeletionBecauseStillUsedByDeviceTypesException(this.getThesaurus(), logBookType, deviceTypesUsingLogBookType);
        }
    }

    private Thesaurus getThesaurus() {
        return ((DeviceConfigurationServiceImpl) deviceConfigurationService).getThesaurus();
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

}