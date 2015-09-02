Ext.define('Mdc.view.setup.deviceconnectionhistory.DeviceConnectionLogGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceConnectionLogGrid',
    itemId: 'deviceConnectionLogGrid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    store: 'DeviceConnectionLog',

    columns: {
        defaults: {
            sortable: false,
            menuDisabled: true
        },
        items: [
            {
                itemId: 'timestamp',
                text: Uni.I18n.translate('deviceconnectionhistory.timeStamp', 'MDC', 'Timestamp'),
                dataIndex: 'timestamp',
                flex: 1,
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '';
                }
            },
            {
                itemId: 'details',
                text: Uni.I18n.translate('deviceconnectionhistory.description', 'MDC', 'Description'),
                dataIndex: 'details',
                flex: 3
            },
            {
                itemId: 'logLevel',
                text: Uni.I18n.translate('deviceconnectionhistory.logLevel', 'MDC', 'Log level'),
                dataIndex: 'logLevel',
                flex: 1
            }
        ]
    },
    initComponent: function () {
        var me = this;
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                noBottomPaging: true,
                store: me.store,
                dock: 'top',
                displayMoreMsg: Uni.I18n.translate('deviceconnectionhistory.pagingtoolbartop.log.displayMoreMsg', 'MDC', '{0} - {2} of {2} log lines')
            }
        ];
        me.callParent();
    }

})
;
