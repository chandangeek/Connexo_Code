package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;

import com.energyict.mdc.device.data.DeviceFields;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;

import javax.inject.Inject;
import java.io.Serializable;
import java.time.Instant;
import java.util.*;


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
                //             .markExhaustive()
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

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {

        SqlBuilder builder = new SqlBuilder();
        builder.append(JoinClauseBuilder.Aliases.END_DEVICE + ".LOCATIONID IN (");
        builder.append(" select locOut.LOCATIONID from mtr_locationmember locOut right join ");
        builder.append(" (select LOCATIONID, UPPERCOUNTRYCODE, UPPERCOUNTRYNAME, UPPERADMINISTRATIVEAREA, UPPERLOCALITY, ");
        builder.append("        UPPERSUBLOCALITY, UPPERSTREETTYPE, UPPERSTREETNAME, UPPERSTREETNUMBER, ");
        builder.append("        UPPERESTABLISHMENTTYPE, UPPERESTABLISHMENTNAME, UPPERESTABLISHMENTNUMBER, ");
        builder.append("        UPPERADDRESSDETAIL, UPPERZIPCODE from mtr_locationmember where ");
        builder.add(toSqlFragment("LOCATIONID", condition, now));
        builder.append("   ) locIn ");
        builder.append("    on locIn.UPPERCOUNTRYCODE = locOut.UPPERCOUNTRYCODE AND locIn.UPPERCOUNTRYNAME = locOut.UPPERCOUNTRYNAME AND ");
        builder.append("        locIn.UPPERADMINISTRATIVEAREA = locOut.UPPERADMINISTRATIVEAREA AND locIn.UPPERLOCALITY = locOut.UPPERLOCALITY AND ");
        builder.append("        locIn.UPPERSUBLOCALITY = locOut.UPPERSUBLOCALITY AND locIn.UPPERSTREETTYPE = locOut.UPPERSTREETTYPE AND ");
        builder.append("        locIn.UPPERSTREETNAME = locOut.UPPERSTREETNAME AND locIn.UPPERSTREETNUMBER = locOut.UPPERSTREETNUMBER AND ");
        builder.append("        locIn.UPPERESTABLISHMENTTYPE = locOut.UPPERESTABLISHMENTTYPE AND locIn.UPPERESTABLISHMENTNAME = locOut.UPPERESTABLISHMENTNAME AND ");
        builder.append("        locIn.UPPERESTABLISHMENTNUMBER = locOut.UPPERESTABLISHMENTNUMBER AND locIn.UPPERADDRESSDETAIL = locOut.UPPERADDRESSDETAIL AND ");
        builder.append("        locIn.UPPERZIPCODE = locOut.UPPERZIPCODE ");
        builder.append(" ) ");

        return builder;
    }


}
