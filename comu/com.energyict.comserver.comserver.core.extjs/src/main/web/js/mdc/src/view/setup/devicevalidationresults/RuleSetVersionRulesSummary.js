Ext.define('Mdc.view.setup.devicevalidationresults.RuleSetVersionRulesSummary', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.ruleSetVersionRulesSummary',

    requires: [
        'Mdc.view.setup.devicevalidationresults.RuleSetVersionRuleList'
    ],

    content: [
        {
            ui: 'large',
            title: Uni.I18n.translate('validationResults.validationRuleSets', 'MDC', 'Validation rule sets'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'ruleSetVersionRuleList'
                    }//	,
                    /*emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'ctr-no-validation-rule-set',
                        title: Uni.I18n.translate('validation.empty.title', 'CFG', 'No validation rule sets found'),
                        reasons: [
                            Uni.I18n.translate('validation.empty.list.item1', 'CFG', 'No validation rule sets have been added yet.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('validation.addRuleSet', 'CFG', 'Add validation rule set'),
                                privileges: ['privilege.administrate.validationConfiguration'],
                                href: '#/administration/validation/rulesets/add'
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'rulesetversionsSummary',
                        itemId: 'ruleSetBrowsePreviewCt'
                    }*/

                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});
