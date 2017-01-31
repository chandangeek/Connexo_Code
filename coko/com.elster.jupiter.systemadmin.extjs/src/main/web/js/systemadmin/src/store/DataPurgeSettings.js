/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.store.DataPurgeSettings', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest'
    ],
    model: 'Sam.model.DataPurgeSetting',
    pageSize: 10,
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/sys/data/lifecycle/categories',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});