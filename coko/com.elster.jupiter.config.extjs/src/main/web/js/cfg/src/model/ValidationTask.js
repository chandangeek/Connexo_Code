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
            name: 'metrologyConfiguration',
            defaultValue: null
        },
        {
            name: 'metrologyContract',
            defaultValue: null
        },
        {name: 'schedule', type: 'auto'},
        {name: 'nextRun', type: 'date', dateFormat: 'time'},
        {name: 'lastRun', type: 'date', dateFormat: 'time'},
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
                if (data.lastRun && (data.lastRun !== 0)) {
                    result = moment(data.lastRun).format('ddd, DD MMM YYYY HH:mm:ss');
                } else {
                    result = '-'
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
            name: 'trigger',
            persist: false,
            mapping: function (data) {
                if (data.lastValidationOccurence && data.lastValidationOccurence.trigger) {
                    return data.lastValidationOccurence.trigger;
                } else {
                    return '-'
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
        var schedule = this.get('schedule'),
            periodsStore = Ext.getStore('Cfg.store.DaysWeeksMonths');

        return Ext.isEmpty(schedule)
            ? Uni.I18n.translate('validation.schedule.manual', 'CFG', 'On request')
            : Uni.I18n.translate('validation.schedule.scheduled', 'CFG', 'Every {0} {1}. Next run {2}', [
            schedule.count,
            periodsStore.findRecord('name', schedule.timeUnit).get('displayValue'),
            this.get('nextRun') ? Uni.DateTime.formatDateTimeLong(this.get('nextRun')) : '-'
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
