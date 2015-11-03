package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlFragment;
import com.google.inject.Inject;

import java.time.Instant;

public class ProtocolDialectDynamicSearchableProperty extends AbstractDynamicSearchableProperty<ProtocolDialectDynamicSearchableProperty> {

    private String relationTableName;
    private long deviceProtocolId;

    @Inject
    public ProtocolDialectDynamicSearchableProperty() {
        super(ProtocolDialectDynamicSearchableProperty.class);
    }

    public ProtocolDialectDynamicSearchableProperty init(SearchDomain domain, SearchablePropertyGroup group, PropertySpec propertySpec, long deviceProtocolId, String relationTableName) {
        super.init(domain, group, propertySpec);
        this.deviceProtocolId = deviceProtocolId;
        this.relationTableName = relationTableName;
        return this;
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
        builder.addProtocolDialectProperties(this.deviceProtocolId, this.relationTableName);
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        return this.toSqlFragment("druprops." + getPropertySpec().getName(), condition, now);
    }
}
