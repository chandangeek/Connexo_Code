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
                                    fieldLabel: Uni.I18n.translate('general.description', 'CFG', 'Description'),
                                    labelAlign: 'right',
                                    labelWidth: 250
                                },
								{
                                    xtype: 'displayfield',
                                    itemId: 'activeVersion',
                                    name: 'activeVersion',
                                    fieldLabel: Uni.I18n.translate('validation.activeVersion', 'CFG', 'Active version'),
                                    labelAlign: 'right',
                                    labelWidth: 250
                                }								
                            ]
                        }
                    ]
                },
                {
                    xtype: 'button',
                    text: Uni.I18n.translate('general.actions', 'CFG', 'Actions'),
                    privileges: Cfg.privileges.Validation.admin,
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
                ui: 'medium',
                items: [
                    {
                        xtype: 'ruleSetSubMenu',
                        itemId: 'stepsMenu',
                        ruleSetId: this.ruleSetId
                    }
                ]
            }
        ];
        this.callParent(arguments);
    }
});
