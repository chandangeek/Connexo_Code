Ext.define('Mdc.view.setup.devicedatavalidation.RulesSetPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceDataValidationRulesSetPreview',
    itemId: 'deviceDataValidationRulesSetPreview',
    title: '',
    rulesSetId: null,
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
                    rulesSetId: me.rulesSetId
                },
                emptyComponent: {


                    xtype: 'no-items-found-panel',
                    itemId: 'ctr-no-validation-rule',
                    title: Uni.I18n.translate('validation.empty.versions.title', 'CFG', 'No validation rule set versions found'),
                    reasons: [
                        Uni.I18n.translate('validation.empty.versions.list.item1', 'CFG', 'No validation rule set versions have been added yet.')
                    ],
                    stepItems: [
                        {
                            text: Uni.I18n.translate('validation.addValidationRulesetVersion', 'CFG', 'Add validation rule set version'),
                            privileges : Cfg.privileges.Validation.admin,
                            ui: 'action',
                            href: '#/administration/validation/rulesets/' + me.ruleSetId  + '/versions/add'
                        }
                    ]
                },
                previewComponent: {
                    xtype: 'deviceDataValidationRulesSetVersionPreview'
                }
            }
        ];
        me.callParent(arguments);
    }
});