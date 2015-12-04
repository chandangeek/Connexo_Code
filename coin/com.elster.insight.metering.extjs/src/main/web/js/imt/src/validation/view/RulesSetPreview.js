Ext.define('Imt.validation.view.RulesSetPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.usagePointDataValidationRulesSetPreview',
    itemId: 'usagePointDataValidationRulesSetPreview',
    title: '',
    ruleSetId: null,
    ui: 'medium',
    padding: 0,
    requires: [
        'Imt.validation.view.RuleSetVersionsGrid',
        'Imt.validation.view.RuleSetVersionPreview'
    ],
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'preview-container',
                grid: {
                    xtype: 'usagePointDataValidationRuleSetVersionsGrid',
                    ruleSetId: me.ruleSetId
                },
                emptyComponent: {


                    xtype: 'no-items-found-panel',
                    itemId: 'ctr-no-validation-rule',
                    title: Uni.I18n.translate('validation.empty.versions.title', 'IMT', 'No validation rule set versions found'),
                    reasons: [
                        Uni.I18n.translate('validation.empty.versions.list.item1', 'IMT', 'No validation rule set versions have been added yet.')
                    ],
                    stepItems: [
                        {
                            text: Uni.I18n.translate('validation.addValidationRulesetVersion', 'IMT', 'Add validation rule set version'),
                            privileges : Cfg.privileges.Validation.admin,
                            ui: 'action',
                            href: '#/administration/validation/rulesets/' + me.ruleSetId  + '/versions/add'
                        }
                    ]
                },
                previewComponent: {
                    xtype: 'container',
                    itemId: 'usagePointDataValidationRuleSetVersionsPreviewCt'
                }
            }
        ];
        me.callParent(arguments);
    }
});