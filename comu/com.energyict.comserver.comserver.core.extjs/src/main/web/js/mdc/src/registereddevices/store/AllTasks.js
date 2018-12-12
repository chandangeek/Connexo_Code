/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.registereddevices.store.AllTasks', {
    extend: 'Ext.data.Store',
    model: 'Mdc.registereddevices.model.TaskInfo',
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
