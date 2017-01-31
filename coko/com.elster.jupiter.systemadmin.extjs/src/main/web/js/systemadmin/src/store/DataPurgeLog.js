/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.store.DataPurgeLog', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest'
    ],
    model: 'Sam.model.DataPurgeLog',
    pageSize: 50,
    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/sys/data/history/{historyId}/logs',
        reader: {
            type: 'json',
            root: 'data'
        },

        setUrl: function (historyId) {
            this.url = this.urlTpl.replace('{historyId}', historyId);
        }
    }
});