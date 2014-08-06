Ext.define('Mdc.view.setup.devicedatavalidation.RulesSetPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceDataValidationRulesSetPreview',
    itemId: 'deviceDataValidationRulesSetPreview',
    title: '',
    rulesSetId: null,
    ui: 'medium',
    padding: 0,
    requires: [
        'Mdc.view.setup.devicedatavalidation.RulesGrid',
        'Mdc.view.setup.devicedatavalidation.RulePreview'
    ],
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'preview-container',
                grid: {
                    xtype: 'deviceDataValidationRulesGrid',
                    rulesSetId: me.rulesSetId
                },
                emptyComponent: {
                    xtype: 'no-items-found-panel',
                    title: Uni.I18n.translate('validation.empty.title', 'MDC', 'No validation rules found'),
                    reasons: [
                        Uni.I18n.translate('validation.empty.list.item1', 'MDC', 'No validation rules have been defined yet.'),
                        Uni.I18n.translate('validation.empty.list.item2', 'MDC', 'Validation rules exist, but you do not have permission to view them.')
                    ],
                    stepItems: [
                        {
                            text: Uni.I18n.translate('validation.addValidationRule', 'CFG', 'Add validation rule'),
                            ui: 'action',
                            href: '#/administration/validation/rulesets/' + me.rulesSetId + '/rules/add'
                        }
                    ]
                },
                previewComponent: {
                    xtype: 'deviceDataValidationRulePreview'
                }
            }
        ];
        me.callParent(arguments);
    }
});