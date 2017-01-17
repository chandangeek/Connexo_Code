Ext.define('Imt.metrologyconfiguration.view.estimation.EstimationRuleSets', {
    extend: 'Uni.view.container.PreviewContainer',
    alias: 'widget.estimation-mc-rule-sets',
    requires: [
        'Imt.metrologyconfiguration.store.PurposesWithValidationRuleSets',
        'Imt.metrologyconfiguration.view.estimation.PurposeWithRuleSetsGrid',
        'Imt.metrologyconfiguration.view.estimation.RulesGrid',
        'Est.estimationrules.view.DetailForm',
        'Imt.metrologyconfiguration.model.EstimationRuleSet'
    ],
    router: null,
    metrologyConfig: null,
    purposes: null,
    rulesStore: null,
    editOrder: false,
    selectByDefault: false,

    initComponent: function () {
        var me = this,
            data = [],
            store,
            ruleSetsCount = 0;

        if (me.purposes && me.purposes.length) {
            me.emptyComponent = {
                xtype: 'no-items-found-panel',
                itemId: 'no-estimation-rule-sets-found-panel',
                title: Uni.I18n.translate('usagepoint.estimation.rulesSetGrid.emptyCmp.title', 'IMT', 'No estimation rule sets found'),
                reasons: [
                    Uni.I18n.translate('usagepoint.estimation.rulesSetGrid.emptyCmp.item1', 'IMT', 'No estimation rule sets have been defined yet.'),
                    Uni.I18n.translate('usagepoint.estimation.rulesSetGrid.emptyCmp.item2', 'IMT', 'Estimation rule sets exist, but you do not have permission to view them.')
                ],
                stepItems: [
                    {
                        itemId: 'metrology-config-add-estimation-rule-set-empty-msg-btn',
                        text: Uni.I18n.translate('estimation.addRuleSet', 'IMT', 'Add estimation rule set'),
                        privileges: Imt.privileges.MetrologyConfig.adminEstimation,
                        disabled: me.metrologyConfig.get('status').id == 'deprecated',
                        href: me.router.getRoute('administration/metrologyconfiguration/view/estimation/add').buildUrl()
                    }
                ]
            };

            me.purposes.forEach(function (purpose) {
                //Unique id for each group of estimation rule sets. Used by gridviewwithgroupsdragdrop plugin
                var hiddenGroupId = Math.random() .toString(36).replace(/[^a-z]+/g, '').substr(0, 10);
                if (purpose.estimationRuleSets().getCount() == 0) {
                    data.push({
                        noRuleSets: true,
                        metrologyContract: purpose.get('name'),
                        metrologyContractIsMandatory: purpose.get('mandatory'),
                        metrologyContractId: purpose.getId(),
                        hiddenGroupId: hiddenGroupId
                    });
                } else {
                    purpose.estimationRuleSets().each(function (estimationRuleSet) {
                        data.push(Ext.merge(estimationRuleSet.getData(), {
                            metrologyContract: purpose.get('name'),
                            metrologyContractIsMandatory: purpose.get('mandatory'),
                            metrologyContractId: purpose.getId(),
                            uniqueId: estimationRuleSet.get('id') + ' ' + purpose.getId(),
                            hiddenGroupId: hiddenGroupId
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
            xtype: 'est-purpose-with-rule-sets-grid',
            itemId: 'est-purpose-with-rule-sets-grid',
            router: me.router,
            store: store,
            purposes: me.purposes,
            editOrder: me.editOrder,
            metrologyConfig: me.metrologyConfig
        };

        me.previewComponent = {
            xtype: 'preview-container',
            grid: {
                xtype: 'est-purpose-rules-grid',
                router: me.router,
                itemId: 'est-purpose-rules-grid',
                store: me.rulesStore
            },
            previewComponent: {
                xtype: 'estimation-rules-detail-form',
                itemId: 'estimation-rules-detail-form',
                frame: true,
                noActionsButton: true,
                title: ''
            },
            emptyComponent: {
                xtype: 'no-items-found-panel',
                itemId: 'purpose-no-estimation-rules',
                title: Uni.I18n.translate('estimation.rules.empty.title', 'IMT', 'No estimation rules found'),
                reasons: [
                    Uni.I18n.translate('estimation.rules.empty.list.item1', 'IMT', 'No estimation rules have been defined yet.')
                ],
                stepItems: [
                    {
                        text: Uni.I18n.translate('estimation.addEstimationRule', 'IMT', 'Add estimation rule'),
                        itemId: 'est-purpose-rule-sets-add-rule-button',
                        privileges: Est.privileges.EstimationConfiguration.administrate,
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
            me.down('#est-purpose-with-rule-sets-grid').getSelectionModel().select(index);
        }, me, {single: true});

        me.callParent(arguments);
    }
});

