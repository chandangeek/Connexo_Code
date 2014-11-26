Ext.define('Dxp.model.DataExportTaskHistory', {
    extend: 'Dxp.model.DataExportTask',
    requires: [
        'Uni.property.model.Property'
    ],
    fields: [
        {name: 'id', type: 'number'},
        {name: 'startedOn', type: 'number'},
        {name: 'finishedOn', type: 'number'},
        {name: 'duration', type: 'number'},
        {name: 'status', type: 'string'},
        {name: 'exportPeriodFrom', type: 'number'},
        {name: 'exportPeriodTo', type: 'number'},
        {
            name: 'startedOn_formatted',
            persist: false,
            mapping: function (data) {
                if (data.startedOn && (data.startedOn !== 0)) {
                    return moment(data.startedOn).format('ddd, DD MMM YYYY \\a\\t h:mm A');
                }
                return '-';
            }
        },
        {
            name: 'finishedOn_formatted',
            persist: false,
            mapping: function (data) {
                if (data.finishedOn && (data.finishedOn !== 0)) {
                    return moment(data.finishedOn).format('ddd, DD MMM YYYY \\a\\t h:mm A');
                }
                return '-';
            }
        },
        {
            name: 'exportPeriodFrom_formatted',
            persist: false,
            mapping: function (data) {
                if (data.exportPeriodFrom && (data.exportPeriodFrom !== 0)) {
                    return moment(data.exportPeriodFrom).format('ddd, DD MMM YYYY \\a\\t h:mm A');
                }
                return '-';
            }
        },
        {
            name: 'exportPeriodTo_formatted',
            persist: false,
            mapping: function (data) {
                if (data.exportPeriodTo && (data.exportPeriodTo !== 0)) {
                    return moment(data.exportPeriodTo).format('ddd, DD MMM YYYY \\a\\t h:mm A');
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
                    return 'From ' + moment(data.exportPeriodFrom).format('ddd, DD MMM YYYY HH:mm:ss') +
                        ' to ' + moment(data.exportPeriodTo).format('ddd, DD MMM YYYY HH:mm:ss');
                }
                return '-';
            }
        }
    ]
});