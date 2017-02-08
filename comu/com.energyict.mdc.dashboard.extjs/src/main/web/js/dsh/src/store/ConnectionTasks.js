/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.store.ConnectionTasks', {
    extend: 'Uni.data.store.Filterable',
    requires: [
        'Dsh.model.ConnectionTask',
        'Dsh.util.FilterStoreHydrator'
    ],
    model: 'Dsh.model.ConnectionTask',
    hydrator: 'Dsh.util.FilterStoreHydrator',
    autoLoad: false,
    remoteFilter: true,
    proxy: {
        type: 'rest',
        url: '/api/dsr/connections',
        reader: {
            type: 'json',
            root: 'connectionTasks',
            totalProperty: 'total'
        }
    }
});

