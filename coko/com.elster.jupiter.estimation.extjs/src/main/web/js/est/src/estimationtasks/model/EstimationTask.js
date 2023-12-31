/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.estimationtasks.model.EstimationTask', {
    extend: 'Uni.model.Version',
    requires: ['Est.estimationtasks.model.TaskInfo'],
    fields: [
        'previousRecurrentTasks', 'nextRecurrentTasks',
        {name: 'id', type: 'number', useNull: true},
        {name: 'active', type: 'boolean'},
        {name: 'revalidate', type: 'boolean', defaultValue: false},
        {name: 'name', type: 'string'},
        {name: 'application', type: 'string'},
        {name: 'schedule', type: 'auto', defaultValue: null},
        {name: 'recurrence', type: 'auto'},
        {name: 'period', type: 'auto', defaultValue: null},
        {name: 'deviceGroup', type: 'auto', defaultValue: null},
        {name: 'usagePointGroup', type: 'auto', defaultValue: null},
        {name: 'metrologyPurpose', type: 'auto', defaultValue: null},
        {name: 'lastEstimationOccurrence', type: 'auto', defaultValue: null},
        {name: 'nextRun', type: 'number', useNull: true},
        {name: 'lastRun', type: 'number', useNull: true},
        {name: 'suspendUntilTime', type:'number', useNull: true},
        {
            name: 'logLevel',
            type: 'int',
            useNull: true
        },
        {
            name: 'deviceGroup_name',
            persist: false,
            mapping: function (data) {
                if (data.deviceGroup && data.deviceGroup.name) {
                    return data.deviceGroup.name;
                } else {
                    return '-'
                }
            }
        },
        {
            name: 'usagePointGroup_name',
            persist: false,
            mapping: function (data) {
                if (data.usagePointGroup && data.usagePointGroup.displayValue) {
                    return data.usagePointGroup.displayValue;
                } else {
                    return '-'
                }
            }
        },
        {
            name: 'metrologyPurpose_name',
            persist: false,
            mapping: function (data) {
                if (data.metrologyPurpose && data.metrologyPurpose.displayValue) {
                    return data.metrologyPurpose.displayValue;
                } else {
                    return '-'
                }
            }
        },
        {
            name: 'status',
            persist: false,
            mapping: function (data) {
                if (data.lastEstimationOccurrence && data.lastEstimationOccurrence.status) {
                    return data.lastEstimationOccurrence.status;
                } else {
                    return Uni.I18n.translate('estimationtasks.general.created', 'EST', 'Created');
                }
            }
        },
        {
            name: 'status_formatted',
            persist: false,
            mapping: function (data) {
                if (data.lastEstimationOccurrence) {
                    if (data.lastEstimationOccurrence.statusPrefix && data.lastEstimationOccurrence.statusDate && data.lastEstimationOccurrence.statusDate != 0) {
                        return data.lastEstimationOccurrence.statusPrefix + ' '
                            + Uni.DateTime.formatDateTimeShort(new Date(data.lastEstimationOccurrence.statusDate));
                    } else {
                        return data.lastEstimationOccurrence.status;
                    }
                } else {
                    return Uni.I18n.translate('estimationtasks.general.created', 'EST', 'Created');
                }
            }
        },
        {
            name: 'lastRun_formatted_long',
            persist: false,
            mapping: function (data) {
                var result;
                if (data.lastEstimationOccurrence && (data.lastEstimationOccurrence.lastRun !== 0)) {
                    result = Uni.DateTime.formatDateTimeLong(new Date(data.lastEstimationOccurrence.lastRun));
                } else {
                    result = '-'
                }
                return result;
            }
        },
        {
            name: 'nextRun_formatted_long',
            persist: false,
            mapping: function (data) {
                var result;
                if (data.nextRun && (data.nextRun !== 0)) {
                    result = Uni.DateTime.formatDateTimeLong(new Date(data.nextRun));
                } else {
                    result = Uni.I18n.translate('estimationtasks.general.notScheduled', 'EST', 'Not scheduled')
                }
                return result;
            }
        },
        {
            name: 'nextRun_formatted_short',
            persist: false,
            mapping: function (data) {
                var result;
                if (data.nextRun && (data.nextRun !== 0)) {
                    result = Uni.DateTime.formatDateTimeShort(new Date(data.nextRun));
                } else {
                    result = Uni.I18n.translate('estimationtasks.general.notScheduled', 'EST', 'Not scheduled')
                }
                return result;
            }
        },
        {
            name: 'startedOn_formatted_long',
            persist: false,
            mapping: function (data) {
                if (data.lastEstimationOccurrence && data.lastEstimationOccurrence.startedOn) {
                    return Uni.DateTime.formatDateTimeLong(new Date(data.lastEstimationOccurrence.startedOn));
                } else {
                    return '-';
                }
            }
        },
        {
            name: 'finishedOn_formatted_long',
            persist: false,
            mapping: function (data) {
                if (data.lastEstimationOccurrence && data.lastEstimationOccurrence.finishedOn) {
                    return Uni.DateTime.formatDateTimeLong(new Date(data.lastEstimationOccurrence.finishedOn));
                } else {
                    return '-';
                }
            }
        },
        {
            name: 'duration',
            persist: false,
            mapping: function (data) {
                if (data.lastEstimationOccurrence && data.lastEstimationOccurrence.duration) {
                    return data.lastEstimationOccurrence.duration;
                } else {
                    return '-';
                }
            }
        },
        {
            name: 'period_name',
            persist: false,
            mapping: function (data) {
                if (data.period && data.period.name) {
                    return data.period.name;
                } else {
                    return Uni.I18n.translate('estimationtasks.general.all', 'EST', 'All');
                }
            }
        }
    ],

    getTriggerText: function () {
        var me = this,
            nextRun = me.get('nextRun');

        return Ext.isEmpty(me.get('schedule'))
            ? Uni.I18n.translate('estimation.schedule.manual', 'EST', 'On request')
            : Uni.I18n.translate('estimation.schedule.scheduled', 'EST', '{0}. Next run {1}', [
            me.get('recurrence'),
            nextRun ? Uni.DateTime.formatDateTimeLong(Ext.isDate(nextRun) ? nextRun : new Date(nextRun)) : '-'
        ]);
    },
    associations: [
        {
            name: 'previousRecurrentTasks',
            type: 'hasMany',
            model: 'Cfg.model.TaskInfo',
            associationKey: 'previousRecurrentTasks',
            foreignKey: 'previousRecurrentTasks',
            getTypeDiscriminator: function (node) {
                return 'Cfg.model.TaskInfo';
            }
        },
        {
            name: 'nextRecurrentTasks',
            type: 'hasMany',
            model: 'Cfg.model.TaskInfo',
            associationKey: 'nextRecurrentTasks',
            foreignKey: 'nextRecurrentTasks',
            getTypeDiscriminator: function (node) {
                return 'Cfg.model.TaskInfo';
            }
        },

    ],
    proxy: {
        type: 'rest',
        url: '/api/est/estimation/tasks',
        timeout: 240000,
        reader: {
            type: 'json'
        }
    }
});
