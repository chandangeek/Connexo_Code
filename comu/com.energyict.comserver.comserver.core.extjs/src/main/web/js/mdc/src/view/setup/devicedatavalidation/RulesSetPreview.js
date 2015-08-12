Ext.define('Mdc.view.setup.devicedatavalidation.RulesSetPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceDataValidationRulesSetPreview',
    itemId: 'deviceDataValidationRulesSetPreview',
    title: '',
    ruleSetId: null,
    ui: 'medium',
    padding: 0,
    requires: [
        'Mdc.view.setup.devicedatavalidation.RuleSetVersionsGrid',
        'Mdc.view.setup.devicedatavalidation.RuleSetVersionPreview'
    ],
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'preview-container',
                grid: {
                    xtype: 'deviceDataValidationRuleSetVersionsGrid',
                    ruleSetId: me.ruleSetId
                },
                emptyComponent: {


                    xtype: 'no-items-found-panel',
                    itemId: 'ctr-no-validation-rule',
                    title: Uni.I18n.translate('validation.empty.versions.title', 'MDC', 'No validation rule set versions found'),
                    reasons: [
                        Uni.I18n.translate('validation.empty.versions.list.item1', 'MDC', 'No validation rule set versions have been added yet.')
                    ],
                    stepItems: [
                        {
                            text: Uni.I18n.translate('validation.addValidationRulesetVersion', 'MDC', 'Add validation rule set version'),
                            privileges : Cfg.privileges.Validation.admin,
                            ui: 'action',
                            href: '#/administration/validation/rulesets/' + me.ruleSetId  + '/versions/add'
                        }
                    ]
                },
                previewComponent: {
                    xtype: 'container',
                    itemId: 'deviceDataValidationRuleSetVersionsPreviewCt'
                }
            }
        ];
        me.callParent(arguments);
    }
});