package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.exceptions.CannotUpdateObisCodeWhenLogBookTypeIsInUseException;
import com.energyict.mdc.masterdata.LogBookType;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Checks;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

/**
 * Monitors update events of {@link LogBookType} and will:
 * <ul>
 * <li>veto the update of the obis code when the LogBookType is used by at least one LogBookSpec</li>
 * <li>forward the update to all {@link com.energyict.mdc.device.config.DeviceConfiguration}s</li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-14 (10:44)
 */
@Component(name="com.energyict.mdc.masterdata.update.logbooktype.eventhandler", service = TopicHandler.class, immediate = true)
public class LogBookTypeUpdateEventHandler implements TopicHandler {

    static final String TOPIC = "com/energyict/mdc/masterdata/logbooktype/UPDATED";
    static final String OLD_OBIS_CODE_PROPERTY_NAME = "oldObisCode";

    private volatile ServerDeviceConfigurationService deviceConfigurationService;
    private Thesaurus thesaurus;

    public LogBookTypeUpdateEventHandler() {
        super();
    }

    public LogBookTypeUpdateEventHandler(ServerDeviceConfigurationService deviceConfigurationService) {
        this();
        this.setDeviceConfigurationService(deviceConfigurationService);
    }

    @Override
    public String getTopicMatcher() {
        return TOPIC;
    }

    @Override
    public void handle(LocalEvent event) {
        LogBookType logBookType = (LogBookType) event.getSource();
        this.validateObisCodeChange(logBookType, event);
        this.validateUpdateLogBookTypeOnDeviceConfigurations(logBookType);
    }

    private void validateUpdateLogBookTypeOnDeviceConfigurations(LogBookType logBookType) {
        List<DeviceConfiguration> deviceConfigurations = this.deviceConfigurationService.findDeviceConfigurationsUsingLogBookType(logBookType);
        for (DeviceConfiguration deviceConfiguration : deviceConfigurations) {
            ((ServerDeviceConfiguration) deviceConfiguration).validateUpdateLogBookType(logBookType);
        }
    }

    private void validateObisCodeChange(LogBookType logBookType, LocalEvent localEvent) {
        String oldObisCode = (String) localEvent.toOsgiEvent().getProperty(OLD_OBIS_CODE_PROPERTY_NAME);
        if (!Checks.is(logBookType.getObisCode().toString()).equalTo(oldObisCode)) {
            List<DeviceConfiguration> deviceConfigurations = this.deviceConfigurationService.findDeviceConfigurationsUsingLogBookType(logBookType);
            if (!deviceConfigurations.isEmpty()) {
                throw new CannotUpdateObisCodeWhenLogBookTypeIsInUseException(this.getThesaurus(), logBookType, MessageSeeds.LOG_BOOK_TYPE_OBIS_CODE_CANNOT_BE_UPDATED);
            }
        }
    }

    private Thesaurus getThesaurus() {
        if (this.thesaurus == null) {
            return deviceConfigurationService.getThesaurus();
        }
        else {
            return this.thesaurus;
        }
    }

    @Reference
    public void setDeviceConfigurationService(ServerDeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

}