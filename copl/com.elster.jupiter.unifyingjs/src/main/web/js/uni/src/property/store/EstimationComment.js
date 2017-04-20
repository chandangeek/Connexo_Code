/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.store.EstimationComment', {
    extend: 'Ext.data.Store',
    fields: [
        'id',
        'comment'
    ],
    autoLoad: true,
    proxy: {
        type: 'rest',
        url: 'api/est/estimation/comments',
        reader: {
            type: 'json'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
