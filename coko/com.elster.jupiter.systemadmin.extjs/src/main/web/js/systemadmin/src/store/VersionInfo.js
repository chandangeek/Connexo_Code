/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.store.VersionInfo', {
    extend: 'Ext.data.Store',
    model: 'Sam.model.VersionInfo',

    proxy: {
        type: 'rest',
        url: '/api/sys/fields/versionInfo',
        reader: {
            type: 'json'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});