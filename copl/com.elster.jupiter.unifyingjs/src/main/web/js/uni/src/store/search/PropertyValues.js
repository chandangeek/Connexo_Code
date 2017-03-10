/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.store.search.PropertyValues
 */
Ext.define('Uni.store.search.PropertyValues', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.search.PropertyValue',

    remoteFilter: true,

    proxy: {
        type: 'ajax',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        url: '/api/jsr/search/{searchDomain}/searchcriteria/{property}', // Gets overwritten anyways.
        reader: {
            type: 'json',
            root: 'values'
        }
    }
});