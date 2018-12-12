/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.main.store.TimeOfUse', {
    extend: 'Ext.data.Store',
    model: 'Est.main.model.TimeOfUse',

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