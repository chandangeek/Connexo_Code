Ext.define('Cfg.model.ValidationTask', {
    extend: 'Ext.data.Model',
   /* requires: [
        'Uni.property.model.Property'
    ],*/
    fields: [
			'id', 'name', 'deviceGroup', 'schedule', 'nextRun', 'lastRun',
	
  //      'id', 'name', 'deviceGroup', 'dataProcessor', 'schedule', 'exportperiod', 'properties', 'readingTypes', 'nextRun', 'lastRun',
  /*      {
            name: 'lastExportOccurence',
            persist: false
        },
    */    {
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
                if (data.lastExportOccurence && data.lastExportOccurence.status) {
                    return data.lastExportOccurence.status;
                } else {
                    return Uni.I18n.translate('validationTasks.general.notPerformed', 'CFG', 'Not performed yet');
                }
            }
        },
        {
            name: 'statusOnDate',
            persist: false,
            mapping: function (data) {
                if (data.lastExportOccurence && data.lastExportOccurence.statusDate && data.lastExportOccurence.statusDate != 0) {
                    return data.lastExportOccurence.statusPrefix + ' ' + moment(data.lastExportOccurence.statusDate).format('ddd, DD MMM YYYY HH:mm:ss');
                } else if (data.lastExportOccurence) {
                    return data.lastExportOccurence.statusPrefix
                } else {
                    return Uni.I18n.translate('validationTasks.general.notPerformed', 'CFG', 'Not performed yet');
                }
            }
        },
        {
            name: 'reason',
            persist: false,
            mapping: function (data) {
                if (data.lastExportOccurence && data.lastExportOccurence.reason) {
                    return data.lastExportOccurence.reason;
                } else {
                    return '';
                }
            }
        },
 /*       {
            name: 'trigger',
            persist: false,
            mapping: function (data) {
                if (data.lastExportOccurence && data.lastExportOccurence.trigger) {
                    return data.lastExportOccurence.trigger;
                } else {
                    return '-'
                }
            }
        },
   */     {
            name: 'startedOn',
            persist: false,
            mapping: function (data) {
                if (data.lastExportOccurence && data.lastExportOccurence.startedOn) {
                    return Uni.DateTime.formatDateTimeLong(new Date(data.lastExportOccurence.startedOn));
                } else {
                    return '-';
                }
            }
        },
        {
            name: 'finishedOn',
            persist: false,
            mapping: function (data) {
                if (data.lastExportOccurence && data.lastExportOccurence.finishedOn) {
                    return Uni.DateTime.formatDateTimeLong(new Date(data.lastExportOccurence.finishedOn));
                } else {
                    return '-';
                }
            }
        },
        {
            name: 'duration',
            persist: false,
            mapping: function (data) {
                if (data.lastExportOccurence && data.lastExportOccurence.duration) {
                    return data.lastExportOccurence.duration;
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