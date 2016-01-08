Ext.define('Imt.metrologyconfiguration.view.validation.RulesOverview', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'validation-rules-overview',

    requires: [
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Imt.metrologyconfiguration.view.validation.RuleSetsGrid',
        'Imt.metrologyconfiguration.view.validation.RulesGrid',
        'Imt.metrologyconfiguration.view.validation.RuleSetView'
    ],

    mcid: null,
    validationRuleSetId: null,
    router: null,
    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'metrology-configuration-side-menu',
                        itemId: 'stepsMenu',
                        router: me.router,
                        mcid: me.mcid
                    }
                ]
            }
        ];

        me.content = [
            {
                ui: 'large',
                xtype: 'panel',
                title: Uni.I18n.translate('validation.validationRuleSets', 'IMT', 'Validation rule sets'),

                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'validation-rulesets-grid',
                            itemId: 'grd-validation-rule-sets',
                            router: me.router,
                            mcid: me.mcid
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'ctr-no-validation-rule-set',
                            title: Uni.I18n.translate('validation.rulesets.empty.title', 'IMT', 'No validation rule sets found'),
                            reasons: [
                                Uni.I18n.translate('device.dataValidation.rulesSetGrid.emptyCmp.item1', 'IMT', 'No validation rule sets have been defined yet.'),
                                Uni.I18n.translate('device.dataValidation.rulesSetGrid.emptyCmp.item2', 'IMT', 'Validation rule sets exist, but you do not have permission to view them.')
                            ],
//                            stepItems: [
//                                {
//                                    text: Uni.I18n.translate('validation.addValidationRuleSets', 'IMT', 'Add validation rule sets'),
//                              //      privileges : Cfg.privileges.Validation.deviceConfiguration,
//                                    href: '#/administration/metrologyconfiguration/' + me.mcid + '/associatedvalidationrulesets/addruleset'
//                                }
//                            ]
                        },
                        previewComponent: {
                            xtype: 'validation-ruleset-view'
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    updateValidationRuleSet: function (validationRuleSet) {
        var preview = this.down('validation-ruleset-view');
        preview.updateValidationRuleSet(validationRuleSet);
    }
});