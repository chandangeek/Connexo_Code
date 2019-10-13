/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Wss.store.RelatedAttributeStore', {
    extend: 'Ext.data.Store',
    model: 'Wss.model.RelatedAttributeModel',
    pageSize: 50,
    autoLoad: false,
    requires: [
        'Ext.data.proxy.Rest'
    ],

    proxy: {
        type: 'rest',
        url: '/api/ws/occurrences/relatedattributes',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});