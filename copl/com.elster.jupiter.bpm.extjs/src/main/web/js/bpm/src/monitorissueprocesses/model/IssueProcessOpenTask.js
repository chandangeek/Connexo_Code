/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.monitorissueprocesses.model.IssueProcessOpenTask', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id',
            type: 'number'
        },
        {
            name: 'name',
            type: 'string',
            convert: function(value, record){
                return (value == '') ? Uni.I18n.translate('bpm.process.noTaskName', 'BPM', 'No task name') : value;
            }
        },
        {
            name: 'taskLinkStyle',
            type: 'string',
            persist: false,
            convert: function (value, record) {
                return (!Bpm.privileges.BpmManagement.canExecute())?"pointer-events: none; cursor: default;text; text-decoration: none;":"";
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
                switch (record.get('status')){
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
            type: 'string',
            convert: function (value, record) {
                return (value == '')?Uni.I18n.translate('bpm.process.unassigned', 'BPM', 'Unassigned'):value;
            }
        }
    ]
});