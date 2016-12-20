package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.metering.ConnectionState;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.EnumFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;

import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Exposes the connectionState
 * of a {@link com.elster.jupiter.metering.UsagePointDetail}
 * as a {@link SearchableProperty}.
 *
 * @author Anton Fomchenko
 * @since 2015-08-10
 */
public class ConnectionStateSearchableProperty implements SearchableUsagePointProperty {

    private final SearchDomain domain;
    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;
    private Clock clock;
    private static final String FIELD_NAME = "connectionState.connectionState";

    public ConnectionStateSearchableProperty(SearchDomain domain, PropertySpecService propertySpecService, Thesaurus thesaurus, Clock clock) {
        super();
        this.domain = domain;
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
        this.clock = clock;
    }

    @Override
    public SearchDomain getDomain() {
        return domain;
    }

    @Override
    public boolean affectsAvailableDomainProperties() {
        return false;
    }

    @Override
    public Optional<SearchablePropertyGroup> getGroup() {
        return Optional.empty();
    }

    @Override
    public Visibility getVisibility() {
        return Visibility.STICKY;
    }

    @Override
    public SelectionMode getSelectionMode() {
        return SelectionMode.MULTI;
    }

    @Override
    public String getDisplayName() {
        return PropertyTranslationKeys.USAGEPOINT_CONNECTIONSTATE.getDisplayName(this.thesaurus);
    }

    @Override
    public String toDisplay(Object value) {
        if (value instanceof ConnectionState) {
            String string = ((ConnectionState) value).getName();
            return this.thesaurus.getFormat(new SimpleTranslationKey(string, string)).format();
        }
        throw new IllegalArgumentException("Value not compatible with domain");
    }

    @Override
    public PropertySpec getSpecification() {
        return this.propertySpecService
                .specForValuesOf(new EnumFactory(ConnectionState.class))
                .named(FIELD_NAME, PropertyTranslationKeys.USAGEPOINT_CONNECTIONSTATE)
                .fromThesaurus(this.thesaurus)
                .addValues(ConnectionState.CONNECTED, ConnectionState.PHYSICALLY_DISCONNECTED, ConnectionState.LOGICALLY_DISCONNECTED)
                .markExhaustive()
                .finish();
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.emptyList();
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        //nothing to refresh
    }

    @Override
    public Condition toCondition(Condition specification) {
        return specification.and(Where.where("connectionState.interval").isEffective(this.clock.instant()));
    }

}