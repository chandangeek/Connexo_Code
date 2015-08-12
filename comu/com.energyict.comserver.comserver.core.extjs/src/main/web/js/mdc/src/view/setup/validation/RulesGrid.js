Ext.define('Mdc.view.setup.validation.RulesGrid', {
    extend: 'Ext.grid.Panel',
    xtype: 'validation-rules-grid',

    requires: [
        'Mdc.view.setup.validation.RuleActionMenu',
        'Uni.view.toolbar.PagingBottom',
        'Uni.view.toolbar.PagingTop',
        'Cfg.store.ValidationRules'
    ],

    store: Ext.create('Cfg.store.ValidationRules'),
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
                flex: 3,
                renderer: function (value, b, record) {				
					return '<a href="#/administration/validation/rulesets/' + record.get('ruleSetId') 
                        + '/versions/' + record.get('ruleSetVersionId') 
                        + '/rules/' + record.getId() + '">' + Ext.String.htmlEncode(value) + '</a>';
                }
            },
            {
				header: Uni.I18n.translate('validation.status', 'MDC', 'Status'),
                dataIndex: 'active',
                flex: 5,
                renderer: function (value) {				
                    if (value) {
                        return Uni.I18n.translate('validation.active', 'MDC', 'Active')
                    } else {
                        return Uni.I18n.translate('validation.inactive', 'MDC', 'Inactive')
                    }
                }
            },
            {
                xtype: 'uni-actioncolumn',
                items: 'Mdc.view.setup.validation.RuleActionMenu'
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                usesExactCount: true,
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('validation.pagingtoolbartop.displayMsgRule', 'MDC', '{0} - {1} of {2} validation rules'),
                displayMoreMsg: Uni.I18n.translate('validation.pagingtoolbartop.displayMoreMsgRule', 'MDC', '{0} - {1} of more than {2} validation rules'),
                emptyMsg: Uni.I18n.translate('validation.pagingtoolbartop.emptyMsgRule', 'MDC', 'There are no validation rules to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                pageSizeParam: 'limit2',
                pageStartParam: 'start2',
                deferLoading: true,
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('validation.pagingtoolbarbottom.itemsPerPageRule', 'MDC', 'Validation rules per page'),
                dock: 'bottom',
                params: {
                    ruleSetId: me.validationRuleSetId
                }
            }
        ];

        me.callParent();
    },

    updateValidationRuleSetId: function (validationRuleSetId) {
        var me = this;
		
        me.validationRuleSetId = validationRuleSetId;
        me.down('pagingtoolbarbottom').params = {
            id: me.validationRuleSetId
        };
    }
});

