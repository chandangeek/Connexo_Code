Ext.define('Mdc.view.setup.validation.RulesGrid', {
    extend: 'Ext.grid.Panel',
    xtype: 'validation-rules-grid',

    requires: [
        'Mdc.view.setup.validation.RuleActionMenu',
        'Uni.view.toolbar.PagingBottom',
        'Uni.view.toolbar.PagingTop'
    ],

    store: Ext.getStore('ValidationRules') || Ext.create('Cfg.store.ValidationRules'),
    selModel: {
        mode: 'SINGLE'
    },

    validationRuleSetId: null,

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('validation.ruleName', 'MDC', 'Validation rule'),
                dataIndex: 'name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('validation.status', 'MDC', 'Status'),
                dataIndex: 'active',
                flex: 1
            }
            // TODO Action column.
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('validation.pagingtoolbartop.displayMsgRule', 'MDC', '{0} - {1} of {2} validation rules'),
                displayMoreMsg: Uni.I18n.translate('validation.pagingtoolbartop.displayMoreMsgRule', 'MDC', '{0} - {1} of more than {2} validation rules'),
                emptyMsg: Uni.I18n.translate('validation.pagingtoolbartop.emptyMsgRule', 'MDC', 'There are no validation rules to display')
            }
            // TODO Currently the bottom toolbar messes up the grid, why?
//            ,
//            {
//                xtype: 'pagingtoolbarbottom',
//                store: me.store,
//                itemsPerPageMsg: Uni.I18n.translate('validation.pagingtoolbarbottom.itemsPerPageRule', 'MDC', 'Validation rules per page'),
//                dock: 'bottom'
//            }
        ];

        me.callParent();
    }
});

