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
                header: Uni.I18n.translate('connectionmethod.name', 'MDC', 'Name'),
                dataIndex: 'name',
                sortable: false,
                hideable: false,
//                renderer: function(value,b,record){
//                    return '<a href="#/setup/devicetypes/' + record.get('id') + '">' + value + '</a>';;
//                },
                fixed: true,
                flex: 0.4
            },
            {
                xtype: 'actioncolumn',
                tdCls: 'view',
                iconCls: 'uni-centered-icon',
                header: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                sortable: false,
                hideable: false,
                fixed: true,
                flex: 0.1,
                items: [
                    {
                        icon: '../mdc/resources/images/gear-16x16.png',
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
                                        xtype: 'menuseparator'
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
                        text: Uni.I18n.translate('connectionmethod.addConnectionMethod', 'MDC', 'Add connection method'),
                        itemId: 'createConnectionButton',
                        xtype: 'button',
                        action: 'createConnectionMethod'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                params: [
                    {deviceType: this.deviceTypeId},
                    {deviceConfig:this.deviceConfigId}
                ],
                dock: 'bottom'
            }
        ];

        this.callParent();
    }
});


