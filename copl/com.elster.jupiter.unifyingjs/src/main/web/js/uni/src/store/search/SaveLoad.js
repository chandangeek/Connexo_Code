/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.store.search.SaveLoad
 */
Ext.define('Uni.store.search.SaveLoad', {
    extend: 'Ext.data.Store',
    storeId: 'Uni.store.search.SaveLoads',
    alias: 'store.saveSearchStore',
    autoLoad: true,
   // remoteFilter: true,
   // pageSize: 100,
    fields: [
        {type: 'string', name: 'name'},
        {type: 'string', name: 'user'},
        {type: 'string', name: 'criteria'},
        {type: 'string', name: 'domainid'},


    ],
    sorters: [{
        property: 'name',
        direction: 'ASC'
    }],
    proxy: {
        type: 'rest',
        url: '../../api/jsr/search/saveSearchCriteria',
        pageParam: false,
        startParam: false,
        limitParam: false,
        reader: {
            type: 'json',
            root: 'numberOfSearchResults'
        }

    }
});