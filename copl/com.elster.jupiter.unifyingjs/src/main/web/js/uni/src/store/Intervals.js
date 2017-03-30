/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.store.Intervals', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.Interval',

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