Ext.define('Mdc.view.setup.validation.AddRuleSetsGrid', {
    extend: 'Ext.grid.Panel',
    xtype: 'validation-add-rulesets-grid',

    requires: [
        'Mdc.view.setup.validation.AddRuleSetActionMenu',
        'Ext.grid.plugin.BufferedRenderer'
    ],

    store: Ext.getStore('ValidationRuleSets') || Ext.create('Cfg.store.ValidationRuleSets'),

    selType: 'checkboxmodel',
    selModel: {
        mode: 'MULTI',
        showHeaderCheckbox: false
    },

    deviceTypeId: null,
    deviceConfigId: null,

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('validation.ruleSetName', 'MDC', 'Validation rule set'),
                dataIndex: 'name',
                renderer: function (value, b, record) {
                    return '<a href="#/administration/validation/rulesets/' + record.getId() + '">' + value + '</a>';
                },
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
                layout: 'vbox',
                items: [
                    {
                        xtype: 'radiogroup',
                        itemId: 'radiogroupAddRuleSet',
                        columns: 1,
                        vertical: true,
                        submitValue: false,
                        defaults: {
                            padding: '0 0 30 0'
                        },
                        items: [
                            {
                                itemId: 'radioAll',
                                boxLabel: '<b>' + Uni.I18n.translate('ruleset.allRuleSets', 'MDC', 'All validation rule sets') + '</b><br/>' +
                                    '<span style="color: grey;">' + Uni.I18n.translate('ruleset.selectAllRuleSets', 'MDC', 'Select all validation rule sets related to device configuration') + '</span>',
                                name: 'rulesetsRadio',
                                inputValue: 'ALL'
                            },
                            {
                                itemId: 'radioSelected',
                                boxLabel: '<b>' + Uni.I18n.translate('ruleset.selectedRuleSets', 'MDC', 'Selected validation rule sets') + '</b><br/><span style="color: grey;">' + Uni.I18n.translate('ruleset.selectRuleSets', 'MDC', 'Select validation rule sets in table') + '</span>',
                                name: 'rulesetsRadio',
                                checked: true,
                                inputValue: 'SELECTED'
                            }
                        ]
                    },
                    {
                        xtype: 'container',
                        items: [
                            {
                                xtype: 'text',
                                itemId: 'selection-counter',
                                text: Uni.I18n.translate('validation.noValidationRuleSetSelected', 'MDC', 'No validation rule set selected')
                            },
                            {
                                xtype: 'button',
                                text: Uni.I18n.translate('general.uncheckAll', 'MDC', 'Uncheck all'),
                                action: 'uncheckAll'
                            }
                        ]
                    }
                ]
            },
            {
                xtype: 'toolbar',
                dock: 'bottom',
                margin: '0 0 0 -10',
                items: [
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                        action: 'addValidationRuleSets',
                        ui: 'action'
                    },
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                        action: 'cancel',
                        ui: 'link',
                        href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigId + '/validationrulesets'
                    }
                ]
            }
        ];

        me.callParent();
    }
});

