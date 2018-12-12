/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Scs.store.Logs', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    model: 'Scs.model.Log',
    pageSize: 50,
    proxy: {
        type: 'rest',
        urlTpl: '/api/scs/servicecalls/{internalId}/logs',
        reader: {
            type: 'json',
            root: 'logs'
        },

        setUrl: function (internalId) {
            this.url = this.urlTpl.replace('{internalId}', internalId);
        }
    }
});
