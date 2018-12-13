/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.store.task.TaskGroups', {
    extend: 'Ext.data.Store',
    model: 'Bpm.model.task.TaskGroup',
    autoLoad: false,
    proxy: {

        type: 'rest',
        url: '/api/bpm/runtime/tasks/mandatory',
        timeout: 240000,
         reader: {
            type: 'json',
            root: 'taskGroups'
         },

        actionMethods: {
            read: 'POST'
        },
        pageParam: false,
        startParam: false,
        limitParam: false

    }

});

