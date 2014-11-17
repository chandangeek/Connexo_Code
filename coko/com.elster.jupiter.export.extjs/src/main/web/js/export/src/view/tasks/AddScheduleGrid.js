Ext.define('Dxp.view.tasks.AddScheduleGrid', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Dxp.model.SchedulePeriod'
    ],
    alias: 'widget.add-schedule-grid',
    store: Ext.create('Ext.data.Store', {
        model: 'Dxp.model.SchedulePeriod'
    }),
    bodyBorder: true,
    enableColumnHide: false,
    enableColumnMove: false,
    enableColumnResize: false,
    sortableColumns: false,
    collapsible: false,
    viewConfig:{
        markDirty:false
    },
    selModel: {
        mode: 'SINGLE'
    },
    columns: {
        items: [
            {
                itemId: 'schedule',
                header: Uni.I18n.translate('general.schedule', 'DES', 'Schedule'),
                dataIndex: 'schedule',
                flex: 1,
                renderer: function (value) {
                    return moment(value).format('ddd, DD MMM YYYY') + ' ' + moment(value).format('HH:mm:ss')
                }
            },
            {
                itemId: 'startPeriod',
                header: Uni.I18n.translate('general.startExportPeriod', 'DES', 'Start export period'),
                dataIndex: 'start',
                flex: 1,
                renderer: function (value) {
                    return moment(value).format('ddd, DD MMM YYYY') + ' ' + moment(value).format('HH:mm:ss')
                }
            },
            {
                itemId: 'endPeriod',
                header: Uni.I18n.translate('general.endExportPeriod', 'DES', 'End export period'),
                dataIndex: 'end',
                flex: 1,
                renderer: function (value) {
                    return moment(value).format('ddd, DD MMM YYYY') + ' ' + moment(value).format('HH:mm:ss')
                }
            }
        ]
    }
});
