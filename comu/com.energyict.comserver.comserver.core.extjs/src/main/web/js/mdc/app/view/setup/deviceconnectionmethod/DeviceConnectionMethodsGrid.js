Ext.define('Mdc.view.setup.connectionmethod.DeviceConnectionMethodsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceConnectionMethodsGrid',
    overflowY: 'auto',
    itemId: 'deviceconnectionmethodsgrid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.ConnectionMethodsOfDevice',
        'Mdc.view.setup.deviceconnectionmethod.DeviceConnectionMethodActionMenu'
    ],
//    controllers: [
//        'Mdc.controller.setup.DeviceTypes'
//    ],
    store: 'ConnectionMethodsOfDevice',
    //padding: '10 10 10 10',
    initComponent: function () {
        var me = this;
        this.columns = [
            {
                header: Uni.I18n.translate('connectionmethod.default', 'MDC', 'Default'),
                dataIndex: 'isDefault',
                renderer: function (value, metadata) {
                    if (value === true) {
                        metadata.style = "padding: 6px 16px 6px 16px;";
                        return '<img src="../mdc/resources/images/1rightarrow.png">';
                    } else {
                        return '';
                    }
                },
                align: 'center',
                flex: 0.1
            },
            {
                header: Uni.I18n.translate('connectionmethod.name', 'MDC', 'Name'),
                dataIndex: 'name',
//                renderer: function(value,b,record){
//                    return '<a href="#/administration/devicetypes/' + record.get('id') + '">' + value + '</a>';;
//                },
                flex: 0.3
            },
            {
                header: Uni.I18n.translate('connectionmethod.direction', 'MDC', 'Direction'),
                dataIndex: 'direction',
                flex: 0.2
            },
            {
                header: Uni.I18n.translate('connectionmethod.connectionType', 'MDC', 'Connection type'),
                dataIndex: 'connectionType',
                flex: 0.3
            },
            {
                header: Uni.I18n.translate('connectionmethod.status', 'MDC', 'Status'),
                dataIndex: 'paused',
                renderer: function(value,b,record){
                    return value?Uni.I18n.translate('general.inactive', 'MDC', 'Inactive'):Uni.I18n.translate('general.active', 'MDC', 'Active');
                },
                flex: 0.2
            },
            {
                xtype: 'uni-actioncolumn',
                items: 'Mdc.view.setup.deviceconnectionmethod.DeviceConnectionMethodActionMenu'
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
                        xtype: 'component',
                        flex: 1
                    },
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('connectionmethod.addConnectionMethod', 'MDC', 'Add connection method'),
                        iconCls: 'x-uni-action-iconD',
                        menu: {
                            plain: true,
                            items: [
                                {
                                    text: Uni.I18n.translate('connectionmethod.addOutboundConnectionMethod', 'MDC', 'Add outbound connection method'),
                                    itemId: 'createDeviceOutboundConnectionButton',
                                    action: 'createDeviceOutboundConnectionMethod'
                                },
                                {
                                    text: Uni.I18n.translate('connectionmethod.addInboundConnectionMethod', 'MDC', 'Add inbound connection method'),
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
                store: this.store,
                params: [
                    {mrid: this.mrid}
                ],
                itemsPerPageMsg: Uni.I18n.translate('connectionmethod.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Connection methods per page'),
                dock: 'bottom'
            }
        ];

        this.callParent();
    }
});


