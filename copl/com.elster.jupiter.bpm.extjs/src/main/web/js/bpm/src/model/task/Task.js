/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.model.task.Task', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id',
            type: 'number'
        },
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'processName',
            type: 'string'
        },
        {
            name: 'deploymentId',
            type: 'string'
        },
        {
            name: 'dueDate',
            type: 'number'
        },
        {
            name: 'dueDateParsed',
            type: 'number',
            convert: function (value, record) {
                return record.get('dueDate') == 0 ? null : record.get('dueDate');
            }
        },
        {
            name: 'dueDateDisplay',
            type: 'string',
            convert: function (value, record) {
                return record.get('dueDate') ? Uni.DateTime.formatDateTimeShort(new Date(record.get('dueDate'))) : '-';
            }
        },
        {
            name: 'createdOn',
            type: 'number'
        },
        {
            name: 'createdOnDisplay',
            type: 'string',
            convert: function (value, record) {
                return record.get('createdOn') ? Uni.DateTime.formatDateTimeShort(new Date(record.get('createdOn'))) : '-';
            }
        },
        {
            name: 'priority',
            type: 'number'
        },
        {
            name: 'priorityDisplay',
            type: 'string',
            convert: function (value, record) {
                var priority = record.get('priority');
                if (priority <= 3) {
                    return Uni.I18n.translate('bpm.task.priority.high', 'BPM', 'High');
                }
                else if (priority <= 7) {
                    return Uni.I18n.translate('bpm.task.priority.medium', 'BPM', 'Medium');
                }
                else {
                    return Uni.I18n.translate('bpm.task.priority.low', 'BPM', 'Low');
                }
                return '';
            }
        },
        {
            name: 'status',
            type: 'string'
        },
        {
            name: 'statusDisplay',
            type: 'string',
            convert: function (value, record) {
                switch (record.get('status')) {
                    case 'Created':
                        return Uni.I18n.translate('bpm.filter.createdStatus', 'BPM', 'Created');
                    case 'Ready':
                        return Uni.I18n.translate('bpm.filter.createdStatus', 'BPM', 'Created');
                    case 'Reserved':
                        return Uni.I18n.translate('bpm.filter.assignedStatus', 'BPM', 'Assigned');
                    case 'InProgress':
                        return Uni.I18n.translate('bpm.filter.inProgressStatus', 'BPM', 'Ongoing');
                    case 'Suspended':
                        return Uni.I18n.translate('bpm.filter.inProgressStatus', 'BPM', 'Ongoing');
                    case 'Completed':
                        return Uni.I18n.translate('bpm.filter.completedStatus', 'BPM', 'Completed');
                    case 'Failed':
                        return Uni.I18n.translate('bpm.filter.failedStatus', 'BPM', 'Failed');
                    case 'Error':
                        return Uni.I18n.translate('bpm.filter.failedStatus', 'BPM', 'Failed');
                    case 'Exited':
                        return Uni.I18n.translate('bpm.filter.cancelledStatus', 'BPM', 'Cancelled');
                    case 'Obsolete':
                        return Uni.I18n.translate('bpm.filter.cancelledStatus', 'BPM', 'Cancelled');
                    default:
                        return value;
                }
                return value;
            }
        },
        {
            name: 'actualOwner',
            type: 'string'
        },
        {
            name: 'userId'
        },
        {
            name: 'workgroup'
        },
        {
            name: 'workgroupId'
        },
        {
            name: 'actualOwnerDisplay',
            type: 'string',
            convert: function (value, record) {
                var actualOwner = record.get('actualOwner');
                if (actualOwner == null || actualOwner.length == 0){
                    return Uni.I18n.translate('bpm.task.unassignee', 'BPM', 'Unassigned');
                }
                return actualOwner;
            }
        },
        {
            name: 'processInstancesId',
            type: 'string'
        },
        {
            name: 'optLock',
            type: 'number'
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/bpm/runtime/tasks',
        reader: {
            type: 'json'
        }
    }
});