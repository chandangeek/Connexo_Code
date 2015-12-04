Ext.define('Imt.validation.view.RuleSetVersionPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.usagePointDataValidationRulesSetVersionPreview',
    itemId: 'usagePointDataValidationRulesSetVersionPreview',
    title: '',
    ruleSetId: null,
    rulesSetVersionId: null,
    ui: 'medium',
    padding: 0,
    requires: [
        'Imt.validation.view.RulesGrid',
        'Imt.validation.view.RulePreview'
    ],
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'preview-container',
                grid: {
                    xtype: 'usagePointDataValidationRulesGrid',
                    ruleSetId: me.ruleSetId,
                    versionId: me.versionId
                },
                emptyComponent: {
                    xtype: 'no-items-found-panel',
                    title: Uni.I18n.translate('validation.rules.empty.title', 'IMT', 'No validation rules found'),
                    reasons: [
                        Uni.I18n.translate('validation.rules.empty.list.item1', 'IMT', 'No validation rules have been defined yet.')
                    ],
                    stepItems: [
                        {
                            text: Uni.I18n.translate('validation.addValidationRule', 'IMT', 'Add validation rule'),
                            privileges : Cfg.privileges.Validation.device,
                            ui: 'action',
                            href: '#/administration/validation/rulesets/' + me.ruleSetId + '/versions/' + me.versionId + '/rules/add'
                        }
                    ]
                },
                previewComponent: {
                    xtype: 'usagePointDataValidationRulePreview'
                }
            }
        ];
        me.callParent(arguments);
    }
});