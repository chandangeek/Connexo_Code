/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.store.ReadingTypes', {
    extend: 'Uni.data.store.Filterable',
    requires: ['Mtr.model.ReadingType'],
    model: 'Mtr.model.ReadingType',
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