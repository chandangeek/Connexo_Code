/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.estimationtasks.store.EstimationTasksHistory', {
    extend: 'Uni.data.store.Filterable',
    model: 'Est.estimationtasks.model.EstimationTaskHistory',
    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/est/estimation/tasks/{taskId}/history',
        reader: {
            type: 'json',
            root: 'data'
        },
        setUrl: function (params) {
            this.url = this.urlTpl.replace('{taskId}', params.taskId);
        }
    }
});
