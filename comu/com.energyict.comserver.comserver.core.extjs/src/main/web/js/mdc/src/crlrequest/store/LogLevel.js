/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.crlrequest.store.LogLevel', {
    extend: 'Ext.data.Store',
    require: ['Mdc.crlrequest.model.LogLevel'],
    model: 'Mdc.crlrequest.model.LogLevel',
    autoLoad: true,

    proxy: {
        type: 'rest',
        url: '/api/ddr/crlprops/loglevels',
        reader: {
            type: 'json',
            root: 'logLevels'
        }
    }
});
