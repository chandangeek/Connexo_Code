/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.monitorprocesses.model.ExtendedProcessNodes', {
    extend: 'Ext.data.Model',
    requires: [
        'Bpm.monitorprocesses.model.ProcessNodeVariable',
        'Bpm.monitorprocesses.model.ExtendedProcessNode',
        'Bpm.monitorprocesses.model.ProcessNode'
    ],
    fields: [
        {
            name: 'processInstanceStatus'
        },
        {
            name: 'list'
        }
    ],
    associations: [
        {
            type: 'hasMany',
            model: 'Bpm.monitorprocesses.model.ExtendedProcessNode',
            associationKey: 'list',
            name: 'list'
        }
    ],
    proxy: {
        type: 'rest',
        urlTpl: '../../api/bpm/runtime/process/instance/{processId}/nodeswithsubprocessinfo',
        reader: {
            type: 'json'
        },
        setUrl: function (processId) {
            this.url = this.urlTpl.replace('{processId}', encodeURIComponent(processId));
        }
    }
});