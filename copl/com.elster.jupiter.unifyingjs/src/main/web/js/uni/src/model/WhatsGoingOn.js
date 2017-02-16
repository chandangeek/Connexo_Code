/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.model.WhatsGoingOn', {
    extend: 'Ext.data.Model',
    idProperty: 'internalId',
    fields: [
        'type',
        'issueType',
        'id',
        'description',
        {
            name: 'due date',
            type: 'date'
        },
        'severity',
        'userAssignee',
        'workGroupAssignee',
        'isMyWorkGroup',
        'userAssigneeIsCurrentUser',
        'reference',
        'status',
        {
            name: 'internalId',
            mapping: function(record){
                return record.type + '_' + record.id;
            }
        },
        {
            name: 'displayValue',
            mapping: function(record){
                return {
                    type: record.type,
                    issueType: record.issueType,
                    id: record.id,
                    reference : record.reference,
                    description: record.description,
                    dueDate: record.dueDate,
                    severity: record.severity,
                    userAssignee: record.userAssignee,
                    isMyWorkGroup: record.isMyWorkGroup,
                    workGroupAssignee: record.workGroupAssignee,
                    status: record.status,
                    userAssigneeIsCurrentUser: record.userAssigneeIsCurrentUser
                }
            }
        }

    ]
});