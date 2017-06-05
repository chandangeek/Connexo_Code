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
        {
            name: 'status',
            type: 'string',
            mapping: function (record) {
                switch (record.status) {
                    case 0:
                        return Uni.I18n.translate('bpm.status.pending', 'BPM', 'Pending');
                    case 1:
                        return Uni.I18n.translate('bpm.status.active', 'BPM', 'Active');
                    case 2:
                        return Uni.I18n.translate('bpm.status.completed', 'BPM', 'Completed');
                    case 3:
                        return Uni.I18n.translate('bpm.status.cancelled', 'BPM', 'Cancelled');
                    case 4:
                        return Uni.I18n.translate('bpm.status.ongoing', 'BPM', 'Ongoing');
                    default :
                        return record.status;
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