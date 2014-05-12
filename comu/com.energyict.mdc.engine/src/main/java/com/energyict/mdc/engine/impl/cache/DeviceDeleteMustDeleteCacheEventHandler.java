package com.energyict.mdc.engine.impl.cache;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.pubsub.EventHandler;
import com.elster.jupiter.pubsub.Subscriber;
import org.osgi.service.component.annotations.Component;

/**
 * Copyrights EnergyICT
 * Date: 12/05/14
 * Time: 08:56
 */
@Component(name="com.energyict.mdc.device.data.comschedule.delete.eventhandler", service = Subscriber.class, immediate = true)
public class DeviceDeleteMustDeleteCacheEventHandler extends EventHandler<LocalEvent> {

    protected DeviceDeleteMustDeleteCacheEventHandler(Class<LocalEvent> eventType) {
        super(eventType);
    }

    @Override
    protected void onEvent(LocalEvent event, Object... eventDetails) {

    }
}
