/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.store.TimeOfUse', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.TimeOfUse',

    proxy: {
        type: 'rest',
        url: '/api/mtr/fields/timeofuse',
        reader: {
            type: 'json',
            root: 'timeOfUse'
        },

        pageParam: false,
        startParam: false,
        limitParam: false
    }
});