Ext.define('Cfg.view.validation.RulePreviewContainer', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.rulePreviewContainer',

    requires: [
        'Cfg.view.validation.RuleList',
        'Cfg.view.validation.RulePreview',
        'Cfg.view.validation.RuleActionMenu',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Cfg.view.validation.RuleSetSubMenu'
    ],

    ruleSetId: null,

    initComponent: function () {
        var me = this;
        this.content = [
            {
                ui: 'large',
                xtype: 'panel',
                title: Uni.I18n.translate('validation.validationRules', 'CFG', 'Validation rules'),
                overflowY: 'auto',
                items: [
                    {
                        xtype: 'preview-container',
                        itemId: 'previewRuleContainer',
                        grid: {
                            xtype: 'validationruleList',
                            ruleSetId: me.ruleSetId
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            title: Uni.I18n.translate('validation.empty.rules.title', 'CFG', 'No validation rules found'),
                            reasons: [
                                Uni.I18n.translate('validation.empty.rules.list.item1', 'CFG', 'No validation rules have been added yet.')
                            ],
                            stepItems: [
                                {
                                    text: Uni.I18n.translate('validation.addValidationRule', 'CFG', 'Add validation rule'),
                                    itemId: 'addRuleLink',
                                    ui: 'action',
                                    listeners: {
                                        click: {
                                            fn: function () {
                                                window.location.href = '#/administration/validation/rulesets/' + me.ruleSetId + '/rules/add';
                                            }
                                        }
                                    }
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'validation-rule-preview'
                        }
                    }
                ]
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
        this.callParent(arguments);
    }
});

