Ext.define('Mdc.view.setup.deviceevents.DataTableView', {
    extend: 'Uni.view.container.PreviewContainer',
    alias: 'widget.deviceLogbookDataTableView',
    device: null,
    router: null,
    eventsView: null,
    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.view.setup.deviceevents.DataGrid',
        'Mdc.view.setup.deviceevents.DataPreview'
    ],
    initComponent: function () {
        var me = this;
        me.grid = {
            xtype: 'deviceLogbookDataGrid'
        };
        me.emptyComponent = {
            xtype: 'no-items-found-panel',
            title: Uni.I18n.translate('deviceevents.dataTableView.empty.title', 'MDC', 'No events found'),
            reasons: [
                Uni.I18n.translate('deviceevents.dataTableView.empty.list.item1', 'MDC', 'No events have been defined yet.'),
                Uni.I18n.translate('deviceevents.dataTableView.empty.list.item2', 'MDC', 'No events comply to the filter.')
            ]
        };
        me.previewComponent = {
            xtype: 'deviceLogbookDataPreview',
            device: me.device,
            router: me.router,
            eventsView: me.eventsView
        };
        me.callParent(arguments)
    }

});