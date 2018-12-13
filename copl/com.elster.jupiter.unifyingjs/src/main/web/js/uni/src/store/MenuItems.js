/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.store.MenuItems
 */
Ext.define('Uni.store.MenuItems', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.MenuItem',
    storeId: 'menuItems',
    singleton: true,
    autoLoad: false,
    clearOnPageLoad: false,
    clearRemovedOnLoad: false,

    proxy: {
        type: 'memory',
        reader: {
            type: 'json',
            root: 'items'
        }
    },

    sorters: [
        {
            property: 'index',
            direction: 'DESC'
        }
    ]
});