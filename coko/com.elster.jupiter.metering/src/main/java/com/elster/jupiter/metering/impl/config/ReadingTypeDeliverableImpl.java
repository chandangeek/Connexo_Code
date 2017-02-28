/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.DeliverableType;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableFilter;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableNode;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.List;


@ValidDeliverable(groups = { Save.Create.class, Save.Update.class })
@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.OBJECT_MUST_HAVE_UNIQUE_NAME + "}")
public class ReadingTypeDeliverableImpl implements ReadingTypeDeliverable, HasUniqueName {

    public enum Fields {
        ID("id"),
        NAME("name"),
        METROLOGY_CONFIGURATION("metrologyConfiguration"),
        DELIVERABLE_TYPE("deliverableType"),
        READING_TYPE("readingType"),
        FORMULA("formula"),;

        private String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private final DataModel dataModel;
    private final EventService eventService;
    private final ServerMeteringService meteringService;
    private final ServerMetrologyConfigurationService metrologyConfigurationService;
    private final CustomPropertySetService customPropertySetService;

    // Managed by ORM
    @SuppressWarnings("unused")
    private long id;
    @NotEmpty(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, message = "{" + MessageSeeds.Constants.FIELD_TOO_LONG + "}")
    private String name;
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<ServerMetrologyConfiguration> metrologyConfiguration = ValueReference.absent();
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<ReadingType> readingType = ValueReference.absent();
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    @ValidExpression
    private Reference<ServerFormula> formula = ValueReference.absent();
    @NotNull(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private DeliverableType deliverableType;

    // Managed by ORM
    @SuppressWarnings("unused")
    private long version;
    // Managed by ORM
    @SuppressWarnings("unused")
    private Instant createTime;
    // Managed by ORM
    @SuppressWarnings("unused")
    private Instant modTime;
    // Managed by ORM
    @SuppressWarnings("unused")
    private String userName;

    @Inject
    public ReadingTypeDeliverableImpl(DataModel dataModel, EventService eventService, ServerMeteringService meteringService, ServerMetrologyConfigurationService metrologyConfigurationService, CustomPropertySetService customPropertySetService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.meteringService = meteringService;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.customPropertySetService = customPropertySetService;
    }

    public ReadingTypeDeliverableImpl init(ServerMetrologyConfiguration metrologyConfiguration, String name, DeliverableType deliverableType, ReadingType readingType, ServerFormula formula) {
        this.name = name;
        this.metrologyConfiguration.set(metrologyConfiguration);
        this.readingType.set(readingType);
        this.formula.set(formula);
        this.deliverableType = deliverableType;
        return this;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    private void setName(String name) {
        this.name = name;
    }

    @Override
    public MetrologyConfiguration getMetrologyConfiguration() {
        return this.metrologyConfiguration.orNull();
    }

    @Override
    public Formula getFormula() {
        return this.formula.orNull();
    }

    @Override
    public ReadingType getReadingType() {
        return this.readingType.orNull();
    }

    @Override
    public DeliverableType getType() {
        return this.deliverableType;
    }

    private void setReadingType(ReadingType readingType) {
        doSetReadingType(readingType);
        ReadingTypeDeliverable deliverableToUpdate =  this.getMetrologyConfiguration().getDeliverables().stream().filter(del -> del.equals(this)).findAny().orElse(null);
        // following code is necessary because to be able check for invalid formulas where this deliverable is used (check is done by the ValidDeliverable class)
        // we also need to set the new readingtype in the expression nodes (ReadingTypeDeliveryNodes) in the formulas that use the deliverable that is updated.
        // Otherwise they still contains the old readingtypes, because the nodes contains copies of the deliverables, no references (ORM framework)
        if (deliverableToUpdate != null) {
            //((ReadingTypeDeliverableImpl) deliverableToUpdate).doSetReadingType(readingType);
        }
        for (ReadingTypeDeliverable deliverable : this.getMetrologyConfiguration().getDeliverables()) {
            List<ReadingTypeDeliverableNode> deliverableNodes = deliverable.getFormula().getExpressionNode().accept(new DeliverableNodesFromExpressionNode());
            for (ReadingTypeDeliverableNode deliverableNode : deliverableNodes) {
                if (deliverableNode.getReadingTypeDeliverable().equals(this)) {
                    ((ReadingTypeDeliverableImpl) deliverableNode.getReadingTypeDeliverable()).doSetReadingType(readingType);
                }
            }
        }
    }

    private void doSetReadingType(ReadingType readingType) {
        this.readingType.set(readingType);
    }

    private void setFormula(String formula) {
        ServerExpressionNode node = this.getExpressionNodeParser().parse(formula);
        this.formula.get().updateExpression(node);
    }

    private ExpressionNodeParser getExpressionNodeParser() {
        return new ExpressionNodeParser(
            this.meteringService.getThesaurus(),
            this.metrologyConfigurationService,
            this.customPropertySetService,
            this.getMetrologyConfiguration(),
            Formula.Mode.AUTO);
    }

    @Override
    public Updater startUpdate() {
        return new UpdaterImpl();
    }

    private void completeUpdate() {
        Save.action(getId()).save(this.dataModel, this);
        this.metrologyConfiguration.get().deliverableUpdated(this);
        this.eventService.postEvent(EventType.READING_TYPE_DELIVERABLE_UPDATED.topic(), this);
    }

    void prepareDelete() {
        ServerFormula serverFormula = this.formula.get();
        formula.setNull();
        dataModel.update(this);
        serverFormula.delete();
    }

    @Override
    public boolean validateName() {
        ReadingTypeDeliverableFilter filter = new ReadingTypeDeliverableFilter().withMetrologyConfigurations(getMetrologyConfiguration());
        return !this.metrologyConfigurationService.findReadingTypeDeliverable(filter)
                .stream()
                .anyMatch(candidate -> candidate.getId() != getId() && candidate.getName().equals(getName()));
    }

    @Override
    public long getVersion() {
        return this.version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReadingTypeDeliverableImpl that = (ReadingTypeDeliverableImpl) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(getId());
    }

    @Override
    public int compareTo(ReadingTypeDeliverable other) {
        return Long.compare(this.id, other.getId());
    }

    private class UpdaterImpl implements Updater {
        @Override
        public Updater setName(String name) {
            ReadingTypeDeliverableImpl.this.setName(name);
            return this;
        }

        @Override
        public Updater setReadingType(ReadingType readingType) {
            ReadingTypeDeliverableImpl.this.setReadingType(readingType);
            return this;
        }

        @Override
        public Updater setFormula(String formula) {
            ReadingTypeDeliverableImpl.this.setFormula(formula);
            return this;
        }

        @Override
        public ReadingTypeDeliverable complete() {
            ReadingTypeDeliverableImpl.this.completeUpdate();
            return ReadingTypeDeliverableImpl.this;
        }
    }

}