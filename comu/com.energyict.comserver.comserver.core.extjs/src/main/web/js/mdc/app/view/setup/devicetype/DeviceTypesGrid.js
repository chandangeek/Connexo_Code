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
    padding: '10 10 10 10',
    initComponent: function () {
        var me = this;
        this.columns = [
            {
                header : Uni.I18n.translate('devicetype.name', 'MDC', 'Name'),
                dataIndex: 'name',
                sortable: false,
                hideable: false,
                renderer: function(value,b,record){
                    return '<a href="#/setup/devicetypes/' + record.get('id') + '">' + value + '</a>';;
                },
                fixed: true,
                flex: 0.4
            },
            {
                header: Uni.I18n.translate('devicetype.communicationProtocol', 'MDC', 'Device communication protocol'),
                dataIndex: 'communicationProtocolName',
                sortable: false,
                hideable: false,
                fixed: true,
                flex: 0.4
            },

            {
                xtype:'actioncolumn',
                tdCls:'view',
                header : Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                sortable: false,
                hideable: false,
                fixed: true,
                flex: 0.1,
                items: [{
                    icon: 'resources/images/gear-16x16.png',
                    handler: function(grid, rowIndex, colIndex,item,e) {
                        var menu = Ext.widget('menu', {
                            items: [{
                                xtype: 'menuitem',
                                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                                listeners: {
                                    click: {
                                        element: 'el',
                                        fn: function(){
                                            this.fireEvent('editItem',grid,grid.getSelectionModel().getSelection());
                                        },
                                        scope: this
                                    }

                                }
                            }, {
                                xtype: 'menuitem',
                                text: Uni.I18n.translate('general.delete', 'MDC', 'Delete'),
                                listeners: {
                                    click: {
                                        element: 'el',
                                        fn: function(){
                                            this.fireEvent('deleteItem',grid,grid.getSelectionModel().getSelection());
                                        },
                                        scope: this
                                    }

                                }
                            }]
                        });
                        menu.showAt(e.getXY());
                    }
                }]
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
            dock: 'bottom'
        }];

        this.callParent();
    }
});
