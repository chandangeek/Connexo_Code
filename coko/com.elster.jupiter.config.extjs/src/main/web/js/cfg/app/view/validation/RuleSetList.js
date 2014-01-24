Ext.define('Cfg.view.validation.RuleSetList', {
    extend: 'Ext.grid.Panel',
    border: true,
    margins: '0 10 0 10',
    alias: 'widget.validationrulesetList',
    itemId: 'validationrulesetList',
    store: 'ValidationRuleSets',

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],





    columns: {
        items: [
            { header: 'Name', dataIndex: 'name', flex: 1,
                renderer: function(value, metaData, record, rowIndex, colIndex, store) {
                    return '<a style="color:#007dc3" href="#/validation/overview/' + record.getId() + '">' + value + '</a>'
                }
            },
            { header: 'Number of rules', dataIndex: 'numberOfRules', flex: 1, align: 'center'  },
            { header: 'Number of inactive rules', dataIndex: 'numberOfInactiveRules', flex: 1, align: 'center' },
            {
                xtype:'actioncolumn',
                align: 'center',
                header: 'Actions',
                width:150,
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
                                            this.fireEvent('edit',grid.getSelectionModel().getSelection());
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
