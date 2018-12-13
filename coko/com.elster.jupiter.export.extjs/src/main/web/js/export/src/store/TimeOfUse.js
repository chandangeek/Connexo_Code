/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.store.TimeOfUse', {
    extend: 'Ext.data.Store',
    model: 'Dxp.model.TimeOfUse',
    storeId: 'TimeOfUse',
    requires: [
        'Dxp.model.TimeOfUse'
    ],
    autoLoad: false,

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
