package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.config.UsagePointRequirement;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyOperator;
import com.elster.jupiter.search.SearchablePropertyValue;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UsagePointRequirementImpl implements UsagePointRequirement {

    public enum Fields {
        METROLOGY_CONFIGURATION("metrologyConfiguration"),
        SEARCHABLE_PROPERTY("searchableProperty"),
        OPERATOR("operator"),
        CONDITION_VALUES("conditionValues"),;

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private final ServerMetrologyConfigurationService metrologyConfigurationService;

    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<UsagePointMetrologyConfiguration> metrologyConfiguration = ValueReference.absent();
    @NotNull(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    @Size(min = 1, max = Table.SHORT_DESCRIPTION_LENGTH)
    private String searchableProperty;
    @NotNull
    private SearchablePropertyOperator operator;
    private List<UsagePointRequirementValue> conditionValues = new ArrayList<>();

    private SearchableProperty property;

    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    @Inject
    public UsagePointRequirementImpl(ServerMetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    public UsagePointRequirementImpl init(UsagePointMetrologyConfiguration metrologyConfiguration, SearchablePropertyValue.ValueBean valueBean) {
        this.metrologyConfiguration.set(metrologyConfiguration);
        this.searchableProperty = valueBean.getPropertyName();
        this.operator = valueBean.getOperator();
        this.initConditionValues(valueBean.getValues());
        return this;
    }

    private void initConditionValues(List<String> conditionValues) {
        this.conditionValues.clear();
        conditionValues.stream()
                .map(v -> metrologyConfigurationService.getDataModel().getInstance(UsagePointRequirementValue.class).init(this, v))
                .forEach(v -> {
                    Save.CREATE.validate(metrologyConfigurationService.getDataModel(), v);
                    this.conditionValues.add(v);
                });
    }

    @Override
    public SearchableProperty getSearchableProperty() {
        if (this.property == null) {
            if (!this.metrologyConfiguration.isPresent()) {
                throw new IllegalStateException("You are trying to use uninitialized instance of UsagePointRequirement");
            }
            this.property = ((UsagePointMetrologyConfigurationImpl) this.metrologyConfiguration.get()).getUsagePointRequirementSearchableProperties()
                    .stream()
                    .map(SearchablePropertyValue::getProperty)
                    .filter(property -> property.getName().equals(this.searchableProperty))
                    .findAny()
                    .orElseThrow(() -> BadUsagePointRequirementException.underlyingSearchablePropertyNotFound(this.metrologyConfigurationService.getThesaurus(), this.searchableProperty));
        }
        return this.property;
    }

    @Override
    public SearchablePropertyValue.ValueBean toValueBean() {
        return new SearchablePropertyValue.ValueBean(this.searchableProperty, this.operator,this.conditionValues.stream().map(UsagePointRequirementValue::getValue).collect(Collectors.toList()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UsagePointRequirementImpl that = (UsagePointRequirementImpl) o;
        return metrologyConfiguration.equals(that.metrologyConfiguration)
                && (searchableProperty != null ? searchableProperty.equals(that.searchableProperty) : that.searchableProperty == null);
    }

    @Override
    public int hashCode() {
        int result = metrologyConfiguration.hashCode();
        result = 31 * result + (searchableProperty != null ? searchableProperty.hashCode() : 0);
        return result;
    }

    void prepareDelete() {
        conditionValues.clear();
    }
}
