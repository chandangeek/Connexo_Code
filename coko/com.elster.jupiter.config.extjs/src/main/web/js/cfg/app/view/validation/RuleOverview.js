Ext.define('Cfg.view.validation.RuleOverview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.ruleOverview',
    itemId: 'ruleOverview',
    requires: [
        'Cfg.model.ValidationRule',
        'Cfg.view.validation.RuleSubMenu',
        'Cfg.view.validation.RuleActionMenu'
    ],

    ruleId: null,
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
                            itemId: 'ruleOverviewForm',
                            name: 'ruleOverviewForm',
                            layout: {
                                type: 'vbox',
                                align: 'stretch'
                            },
                            defaults: {
                                xtype: 'displayfield',
                                labelWidth: 260,
                                labelAlign: 'right'
                            },
                            items: [
                                {
                                    name: 'displayName',
                                    fieldLabel: Uni.I18n.translate('validation.validationRule', 'CFG', 'Validation rule'),
                                    labelAlign: 'right'
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'active',
                                    fieldLabel: Uni.I18n.translate('validation.status', 'CFG', 'Status'),
                                    renderer: function (value) {
                                        if (value) {
                                            return Uni.I18n.translate('validation.active', 'CFG', 'Active')
                                        } else {
                                            return Uni.I18n.translate('validation.inactive', 'CFG', 'Inactive')
                                        }
                                    }
                                },
                                {
                                    xtype: 'container',
                                    itemId: 'readingTypesArea',
                                    items: []
                                },
                                {
                                    xtype: 'container',
                                    margin: '5 0 0 0',
                                    itemId: 'propertiesArea',
                                    items: []
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
                        xtype: 'validation-rule-action-menu'
                    }
                }
            ]
        }
    ],

    initComponent: function () {
        this.side = [
            {
                xtype: 'panel',
                title: Uni.I18n.translate('validation.validationRule', 'CFG', 'Validation rule'),
                ui: 'medium',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'ruleSubMenu',
                        itemId: 'stepsRuleMenu',
                        ruleSetId: this.ruleSetId,
                        ruleId: this.ruleId,
                        toggle: 0
                    }
                ]
            }
        ];
        this.callParent(arguments);
    }
});

