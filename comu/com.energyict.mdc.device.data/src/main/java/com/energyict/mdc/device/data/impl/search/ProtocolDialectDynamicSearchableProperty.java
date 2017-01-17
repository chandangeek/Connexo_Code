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
import com.google.inject.Inject;

import java.time.Instant;

public class ProtocolDialectDynamicSearchableProperty extends AbstractDynamicSearchableProperty<ProtocolDialectDynamicSearchableProperty> {

    private String relationTableName;
    private ProtocolDialectSearchableProperty.ProtocolDialect protocolDialect;

    @Inject
    public ProtocolDialectDynamicSearchableProperty(Thesaurus thesaurus) {
        super(ProtocolDialectDynamicSearchableProperty.class, thesaurus);
    }

    public ProtocolDialectDynamicSearchableProperty init(SearchDomain domain, SearchablePropertyGroup group, PropertySpec propertySpec,
                                                         SearchableProperty constraintProperty, ProtocolDialectSearchableProperty.ProtocolDialect protocolDialect,
                                                         String relationTableName) {
        super.init(domain, group, propertySpec, constraintProperty);
        this.protocolDialect = protocolDialect;
        this.relationTableName = relationTableName;
        return this;
    }

    @Override
    public String getName() {
        return getGroup().get().getId() + "." + this.protocolDialect.getProtocolDialect().getDeviceProtocolDialectName() + "." + getPropertySpec().getName();
    }

    @Override
    protected TranslationKey getNameTranslationKey() {
        return null;    // Should be fine since I am overwritting getDisplayName also
    }

    @Override
    public String getDisplayName() {
        return super.getDisplayName() + " (" + this.protocolDialect.getProtocolDialect().getDeviceProtocolDialectDisplayName() + ")";
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
        builder.addProtocolDialectProperties(this.protocolDialect.getPluggableClass().getId(), this.relationTableName);
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder builder = new SqlBuilder();
        builder.openBracket();
        builder.add(this.toSqlFragment(this.relationTableName + "." + getPropertySpec().getName(), condition, now));
        builder.append(" OR " + JoinClauseBuilder.Aliases.DEVICE + ".DEVICECONFIGID IN ");
        builder.openBracket();
        builder.add(selectDeviceConfigurationProperties(condition, now));
        builder.closeBracket();
        builder.closeBracket();
        return builder;
    }

    // If we want to search by configuration properties, the ProtocolDialectDynamicPropertyJoinType should use left join
    private SqlFragment selectDeviceConfigurationProperties(Condition condition, Instant now) {
        SqlBuilder builder = new SqlBuilder();
        builder.append("SELECT dpcp.DEVICECONFIGURATION ");
        builder.append("FROM DTC_DIALECTCONFIGPROPSATTR dpca join DTC_DIALECTCONFIGPROPERTIES dpcp on dpca.id = dpcp.id ");
        builder.append("WHERE dpcp.DEVICEPROTOCOLDIALECT = '");
        builder.append(this.protocolDialect.getProtocolDialect().getDeviceProtocolDialectName());
        builder.append("' AND dpca.NAME = '");
        builder.append(getPropertySpec().getName());
        builder.append("' AND ");
        builder.add(toSqlFragment("dpca.VALUE", condition, now));
        return builder;
    }
}
