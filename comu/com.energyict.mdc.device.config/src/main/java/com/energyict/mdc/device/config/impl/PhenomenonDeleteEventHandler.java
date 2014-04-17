package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pubsub.EventHandler;
import com.elster.jupiter.pubsub.Subscriber;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.exceptions.CannotDeleteBecauseStillInUseException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

/**
 * Handles delete events that are being sent when a {@link Phenomenon}
 * is about to be deleted and will veto the delete when it is still used by:
 * <ul>
 * <li>a {@link com.energyict.mdc.device.config.ChannelSpec}</li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-16 (13:33)
 */
@Component(name="com.energyict.mdc.device.config.phenomenon.delete.eventhandler", service = Subscriber.class, immediate = true)
public class PhenomenonDeleteEventHandler extends EventHandler<LocalEvent> {

    private static final String TOPIC = "com/energyict/mdc/masterdata/phenomenon/VALIDATEDELETE";

    private volatile Thesaurus thesaurus;
    private volatile DeviceConfigurationService deviceConfigurationService;

    public PhenomenonDeleteEventHandler() {
        super(LocalEvent.class);
    }

    @Override
    protected void onEvent(LocalEvent event, Object... objects) {
        if (event.getType().getTopic().equals(TOPIC)) {
            Phenomenon phenomenon = (Phenomenon) event.getSource();
            this.validateDelete(phenomenon);
        }
    }

    private void validateDelete(Phenomenon phenomenon) {
        if (this.isInUse(phenomenon)) {
            throw CannotDeleteBecauseStillInUseException.phenomenonIsStillInUse(this.thesaurus, phenomenon);
        }
    }

    private boolean isInUse(Phenomenon phenomenon) {
        return this.deviceConfigurationService.isPhenomenonInUse(phenomenon);
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