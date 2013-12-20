Ext.define('Mdc.view.setup.comserver.ComServersGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.comServersGrid',
    store: 'ComServers',
    overflowY: 'auto',
    itemId: 'comservergrid',
    padding: 10,
    selModel: {
        mode: 'MULTI'
    },
    selType: 'checkboxmodel',
    initComponent: function () {
        var me = this;
        this.columns = [
            {
                header : 'name',
                dataIndex: 'name',
                flex: 1
            },
            {
                header : 'comServerType',
                dataIndex: 'comServerType'
            },
//            {
//                xtype:'actioncolumn',
//                dataIndex: 'active',
//                width:24,
//                renderer: function(value){
//                    if(value===true){
//                        me.columns[2].items[0].icon = 'resources/images/stop.png';
//                    } else {
//                        me.columns[2].items[0].icon = 'resources/images/start.png';
//                    }
//                },
//                items: [{
//                    tooltip: 'start/stop',
//                    handler: function(grid, rowIndex, colIndex,item,e) {
//                        this.fireEvent('startStopComserver',grid,grid.getRecord(rowIndex));
//                    }
//                }]
//            },
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
            xtype: 'pagingtoolbar',
            store: 'ComServers',
            dock: 'bottom',
            displayInfo: true
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
                        text: 'Online'
                    },{
                        text: 'Remote'
                    },{
                        text: 'Mobile'
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