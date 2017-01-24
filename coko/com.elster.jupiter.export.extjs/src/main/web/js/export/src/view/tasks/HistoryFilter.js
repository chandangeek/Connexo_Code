Ext.define('Dxp.view.tasks.HistoryFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'dxp-view-tasks-historyfilter',
    showExportTask: true,

    store: 'Dxp.store.DataExportTasksHistory',
    requires: [
        'Uni.form.filter.FilterCombobox'
    ],


    initComponent: function () {
        var me = this;
        // me.items = [
        //     {
        //         xtype: 'uni-filter-combo',
        //         labelAlign: 'top',
        //         itemId: 'cbo-status',
        //         name: 'status',
        //         fieldLabel: Uni.I18n.translate('general.status', 'FIM', 'Status'),
        //         displayField: 'display',
        //         valueField: 'value',
        //         store: 'Fim.store.Status'
        //     }
        // ];
        me.filters = [
            {
                type: 'interval',
                dataIndex: 'startedBetween',
                dataIndexFrom: 'startedOnFrom',
                dataIndexTo: 'startedOnTo',
                text: Uni.I18n.translate('tasks.historyFilter.startedBetween', 'DES', 'Started between')
            },
            {
                type: 'interval',
                dataIndex: 'finishedBetween',
                dataIndexFrom: 'finishedOnFrom',
                dataIndexTo: 'finishedOnTo',
                text: Uni.I18n.translate('tasks.historyFilter.finishedBetween', 'DES', 'Finished between')
            },
            {
                type: 'combobox',
                itemId: 'cbo-import-service',
                dataIndex: 'exportTask',
                emptyText: Uni.I18n.translate('general.exportTask', 'DES', 'Export task'),
                displayField: 'name',
                valueField: 'id',
                multiSelect: true,
                store: 'Dxp.store.DataExportTaskFilter',
                hidden: me.showExportTask
            },
            {
                type: 'combobox',
                dataIndex: 'status',
                emptyText: Uni.I18n.translate('general.status', 'DES', 'Status'),
                multiSelect: true,
                displayField: 'display',
                valueField: 'value',
                store: 'Dxp.store.Status'
            }
        ];
        me.callParent(arguments);
    }
});