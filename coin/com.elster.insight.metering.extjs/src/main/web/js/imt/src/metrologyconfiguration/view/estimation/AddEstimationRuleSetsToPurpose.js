Ext.define('Imt.metrologyconfiguration.view.estimation.AddEstimationRuleSetsToPurpose', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.add-estimation-rule-sets-to-purpose',
    requires: [
        'Imt.metrologyconfiguration.view.MetrologyConfigurationSideMenu',
        'Imt.metrologyconfiguration.view.estimation.AddEstimationRuleSetsToPurposeGrid',
        'Imt.metrologyconfiguration.view.estimation.RulesGrid',
        'Est.estimationrules.view.DetailForm',
        'Imt.metrologyconfiguration.model.PurposeWithValidationRuleSets',
        'Imt.metrologyconfiguration.model.EstimationRuleSet',
        'Imt.store.EstimationRules'
    ],
    router: null,
    metrologyConfig: null,
    purposesStore: null,
    rulesStore: null,
    purposeWithLinkableRuleSets: null,

    initComponent: function () {
        var me = this,
            purpose = me.purposeWithLinkableRuleSets;

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('estimation.addRuleSet', 'IMT', 'Add estimation rule set'),
            items: [
                {
                    xtype: 'combobox',
                    itemId: 'purpose-combo',
                    name: 'metrologyContract',
                    store: me.purposesStore,
                    editable: false,
                    valueField: 'id',
                    displayField: 'name',
                    queryMode: 'local',
                    fieldLabel: Uni.I18n.translate('estimation.addRuleSet.purpose', 'IMT', 'Purpose'),
                    value: me.purposesStore.first(),
                    labelAlign: 'left',
                    forceSelection: true,
                    labelWidth: 60,
                    width: 200
                },
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'add-estimation-rule-sets-to-purpose-grid',
                        itemId: 'add-estimation-rule-sets-to-purpose-grid',
                        store: purpose.estimationRuleSets(),
                        router: me.router,
                        bbar: [
                            {
                                xtype: 'button',
                                itemId: 'add-estimation-rule-sets-to-purpose-add-button',
                                text: Uni.I18n.translate('general.add', 'IMT', 'Add'),
                                ui: 'action',
                                action: 'add',
                                width: 47,
                                margin: '80 0 -40 0',
                                disabled: true
                            },
                            {
                                xtype: 'button',
                                itemId: 'add-estimation-rule-sets-to-purpose-cancel-button',
                                text: Uni.I18n.translate('general.cancel', 'IMT', 'Cancel'),
                                ui: 'link',
                                width: 80,
                                action: 'cancel',
                                margin: '80 0 -40 0',
                                href: me.router.getRoute('administration/metrologyconfiguration/view/estimation').buildUrl()
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'est-purpose-rules-grid',
                            router: me.router,
                            itemId: 'add-estimation-rule-sets-purpose-rules-grid',
                            store: me.rulesStore
                        },
                        previewComponent: {
                            xtype: 'estimation-rules-detail-form',
                            itemId: 'add-estimation-rules-detail-form',
                            frame: true,
                            noActionsButton: true,
                            title: ''
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'purpose-add-estimation-rule-sets-no-estimation-rules',
                            title: Uni.I18n.translate('estimation.rules.empty.title', 'IMT', 'No estimation rules found'),
                            reasons: [
                                Uni.I18n.translate('estimation.rules.empty.list.item1', 'IMT', 'No estimation rules have been defined yet.')
                            ],
                            stepItems: [
                                {
                                    text: Uni.I18n.translate('estimation.addEstimationRule', 'IMT', 'Add estimation rule'),
                                    itemId: 'purpose-estimation-rule-sets-add-rule-button',
                                    privileges: Cfg.privileges.Validation.admin,
                                    preventDefault: false
                                }
                            ]
                        }
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'add-estimation-rule-sets-no-items-found-panel',
                        title: Uni.I18n.translate('usagepoint.estimation.rulesSetGrid.emptyCmp.title', 'IMT', 'No estimation rule sets found'),
                        reasons: [
                            Uni.I18n.translate('usagepoint.estimation.addRulesSetGrid.emptyCmp.item2', 'IMT', 'All estimation rule sets already added'),
                            Uni.I18n.translate('usagepoint.estimation.rulesSetGrid.emptyCmp.item1', 'IMT', 'No estimation rule sets have been defined yet.')
                        ],
                        stepItems: [
                            {
                                itemId: 'metrology-config-add-estimation-rule-set-to-purpose-empty-msg-btn',
                                text: Uni.I18n.translate('estimation.addRuleSet', 'IMT', 'Add estimation rule set'),
                                privileges: Cfg.privileges.Validation.admin,
                                href: me.router.getRoute('administration/estimationrulesets/addruleset').buildUrl()
                            }
                        ]
                    }
                }
            ]
        };

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'metrology-configuration-side-menu',
                        itemId: 'metrology-configuration-side-menu-add-rule-sets',
                        router: me.router,
                        metrologyConfig: me.metrologyConfig
                    }
                ]
            }
        ];
        me.on('afterrender', function () {
            purpose.estimationRuleSets().fireEvent('load');
        }, me, {single: true});

        me.callParent(arguments);
    }
});
