Ext.define('Cfg.view.validation.RuleSetList', {
    extend: 'Ext.grid.Panel',
    border: true,
    alias: 'widget.validationrulesetList',
    itemId: 'validationrulesetList',

    store: 'ValidationRuleSets',

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Uni.grid.column.Action',
        'Cfg.view.validation.RuleSetActionMenu'
    ],

    columns: {
        items: [
            { 
				header: Uni.I18n.translate('validation.validationRuleSet', 'CFG', 'Validation rule set'), 
				dataIndex: 'name', 
				flex: 3, 
				sortable: false, 
				fixed: true,
                renderer: function (value, metaData, record) {
                    metaData.tdAttr = 'data-qtip="' + Ext.String.htmlEncode(Ext.String.htmlEncode(record.get('description'))) + '"';
                    return '<a href="#/administration/validation/rulesets/' + record.getId() + '">' + Ext.String.htmlEncode(value) + '</a>'
                }
            },
			{
                header: Uni.I18n.translate('validation.activeVersion', 'CFG', 'Active version'),
                dataIndex: 'activeVersion',
                flex: 5,
                align: 'left',
                sortable: false,
                fixed: true
            },	
            {
                xtype: 'uni-actioncolumn',				
                privileges: Cfg.privileges.Validation.admin,
                items: 'Cfg.view.validation.RuleSetActionMenu'
            }
        ]
    },

    initComponent: function () {
        var me = this;

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                displayMsg: Uni.I18n.translate('validation.ruleset.display.msg', 'CFG', '{0} - {1} of {2} validation rule sets'),
                displayMoreMsg: Uni.I18n.translate('validation.ruleset.display.more.msg', 'CFG', '{0} - {1} of more than {2} validation rule sets'),
                emptyMsg: Uni.I18n.translate('validation.ruleset.pagingtoolbartop.emptyMsg', 'CFG', 'There are no validation rule sets to display'),
                dock: 'top',
                items: [
                    {
                        text: Uni.I18n.translate('validation.addRuleSet', 'CFG', 'Add validation rule set'),
                        privileges: Cfg.privileges.Validation.admin,
                        itemId: 'newRuleset',
                        xtype: 'button',
                        href: '#/administration/validation/rulesets/add',
                        hrefTarget: '_self'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: 'Validation rule sets per page',
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }

});
