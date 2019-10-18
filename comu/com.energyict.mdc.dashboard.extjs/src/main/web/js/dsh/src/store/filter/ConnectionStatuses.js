/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.store.filter.ConnectionStatuses', {
    extend: 'Ext.data.Store',
    fields: ['localizedValue', 'taskStatus'],
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/dsr/field/connectionstatuses',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'taskStatuses'
        }
    }
});
