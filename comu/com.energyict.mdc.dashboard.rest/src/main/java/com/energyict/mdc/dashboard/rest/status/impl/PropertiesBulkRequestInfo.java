package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.rest.util.properties.PropertyInfo;
import java.util.List;

/**
 * We need this class because ExtJS can't handle a list as-is, needs to be assigned to a value (or so I'm told)
 * [ 1,2,3 ]
 * needs to be
 * "properties" = [ 1,2,3]
 *
 * Created by bvn on 4/15/15.
 */
public class PropertiesBulkRequestInfo extends ConnectionsBulkRequestInfo {
    public List<PropertyInfo> properties;

    public PropertiesBulkRequestInfo() {
    }
}
