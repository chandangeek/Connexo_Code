/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.store.UsagePointHistoryDevices', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.usagepointmanagement.model.UsagePointHistoryDevice'
    ],
    model: 'Mdc.usagepointmanagement.model.UsagePointHistoryDevice',
    proxy: {
        type: 'rest',
        url: '/api/upr/usagepoints/{usagePointId}/history/devices',
        reader: {
            type: 'json',
            root: 'devices',
            totalProperty: 'total'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});