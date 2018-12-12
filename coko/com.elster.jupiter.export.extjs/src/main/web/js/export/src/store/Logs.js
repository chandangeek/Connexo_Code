/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.store.Logs', {
    extend: 'Ext.data.Store',
    model: 'Dxp.model.Log',
    pageSize: 50,
    proxy: {
        type: 'rest',
        urlTpl: '/api/export/dataexporttask/history/{occurrenceId}/logs',
        reader: {
            type: 'json',
            root: 'data'
        },

        setUrl: function (params) {
            this.url = this.urlTpl.replace('{occurrenceId}', params.occurrenceId);
        }
    }
});
