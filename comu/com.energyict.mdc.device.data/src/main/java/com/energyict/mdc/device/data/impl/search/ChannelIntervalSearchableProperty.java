package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ChannelIntervalSearchableProperty extends AbstractSearchableDeviceProperty {

    static final String PROPERTY_NAME = "device.channel.interval";
    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;
    private DeviceSearchDomain domain;
    private SearchablePropertyGroup group;

    @Inject
    public ChannelIntervalSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    ChannelIntervalSearchableProperty init(DeviceSearchDomain domain, SearchablePropertyGroup group) {
        this.domain = domain;
        this.group = group;
        return this;
    }

    @Override
    protected boolean valueCompatibleForDisplay(Object value) {
        return false; // TODO
    }

    @Override
    protected String toDisplayAfterValidation(Object value) {
        return null; // TODO
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
        builder.addChannelSpec();
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        if (!(condition instanceof Contains)) {
            throw new IllegalAccessError("Condition must be IN or NOT IN");
        }
        Contains contains = (Contains) condition;
        SqlBuilder sqlBuilder = new SqlBuilder();
        if (contains.getOperator() == ListOperator.NOT_IN) {
            sqlBuilder.append(" NOT ");
        }
        sqlBuilder.openBracket();
        sqlBuilder.append(contains.getCollection().stream()
                .map(Object.class::cast) // TODO: cast to your info type
                .map(interval -> {
                    StringBuilder builder = new StringBuilder();
                    builder.append("ch_spec.interval = ");
                    builder.append(0); // TODO: get interval value
                    builder.append(" AND ch_spec.intervalcode = ");
                    builder.append(0); // TODO: get interval code
                    return builder.toString();
                })
                .collect(Collectors.joining(" OR ")));
        sqlBuilder.closeBracket();
        return sqlBuilder;
    }

    @Override
    public SearchDomain getDomain() {
        return this.domain;
    }

    @Override
    public boolean affectsAvailableDomainProperties() {
        return false;
    }

    @Override
    public Optional<SearchablePropertyGroup> getGroup() {
        return Optional.of(this.group);
    }

    @Override
    public PropertySpec getSpecification() {
        return this.propertySpecService.longPropertySpec(
                PROPERTY_NAME,
                false,
                0L); // TODO replace by correct definition
    }

    @Override
    public Visibility getVisibility() {
        return Visibility.REMOVABLE;
    }

    @Override
    public SelectionMode getSelectionMode() {
        return SelectionMode.MULTI;
    }

    @Override
    public String getDisplayName() {
        return this.thesaurus.getFormat(PropertyTranslationKeys.CHANNEL_INTERVAL).format();
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.emptyList();
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        //nothing to refresh
    }
}
