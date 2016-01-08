Ext.define('Imt.metrologyconfiguration.view.validation.AddRuleSets', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'validation-add-rulesets',

    requires: [
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Imt.metrologyconfiguration.view.validation.AddRuleSetsGrid',
        'Imt.metrologyconfiguration.view.validation.RuleSetView'
    ],

    mcid: null,
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
                title: Uni.I18n.translate('validation.validationRules', 'IMT', 'Add validation rule sets'),

                items: [
                    {
                        xtype: 'preview-container',
                        itemId: 'previewContainer',
                        selectByDefault: false,
                        grid: {
                            xtype: 'validation-add-rulesets-grid',
                            itemId: 'grid-add-rule-sets',
                            router: me.router,
                            mcid: me.mcid
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'ctr-add-no-validation-rule-set',
                            title: Uni.I18n.translate('validation.rulesets.empty.title', 'IMT', 'No validation rule sets found'),
                            reasons: [
                                Uni.I18n.translate('validation.rulesets.empty.list.item1', 'IMT', 'No validation rule sets have been added yet.'),
                                Uni.I18n.translate('validation.rulesets.empty.list.item2', 'IMT', 'Validation rule sets exists, but you do not have permission to view them.')
                            ],
//                            stepItems: [
//                                {
//                                    xtype: 'button',
//                                    text: Uni.I18n.translate('validation.addValidationRuleSets', 'IMT', 'Add validation rule sets'),
//                                    privileges : Cfg.privileges.Validation.deviceConfiguration,
//                                    href: '#/administration/validation/rulesets/add'
//                                }
//                            ]
                        },
                        previewComponent: {
                            xtype: 'validation-ruleset-view',
                            hidden: true
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    updateValidationRuleSet: function (validationRuleSet) {
        this.down('validation-ruleset-view').updateValidationRuleSet(validationRuleSet);
    }
});