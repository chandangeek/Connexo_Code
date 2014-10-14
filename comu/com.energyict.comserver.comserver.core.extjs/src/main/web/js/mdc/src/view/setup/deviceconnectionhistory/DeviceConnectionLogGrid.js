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
                itemId: 'timeStamp',
                text: Uni.I18n.translate('deviceconnectionhistory.timeStamp', 'MDC', 'Timestamp'),
                dataIndex: 'timeStamp',
                flex: 1,
                renderer: function (value, metadata) {
                    if (value !== null) {
                        return new Date(value).toLocaleString();
                    }
                }
            },
            {
                itemId: 'details',
                text: Uni.I18n.translate('deviceconnectionhistory.details', 'MDC', 'Details'),
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
//        me.dockedItems = [
//            {
//                xtype: 'pagingtoolbartop',
//                store: me.store,
//                dock: 'top',
//                displayMsg: Uni.I18n.translate('deviceconnectionhistory.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} connections'),
//                displayMoreMsg: Uni.I18n.translate('deviceconnectionhistory.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} connections'),
//                emptyMsg: Uni.I18n.translate('deviceconnectionhistory.pagingtoolbartop.emptyMsg', 'MDC', 'There are no connections to display')
//            },
//            {
//                xtype: 'pagingtoolbarbottom',
//                store: me.store,
////            params: [
////                {mrid: me.mrid}
////            ],
//                itemsPerPageMsg: Uni.I18n.translate('deviceconnectionmethod.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Connections per page'),
//                dock: 'bottom'
//            }
//        ];
        me.callParent();
    }

})
;
