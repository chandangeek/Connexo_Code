/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.audit.store.Users', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['id', 'name'],
    proxy: {
        type: 'rest',
        url: '/api/aud/audit/users',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: ''
        }
    }
});
