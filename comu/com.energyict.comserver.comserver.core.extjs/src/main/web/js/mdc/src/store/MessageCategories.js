/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.MessageCategories', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.MessageCategory'
    ],
    model: 'Mdc.model.MessageCategory',
    storeId: 'MessageCategories',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/cts/comtasks/messages',
        reader: {
            type: 'json',
            root: 'data'
        },
        timeout: 300000,
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});