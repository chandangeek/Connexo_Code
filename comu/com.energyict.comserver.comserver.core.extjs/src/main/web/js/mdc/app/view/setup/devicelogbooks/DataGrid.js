Ext.define('Mdc.view.setup.devicelogbooks.DataGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceLogbookDataGrid',
    itemId: 'deviceLogbookDataGrid',
    store: 'Mdc.store.LogbookOfDeviceData',
    router: null,
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    columns: [
        {
            header: Uni.I18n.translate('devicelogbooks.eventDate', 'MDC', 'Event date'),
            dataIndex: 'eventDate',
            renderer: function (value) {
                return value ? Uni.I18n.formatDate('devicelogbooks.eventDate.dateFormat', value, 'MDC', 'M d, Y H:i:s') : '';
            },
            flex: 1
        },
        {
            header: Uni.I18n.translate('devicelogbooks.domain', 'MDC', 'Domain'),
            dataIndex: 'domain',
            renderer: function (value) {
                return value ? value.name : '';
            },
            flex: 1
        },
        {
            header: Uni.I18n.translate('devicelogbooks.subDomain', 'MDC', 'Subdomain'),
            dataIndex: 'subDomain',
            renderer: function (value) {
                return value ? value.name : '';
            },
            flex: 1
        },
        {
            header: Uni.I18n.translate('devicelogbooks.eventOrAction', 'MDC', 'Event or action'),
            dataIndex: 'eventOrAction',
            renderer: function (value) {
                return value ? value.name : '';
            },
            flex: 1
        },
        {
            header: Uni.I18n.translate('devicelogbooks.message', 'MDC', 'Message'),
            dataIndex: 'message',
            flex: 1
        }
    ],
    initComponent: function () {
        var me = this;

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('devicelogbooks.dataGrid.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} events'),
                displayMoreMsg: Uni.I18n.translate('devicelogbooks.dataGrid.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} events'),
                emptyMsg: Uni.I18n.translate('devicelogbooks.dataGrid.pagingtoolbartop.emptyMsg', 'MDC', 'There are no events to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('devicelogbooks.dataGrid.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Events per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});