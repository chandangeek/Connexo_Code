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



