package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.impl.ServerDeviceDataService;
import com.google.common.base.Optional;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Implements the {@link MessageHandlerFactory} for the {@link ComTaskEnablementStatusMessageHandler}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-30 (16:54)
 */
@Component(name = "com.energyict.mdc.device.data.update.comtaskenablement.status.messagehandlerfactory", property = {"subscriber=CTESMH", "destination=" + EventService.JUPITER_EVENTS}, service = MessageHandlerFactory.class, immediate = true)
public class ComTaskEnablementStatusMessageHandlerFactory extends ComTaskEnablementMessageHandlerFactory {

    private static final String SUBSCRIBER_NAME = "CTESMH";

    public ComTaskEnablementStatusMessageHandlerFactory() {
        super(SUBSCRIBER_NAME);
    }

    @Override
    public MessageHandler newMessageHandler() {
        return new ComTaskEnablementStatusMessageHandler(this.getJsonService(), this.getDeviceConfigurationService(), this.getDeviceDataService());
    }

}