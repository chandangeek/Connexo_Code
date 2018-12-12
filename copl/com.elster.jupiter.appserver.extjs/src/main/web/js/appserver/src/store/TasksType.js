/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.store.TasksType', {
    extend: 'Ext.data.Store',
    fields: [
        'queue',
        'queue'
    ],
    proxy: {
        type: 'rest',
        urlTpl: '/api/tsk/task/compatiblequeues/{id}',
        reader: {
            type: 'json',
            root: 'queue'
        },
        pageParam: false,
        startParam: false,
        limitParam: false,
        setUrl: function (taskId) {
            this.url = this.urlTpl.replace('{id}', taskId);
        }
    }
});