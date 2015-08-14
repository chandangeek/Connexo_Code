Ext.define('Mdc.view.setup.deviceconnectionmethod.DeviceConnectionMethodsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceConnectionMethodsGrid',
    overflowY: 'auto',
    itemId: 'deviceconnectionmethodsgrid',

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.ConnectionMethodsOfDevice',
        'Mdc.view.setup.deviceconnectionmethod.DeviceConnectionMethodActionMenu',
        'Uni.grid.column.Default'
    ],

    store: 'ConnectionMethodsOfDevice',

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                xtype: 'uni-default-column',
                dataIndex: 'isDefault',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                dataIndex: 'name',
                flex: 2
            },
            {
                header: Uni.I18n.translate('deviceconnectionmethod.direction', 'MDC', 'Direction'),
                dataIndex: 'direction',
                flex: 2
            },
            {
                header: Uni.I18n.translate('deviceconnectionmethod.connectionType', 'MDC', 'Connection type'),
                dataIndex: 'connectionType',
                flex: 3
            },
            {
                header: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                dataIndex: 'status',
                renderer: function (value, b, record) {
                    switch (value) {
                        case 'connectionTaskStatusIncomplete':
                            return Uni.I18n.translate('deviceconnectionmethod.status.incomplete', 'MDC', 'Incomplete');
                        case 'connectionTaskStatusActive':
                            return Uni.I18n.translate('general.active', 'MDC', 'Active');
                        case 'connectionTaskStatusInActive':
                            return Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
                        default :
                            return '';
                    }
                },
                flex: 2
            },
            {
                xtype: 'uni-actioncolumn',
                menu: {
                    xtype: 'device-connection-method-action-menu',
                    itemId: 'device-connection-method-action-menu'
                }
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
                        xtype: 'button',
                        text: Uni.I18n.translate('deviceconnectionmethod.addConnectionMethod', 'MDC', 'Add connection method'),
                        privileges: Mdc.privileges.Device.administrateDeviceCommunication,
                        iconCls: 'x-uni-action-iconD',
                        dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.connectionMethodsActions,
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


