/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices;

import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyValue;

import java.util.HashMap;
import java.util.Map;

public class SAPRegisteredNotificationOnDevicesFilterSpecification {
    public Map<String, SearchablePropertyValue.ValueBean> properties = new HashMap<>();

    public SearchablePropertyValue.ValueBean getPropertyValue(SearchableProperty property) {
        return this.properties.get(property.getName());
    }
}
