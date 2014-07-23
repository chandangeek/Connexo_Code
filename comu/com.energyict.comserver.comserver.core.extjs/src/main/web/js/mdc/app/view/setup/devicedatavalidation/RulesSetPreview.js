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
                        Uni.I18n.translate('validation.empty.list.item1', 'MDC', 'No validation rules have been included in this validation rules set.'),
                        Uni.I18n.translate('validation.empty.list.item2', 'MDC', 'Validation rules exists, but you do not have permission to view them.')
                    ],
                    stepItems: []
                },
                previewComponent: {
                    xtype: 'deviceDataValidationRulePreview'
                }
            }
        ];
        me.callParent(arguments);
    }
});