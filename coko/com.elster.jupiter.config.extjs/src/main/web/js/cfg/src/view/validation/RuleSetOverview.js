Ext.define('Cfg.view.validation.RuleSetOverview', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'ruleSetOverview',
    itemId: 'ruleSetOverview',

    requires: [
        'Cfg.model.ValidationRuleSet',
        'Cfg.view.validation.RuleSetSubMenu',
        'Cfg.view.validation.RuleSetActionMenu'
    ],

    ruleSetId: null,

    content: [
        {
            xtype: 'container',
            layout: 'hbox',
            items: [
                {
                    title: Uni.I18n.translate('general.overview', 'CFG', 'Overview'),
                    ui: 'large',
                    flex: 1,
                    items: [
                        {
                            xtype: 'form',
                            border: false,
                            itemId: 'rulesetOverviewForm',
                            layout: {
                                type: 'vbox',
                                align: 'stretch'
                            },
                            items: [
                                {
                                    xtype: 'displayfield',
                                    name: 'name',
                                    fieldLabel: Uni.I18n.translate('validation.validationRuleSet', 'CFG', 'Validation rule set'),
                                    labelAlign: 'right',
                                    labelWidth: 250
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'description',
                                    fieldLabel: Uni.I18n.translate('validation.description', 'CFG', 'Description'),
                                    labelAlign: 'right',
                                    labelWidth: 250
                                },
                                {
                                    xtype: 'displayfield',
                                    itemId: 'activeRules',
                                    name: 'active_rules',
                                    fieldLabel: Uni.I18n.translate('validation.activeRules', 'CFG', 'Active rules'),
                                    labelAlign: 'right',
                                    labelWidth: 250
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'numberOfInactiveRules',
                                    fieldLabel: Uni.I18n.translate('validation.inactiveRules', 'CFG', 'Inactive rules'),
                                    labelAlign: 'right',
                                    labelWidth: 250
                                }
                            ]
                        }
                    ]
                },
                {
                    xtype: 'button',
                    text: Uni.I18n.translate('general.actions', 'CFG', Uni.I18n.translate('general.actions', 'CFG', 'Actions')),
                    iconCls: 'x-uni-action-iconD',
                    margin: '20 0 0 0',
                    menu: {
                        xtype: 'ruleset-action-menu'
                    }
                }
            ]
        }
    ],

    initComponent: function () {
        this.side = [
            {
                xtype: 'panel',
                title: Uni.I18n.translate('validation.validationRuleSet', 'CFG', 'Validation rule set'),
                ui: 'medium',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'ruleSetSubMenu',
                        itemId: 'stepsMenu',
                        ruleSetId: this.ruleSetId,
                        toggle: 0
                    }
                ]
            }
        ];
        this.callParent(arguments);
    }
});
