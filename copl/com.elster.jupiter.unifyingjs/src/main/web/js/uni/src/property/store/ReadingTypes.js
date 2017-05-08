/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.store.ReadingTypes', {
    extend: 'Ext.data.Store',
    requires: [
        'Uni.property.model.ReadingType'
    ],
    model: 'Uni.property.model.ReadingType',
    pageSize: 50,
    proxy: {
        type: 'rest',
        url: '/api/mtr/readingtypes',
        start: 0,
        limit: 50,
        reader: {
            type: 'json',
            root: 'readingTypes'
        }
    }
});
