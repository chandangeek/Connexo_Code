Ext.define('Mdc.view.setup.devicelogbooks.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceLogbooksGrid',
    store: 'Mdc.store.LogbooksOfDevice',
    router: null,
    overflowY: 'auto',
    requires: [
        'Uni.grid.column.Action',
        'Uni.grid.column.Obis',
        'Uni.grid.column.LastEventType',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.view.setup.devicelogbooks.ActionMenu'
    ],
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('devicelogbooks.logbook', 'MDC', 'Logbook'),
                dataIndex: 'name',
                renderer: function (value, metaData, record) {
                    var url = me.router.getRoute('devices/device/logbooks/logbookdata').buildUrl({logbookId: record.get('id')});
                    return '<a href="'+url+'">value</a>'.replace('url', url).replace('value', Ext.String.htmlEncode(value));
                },
                flex: 1
            },
            {
                xtype: 'obis-column',
                dataIndex: 'overruledObisCode',
                flex: 1
            },
            {
                xtype: 'last-event-type-column',
                dataIndex: 'lastEventType',
                flex: 1
            },
            {
                header: Uni.I18n.translate('devicelogbooks.timestampLastEvent', 'MDC', 'Timestamp last event'),
                dataIndex: 'lastEventDate',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '';
                },
                flex: 1
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('devicelogbooks.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} logbooks'),
                displayMoreMsg: Uni.I18n.translate('devicelogbooks.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} logbooks'),
                emptyMsg: Uni.I18n.translate('devicelogbooks.pagingtoolbartop.emptyMsg', 'MDC', 'There are no logbooks to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('devicelogbooks.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Logbooks per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});