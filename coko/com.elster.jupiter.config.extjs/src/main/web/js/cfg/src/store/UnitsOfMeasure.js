/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.store.UnitsOfMeasure', {
    extend: 'Ext.data.Store',
    model: 'Cfg.model.UnitOfMeasure',
    storeId: 'UnitsOfMeasure',
    requires: [
        'Cfg.model.UnitOfMeasure'
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