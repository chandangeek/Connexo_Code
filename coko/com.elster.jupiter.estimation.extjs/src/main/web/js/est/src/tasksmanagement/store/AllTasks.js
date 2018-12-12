/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.tasksmanagement.store.AllTasks', {
    extend: 'Ext.data.Store',
    model: 'Est.estimationtasks.model.TaskInfo',
    autoLoad: false,
    proxy: {
        type: 'rest',
        pageParam: false,
        startParam: false,
        limitParam: false,
        url: '/api/tsk/task/byapplication',
        extraParams: {
            application: "MultiSense"
        },
        reader: {
            type: 'json'
        }
    }
});
