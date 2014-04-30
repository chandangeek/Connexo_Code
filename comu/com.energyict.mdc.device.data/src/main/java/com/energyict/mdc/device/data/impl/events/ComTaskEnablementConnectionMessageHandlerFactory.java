package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * Implements the {@link MessageHandlerFactory} for the {@link ComTaskEnablementConnectionMessageHandler}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-30 (16:15)
 */
@Component(name = "com.energyict.mdc.device.data.update.comtaskenablement.connection.messagehandlerfactory", property = {"subscriber=CTECMH", "destination=" + EventService.JUPITER_EVENTS}, service = MessageHandlerFactory.class, immediate = true)
public class ComTaskEnablementConnectionMessageHandlerFactory extends ComTaskEnablementMessageHandlerFactory {

    private static final String SUBSCRIBER_NAME = "CTECMH";

    public ComTaskEnablementConnectionMessageHandlerFactory() {
        super(SUBSCRIBER_NAME);
    }

    @Override
    public MessageHandler newMessageHandler() {
        return new ComTaskEnablementConnectionMessageHandler(this.getJsonService(), this.getDeviceConfigurationService(), this.getDeviceDataService());
    }

}