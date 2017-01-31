/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.readingtypes.store.ReadingTypes', {
    extend: 'Uni.data.store.Filterable',
    requires: ['Mtr.readingtypes.model.ReadingType'],
    model: 'Mtr.readingtypes.model.ReadingType',
    storeId: 'ReadingTypes',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '../../api/mtr/readingtypes',
        reader: {
            type: 'json',
            root: 'readingTypes'
        }
    }
});