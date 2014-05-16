Ext.define('Cfg.view.validation.RuleList', {
    extend: 'Ext.grid.Panel',
    border: true,
    alias: 'widget.validationruleList',
    itemId: 'validationruleList',
    store: 'ValidationRules',
    overflowY: 'auto',
    selModel: {
        mode: 'SINGLE'
    },

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],

    ruleSetId: null,
    initComponent: function () {
        var me = this;
        this.columns = [

            { header: Uni.I18n.translate('validation.name', 'CFG', 'Name'), dataIndex: 'name', flex: 0.3, sortable: false, fixed: true},
            { header: Uni.I18n.translate('validation.rule', 'CFG', 'Rule'), dataIndex: 'displayName', flex: 0.3, sortable: false, fixed: true},
            { header: Uni.I18n.translate('validation.active', 'CFG', 'Active'), dataIndex: 'active', flex: 0.3, sortable: false, fixed: true,
                renderer: function (value) {
                    if (value) {
                        return Uni.I18n.translate('general.yes', 'CFG', 'Yes')
                    } else {
                        return Uni.I18n.translate('general.no', 'CFG', 'No')
                    }
                }
            },
            {
                xtype: 'actioncolumn',
                iconCls: 'uni-actioncolumn-gear',
                columnWidth: 32,
                fixed: true,
                header: Uni.I18n.translate('validation.actions', 'CFG', 'Actions'),
                sortable: false,
                hideable: false,
                items: [
                    {
                        handler: function (grid, rowIndex, colIndex, item, e) {
                            var menu = Ext.widget('menu', {
                                items: [
                                    {
                                        xtype: 'menuitem',
                                        text: Uni.I18n.translate('general.edit', 'CFG', 'Edit'),
                                        listeners: {
                                            click: {
                                                element: 'el',
                                                fn: function () {
                                                    this.fireEvent('edit', grid.getSelectionModel().getSelection());
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
                                                fn: function () {
                                                    console.log('delete');
                                                    this.fireEvent('delete', grid.getSelectionModel().getSelection());
                                                },
                                                scope: this
                                            }
                                        }
                                    }
                                ]
                            });
                            menu.showAt(e.getXY());
                        }
                    }
                ]
            }
        ];
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
                        text: Uni.I18n.translate('validation.addRule', 'CFG', 'Add rule'),
                        itemId: 'addRuleLink',
                        href: '#/administration/validation/addRule/' + this.ruleSetId,
                        hrefTarget: '_self'
                    },
                    {
                        text: Uni.I18n.translate('general.bulkAction', 'CFG', 'Bulk action'),
                        itemId: 'ruleBulkAction',
                        action: 'ruleBulkAction'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                margins: '10 10 10 10',
                store: this.store,
                dock: 'bottom',
                itemsPerPageMsg: 'Rules per page',
                itemId: 'rulesListBottomPagingToolbar',
                params: {id: this.ruleSetId}
            }
        ];
        this.callParent(arguments);
    }



});
