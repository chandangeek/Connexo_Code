/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.store.ValidationTasksHistory', {
    extend: 'Uni.data.store.Filterable',
    model: 'Cfg.model.ValidationTaskHistory',
    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/val/validationtasks/{taskId}/history',
        reader: {
            type: 'json',
            root: 'data'
        },
        setUrl: function (params) {
            this.url = this.urlTpl.replace('{taskId}', params.taskId);
        }
    }
});
