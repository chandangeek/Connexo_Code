/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Uni.store.LogLevels', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.LogLevel',
    storeId: 'LogLevelsStore',

    proxy: {
        type: 'rest',
        url: '/api/rut/loglevels',
        timeout: 120000,
        reader: {
            type: 'json',
            root: 'logLevels'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }

});