Ext.define('Mdc.view.setup.validation.RuleSetsGrid', {
    extend: 'Ext.grid.Panel',
    xtype: 'validation-rulesets-grid',
    overflowY: 'auto',

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.DeviceConfigValidationRuleSets'
    ],

    store: 'DeviceConfigValidationRuleSets',

    deviceTypeId: null,
    deviceConfigId: null,

    initComponent: function () {
        var me = this;

        this.columns = [
            {
                header: Uni.I18n.translate('validation.ruleSetName', 'MDC', 'Validation rule set'),
                dataIndex: 'name'
            },
            {
                header: Uni.I18n.translate('validation.status', 'MDC', 'Status'),
                dataIndex: 'active'
            },
            {
                header: Uni.I18n.translate('validation.activeRules', 'MDC', 'Active rule(s)'),
                dataIndex: 'numberOfRules'
            },
            {
                header: Uni.I18n.translate('validation.inactiveRules', 'MDC', 'Inactive rule(s)'),
                dataIndex: 'numberOfInactiveRules'
            }
//            ,
//            {
//                xtype: 'uni-actioncolumn',
//                items: 'Mdc.view.setup.validation.RuleSetActionMenu'
//            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('validation.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} validation rule sets'),
                displayMoreMsg: Uni.I18n.translate('validation.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} validation rule sets'),
                emptyMsg: Uni.I18n.translate('validation.pagingtoolbartop.emptyMsg', 'MDC', 'There are no validation rule sets to display'),
                items: [
                    {
                        xtype: 'component',
                        flex: 1
                    },
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('validation.addValidationRuleSets', 'MDC', 'Add validation rule sets'),
                        href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigId + '/validationrules/add'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('validation.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Validation rule sets per page'),
                dock: 'bottom'
            }
        ];

        me.callParent();
    }
});

