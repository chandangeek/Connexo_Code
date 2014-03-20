package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pubsub.EventHandler;
import com.elster.jupiter.pubsub.Subscriber;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.pluggable.PluggableClass;
import java.util.List;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Handles delete events that are being sent when a PluggableClass
 * is being deleted and will check if a ComPortPool is using it.
 * If that is the case, the delete will be vetoed by throwing an exception.
 */
@Component(name="com.energyict.mdc.device.config.protocol.delete.eventhandler", service = Subscriber.class, immediate = true)
public class ComPortPoolPluggableClassDeletionEventHandler extends EventHandler<LocalEvent> {

    private static final String TOPIC = "com/energyict/mdc/pluggable/pluggableclass/DELETED";

    private volatile Thesaurus thesaurus;
    private volatile EngineModelService engineModelService;

    public ComPortPoolPluggableClassDeletionEventHandler() {
        super(LocalEvent.class);
    }

    @Override
    protected void onEvent(LocalEvent event, Object... objects) {
        if (event.getType().getTopic().equals(TOPIC)) {
            this.handleDeleteProtocolPluggableClass((PluggableClass) event.getSource());
        }
    }

    private void handleDeleteProtocolPluggableClass(PluggableClass pluggableClass) {
        List<InboundComPortPool> inboundComPortPools = engineModelService.findComPortPoolByDiscoveryProtocol(pluggableClass);
        if (!inboundComPortPools.isEmpty()) {
            throw new VetoDiscoveryProtocolPluggableClassDeletionBecauseStillUsedByComPortPoolException(this.thesaurus, pluggableClass, inboundComPortPools);
        }
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.setThesaurus(nlsService.getThesaurus(EngineModelService.COMPONENT_NAME, Layer.DOMAIN));
    }

    private void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Reference
    public void setEngineModelService(EngineModelService engineModelService) {
        this.engineModelService = engineModelService;
    }

}