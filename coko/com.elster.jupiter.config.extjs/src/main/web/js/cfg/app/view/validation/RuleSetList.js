Ext.define('Cfg.view.validation.RuleSetList', {
    extend: 'Ext.grid.Panel',
    border: true,
    //margins: '0 10 0 10',
    //margins: '0 10 10 10',
    alias: 'widget.validationrulesetList',
    itemId: 'validationrulesetList',
    store: 'ValidationRuleSets',

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],

    listeners: {
        'render': function (component) {
            // Get sure that the store is not loading and that it
            // has at least a record on it
            if (this.store.isLoading() || this.store.getCount() == 0) {
                // If it is still pending attach a listener to load
                // event for a single time to handle the selection
                // after the store has been loaded
                this.store.on('load', function () {
                    this.getView().getSelectionModel().select(0);
                    this.getView().focusRow(0);
                }, this, {
                    single: true
                });
            } else {
                this.getView().getSelectionModel().select(0);
                this.getView().focusRow(0);
            }

        }
    },






    columns: {
        items: [
            { header: Uni.I18n.translate('general.name', 'CFG', 'Name'), dataIndex: 'name', flex: 0.3, sortable: false, fixed: true,

                renderer: function (value, b, record) {
                    return '<a href="#administration/validation/overview/' + record.getId() + '">' + value + '</a>'
                }
            },
            { header: Uni.I18n.translate('validation.numberOfRules', 'CFG', 'Number of rules'), dataIndex: 'numberOfRules', flex: 0.3, align: 'right', sortable: false, fixed: true  },
            { header:Uni.I18n.translate('validation.numberOfInActiveRules', 'CFG', 'Number of inactive rules'), dataIndex: 'numberOfInactiveRules', flex: 0.3, align: 'right', sortable: false, fixed: true },
            {
                xtype: 'actioncolumn',
                iconCls: 'uni-actioncolumn-gear',
                columnWidth: 32,
                fixed: true,
                header: Uni.I18n.translate('validation.actions', 'CFG', 'Actions'),
                sortable: false,
                hideable: false,
                items: [{
                    handler: function(grid, rowIndex, colIndex,item,e) {
                        var menu = Ext.widget('menu', {
                            items: [{
                                xtype: 'menuitem',
                                text: Uni.I18n.translate('general.edit', 'CFG', 'Edit'),
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
                                xtype: 'menuitem',
                                text: Uni.I18n.translate('general.delete', 'CFG', 'Delete'),
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
                        text: Uni.I18n.translate('validation.createRuleSet', 'CFG', 'Add rule set'),
                        itemId: 'newRuleset',
                        xtype: 'button',
                        href: '#administration/validation/createset',
                        hrefTarget: '_self'
                    },
                    {
                        text:  Uni.I18n.translate('general.bulkAction', 'CFG', 'Bulk action'),
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
