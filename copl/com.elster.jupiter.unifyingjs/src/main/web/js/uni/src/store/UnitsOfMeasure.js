/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.store.UnitsOfMeasure', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.UnitOfMeasure',

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