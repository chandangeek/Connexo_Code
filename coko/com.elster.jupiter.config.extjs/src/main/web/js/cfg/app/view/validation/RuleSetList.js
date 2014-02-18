Ext.define('Cfg.view.validation.RuleSetList', {
    extend: 'Ext.grid.Panel',
    border: true,
    //margins: '0 10 0 10',
    margins: '0 10 10 10',
    alias: 'widget.validationrulesetList',
    itemId: 'validationrulesetList',
    store: 'ValidationRuleSets',

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],





    columns: {
        items: [
            { header: I18n.translate('general.name', 'CFG', 'Name'), dataIndex: 'name', flex: 1, sortable: false,
                renderer: function(value, metaData, record, rowIndex, colIndex, store) {
                    return '<a style="color:#007dc3" href="#/validation/overview/' + record.getId() + '">' + value + '</a>'
                }
            },
            { header: I18n.translate('validation.numberOfRules', 'CFG', 'Number of rules'), dataIndex: 'numberOfRules', flex: 1, align: 'center', sortable: false  },
            { header:I18n.translate('validation.numberOfInActiveRules', 'CFG', 'Number of inactive rules'), dataIndex: 'numberOfInactiveRules', flex: 1, align: 'center', sortable: false },
            {
                xtype:'actioncolumn',
                sortable: false,
                align: 'center',
                header: I18n.translate('validation.actions', 'CFG', 'Actions'),
                width:150,
                items: [{
                    icon: 'resources/images/gear-16x16.png',
                    handler: function(grid, rowIndex, colIndex,item,e) {
                        var menu = Ext.widget('menu', {
                            items: [{
                                xtype: 'menuitem',
                                text: I18n.translate('general.edit', 'CFG', 'Edit'),
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
                                text: I18n.translate('general.delete', 'CFG', 'Delete'),
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
                xtype: 'pagingtoolbartop',
                store: this.store,
                displayMsg: '{0} - {1} of {2} rule sets',
                dock: 'top',
                items: [
                    {
                        xtype: 'component',
                        flex: 1
                    },
                    {
                        text: I18n.translate('validation.createRuleSet', 'CFG', 'Create rule set'),
                        itemId: 'newRuleset',
                        xtype: 'button',
                        href: '#/validation/createset',
                        hrefTarget: '_self'
                    },
                    {
                        text:  I18n.translate('general.bulkAction', 'CFG', 'Bulk action'),
                        itemId: 'rulesetBulkAction',
                        action: 'rulesetBulkAction'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                itemsPerPageMsg: 'Rule sets per page',
                dock: 'bottom'
            }];

        this.callParent(arguments);
    }
});
