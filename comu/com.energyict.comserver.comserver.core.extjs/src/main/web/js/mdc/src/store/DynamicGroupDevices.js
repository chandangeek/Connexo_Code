/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.DynamicGroupDevices', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.search.Result',
    remoteFilter: true,
    sorters: ['name'],
    proxy: {
        timeout: 9999999,
        type: 'ajax',
        reader: {
            type: 'json',
            root: 'searchResults'
        },
        actionMethods: {
            create: 'POST',
            read: 'POST',
            update: 'PUT',
            destroy: 'DELETE'
        }
    }
});