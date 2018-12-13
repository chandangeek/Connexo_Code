/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.store.Logs', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    model: 'Wss.model.Log',
    pageSize: 50,
    proxy: {
        type: 'rest',
        urlTpl: '/api/ws/endpointconfigurations/{endpointId}/logs',
        reader: {
            type: 'json',
            root: 'logs'
        },

        setUrl: function (endpointId) {
            this.url = this.urlTpl.replace('{endpointId}', endpointId);
        }
    }
});
