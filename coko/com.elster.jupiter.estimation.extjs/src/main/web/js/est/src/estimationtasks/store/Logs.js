/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.estimationtasks.store.Logs', {
    extend: 'Ext.data.Store',
    model: 'Est.estimationtasks.model.Logs',
    pageSize: 50,
    proxy: {
        type: 'rest',
        urlTpl: '/api/est/estimation/tasks/{taskId}/history/{occurrenceId}',
        reader: {
            type: 'json',
            root: 'data'
        },

        setUrl: function (params) {
            this.url = this.urlTpl.replace('{taskId}', params.taskId).replace('{occurrenceId}', params.occurrenceId);
        }
    }
});
