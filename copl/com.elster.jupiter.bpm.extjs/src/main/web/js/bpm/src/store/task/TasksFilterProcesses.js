/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.store.task.TasksFilterProcesses', {
    extend: 'Ext.data.Store',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/bpm/runtime/availableactiveprocesses',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'processes'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    },

    fields: [
        {name: 'name',  type: 'string'},
        {name: 'processId', type: 'string'},
        {name: 'version',  type: 'string'},
        {name: 'deploymentId', type: 'string'},
        {
            name: 'displayName',
            type: 'string',
            convert: function (value, record) {
                return Ext.String.format('{0} ({1})', record.get('name'), record.get('version'));
            }
        },
        {
            name: 'fullName',
            type: 'string',
            convert: function (value, record) {
                return Ext.String.format('{0} ({1}) ({2})', record.get('processId'), record.get('version'), record.get('deploymentId'));
            }
        }
    ]
});
