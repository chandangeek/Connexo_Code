/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.model.task.Assign', {
    extend: 'Ext.data.Model',
    proxy: {
        type: 'rest',
        urlTpl: '/api/bpm/runtime/tasks/{taskId}/{optLock}/assign',
        reader: {
            type: 'json'
        },
        setUrl: function (taskId, optLock) {
            this.url = this.urlTpl.replace('{taskId}', taskId).replace('{optLock}', optLock);
        }
    }
});