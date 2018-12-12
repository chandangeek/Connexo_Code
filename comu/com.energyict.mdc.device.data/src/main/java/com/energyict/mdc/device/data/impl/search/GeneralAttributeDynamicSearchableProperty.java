/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import com.google.inject.Inject;

import java.time.Instant;

public class GeneralAttributeDynamicSearchableProperty extends AbstractDynamicSearchableProperty<GeneralAttributeDynamicSearchableProperty> {

    private DeviceProtocolPluggableClass pluggableClass;

    @Inject
    public GeneralAttributeDynamicSearchableProperty(Thesaurus thesaurus) {
        super(GeneralAttributeDynamicSearchableProperty.class, thesaurus);
    }

    public GeneralAttributeDynamicSearchableProperty init(SearchDomain domain, SearchablePropertyGroup group, PropertySpec propertySpec,
                                                          SearchableProperty constraintProperty, DeviceProtocolPluggableClass pluggableClass) {
        super.init(domain, group, propertySpec, constraintProperty);
        this.pluggableClass = pluggableClass;
        return this;
    }

    @Override
    public String getName() {
        return getGroup().get().getId() + "." + this.pluggableClass.getJavaClassName() + "." + getPropertySpec().getName();
    }

    @Override
    protected TranslationKey getNameTranslationKey() {
        return null;    // Should be fine since I am overwritting getDisplayName also
    }

    @Override
    public String getDisplayName() {
        return super.getDisplayName() + " (" + this.pluggableClass.getName() + ")";
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder builder = new SqlBuilder();
        builder.openBracket();
        // search property value defined on device level
        builder.append(JoinClauseBuilder.Aliases.DEVICE + ".ID IN (");
        builder.add(selectDevicesHavingPropertyValue());
        builder.append(" AND ");
        builder.add(toSqlFragment("DDC_DEVICEPROTOCOLPROPERTY.INFOVALUE", condition, now));
        builder.append(")");
        // if not found on device level then search on device configuration level
        builder.append(" OR ");
        builder.append(JoinClauseBuilder.Aliases.DEVICE + ".ID NOT IN (");
        builder.add(selectDevicesHavingPropertyValue());
        builder.append(") AND " + JoinClauseBuilder.Aliases.DEVICE + ".DEVICECONFIGID IN (");
        builder.add(selectDeviceConfigurationsHavingPropertyValue());
        builder.append(" AND ");
        builder.add(toSqlFragment("DTC_PROTOCOLCONFIGPROPSATTR.VALUE", condition, now));
        builder.append(")");
        PropertySpec propertySpec = getPropertySpec();
        if (hasDefaultValue(propertySpec)) {
            // if not found on device level and device configuration level and property has default value
            // then check this default value according to condition
            builder.append(" OR ");
            builder.append(JoinClauseBuilder.Aliases.DEVICE + ".ID NOT IN (");
            builder.add(selectDevicesHavingPropertyValue());
            builder.append(") AND ");
            builder.append(JoinClauseBuilder.Aliases.DEVICE + ".DEVICECONFIGID NOT IN (");
            builder.add(selectDeviceConfigurationsHavingPropertyValue());
            builder.append(") AND ");
            Object defaultValue = propertySpec.getPossibleValues().getDefault();
            builder.add(toSqlFragment("'" + propertySpec.getValueFactory().valueToDatabase(defaultValue) + "'", condition, now));
        }
        builder.closeBracket();
        return builder;
    }

    private boolean hasDefaultValue(PropertySpec propertySpec) {
        return propertySpec.getPossibleValues() != null && propertySpec.getPossibleValues().getDefault() != null;
    }

    private SqlFragment selectDevicesHavingPropertyValue() {
        SqlBuilder builder = new SqlBuilder();
        builder.append("select DEVICEID from DDC_DEVICEPROTOCOLPROPERTY where DDC_DEVICEPROTOCOLPROPERTY.PROPERTYSPEC = ");
        builder.addObject(getPropertySpec().getName());
        return builder;
    }

    private SqlFragment selectDeviceConfigurationsHavingPropertyValue() {
        SqlBuilder builder = new SqlBuilder();
        builder.append("select DEVICECONFIGURATION from DTC_PROTOCOLCONFIGPROPSATTR where DTC_PROTOCOLCONFIGPROPSATTR.NAME = ");
        builder.addObject(getPropertySpec().getName());
        return builder;
    }
}
