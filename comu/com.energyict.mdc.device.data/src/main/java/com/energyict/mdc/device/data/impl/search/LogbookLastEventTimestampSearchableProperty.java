package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.InstantFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class LogbookLastEventTimestampSearchableProperty extends AbstractSearchableDeviceProperty {

    static final String PROPERTY_NAME = "device.logbook.last.event";

    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;

    private DeviceSearchDomain domain;
    private SearchablePropertyGroup propertyGroup;

    @Inject
    public LogbookLastEventTimestampSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    LogbookLastEventTimestampSearchableProperty init(DeviceSearchDomain domain, SearchablePropertyGroup parent) {
        this.domain = domain;
        this.propertyGroup = parent;
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
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder builder = new SqlBuilder();
        builder.append(JoinClauseBuilder.Aliases.DEVICE + ".id IN (" +
                "select DEVICEID from DDC_LOGBOOK where ");
        builder.add(toSqlFragment("DDC_LOGBOOK.LASTLOGBOOK", condition, now));
        builder.closeBracket();
        return builder;
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
        return Optional.of(this.propertyGroup);
    }

    @Override
    public PropertySpec getSpecification() {
        return this.propertySpecService.basicPropertySpec(
                PROPERTY_NAME,
                false,
                new InstantFactory()
        );
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
        return this.thesaurus.getFormat(PropertyTranslationKeys.LOGBOOK_LAST_EVENT).format();
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
