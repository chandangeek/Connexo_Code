Ext.define('Cfg.view.validation.RuleList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.validationruleList',
    itemId: 'validationruleList',
    overflowY: 'auto',

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Cfg.view.validation.RuleActionMenu'
    ],

    ruleSetId: null,
    versionId: null,
    isSecondPagination: false,

    initComponent: function () {
        var me = this;

        me.store = Ext.create('Cfg.store.ValidationRules');

        me.columns = [
            {
                header: Uni.I18n.translate('validation.validationRule', 'CFG', 'Validation rule'),
                dataIndex: 'name',
                flex: 3,
                sortable: false,
                fixed: true,
                renderer: function (value, b, record) {
                    return '<a href="#/administration/validation/rulesets/' + record.get('ruleSetVersion').ruleSet.id + '/versions/' + record.get('ruleSetVersion').id + '/rules/' + record.getId() + '">' + Ext.String.htmlEncode(value) + '</a>'
                }
            },
            {
                header: Uni.I18n.translate('validation.status', 'CFG', 'Status'),
                dataIndex: 'active',
                flex: 5,
                sortable: false,
                fixed: true,
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
                privileges: Cfg.privileges.Validation.admin,
                menu: {
                    itemId: 'ruleGridMenu',
                    xtype: 'validation-rule-action-menu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                usesExactCount: true,
                store: me.store,
                itemId: 'rulesTopPagingToolbar',
                dock: 'top',
                displayMsg: Uni.I18n.translate('validation.pagingtoolbartop.displayMsgRule', 'CFG', '{0} - {1} of {2} validation rules'),
                displayMoreMsg: Uni.I18n.translate('validation.pagingtoolbartop.displayMoreMsgRule', 'CFG', '{0} - {1} of more than {2} validation rules'),
                emptyMsg: Uni.I18n.translate('validation.pagingtoolbartop.emptyMsgRule', 'CFG', 'There are no validation rule sets to display'),
                items: [
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('validation.addValidationRule', 'CFG', 'Add validation rule'),
                        privileges: Cfg.privileges.Validation.admin,
                        itemId: 'addRuleLink',                        
						href: '#/administration/validation/rulesets/' + me.ruleSetId  + '/versions/'+ me.versionId + '/rules/add',
                        hrefTarget: '_self'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('validation.pagingtoolbartop.itemsPerPage', 'CFG', 'Validation rules per page'),
                itemId: 'rulesListBottomPagingToolbar',
                isSecondPagination: me.isSecondPagination,
                params: {
                    ruleSetId: me.ruleSetId,
                    versionId: me.versionId
                }
            }
        ];

        me.callParent(arguments);
    }
});
