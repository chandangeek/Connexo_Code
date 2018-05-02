/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.model.CustomTask', {
    extend: 'Uni.model.Version',
    requires: [
        'Apr.store.DaysWeeksMonths',
        'Apr.model.TaskProperties'
    ],

    fields: [
        'id', 'name', 'previousRecurrentTasks', 'nextRecurrentTasks', 'type', //'properties',
        {name: 'logLevel', type: 'int', useNull: true},
        {name: 'schedule', type: 'auto'},
        {name: 'recurrence', type: 'auto'},
        {name: 'nextRun', defaultValue: null},
        {name: 'lastRun', defaultValue: null},
        {name: 'schedule', defaultValue: null},
        {
            name: 'lastRun_formatted',
            persist: false,
            mapping: function (data) {
                var result;
                var lastRun = data.lastOccurrence ? data.lastOccurrence.lastRun : null;
                if (lastRun && (lastRun !== 0)) {
                    var lastRunFormatted = Uni.DateTime.formatDateTimeLong(Ext.isDate(lastRun) ? lastRun : new Date(lastRun));
                    result = data.lastOccurrence && data.lastOccurrence.wasScheduled
                        ? Uni.I18n.translate('customTask.general.lastRunBySchedule', 'APR', '{0} by schedule', [lastRunFormatted], false)
                        : Uni.I18n.translate('customTask.general.lastRunOnRequest', 'APR', '{0} on request', [lastRunFormatted], false);
                } else {
                    result = '-';
                }
                return result;
            }
        },
        {
            name: 'nextRun_formatted',
            persist: false,
            mapping: function (data) {
                var result;
                if (data.nextRun && (data.nextRun !== 0)) {
                    result = moment(data.nextRun).format('ddd, DD MMM YYYY HH:mm:ss');
                    result = Uni.DateTime.formatDateTimeLong(new Date(data.nextRun));
                } else {
                    result = Uni.I18n.translate('customTask.general.notScheduled', 'APR', 'Not scheduled')
                }
                return result;
            }
        },
        {
            name: 'status',
            persist: false,
            mapping: function (data) {
                if (data.lastOccurrence && data.lastOccurrence.status) {
                    return data.lastOccurrence.status;
                } else {
                    return Uni.I18n.translate('customTask.general.created', 'APR', 'Created');
                }
            }
        },
        {
            name: 'statusType',
            persist: false,
            mapping: function (data) {
                if (data.lastOccurrence && data.lastOccurrence.statusType) {
                    return data.lastOccurrence.statusType;
                } else {
                    return 'NOT_PERFORMED';
                }
            }
        },
        {
            name: 'statusOnDate',
            persist: false,
            mapping: function (data) {
                if (data.lastOccurrence && data.lastOccurrence.statusDate && data.lastOccurrence.statusDate != 0) {
                    return data.lastOccurrence.statusPrefix + ' ' + Uni.DateTime.formatDateTimeLong(new Date(data.lastOccurrence.statusDate));
                } else if (data.lastOccurrence) {
                    return data.lastOccurrence.statusPrefix
                } else {
                    return Uni.I18n.translate('customTask.general.created', 'APR', 'Created');
                }
            }
        },
        {
            name: 'reason',
            persist: false,
            mapping: function (data) {
                if (data.lastOccurrence && data.lastOccurrence.reason) {
                    return data.lastOccurrence.reason;
                } else {
                    return '';
                }
            }
        },
        {
            name: 'startedOn',
            persist: false,
            mapping: function (data) {
                if (data.lastOccurrence && data.lastOccurrence.startedOn) {
                    return Uni.DateTime.formatDateTimeLong(new Date(data.lastOccurrence.startedOn));
                } else {
                    return '-';
                }
            }
        },
        {
            name: 'finishedOn',
            persist: false,
            mapping: function (data) {
                if (data.lastOccurrence && data.lastOccurrence.finishedOn) {
                    return Uni.DateTime.formatDateTimeLong(new Date(data.lastOccurrence.finishedOn));
                } else {
                    return '-';
                }
            }
        },
        {
            name: 'duration',
            persist: false,
            mapping: function (data) {
                if (data.lastOccurrence && data.lastOccurrence.duration) {
                    return data.lastOccurrence.duration;
                } else {
                    return '-';
                }
            }
        }
    ],

    getTriggerText: function () {
        var me = this,
            nextRun = me.get('nextRun');

        return Ext.isEmpty(me.get('schedule'))
            ? Uni.I18n.translate('customTask.schedule.manual', 'APR', 'On request')
            : Uni.I18n.translate('customTask.schedule.scheduled', 'APR', '{0}. Next run {1}', [
                me.get('recurrence'),
                nextRun ? Uni.DateTime.formatDateTimeLong(Ext.isDate(nextRun) ? nextRun : new Date(nextRun)) : '-'
            ]);
    },

    associations: [
        {
            name: 'previousRecurrentTasks',
            type: 'hasMany',
            model: 'Apr.model.TaskInfo',
            associationKey: 'previousRecurrentTasks',
            foreignKey: 'previousRecurrentTasks',
            getTypeDiscriminator: function (node) {
                return 'Apr.model.TaskInfo';
            }
        },
        {
            name: 'nextRecurrentTasks',
            type: 'hasMany',
            model: 'Apr.model.TaskInfo',
            associationKey: 'nextRecurrentTasks',
            foreignKey: 'nextRecurrentTasks',
            getTypeDiscriminator: function (node) {
                return 'Apr.model.TaskInfo';
            }
        },
        {
            name: 'properties',
            type: 'hasMany',
            model: 'Apr.model.TaskProperties',
            associationKey: 'properties',
            foreignKey: 'properties',
            getterName: 'getProperties',
            setterName: 'setProperties',
            getTypeDiscriminator: function (node) {
                return 'Apr.model.TaskProperties';
            }
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/ctk/customtask',
        reader: {
            type: 'json'
        }
    }
});
