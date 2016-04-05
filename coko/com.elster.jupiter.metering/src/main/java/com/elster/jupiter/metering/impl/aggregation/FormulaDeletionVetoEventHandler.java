package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.impl.config.MetrologyConfigurationServiceImpl;
import com.elster.jupiter.metering.impl.config.ReadingTypeDeliverableImpl;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.elster.jupiter.metering.impl.aggregation.FormulaDeletionVetoEventHandler", service = TopicHandler.class, immediate = true)
public class FormulaDeletionVetoEventHandler implements TopicHandler {

    private volatile Thesaurus thesaurus;
    private volatile MetrologyConfigurationServiceImpl metrologyConfigurationService;

    @SuppressWarnings("unused") //OSGI
    public FormulaDeletionVetoEventHandler() {
    }

    @Inject
    public FormulaDeletionVetoEventHandler(MetrologyConfigurationService metrologyConfigurationService, NlsService nlsService) {
        setMetrologyConfigurationService(metrologyConfigurationService);
        setNlsService(nlsService);
    }

    @Reference
    @SuppressWarnings("unused")
    public void setMetrologyConfigurationService(MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = (MetrologyConfigurationServiceImpl) metrologyConfigurationService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(MeteringService.COMPONENTNAME, Layer.SERVICE);
    }

    @Override
    public void handle(LocalEvent localEvent) {
        Formula formula = (Formula) localEvent.getSource();
        if (!this.metrologyConfigurationService.getDataModel().query(ReadingTypeDeliverable.class)
                .select(where(ReadingTypeDeliverableImpl.Fields.FORMULA.fieldName()).isEqualTo(formula))
                .isEmpty()) {
            throw new CannotDeleteFormulaException(this.thesaurus);
        }
    }

    @Override
    public String getTopicMatcher() {
        return EventType.FORMULA_DELETED.topic();
    }
}
