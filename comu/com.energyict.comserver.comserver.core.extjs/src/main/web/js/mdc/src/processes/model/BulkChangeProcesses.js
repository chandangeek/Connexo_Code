/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.processes.model.BulkChangeProcesses', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'operation', type: 'string'},
        {name: 'status', type: 'string'},
        {name: 'comment', type: 'string'},
        {name: 'assignee', type: 'auto'}
    ],

    hasMany: {model: 'Mdc.processes.model.ProcessGeneralModel', name: 'processes'}
});
