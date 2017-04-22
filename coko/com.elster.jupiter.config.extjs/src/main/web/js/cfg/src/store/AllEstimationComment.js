/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.store.EstimationComment', {
    extend: 'Ext.data.Store',
    fields: [
        'id',
        'comment'
    ],

    proxy: {
        type: 'rest',
        url: '/api/est/estimation/comments',
        reader: {
            type: 'json',
            root: 'data'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
