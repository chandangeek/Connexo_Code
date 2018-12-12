/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.store.Workgroups', {
    extend: 'Ext.data.Store',
    model: 'Usr.model.Workgroup',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/usr/workgroups',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'workGroups'
        }
    }
});
