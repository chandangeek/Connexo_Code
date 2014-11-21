Ext.define('Cfg.view.validation.RulePreviewContainerPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.rule-preview-container-panel',
    itemId: 'rulePreviewContainerPanel',
    title: Uni.I18n.translate('validation.validationRules', 'CFG', 'Validation rules'),
    ui: 'large',
    ruleSetId: null,
    isSecondPagination: false,
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
                    ruleSetId: me.ruleSetId,
                    isSecondPagination: me.isSecondPagination
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
                            privileges: ['privilege.administrate.validationConfiguration'],
                            href: '#/administration/validation/rulesets/' + me.ruleSetId  + '/rules/add'
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
