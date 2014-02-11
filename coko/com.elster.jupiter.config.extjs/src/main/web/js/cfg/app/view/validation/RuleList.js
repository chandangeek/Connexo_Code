Ext.define('Cfg.view.validation.RuleList', {
    extend: 'Ext.grid.Panel',
    border: true,
    margins: '0 10 0 10',
    alias: 'widget.validationruleList',
    itemId: 'validationruleList',
    store: 'ValidationRules',

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],

    ruleSetId: 9001,

    setRuleSetId: function(value) {
        this.ruleSetId = value;
    },

    columns: {

        items: [
            { header: 'Name', dataIndex: 'name', flex: 1},
            { header: 'Rule', dataIndex: 'displayName', flex: 1},
            { header: 'Active', dataIndex: 'active', flex: 1,
                renderer:function(value){
                    if (value) {
                        return 'Yes'
                    } else {
                        return 'No'
                    }
                }
            },
            {
                xtype:'actioncolumn',
                header: 'Actions',
                align: 'center',
                width:150,
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

    initComponent: function () {
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
                        xtype: 'button',
                        text: 'Add Rule',
                        itemId: 'addRuleLink',
                        href: '#/validation/addRule',
                        hrefTarget: '_self'
                    },
                    {
                        text: 'Bulk action',
                        itemId: 'ruleBulkAction',
                        action: 'ruleBulkAction'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                dock: 'bottom',
                itemId: 'rulesListBottomPagingToolbar',
                param: this.ruleSetId
            }];
        this.callParent(arguments);
    }

});
