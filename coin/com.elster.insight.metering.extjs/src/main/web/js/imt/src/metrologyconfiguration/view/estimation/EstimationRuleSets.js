Ext.define('Imt.metrologyconfiguration.view.estimation.EstimationRuleSets', {
    extend: 'Uni.view.container.PreviewContainer',
    alias: 'widget.estimation-mc-rule-sets',
    requires: [
        'Imt.metrologyconfiguration.store.PurposesWithValidationRuleSets',
        // 'Imt.metrologyconfiguration.view.validation.PurposeWithRuleSetsGrid',
        // 'Imt.metrologyconfiguration.view.estimation.RulesGrid',
        'Cfg.view.validation.RulePreview',
        'Imt.metrologyconfiguration.model.EstimationRuleSet',
        // 'Imt.store.ValidationRules'
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
                title: Uni.I18n.translate('usagepoint.dataEstimation.rulesSetGrid.emptyCmp.title', 'IMT', 'No estimation rule sets found'),
                reasons: [
                    Uni.I18n.translate('usagepoint.dataEstimation.rulesSetGrid.emptyCmp.item1', 'IMT', 'No estimation rule sets have been defined yet.'),
                    Uni.I18n.translate('usagepoint.dataEstimation.rulesSetGrid.emptyCmp.item2', 'IMT', 'Estimation rule sets exist, but you do not have permission to view them.')
                ],
                stepItems: [
                    {
                        itemId: 'metrology-config-add-estimation-rule-set-empty-msg-btn',
                        text: Uni.I18n.translate('validation.addRuleSet', 'IMT', 'Add estimation rule set'),
                        privileges: Imt.privileges.MetrologyConfig.adminValidation,
                        disabled: me.metrologyConfig.get('status').id == 'deprecated',
                        href: me.router.getRoute('administration/metrologyconfiguration/view/estimation/add').buildUrl()
                    }
                ]
            };

            me.purposes.forEach(function (purpose) {
                if (purpose.estimationRuleSets().getCount() == 0) {
                    data.push({
                        noRuleSets: true,
                        metrologyContract: purpose.get('name'),
                        metrologyContractIsMandatory: purpose.get('mandatory'),
                        metrologyContractId: purpose.getId()
                    });
                } else {
                    purpose.estimationRuleSets().each(function (estimationRuleSet) {
                        data.push(Ext.merge(estimationRuleSet.getData(), {
                            metrologyContract: purpose.get('name'),
                            metrologyContractIsMandatory: purpose.get('mandatory'),
                            metrologyContractId: purpose.getId(),
                            uniqueId: estimationRuleSet.get('id') + ' ' + purpose.getId()
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
            model: 'Imt.metrologyconfiguration.model.EstimationRuleSet',
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
                // xtype: 'estimation-rule-preview',
                // itemId: 'purpose-rule-sets-rule-preview',
                // noActionsButton: true,
                // title: ''
            },
            emptyComponent: {
                xtype: 'no-items-found-panel',
                itemId: 'purpose-no-estimation-rules',
                title: Uni.I18n.translate('validation.rules.empty.title', 'IMT', 'No estimation rules found'),
                reasons: [
                    Uni.I18n.translate('validation.rules.empty.list.item1', 'IMT', 'No estimation rules have been defined yet.')
                ],
                stepItems: [
                    {
                        text: Uni.I18n.translate('validation.addEstimationRule', 'IMT', 'Add estimation rule'),
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

