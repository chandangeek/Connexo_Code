Ext.define('Mdc.view.setup.connectionmethod.ConnectionMethodsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.connectionMethodsGrid',
    overflowY: 'auto',
    itemId: 'connectionmethodsgrid',

    deviceTypeId: null,
    deviceConfigId: null,

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.ConnectionMethodsOfDeviceConfiguration',
        'Mdc.view.setup.connectionmethod.ConnectionMethodActionMenu',
        'Uni.grid.column.Default'
    ],

    store: 'ConnectionMethodsOfDeviceConfiguration',

    initComponent: function () {
        var me = this;
        this.columns = [
            {
                xtype: 'uni-default-column',
                dataIndex: 'isDefault',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                dataIndex: 'name',
                flex: 3
            },
            {
                header: Uni.I18n.translate('connectionmethod.direction', 'MDC', 'Direction'),
                dataIndex: 'direction',
                flex: 3
            },
            {
                header: Uni.I18n.translate('connectionmethod.connectionType', 'MDC', 'Connection type'),
                dataIndex: 'connectionTypePluggableClass',
                flex: 3
            },

            {
                xtype: 'uni-actioncolumn',
                privileges: Mdc.privileges.DeviceType.admin,
                items: 'Mdc.view.setup.connectionmethod.ConnectionMethodActionMenu'
            }

        ];
        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('connectionmethod.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} connection methods'),
                displayMoreMsg: Uni.I18n.translate('connectionmethod.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} connection methods'),
                emptyMsg: Uni.I18n.translate('connectionmethod.pagingtoolbartop.emptyMsg', 'MDC', 'There are no connection methods to display'),
                items: [
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('connectionmethod.addConnectionMethod', 'MDC', 'Add connection method'),
                        privileges: Mdc.privileges.DeviceType.admin,
                        iconCls: 'x-uni-action-iconD',
                        menu: {
                            plain: true,
                            items: [
                                {
                                    text: Uni.I18n.translate('connectionmethod.addOutboundConnectionMethod', 'MDC', 'Add outbound connection method'),
                                    itemId: 'createOutboundConnectionButton',
                                    action: 'createOutboundConnectionMethod'
                                },
                                {
                                    text: Uni.I18n.translate('connectionmethod.addInboundConnectionMethod', 'MDC', 'Add inbound connection method'),
                                    itemId: 'createInboundConnectionButton',
                                    action: 'createInboundConnectionMethod'
                                }
                            ]

                        }
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                params: [
                    {deviceType: this.deviceTypeId},
                    {deviceConfig: this.deviceConfigId}
                ],
                itemsPerPageMsg: Uni.I18n.translate('connectionmethod.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Connection methods per page'),
                dock: 'bottom'
            }
        ];

        this.callParent();
    }
});


