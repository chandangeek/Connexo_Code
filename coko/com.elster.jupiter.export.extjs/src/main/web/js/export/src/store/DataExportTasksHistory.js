/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.store.DataExportTasksHistory', {
    extend: 'Uni.data.store.Filterable',
    model: 'Dxp.model.DataExportTaskHistory',
    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/export/dataexporttask/{taskId}/history',
        reader: {
            type: 'json',
            root: 'data'
        },
        setUrl: function (params) {
            this.url = this.urlTpl.replace('{taskId}', params.taskId);
        },
        setCommonUrl: function () {
            this.url = '/api/export/dataexporttask/history';
        }
    }
});
