/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.store.Purposes', {
    extend: 'Ext.data.Store',
    model: 'Imt.usagepointmanagement.model.Purpose',
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{usagePointId}/purposes',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'purposes'
        },
        remoteFilter: false,
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});