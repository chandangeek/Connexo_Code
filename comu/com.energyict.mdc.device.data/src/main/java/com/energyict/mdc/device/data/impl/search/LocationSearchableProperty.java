package com.energyict.mdc.device.data.impl.search;

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
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.energyict.mdc.device.data.DeviceFields;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class LocationSearchableProperty extends AbstractSearchableDeviceProperty {

    private final PropertySpecService propertySpecService;
    private DeviceSearchDomain domain;

    @Inject
    public LocationSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(thesaurus);
        this.propertySpecService = propertySpecService;
    }

    LocationSearchableProperty init(DeviceSearchDomain domain) {
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
                .named(DeviceFields.LOCATION.fieldName(), this.getNameTranslationKey())
                .fromThesaurus(this.getThesaurus())
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
    protected TranslationKey getNameTranslationKey() {
        return PropertyTranslationKeys.DEVICE_LOCATION;
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
    public void appendJoinClauses(JoinClauseBuilder builder) {
        builder.addEndDevice();
    }

    private boolean isJSONArrayValid(String jsonData) {
        try {
            new JSONArray(jsonData);
        } catch (JSONException ex) {
            return false;
        }
        return true;
    }

    private JSONArray getJSONArrayData(String jsonData) throws JSONException {
        return new JSONArray(jsonData);
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder builder = new SqlBuilder();

        String searchCondition = ((Comparison) condition).getValues()[0].toString();
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
                builder.append(JoinClauseBuilder.Aliases.END_DEVICE + ".LOCATIONID IN (");
                builder.append(" select LOCATIONID from mtr_locationmember where ");
                builder.append(whereClauses.stream().map(Object::toString).collect(Collectors.joining(" AND ")));
                builder.append(" ) ");

            } catch (JSONException ex) {
            }
        } else {
            builder.append(JoinClauseBuilder.Aliases.END_DEVICE + ".LOCATIONID IN (");
            builder.append(" select locOut.LOCATIONID from mtr_locationmember locOut right join ");
            builder.append(" (select LOCATIONID, UPPERCOUNTRYCODE, UPPERCOUNTRYNAME, UPPERADMINISTRATIVEAREA, UPPERLOCALITY, ");
            builder.append("        UPPERSUBLOCALITY, UPPERSTREETTYPE, UPPERSTREETNAME, UPPERSTREETNUMBER, ");
            builder.append("        UPPERESTABLISHMENTTYPE, UPPERESTABLISHMENTNAME, UPPERESTABLISHMENTNUMBER, ");
            builder.append("        UPPERADDRESSDETAIL, UPPERZIPCODE from mtr_locationmember where ");
            builder.add(toSqlFragment("LOCATIONID", condition, now));
            builder.append("   ) locIn ");
            builder.append("    on (locIn.UPPERCOUNTRYCODE = locOut.UPPERCOUNTRYCODE OR locIn.UPPERCOUNTRYCODE is null)");
            builder.append("    AND (locIn.UPPERCOUNTRYNAME = locOut.UPPERCOUNTRYNAME OR locIn.UPPERCOUNTRYNAME is null)");
            builder.append("    AND (locIn.UPPERADMINISTRATIVEAREA = locOut.UPPERADMINISTRATIVEAREA OR locIn.UPPERADMINISTRATIVEAREA is null)");
            builder.append("    AND (locIn.UPPERLOCALITY = locOut.UPPERLOCALITY OR locIn.UPPERLOCALITY is null)");
            builder.append("    AND (locIn.UPPERSUBLOCALITY = locOut.UPPERSUBLOCALITY OR locIn.UPPERSUBLOCALITY is null)");
            builder.append("    AND (locIn.UPPERSTREETTYPE = locOut.UPPERSTREETTYPE OR locIn.UPPERSTREETTYPE is null)");
            builder.append("    AND (locIn.UPPERSTREETNAME = locOut.UPPERSTREETNAME OR locIn.UPPERSTREETNAME is null)");
            builder.append("    AND (locIn.UPPERSTREETNUMBER = locOut.UPPERSTREETNUMBER OR locIn.UPPERSTREETNUMBER is null)");
            builder.append("    AND (locIn.UPPERESTABLISHMENTTYPE = locOut.UPPERESTABLISHMENTTYPE OR locIn.UPPERESTABLISHMENTTYPE is null)");
            builder.append("    AND (locIn.UPPERESTABLISHMENTNAME = locOut.UPPERESTABLISHMENTNAME OR locIn.UPPERESTABLISHMENTNAME is null)");
            builder.append("    AND (locIn.UPPERESTABLISHMENTNUMBER = locOut.UPPERESTABLISHMENTNUMBER OR locIn.UPPERESTABLISHMENTNUMBER is null)");
            builder.append("    AND (locIn.UPPERADDRESSDETAIL = locOut.UPPERADDRESSDETAIL OR locIn.UPPERADDRESSDETAIL is null)");
            builder.append("    AND (locIn.UPPERZIPCODE = locOut.UPPERZIPCODE OR locIn.UPPERZIPCODE is null)");
            builder.append(" ) ");
        }
        return builder;
    }
}
