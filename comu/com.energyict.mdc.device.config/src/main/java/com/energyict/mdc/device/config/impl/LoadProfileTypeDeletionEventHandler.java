package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.masterdata.LoadProfileType;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
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
@Component(name="com.energyict.mdc.device.config.loadprofiletype.delete.eventhandler", service = TopicHandler.class, immediate = true)
public class LoadProfileTypeDeletionEventHandler implements TopicHandler {

    private static final String TOPIC = "com/energyict/mdc/masterdata/loadprofiletype/VALIDATEDELETE";

    private volatile Thesaurus thesaurus;
    private volatile DeviceConfigurationService deviceConfigurationService;

    public LoadProfileTypeDeletionEventHandler() {
        super();
    }

    public LoadProfileTypeDeletionEventHandler(DeviceConfigurationService deviceConfigurationService) {
        this();
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Override
    public String getTopicMatcher() {
        return TOPIC;
    }

    @Override
    public void handle(LocalEvent event) {
        LoadProfileType loadProfileType = (LoadProfileType) event.getSource();
        this.validateDelete(loadProfileType);
    }

    private void validateDelete(LoadProfileType loadProfileType) {
        List<DeviceType> deviceTypes = this.deviceConfigurationService.findDeviceTypesUsingLoadProfileType(loadProfileType);
        if (!deviceTypes.isEmpty()) {
            throw CannotDeleteBecauseStillInUseException.loadProfileTypeIsStillInUseByDeviceType(loadProfileType, deviceTypes, this.thesaurus, MessageSeeds.LOAD_PROFILE_TYPE_STILL_IN_USE_BY_DEVICE_TYPES);
        }
        List<LoadProfileSpec> loadProfileSpecs = this.deviceConfigurationService.findLoadProfileSpecsByLoadProfileType(loadProfileType);
        if (!loadProfileSpecs.isEmpty()) {
            throw CannotDeleteBecauseStillInUseException.loadProfileTypeIsStillInUseByLoadProfileSpec(loadProfileType, loadProfileSpecs, this.thesaurus, MessageSeeds.LOAD_PROFILE_TYPE_STILL_IN_USE_BY_LOAD_PROFILE_SPECS);
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