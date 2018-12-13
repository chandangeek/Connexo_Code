/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.store.SystemInfo', {
    extend: 'Ext.data.Store',
    model: 'Sam.model.SystemInfo',

    proxy: {
        type: 'rest',
        url: '/api/sys/systeminfo',
        reader: {
            type: 'json'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});