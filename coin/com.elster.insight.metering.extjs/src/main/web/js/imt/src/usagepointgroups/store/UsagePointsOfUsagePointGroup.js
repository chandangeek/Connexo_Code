/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointgroups.store.UsagePointsOfUsagePointGroup', {
    extend: 'Ext.data.Store',
    requires: [
        'Imt.usagepointmanagement.model.UsagePoint'
    ],
    model: 'Imt.usagepointmanagement.model.UsagePoint',
    storeId: 'UsagePointsOfUsagePointGroup',
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepointgroups/{id}/usagepoints',
        reader: {
            type: 'json',
            root: 'usagepoints'
        }
    }
});
