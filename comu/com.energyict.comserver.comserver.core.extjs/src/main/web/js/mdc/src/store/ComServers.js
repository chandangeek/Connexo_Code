/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.ComServers',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.ComServer'
    ],
    model: 'Mdc.model.ComServer',
    storeId: 'ComServers',
    /*sorters: [{
       property: 'name',
       direction: 'ASC'
    }],*/
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
