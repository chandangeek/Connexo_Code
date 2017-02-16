/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.sql.SqlBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class LocationSearchableProperty extends AbstractSearchableUsagePointProperty {

    private final PropertySpecService propertySpecService;
    private UsagePointSearchDomain domain;
    private final Thesaurus thesaurus;
    private final Clock clock;
    private static final String FIELDNAME = "location";

    @Inject
    LocationSearchableProperty(UsagePointSearchDomain domain, PropertySpecService propertySpecService, Thesaurus thesaurus, Clock clock) {
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
                .stringSpec()
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

    private boolean isJSONArrayValid(String jsonData) {
        try {
            return new JSONObject(jsonData).get("values") instanceof JSONArray;
        } catch (JSONException ex) {
            return false;
        }
    }

    private JSONArray getJSONArrayData(String jsonData) throws JSONException {
        return new JSONObject(jsonData).getJSONArray("values");
    }

    @Override
    public Condition toCondition(Condition specification) {
        return ListOperator.IN.contains(() -> getBuilderFor(specification), "location");
    }

    private SqlBuilder getBuilderFor(Condition specification) {
        SqlBuilder builder = new SqlBuilder();

        String searchCondition = ((Comparison) specification).getValues()[0].toString();
        if (isJSONArrayValid(searchCondition)) {
            try {
                JSONArray arrayConditions = getJSONArrayData(searchCondition);
                List<String> whereClauses = new ArrayList<>();

                for (int i = 0; i < arrayConditions.length(); i++) {
                    JSONObject cond = arrayConditions.getJSONObject(i);
                    String propertyName = cond.get("propertyName").toString();
                    String propertyValue = cond.get("propertyValue").toString();

                    whereClauses.add("(UPPER" + propertyName + " like UPPER('" + propertyValue + "'))");
                }

                builder.append(" select LOCATIONID from mtr_locationmember where ");
                builder.append(whereClauses.stream().map(Object::toString).collect(Collectors.joining(" AND ")));

            } catch (JSONException ex) {
                // IsJSONArrayValid catches JSONException and returns false so not expecting any JSONException any longer
            }
        } else {
            builder.append(" select locOut.LOCATIONID from mtr_locationmember locOut right join ");
            builder.append(" (select LOCATIONID, UPPERCOUNTRYCODE, UPPERCOUNTRYNAME, UPPERADMINISTRATIVEAREA, UPPERLOCALITY, ");
            builder.append("        UPPERSUBLOCALITY, UPPERSTREETTYPE, UPPERSTREETNAME, UPPERSTREETNUMBER, ");
            builder.append("        UPPERESTABLISHMENTTYPE, UPPERESTABLISHMENTNAME, UPPERESTABLISHMENTNUMBER, ");
            builder.append("        UPPERADDRESSDETAIL, UPPERZIPCODE from mtr_locationmember where ");
            builder.add(toSqlFragment("LOCATIONID", specification, this.clock.instant()));
            builder.append("   ) locIn ");
            builder.append("    on ((locIn.UPPERCOUNTRYCODE = locOut.UPPERCOUNTRYCODE AND locIn.UPPERCOUNTRYCODE is not null)");
            builder.append("        OR (locOut.UPPERCOUNTRYCODE is null AND locIn.UPPERCOUNTRYCODE is null))");

            builder.append("    AND ((locIn.UPPERCOUNTRYNAME = locOut.UPPERCOUNTRYNAME AND locIn.UPPERCOUNTRYNAME is not null)");
            builder.append("        OR (locOut.UPPERCOUNTRYNAME is null AND locIn.UPPERCOUNTRYNAME is null))");

            builder.append("    AND ((locIn.UPPERADMINISTRATIVEAREA = locOut.UPPERADMINISTRATIVEAREA AND locIn.UPPERADMINISTRATIVEAREA is not null)");
            builder.append("        OR (locOut.UPPERADMINISTRATIVEAREA is null AND locIn.UPPERADMINISTRATIVEAREA is null))");

            builder.append("    AND ((locIn.UPPERLOCALITY = locOut.UPPERLOCALITY AND locIn.UPPERLOCALITY is not null)");
            builder.append("        OR (locOut.UPPERLOCALITY is null AND locIn.UPPERLOCALITY is null))");

            builder.append("    AND ((locIn.UPPERSUBLOCALITY = locOut.UPPERSUBLOCALITY AND locIn.UPPERSUBLOCALITY is not null)");
            builder.append("        OR (locOut.UPPERSUBLOCALITY is null AND locIn.UPPERSUBLOCALITY is null))");

            builder.append("    AND ((locIn.UPPERSTREETTYPE = locOut.UPPERSTREETTYPE AND locIn.UPPERSTREETTYPE is not null)");
            builder.append("        OR (locOut.UPPERSTREETTYPE is null AND locIn.UPPERSTREETTYPE is null))");

            builder.append("    AND ((locIn.UPPERSTREETNAME = locOut.UPPERSTREETNAME AND locIn.UPPERSTREETNAME is not null)");
            builder.append("        OR (locOut.UPPERSTREETNAME is null AND locIn.UPPERSTREETNAME is null))");

            builder.append("    AND ((locIn.UPPERSTREETNUMBER = locOut.UPPERSTREETNUMBER AND locIn.UPPERSTREETNUMBER is not null)");
            builder.append("        OR (locOut.UPPERSTREETNUMBER is null AND locIn.UPPERSTREETNUMBER is null))");

            builder.append("    AND ((locIn.UPPERESTABLISHMENTTYPE = locOut.UPPERESTABLISHMENTTYPE AND locIn.UPPERESTABLISHMENTTYPE is not null)");
            builder.append("        OR (locOut.UPPERESTABLISHMENTTYPE is null AND locIn.UPPERESTABLISHMENTTYPE is null))");

            builder.append("    AND ((locIn.UPPERESTABLISHMENTNAME = locOut.UPPERESTABLISHMENTNAME AND locIn.UPPERESTABLISHMENTNAME is not null)");
            builder.append("        OR (locOut.UPPERESTABLISHMENTNAME is null AND locIn.UPPERESTABLISHMENTNAME is null))");

            builder.append("    AND ((locIn.UPPERESTABLISHMENTNUMBER = locOut.UPPERESTABLISHMENTNUMBER AND locIn.UPPERESTABLISHMENTNUMBER is not null)");
            builder.append("        OR (locOut.UPPERESTABLISHMENTNUMBER is null AND locIn.UPPERESTABLISHMENTNUMBER is null))");

            builder.append("    AND ((locIn.UPPERADDRESSDETAIL = locOut.UPPERADDRESSDETAIL AND locIn.UPPERADDRESSDETAIL is not null)");
            builder.append("        OR (locOut.UPPERADDRESSDETAIL is null AND locIn.UPPERADDRESSDETAIL is null))");

            builder.append("    AND ((locIn.UPPERZIPCODE = locOut.UPPERZIPCODE AND locIn.UPPERZIPCODE is not null)");
            builder.append("        OR (locOut.UPPERZIPCODE is null AND locIn.UPPERZIPCODE is null))");
        }

        return builder;
    }
}
