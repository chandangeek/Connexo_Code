Ext.define('Sam.view.datapurge.HistoryDetails', {
    extend: 'Sam.view.datapurge.SettingGrid',
    alias: 'widget.data-purge-history-details',
    ui: 'medium',
    title: '&nbsp;',
    padding: 0,
    store: 'Sam.store.DataPurgeHistoryCategories',
    plugins: [
        'showConditionalToolTip'
    ],
    showButtons: false
});