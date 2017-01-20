Ext.define('Cfg.model.ValidationTask', {
    extend: 'Uni.model.Version',
    requires: [
        'Cfg.store.DaysWeeksMonths'
    ],

    fields: [
			'id', 'name',
        {
            name: 'deviceGroup',
            defaultValue: null
        },
        {
            name: 'usagePointGroup',
            defaultValue: null
        },
        {
            name: 'metrologyPurpose',
            defaultValue: null
        },
        {name: 'schedule', type: 'auto'},
        {name: 'recurrence', type: 'auto'},
        {name: 'nextRun', defaultValue: null},
        {name: 'lastRun', defaultValue: null},
        {
            name: 'schedule',
            defaultValue: null
        },
        {
            name: 'lastValidationOccurence',
            persist: false
        },
        {
            name: 'lastRun_formatted',
            persist: false,
            mapping: function (data) {
                var result;
                var lastRun = data.lastRun;
                if (lastRun && (lastRun !== 0)) {
                    var lastRunFormatted = Uni.DateTime.formatDateTimeLong(Ext.isDate(lastRun) ? lastRun : new Date(lastRun));
                    result = data.lastValidationOccurence && data.lastValidationOccurence.wasScheduled
                        ? Uni.I18n.translate('validationTasks.general.lastRunBySchedule', 'CFG', '{0} by schedule', [lastRunFormatted], false)
                        : Uni.I18n.translate('validationTasks.general.lastRunOnRequest', 'CFG', '{0} on request', [lastRunFormatted], false);
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
                    result = Uni.I18n.translate('validationTasks.general.notScheduled', 'CFG', 'Not scheduled')
                }
                return result;
            }
        },
        {
            name: 'status',
            persist: false,
            mapping: function (data) {
                if (data.lastValidationOccurence && data.lastValidationOccurence.status) {
                    return data.lastValidationOccurence.status;
                } else {
                    return Uni.I18n.translate('validationTasks.general.created', 'CFG', 'Created');
                }
            }
        },
        {
            name: 'statusOnDate',
            persist: false,
            mapping: function (data) {
                if (data.lastValidationOccurence && data.lastValidationOccurence.statusDate && data.lastValidationOccurence.statusDate != 0) {
                    return data.lastValidationOccurence.statusPrefix + ' ' + Uni.DateTime.formatDateTimeLong(new Date(data.lastValidationOccurence.statusDate));
                } else if (data.lastValidationOccurence) {
                    return data.lastValidationOccurence.statusPrefix
                } else {
                    return Uni.I18n.translate('validationTasks.general.created', 'CFG', 'Created');
                }
            }
        },
        {
            name: 'reason',
            persist: false,
            mapping: function (data) {
                if (data.lastValidationOccurence && data.lastValidationOccurence.reason) {
                    return data.lastValidationOccurence.reason;
                } else {
                    return '';
                }
            }
        },
		{
            name: 'startedOn',
            persist: false,
            mapping: function (data) {
                if (data.lastValidationOccurence && data.lastValidationOccurence.startedOn) {
                    return Uni.DateTime.formatDateTimeLong(new Date(data.lastValidationOccurence.startedOn));
                } else {
                    return '-';
                }
            }
        },
        {
            name: 'finishedOn',
            persist: false,
            mapping: function (data) {
                if (data.lastValidationOccurence && data.lastValidationOccurence.finishedOn) {
                    return Uni.DateTime.formatDateTimeLong(new Date(data.lastValidationOccurence.finishedOn));
                } else {
                    return '-';
                }
            }
        },
        {
            name: 'duration',
            persist: false,
            mapping: function (data) {
                if (data.lastValidationOccurence && data.lastValidationOccurence.duration) {
                    return data.lastValidationOccurence.duration;
                } else {
                    return '-';
                }
            }
        }
    ],

    getTriggerText: function() {
        var me = this,
            nextRun = me.get('nextRun');

        return Ext.isEmpty(me.get('schedule'))
            ? Uni.I18n.translate('validation.schedule.manual', 'CFG', 'On request')
            : Uni.I18n.translate('validation.schedule.scheduled', 'CFG', '{1}. Next run {0}', [
            me.get('recurrence'),
            nextRun ? Uni.DateTime.formatDateTimeLong(Ext.isDate(nextRun) ? nextRun : new Date(nextRun)) : '-'
        ]);
    },

    proxy: {
        type: 'rest',
         url: '/api/val/validationtasks',
        reader: {
            type: 'json'
        }
    }
});
