/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.metrologyconfiguration.view.validation.AddValidationRuleSetsToPurpose', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.add-validation-rule-sets-to-purpose',
    requires: [
        'Imt.metrologyconfiguration.view.MetrologyConfigurationSideMenu',
        'Imt.metrologyconfiguration.view.validation.AddValidationRuleSetsToPurposeGrid',
        'Imt.metrologyconfiguration.view.validation.RulesGrid',
        'Cfg.view.validation.RulePreview',
        'Imt.metrologyconfiguration.model.PurposeWithValidationRuleSets',
        'Imt.metrologyconfiguration.model.ValidationRuleSet',
        'Imt.store.ValidationRules'
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
            title: Uni.I18n.translate('validation.addRuleSet', 'IMT', 'Add validation rule set'),
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
                    fieldLabel: Uni.I18n.translate('validation.addRuleSet.purpose', 'IMT', 'Purpose'),
                    value: me.purposesStore.first(),
                    labelAlign: 'left',
                    forceSelection: true,
                    labelWidth: 60,
                    width: 200
                },
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'add-validation-rule-sets-to-purpose-grid',
                        itemId: 'add-validation-rule-sets-to-purpose-grid',
                        store: purpose.validationRuleSets(),
                        router: me.router,
                        bbar: [
                            {
                                xtype: 'button',
                                itemId: 'add-validation-rule-sets-to-purpose-add-button',
                                text: Uni.I18n.translate('general.add', 'IMT', 'Add'),
                                ui: 'action',
                                action: 'add',
                                width: 47,
                                margin: '80 0 -40 0',
                                disabled: true
                            },
                            {
                                xtype: 'button',
                                itemId: 'add-validation-rule-sets-to-purpose-cancel-button',
                                text: Uni.I18n.translate('general.cancel', 'IMT', 'Cancel'),
                                ui: 'link',
                                width: 80,
                                action: 'cancel',
                                margin: '80 0 -40 0',
                                href: me.router.getRoute('administration/metrologyconfiguration/view/validation').buildUrl()
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'purpose-rules-grid',
                            router: me.router,
                            itemId: 'add-validation-rule-sets-purpose-rules-grid',
                            store: me.rulesStore
                        },
                        previewComponent: {
                            xtype: 'validation-rule-preview',
                            itemId: 'add-validation-rule-sets-rule-preview',
                            noActionsButton: true,
                            title: ''
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'purpose-add-validation-rule-sets-no-validation-rules',
                            title: Uni.I18n.translate('validation.rules.empty.title', 'IMT', 'No validation rules found'),
                            reasons: [
                                Uni.I18n.translate('validation.rules.empty.list.item1', 'IMT', 'No validation rules have been defined yet.')
                            ],
                            stepItems: [
                                {
                                    text: Uni.I18n.translate('validation.addValidationRule', 'IMT', 'Add validation rule'),
                                    itemId: 'purpose-rule-sets-add-rule-button',
                                    privileges: Cfg.privileges.Validation.admin,
                                    preventDefault: false
                                }
                            ]
                        }
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'add-validation-rule-sets-no-items-found-panel',
                        title: Uni.I18n.translate('usagepoint.dataValidation.rulesSetGrid.emptyCmp.title', 'IMT', 'No validation rule sets found'),
                        reasons: [
                            Uni.I18n.translate('usagepoint.dataValidation.addRulesSetGrid.emptyCmp.item2', 'IMT', 'All validation rule sets already added'),
                            Uni.I18n.translate('usagepoint.dataValidation.rulesSetGrid.emptyCmp.item1', 'IMT', 'No validation rule sets have been defined yet.')
                        ],
                        stepItems: [
                            {
                                itemId: 'metrology-config-add-validation-rule-set-to-purpose-empty-msg-btn',
                                text: Uni.I18n.translate('validation.addRuleSet', 'IMT', 'Add validation rule set'),
                                privileges: Cfg.privileges.Validation.admin,
                                href: me.router.getRoute('administration/rulesets/add').buildUrl()
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
            purpose.validationRuleSets().fireEvent('load');
        }, me, {single: true});

        me.callParent(arguments);
    }
});
