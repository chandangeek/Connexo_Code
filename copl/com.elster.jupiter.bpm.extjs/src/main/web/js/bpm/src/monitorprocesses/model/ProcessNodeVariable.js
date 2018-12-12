/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.monitorprocesses.model.ProcessNodeVariable', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'variableName2',
            type: 'string'
        },
        {
            name: 'value2',
            type: 'string'
        },
        {
            name: 'logDate',
            type: 'number'
        },
        {
            name: 'logDateDisplay',
            type: 'string',
            convert: function (value, record) {
                return record.get('logDate') ? Uni.DateTime.formatDateTimeShort(new Date(record.get('logDate'))) : '-';
            }
        },
        {
            name: 'nodeInstanceId',
            type: 'number'
        }
    ]
});