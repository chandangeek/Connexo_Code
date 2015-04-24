Ext.define('Est.estimationtasks.model.EstimationTask', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'active', type: 'boolean'},
        {name: 'name', type: 'string'},
        {name: 'schedule', type: 'auto'},
        {name: 'period', type: 'auto'},
        {name: 'deviceGroup', type: 'auto'},
        {name: 'lastEstimationOccurrence', type: 'auto'},
        {name: 'nextRun', type: 'number', useNull: true},
        {name: 'lastRun', type: 'number', useNull: true},
        {
            name: 'deviceGroup_name',
            persist: false,
            mapping: function (data) {
                var result;
                if (data.deviceGroup && data.deviceGroup.name) {
                    return data.deviceGroup.name;
                } else {
                    result = '-'
                }
                return result;
            }
        },
        {
            name: 'status',
            persist: false,
            mapping: function (data) {
                if (data.lastEstimationOccurrence && data.lastEstimationOccurrence.status) {
                    return data.lastEstimationOccurrence.status;
                } else {
                    return Uni.I18n.translate('estimationtasks.general.notPerformed', 'EST', 'Not performed yet');
                }
            }
        },
        {
            name: 'status_formatted',
            persist: false,
            mapping: function (data) {
                if (data.lastEstimationOccurrence && data.lastEstimationOccurrence.status) {
                    if (data.lastEstimationOccurrence.statusPrefix && data.lastEstimationOccurrence.statusDate) {
                        return data.lastEstimationOccurrence.status + ' '
                            + data.lastEstimationOccurrence.statusPrefix + ' '
                            + Uni.DateTime.formatDateTimeShort(new Date(data.lastEstimationOccurrence.statusDate));
                    } else {
                        return data.lastEstimationOccurrence.status;
                    }
                } else {
                    return Uni.I18n.translate('estimationtasks.general.notPerformed', 'EST', 'Not performed yet');
                }
            }
        },
        {
            name: 'lastRun_formatted_long',
            persist: false,
            mapping: function (data) {
                var result;
                if (data.lastRun && (data.lastRun !== 0)) {
                    result = Uni.DateTime.formatDateTimeLong(new Date(data.lastRun));
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
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/est/estimation/tasks',
//      url: '/apps/est/src/estimationtasks/fakedata/estimationtask.json',
//        url: '/apps/est/src/estimationtasks/fakedata/estimationtasksempty.json',
        timeout: 240000,
        reader: {
            type: 'json'
        }
    }
});
