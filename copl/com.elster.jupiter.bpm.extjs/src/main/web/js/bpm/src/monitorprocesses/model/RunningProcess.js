/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.monitorprocesses.model.RunningProcess', {
    extend: 'Ext.data.Model',
    requires: [
        'Bpm.monitorprocesses.model.RunningProcessOpenTask'
    ],
    fields: [
        {
            name: 'processId',
            type: 'string'
        },
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'startDate',
            type: 'number'
        },
        {
            name: 'startDateDisplay',
            type: 'string',
            convert: function (value, record) {
                return record.get('startDate') ? Uni.DateTime.formatDateTimeShort(new Date(record.get('startDate'))) : '-';
            }
        },
        {
            name: 'status',
            type: 'number'
        },
        {
            name: 'statusDisplay',
            type: 'string',
            convert: function (value, record) {
                switch (record.get('status')) {
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
                        return value;
                }
            }
        },
        {
            name: 'startedBy',
            type: 'string'
        },
        {
            name: 'version',
            type: 'string'
        },
        {
            name: 'openTasks'
        }
    ],
    associations: [
        {
            type: 'hasMany',
            model: 'Bpm.monitorprocesses.model.RunningProcessOpenTask',
            associationKey: 'openTasks',
            name: 'openTasks'
        }
    ]
});