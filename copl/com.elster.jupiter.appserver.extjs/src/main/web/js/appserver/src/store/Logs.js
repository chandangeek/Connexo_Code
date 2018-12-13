/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.store.Logs', {
    extend: 'Ext.data.Store',
    model: 'Apr.model.Log',
    pageSize: 50,
    proxy: {
        type: 'rest',
        urlTpl: '/api/ctk/customtask/history/{occurrenceId}/logs',
        reader: {
            type: 'json',
            root: 'data'
        },

        setUrl: function (params) {
            this.url = this.urlTpl.replace('{occurrenceId}', params.occurrenceId);
        }
    }
});
