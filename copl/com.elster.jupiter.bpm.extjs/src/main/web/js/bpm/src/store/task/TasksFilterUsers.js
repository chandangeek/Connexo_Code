/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.store.task.TasksFilterUsers', {
    extend: 'Ext.data.Store',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/bpm/runtime/assignees?me=false',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'data'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    },

    fields: [
        {name: 'id',    type: 'int'},
        {name: 'name',  type: 'string'}
    ]
});
