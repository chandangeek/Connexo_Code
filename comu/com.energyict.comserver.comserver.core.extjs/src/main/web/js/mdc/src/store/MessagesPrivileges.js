/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.MessagesPrivileges', {
    extend: 'Ext.data.Store',
    storeId: 'MessagesPrivileges',
    autoLoad: false,
    fields: [
        { name: 'name', type: 'string' },
        { name: 'roles', type: 'auto' },
        { name: 'privilege', type: 'auto'}
    ],
    proxy: {
        type: 'rest',
        url: '/api/dtc/devicemessageprivileges',
        reader: {
            type: 'json',
            root: 'privileges'
        }
    },
    sorters: [
        {
            property: 'name',
            direction: 'ASC'
        }
    ]
});