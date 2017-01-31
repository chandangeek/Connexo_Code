/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.store.Intervals', {
    extend: 'Ext.data.Store',
    model: 'Cfg.model.Interval',
    storeId: 'Intervals',
    requires: [
        'Cfg.model.Interval'
    ],
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/mtr/fields/intervals',
        reader: {
            type: 'json',
            root: 'intervals'
        },

        pageParam: false,
        startParam: false,
        limitParam: false
    }
});