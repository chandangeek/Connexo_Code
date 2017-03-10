/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.store.PropertyReadingTypes', {
    extend: 'Ext.data.Store',
    requires: [
        'Uni.property.model.ReadingType'
    ],
    model: 'Uni.property.model.ReadingType',
    storeId: 'PropertyReadingTypes',
    proxy: {
        type: 'ajax',
        url: '/api/mtr/readingtypes',
        reader: {
            type: 'json',
            root: 'readingTypes'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});