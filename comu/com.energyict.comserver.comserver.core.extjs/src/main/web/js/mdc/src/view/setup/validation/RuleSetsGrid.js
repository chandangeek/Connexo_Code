Ext.define('Mdc.view.setup.validation.RuleSetsGrid', {
    extend: 'Ext.grid.Panel',
    xtype: 'validation-rulesets-grid',
    overflowY: 'auto',

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.DeviceConfigValidationRuleSets',
        'Mdc.view.setup.validation.RuleSetActionMenu'
    ],

    store: 'DeviceConfigValidationRuleSets',

    deviceTypeId: null,
    deviceConfigId: null,

    initComponent: function () {
        var me = this;

        this.columns = [
            {
                header: Uni.I18n.translate('validation.ruleSetName', 'MDC', 'Validation rule set'),
                dataIndex: 'name',
                renderer: function (value, metaData, record) {
                    metaData.tdAttr = 'data-qtip="' + record.get('description') + '"';
                    return '<a href="#/administration/validation/rulesets/' + record.getId() + '">' + Ext.String.htmlEncode(value) + '</a>';
                },
                flex: 3
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
                items: 'Mdc.view.setup.validation.RuleSetActionMenu'
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('validation.pagingtoolbartop.displayMsgRuleSet', 'MDC', '{0} - {1} of {2} validation rule sets'),
                displayMoreMsg: Uni.I18n.translate('validation.pagingtoolbartop.displayMoreMsgRuleSet', 'MDC', '{0} - {1} of more than {2} validation rule sets'),
                emptyMsg: Uni.I18n.translate('validation.pagingtoolbartop.emptyMsgRuleSet', 'MDC', 'There are no validation rule sets to display'),
                items: [
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('validation.addValidationRuleSets', 'MDC', 'Add validation rule sets'),
                        privileges: Cfg.privileges.Validation.deviceConfiguration,
                        href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigId + '/validationrulesets/add'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('validation.pagingtoolbarbottom.itemsPerPageRuleSet', 'MDC', 'Validation rule sets per page'),
                dock: 'bottom'
            }
        ];

        me.callParent();
    }
});

