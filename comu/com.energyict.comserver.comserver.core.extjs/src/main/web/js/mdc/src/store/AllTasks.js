/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.AllTasks', {
    extend: 'Ext.data.Store',
    model: 'Mdc.model.TaskInfo',
    proxy: {
        type: 'rest',
        pageParam: false,
        startParam: false,
        limitParam: false,
        url: '/api/tsk/task/byapplication',
        extraParams: {
            application: Uni.util.Application.getAppName() == 'MultiSense' ? 'MultiSense' : Uni.util.Application.getAppName() == 'MdmApp' ? 'Insight' : ''
        },
        reader: {
            type: 'json'
        }
    }
});
