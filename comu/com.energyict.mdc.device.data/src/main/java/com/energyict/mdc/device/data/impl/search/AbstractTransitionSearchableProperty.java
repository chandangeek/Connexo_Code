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
import com.elster.jupiter.util.sql.SqlFragment;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class AbstractTransitionSearchableProperty<T> extends AbstractSearchableDeviceProperty {
    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;
    private final Class<T> implClass;

    private SearchDomain searchDomain;
    private SearchablePropertyGroup propertyGroup;

    public AbstractTransitionSearchableProperty(Class<T> clazz, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this.implClass = clazz;
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    public T init(SearchDomain searchDomain, SearchablePropertyGroup propertyGroup) {
        this.searchDomain = searchDomain;
        this.propertyGroup = propertyGroup;
        return this.implClass.cast(this);
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
        builder.addEndDevice();
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        return toSqlFragment(JoinClauseBuilder.Aliases.END_DEVICE + "." + getCIMDateColumnAlias(), condition, now);
    }

    protected abstract String getCIMDateColumnAlias();

    @Override
    public SearchDomain getDomain() {
        return this.searchDomain;
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
    public abstract String getName();

    @Override
    public PropertySpec getSpecification() {
        return this.propertySpecService.basicPropertySpec(
                getName(),
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
    public List<SearchableProperty> getConstraints() {
        return Collections.emptyList();
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        //nothing to refresh
    }

    protected Thesaurus getThesaurus() {
        return thesaurus;
    }
}
