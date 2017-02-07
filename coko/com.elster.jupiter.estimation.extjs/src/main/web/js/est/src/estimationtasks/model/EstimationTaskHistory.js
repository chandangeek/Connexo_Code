Ext.define('Est.estimationtasks.model.EstimationTaskHistory', {
    extend: 'Ext.data.Model',
    requires: [],
    fields: [
        {name: 'task', persist: false},
        {name: 'id', type: 'number'},
        {name: 'startedOn', type: 'number'},
        {name: 'finishedOn', type: 'number'},
        {name: 'duration', type: 'number'},
        {name: 'status', type: 'string'},
        {name: 'reason', type: 'string'},
        {name: 'statusDate', type: 'number'},
        {name: 'statusPrefix', type: 'string'},
        {
            name: 'deviceGroup',
            persist: false,
            mapping: function (data) {

                if (data.task.deviceGroup && data.task.deviceGroup.name) {
                    return data.task.deviceGroup.name;
                } else {
                    return '-';
                }

            }
        },
        {
            name: 'usagePointGroup',
            persist: false,
            mapping: function (data) {

                if (data.task.usagePointGroup && data.task.usagePointGroup.displayValue) {
                    return data.task.usagePointGroup.displayValue;
                } else {
                    return '-';
                }

            }
        },
        {
            name: 'name',
            persist: false,
            mapping: function (data) {
                return data.task.name;
            }
        },
        {
            name: 'logLevel',
            persist: false,
            mapping: function (data) {
                return data.task.logLevel;
            }
        },
        {
            name: 'statusOnDate',
            persist: false,
            mapping: function (data) {
                if (data.statusDate && (data.statusDate !== 0)) {
                    return data.statusPrefix + ' ' + Uni.DateTime.formatDateTimeLong(new Date(data.statusDate));
                }
                return data.statusPrefix;
            }
        },
        {
            name: 'startedOn_formatted',
            persist: false,
            mapping: function (data) {
                if (data.startedOn && (data.startedOn !== 0)) {
                    return Uni.DateTime.formatDateTimeLong(new Date(data.startedOn));
                }
                return '-';
            }
        },
        {
            name: 'finishedOn_formatted',
            persist: false,
            mapping: function (data) {
                if (data.finishedOn && (data.finishedOn !== 0)) {
                    return Uni.DateTime.formatDateTimeLong(new Date(data.finishedOn));
                }
                return '-';
            }
        },
        {
            name: 'period_name',
            persist: false,
            mapping: function (data) {
                if (data.task.period && data.task.period.name) {
                    return data.task.period.name;
                } else {
                    return 'All';
                }
            }
        }
    ]
});