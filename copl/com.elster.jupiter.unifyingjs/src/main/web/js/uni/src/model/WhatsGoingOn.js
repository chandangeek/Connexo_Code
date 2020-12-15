/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.model.WhatsGoingOn', {
    extend: 'Ext.data.Model',
    idProperty: 'internalId',
    fields: [
        'type',
        'issueType',
        'reason',
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
        {
            name: 'status',
            type: 'string',
            convert: function (value, record) {
                switch (value) {
                    case '0':
                        return Uni.I18n.translate('whatsGoingOn.status.pending', 'UNI', 'Pending');
                    case '1':
                        return Uni.I18n.translate('whatsGoingOn.status.active', 'UNI', 'Active');
                    case '2':
                        return Uni.I18n.translate('whatsGoingOn.status.completed', 'UNI', 'Completed');
                    case '3':
                        return Uni.I18n.translate('whatsGoingOn.status.cancelled', 'UNI', 'Cancelled');
                    case '4':
                        return Uni.I18n.translate('whatsGoingOn.status.ongoing', 'UNI', 'Ongoing');
                    default :
                        return value;
                }
            }
        },
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
                    reason: record.reason,
                    id: record.id,
                    reference : record.reference,
                    description: record.description,
                    dueDate: record.dueDate,
                    severity: record.severity,
                    userAssignee: record.userAssignee,
                    isMyWorkGroup: record.isMyWorkGroup,
                    workGroupAssignee: record.workGroupAssignee,
                    status: record.status,
                    userAssigneeIsCurrentUser: record.userAssigneeIsCurrentUser,
                    userTaskInfo : record.userTaskInfo
                }
            }
        },
        'userTaskInfo'
    ]
});