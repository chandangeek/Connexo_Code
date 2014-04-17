package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pubsub.EventHandler;
import com.elster.jupiter.pubsub.Subscriber;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.masterdata.LoadProfileType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

/**
 * Handles delete events that are being sent when a {@link LoadProfileType}
 * is about to be deleted and will veto the delete when it is still used by:
 * <ul>
 * <li>a {@link DeviceType}</li>
 * <li>a {@link LoadProfileSpec}</li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-15 (17:02)
 */
@Component(name="com.energyict.mdc.device.config.loadprofiletype.delete.eventhandler", service = Subscriber.class, immediate = true)
public class LoadProfileTypeDeletionEventHandler extends EventHandler<LocalEvent> {

    private static final String TOPIC = "com/energyict/mdc/masterdata/loadprofiletype/VALIDATEDELETE";

    private volatile Thesaurus thesaurus;
    private volatile DeviceConfigurationService deviceConfigurationService;

    public LoadProfileTypeDeletionEventHandler() {
        super(LocalEvent.class);
    }

    public LoadProfileTypeDeletionEventHandler(DeviceConfigurationService deviceConfigurationService) {
        this();
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Override
    protected void onEvent(LocalEvent event, Object... objects) {
        if (event.getType().getTopic().equals(TOPIC)) {
            LoadProfileType loadProfileType = (LoadProfileType) event.getSource();
            this.validateDelete(loadProfileType);
        }
    }

    private void validateDelete(LoadProfileType loadProfileType) {
        List<DeviceType> deviceTypes = this.deviceConfigurationService.findDeviceTypesUsingLoadProfileType(loadProfileType);
        if (!deviceTypes.isEmpty()) {
            throw CannotDeleteBecauseStillInUseException.loadProfileTypeIsStillInUseByDeviceType(this.thesaurus, loadProfileType, deviceTypes);
        }
        List<LoadProfileSpec> loadProfileSpecs = this.deviceConfigurationService.findLoadProfileSpecsByLoadProfileType(loadProfileType);
        if (!loadProfileSpecs.isEmpty()) {
            throw CannotDeleteBecauseStillInUseException.loadProfileTypeIsStillInUseByLoadProfileSpec(this.thesaurus, loadProfileType, loadProfileSpecs);
        }
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.setThesaurus(nlsService.getThesaurus(DeviceConfigurationService.COMPONENTNAME, Layer.DOMAIN));
    }

    private void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

}