/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.search;

import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchDomainExtension;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.util.Holder;
import com.elster.jupiter.util.HolderBuilder;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.impl.search.SearchableDeviceProperty;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component(name = "com.energyict.mdc.sap.impl.search.SapAttributesDeviceSearchDomainExtension", service = SearchDomainExtension.class, immediate = true)
public class SapAttributesDeviceSearchDomainExtension implements SearchDomainExtension {

    private volatile WebServiceActivator webServiceActivator;
    private volatile Clock clock;

    public SapAttributesDeviceSearchDomainExtension() {
        // for OSGI purposes
    }

    @Inject
    public SapAttributesDeviceSearchDomainExtension(WebServiceActivator webServiceActivator, Clock clock) {
        setWebServiceActivator(webServiceActivator);
        setClock(clock);
    }

    @Reference
    public void setWebServiceActivator(WebServiceActivator webServiceActivator) {
        this.webServiceActivator = webServiceActivator;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public boolean isExtensionFor(SearchDomain domain, List<SearchablePropertyConstriction> constrictions) {
        return domain.getDomainClass().isAssignableFrom(Device.class);
    }

    @Override
    public List<SearchableProperty> getProperties() {
        return this.webServiceActivator.getSearchableProperties();
    }

    @Override
    public SqlFragment asFragment(List<SearchablePropertyCondition> conditions) {
        Instant now = clock.instant();
        SqlBuilder builder = new SqlBuilder();
        Holder<String> holder = HolderBuilder.first("").andThen(" INTERSECT ");
        conditions.forEach(condition -> {
            Optional<SearchableProperty> property = getProperties().stream()
                    .filter(prop -> prop.getName().equals(condition.getProperty().getName())).findAny();
            if (property.isPresent()) {
                builder.append(holder.get());
                builder.add(((SearchableDeviceProperty)property.get()).toSqlFragment(condition.getCondition(), now));
            }
        });
        return builder;
    }
}
