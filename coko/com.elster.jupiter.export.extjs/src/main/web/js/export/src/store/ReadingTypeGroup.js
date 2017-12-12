/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.store.ReadingTypeGroup', {
    extend: 'Ext.data.Store',
    model: 'Dxp.model.ReadingTypeGroup',
    storeId: 'ReadingTypeGroup',
    requires: [
        'Dxp.model.ReadingTypeGroup'
    ],
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/mtr/fields/readingtypegroup/',
        reader: {
            type: 'json',
            root: 'readingTypeGroup'
        },

        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
