Ext.define('Mdc.view.setup.comportpool.ComPortPoolsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.comPortPoolsGrid',

    requires: [
        'Mdc.store.ComPortPools',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    overflowY: 'auto',
    layout: 'fit',
    itemId: 'comportpoolgrid',
    selModel: {
        mode: 'MULTI'
    },
    selType: 'checkboxmodel',
    store: 'ComPortPools',

    initComponent: function () {
        this.columns = [
            {
                header : 'name',
                dataIndex: 'name',
                flex: 1
            },
            {
                header : 'direction',
                dataIndex: 'direction'
            },
            {
               dataIndex: 'active',
                width: 24,
               renderer: function(value,metadata){
                    if(value===true){
                        metadata.style = "background-color:lightgreen;";
                    } else {
                        metadata.style = "background-color:pink;";
                    }
                }
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

        this.dockedItems = [{
            xtype: 'pagingtoolbarbottom',
            store: this.store,
            dock: 'bottom'
        },{
            xtype: 'toolbar',
            dock: 'top',
            ui: 'footer',
            defaults: {minWidth: this.minButtonWidth},
            items: [
                {
                    xtype: 'component',
                    flex: 1
                },
                {
                    text: 'Add',
                    action: 'add',
                    menu: [{
                        text: 'Inbound'
                    },{
                        text: 'Outbound'
                    }]
                },
                {
                    text: 'Delete',
                    action: 'delete'
                }
            ]
        }];

        this.callParent();
    }
});