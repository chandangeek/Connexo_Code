/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.processes.model.ProcessGeneralModel', {
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
            name: 'variableId',
            type: 'string'
        },

        {
            name: 'type',
            type: 'string',
            convert: function (value, record) {
                var tmptype = record.get('variableId');
                if (tmptype == "deviceId")
                {
                    return "Device"
                }
                if (tmptype == "usagePointId")
                {
                	return "UsagePoint"
                }
                if (tmptype == "alarmId")
                {
                    return "Alarm"
                }
                if (tmptype == "issueId")
                {
                    return "Issue"
                }

                return "-";
            }

        },
        {
            name: 'objectName',
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
            name: 'endDate',
            type: 'number'
        },
        {
            name: 'endDateDisplay',
            type: 'string',
            convert: function (value, record) {
                return record.get('endDate') ? Uni.DateTime.formatDateTimeShort(new Date(record.get('endDate'))) : '-';
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
                        return Uni.I18n.translate('imt.processstatus.pending', 'IMT', 'Pending');
                    case 1:
                        return Uni.I18n.translate('imt.processstatus.active', 'IMT', 'Active');
                    case 2:
                        return Uni.I18n.translate('imt.processstatus.completed', 'IMT', 'Completed');
                    case 3:
                        return Uni.I18n.translate('imt.processstatus.cancelled', 'IMT', 'Cancelled');
                    case 4:
                        return Uni.I18n.translate('imt.processstatus.ongoing', 'IMT', 'Ongoing');
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
            name: 'duration',
            type: 'number'
        },
        {
            name: 'version',
            type: 'string'
        },
        {
            name: 'value', //mrID for device and usagePoint or Id for alarm and issue
            type: 'string'
        },
        {
            name: 'corrDeviceName',
            type: 'string'
        },
        {
            name: 'issueType',
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