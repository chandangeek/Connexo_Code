Ext.define('Cfg.view.validation.RuleList', {
    extend: 'Ext.grid.Panel',
    border: true,
    margins: '0 10 10 10',
    alias: 'widget.validationruleList',
    itemId: 'validationruleList',
    store: 'ValidationRules',

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],

    ruleSetId: null,

    columns: {

        items: [
            { header: I18n.translate('validation.name', 'CFG', 'Name'), dataIndex: 'name', flex: 1},
            { header: I18n.translate('validation.rule', 'CFG', 'Rule'), dataIndex: 'displayName', flex: 1},
            { header: I18n.translate('validation.active', 'CFG', 'Active'), dataIndex: 'active', flex: 1,
                renderer:function(value){
                    if (value) {
                        return I18n.translate('general.yes', 'CFG', 'Yes')
                    } else {
                        return I18n.translate('general.no', 'CFG', 'Yes')
                    }
                }
            },
            {
                xtype:'actioncolumn',
                header: I18n.translate('validation.actions', 'CFG', 'Actions'),
                align: 'center',
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
                            }, {
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
                dock: 'top',
                displayMsg: '{0} - {1} of {2} rules',
                items: [
                    {
                        xtype: 'component',
                        flex: 1
                    },
                    {
                        xtype: 'button',
                        text:  I18n.translate('validation.addRule', 'CFG', 'Add rule'),
                        itemId: 'addRuleLink',
                        href: '#/validation/addRule',
                        hrefTarget: '_self'
                    },
                    {
                        text: I18n.translate('general.bulkAction', 'CFG', 'Bulk action'),
                        itemId: 'ruleBulkAction',
                        action: 'ruleBulkAction'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                dock: 'bottom',
                itemsPerPageMsg: 'Rules per page',
                itemId: 'rulesListBottomPagingToolbar',
                params: {id: this.ruleSetId}
            }];
        this.callParent(arguments);
    }

});
