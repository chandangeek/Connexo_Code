package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.config.ConstantNode;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FunctionCallNode;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.NullNode;
import com.elster.jupiter.metering.config.OperationNode;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableNode;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeRequirementNode;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MetrologyContractImpl implements MetrologyContract {
    public enum Fields {
        METROLOGY_CONFIG("metrologyConfiguration"),
        METROLOGY_PURPOSE("metrologyPurpose"),
        MANDATORY("mandatory"),
        DELIVERABLES("deliverables"),;

        private String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private final ServerMetrologyConfigurationService metrologyConfigurationService;

    private long id;
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private final Reference<MetrologyConfiguration> metrologyConfiguration = ValueReference.absent();
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private final Reference<MetrologyPurpose> metrologyPurpose = ValueReference.absent();
    private boolean mandatory;
    private List<MetrologyContractReadingTypeDeliverableUsage> deliverables = new ArrayList<>();

    @Inject
    public MetrologyContractImpl(ServerMetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    public MetrologyContractImpl init(MetrologyConfiguration meterConfiguration, MetrologyPurpose metrologyPurpose) {
        this.metrologyConfiguration.set(meterConfiguration);
        this.metrologyPurpose.set(metrologyPurpose);
        return this;
    }

    private void touch() {
        this.metrologyConfigurationService.getDataModel().touch(this.getMetrologyConfiguration());
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public MetrologyConfiguration getMetrologyConfiguration() {
        return this.metrologyConfiguration.orNull();
    }

    @Override
    public MetrologyContract addDeliverable(ReadingTypeDeliverable deliverable) {
        MetrologyContractReadingTypeDeliverableUsage deliverableMapping = this.metrologyConfigurationService.getDataModel()
                .getInstance(MetrologyContractReadingTypeDeliverableUsage.class)
                .init(this, deliverable);
        Save.CREATE.validate(this.metrologyConfigurationService.getDataModel(), deliverableMapping);
        this.deliverables.add(deliverableMapping);
        touch();
        return this;
    }

    @Override
    public void removeDeliverable(ReadingTypeDeliverable deliverable) {
        Iterator<MetrologyContractReadingTypeDeliverableUsage> iterator = this.deliverables.iterator();
        while (iterator.hasNext()) {
            MetrologyContractReadingTypeDeliverableUsage usage = iterator.next();
            if (usage.getDeliverable().equals(deliverable)) {
                iterator.remove();
                this.touch();
                return;
            }
        }
    }

    @Override
    public List<ReadingTypeDeliverable> getDeliverables() {
        return this.deliverables.stream()
                .map(MetrologyContractReadingTypeDeliverableUsage::getDeliverable)
                .collect(Collectors.toList());
    }

    @Override
    public MetrologyPurpose getMetrologyPurpose() {
        return this.metrologyPurpose.orNull();
    }

    @Override
    public boolean isMandatory() {
        return this.mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    @Override
    public Status getStatus() {
        return new StatusImpl(this.metrologyConfigurationService.getThesaurus(), getMetrologyContractStatusKey());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MetrologyContractImpl that = (MetrologyContractImpl) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(this.id);
    }

    private static class StatusImpl implements Status {
        private final Thesaurus thesaurus;
        private final MetrologyContractStatusKey statusKey;

        public StatusImpl(Thesaurus thesaurus, MetrologyContractStatusKey statusKey) {
            this.thesaurus = thesaurus;
            this.statusKey = statusKey;
        }

        @Override
        public String getKey() {
            return this.statusKey.name();
        }

        @Override
        public String getName() {
            return this.thesaurus.getFormat(this.statusKey.getTranslation()).format();
        }
    }

    MetrologyContractStatusKey getMetrologyContractStatusKey() {
        if (this.metrologyConfiguration.isPresent() && this.metrologyConfiguration.get() instanceof UsagePointMetrologyConfiguration) {
            UsagePointMetrologyConfiguration configuration = (UsagePointMetrologyConfiguration) this.metrologyConfiguration.get();
            ReadingTypeRequirementChecker requirementChecker = new ReadingTypeRequirementChecker();
            getDeliverables()
                    .stream()
                    .map(ReadingTypeDeliverable::getFormula)
                    .map(Formula::getExpressionNode)
                    .forEach(expressionNode -> expressionNode.accept(requirementChecker));

            List<MeterRole> meterRoles = requirementChecker.getReadingTypeRequirements()
                    .stream()
                    .map(configuration::getMeterRoleFor)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

            boolean allMeterRolesHasMeters = true;
            for (MeterRole meterRole : meterRoles) {
                allMeterRolesHasMeters &= !configuration.getMetersForRole(meterRole).isEmpty();
            }
            return allMeterRolesHasMeters ? MetrologyContractStatusKey.COMPLETE : MetrologyContractStatusKey.INCOMPLETE;
        }
        return MetrologyContractStatusKey.UNKNOWN;
    }

    enum MetrologyContractStatusKey {
        COMPLETE(DefaultMetrologyPurpose.Translation.METROLOGY_CONTRACT_STATUS_COMPLETE),
        INCOMPLETE(DefaultMetrologyPurpose.Translation.METROLOGY_CONTRACT_STATUS_INCOMPLETE),
        UNKNOWN(DefaultMetrologyPurpose.Translation.METROLOGY_CONTRACT_STATUS_UNKNOWN);

        private final TranslationKey statusTranslation;

        MetrologyContractStatusKey(TranslationKey statusTranslation) {
            this.statusTranslation = statusTranslation;
        }

        TranslationKey getTranslation() {
            return this.statusTranslation;
        }
    }

    private static class ReadingTypeRequirementChecker implements ExpressionNode.Visitor<ReadingTypeRequirementChecker> {
        private List<ReadingTypeRequirement> readingTypeRequirements = new ArrayList<>();

        @Override
        public ReadingTypeRequirementChecker visitConstant(ConstantNode constant) {
            return this;
        }

        @Override
        public ReadingTypeRequirementChecker visitRequirement(ReadingTypeRequirementNode requirement) {
            this.readingTypeRequirements.add(requirement.getReadingTypeRequirement());
            return this;
        }

        @Override
        public ReadingTypeRequirementChecker visitDeliverable(ReadingTypeDeliverableNode deliverable) {
            return this;
        }

        @Override
        public ReadingTypeRequirementChecker visitOperation(OperationNode operationNode) {
            return this;
        }

        @Override
        public ReadingTypeRequirementChecker visitFunctionCall(FunctionCallNode functionCall) {
            return this;
        }

        @Override
        public ReadingTypeRequirementChecker visitNull(NullNode nullNode) {
            return this;
        }

        public List<ReadingTypeRequirement> getReadingTypeRequirements() {
            return this.readingTypeRequirements;
        }
    }
}
