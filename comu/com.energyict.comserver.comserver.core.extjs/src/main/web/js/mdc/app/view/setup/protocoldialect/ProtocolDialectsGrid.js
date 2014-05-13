Ext.define('Mdc.view.setup.protocoldialect.ProtocolDialectsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.protocolDialectsGrid',
    overflowY: 'auto',
    itemId: 'protocoldialectsgrid',
    deviceTypeId: null,
    deviceConfigId: null,
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.ProtocolDialectsOfDeviceConfiguration'
    ],
    selModel: {
        mode: 'SINGLE'
    },
    store: 'ProtocolDialectsOfDeviceConfiguration',
    padding: '10 10 10 10',
    initComponent: function () {
        var me = this;
        this.columns = [
            {
                header: Uni.I18n.translate('protocolDialect.name', 'MDC', 'Name'),
                dataIndex: 'name',
                sortable: false,
                hideable: false,
                fixed: true,
                flex: 0.6
            },
            /*{
                header: Uni.I18n.translate('protocolDialect.availableForUse', 'MDC', 'Available for use'),
                dataIndex: 'availableForUse',
                sortable: false,
                hideable: false,
                renderer: function (value, b, record) {
                    return value === true ? Uni.I18n.translate('general.yes', 'MDC', 'Yes') : Uni.I18n.translate('general.no', 'MDC', 'No');
                },
                fixed: true,
                flex: 0.2
            },*/
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
                        icon: '../mdc/resources/images/masterActions.png',
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
                                    }//,
                                    /*{
                                        xtype: 'menuseparator'
                                    },
                                    {
                                        xtype: 'menuitem',
                                        text: Uni.I18n.translate('protocolDialects.notAvailableForUse', 'MDC', 'Not available for use'),
                                        text: record.get('availableForUse') === true ? Uni.I18n.translate('protocolDialects.notAvailableForUse', 'MDC', 'Not available for use') : Uni.I18n.translate('protocolDialects.availableForUse', 'MDC', 'Available For use'),
                                        listeners: {
                                            click: {
                                                element: 'el',
                                                fn: function () {
                                                    this.fireEvent('changeAvailability', record);
                                                },
                                                scope: this
                                            }

                                        }
                                    }*/
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



