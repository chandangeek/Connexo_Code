/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Ldr.store.LogLevels', {
    extend: 'Ext.data.Store',
    model: 'Ldr.model.LogLevel',
    storeId: 'LogLevelsStore',
    singleton: true,
    autoLoad: false,

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