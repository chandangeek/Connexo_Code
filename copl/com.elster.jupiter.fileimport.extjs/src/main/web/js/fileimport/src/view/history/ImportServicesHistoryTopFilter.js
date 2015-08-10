Ext.define('Fim.view.history.ImportServicesHistoryTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'fim-view-history-importservices-topfilter',

    store: 'Fim.store.ImportServicesHistory',
    includeServiceCombo: true,

    initComponent: function () {
        var me = this;

        me.filters = [
            {
                type: 'combobox',
                dataIndex: 'importService',
                emptyText: Uni.I18n.translate('general.importService', 'FIM', 'Import service'),
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: 'Fim.store.ImportServicesFilter',
                hidden: !me.includeServiceCombo
            },
            {
                type: 'interval',
                dataIndex: 'started',
                dataIndexFrom: 'startedOnFrom',
                dataIndexTo: 'startedOnTo',
                text: Uni.I18n.translate('importService.history.started', 'FIM', 'Started between')
            },
            {
                type: 'interval',
                dataIndex: 'finished',
                dataIndexFrom: 'finishedOnFrom',
                dataIndexTo: 'finishedOnTo',
                text: Uni.I18n.translate('importService.history.finished', 'FIM', 'Finished between')
            },
            {
                type: 'combobox',
                dataIndex: 'status',
                emptyText: Uni.I18n.translate('importService.history.status', 'FIM', 'Status'),
                multiSelect: true,
                displayField: 'display',
                valueField: 'value',
                store: 'Fim.store.Status'
            }
        ]
        me.callParent(arguments);
    }
});