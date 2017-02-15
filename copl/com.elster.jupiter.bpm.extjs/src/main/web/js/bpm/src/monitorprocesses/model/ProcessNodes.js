/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.monitorprocesses.model.ProcessNodes', {
    extend: 'Ext.data.Model',
    requires: [
        'Bpm.monitorprocesses.model.ProcessNodeVariable',
        'Bpm.monitorprocesses.model.ProcessNode'
    ],
    fields: [
        {
            name: 'processInstanceStatus'
        },
        {
            name: 'processInstanceNodes'
        }
    ],
    associations: [
        {
            type: 'hasMany',
            model: 'Bpm.monitorprocesses.model.ProcessNode',
            associationKey: 'processInstanceNodes',
            name: 'processInstanceNodes'
        }
    ],
    proxy: {
        type: 'rest',
        urlTpl: '../../api/bpm/runtime/process/instance/{processId}/nodes',
        reader: {
            type: 'json'
        },
        setUrl: function (processId) {
            this.url = this.urlTpl.replace('{processId}', encodeURIComponent(processId));
        }
    }
});