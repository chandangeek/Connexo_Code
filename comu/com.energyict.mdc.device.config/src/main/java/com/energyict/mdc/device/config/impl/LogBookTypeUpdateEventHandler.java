package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pubsub.EventHandler;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.exceptions.CannotUpdateObisCodeWhenLogBookTypeIsInUseException;
import com.energyict.mdc.masterdata.LogBookType;
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
@Component(name="com.energyict.mdc.masterdata.update.logbooktype.eventhandler", service = Subscriber.class, immediate = true)
public class LogBookTypeUpdateEventHandler extends EventHandler<LocalEvent> {

    static final String TOPIC = "com/energyict/mdc/masterdata/logbooktype/UPDATED";
    static final String OLD_OBIS_CODE_PROPERTY_NAME = "oldObisCode";

    private volatile DeviceConfigurationService deviceConfigurationService;
    private Thesaurus thesaurus;

    public LogBookTypeUpdateEventHandler() {
        super(LocalEvent.class);
    }

    public LogBookTypeUpdateEventHandler(DeviceConfigurationService deviceConfigurationService) {
        this();
        this.setDeviceConfigurationService(deviceConfigurationService);
    }

    @Override
    protected void onEvent(LocalEvent event, Object... eventDetails) {
        if (event.getType().getTopic().equals(TOPIC)) {
            this.handle(event);
        }
    }

    private void handle(LocalEvent localEvent) {
        LogBookType logBookType = (LogBookType) localEvent.getSource();
        this.validateObisCodeChange(logBookType, localEvent);
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
                throw new CannotUpdateObisCodeWhenLogBookTypeIsInUseException(this.getThesaurus(), logBookType);
            }
        }
    }

    private Thesaurus getThesaurus() {
        if (this.thesaurus == null) {
            return ((DeviceConfigurationServiceImpl) deviceConfigurationService).getThesaurus();
        }
        else {
            return this.thesaurus;
        }
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

}