/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.store.Locations
 */
Ext.define('Uni.store.FindLocations', {
    extend: 'Ext.data.Store',
    storeId: 'Uni.store.FindLocations',
    autoLoad: false,

    fields: [
        {name:'displayValue'}
    ],


    proxy: {
        type: 'rest',
        url: '',
        reader: {
            type: 'json',
            root: 'values'
        },
        setUrl: function(url){
            this.url = url;
        }
    }
});