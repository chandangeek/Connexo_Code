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
        'Uni.view.toolbar.PagingBottom',
        'Cfg.view.validation.RuleActionMenu'
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
            { header: Uni.I18n.translate('validation.validationRule', 'CFG', 'Validation rule'), dataIndex: 'name', flex: 0.3, sortable: false, fixed: true,
                renderer: function (value, b, record) {
                    return '<a href="#/administration/validation/rulesets/' + record.get('ruleSetId') + '/rules/' + record.getId() + '">' + value + '</a>'
                }
            },
            { header: Uni.I18n.translate('validation.status', 'CFG', 'Status'), dataIndex: 'active', flex: 0.3, sortable: false, fixed: true,
                renderer: function (value) {
                    if (value) {
                        return Uni.I18n.translate('validation.active', 'CFG', 'Active')
                    } else {
                        return Uni.I18n.translate('validation.inactive', 'CFG', 'Inactive')
                    }
                }
            },
            {
                xtype: 'uni-actioncolumn',
                menu: {
                    itemId: 'ruleGridMenu',
                    xtype: 'validation-rule-action-menu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                itemId: 'rulesTopPagingToolbar',
                dock: 'top',
                displayMsg: '{0} - {1} of {2} validation rules',
                items: [
                    {
                        xtype: 'component',
                        flex: 1
                    },
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('validation.addValidationRule', 'CFG', 'Add validation rule'),
                        itemId: 'addRuleLink',
                        ui: 'action',
                        href: '#/administration/validation/rulesets/' + me.ruleSetId + '/rules/add',
                        hrefTarget: '_self'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                margins: '10 10 10 10',
                store: me.store,
                dock: 'bottom',
                itemsPerPageMsg: 'Validation rules per page',
                itemId: 'rulesListBottomPagingToolbar',
                params: {id: me.ruleSetId}
            }
        ];

        me.callParent(arguments);
    }
});
