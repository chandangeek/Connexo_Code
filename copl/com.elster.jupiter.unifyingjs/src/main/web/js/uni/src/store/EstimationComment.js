/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.store.EstimationComment', {
    extend: 'Ext.data.Store',
    fields: [
        'id',
        'comment'
    ],

    proxy: {
        type: 'rest',
        url: '/api/est/field/comments',
        reader: {
            type: 'json',
            root: 'estimationComments'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
