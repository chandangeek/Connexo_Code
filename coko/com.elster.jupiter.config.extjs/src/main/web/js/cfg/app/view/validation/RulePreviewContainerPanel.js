Ext.define('Cfg.view.validation.RulePreviewContainerPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.rule-preview-container-panel',
    itemId: 'rulePreviewContainerPanel',
    title: Uni.I18n.translate('validation.validationRules', 'CFG', 'Validation rules'),
    ui: 'large',
    overflowY: 'auto',
    ruleSetId: null,
    requires: [
        'Cfg.view.validation.RuleList',
        'Cfg.view.validation.RulePreview',
        'Cfg.view.validation.RuleActionMenu',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],
    initComponent: function () {
        var me = this;
        me.items = [
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
                        Uni.I18n.translate('validation.empty.rules.list.item1', 'CFG', 'No validation rules have been added yet.'),
                        Uni.I18n.translate('validation.empty.list.item2', 'MDC', 'Validation rules exists, but you do not have permission to view them.')
                    ],
                    stepItems: [
                        {
                            text: Uni.I18n.translate('validation.addValidationRule', 'CFG', 'Add validation rule'),
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
        ];
        me.callParent(arguments);
    }
});
