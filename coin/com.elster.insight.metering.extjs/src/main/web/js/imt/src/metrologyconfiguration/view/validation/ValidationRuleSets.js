/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.metrologyconfiguration.view.validation.ValidationRuleSets', {
    extend: 'Uni.view.container.PreviewContainer',
    alias: 'widget.validation-mc-rule-sets',
    requires: [
        'Imt.metrologyconfiguration.store.PurposesWithValidationRuleSets',
        'Imt.metrologyconfiguration.view.validation.PurposeWithRuleSetsGrid',
        'Imt.metrologyconfiguration.view.validation.RulesGrid',
        'Cfg.view.validation.RulePreview',
        'Imt.metrologyconfiguration.model.ValidationRuleSet',
        'Imt.store.ValidationRules'
    ],
    router: null,
    metrologyConfig: null,
    purposes: null,
    rulesStore: null,
    selectByDefault: false,

    initComponent: function () {
        var me = this,
            data = [],
            store,
            ruleSetsCount = 0;

        if (me.purposes && me.purposes.length) {
            me.emptyComponent = {
                xtype: 'no-items-found-panel',
                itemId: 'no-validation-rule-sets-found-panel',
                title: Uni.I18n.translate('usagepoint.dataValidation.rulesSetGrid.emptyCmp.title', 'IMT', 'No validation rule sets found'),
                reasons: [
                    Uni.I18n.translate('usagepoint.dataValidation.rulesSetGrid.emptyCmp.item1', 'IMT', 'No validation rule sets have been defined yet.'),
                    Uni.I18n.translate('usagepoint.dataValidation.rulesSetGrid.emptyCmp.item2', 'IMT', 'Validation rule sets exist, but you do not have permission to view them.')
                ],
                stepItems: [
                    {
                        itemId: 'metrology-config-add-validation-rule-set-empty-msg-btn',
                        text: Uni.I18n.translate('validation.addRuleSet', 'IMT', 'Add validation rule set'),
                        privileges: Imt.privileges.MetrologyConfig.adminValidation,
                        disabled: me.metrologyConfig.get('status').id == 'deprecated',
                        href: me.router.getRoute('administration/metrologyconfiguration/view/validation/add').buildUrl()
                    }
                ]
            };

            me.purposes.forEach(function (purpose) {
                if (purpose.validationRuleSets().getCount() == 0) {
                    data.push({
                        noRuleSets: true,
                        metrologyContract: purpose.get('name'),
                        metrologyContractIsMandatory: purpose.get('mandatory'),
                        metrologyContractId: purpose.getId()
                    });
                } else {
                    purpose.validationRuleSets().each(function (validationRuleSet) {
                        data.push(Ext.merge(validationRuleSet.getData(), {
                            metrologyContract: purpose.get('name'),
                            metrologyContractIsMandatory: purpose.get('mandatory'),
                            metrologyContractId: purpose.getId(),
                            uniqueId: validationRuleSet.get('id') + ' ' + purpose.getId()
                        }));                        
                        ruleSetsCount++;
                    });
                }
            });
        } else {
            me.emptyComponent = {
                xtype: 'no-items-found-panel',
                itemId: 'no-purposes-found-panel',
                title: Uni.I18n.translate('metrologyConfigPurposes.empty.title', 'IMT', 'No purposes found'),
                reasons: [
                    Uni.I18n.translate('purposes.empty.list.item', 'IMT', 'No purposes have been added yet.')
                ]
            };
        }
        store = Ext.create('Ext.data.Store', {
            model: 'Imt.metrologyconfiguration.model.ValidationRuleSet',
            groupField: 'metrologyContract'
        });
        if (ruleSetsCount !== 0) {
            store.loadRawData(data);
        }                
        store.totalCount = ruleSetsCount;

        me.grid = {
            xtype: 'purpose-with-rule-sets-grid',
            itemId: 'purpose-with-rule-sets-grid',
            router: me.router,
            store: store,
            purposes: me.purposes,
            metrologyConfig: me.metrologyConfig
        };
        
        me.previewComponent = {
            xtype: 'preview-container',
            grid: {
                xtype: 'purpose-rules-grid',                
                router: me.router,
                itemId: 'purpose-rules-grid',
                store: me.rulesStore
            },
            previewComponent: {
                xtype: 'validation-rule-preview',
                itemId: 'purpose-rule-sets-rule-preview',
                noActionsButton: true,
                title: ''
            },
            emptyComponent: {
                xtype: 'no-items-found-panel',
                itemId: 'purpose-no-validation-rules',
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
        };
        
        me.on('afterrender', function () {
            store.fireEvent('load');
            var index = store.findBy(function (record) {                
                return record.get('id');
            });
            me.down('#purpose-with-rule-sets-grid').getSelectionModel().select(index);
        }, me, {single: true});

        me.callParent(arguments);
    }
});

