/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.store.AllTasks', {
    extend: 'Ext.data.Store',
    model: 'Cfg.model.TaskInfo',
    proxy: {
        type: 'rest',
        pageParam: false,
        startParam: false,
        limitParam: false,
        url: '/api/tsk/task/byapplication',
        reader: {
            type: 'json'
        }
    },
    listeners: {
        beforeload: function (store, operation, options) {
            store.getProxy().setExtraParam('application', Uni.util.Application.getAppName() == 'MultiSense'
                ? 'MultiSense' : Uni.util.Application.getAppName() == 'MdmApp' ? 'Insight' : '');
        }
    }
});
