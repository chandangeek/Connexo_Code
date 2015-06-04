package com.energyict.mdc.device.data.impl.search;

import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

import com.elster.jupiter.search.SearchablePropertyGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link SearchablePropertyGroup} interface
 * that will group all properties of one {@link ConnectionTypePluggableClass}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-03 (14:16)
 */
public class ConnectionTypeSearchablePropertyGroup implements SearchablePropertyGroup {

    private final DeviceSearchDomain domain;
    private final ConnectionTypePluggableClass pluggableClass;
    private List<ConnectionTypeSearchableProperty> properties = new ArrayList<>();

    ConnectionTypeSearchablePropertyGroup(DeviceSearchDomain domain, ConnectionTypePluggableClass pluggableClass) {
        super();
        this.domain = domain;
        this.pluggableClass = pluggableClass;
        this.initializeProperties();
    }

    private void initializeProperties() {
        this.properties = this.pluggableClass
                .getPropertySpecs()
                .stream()
                .map(spec -> new ConnectionTypeSearchableProperty(this.domain, this, this.pluggableClass, spec))
                .collect(Collectors.toList());
    }

    @Override
    public String getId() {
        return String.valueOf(this.pluggableClass.getId());
    }

    @Override
    public String getDisplayName() {
        return this.pluggableClass.getName();
    }

    List<ConnectionTypeSearchableProperty> getProperties() {
        return properties;
    }

}