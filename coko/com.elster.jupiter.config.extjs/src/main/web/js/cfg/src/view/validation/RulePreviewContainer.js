/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.validation.RulePreviewContainer', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.rulePreviewContainer',
    requires: [
        'Cfg.view.validation.RulePreviewContainerPanel',		        
        'Cfg.view.validation.RuleSetSubMenu'		      
    ],
    ruleSetId: null,
    initComponent: function () {
        var me = this;
        this.content = [
            {
                xtype: 'rule-preview-container-panel',				
                ruleSetId: me.ruleSetId
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
                        xtype: 'ruleSetSubMenu',
                        itemId: 'stepsMenu',
                        ruleSetId: this.ruleSetId,
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

