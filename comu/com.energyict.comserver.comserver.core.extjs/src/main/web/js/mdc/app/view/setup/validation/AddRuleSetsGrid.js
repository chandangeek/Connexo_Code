Ext.define('Mdc.view.setup.validation.AddRuleSetsGrid', {
    extend: 'Ext.grid.Panel',
    xtype: 'validation-add-rulesets-grid',
    overflowY: 'auto',

    requires: [
        'Mdc.store.ValidationRuleSets',
        'Mdc.view.setup.validation.AddRuleSetActionMenu'
    ],

    store: 'ValidationRuleSets',
    selType: 'checkboxmodel',
    selModel: {
        mode: 'SINGLE'
    },

    deviceTypeId: null,
    deviceConfigId: null,

    initComponent: function () {
        var me = this;

        this.columns = [
            {
                header: Uni.I18n.translate('validation.ruleSetName', 'MDC', 'Validation rule set'),
                dataIndex: 'name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('validation.status', 'MDC', 'Status'),
                dataIndex: 'active',
                flex: 1
            },
            {
                header: Uni.I18n.translate('validation.activeRules', 'MDC', 'Active rule(s)'),
                dataIndex: 'numberOfRules',
                flex: 1
            },
            {
                header: Uni.I18n.translate('validation.inactiveRules', 'MDC', 'Inactive rule(s)'),
                dataIndex: 'numberOfInactiveRules',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                items: 'Mdc.view.setup.validation.AddRuleSetActionMenu'
            }
        ];

        me.dockedItems = [
            {
                xtype: 'toolbar',
                dock: 'top',
                items: [
                    {
                        // TODO Selection text.
                        xtype: 'component',
                        html: 'Here be dragons.'
                    },
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.uncheckAll', 'MDC', 'Uncheck all'),
                        action: 'uncheckAll'
                    }
                ]
            },
            {
                xtype: 'toolbar',
                dock: 'bottom',
                items: [
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                        action: 'add',
                        ui: 'action'
                    },
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                        action: 'cancel',
                        ui: 'link',
                        href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigId + '/validationrules'
                    }
                ]
            }
        ];

        me.callParent();
    }
});

