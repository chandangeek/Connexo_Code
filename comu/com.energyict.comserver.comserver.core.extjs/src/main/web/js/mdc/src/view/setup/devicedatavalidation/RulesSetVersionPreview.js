Ext.define('Mdc.view.setup.devicedatavalidation.RuleSetVersionPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceDataValidationRulesSetVersionPreview',
    itemId: 'deviceDataValidationRulesSetVersionPreview',
    title: '',
    rulesSetId: null,
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
                    rulesSetId: me.rulesSetId,
                    rulesSetVersionId: me.rulesSetVersionId
                },
                emptyComponent: {
                    xtype: 'no-items-found-panel',
                    title: Uni.I18n.translate('validation.empty.title', 'MDC', 'No validation rules found'),
                    reasons: [
                        Uni.I18n.translate('validation.empty.list.item1', 'MDC', 'No validation rules have been defined yet.')
                    ],
                    stepItems: [
                        {
                            text: Uni.I18n.translate('validation.addValidationRule', 'CFG', 'Add validation rule'),
                            privileges:['privilege.view.fineTuneValidationConfiguration.onDevice'],
                            ui: 'action',
                            href: '#/administration/validation/rulesets/' + me.rulesSetId + '/versions/' + me.rulesSetVersionId + '/rules/add'
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