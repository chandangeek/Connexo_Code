/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.store.TasksQueueTypes', {
    extend: 'Ext.data.Store',
    autoLoad: false,

    fields: [
        'queueType'
    ],

    proxy: {
        type: 'rest',
        url: '/api/tsk/task/queueTypes',
        reader: {
            type: 'json',
            root: 'queueTypes'
        }
    },

    sorters: [
        {
            property: 'queueType',
            direction: 'ASC'
        }
    ]
});