Ext.define('Cfg.view.validation.RuleOverview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.ruleOverview',
    itemId: 'ruleOverview',
    requires: [
        'Cfg.model.ValidationRule',
        'Cfg.view.validation.RuleSubMenu',
        'Cfg.view.validation.RuleActionMenu',
        'Cfg.view.validation.RulePreview'
    ],

	ruleSetId: null,
	versionID: null,
    ruleId: null,
	
    

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
                            xtype: 'validation-rule-preview',
                            frame: false,
                            margin: '-30 0 0 -10'
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
                ui: 'medium',
                items: [
                    {
                        xtype: 'ruleSubMenu',
                        itemId: 'stepsRuleMenu',
                        ruleSetId: this.ruleSetId,
						versionId: this.versionId,
                        ruleId: this.ruleId
                    }
                ]
            }
        ];
        this.callParent(arguments);
    }
});

