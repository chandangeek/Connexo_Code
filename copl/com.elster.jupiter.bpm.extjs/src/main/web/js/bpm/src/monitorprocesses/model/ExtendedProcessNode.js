/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.monitorprocesses.model.ExtendedProcessNode', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'nodeInfo.name',
            mapping: 'nodeInfo.name',
            type: 'string'
        },
        {
            name: 'nodeInfo.type',
            mapping: 'nodeInfo.type',
            type: 'string'
        },
        {
            name: 'nodeInfo.status',
            mapping: 'nodeInfo.status',
            type: 'string'
        },
        {
            name: 'nodeInfo.logDate',
            mapping: 'nodeInfo.logDate',
            type: 'number'
        },
        {
            name: 'nodeInfo.logDateDisplay',
            type: 'string',
            convert: function (value, record) {
                return record.get('nodeInfo.logDate') ? Uni.DateTime.formatDateTimeLong(new Date(record.get('nodeInfo.logDate'))) : '-';
            }
        },
        {
            name: 'nodeInfo.nodeInstanceId',
            mapping: 'nodeInfo.nodeInstanceId',
            type: 'number'
        },
        {
            name: 'nodeInfo.processInstanceVariables'
        },
        {
            type: 'hasMany',
            model: 'Bpm.monitorprocesses.model.ProcessNodeVariable',
            associationKey: 'nodeInfo.processInstanceVariables',
            name: 'nodeInfo.processInstanceVariables'
        },   
        {
            name: 'childSubprocessLog.childProcessInstanceId',
            mapping: 'childSubprocessLog.childProcessInstanceId',
            type: 'number'
        },
        {
            name: 'childSubprocessLog.childProcessId',
            mapping: 'childSubprocessLog.childProcessId',
            type: 'string'
        },
        {
            name: 'childSubprocessLog.processName',
            mapping: 'childSubprocessLog.processName',
            type: 'string'
        },
        {
            name: 'childSubprocessLog.externalId',
            mapping: 'childSubprocessLog.externalId',
            type: 'string'
        }
    ]
});