/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.store.RelatedObjectStore', {
    extend: 'Ext.data.Store',
    model: 'Wss.model.RelatedObjectModel',
    pageSize: 50,
    autoLoad: false,
    requires: [
        'Ext.data.proxy.Rest'
    ],

    proxy: {
        type: 'rest',
        url: '/api/ws/occurrences/relatedobjects',
        reader: {
            type: 'json',
            root: 'data'
        }//,
    }
});