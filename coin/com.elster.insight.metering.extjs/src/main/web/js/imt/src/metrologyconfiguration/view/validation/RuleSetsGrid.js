Ext.define('Imt.metrologyconfiguration.view.validation.RuleSetsGrid', {
    extend: 'Ext.grid.Panel',
    xtype: 'validation-rulesets-grid',
    overflowY: 'auto',

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Imt.metrologyconfiguration.store.LinkedValidationRulesSet',
        'Imt.metrologyconfiguration.view.validation.RuleSetActionMenu'
    ],

    store: 'Imt.metrologyconfiguration.store.LinkedValidationRulesSet',

    mcid: null,

    initComponent: function () {
        var me = this;

        this.columns = [
            {
                header: Uni.I18n.translate('validation.ruleSetName', 'IMT', 'Validation rule set'),
                dataIndex: 'name',
                renderer: function (value, metaData, record) {
                    metaData.tdAttr = 'data-qtip="' + record.get('description') + '"';
                    return '<a href="#/administration/validation/rulesets/' + record.getId() + '">' + Ext.String.htmlEncode(value) + '</a>';
                },
                flex: 3
            },
			{
                header: Uni.I18n.translate('validation.activeVersion', 'IMT', 'Active version'),
                dataIndex: 'activeVersion',
                flex: 5,
                align: 'left',
                sortable: false,
                fixed: true
            },	            
            {
                xtype: 'uni-actioncolumn',
                items: 'Imt.metrologyconfiguration.view.validation.RuleSetActionMenu'
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('validation.pagingtoolbartop.displayMsgRuleSet', 'IMT', '{0} - {1} of {2} validation rule sets'),
                displayMoreMsg: Uni.I18n.translate('validation.pagingtoolbartop.displayMoreMsgRuleSet', 'IMT', '{0} - {1} of more than {2} validation rule sets'),
                emptyMsg: Uni.I18n.translate('validation.pagingtoolbartop.emptyMsgRuleSet', 'IMT', 'There are no validation rule sets to display'),
//                items: [
//                    {
//                        xtype: 'button',
//                        text: Uni.I18n.translate('validation.addValidationRuleSets', 'IMT', 'Add validation rule sets'),
//                    //    privileges: Cfg.privileges.Validation.deviceConfiguration,
//                        href: '#/administration/metrologyconfiguration/' + encodeURIComponent(me.mcid) + '/associatedvalidationrulesets/addruleset'
//                    }
//                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('validation.pagingtoolbarbottom.itemsPerPageRuleSet', 'IMT', 'Validation rule sets per page'),
                dock: 'bottom'
            }
        ];

        me.callParent();
    }
});

