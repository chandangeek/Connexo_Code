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
        defaults: {
            flex: 1,
            renderer: function (value) {
                return Uni.DateTime.formatDateTimeShort(new Date(value));
            }
        },
        items: [
            {
                itemId: 'schedule',
                header: Uni.I18n.translate('general.schedule', 'DES', 'Schedule'),
                dataIndex: 'schedule'
            },
            {
                itemId: 'startPeriod',
                header: Uni.I18n.translate('general.startExportPeriod', 'DES', 'Start export period'),
                dataIndex: 'start'
            },
            {
                itemId: 'endPeriod',
                header: Uni.I18n.translate('general.endExportPeriod', 'DES', 'End export period'),
                dataIndex: 'end'
            },
            {
                itemId: 'startUpdatePeriod',
                header: Uni.I18n.translate('general.startUpdatePeriod', 'DES', 'Start update period'),
                dataIndex: 'updateStart',
                hidden: true
            },
            {
                itemId: 'endUpdatePeriod',
                header: Uni.I18n.translate('general.endUpdatePeriod', 'DES', 'End update period'),
                dataIndex: 'updateEnd',
                hidden: true
            }
        ]
    }
});
