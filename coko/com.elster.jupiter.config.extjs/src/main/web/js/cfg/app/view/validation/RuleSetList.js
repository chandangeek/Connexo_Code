Ext.define('Cfg.view.validation.RuleSetList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.validationrulesetList',
    itemId: 'validationrulesetList',
    store: 'ValidationRuleSets',
    selType: 'checkboxmodel',
    tbar: [
        '->',
        {
            text: 'Create new ruleset',
            itemId: 'newRuleset',
            action: 'newRuleset'
        },
        {
            text: 'Bulk actions',
            menu:{
                items:[
                    {
                        text: 'Remove selected rulesets',
                        itemId: 'removeRulesets',
                        action: 'removeRulesets'

                    }
                ]
            }
        }],
    columns: {
        items: [
            { header: 'Name', dataIndex: 'name', flex: 1 },
            { header: 'Description', dataIndex: 'description', flex: 1 },
            { header: '# Active Rules', dataIndex: 'numberOfActiveRules', flex: 1 },
            { header: '# Rules', dataIndex: 'numberOfRules', flex: 1  },
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
        //checkOnly: true
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
