Ext.define('Cfg.view.validation.RuleList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.validationruleList',
    itemId: 'validationruleList',
    store: 'ValidationRules',
    selType: 'checkboxmodel',
    tbar: [
        '->',
        {
            text: 'Create new rule',
            itemId: 'newRule',
            action: 'newRule'
        },
        {
            text: 'Bulk actions',
            menu:{
                items:[
                    {
                        text: 'Remove selected rules',
                        itemId: 'removeRules',
                        action: 'removeRules'

                    }
                ]
            }
        }
    ],
    columns: {

        items: [
            { header: 'Name', dataIndex: 'implementation', flex: 1 },
            { header: 'Active', dataIndex: 'active', xtype: 'checkcolumn', flex: 1 },
            { header: 'Action', dataIndex: 'action', flex: 1 },
            {
                xtype:'actioncolumn',
                header: 'Actions',
                flex: 1,
                tdCls:'view',
                width:40,
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
                                            this.fireEvent('edit',grid.getSelectionModel().getSelection());
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
                                            this.fireEvent('delete',grid.getSelectionModel().getSelection());
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
        ]
    },

    selModel: Ext.create('Ext.selection.CheckboxModel', {
        mode: 'MULTI'
    }),
    selType: 'checkboxmodel',
    initComponent: function () {
        this.dockedItems = [
            {
                xtype: 'pagingtoolbar',
                store: this.store,
                dock: 'bottom',
                displayInfo: true,
                afterPageText: '',
                displayMsg: 'Displaying {0} - {1}'
            }
        ];
        this.callParent(arguments);
    }
});
