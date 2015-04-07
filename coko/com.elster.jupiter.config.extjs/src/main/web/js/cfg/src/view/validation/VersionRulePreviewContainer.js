Ext.define('Cfg.view.validation.VersionRulePreviewContainer', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.versionRulePreviewContainer',
    requires: [
        'Cfg.view.validation.RulePreviewContainerPanel',
        'Cfg.view.validation.VersionSubMenu'
    ],
    ruleSetId: null,
	versionId: null,
    initComponent: function () {
        var me = this;
        this.content = [
            {
                xtype: 'rule-preview-container-panel',
                ruleSetId: me.ruleSetId,
				versionId: this.versionId
            }
        ];
        this.side = [
            {
                xtype: 'panel',
                title: Uni.I18n.translate('validation.validationRuleSet', 'CFG', 'Validation rule set'),
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                ui: 'medium',
                items: [
                    {
                        xtype: 'versionSubMenu',
                        itemId: 'versionMenu',
                        ruleSetId: this.ruleSetId,
						versionId: this.versionId,
                        toggle: 1
                    }
                ]
            }
        ];
        this.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'versionSubMenu',
                        itemId: 'versionMenu',
                        ruleSetId: this.ruleSetId,
						versionId: this.versionId,
                    }
                ]
            }
        ];
        this.callParent(arguments);
    }
});

