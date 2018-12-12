/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.store.search.Properties
 */
Ext.define('Uni.store.search.Properties', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.search.Property',
    storeId: 'Uni.store.search.Properties',
    singleton: true,
    autoLoad: false,
    remoteFilter: true,

    proxy: {
        type: 'ajax',
        url: '/api/jsr/search/{domainLinkHref}/searchcriteria', // Gets overwritten anyways.
        limitParam: null,
        pageParam: null,
        startparam: null,
        reader: {
            type: 'json',
            root: 'properties'
        }
    },

    sorters: [
        {
            sorterFn: function (o1, o2) {
                if (    !(o1.get('constraints') && o1.get('constraints').length)
                    &&  !(o2.get('constraints') && o2.get('constraints').length)
                ) {
                    return 0;
                }
                if (o1.get('constraints').length) {
                    if (o1.get('constraints').indexOf(o2.get('name')) >= 0) {
                        return 1;
                    }
                }

                if (o2.get('constraints').length) {
                    if (o2.get('constraints').indexOf(o1.get('name')) >= 0) {
                        return -1;
                    }
                }

                return 0;
            }
        }
    ]
});