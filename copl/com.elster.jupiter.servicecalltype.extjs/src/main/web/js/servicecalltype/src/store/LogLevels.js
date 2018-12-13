/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sct.store.LogLevels', {
    extend: 'Ext.data.Store',
    model: 'Sct.model.LogLevel',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/scs/field/loglevels',
        timeout: 120000,
        reader: {
            type: 'json',
            root: 'logLevels'
        }
    }

});
