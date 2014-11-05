Ext.define('Mdc.view.setup.devicecommunicationtaskhistory.DeviceCommunicationTaskHistoryLogGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceCommunicationTaskHistoryLogGrid',
    itemId: 'deviceCommunicationTaskHistoryLogGrid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    store: 'DeviceCommunicationTaskLog',
//    plugins: {
//        ptype: 'bufferedrenderer'
//    },
    columns: {
        defaults: {
            sortable: false,
            menuDisabled: true
        },
        items: [
            {
                itemId: 'timestamp',
                text: Uni.I18n.translate('devicecommunicationtaskhistory.timeStamp', 'MDC', 'Timestamp'),
                dataIndex: 'timestamp',
                flex: 1,
                renderer: function (value, metadata) {
                    if (value) {
                        return new Date(value).toLocaleString();
                    }
                }
            },
            {
                itemId: 'details',
                text: Uni.I18n.translate('devicecommunicationtaskhistory.details', 'MDC', 'Details'),
                dataIndex: 'details',
                flex: 3
            },
            {
                itemId: 'logLevel',
                text: Uni.I18n.translate('devicecommunicationtaskhistory.logLevel', 'MDC', 'Log level'),
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
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('devicecommunicationtaskhistory.pagingtoolbartop.log.displayMsg', 'MDC', '{0} - {1} of {2} log lines'),
                displayMoreMsg: Uni.I18n.translate('devicecommunicationtaskhistory.pagingtoolbartop.log.displayMoreMsg', 'MDC', '{0} - {1} of {2} log lines')
//                emptyMsg: Uni.I18n.translate('deviceconnectionhistory.pagingtoolbartop.log.emptyMsg', 'MDC', 'There are no log lines')
            }
        ];
        me.callParent();
    }

})
;
