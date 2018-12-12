/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.store.Tasks', {
    extend: 'Ext.data.Store',
    model: 'Apr.model.Task',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/tsk/task',
        reader: {
            type: 'json',
            root: 'tasks'
        }
    }
});