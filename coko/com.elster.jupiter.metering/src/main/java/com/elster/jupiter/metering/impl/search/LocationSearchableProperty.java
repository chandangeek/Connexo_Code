package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.metering.LocationMember;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.query.impl.WhereClauseBuilder;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.And;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.FragmentExpression;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.conditions.Visitor;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;

import javax.inject.Inject;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.*;


public class LocationSearchableProperty extends AbstractSearchableUsagePointProperty {

    private final PropertySpecService propertySpecService;
    private UsagePointSearchDomain domain;
    private final Thesaurus thesaurus;
    private final Clock clock;
    private static final String FIELDNAME = "location";

    @Inject
    public LocationSearchableProperty(UsagePointSearchDomain domain, PropertySpecService propertySpecService, Thesaurus thesaurus, Clock clock) {
        super(thesaurus);
        this.domain = domain;
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
        this.clock = clock;
    }

    LocationSearchableProperty init(UsagePointSearchDomain domain) {
        this.domain = domain;
        return this;
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
        return Optional.empty();
    }

    @Override
    public PropertySpec getSpecification() {
        return this.propertySpecService
                .longSpec()
                .named(FIELDNAME, PropertyTranslationKeys.USAGEPOINT_LOCATION)
                .fromThesaurus(this.thesaurus)
                .finish();
    }

    @Override
    public Visibility getVisibility() {
        return Visibility.STICKY;
    }

    @Override
    public SelectionMode getSelectionMode() {
        return SelectionMode.SINGLE;
    }

    @Override
    public String getDisplayName() {
        return PropertyTranslationKeys.USAGEPOINT_LOCATION.getDisplayName(this.thesaurus);
    }

    @Override
    protected boolean valueCompatibleForDisplay(Object value) {
        return value instanceof String;
    }

    @Override
    protected String toDisplayAfterValidation(Object value) {
        return String.valueOf(value);
    }

    @Override
    protected TranslationKey getNameTranslationKey() {
        return PropertyTranslationKeys.USAGEPOINT_LOCATION;
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.emptyList();
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        if (!constrictions.isEmpty()) {
            throw new IllegalArgumentException("No constraint to refresh");
        }
    }

    @Override
    public Condition toCondition(Condition specification) {

        Condition specialCases = ListOperator.IN.contains(new Subquery() {
            @Override
            public SqlFragment toFragment() {
                return getBuilderFor(specification);
            }
        }, new String[]{"location"});
        return specialCases;
    }

    private SqlBuilder getBuilderFor(Condition specification) {
        SqlBuilder builder = new SqlBuilder();

        builder.append(" select locOut.LOCATIONID from mtr_locationmember locOut right join ");
        builder.append(" (select LOCATIONID, UPPERCOUNTRYCODE, UPPERCOUNTRYNAME, UPPERADMINISTRATIVEAREA, UPPERLOCALITY, ");
        builder.append("        UPPERSUBLOCALITY, UPPERSTREETTYPE, UPPERSTREETNAME, UPPERSTREETNUMBER, ");
        builder.append("        UPPERESTABLISHMENTTYPE, UPPERESTABLISHMENTNAME, UPPERESTABLISHMENTNUMBER, ");
        builder.append("        UPPERADDRESSDETAIL, UPPERZIPCODE from mtr_locationmember where ");
        builder.add(toSqlFragment("LOCATIONID", specification, this.clock.instant()));
        builder.append("   ) locIn ");
        builder.append("    on locIn.UPPERCOUNTRYCODE = locOut.UPPERCOUNTRYCODE AND locIn.UPPERCOUNTRYNAME = locOut.UPPERCOUNTRYNAME AND ");
        builder.append("        locIn.UPPERADMINISTRATIVEAREA = locOut.UPPERADMINISTRATIVEAREA AND locIn.UPPERLOCALITY = locOut.UPPERLOCALITY AND ");
        builder.append("        locIn.UPPERSUBLOCALITY = locOut.UPPERSUBLOCALITY AND locIn.UPPERSTREETTYPE = locOut.UPPERSTREETTYPE AND ");
        builder.append("        locIn.UPPERSTREETNAME = locOut.UPPERSTREETNAME AND locIn.UPPERSTREETNUMBER = locOut.UPPERSTREETNUMBER AND ");
        builder.append("        locIn.UPPERESTABLISHMENTTYPE = locOut.UPPERESTABLISHMENTTYPE AND locIn.UPPERESTABLISHMENTNAME = locOut.UPPERESTABLISHMENTNAME AND ");
        builder.append("        locIn.UPPERESTABLISHMENTNUMBER = locOut.UPPERESTABLISHMENTNUMBER AND locIn.UPPERADDRESSDETAIL = locOut.UPPERADDRESSDETAIL AND ");
        builder.append("        locIn.UPPERZIPCODE = locOut.UPPERZIPCODE ");

        return builder;
    }
}
