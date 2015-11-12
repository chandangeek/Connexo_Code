package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.search.SearchDomain;
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
    public GeneralAttributeDynamicSearchableProperty() {
        super(GeneralAttributeDynamicSearchableProperty.class);
    }

    public GeneralAttributeDynamicSearchableProperty init(SearchDomain domain, SearchablePropertyGroup group, PropertySpec propertySpec, DeviceProtocolPluggableClass pluggableClass) {
        super.init(domain, group, propertySpec);
        this.pluggableClass = pluggableClass;
        return this;
    }

    @Override
    public String getName() {
        return getGroup().get().getId() + "." + this.pluggableClass.getJavaClassName() + "." + getPropertySpec().getName();
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
        builder.append(JoinClauseBuilder.Aliases.DEVICE + ".ID IN (");
        builder.add(selectDeviceProperties(condition, now));
        builder.closeBracket();
        builder.append(" OR ");
        builder.append(JoinClauseBuilder.Aliases.DEVICE + ".ID NOT IN (");
        builder.append("select DEVICEID from DDC_DEVICEPROTOCOLPROPERTY where DDC_DEVICEPROTOCOLPROPERTY.PROPERTYSPEC = '");
        builder.append(getPropertySpec().getName());
        builder.append("' AND " + JoinClauseBuilder.Aliases.DEVICE + ".DEVICECONFIGID IN (");
        builder.add(selectDeviceConfigurationProperties(condition, now));
        builder.closeBracket();
        builder.closeBracket();
        return builder;
    }

    private SqlFragment selectDeviceProperties(Condition condition, Instant now) {
        SqlBuilder builder = new SqlBuilder();
        builder.append("select DEVICEID from DDC_DEVICEPROTOCOLPROPERTY where DDC_DEVICEPROTOCOLPROPERTY.PROPERTYSPEC = '");
        builder.append(getPropertySpec().getName());
        builder.append("' AND ");
        builder.add(toSqlFragment("DDC_DEVICEPROTOCOLPROPERTY.INFOVALUE", condition, now));
        return builder;
    }

    private SqlFragment selectDeviceConfigurationProperties(Condition condition, Instant now) {
        SqlBuilder builder = new SqlBuilder();
        builder.append("select DEVICECONFIGURATION from DTC_PROTOCOLCONFIGPROPSATTR where DTC_PROTOCOLCONFIGPROPSATTR.NAME = '");
        builder.append(getPropertySpec().getName());
        builder.append("' AND ");
        builder.add(toSqlFragment("DTC_PROTOCOLCONFIGPROPSATTR.VALUE", condition, now));
        return builder;
    }
}
