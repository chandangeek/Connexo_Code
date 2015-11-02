Ext.define('Apr.view.taskoverview.TaskFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'taskFilter',

    store: 'Apr.store.Tasks',

    filters: [
        {
            type: 'combobox',
            dataIndex: 'application',
            emptyText: Uni.I18n.translate('general.application', 'APR', 'Application'),
            multiSelect: true,
            displayField: 'application',
            valueField: 'application',
            store: 'Apr.store.Applications'
         //   hidden: !me.includeServiceCombo
        },
        {
            type: 'combobox',
            dataIndex: 'queue',
            emptyText: Uni.I18n.translate('general.queue', 'APR', 'Queue'),
            multiSelect: true,
            displayField: 'queue',
            valueField: 'queue',
            store: 'Apr.store.Applications'
            //   hidden: !me.includeServiceCombo
        },
        {
            type: 'interval',
            dataIndex: 'startedBetween',
            dataIndexFrom: 'startedOnFrom',
            dataIndexTo: 'startedOnTo',
            text: Uni.I18n.translate('validationtask.historyFilter.startedBetween', 'CFG', 'Started between')
        }
    ]
});
