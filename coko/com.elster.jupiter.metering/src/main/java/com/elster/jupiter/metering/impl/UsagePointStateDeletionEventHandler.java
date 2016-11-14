package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;
import com.elster.jupiter.util.conditions.Order;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;

import static com.elster.jupiter.util.conditions.Where.where;


@Component(name = "UsagePointStateDeletionEventHandler",
        service = {TopicHandler.class},
        immediate = true)
public class UsagePointStateDeletionEventHandler implements TopicHandler {

    private Clock clock;
    private ServerMeteringService meteringService;
    private Thesaurus thesaurus;

    @SuppressWarnings("unused") // OSGI
    public UsagePointStateDeletionEventHandler() {
    }

    @Inject
    public UsagePointStateDeletionEventHandler(Clock clock, ServerMeteringService meteringService, NlsService nlsService) {
        setClock(clock);
        setMeteringService(meteringService);
        setNlsService(nlsService);
    }

    @Override
    public void handle(LocalEvent localEvent) {
        UsagePointState source = (UsagePointState) localEvent.getSource();
        if (!this.meteringService.getDataModel().query(UsagePointStateTemporalImpl.class)
                .select(where("state").isEqualTo(source).and(where("interval").isEffective(this.clock.instant())), Order.NOORDER, false, new String[0], 1, 2)
                .isEmpty()) {
            throw UsagePointLifeCycleDeleteObjectException.canNotDeleteActiveState(this.thesaurus);
        }
    }

    @Override
    public String getTopicMatcher() {
        return "com/elster/jupiter/usagepoint/lifecycle/state/BEFORE_DELETE";
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setMeteringService(ServerMeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(MeteringDataModelService.COMPONENT_NAME, Layer.DOMAIN);
    }
}