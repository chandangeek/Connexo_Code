Ext.define('Mdc.view.setup.devicetype.DeviceTypesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceTypesGrid',
    overflowY: 'auto',
    itemId: 'devicetypegrid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.DeviceTypes'
    ],
//    controllers: [
//        'Mdc.controller.setup.DeviceTypes'
//    ],
    store: 'DeviceTypes',
    initComponent: function () {
        var me = this;
        this.columns = [
            {
                header: Uni.I18n.translate('devicetype.name', 'MDC', 'Name'),
                dataIndex: 'name',
                sortable: false,
                hideable: false,
                renderer: function (value, b, record) {
                    return '<a href="#/setup/devicetypes/' + record.get('id') + '">' + value + '</a>';
                },
                fixed: true,
                flex: 0.4
            },
            {
                header: Uni.I18n.translate('devicetype.communicationProtocol', 'MDC', 'Communication protocol'),
                dataIndex: 'communicationProtocolName',
                sortable: false,
                hideable: false,
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
                displayMsg: Uni.I18n.translate('devicetype.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} device types'),
                displayMoreMsg: Uni.I18n.translate('devicetype.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} device types'),
                emptyMsg: Uni.I18n.translate('devicetype.pagingtoolbartop.emptyMsg', 'MDC', 'There are no device types to display'),
                items: [
                    '->',
                    {
                        text: Uni.I18n.translate('devicetype.createDeviceType', 'MDC', 'Create device type'),
                        itemId: 'createDeviceType',
                        xtype: 'button',
                        action: 'createDeviceType'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('devicetype.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Device types per page')
            }
        ];

        this.callParent();
    }
});
