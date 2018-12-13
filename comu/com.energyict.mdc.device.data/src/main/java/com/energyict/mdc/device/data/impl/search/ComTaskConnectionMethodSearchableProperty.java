/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Optional;

public class ComTaskConnectionMethodSearchableProperty extends ConnectionMethodSearchableProperty {
    static final String PROPERTY_NAME = "device.comtask.connection";
    private SearchablePropertyGroup parentGroup;

    @Inject
    public ComTaskConnectionMethodSearchableProperty(ProtocolPluggableService protocolPluggableService, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(protocolPluggableService, propertySpecService, thesaurus);
    }

    ComTaskConnectionMethodSearchableProperty init(DeviceSearchDomain domain, SearchablePropertyGroup parentGroup) {
        super.init(domain);
        this.parentGroup = parentGroup;
        return this;
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder builder = new SqlBuilder();
        builder.append(JoinClauseBuilder.Aliases.DEVICE + ".DEVICECONFIGID IN ");
        builder.openBracket();
        builder.append(" select pctask.DEVICECONFIG from DTC_COMTASKENABLEMENT cenab left join DTC_PARTIALCONNECTIONTASK pctask on ");
        builder.append(" ((cenab.PARTIALCONNECTIONTASK IS NOT NULL AND cenab.PARTIALCONNECTIONTASK = pctask.id) OR ");
        builder.append("(pctask.ISDEFAULT = 1 AND cenab.DEVICECOMCONFIG = pctask.DEVICECONFIG AND cenab.USEDEFAULTCONNECTIONTASK = 1)) where ");
        builder.add(toSqlFragment("CONNECTIONTYPE", condition, now));
        builder.closeBracket();
        return builder;
    }

    @Override
    public String getName() {
        return PROPERTY_NAME;
    }

    @Override
    public Optional<SearchablePropertyGroup> getGroup() {
        return Optional.of(this.parentGroup);
    }

    @Override
    public boolean affectsAvailableDomainProperties() {
        return false;
    }
}
