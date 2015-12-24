Ext.define('Mdc.view.setup.devicedatavalidation.RuleSetVersionPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceDataValidationRulesSetVersionPreview',
    itemId: 'deviceDataValidationRulesSetVersionPreview',
    title: '',
    ruleSetId: null,
    rulesSetVersionId: null,
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
                    ruleSetId: me.ruleSetId,
                    versionId: me.versionId
                },
                emptyComponent: {
                    xtype: 'no-items-found-panel',
                    title: Uni.I18n.translate('validation.rules.empty.title', 'MDC', 'No validation rules found'),
                    reasons: [
                        Uni.I18n.translate('validation.rules.empty.list.item1', 'MDC', 'No validation rules have been defined yet.')
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