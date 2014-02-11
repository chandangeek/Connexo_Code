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
    store: 'DeviceTypes',
    padding: '10 10 10 10',
    initComponent: function () {
        var me = this;
        this.columns = [
            {
                header : 'Name',
                dataIndex: 'name',
                sortable: false,
                hideable: false,
                renderer: function(value,b,record){
                    return '<a href="#/setup/devicetypes/' + record.get('id') + '">' + value + '</a>';;
                },
                flex: 1
            },
            {
                header: 'Protocol',
                dataIndex: 'communicationProtocolName',
                sortable: false,
                hideable: false,
                flex: 1
            },

            {
                xtype:'actioncolumn',
                tdCls:'view',
                header : 'Actions',
                sortable: false,
                hideable: false,
                items: [{
                    icon: 'resources/images/gear-16x16.png',
                    handler: function(grid, rowIndex, colIndex,item,e) {
                        var menu = Ext.widget('menu', {
                            items: [{
                                xtype: 'menuitem',
                                text: 'Edit',
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
                                text: 'Delete',
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
                        text: 'Create device type',
                        itemId: 'createDeviceType',
                        xtype: 'button',
                        action: 'createDeviceType'
                    },
                    {
                        text: 'Bulk action',
                        itemId: 'deviceTypesBulkAction',
                        xtype: 'button'
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
