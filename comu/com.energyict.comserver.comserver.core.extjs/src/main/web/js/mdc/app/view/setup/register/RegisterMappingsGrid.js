Ext.define('Mdc.view.setup.register.RegisterMappingsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.registerMappingsGrid',
    overflowY: 'auto',
    itemId: 'registermappinggrid',
    selModel: {
        mode: 'MULTI'
    },
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.RegisterMappings'
    ],
    store: 'RegisterMappings',
    padding: '10 10 10 10',
    initComponent: function () {
        var me = this;
        this.columns = [
            {
                header : 'Name',
                dataIndex: 'name',
                flex: 1
            },
            {
                header: 'ObisCode',
                dataIndex: 'obisCode',
                flex: 1
            },

            {
                xtype:'actioncolumn',
                tdCls:'view',
                width:24,
                items: [{
                    icon: 'resources/images/gear-16x16.png',
                    tooltip: 'View',
                    handler: function(grid, rowIndex, colIndex,item,e) {
                        var menu = Ext.widget('menu', {
                            items: [{
                                xtype: 'menuitem',
                                text: 'Edit',
                                listeners: {
                                    click: {
                                        element: 'el',
                                        fn: function(){
                                            this.fireEvent('edit',grid,grid.getSelectionModel().getSelection());
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
                                            console.log('delete');
                                            this.fireEvent('delete',grid,grid.getSelectionModel().getSelection());
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
                dock: 'top'
            },
            {
            xtype: 'pagingtoolbarbottom',
            store: this.store,
            dock: 'bottom'
        }];

        this.callParent();
    }
});
