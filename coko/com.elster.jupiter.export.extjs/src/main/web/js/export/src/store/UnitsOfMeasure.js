/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.store.UnitsOfMeasure', {
    extend: 'Ext.data.Store',
    model: 'Dxp.model.UnitOfMeasure',
    storeId: 'UnitsOfMeasure',
    requires: [
        'Dxp.model.UnitOfMeasure'
    ],
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/mtr/fields/unitsofmeasure',
        reader: {
            type: 'json',
            root: 'unitsOfMeasure'
        },

        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
