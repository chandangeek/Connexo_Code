Ext.define('Cfg.model.ValidationTask', {
    extend: 'Ext.data.Model',
    fields: [
			'id', 'name', 'deviceGroup', 'schedule', 'nextRun', 'lastRun',
	
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
                    return Uni.I18n.translate('validationTasks.general.notPerformed', 'CFG', 'Not performed yet');
                }
            }
        },
        {
            name: 'statusOnDate',
            persist: false,
            mapping: function (data) {
                if (data.lastValidationOccurence && data.lastValidationOccurence.statusDate && data.lastValidationOccurence.statusDate != 0) {
                    return data.lastValidationOccurence.statusPrefix + ' ' + moment(data.lastValidationOccurence.statusDate).format('ddd, DD MMM YYYY HH:mm:ss');
                } else if (data.lastValidationOccurence) {
                    return data.lastValidationOccurence.statusPrefix
                } else {
                    return Uni.I18n.translate('validationTasks.general.notPerformed', 'CFG', 'Not performed yet');
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
    proxy: {
        type: 'rest',
         url: '/api/val/validationtasks',
        reader: {
            type: 'json'
        }
    }
});