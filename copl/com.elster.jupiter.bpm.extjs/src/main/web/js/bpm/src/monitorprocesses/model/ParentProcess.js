/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.monitorprocesses.model.ParentProcess', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id',
            type: 'string'
        },
        {
            name: 'processInstanceId',
            type: 'number'
        },
        {
            name: 'processId',
            type: 'string'
        },
        {
            name: 'processName',
            type: 'string'
        }
    ],
    proxy: {
        type: 'rest',
        urlTpl: '../../api/bpm/runtime/process/instance/{processId}/parent',
        reader: {
            type: 'json'
        },
        setUrl: function (processId) {
            this.url = this.urlTpl.replace('{processId}', encodeURIComponent(processId));
        }
    }
});