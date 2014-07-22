Ext.define('Mdc.view.setup.connectionmethod.DeviceConnectionMethodsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceConnectionMethodsGrid',
    overflowY: 'auto',
    itemId: 'deviceconnectionmethodsgrid',

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.ConnectionMethodsOfDevice',
        'Mdc.view.setup.deviceconnectionmethod.DeviceConnectionMethodActionMenu',
        'Uni.grid.column.DefaultColumn'
    ],

    store: 'ConnectionMethodsOfDevice',

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                xtype: 'default-column',
                dataIndex: 'isDefault',
                flex: 0.1
            },
            {
                header: Uni.I18n.translate('deviceconnectionmethod.name', 'MDC', 'Name'),
                dataIndex: 'name',
                flex: 0.3
            },
            {
                header: Uni.I18n.translate('deviceconnectionmethod.direction', 'MDC', 'Direction'),
                dataIndex: 'direction',
                flex: 0.2
            },
            {
                header: Uni.I18n.translate('deviceconnectionmethod.connectionType', 'MDC', 'Connection type'),
                dataIndex: 'connectionType',
                flex: 0.3
            },
            {
                header: Uni.I18n.translate('deviceconnectionmethod.status', 'MDC', 'Status'),
                dataIndex: 'status',
                renderer: function (value, b, record) {
                    switch (value) {
                        case 'connectionTaskStatusIncomplete':
                            return Uni.I18n.translate('deviceconnectionmethod.status.incomplete', 'MDC', 'Incomplete');
                        case 'connectionTaskStatusActive':
                            return Uni.I18n.translate('deviceconnectionmethod.status.active', 'MDC', 'Active');
                        case 'connectionTaskStatusInActive':
                            return Uni.I18n.translate('deviceconnectionmethod.status.inactive', 'MDC', 'Inactive');
                        default :
                            return '';
                    }
                },
                flex: 0.2
            },
            {
                xtype: 'uni-actioncolumn',
                items: 'Mdc.view.setup.deviceconnectionmethod.DeviceConnectionMethodActionMenu'
            }

        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('deviceconnectionmethod.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} connection methods'),
                displayMoreMsg: Uni.I18n.translate('deviceconnectionmethod.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} connection methods'),
                emptyMsg: Uni.I18n.translate('deviceconnectionmethod.pagingtoolbartop.emptyMsg', 'MDC', 'There are no connection methods to display'),
                items: [
                    {
                        xtype: 'component',
                        flex: 1
                    },
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('deviceconnectionmethod.addConnectionMethod', 'MDC', 'Add connection method'),
                        iconCls: 'x-uni-action-iconD',
                        menu: {
                            plain: true,
                            items: [
                                {
                                    text: Uni.I18n.translate('deviceconnectionmethod.addOutboundConnectionMethod', 'MDC', 'Add outbound connection method'),
                                    itemId: 'createDeviceOutboundConnectionButton',
                                    action: 'createDeviceOutboundConnectionMethod'
                                },
                                {
                                    text: Uni.I18n.translate('deviceconnectionmethod.addInboundConnectionMethod', 'MDC', 'Add inbound connection method'),
                                    itemId: 'createDeviceInboundConnectionButton',
                                    action: 'createDeviceInboundConnectionMethod'
                                }
                            ]

                        }
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                params: [
                    {mrid: me.mrid}
                ],
                itemsPerPageMsg: Uni.I18n.translate('deviceconnectionmethod.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Connection methods per page'),
                dock: 'bottom'
            }
        ];

        me.callParent();
    }
});


