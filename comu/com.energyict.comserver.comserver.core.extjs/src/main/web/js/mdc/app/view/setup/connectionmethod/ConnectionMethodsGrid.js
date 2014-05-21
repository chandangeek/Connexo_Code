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
        'Mdc.store.ConnectionMethodsOfDeviceConfiguration'
    ],
//    controllers: [
//        'Mdc.controller.setup.DeviceTypes'
//    ],
    store: 'ConnectionMethodsOfDeviceConfiguration',
    padding: '10 10 10 10',
    initComponent: function () {
        var me = this;
        this.columns = [
            {
                header: Uni.I18n.translate('connectionmethod.default', 'MDC', 'Default'),
                dataIndex: 'isDefault',
                sortable: false,
                hideable: false,
                renderer: function (value, metadata) {
                    if (value === true) {
                        metadata.style = "padding: 6px 16px 6px 16px;";
                        return '<img src=../mdc/resources/images/1rightarrow.png/>';
                    } else {
                        return '';
                    }
                },
                align: 'center',
                fixed: true,
                flex: 0.1
            },
            {
                header: Uni.I18n.translate('connectionmethod.name', 'MDC', 'Name'),
                dataIndex: 'name',
                sortable: false,
                hideable: false,
//                renderer: function(value,b,record){
//                    return '<a href="#/administration/devicetypes/' + record.get('id') + '">' + value + '</a>';;
//                },
                fixed: true,
                flex: 0.3
            },
            {
                header: Uni.I18n.translate('connectionmethod.direction', 'MDC', 'Direction'),
                dataIndex: 'direction',
                sortable: false,
                hideable: false,
                fixed: true,
                flex: 0.3
            },
            {
                header: Uni.I18n.translate('connectionmethod.connectionType', 'MDC', 'Connection type'),
                dataIndex: 'connectionType',
                sortable: false,
                hideable: false,
                fixed: true,
                flex: 0.3
            },
            {
                xtype: 'actioncolumn',
                iconCls: 'uni-actioncolumn-gear',
                columnWidth: 32,
                fixed: true,
                header: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                sortable: false,
                hideable: false,
                items: [
                    {
                        handler: function (grid, rowIndex, colIndex, item, e, record, row) {
                            grid.getSelectionModel().select(rowIndex);
                            var menu = Ext.widget('menu', {
                                items: [
                                    {
                                        xtype: 'menuitem',
                                        text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                                        listeners: {
                                            click: {
                                                element: 'el',
                                                fn: function () {
                                                    this.fireEvent('editItem', record);
                                                },
                                                scope: this
                                            }
                                        }
                                    },
                                    {
                                        xtype: 'menuitem',
                                        text: Uni.I18n.translate('general.delete', 'MDC', 'Delete'),
                                        listeners: {
                                            click: {
                                                element: 'el',
                                                fn: function () {
                                                    this.fireEvent('deleteItem', record);
                                                },
                                                scope: this
                                            }
                                        }
                                    },
                                    {
                                        xtype: 'menuitem',
                                        text: record.get('isDefault') === true ? Uni.I18n.translate('connectionmethod.unsetAsDefault', 'MDC', 'Remove as default') : Uni.I18n.translate('connectionmethod.setAsDefault', 'MDC', 'Set as default'),
                                        listeners: {
                                            click: {
                                                element: 'el',
                                                fn: function () {
                                                    this.fireEvent('toggleDefault', record);
                                                },
                                                scope: this
                                            }
                                        }
                                    }
                                ]
                            });
                            menu.showAt(e.getXY());
                        }
                    }
                ]
            }
        ];
        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                items: [
                    {
                        xtype: 'component',
                        flex: 1
                    },
                    {
                        text: Uni.I18n.translate('connectionmethod.addOutboundConnectionMethod', 'MDC', 'Add outbound connection method'),
                        itemId: 'createOutboundConnectionButton',
                        xtype: 'button',
                        action: 'createOutboundConnectionMethod'
                    },
                    {
                        text: Uni.I18n.translate('connectionmethod.addInboundConnectionMethod', 'MDC', 'Add inbound connection method'),
                        itemId: 'createInboundConnectionButton',
                        xtype: 'button',
                        action: 'createInboundConnectionMethod'
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
                dock: 'bottom'
            }
        ];

        this.callParent();
    }
});


