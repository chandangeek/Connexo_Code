Ext.define('Mdc.view.setup.devicelogbooks.DataTableView', {
    extend: 'Uni.view.container.PreviewContainer',
    alias: 'widget.deviceLogbookDataTableView',
    itemId: 'deviceLogbookDataTableView',
    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.view.setup.devicelogbooks.DataGrid',
        'Mdc.view.setup.devicelogbooks.DataPreview'
    ],

    grid: {
        xtype: 'deviceLogbookDataGrid'
    },

    emptyComponent: {
        xtype: 'no-items-found-panel',
        title: Uni.I18n.translate('devicelogbooks.dataTableView.empty.title', 'MDC', 'No events found'),
        reasons: [
            Uni.I18n.translate('devicelogbooks.dataTableView.empty.list.item1', 'MDC', 'No events have been defined yet.'),
            Uni.I18n.translate('devicelogbooks.dataTableView.empty.list.item2', 'MDC', 'No events comply to the filter.')
        ]
    },

    previewComponent: {
        xtype: 'deviceLogbookDataPreview'
    }
});