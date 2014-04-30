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
import org.osgi.service.component.annotations.Reference;

/**
 * Provides reuse opportunities for {@link MessageHandlerFactory}
 * that relate to messages that are posted from {@link com.energyict.mdc.device.config.ComTaskEnablement}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-30 (17:05)
 */
public abstract class ComTaskEnablementMessageHandlerFactory implements MessageHandlerFactory {

    private final String subscriberName;

    private volatile JsonService jsonService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile ServerDeviceDataService deviceDataService;
    private volatile MessageService messageService;
    private volatile TransactionService transactionService;

    protected ComTaskEnablementMessageHandlerFactory(String subscriberName) {
        super();
        this.subscriberName = subscriberName;
    }

    protected JsonService getJsonService() {
        return jsonService;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    protected DeviceConfigurationService getDeviceConfigurationService() {
        return deviceConfigurationService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    protected ServerDeviceDataService getDeviceDataService() {
        return deviceDataService;
    }

    @Reference
    public void setDeviceDataService(DeviceDataService deviceDataService) {
        this.setDeviceDataService((ServerDeviceDataService) deviceDataService);
    }

    private void setDeviceDataService(ServerDeviceDataService deviceDataService) {
        this.deviceDataService = deviceDataService;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Activate
    public void activate() {
        try {
            Optional<SubscriberSpec> geocoder = this.messageService.getSubscriberSpec(EventService.JUPITER_EVENTS, this.subscriberName);
            if (!geocoder.isPresent()) {
                this.transactionService.execute(new VoidTransaction() {
                    @Override
                    protected void doPerform() {
                        createSubscription();
                    }
                });
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createSubscription() {
        Optional<DestinationSpec> destinationSpec = this.messageService.getDestinationSpec(EventService.JUPITER_EVENTS);
        if (destinationSpec.isPresent()) {
            destinationSpec.get().subscribe(this.subscriberName);
        }
        else {
            throw new IllegalStateException("JUPITER_EVENTS destination is missing");
        }
    }

}