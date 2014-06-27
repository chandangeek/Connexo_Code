Ext.define('Cfg.view.validation.RuleSetOverview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.ruleSetOverview',
    itemId: 'ruleSetOverview',
    requires: [
        'Cfg.model.ValidationRuleSet',
        'Cfg.view.validation.RuleSetSubMenu'
    ],

    ruleSetId: null,

    content: [
        {
            title: Uni.I18n.translate('general.overview', 'CFG', 'Overview'),
            ui: 'large',
            items: [
                {
                    xtype: 'form',
                    border: false,
                    itemId: 'rulesetOverviewForm',
                    name: 'rulesetOverviewForm',
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
                            labelWidth:	250
                        },
                        {
                            xtype: 'displayfield',
                            name: 'description',
                            fieldLabel: Uni.I18n.translate('validation.description', 'CFG', 'Description'),
                            labelAlign: 'right',
                            labelWidth:	250
                        },
                        {
                            xtype: 'fieldcontainer',
                            itemId: 'activeRules',
                            fieldLabel:  Uni.I18n.translate('validation.activeRules', 'CFG', 'Active rules'),
                            labelAlign: 'right',
                            labelWidth:	250,
                            layout: 'vbox'
                        },
                        {
                            xtype: 'fieldcontainer',
                            itemId: 'inactiveRules',
                            fieldLabel:  Uni.I18n.translate('validation.inactiveRules', 'CFG', 'Inactive rules'),
                            labelAlign: 'right',
                            labelWidth:	250,
                            layout: 'vbox'
                        }
                    ]
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
