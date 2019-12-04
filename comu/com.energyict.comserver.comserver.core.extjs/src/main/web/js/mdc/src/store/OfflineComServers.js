/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.OfflineComServers',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.ComServer'
    ],
    model: 'Mdc.model.ComServer',
    storeId: 'OfflineComServers',
    filters: [{
        property: 'comServerType',
        value: '1'
    }],
    remoteFilter: true,
    remoteSort: true,
    proxy: {
        type: 'rest',
        url: '/api/mdc/comservers',
        reader: {
            type: 'json',
            root: 'data'
        }/*,
        simpleSortMode: true*/
    }
});
