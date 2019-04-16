/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.store.search.Results
 */
Ext.define('Uni.store.search.Results', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.search.Result',
    storeId: 'Uni.store.search.Results',
    singleton: true,
    autoLoad: false,
    remoteFilter: true,
    pageSize: 100,
    sorters: ['name'],
    proxy: {
        timeout: 9999999,
        type: 'ajax',
        // actionMethods: {
        //     create : 'POST',
        //     read   : 'POST',
        //     update : 'POST',
        //     destroy: 'POST'
        // },
        reader: {
            type: 'json',
            root: 'searchResults'
        }
    }
});