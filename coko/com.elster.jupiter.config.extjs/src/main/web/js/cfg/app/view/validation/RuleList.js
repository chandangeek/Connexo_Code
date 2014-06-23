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

    ruleSetId: null,

    initComponent: function () {
        var me = this;

        me.columns = [
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
                xtype: 'uni-actioncolumn',
                items: 'Cfg.view.validation.RuleActionMenu'
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
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
                        href: '#/administration/validation/addRule/' + me.ruleSetId,
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
                store: me.store,
                dock: 'bottom',
                itemsPerPageMsg: 'Rules per page',
                itemId: 'rulesListBottomPagingToolbar',
                params: {id: me.ruleSetId}
            }
        ];

        me.callParent(arguments);
    }
});
