/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.store.readingtypes.ReadingTypes', {
    extend: 'Uni.data.store.Filterable',
    requires: ['Mtr.model.readingtypes.ReadingType'],
    model: 'Mtr.model.readingtypes.ReadingType',
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