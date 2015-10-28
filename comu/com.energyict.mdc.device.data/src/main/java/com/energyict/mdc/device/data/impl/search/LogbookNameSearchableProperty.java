package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.energyict.mdc.dynamic.PropertySpecService;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class LogbookNameSearchableProperty extends AbstractSearchableDeviceProperty {

    static final String PROPERTY_NAME = "device.logbook.name";
    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;
    private DeviceSearchDomain domain;
    private SearchablePropertyGroup group;

    @Inject
    public LogbookNameSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    LogbookNameSearchableProperty init(DeviceSearchDomain domain, SearchablePropertyGroup group) {
        this.domain = domain;
        this.group = group;
        return this;
    }

    @Override
    protected boolean valueCompatibleForDisplay(Object value) {
        return false;
    }

    @Override
    protected String toDisplayAfterValidation(Object value) {
        return null;
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
        builder.addLogbookType();
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.openBracket();
        sqlBuilder.add(this.toSqlFragment(JoinClauseBuilder.Aliases.LOGBOOK_TYPE + ".name", condition, now));
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
        return this.propertySpecService.basicPropertySpec(
                PROPERTY_NAME,
                false,
                new StringFactory());
    }

    @Override
    public Visibility getVisibility() {
        return Visibility.REMOVABLE;
    }

    @Override
    public SelectionMode getSelectionMode() {
        return SelectionMode.SINGLE;
    }

    @Override
    public String getDisplayName() {
        return this.thesaurus.getFormat(PropertyTranslationKeys.LOGBOOK_NAME).format();
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
