Ext.define('Dxp.model.DataExportTaskHistory', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.property.model.Property',
        'Dxp.model.DataExportTask'
    ],
    fields: [
        {name: 'task', persist: false},
        {name: 'id', type: 'number'},
        {name: 'startedOn', type: 'number'},
        {name: 'finishedOn', type: 'number'},
        {name: 'duration', type: 'number'},
        {name: 'status', type: 'string'},
        {name: 'reason', type: 'string'},
        {name: 'exportPeriodFrom', type: 'number'},
        {name: 'exportPeriodTo', type: 'number'},
        {name: 'statusDate', type: 'number'},
        {name: 'statusPrefix', type: 'string'},
        {name: 'trigger', type: 'string'},

        {
            name: 'properties',
            persist:false,
            mapping:  function (data) {
                return data.task.properties;
            }
        },

        {
            name: 'dataProcessor',
            persist:false,
            mapping:  function (data) {
                return data.task.dataProcessor;
            }
        },

        {
            name: 'exportperiod',
            persist:false,
            mapping:  function (data) {
                return data.task.standardDataSelector.exportPeriod;
            }
        },

        {
            name: 'readingTypes',
            persist:false,
            mapping:  function (data) {
                return data.task.standardDataSelector.readingTypes;
            }
        },

        {
            name: 'deviceGroup',
            persist:false,
            mapping:  function (data) {
                return data.task.standardDataSelector.deviceGroup;
            }
        },

        {
            name: 'name',
            persist:false,
            mapping:  function (data) {
                return data.task.name;
            }
        },

        {
            name: 'statusOnDate',
            persist: false,
            mapping: function (data) {
                if (data.statusDate && (data.statusDate !== 0)) {
                    return data.statusPrefix + ' ' + moment(data.statusDate).format('ddd, DD MMM YYYY HH:mm:ss');
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
            name: 'exportPeriodFrom_formatted',
            persist: false,
            mapping: function (data) {
                if (data.exportPeriodFrom && (data.exportPeriodFrom !== 0)) {
                    return moment(data.exportPeriodFrom).format('ddd, DD MMM YYYY HH:mm:ss');
                }
                return '-';
            }
        },
        {
            name: 'exportPeriodTo_formatted',
            persist: false,
            mapping: function (data) {
                if (data.exportPeriodTo && (data.exportPeriodTo !== 0)) {
                    return moment(data.exportPeriodTo).format('ddd, DD MMM YYYY HH:mm:ss');
                }
                return '-';
            }
        },
        {
            name: 'exportPeriod_range',
            persist: false,
            mapping: function (data) {
                if ((data.exportPeriodFrom && data.exportPeriodFrom !== 0) &&
                    (data.exportPeriodTo && data.exportPeriodTo !== 0)) {
                    return 'From ' + Uni.DateTime.formatDateTimeShort(new Date(data.exportPeriodFrom)) +
                        ' to ' + Uni.DateTime.formatDateTimeShort(new Date(data.exportPeriodTo));
                }
                return '-';
            }
        }
    ],

    associations: [
        {
            type: 'hasOne',
            model: 'Dxp.model.DataExportTask',
            associationKey: 'task',
            name: 'task',
            getterName: 'getTask'
        }
    ]

});