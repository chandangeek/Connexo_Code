/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.store.ReadingTypes', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.ReadingType',

    buffered: true,
    pageSize: 200,

    proxy: {
        type: 'rest',
        url: '/api/mtr/readingtypes',
        reader: {
            type: 'json',
            root: 'readingTypes'
        }
    }
});