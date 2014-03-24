Ext.define('Mdc.view.setup.deviceconfiguration.DeviceConfigurationsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceConfigurationsGrid',
    overflowY: 'auto',
    itemId: 'deviceconfigurationsgrid',
    deviceTypeId: null,
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.DeviceConfigurations'
    ],
//    controllers: [
//        'Mdc.controller.setup.DeviceTypes'
//    ],
    store: 'DeviceConfigurations',
    padding: '10 10 10 10',
    initComponent: function () {
        var me = this;
        this.columns = [
            {
                header: Uni.I18n.translate('deviceconfiguration.name', 'MDC', 'Name'),
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
                header: Uni.I18n.translate('deviceconfiguration.active', 'MDC', 'Active'),
                dataIndex: 'active',
                sortable: false,
                hideable: false,
                renderer: function (value, b, record) {
                    return value === true ? Uni.I18n.translate('general.active', 'MDC', 'Active') : Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
                },
                fixed: true,
                flex: 0.4
            },

            {
                xtype: 'actioncolumn',
                tdCls: 'view',
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
                                    },
                                    {
                                        xtype: 'menuseparator'
                                    },
                                    {
                                        xtype: 'menuitem',
                                        text: record.get('active') === true ? Uni.I18n.translate('general.deActivate', 'MDC', 'Deactivate') : Uni.I18n.translate('general.activate', 'MDC', 'Activate'),
                                        listeners: {
                                            click: {
                                                element: 'el',
                                                fn: function () {
                                                    this.fireEvent('activateItem', record);
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
                        text: Uni.I18n.translate('deviceconfiguration.createDeviceConfiguration', 'MDC', 'Create device configuration'),
                        itemId: 'createDeviceConfiguration',
                        xtype: 'button',
                        action: 'createDeviceConfiguration'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                //todo: check if this works
                params: [
                    {deviceType: this.deviceTypeId}
                ],
                dock: 'bottom'
            }
        ];

        this.callParent();
    }
});

